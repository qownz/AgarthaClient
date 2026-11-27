package net.agartha.client.util.b_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public final class d {
   private d() {
   }

   public static c fromTo(Vec3d vec3d, Vec3d vec3d2) {
      double var2 = vec3d2.x - vec3d.x;
      double var4 = vec3d2.y - vec3d.y;
      double var6 = vec3d2.z - vec3d.z;
      double var8 = Math.sqrt(var2 * var2 + var6 * var6);
      float var10 = (float)(MathHelper.atan2(var6, var2) * 180.0F / (float)Math.PI) - 90.0F;
      float var11 = (float)(-(MathHelper.atan2(var4, var8) * 180.0F / (float)Math.PI));
      return new c(MathHelper.wrapDegrees(var10), MathHelper.clamp(var11, -90.0F, 90.0F));
   }

   public static c entityRotation(Entity entity) {
      return new c(entity.getYaw(), entity.getPitch());
   }

   public static float yawDelta(float f, float g) {
      return MathHelper.wrapDegrees(g - f);
   }

   public static float pitchDelta(float f, float g) {
      return MathHelper.clamp(g - f, -180.0F, 180.0F);
   }

   public static double angleTo(c arg, c arg2) {
      float var2 = yawDelta(arg.yaw(), arg2.yaw());
      float var3 = pitchDelta(arg.pitch(), arg2.pitch());
      return Math.hypot(var2, var3);
   }

   public static float approachAngle(float f, float g, float h) {
      float var3 = yawDelta(f, g);
      return MathHelper.wrapDegrees(f + MathHelper.clamp(var3, -h, h));
   }

   public static float approachPitch(float f, float g, float h) {
      float var3 = g - f;
      return MathHelper.clamp(f + MathHelper.clamp(var3, -h, h), -90.0F, 90.0F);
   }
}
