package reactmann;

import org.vertx.java.platform.Verticle;
import rx.Observable;

/**
 * @author blake
 */
public abstract class RiemannVerticle extends Verticle {
   private Index index;

   @Override
   public void start() {
      container.deployModule("reactmann~reactmann-core~1.0-SNAPSHOT", event -> {
         index = new Index(vertx);
         observeStream(Riemann.getEvents(vertx));
      });
   }

   public void index(Event event) {
      index.put(Tup2.create(event.getHost(), event.getService()), event);
   }

   public abstract void observeStream(Observable<Event> events);
}
