package reactmann;

import com.aphyr.riemann.Proto;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.vertx.java.core.impl.StringEscapeUtils;
import riemann.QueryLexer;
import riemann.QueryParser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author blake
 */
public class Query {
   public static rx.functions.Func1<Proto.Event, Boolean> parse(String query) {
      QueryLexer tokenSource = new QueryLexer(new ANTLRStringStream(query));
      QueryParser queryParser = new QueryParser(new CommonTokenStream(tokenSource));
      try {
         CommonTree tree = (CommonTree) queryParser.expr().getTree();
         return (event) -> (boolean) getFilter(tree, event);
      } catch (RecognitionException e) {
         throw new RuntimeException(e);
      }
   }

   private static Object getFilter(CommonTree tree, Proto.Event event) {
      List<CommonTree> children = Optional.ofNullable(tree.getChildren())
         .map(t -> (List<CommonTree>) t)
         .orElseGet(ArrayList::new)
         .stream()
         .filter(f -> !"(".equals(f.getText()) && !")".equals(f.getText()))
         .collect(Collectors.toList());
      //@SuppressWarnings("unchecked") Stream<CommonTree> map = tree.getChildren() != null ? tree.getChildren().stream().map(f -> (CommonTree) f) : Stream
      //   .<CommonTree>empty();
      //List<CommonTree> children = map.filter(f -> !f.getText().equals("(") && !f.getText().equals(")")).collect(Collectors.toList());

      switch (tree.getText()) {
      case "or":
         return parseOr(event, children);
      case "and":
         return parseAnd(event, children);
      case "not":
         return !(boolean) getFilter(children.get(0), event);
      case "=":
         return parseEquals(event, children);
      case ">":
         return parseLargerThan(event, children);
      case ">=":
         return parseLargerThanOrEqual(event, children);
      case "<":
         return parseLessThan(event, children);
      case "<=":
         return parseLessThanOrEqual(event, children);
      case "!=":
         return !(boolean) parseEquals(event, children);
      case "=~":
      case "~=":
      case "nil":
         return null;
      case "null":
         return null;
      case "true":
         return true;
      case "false":
         return false;
      case "tagged":
         //noinspection SuspiciousMethodCalls
         return event.getTagsList().contains(parseString(children.get(0)));
      case "host":
         return event.getHost();
      case "service":
         return event.getService();
      case "state":
         return event.getState();
      case "description":
         return event.getDescription();
      case "metric_f":
         return event.getMetricD();
      case "metric":
         return event.getMetricSint64();
      case "time":
         return event.getTime();
      case "ttl":
         return event.getTtl();
      default:
         return parseString(tree);
      }
   }

   private static Object parseNumber(CommonTree tree) throws ParseException {
      String text = tree.getText();
      if (text.contains(".") || text.contains("E") || text.contains("e")) {
         return Double.valueOf(text);
      }
      return Long.parseLong(text);
   }

   private static Object parseString(CommonTree tree) {
      try {
         return parseNumber(tree);
      } catch (ParseException | NumberFormatException parseEx) {
         try {
            return StringEscapeUtils.unescapeJava(tree.getText().replaceAll("^\"|\"$", ""));
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }
   }

   private static Object parseOr(Proto.Event event, List<CommonTree> children) {
      Object left = getFilter(children.get(0), event);
      Object right = getFilter(children.get(1), event);

      return (boolean) left || (boolean) right;
   }

   private static Object parseAnd(Proto.Event event, List<CommonTree> children) {
      Object left = getFilter(children.get(0), event);
      Object right = getFilter(children.get(1), event);

      return (boolean) left && (boolean) right;
   }

   private static Object parseEquals(Proto.Event event, List<CommonTree> children) {
      Object left = getFilter(children.get(0), event);
      Object right = getFilter(children.get(1), event);
      if (left instanceof Number) {
         return left.equals(right);
      }

      return left.equals("" + right);
   }

   private static boolean parseLessThan(Proto.Event event, List<CommonTree> children) {
      Object number1 = getFilter(children.get(0), event);
      Object number2 = getFilter(children.get(1), event);

      if (number1 instanceof Long) {
         return (Long) number1 < ((number2 instanceof Long) ? (Long) number2 : (Double) number2);
      }

      if (number1 instanceof Double) {
         return (Double) number1 < ((number2 instanceof Long) ? (Long) number2 : (Double) number2);
      }

      throw new IllegalStateException("Arguments are not numbers");
   }

   private static boolean parseLessThanOrEqual(Proto.Event event, List<CommonTree> children) {
      Object number1 = getFilter(children.get(0), event);
      Object number2 = getFilter(children.get(1), event);

      if (number1 instanceof Long) {
         return (Long) number1 <= ((number2 instanceof Long) ? (Long) number2 : (Double) number2);
      }

      if (number1 instanceof Double) {
         return (Double) number1 <= ((number2 instanceof Long) ? (Long) number2 : (Double) number2);
      }

      throw new IllegalStateException("Arguments are not numbers");
   }

   private static boolean parseLargerThan(Proto.Event event, List<CommonTree> children) {
      Object number1 = getFilter(children.get(0), event);
      Object number2 = getFilter(children.get(1), event);

      if (number1 instanceof Long) {
         return (Long) number1 > ((number2 instanceof Long) ? (Long) number2 : (Double) number2);
      }

      if (number1 instanceof Double) {
         return (Double) number1 > ((number2 instanceof Long) ? (Long) number2 : (Double) number2);
      }

      throw new IllegalStateException("Arguments are not numbers");
   }

   private static boolean parseLargerThanOrEqual(Proto.Event event, List<CommonTree> children) {
      Object number1 = getFilter(children.get(0), event);
      Object number2 = getFilter(children.get(1), event);

      if (number1 instanceof Long) {
         return (Long) number1 >= ((number2 instanceof Long) ? (Long) number2 : (Double) number2);
      }

      if (number1 instanceof Double) {
         return (Double) number1 >= ((number2 instanceof Long) ? (Long) number2 : (Double) number2);
      }

      throw new IllegalStateException("Arguments are not numbers");
   }
}
