package reactmann.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.rx.java.RxHelper;
import reactmann.Event;
import reactmann.Index;
import reactmann.Tup2;
import rx.Observable;

/**
 * Created by blake on 04/03/15.
 */
public class IndexVerticle extends AbstractVerticle {
    private Index index;

    public void start() {
        index = new Index(vertx);
        getIndexEvents(vertx).subscribe(e -> {
            index.put(Tup2.create(e.getHost(), e.getService()), e);
        });
    }

    private static Observable<Event> getIndexEvents(Vertx vertx) {
        return RxHelper.toObservable(vertx.eventBus().consumer("riemann.addToIndex").bodyStream()).map(m -> (Event) m);
    }
}
