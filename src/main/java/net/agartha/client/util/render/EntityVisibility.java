package net.agartha.client.util.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public final class EntityVisibility {
   private EntityVisibility() {
   }

   public static boolean drawnByGame(Entity entity) {
      if (entity != null && !entity.isRemoved()) {
         Camera var1 = MinecraftClient.getInstance().gameRenderer.getCamera();
         if (var1 != null && var1.isReady()) {
            Vec3d var2 = var1.getCameraPos();
            return entity.shouldRender(var2.x, var2.y, var2.z);
         } else {
            return true;
         }
      } else {
         return false;
      }
   }
}
