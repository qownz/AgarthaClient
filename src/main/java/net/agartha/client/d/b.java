package net.agartha.client.d;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class b<E extends a> extends j<E> {
   public b() {
   }

   public b(String string) {
      super(string);
   }

   public void dispatchPre(E arg) {
      dispatchPreCancellable(this.list.preSnap(), (E)arg);
   }

   public void dispatchPost(E arg) {
      dispatchPostGuarded(this.list.postSnap(), (E)arg);
   }

   static <E extends a> void dispatchPreCancellable(o.a<E> arg, E arg2) {
      Consumer[] var2 = arg.callbacks;

      for (int var3 = 0; var3 < var2.length; var3++) {
         long var4 = net.agartha.client.k.b_o.begin();

         try {
            var2[var3].accept(arg2);
         } catch (Exception var10) {
            m.report(arg.handles[var3], arg2, var10);
         } finally {
            net.agartha.client.k.b_o.recordListener(arg2, arg.handles[var3], var4);
         }

         if (arg2.cancelled) {
            break;
         }
      }
   }

   static <E extends a> void dispatchPostGuarded(o.a<E> arg, E arg2) {
      arg2.postStage = true;

      try {
         Consumer[] var2 = arg.callbacks;

         for (int var3 = 0; var3 < var2.length; var3++) {
            long var4 = net.agartha.client.k.b_o.begin();

            try {
               var2[var3].accept(arg2);
            } catch (Exception var15) {
               m.report(arg.handles[var3], arg2, var15);
            } finally {
               net.agartha.client.k.b_o.recordListener(arg2, arg.handles[var3], var4);
            }
         }
      } finally {
         arg2.postStage = false;
      }
   }
}
