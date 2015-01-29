package reactmann;

import io.vertx.core.AbstractVerticle;
import rx.Observable;

/**
 * @author blake
 */
public abstract class RiemannVerticle extends AbstractVerticle {
    private Index index;

    @Override
    public void start() {
        vertx.deployVerticle("java:" + WebSocketVerticle.class.getName(), webSocketCallback -> {
            vertx.deployVerticle("java:" + TcpMessageVerticle.class.getName(), tcpMessageCallback -> observeStream(Riemann.getEvents(vertx)));
            if(index == null) {
                index = new Index(vertx);
            }
        });
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public void index(Event event) {
        index.put(Tup2.create(event.getHost(), event.getService()), event);
    }

    public abstract void observeStream(Observable<Event> events);
}
