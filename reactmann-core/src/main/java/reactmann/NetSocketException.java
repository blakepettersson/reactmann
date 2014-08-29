package reactmann;

import org.vertx.java.core.streams.WriteStream;

/**
 * @author blake
 */
public class NetSocketException extends RuntimeException {

   private final WriteStream<?> socket;

   public NetSocketException(WriteStream<?> socket, Throwable cause) {
      super(cause);
      this.socket = socket;
   }

   public WriteStream<?> getSocket() {
      return socket;
   }
}
