package reactmann;

import com.aphyr.riemann.Proto;
import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import io.vertx.rx.java.RxHelper;
import reactmann.subscribers.BufferAction;
import rx.Observable;

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
        }).map(e -> Event.builder().fromProtoBufEvent(e).build());
    }

    public static <T extends WriteStream<Buffer>> Observable<Tup2<T, Proto.Msg>> convertBufferStreamToMessages(T socket, Observable<Buffer> observable) {
        return Observable.create(new BufferAction(observable)).map(r -> {
            try {
                return Proto.Msg.parseFrom(r.getBytes(4, r.length()));
            } catch (Exception e) {
                throw new NetSocketException(socket, e);
            }
        }).map(b -> Tup2.create(socket, b));
    }
}
