package reactmann;

import com.aphyr.riemann.Proto;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.core.streams.Pump;
import org.vertx.java.platform.Verticle;

public class TcpMessageVerticle extends Verticle {
   
   public void start() {
      container.logger().info("Starting TCP listener at port 5555");
      vertx.createNetServer().connectHandler(sock -> {
         Pump.createPump(sock, sock).start();

         sock.dataHandler(new Handler<Buffer>() {

            Buffer received;

            @Override
            public void handle(Buffer buffer) {
               if (received == null) {
                  received = new Buffer();
               }
               received.appendBuffer(buffer);

               int size = received.getInt(0);
               int expectedSize = size + 4;

               if (received.length() == expectedSize) {
                  try {
                     byte[] protobuf = received.getBytes(4, expectedSize);

                     //Check to see that message can be parsed.
                     Proto.Msg.parseFrom(protobuf);

                     sendResponse(Proto.Msg.newBuilder().setOk(true).build(), sock);

                     vertx.eventBus().publish("riemann.stream", protobuf);

                  } catch (Exception e) {
                     sendResponse(Proto.Msg.newBuilder().setError(e.getMessage()).build(), sock);
                     container.logger().error(e);
                  }

                  received = null;
               }
            }
         });
      }).listen(5555);
   }

   private void sendResponse(Proto.Msg msg, NetSocket sock) {
      byte[] bytes = msg.toByteArray();
      Buffer response = new Buffer();
      response.appendInt(bytes.length);
      response.appendBytes(bytes);
      sock.write(response);
   }
}
