package reactmann;

import com.aphyr.riemann.Proto;
import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.rxcore.java.RxVertx;
import org.vertx.java.platform.Verticle;
import rx.Observable;

/**
 * @author blake
 */
public abstract class RiemannVerticle extends Verticle {
   @Override
   public void start() {
      container.deployModule("reactmann~reactmann-core~1.0-SNAPSHOT", event -> {
         Observable<Proto.Event> events = new RxVertx(vertx).eventBus().registerHandler("riemann.stream").flatMap(m -> {
            try {
               return Observable.from(Proto.Msg.parseFrom((byte[]) m.body()).getEventsList());
            } catch (InvalidProtocolBufferException e) {
               throw new RuntimeException(e);
            }
         });
         observeStream(events);
      });
   }

   public abstract void observeStream(Observable<Proto.Event> events);
}
