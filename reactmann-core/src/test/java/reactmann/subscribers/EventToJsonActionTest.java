package reactmann.subscribers;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.impl.ws.WebSocketFrameImpl;
import org.junit.Test;
import reactmann.Event;
import reactmann.Tup2;
import rx.Observable;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class EventToJsonActionTest {

    @Test
    public void testEventAction() {
        WebSocketFrameImpl frame = new WebSocketFrameImpl();
        ServerWebSocket socketMock = mock(ServerWebSocket.class);
        Event event = new Event("host", "service", "state", "desc", Arrays.asList("blaha"), null, 1, 1.0F, 1.0D);
        new EventToJsonAction(Observable.just(event), s -> {
            assertEquals(s, "{\"tags\":[\"blaha\"],\"host\":\"host\",\"state\":\"state\",\"service\":\"service\",\"description\":\"desc\",\"metric\":1.0,\"time\":1,\"ttl\":1.0}");
            return frame;
        }).call(Tup2.create(socketMock, e -> true));


        verify(socketMock).writeFrame(frame);
    }
}