package net.agartha.client.g.a_pkg;

import com.sun.jna.Library;
import com.sun.jna.Native;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
final class a {
   private a() {
   }

   static <T extends Library> T load(Class<T> class_, String... strings) {
      RuntimeException var2 = null;

      for (String var6 : strings) {
         try {
            return (T)Native.load(var6, class_);
         } catch (UnsatisfiedLinkError | RuntimeException var9) {
            var2 = var9 instanceof RuntimeException var8 ? var8 : new RuntimeException(var9);
         }
      }

      throw var2 != null ? var2 : new RuntimeException("No candidate libraries provided");
   }
}
