package reactmann;

import io.vertx.core.Vertx;

/**
 * @author blake
 */
public class RiemannRun {
    public static void main(String... args) {
        Vertx.vertx().deployVerticle("java:" + TestVerticle.class.getName());
    }
}
