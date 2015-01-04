package ${package};

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import reactmann.Event;
import reactmann.RiemannVerticle;
import rx.Observable;

public class BasicVerticle extends RiemannVerticle {
    private final Logger log = LoggerFactory.getLogger(BasicVerticle.class);

    @Override
    public void observeStream(Observable<Event> events) {
        events.subscribe(e -> {
            log.info(e);
        });
    }
}
