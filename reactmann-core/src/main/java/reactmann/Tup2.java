package reactmann;

import org.vertx.java.core.shareddata.Shareable;

/**
 * @author blake
 */
public class Tup2<L, R> implements Shareable {
   private final L left;
   private final R right;

   private Tup2(L left, R right) {
      this.left = left;
      this.right = right;
   }

   public static <L, R> Tup2<L, R> create(L left, R right) {
      return new Tup2<>(left, right);
   }

   public L getLeft() {
      return left;
   }

   public R getRight() {
      return right;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      Tup2 tup2 = (Tup2) o;

      if (!left.equals(tup2.left)) {
         return false;
      }
      if (!right.equals(tup2.right)) {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode() {
      int result = left.hashCode();
      result = 31 * result + right.hashCode();
      return result;
   }
}
