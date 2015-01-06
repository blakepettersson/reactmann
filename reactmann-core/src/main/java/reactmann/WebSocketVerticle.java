package reactmann;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.impl.ws.WebSocketFrameImpl;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import reactmann.observables.EventObservable;
import reactmann.subscribers.EventToJsonAction;
import rx.Observable;
import rx.functions.Func1;

/**
 * @author blake
 */
public class WebSocketVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(WebSocketVerticle.class);

    @Override
    public void start() throws Exception {
        ObservableFuture<HttpServer> httpServerObservable = RxHelper.observableFuture();
        HttpServer httpServer = vertx.createHttpServer(new HttpServerOptions().setPort(5556));
        httpServerObservable.subscribe(a -> {
            log.info("Started web socket listener at port 5556");
        }, e -> {

        }, () -> {

        });

        Observable<Tup2<ServerWebSocket, Func1<Event, Boolean>>> eventObservable = EventObservable.convertFromWebSocketObservable(RxHelper.toObservable(httpServer.websocketStream()));
        eventObservable.subscribe(new EventToJsonAction(Riemann.getEvents(vertx), WebSocketFrameImpl::new), e -> {
            log.error(e);
            //TODO: Fix proper error handling
        });

        httpServer.listen(httpServerObservable.asHandler());
    }
}
