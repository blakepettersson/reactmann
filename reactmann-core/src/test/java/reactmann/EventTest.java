package reactmann;

import com.aphyr.riemann.Proto;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class EventTest {
    @Test
    public void testToProtoBufEvent() {
        Proto.Event expected = new Event("", "", "", "", null, null, 0, 0.0F, 0.0).toProtoBufEvent();
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
    public void testToProtoBufEventWithAttributesPresent() {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("anAttributeKey", "anAttributeValue");

        Proto.Event expected = new Event("", "", "", "", null, attributes, 0, 0.0F, 0.0).toProtoBufEvent();
        Proto.Event actual = Proto.Event.newBuilder()
                .addAttributes(Proto.Attribute.newBuilder().setKey("anAttributeKey").setValue("anAttributeValue").build())
                .build();
        assertEquals(expected.getAttributesList(), actual.getAttributesList());
    }

    @Test
    public void testFromProtoBufEvent() {
        Event event = Event.fromProtoBufEvent(Proto.Event.getDefaultInstance());
        assertEquals(new Event("", "", "", "", null, null, 0, 0.0F, 0.0), event);
    }
}