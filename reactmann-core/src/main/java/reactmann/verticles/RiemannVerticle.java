package reactmann.verticles;

import io.vertx.core.AbstractVerticle;
import reactmann.*;
import rx.Observable;

/**
 * @author blake
 */
public abstract class RiemannVerticle extends AbstractVerticle {

    @Override
    public void start() {
        vertx.eventBus().registerDefaultCodec(Event.class, new EventMessageCodec());
        vertx.deployVerticle("java:" + IndexVerticle.class.getName(), indexCallback -> {
            vertx.deployVerticle("java:" + WebSocketVerticle.class.getName(), webSocketCallback -> {
                vertx.deployVerticle("java:" + TcpMessageVerticle.class.getName(), tcpMessageCallback -> {
                    observeStream(Riemann.getEvents(vertx, EventType.STREAM));
                });
            });
        });
    }

    public final void index(Event event) {
        vertx.eventBus().publish("riemann.addToIndex", event);
    }

    public abstract void observeStream(Observable<Event> events);
}
