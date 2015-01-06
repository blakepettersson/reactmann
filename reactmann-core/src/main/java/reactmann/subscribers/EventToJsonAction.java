package reactmann.subscribers;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonObject;
import reactmann.Event;
import reactmann.Tup2;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class EventToJsonAction implements Action1<Tup2<ServerWebSocket, Func1<Event, Boolean>>> {
    private final Observable<Event> eventObservable;
    private final Func1<String, WebSocketFrame> frameFactory;

    public EventToJsonAction(Observable<Event> eventObservable, Func1<String, WebSocketFrame> frameFactory) {
        this.frameFactory = frameFactory;
        this.eventObservable = eventObservable;
    }

    @Override
    public void call(Tup2<ServerWebSocket, Func1<Event, Boolean>> r) {
        ServerWebSocket socket = r.getLeft();
        Subscription subscription = eventObservable
                .filter(r.getRight())
                .map(e -> new JsonObject()
                                .put("tags", e.getTags())
                                .put("host", e.getHost())
                                .put("state", e.getState())
                                .put("service", e.getService())
                                .put("description", e.getDescription())
                                .put("metric", e.getMetric())
                                .put("time", e.getTime())
                                .put("ttl", e.getTtl())
                )
                .subscribe(json -> socket.writeFrame(frameFactory.call(json.encode())));

        socket.closeHandler(h -> subscription.unsubscribe());
    }
}
