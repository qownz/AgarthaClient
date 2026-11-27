package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public final class r extends net.agartha.client.d.c {
   public static final net.agartha.client.d.j<r> TYPE = new net.agartha.client.d.j<>("WorldEntityAddEvent");
   public Entity entity;
   private static final r INSTANCE = new r();

   private r() {
   }

   public static void dispatch(Entity entity) {
      if (TYPE.hasPreListeners()) {
         INSTANCE.entity = entity;
         TYPE.dispatch(INSTANCE);
         INSTANCE.entity = null;
      }
   }
}
