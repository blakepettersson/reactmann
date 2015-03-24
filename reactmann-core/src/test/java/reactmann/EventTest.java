package reactmann;

import com.aphyr.riemann.Proto;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class EventTest {


    @Test
    public void testFromProtoBufEvent() {
        Event event = Event.builder().fromProtoBufEvent(Proto.Event.getDefaultInstance()).build();
        assertEquals(new Event("", "", "", "", null, null, 0, 0.0F, 0.0), event);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTagsImmutability() {
        Event event = Event.builder().fromProtoBufEvent(Proto.Event.getDefaultInstance()).build();
        event.getTags().add("fail");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAttributesImmutability() {
        Event event = Event.builder().fromProtoBufEvent(Proto.Event.getDefaultInstance()).build();
        event.getAttributes().put("fail", "more fail");
    }

    @Test
    public void testWithTtl() {
        assertEquals(1337, Event.builder().withTtl(1337).build().getTtl(), 0.001);
    }

    @Test
    public void testWithTime() {
        assertEquals(1337, Event.builder().withTime(1337).build().getTime());
    }

    @Test
    public void testAddTags() {
        assertEquals("1337", Event.builder().addTags("1337").build().getTags().get(0));
        assertEquals(Arrays.asList("1","2"), Event.builder().addTags("1", "2").build().getTags());
    }

    @Test
    public void testWithTags() {
        assertEquals("1337", Event.builder().withTags(Arrays.asList("1337")).build().getTags().get(0));
    }

    @Test
    public void testWithHost() {
        assertEquals("1337", Event.builder().withHost("1337").build().getHost());
    }

    @Test
    public void testWithState() {
        assertEquals("1337", Event.builder().withState("1337").build().getState());
    }

    @Test
    public void testWithService() {
        assertEquals("1337", Event.builder().withState("1337").build().getState());
    }

    @Test
    public void testAddAttribute() {
        assertEquals("value", Event.builder().addAttribute("key", "value").build().getAttributes().get("key"));
    }

    @Test
    public void testWithAttributes() {
        assertEquals("value", Event.builder().withAttributes(ImmutableMap.of("key", "value")).build().getAttributes().get("key"));
    }

    @Test
    public void testWithDescription() {
        assertEquals("1337", Event.builder().withDescription("1337").build().getDescription());
    }

    @Test
    public void testWithMetric() {
        assertEquals(1337, Event.builder().withMetric(1337).build().getMetric(), 0.001);
    }
}