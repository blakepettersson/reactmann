package reactmann.subscribers;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.Arguments;

public class RiemannParser implements Handler<Buffer> {
    private Buffer buff;
    private int start;          // Position of beginning of current record
    private boolean reset;      // Allows user to toggle mode / change delim when records are emitted

    private int recordSize = -1;
    private Handler<Buffer> output;

    private RiemannParser(Handler<Buffer> output) {
        this.output = output;
    }

    /**
     * Create a new {@code RecordParser} instance, initially in fixed size mode, and where the record size is specified
     * by the {@code size} parameter.<p>
     * {@code output} Will receive whole records which have been parsed.
     */
    public static RiemannParser newFixed(Handler<Buffer> output) {
        return new RiemannParser(output);
    }

    /**
     * Flip the parser into fixed size mode, where the record size is specified by {@code size} in bytes.<p>
     * This method can be called multiple times with different values of size while data is being parsed.
     */
    private void fixedSizeMode(int size) {
        Arguments.require(size > 0, "Size must be > 0");
        recordSize = size;
        reset = true;
    }

    private void handleParsing() {
        int len = buff.length();
        do {
            reset = false;
            parseFixed();
        } while (reset);

        if (start == len) {
            //Nothing left
            buff = null;
            recordSize = -1;
        } else {
            buff = buff.getBuffer(start, len);
            fixedSizeMode(buff.getInt(0) + 4);
        }
        start = 0;
    }

    private void parseFixed() {
        int len = buff.length();
        while (len - start >= recordSize && !reset) {
            int end = start + recordSize;
            Buffer ret = buff.getBuffer(start, end);
            start = end;
            output.handle(ret);
        }
    }

    /**
     * This method is called to provide the parser with data.
     * @param buffer
     */
    public void handle(Buffer buffer) {
        if (buff == null) {
            buff = buffer;
        } else {
            buff.appendBuffer(buffer);
        }

        if(recordSize == -1) {
            fixedSizeMode(buff.getInt(0) + 4);
        }

        handleParsing();
    }
}
