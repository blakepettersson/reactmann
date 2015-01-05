package reactmann.observables;

import com.aphyr.riemann.Proto;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;
import reactmann.Event;
import reactmann.Tup2;
import rx.Observable;
import rx.functions.Func1;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class EventObservableTest extends VertxTestBase {

    @Test
    public void testConversionFromWebSocket() {
        ServerWebSocket socket = mock(ServerWebSocket.class);
        when(socket.uri()).thenReturn("http://something.com?query=true");
        Observable<Tup2<ServerWebSocket, Func1<Event, Boolean>>> observable = EventObservable.convertFromWebSocketObservable(Observable.just(socket));
        observable.subscribe(t -> {
            assertEquals(true, t.getRight().call(null));
            testComplete();
        });

        await();
    }

    @Test
    public void testConversionFromWebSocketWithMoreComplexQuery() {
        ServerWebSocket socket = mock(ServerWebSocket.class);
        when(socket.uri()).thenReturn("http://something.com?query=host+%3D+test");
        Observable<Tup2<ServerWebSocket, Func1<Event, Boolean>>> observable = EventObservable.convertFromWebSocketObservable(Observable.just(socket));
        observable.subscribe(t -> {
            assertEquals(true, t.getRight().call(Event.fromProtoBufEvent(Proto.Event.newBuilder().setHost("test").build())));
            assertEquals(false, t.getRight().call(Event.fromProtoBufEvent(Proto.Event.newBuilder().setHost("fail").build())));
            testComplete();
        });

        await();
    }

    @Test
    public void testConversionFromWebSocketWithInvalidUri() {
        ServerWebSocket socket = mock(ServerWebSocket.class);
        when(socket.uri()).thenReturn("httpquery=true");
        Observable<Tup2<ServerWebSocket, Func1<Event, Boolean>>> observable = EventObservable.convertFromWebSocketObservable(Observable.just(socket));
        observable.subscribe(t -> {
            fail();
            testComplete();
        }, e -> {
            testComplete();
        });

        await();
    }
}