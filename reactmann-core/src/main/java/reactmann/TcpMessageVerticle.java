package reactmann;

import com.aphyr.riemann.Proto;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.impl.ws.WebSocketFrameImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.rx.java.ObservableHandler;
import io.vertx.ext.rx.java.RxHelper;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import rx.Subscription;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class TcpMessageVerticle extends AbstractVerticle {
    Logger log = LoggerFactory.getLogger(TcpMessageVerticle.class);

    public void start() {
        ObservableHandler<NetServer> foo = RxHelper.observableHandler();
        NetServer netServer = vertx.createNetServer(new NetServerOptions().setPort(5555));
        foo.subscribe(a -> {
        }, e -> {

        }, () -> {

        });

        ObservableHandler<HttpServer> bla = RxHelper.observableHandler();
        HttpServer httpServer = vertx.createHttpServer(new HttpServerOptions().setPort(5556));
        bla.subscribe(a -> {
        }, e -> {

        }, () -> {

        });

        RxHelper.toObservable(httpServer.websocketStream()).map(s -> {
            try {
                List<NameValuePair> query = URLEncodedUtils.parse(new URI(s.uri()), "UTF-8");
                NameValuePair nameValuePair = query.stream().filter(p -> "query".equals(p.getName())).findAny().get();
                return Tup2.create(s, Query.parse(nameValuePair.getValue()));
            } catch (URISyntaxException e) {
                throw new NetSocketException(s, e);
            }
        }).subscribe(r -> {
            ServerWebSocket socket = r.getLeft();
            Subscription subscription = Riemann.getEvents(vertx)
                    .filter(r.getRight())
                    .map(e -> {
                          return new JsonObject()
                             .put("tags", new JsonArray().add(e.getTags().toArray()))
                             .put("host", e.getHost())
                             .put("state", e.getState())
                             .put("service", e.getService())
                             .put("description", e.getDescription())
                             .put("metric", e.getMetric())
                             .put("time", e.getTime())
                             .put("ttl", e.getTtl());
                       }
                    )
                    .subscribe(json -> socket.writeFrame(new WebSocketFrameImpl(json.encode())));

            socket.closeHandler(h -> subscription.unsubscribe());
        }, e -> {
            log.error(e);
            //TODO: Fix proper error handling
        });


        RxHelper.toObservable(netServer.connectStream())
                .flatMap(s -> Riemann.convertBufferStreamToMessages(s, RxHelper.toObservable(s)))
                .subscribe(s -> {
                    sendResponse(Proto.Msg.newBuilder().setOk(true).build(), s.getLeft());
                    vertx.eventBus().publish("riemann.stream", s.getRight().toByteArray());
                }, e -> {
                    log.error(e);

                    if (e instanceof NetSocketException) {
                        sendResponse(Proto.Msg.newBuilder().setError(e.getMessage()).build(), ((NetSocketException) e).getSocket());
                    }
                });

        netServer.listen(foo.asHandler());
        httpServer.listen(bla.asHandler());
        //container.logger().info("Started TCP listener at port 5555");
    }

    private void sendResponse(Proto.Msg msg, WriteStream<Buffer> sock) {
        byte[] bytes = msg.toByteArray();
        Buffer response = Buffer.buffer();
        response.appendInt(bytes.length);
        response.appendBytes(bytes);
        sock.write(response);
    }
}
