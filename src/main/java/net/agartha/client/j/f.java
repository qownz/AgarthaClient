package net.agartha.client.j;

import com.sun.jna.Platform;
import net.agartha.client.AgarthaClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class f {
   private static final net.agartha.client.g.b_pkg.a INPUT = a();
   private static final net.agartha.client.g.b_pkg.b FOCUS_GUARD = new net.agartha.client.g.b_pkg.b();
   private static final net.agartha.client.j.a_pkg.b SCHEDULER = new net.agartha.client.j.a_pkg.b();

   private f() {
   }

   private static net.agartha.client.g.b_pkg.a a() {
      Object var0;
      if (Platform.isWindows()) {
         var0 = new net.agartha.client.g.b_pkg.d();
      } else if (Platform.isLinux()) {
         var0 = new net.agartha.client.g.a_pkg.b();
      } else {
         var0 = new g();
      }

      AgarthaClientMod.LOGGER
         .info(
            "[Macro] Input bridge: {} (available={})",
            ((net.agartha.client.g.b_pkg.a)var0).getClass().getSimpleName(),
            ((net.agartha.client.g.b_pkg.a)var0).isAvailable()
         );
      return (net.agartha.client.g.b_pkg.a)var0;
   }

   public static net.agartha.client.g.b_pkg.a input() {
      return INPUT;
   }

   public static net.agartha.client.g.b_pkg.b focusGuard() {
      return FOCUS_GUARD;
   }

   public static net.agartha.client.j.a_pkg.b scheduler() {
      return SCHEDULER;
   }
}
