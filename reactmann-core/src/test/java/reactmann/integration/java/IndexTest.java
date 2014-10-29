package reactmann.integration.java;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

import org.junit.Test;
import org.vertx.testtools.TestVerticle;
import reactmann.Event;
import reactmann.Index;
import reactmann.Riemann;
import reactmann.Tup2;

/**
 * @author blake
 */
public class IndexTest extends TestVerticle {

   @Override
   public void start() {
      // Make sure we call initialize() - this sets up the assert stuff so assert functionality works correctly
      initialize();
      startTests();
   }

   @Test
   public void testRemove() {
      Index index = new Index(vertx);
      Tup2<String, String> key = Tup2.create("test", "test");
      index.put(key, new Event("test", "test", "hello", "", null, System.currentTimeMillis(), Integer.MAX_VALUE, 1.0));
      assertEquals(1, index.size());
      index.remove(key);
      assertEquals(0, index.size());
      testComplete();
   }

   @Test
   public void testRemoveWithEventBus() {
      Index index = new Index(vertx);

      Riemann.getEvents(vertx).forEach(e -> {
         assertEquals(0, index.size());
         assertEquals("expired", e.getState());
         testComplete();
      });

      Tup2<String, String> key = Tup2.create("test", "test");
      index.put(key, new Event("test", "test", "hello", "", null, System.currentTimeMillis(), Integer.MAX_VALUE, 1.0));
      assertEquals(1, index.size());
      index.remove(key);
      assertEquals(0, index.size());
   }

   @Test
   public void testPutWithTtl() {
      Index index = new Index(vertx);
      index.put(Tup2.create("test", "test"), new Event("test", "test", "hello", "", null, System.currentTimeMillis(), 5, 1.0));
      assertEquals(1, index.size());
      vertx.setTimer(10, (l) -> {
         assertEquals(0, index.size());
         testComplete();
      });
   }

   @Test
   public void testPutWithTtlAndEventBus() {
      Index index = new Index(vertx);

      Riemann.getEvents(vertx).forEach(e -> {
         assertEquals(0, index.size());
         assertEquals("expired", e.getState());
         testComplete();
      });

      index.put(Tup2.create("test", "test"), new Event("test", "test", "hello", "", null, System.currentTimeMillis(), 2, 1.0));
      assertEquals(1, index.size());
   }

}
