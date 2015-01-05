package reactmann.observables;

import io.vertx.core.http.ServerWebSocket;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import reactmann.Event;
import reactmann.NetSocketException;
import reactmann.Query;
import reactmann.Tup2;
import rx.Observable;
import rx.functions.Func1;

import java.net.URI;
import java.util.List;

public class EventObservable {
    public static Observable<Tup2<ServerWebSocket, Func1<Event, Boolean>>> convertFromWebSocketObservable(Observable<ServerWebSocket> webSocketObservable) {
        return webSocketObservable.map(s -> {
            try {
                List<NameValuePair> query = URLEncodedUtils.parse(new URI(s.uri()), "UTF-8");
                NameValuePair nameValuePair = query.stream().filter(p -> "query".equals(p.getName())).findAny().get();
                return Tup2.create(s, Query.parse(nameValuePair.getValue()));
            } catch (Exception e) {
                throw new NetSocketException(s, e);
            }
        });
    }
}
