package reactmann;

import com.aphyr.riemann.Proto;
import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by blake on 04/03/15.
 */
public class EventMessageCodec implements MessageCodec<Event, Event> {
    @Override
    public void encodeToWire(Buffer buffer, Event event) {
        List<Proto.Attribute> attributeList = event.getAttributes()
                .entrySet()
                .stream()
                .map(a -> Proto.Attribute.newBuilder().setKey(a.getKey()).setValue(a.getValue()).build())
                .collect(Collectors.toList());

        Proto.Event protoEvent = Proto.Event.newBuilder()
                .setDescription(event.getDescription())
                .setHost(event.getHost())
                .setMetricD(event.getMetric())
                .setService(event.getService())
                .setTime(event.getTime())
                .setTtl(event.getTtl())
                .setState(event.getState())
                .addAllTags(event.getTags())
                .addAllAttributes(attributeList)
                .build();

        byte[] bytes = protoEvent.toByteArray();

        buffer.appendInt(bytes.length);
        buffer.appendBytes(bytes);
    }

    @Override
    public Event decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        pos += 4;

        try {
            Proto.Event event = Proto.Event.parseFrom(buffer.getBytes(pos, pos + length));
            double metric;
            if (event.hasMetricD()) {
                metric = event.getMetricD();
            } else if (event.hasMetricF()) {
                metric = event.getMetricF();
            } else {
                metric = event.getMetricSint64();
            }

            Map<String, String> attributes = event.getAttributesList().stream().collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));
            return new Event(event.getHost(), event.getService(), event.getState(), event.getDescription(), event.getTagsList(), attributes, event.getTime(), event.getTtl(), metric);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Event transform(Event event) {
        return event;
    }

    @Override
    public String name() {
        return "event";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
