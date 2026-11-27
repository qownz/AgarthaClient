package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class h extends net.agartha.client.d.a {
   public static final net.agartha.client.d.b<h> TYPE = new net.agartha.client.d.b<>("MouseScrollEvent");
   public double value;
   private static final h INSTANCE = new h();

   private h() {
   }

   public static h get(double d) {
      INSTANCE.resetCancellation();
      INSTANCE.value = d;
      return INSTANCE;
   }

   public static boolean dispatch(double d) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.value = d;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         return INSTANCE.isCancelled();
      }
   }
}
