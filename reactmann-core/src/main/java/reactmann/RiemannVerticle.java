package reactmann;

import io.vertx.core.AbstractVerticle;
import rx.Observable;

/**
 * @author blake
 */
public abstract class RiemannVerticle extends AbstractVerticle {
    @Override
    public void start() {
        vertx.deployVerticle("java:reactmann.TcpMessageVerticle", event -> observeStream(Riemann.getEvents(vertx)));
    }

    public abstract void observeStream(Observable<Event> events);
}
