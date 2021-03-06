package reactmann;

import com.aphyr.riemann.Proto;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.streams.WriteStream;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;

public class TcpMessageVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(TcpMessageVerticle.class);

    public void start() {
        //TODO: Fix a better way of configuration other than system properties?
        Integer port = Integer.getInteger("tcp.port", 5555);

        ObservableFuture<NetServer> netServerObservable = RxHelper.observableFuture();
        NetServer netServer = vertx.createNetServer(new NetServerOptions().setPort(port));
        netServerObservable.subscribe(a ->
                log.info("Starting TCP listener.."),
                e -> log.error("Could not start TCP listener on port " + port, e),
                () -> log.info("Started TCP listener on port " + port + ".")
        );

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

        netServer.listen(netServerObservable.asHandler());
    }

    private void sendResponse(Proto.Msg msg, WriteStream<Buffer> sock) {
        byte[] bytes = msg.toByteArray();
        Buffer response = Buffer.buffer();
        response.appendInt(bytes.length);
        response.appendBytes(bytes);
        sock.write(response);
    }
}
