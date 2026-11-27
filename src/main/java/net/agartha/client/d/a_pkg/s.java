package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public final class s extends net.agartha.client.d.c {
   public static final net.agartha.client.d.j<s> TYPE = new net.agartha.client.d.j<>("WorldEntityRemoveEvent");
   public Entity entity;
   private static final s INSTANCE = new s();

   private s() {
   }

   public static void dispatch(Entity entity) {
      if (TYPE.hasPreListeners()) {
         INSTANCE.entity = entity;
         TYPE.dispatch(INSTANCE);
         INSTANCE.entity = null;
      }
   }
}
