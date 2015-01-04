package reactmann;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;

/**
 * @author blake
 */
public class NetSocketException extends RuntimeException {

    private final WriteStream<Buffer> socket;

    public NetSocketException(WriteStream<Buffer> socket, Throwable cause) {
        super(cause);
        this.socket = socket;
    }

    public WriteStream<Buffer> getSocket() {
        return socket;
    }
}
