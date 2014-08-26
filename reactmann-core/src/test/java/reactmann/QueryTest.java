package reactmann;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.aphyr.riemann.Proto;
import org.junit.Test;

public class QueryTest {

   @Test
   public void testFields() {
      assertTrue(Query.parse("host = true").call(Proto.Event.newBuilder().setHost("true").build()));
      assertTrue(Query.parse("state = true").call(Proto.Event.newBuilder().setState("true").build()));
      assertTrue(Query.parse("service = true").call(Proto.Event.newBuilder().setService("true").build()));
      assertTrue(Query.parse("description = true").call(Proto.Event.newBuilder().setDescription("true").build()));
   }

   @Test
   public void testState() throws Exception {
      assertTrue(Query.parse("state = \"\"").call(Proto.Event.newBuilder().setState("").build()));
      assertTrue(Query.parse("state = \"foo\"").call(Proto.Event.newBuilder().setState("foo").build()));
      assertTrue(Query.parse("state = \"辻斬\"").call(Proto.Event.newBuilder().setState("辻斬").build()));
      assertTrue(Query.parse("state = \"\\b\\t\\n\\f\\r\"").call(Proto.Event.newBuilder().setState("\b\t\n\f\r").build()));
      assertTrue(Query.parse("state = \" \\\" \\\\ \"").call(Proto.Event.newBuilder().setState(" \" \\ ").build()));
   }

   @Test
   public void testInts() throws Exception {
      assertTrue(Query.parse("metric = 0").call(Proto.Event.newBuilder().setMetricSint64(0).build()));
      assertTrue(Query.parse("metric = 1").call(Proto.Event.newBuilder().setMetricSint64(1).build()));
      assertTrue(Query.parse("metric = -1").call(Proto.Event.newBuilder().setMetricSint64(-1).build()));
   }

   @Test
   public void testFloats() throws Exception {
      assertTrue(Query.parse("metric_f = 1.").call(Proto.Event.newBuilder().setMetricD(1.0).build()));
      assertTrue(Query.parse("metric_f = 0.0").call(Proto.Event.newBuilder().setMetricD(0.0).build()));
      assertTrue(Query.parse("metric_f = 1.5").call(Proto.Event.newBuilder().setMetricD(1.5).build()));
      assertTrue(Query.parse("metric_f = -1.5").call(Proto.Event.newBuilder().setMetricD(-1.5).build()));
      assertTrue(Query.parse("metric_f = 1e5").call(Proto.Event.newBuilder().setMetricD(100000).build()));
      assertTrue(Query.parse("metric_f = 1E5").call(Proto.Event.newBuilder().setMetricD(100000).build()));
      assertTrue(Query.parse("metric_f = -1.2e-5").call(Proto.Event.newBuilder().setMetricD(-0.000012).build()));
   }

   @Test
   public void testTags() throws Exception {
      assertTrue(Query.parse("tagged \"cat\"").call(Proto.Event.newBuilder().addTags("cat").build()));
   }

   @Test
   public void testLiterals() throws Exception {
      assertTrue(Query.parse("true").call(Proto.Event.getDefaultInstance()));
      assertFalse(Query.parse("false").call(Proto.Event.getDefaultInstance()));
   }

   @Test
   public void testBooleanOperators() throws Exception {
      assertTrue(Query.parse("not host = 1").call(Proto.Event.newBuilder().setHost("2").build()));
      assertFalse(Query.parse("not host = 1").call(Proto.Event.newBuilder().setHost("1").build()));

      assertTrue(Query.parse("host = 1 and state = 2").call(Proto.Event.newBuilder().setHost("1").setState("2").build()));
      assertFalse(Query.parse("host = 1 and state = 2").call(Proto.Event.newBuilder().setHost("2").setState("1").build()));

      assertTrue(Query.parse("host = 1 or state = 2").call(Proto.Event.newBuilder().setHost("1").setState("1").build()));
      assertTrue(Query.parse("host = 1 or state = 2").call(Proto.Event.newBuilder().setHost("2").setState("2").build()));
   }

   @Test
   public void testPredicates() throws Exception {
      assertTrue(Query.parse("time = 2").call(Proto.Event.newBuilder().setTime(2).build()));
      assertFalse(Query.parse("time = 2").call(Proto.Event.newBuilder().setTime(3).build()));
      assertFalse(Query.parse("time = 2").call(Proto.Event.newBuilder().setTime(1).build()));

      assertTrue(Query.parse("time > 2").call(Proto.Event.newBuilder().setTime(3).build()));
      assertFalse(Query.parse("time > 2").call(Proto.Event.newBuilder().setTime(1).build()));
      assertFalse(Query.parse("time > 2").call(Proto.Event.newBuilder().setTime(2).build()));

      assertTrue(Query.parse("time < 2").call(Proto.Event.newBuilder().setTime(1).build()));
      assertFalse(Query.parse("time < 2").call(Proto.Event.newBuilder().setTime(2).build()));

      assertTrue(Query.parse("time >= 2").call(Proto.Event.newBuilder().setTime(2).build()));
      assertTrue(Query.parse("time >= 2").call(Proto.Event.newBuilder().setTime(3).build()));
      assertFalse(Query.parse("time >= 2").call(Proto.Event.newBuilder().setTime(1).build()));

      assertTrue(Query.parse("time <= 2").call(Proto.Event.newBuilder().setTime(2).build()));
      assertTrue(Query.parse("time <= 2").call(Proto.Event.newBuilder().setTime(1).build()));
      assertFalse(Query.parse("time <= 2").call(Proto.Event.newBuilder().setTime(3).build()));

      assertTrue(Query.parse("time != 2").call(Proto.Event.newBuilder().setTime(1).build()));
      assertTrue(Query.parse("time != 2").call(Proto.Event.newBuilder().setTime(3).build()));
      assertFalse(Query.parse("time != 2").call(Proto.Event.newBuilder().setTime(2).build()));
   }

   @Test
   public void testPrecedence() {
      assertTrue(Query.parse("not host = 1 and host = 2").call(Proto.Event.newBuilder().setHost("2").build()));
      assertFalse(Query.parse("not host = 1 and host = 2").call(Proto.Event.newBuilder().setHost("1").build()));

      assertTrue(Query.parse("not host = 1 or host = 2 and host = 3").call(Proto.Event.newBuilder().setHost("3").build()));
      assertTrue(Query.parse("not host = 1 or host = 2 and host = 3").call(Proto.Event.newBuilder().setHost("2").build()));
      assertFalse(Query.parse("not host = 1 or host = 2 and host = 3").call(Proto.Event.newBuilder().setHost("1").build()));

      assertTrue(Query.parse("not ((host = 1 or host = 2) and host = 3)").call(Proto.Event.newBuilder().setHost("1").build()));
      assertTrue(Query.parse("not ((host = 1 or host = 2) and host = 3)").call(Proto.Event.newBuilder().setHost("2").build()));
      assertTrue(Query.parse("not ((host = 1 or host = 2) and host = 3)").call(Proto.Event.newBuilder().setHost("3").build()));
   }
}
