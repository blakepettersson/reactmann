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
import static org.mockito.Mockito.mock;

public class RiemannTest extends VertxTestBase {
    @Test
    public void testGetEvents() {
        Event event = new Event("test", "test", "test", "test", null, null, 1, 1.0F, 1.0);
        Riemann.getEvents(vertx).forEach(e -> {
            assertEquals(e, event);
            testComplete();
        });

        vertx.eventBus().send("riemann.stream", event.toProtoBufMessage().toByteArray());

        await();
    }

    @Test
    public void testConvertBufferStreamToMessages() {
        Event event = new Event("test", "test", "test", "test", null, null, 1, 1.0F, 1.0);
        byte[] bytes = event.toProtoBufMessage().toByteArray();

        Riemann.convertBufferStreamToMessages(mock(NetSocket.class), Observable.just(
                Buffer.buffer().appendInt(bytes.length),
                Buffer.buffer().appendBytes(Arrays.copyOfRange(bytes, 0, 22)),
                Buffer.buffer().appendBytes(Arrays.copyOfRange(bytes, 22, 42))
        )).forEach(t -> {
            Proto.Event e = t.getRight().getEventsList().get(0);
            assertEquals(Event.builder().fromProtoBufEvent(e).build(), event);
            testComplete();
        });

        await();
    }

    @Test
    public void testConvertBufferStreamToMessagesWithMultipleEvents() {
        Event event = new Event("test", "test", "test", "test", null, null, 1, 1.0F, 1.0);
        Event secondEvent = new Event("test2", "test2", "test2", "test2", null, null, 1, 1.0F, 1.0);
        byte[] bytes = event.toProtoBufMessage().toByteArray();
        byte[] secondBytes = secondEvent.toProtoBufMessage().toByteArray();

        byte[] array = ByteBuffer
                .allocate(34)
                .put(Arrays.copyOfRange(bytes, 22, 42))
                .putInt(secondBytes.length)
                .put(Arrays.copyOfRange(secondBytes, 0, 10))
                .array();

        Riemann.convertBufferStreamToMessages(mock(NetSocket.class), Observable.just(
                Buffer.buffer().appendInt(bytes.length),
                Buffer.buffer().appendBytes(Arrays.copyOfRange(bytes, 0, 22)),
                Buffer.buffer().appendBytes(array),
                Buffer.buffer().appendBytes(Arrays.copyOfRange(secondBytes, 10, 46))

        )).buffer(2).forEach(t -> {
            Proto.Event first = t.get(0).getRight().getEventsList().get(0);
            assertEquals(Event.builder().fromProtoBufEvent(first).build(), event);

            Proto.Event second = t.get(1).getRight().getEventsList().get(0);
            assertEquals(Event.builder().fromProtoBufEvent(second).build(), secondEvent);
            testComplete();
        });

        await();
    }

}