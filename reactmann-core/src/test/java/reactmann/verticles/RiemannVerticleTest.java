package reactmann.verticles;

import io.vertx.test.core.VertxTestBase;
import org.junit.Test;
import reactmann.*;
import rx.Observable;

public class RiemannVerticleTest extends VertxTestBase {

    @Test
    public void blaha() {
        RiemannVerticle riemannVerticle = new RiemannVerticle() {
            @Override
            public void observeStream(Observable<Event> events) {
                events.doOnNext(e ->{
                    if(!"expired".equals(e.getState())) {
                        index(e);
                    }
                }).buffer(2).subscribe(e -> {
                    assertEquals("ok", e.get(0).getState());
                    assertEquals("expired", e.get(1).getState());
                    testComplete();
                });
            }
        };
        IndexVerticle indexVerticle = new IndexVerticle();
        indexVerticle.init(vertx, null);
        indexVerticle.start();

        riemannVerticle.init(vertx, null);

        vertx.eventBus().registerDefaultCodec(Event.class, new EventMessageCodec());
        riemannVerticle.observeStream(Riemann.getEvents(vertx, EventType.STREAM).startWith(Event.builder().withState("ok").build()));
        await();
    }
}