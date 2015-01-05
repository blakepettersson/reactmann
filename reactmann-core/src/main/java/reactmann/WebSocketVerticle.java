package reactmann;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.impl.ws.WebSocketFrameImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import reactmann.observables.EventObservable;
import rx.Subscription;

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

        EventObservable.convertFromWebSocketObservable(RxHelper.toObservable(httpServer.websocketStream())).subscribe(r -> {
            ServerWebSocket socket = r.getLeft();
            Subscription subscription = Riemann.getEvents(vertx)
                    .filter(r.getRight())
                    .map(e -> new JsonObject()
                                    .put("tags", new JsonArray().add(e.getTags().toArray()))
                                    .put("host", e.getHost())
                                    .put("state", e.getState())
                                    .put("service", e.getService())
                                    .put("description", e.getDescription())
                                    .put("metric", e.getMetric())
                                    .put("time", e.getTime())
                                    .put("ttl", e.getTtl())
                    )
                    .subscribe(json -> socket.writeFrame(new WebSocketFrameImpl(json.encode())));

            socket.closeHandler(h -> subscription.unsubscribe());
        }, e -> {
            log.error(e);
            //TODO: Fix proper error handling
        });

        httpServer.listen(httpServerObservable.asHandler());
    }
}
