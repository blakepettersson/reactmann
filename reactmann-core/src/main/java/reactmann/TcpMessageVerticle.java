package reactmann;

import com.aphyr.riemann.Proto;
import io.vertx.rxcore.java.RxVertx;
import io.vertx.rxcore.java.http.RxHttpServer;
import io.vertx.rxcore.java.http.RxServerWebSocket;
import io.vertx.rxcore.java.net.RxNetServer;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.streams.WriteStream;
import org.vertx.java.platform.Verticle;
import rx.Subscription;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class TcpMessageVerticle extends Verticle {

   public void start() {
      RxVertx rxVertx = new RxVertx(vertx);
      RxNetServer netServer = rxVertx.createNetServer();
      RxHttpServer httpServer = rxVertx.createHttpServer();

      httpServer.websocket().map(s -> {
         try {
            List<NameValuePair> query = URLEncodedUtils.parse(new URI(s.uri()), "UTF-8");
            NameValuePair nameValuePair = query.stream().filter(p -> "query".equals(p.getName())).findAny().get();
            return Tup2.create(s, Query.parse(nameValuePair.getValue()));
         } catch (URISyntaxException e) {
            throw new NetSocketException(s, e);
         }
      }).subscribe(r -> {
         RxServerWebSocket socket = r.getLeft();

         Subscription subscription = Riemann.getIndex(vertx)
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
            .subscribe(json -> socket.writeTextFrame(json.encode()));

         socket.closeHandler(h -> subscription.unsubscribe());
      }, e -> {
         container.logger().error(e);

         //TODO: Fix proper error handling
      });

      netServer
         .connectStream()
         .flatMap(s -> Riemann.convertBufferStreamToMessages(s.coreSocket(), s.asObservable()))
         .subscribe(s -> {
            sendResponse(Proto.Msg.newBuilder().setOk(true).build(), s.getLeft());
            vertx.eventBus().publish("riemann.stream", s.getRight().toByteArray());
         }, e -> {
            container.logger().error(e);

            if (e instanceof NetSocketException) {
               sendResponse(Proto.Msg.newBuilder().setError(e.getMessage()).build(), ((NetSocketException) e).getSocket());
            }
         });

      netServer.coreServer().listen(5555);
      httpServer.coreHttpServer().listen(5556);
      container.logger().info("Started TCP listener at port 5555");
   }

   private void sendResponse(Proto.Msg msg, WriteStream<?> sock) {
      byte[] bytes = msg.toByteArray();
      Buffer response = new Buffer();
      response.appendInt(bytes.length);
      response.appendBytes(bytes);
      sock.write(response);
   }
}
