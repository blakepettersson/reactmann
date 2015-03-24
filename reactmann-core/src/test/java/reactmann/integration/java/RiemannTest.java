package reactmann.integration.java;

import com.aphyr.riemann.Proto;
import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;
import reactmann.Event;
import reactmann.EventMessageCodec;
import reactmann.EventType;
import reactmann.Riemann;
import rx.Observable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class RiemannTest extends VertxTestBase {
    @Test
    public void testGetEvents() {
        vertx.eventBus().registerDefaultCodec(Event.class, new EventMessageCodec());
        Event event = new Event("test", "test", "test", "test", null, null, 1, 1.0F, 1.0);
        Riemann.getEvents(vertx, EventType.STREAM).forEach(e -> {
            assertEquals(e, event);
            testComplete();
        });

        vertx.eventBus().send(EventType.STREAM.getAddress(), event);

        await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testConvertBufferStreamToMessages() throws InvalidProtocolBufferException {
        Event event = new Event("test", "test", "test", "test", null, null, 1, 1.0F, 1.0);
        byte[] bytes = toProtoBufMessage(event).toByteArray();

        Riemann.convertBufferStreamToMessages(mock(NetSocket.class), Observable.just(
                Buffer.buffer().appendInt(bytes.length),
                Buffer.buffer().appendBytes(Arrays.copyOfRange(bytes, 0, 22)),
                Buffer.buffer().appendBytes(Arrays.copyOfRange(bytes, 22, 42))
        )).forEach(t -> {
            Proto.Event e = t.getRight().getEventsList().get(0);
            assertEquals(Event.builder().fromProtoBufEvent(e).build(), event);
            testComplete();
        });

        await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testConvertBufferStreamToMessagesWithMultipleEvents() throws InvalidProtocolBufferException {
        Event event = new Event("test", "test", "test", "test", null, null, 1, 1.0F, 1.0);
        Event secondEvent = new Event("test2", "test2", "test2", "test2", null, null, 1, 1.0F, 1.0);
        byte[] bytes = toProtoBufMessage(event).toByteArray();
        byte[] secondBytes = toProtoBufMessage(secondEvent).toByteArray();

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

        await(5, TimeUnit.SECONDS);
    }


    private Proto.Msg toProtoBufMessage(Event event) throws InvalidProtocolBufferException {
        Buffer buffer = Buffer.buffer();
        new EventMessageCodec().encodeToWire(buffer, event);
        Proto.Event protobufEvent = Proto.Event.parseFrom(buffer.getBytes(4, buffer.length()));
        return Proto.Msg.newBuilder(Proto.Msg.getDefaultInstance()).addEvents(protobufEvent).build();
    }
}