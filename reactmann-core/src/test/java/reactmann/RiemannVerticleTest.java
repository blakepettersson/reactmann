package reactmann;

import io.vertx.test.core.VertxTestBase;
import org.junit.Test;
import rx.Observable;

import static org.junit.Assert.*;

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
        riemannVerticle.setIndex(new Index(vertx));
        riemannVerticle.observeStream(Riemann.getEvents(vertx).startWith(Event.builder().withState("ok").build()));
        await();
    }
}