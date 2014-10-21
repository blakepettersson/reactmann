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
import io.vertx.ext.rxjava.ObservableFuture;
import io.vertx.ext.rxjava.RxHelper;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import rx.Subscription;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class TcpMessageVerticle extends AbstractVerticle {
    Logger log = LoggerFactory.getLogger(TcpMessageVerticle.class);

    public void start() {
        ObservableFuture<NetServer> foo = RxHelper.observableFuture();
        NetServer netServer = vertx.createNetServer(new NetServerOptions().setPort(5555));
        foo.subscribe(a -> {
        }, e -> {

        }, () -> {

        });

        ObservableFuture<HttpServer> bla = RxHelper.observableFuture();
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
                    .map(e -> new JsonObject()
                            .putArray("tags", new JsonArray(e.getTags().toArray()))
                            .putString("host", e.getHost())
                            .putString("state", e.getState())
                            .putString("service", e.getService())
                            .putString("description", e.getDescription())
                            .putNumber("metric", e.getMetric())
                            .putNumber("time", e.getTime())
                            .putNumber("ttl", e.getTtl()))
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

        netServer.listen(foo);
        httpServer.listen(bla);
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
