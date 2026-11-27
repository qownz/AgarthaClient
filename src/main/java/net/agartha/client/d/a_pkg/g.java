package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class g extends net.agartha.client.d.a {
   public static final net.agartha.client.d.b<g> TYPE = new net.agartha.client.d.b<>("MouseInputEvent");
   public long window;
   public int button;
   public int action;
   public int mods;
   private static final g INSTANCE = new g();

   private g() {
   }

   public static boolean dispatch(long l, int i, int j, int k) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.window = l;
         INSTANCE.button = i;
         INSTANCE.action = j;
         INSTANCE.mods = k;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         return INSTANCE.isCancelled();
      }
   }
}
