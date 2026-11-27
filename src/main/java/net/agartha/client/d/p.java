package net.agartha.client.d;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class p<E extends a> extends q<E> {
   public p(Supplier<E> supplier, int i) {
      super(supplier, i);
   }

   public p(String string, Supplier<E> supplier, int i) {
      super(string, supplier, i);
   }

   public void dispatchPre(E arg) {
      b.dispatchPreCancellable(this.list.preSnap(), (E)arg);
   }

   public void dispatchPost(E arg) {
      b.dispatchPostGuarded(this.list.postSnap(), (E)arg);
   }
}
