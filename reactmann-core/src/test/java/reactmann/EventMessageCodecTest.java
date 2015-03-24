package reactmann;

import com.aphyr.riemann.Proto;
import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.core.buffer.Buffer;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class EventMessageCodecTest {

    @Test
    public void testEncodeToWire() throws Exception {
        Buffer buffer = Buffer.buffer();
        Event event = Event.builder().build();

        assertEquals(0, buffer.length());
        new EventMessageCodec().encodeToWire(buffer, event);
        assertEquals(33, buffer.length());
        assertEquals(buffer.length() - 4, buffer.getInt(0));
    }

    @Test
    public void testDecodeFromWire() throws Exception {
        Buffer buffer = Buffer.buffer();
        Event event1 = Event.builder().build();
        EventMessageCodec eventMessageCodec = new EventMessageCodec();
        eventMessageCodec.encodeToWire(buffer, event1);
        Event event2 = eventMessageCodec.decodeFromWire(0, buffer);

        assertEquals(event1, event2);
    }

    @Test
    public void testToProtoBuf() throws InvalidProtocolBufferException {
        Buffer buffer = Buffer.buffer();
        Event event = new Event("", "", "", "", null, null, 0, 0.0F, 0.0);
        new EventMessageCodec().encodeToWire(buffer, event);

        Proto.Event expected = Proto.Event.parseFrom(buffer.getBytes(4, buffer.length()));
        Proto.Event actual = Proto.Event.getDefaultInstance();
        assertEquals(expected.getTtl(), actual.getTtl(), 0.01);
        assertEquals(expected.getMetricF(), actual.getMetricF(), 0.01);
        assertEquals(expected.getMetricD(), actual.getMetricD(), 0.01);
        assertEquals(expected.getHost(), actual.getHost());
        assertEquals(expected.getTime(), actual.getTime());
        assertEquals(expected.getState(), actual.getState());
        assertEquals(expected.getService(), actual.getService());
        assertEquals(expected.getTagsList(), actual.getTagsList());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getMetricSint64(), actual.getMetricSint64());
        assertEquals(expected.getAttributesList(), actual.getAttributesList());
    }

    @Test
    public void testToProtoBufEventWithAttributesPresent() throws InvalidProtocolBufferException {
        Buffer buffer = Buffer.buffer();
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("anAttributeKey", "anAttributeValue");

        Event event = new Event("", "", "", "", null, attributes, 0, 0.0F, 0.0);
        new EventMessageCodec().encodeToWire(buffer, event);
        Proto.Event expected = Proto.Event.parseFrom(buffer.getBytes(4, buffer.length()));
        Proto.Event actual = Proto.Event.newBuilder()
                .addAttributes(Proto.Attribute.newBuilder().setKey("anAttributeKey").setValue("anAttributeValue").build())
                .build();
        assertEquals(expected.getAttributesList(), actual.getAttributesList());
    }
}