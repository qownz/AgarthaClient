package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public final class l extends net.agartha.client.d.a {
   public static final net.agartha.client.d.b<l> TYPE = new net.agartha.client.d.b<>("PlayerAttackEntityEvent");
   public Entity target;
   private static final l INSTANCE = new l();

   private l() {
   }

   public static boolean dispatch(Entity entity) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.target = entity;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         boolean var1 = INSTANCE.isCancelled();
         INSTANCE.target = null;
         return var1;
      }
   }
}
