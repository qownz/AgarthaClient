package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class e extends net.agartha.client.d.a {
   public static final net.agartha.client.d.b<e> TYPE = new net.agartha.client.d.b<>("KeyEvent");
   public int key;
   public int scancode;
   public int action;
   public int mods;
   private static final e INSTANCE = new e();

   private e() {
   }

   public static boolean dispatch(int i, int j, int k, int l) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.key = i;
         INSTANCE.scancode = j;
         INSTANCE.action = k;
         INSTANCE.mods = l;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         return INSTANCE.isCancelled();
      }
   }
}
