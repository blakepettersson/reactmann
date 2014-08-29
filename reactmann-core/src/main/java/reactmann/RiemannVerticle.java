package reactmann;

import org.vertx.java.platform.Verticle;
import rx.Observable;

/**
 * @author blake
 */
public abstract class RiemannVerticle extends Verticle {
   @Override
   public void start() {
      container.deployModule("reactmann~reactmann-core~1.0-SNAPSHOT", event -> observeStream(Riemann.getEvents(vertx)));
   }

   public abstract void observeStream(Observable<Event> events);
}
