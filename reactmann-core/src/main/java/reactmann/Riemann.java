package reactmann;

import com.aphyr.riemann.Proto;
import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import io.vertx.rx.java.RxHelper;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author blake
 */
public class Riemann {
    private Riemann() {
    }

    public static Observable<Event> getEvents(Vertx vertx) {
        return RxHelper.toObservable(vertx.eventBus().consumer("riemann.stream").bodyStream()).flatMap(m -> {
            try {
                //noinspection unchecked
                return Observable.from(Proto.Msg.parseFrom((byte[]) m).getEventsList());
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }).map(Event::fromProtoBufEvent);
    }

    public static <T extends WriteStream<Buffer>> Observable<Tup2<T, Proto.Msg>> convertBufferStreamToMessages(T socket, Observable<Buffer> observable) {
        List<Buffer> buffers = new ArrayList<>();
        Observable.OnSubscribe<Buffer> bufferUntilEverythingHasBeenReceived = (subscriber) -> observable.subscribe(buffer -> {
            buffers.add(buffer);
            long size = buffers.stream().mapToLong(Buffer::length).sum();
            if (buffers.get(0).getInt(0) + 4 == size) {
                subscriber.onNext(buffers.stream().reduce(Buffer.buffer(), (a, b) -> a.appendBuffer(b)));
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
