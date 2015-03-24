package reactmann;

import com.aphyr.riemann.Proto;
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

    public static Observable<Event> getEvents(Vertx vertx, EventType eventType) {
        return RxHelper.toObservable(vertx.eventBus().consumer(eventType.getAddress()).bodyStream()).map(o -> (Event)o);
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
