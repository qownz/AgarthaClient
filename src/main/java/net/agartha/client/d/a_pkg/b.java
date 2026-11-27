package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class b extends net.agartha.client.d.a {
   public static final net.agartha.client.d.b<b> TYPE = new net.agartha.client.d.b<>("ChatSendEvent");
   public String message;
   private static final b INSTANCE = new b();

   private b() {
   }

   public static boolean dispatch(String string) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.message = string;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         boolean var1 = INSTANCE.isCancelled();
         INSTANCE.message = null;
         return var1;
      }
   }
}
