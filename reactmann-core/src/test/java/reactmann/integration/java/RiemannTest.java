package reactmann.integration.java;

import com.aphyr.riemann.Proto;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;
import reactmann.Event;
import reactmann.Riemann;
import rx.Observable;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class RiemannTest extends VertxTestBase {
    @Test
    public void testGetEvents() {
        Event event = new Event("test", "test", "test", "test", null, 1, 1.0F, 1.0);
        Riemann.getEvents(vertx).forEach(e -> {
            assertEquals(e, event);
            testComplete();
        });

        Proto.Msg.Builder builder = Proto.Msg.newBuilder(Proto.Msg.getDefaultInstance()).addEvents(event.toProtoBufEvent());

        vertx.eventBus().send("riemann.stream", builder.build().toByteArray());

        await();
    }

    @Test
    public void testConvertBufferStreamToMessages() {
        Event event = new Event("test", "test", "test", "test", null, 1, 1.0F, 1.0);
        byte[] bytes = Proto.Msg.newBuilder(Proto.Msg.getDefaultInstance()).addEvents(event.toProtoBufEvent()).build().toByteArray();

        Riemann.convertBufferStreamToMessages(mock(NetSocket.class), Observable.just(
                Buffer.buffer().appendInt(bytes.length),
                Buffer.buffer().appendBytes(Arrays.copyOfRange(bytes, 0, 22)),
                Buffer.buffer().appendBytes(Arrays.copyOfRange(bytes, 22, 42))
        )).forEach(t -> {
            Proto.Event e = t.getRight().getEventsList().get(0);
            assertEquals(Event.fromProtoBufEvent(e), event);
            testComplete();
        });

        await();
    }

}