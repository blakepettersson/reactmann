package reactmann;

import com.aphyr.riemann.Proto;
import io.vertx.rxcore.java.RxVertx;
import io.vertx.rxcore.java.net.RxNetServer;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.platform.Verticle;

public class TcpMessageVerticle extends Verticle {

   public void start() {
      /*
      vertx.createHttpServer().websocketHandler(sock -> {
         Pump.createPump(sock, sock).start();

         Riemann.getEvents(vertx).forEach(e -> {
            JsonObject obj = new JsonObject()
               .putArray("tags", new JsonArray(e.getTags().toArray()))
               .putString("host", e.getHost())
               .putString("state", e.getState())
               .putString("service", e.getService())
               .putString("description", e.getDescription())
               .putNumber("metric", e.getMetric())
               .putNumber("metric_f", e.getMetricD())
               .putNumber("time", e.getTime())
               .putNumber("ttl", e.getTtl());

            sock.writeTextFrame(obj.encode());
         });
      }).listen(5556);
      */

      RxVertx rxVertx = new RxVertx(vertx);

      RxNetServer netServer = rxVertx.createNetServer();

      netServer
         .connectStream()
         .flatMap(s -> Riemann.convertBufferStreamToMessages(s.coreSocket(), s.asObservable()))
         .subscribe(s -> {
            sendResponse(Proto.Msg.newBuilder().setOk(true).build(), s.getLeft());
            vertx.eventBus().publish("riemann.stream", s.getRight().toByteArray());
         });

      netServer.coreServer().listen(5555);
      container.logger().info("Started TCP listener at port 5555");
   }

   private void sendResponse(Proto.Msg msg, NetSocket sock) {
      byte[] bytes = msg.toByteArray();
      Buffer response = new Buffer();
      response.appendInt(bytes.length);
      response.appendBytes(bytes);
      sock.write(response);
   }
}
