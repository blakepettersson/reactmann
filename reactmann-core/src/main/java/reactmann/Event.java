package reactmann;

import com.aphyr.riemann.Proto;

import java.util.List;

/**
 * @author blake
 */
public class Event {
   private final String host;
   private final String service;
   private final String state;
   private final String description;
   private final List<String> tags;
   private final long time;
   private final float ttl;
   private final double metric;

   public Event(String host, String service, String state, String description, List<String> tags, long time, float ttl, double metric) {
      this.host = host;
      this.service = service;
      this.state = state;
      this.description = description;
      this.tags = tags;
      this.time = time;
      this.ttl = ttl;
      this.metric = metric;
   }

   public static Event fromProtoBufEvent(Proto.Event event) {
      double metric;
      if (event.hasMetricD()) {
         metric = event.getMetricD();
      } else if (event.hasMetricF()) {
         metric = event.getMetricF();
      } else {
         metric = event.getMetricSint64();
      }

      return new Event(event.getHost(), event.getService(), event.getState(), event.getDescription(), event.getTagsList(), event.getTime(), event.getTtl(), metric);
   }

   public double getMetric() {
      return metric;
   }

   public float getTtl() {
      return ttl;
   }

   public long getTime() {
      return time;
   }

   public List<String> getTags() {
      return tags;
   }

   public String getDescription() {
      return description;
   }

   public String getState() {
      return state;
   }

   public String getService() {
      return service;
   }

   public String getHost() {
      return host;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      Event event = (Event) o;

      if (Double.compare(event.metric, metric) != 0) {
         return false;
      }
      if (time != event.time) {
         return false;
      }
      if (Float.compare(event.ttl, ttl) != 0) {
         return false;
      }
      if (description != null ? !description.equals(event.description) : event.description != null) {
         return false;
      }
      if (host != null ? !host.equals(event.host) : event.host != null) {
         return false;
      }
      if (service != null ? !service.equals(event.service) : event.service != null) {
         return false;
      }
      if (state != null ? !state.equals(event.state) : event.state != null) {
         return false;
      }
      if (tags != null ? !tags.equals(event.tags) : event.tags != null) {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode() {
      int result;
      long temp;
      result = host != null ? host.hashCode() : 0;
      result = 31 * result + (service != null ? service.hashCode() : 0);
      result = 31 * result + (state != null ? state.hashCode() : 0);
      result = 31 * result + (description != null ? description.hashCode() : 0);
      result = 31 * result + (tags != null ? tags.hashCode() : 0);
      result = 31 * result + (int) (time ^ (time >>> 32));
      result = 31 * result + (ttl != +0.0f ? Float.floatToIntBits(ttl) : 0);
      temp = Double.doubleToLongBits(metric);
      result = 31 * result + (int) (temp ^ (temp >>> 32));
      return result;
   }

   @Override
   public String toString() {
      return "Event{" +
         "host='" + host + '\'' +
         ", service='" + service + '\'' +
         ", state='" + state + '\'' +
         ", description='" + description + '\'' +
         ", tags=" + tags +
         ", time=" + time +
         ", ttl=" + ttl +
         ", metric=" + metric +
         '}';
   }
}
