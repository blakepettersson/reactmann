package reactmann;

import com.aphyr.riemann.Proto;
import io.vertx.core.Vertx;
import org.cliffc.high_scale_lib.NonBlockingHashMap;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author blake
 */
public class Index implements ConcurrentMap<Tup2<String, String>, Event> {
    private final Vertx vertx;
    private final NonBlockingHashMap<Tup2<String, String>, Long> timeouts;
    private final NonBlockingHashMap<Tup2<String, String>, Event> map;

    public Index(Vertx vertx) {
        this.vertx = vertx;
        this.map = new NonBlockingHashMap<>();
        this.timeouts = new NonBlockingHashMap<>();
        //this.map = vertx.sharedData().getMap("events");
        //this.timeouts = vertx.sharedData().getMap("timeouts");
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Set<Tup2<String, String>> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Event> values() {
        return map.values();
    }

    @Override
    public Set<Entry<Tup2<String, String>, Event>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Event get(Object key) {
        return map.get(key);
    }

    @Override
    public Event put(Tup2<String, String> key, Event value) {
        Long timeout = timeouts.get(key);
        if (timeout != null) {
            vertx.cancelTimer(timeout);
            timeouts.remove(key);
        }

        vertx.eventBus().publish("riemann.index", value.toProtoBufEvent().toByteArray());
        timeouts.put(key, vertx.setTimer(Math.round(value.getTtl()), (e) -> remove(key)));
        return map.put(key, value);
    }

    @Override
    public Event remove(Object key) {
        Long timeout = timeouts.get(key);
        if (timeout != null) {
            vertx.cancelTimer(timeout);
            timeouts.remove(key);
        }

        Event remove = map.remove(key);

        Event expired = new Event(remove.getHost(), remove.getService(), "expired", remove.getDescription(), remove.getTags(), remove.getAttributes(), remove.getTime(),
                remove.getTtl(), remove.getMetric());

        Proto.Event value = expired.toProtoBufEvent();

        vertx.eventBus().publish("riemann.index", value.toByteArray());
        vertx.eventBus().publish("riemann.stream", Proto.Msg.newBuilder().addEvents(value).build().toByteArray());

        return expired;
    }

    @Override
    public void putAll(Map<? extends Tup2<String, String>, ? extends Event> m) {
        for (Entry<? extends Tup2<String, String>, ? extends Event> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        timeouts.keySet().forEach(this::remove);
    }

    @Override
    public Event putIfAbsent(Tup2<String, String> key, Event value) {
        Event event = map.get(key);
        if (event == null) {
            return put(key, value);
        }
        return event;
    }

    @Override
    public boolean remove(Object key, Object value) {
        if (map.containsKey(key) && Objects.equals(map.get(key), value)) {
            remove(key);
            return true;
        }

        return false;
    }

    @Override
    public boolean replace(Tup2<String, String> key, Event oldValue, Event newValue) {
        if (map.containsKey(key) && Objects.equals(map.get(key), oldValue)) {
            put(key, newValue);
            return true;
        }

        return false;
    }

    @Override
    public Event replace(Tup2<String, String> key, Event value) {
        if (map.containsKey(key)) {
            return put(key, value);
        }

        return null;
    }
}
