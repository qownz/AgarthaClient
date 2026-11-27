package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public final class m extends net.agartha.client.d.a {
   public static final net.agartha.client.d.b<m> TYPE = new net.agartha.client.d.b<>("PlayerMoveEvent");
   public double x;
   public double y;
   public double z;
   private static final m INSTANCE = new m();

   private m() {
   }

   public static boolean dispatch(Vec3d vec3d) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.x = vec3d.x;
         INSTANCE.y = vec3d.y;
         INSTANCE.z = vec3d.z;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         return INSTANCE.isCancelled();
      }
   }
}
