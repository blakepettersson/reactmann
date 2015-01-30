package reactmann;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import rx.functions.Func1;

public class QueryTest {

   @Test
   public void testFields() {
      assertTrue(Query.parse("host = true").call(Event.builder().withHost("true").build()));
      assertTrue(Query.parse("state = true").call(Event.builder().withState("true").build()));
      assertTrue(Query.parse("service = true").call(Event.builder().withService("true").build()));
      assertTrue(Query.parse("description = true").call(Event.builder().withDescription("true").build()));
   }

   @Test
   public void testState() throws Exception {
      assertTrue(Query.parse("state = \"\"").call(Event.builder().withState("").build()));
      assertTrue(Query.parse("state = \"foo\"").call(Event.builder().withState("foo").build()));
      assertTrue(Query.parse("state = \"辻斬\"").call(Event.builder().withState("辻斬").build()));
      assertTrue(Query.parse("state = \"\\b\\t\\n\\f\\r\"").call(Event.builder().withState("\b\t\n\f\r").build()));
      assertTrue(Query.parse("state = \" \\\" \\\\ \"").call(Event.builder().withState(" \" \\ ").build()));
   }

   @Test
   public void testInts() throws Exception {
      assertTrue(Query.parse("metric = 0").call(Event.builder().withMetric(0).build()));
      assertTrue(Query.parse("metric = 1").call(Event.builder().withMetric(1).build()));
      assertTrue(Query.parse("metric = -1").call(Event.builder().withMetric(-1).build()));
   }

   @Test
   public void testFloats() throws Exception {
      assertTrue(Query.parse("metric_f = 1.").call(Event.builder().withMetric(1.0).build()));
      assertTrue(Query.parse("metric_f = 0.0").call(Event.builder().withMetric(0.0).build()));
      assertTrue(Query.parse("metric_f = 1.5").call(Event.builder().withMetric(1.5).build()));
      assertTrue(Query.parse("metric_f = -1.5").call(Event.builder().withMetric(-1.5).build()));
      assertTrue(Query.parse("metric_f = 1e5").call(Event.builder().withMetric(100000).build()));
      assertTrue(Query.parse("metric_f = 1E5").call(Event.builder().withMetric(100000).build()));
      assertTrue(Query.parse("metric_f = -1.2e-5").call(Event.builder().withMetric(-0.000012).build()));
   }

   @Test
   public void testTags() throws Exception {
      assertTrue(Query.parse("tagged \"cat\"").call(Event.builder().addTags("cat").build()));
   }

   @Test
   public void testLiterals() throws Exception {
      assertTrue(Query.parse("true").call(Event.builder().build()));
      assertFalse(Query.parse("false").call(Event.builder().build()));
   }

   @Test
   public void testBooleanOperators() throws Exception {
      assertTrue(Query.parse("not host = 1").call(Event.builder().withHost("2").build()));
      assertFalse(Query.parse("not host = 1").call(Event.builder().withHost("1").build()));

      assertTrue(Query.parse("host = 1 and state = 2").call(Event.builder().withHost("1").withState("2").build()));
      assertFalse(Query.parse("host = 1 and state = 2").call(Event.builder().withHost("2").withState("1").build()));

      assertTrue(Query.parse("host = 1 or state = 2").call(Event.builder().withHost("1").withState("1").build()));
      assertTrue(Query.parse("host = 1 or state = 2").call(Event.builder().withHost("2").withState("2").build()));
   }

   @Test
   public void testPredicates() throws Exception {
      assertTrue(Query.parse("time = 2").call(Event.builder().withTime(2).build()));
      assertFalse(Query.parse("time = 2").call(Event.builder().withTime(3).build()));
      assertFalse(Query.parse("time = 2").call(Event.builder().withTime(1).build()));

      assertTrue(Query.parse("time > 2").call(Event.builder().withTime(3).build()));
      assertFalse(Query.parse("time > 2").call(Event.builder().withTime(1).build()));
      assertFalse(Query.parse("time > 2").call(Event.builder().withTime(2).build()));

      assertTrue(Query.parse("time < 2").call(Event.builder().withTime(1).build()));
      assertFalse(Query.parse("time < 2").call(Event.builder().withTime(2).build()));

      assertTrue(Query.parse("time >= 2").call(Event.builder().withTime(2).build()));
      assertTrue(Query.parse("time >= 2").call(Event.builder().withTime(3).build()));
      assertFalse(Query.parse("time >= 2").call(Event.builder().withTime(1).build()));

      assertTrue(Query.parse("time <= 2").call(Event.builder().withTime(2).build()));
      assertTrue(Query.parse("time <= 2").call(Event.builder().withTime(1).build()));
      assertFalse(Query.parse("time <= 2").call(Event.builder().withTime(3).build()));

      assertTrue(Query.parse("time != 2").call(Event.builder().withTime(1).build()));
      assertTrue(Query.parse("time != 2").call(Event.builder().withTime(3).build()));
      assertFalse(Query.parse("time != 2").call(Event.builder().withTime(2).build()));
   }

   @Test
   public void testPrecedence() {
      assertTrue(Query.parse("not host = 1 and host = 2").call(Event.builder().withHost("2").build()));
      assertFalse(Query.parse("not host = 1 and host = 2").call(Event.builder().withHost("1").build()));

      assertTrue(Query.parse("not host = 1 or host = 2 and host = 3").call(Event.builder().withHost("3").build()));
      assertTrue(Query.parse("not host = 1 or host = 2 and host = 3").call(Event.builder().withHost("2").build()));
      assertFalse(Query.parse("not host = 1 or host = 2 and host = 3").call(Event.builder().withHost("1").build()));

      assertTrue(Query.parse("not ((host = 1 or host = 2) and host = 3)").call(Event.builder().withHost("1").build()));
      assertTrue(Query.parse("not ((host = 1 or host = 2) and host = 3)").call(Event.builder().withHost("2").build()));
      assertTrue(Query.parse("not ((host = 1 or host = 2) and host = 3)").call(Event.builder().withHost("3").build()));
   }

   @Test
   public void testNull() throws Exception {
      Func1<Event, Boolean> query = Query.parse("host = null and description != nil");
      assertTrue(query.call(Event.builder().withHost(null).build()));
      assertFalse(query.call(Event.builder().withHost("not null").build()));
      assertFalse(query.call(Event.builder().withHost("not null").withDescription(null).build()));
   }

   @Test
   public void testRegexps() {
      Func1<Event, Boolean> query = Query.parse("host ~= \"foo?[1-9]+\"");
      assertTrue(query.call(Event.builder().withHost("foo19").build()));
      assertTrue(query.call(Event.builder().withHost("foo1").build()));
      assertTrue(query.call(Event.builder().withHost("fo42").build()));

      assertFalse(query.call(Event.builder().withHost("abc").build()));
      assertFalse(query.call(Event.builder().withHost("foo").build()));
      assertFalse(query.call(Event.builder().withHost("fooo42").build()));
   }

   @Test
   public void testWildcards() {
      Func1<Event, Boolean> query = Query.parse("host =~ \"%s.\"");
      assertTrue(query.call(Event.builder().withHost("s.").build()));
      assertTrue(query.call(Event.builder().withHost("foos.").build()));

      assertFalse(query.call(Event.builder().withHost("a.").build()));
      assertFalse(query.call(Event.builder().withHost("s.murf").build()));
   }
}
