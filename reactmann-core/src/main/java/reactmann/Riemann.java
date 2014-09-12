package reactmann;

import com.aphyr.riemann.Proto;
import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.rxcore.java.RxVertx;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.streams.WriteStream;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author blake
 */
public class Riemann {
   private Riemann() {
   }

   public static Observable<Event> getIndex(Vertx vertx) {
      return Observable.concat(Observable.from(new Index(vertx).values()), new RxVertx(vertx).eventBus().registerHandler("riemann.index").flatMap(m -> {
            try {
               Event event = Event.fromProtoBufEvent(Proto.Event.parseFrom((byte[]) m.body()));
               return Observable.just(event);
            } catch (InvalidProtocolBufferException e) {
               throw new RuntimeException(e);
            }
         }
      ));
   }

   public static Observable<Event> getEvents(Vertx vertx) {
      return new RxVertx(vertx).eventBus().registerHandler("riemann.stream").flatMap(m -> {
         try {
            //noinspection unchecked
            return Observable.from(Proto.Msg.parseFrom((byte[]) m.body()).getEventsList());
         } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
         }
      }).map(Event::fromProtoBufEvent);
   }

   public static <T extends WriteStream<T>> Observable<Tup2<T, Proto.Msg>> convertBufferStreamToMessages(T socket, Observable<Buffer> observable) {
      List<Buffer> buffers = new ArrayList<>();
      Observable.OnSubscribe<Buffer> bufferUntilEverythingHasBeenReceived = (subscriber) -> observable.subscribe(buffer -> {
         buffers.add(buffer);
         long size = buffers.stream().mapToLong(Buffer::length).sum();
         if (buffers.get(0).getInt(0) + 4 == size) {
            subscriber.onNext(buffers.stream().reduce(new Buffer(), (a, b) -> a.appendBuffer(b)));
            buffers.clear();
         }
      });

      return Observable.create(bufferUntilEverythingHasBeenReceived).map(r -> {
         try {
            return Proto.Msg.parseFrom(r.getBytes(4, r.length()));
         } catch (Exception e) {
            throw new NetSocketException(socket, e);
         }
      }).map(b -> Tup2.create(socket, b));
   }
}
