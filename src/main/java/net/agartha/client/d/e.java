package net.agartha.client.d;

import java.util.ArrayList;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class e {
   private e() {
   }

   public static String dumpListeners() {
      ArrayList<j> var0 = new ArrayList<>(j.REGISTRY);
      var0.sort(Comparator.comparing(j::name));
      StringBuilder var1 = new StringBuilder("=== Event listeners ===\n");

      for (j var3 : (Iterable<j>) var0) {
         var1.append(var3.name()).append(" (").append(var3.listenerCount()).append(")\n");
         a(var1, "PRE ", var3.list.preSnapshot());
         a(var1, "POST", var3.list.postSnapshot());
      }

      return var1.toString();
   }

   private static void a(StringBuilder stringBuilder, String string, n<?>[] args) {
      for (n var6 : args) {
         Object var7 = var6.owner();
         stringBuilder.append("  ")
            .append(string)
            .append(' ')
            .append(var6.priority())
            .append(' ')
            .append(var7 == null ? "<no-owner>" : var7.getClass().getName())
            .append('\n');
      }
   }
}
