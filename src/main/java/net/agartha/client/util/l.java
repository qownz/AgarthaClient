package net.agartha.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public final class l {
   private static final double CAMERA_MARGIN = 0.2;
   private static final double LARGE_BOX_MIN = 4.0;
   private static final double MIN_CAMERA_LINE_DISTANCE_SQ = 0.04;
   private static Vec3d cameraPosition;
   private static Frustum frustum;

   private l() {
   }

   public static void beginFrame() {
      beginFrame(null);
   }

   public static void beginFrame(Frustum frustum) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      Camera var2 = var1.gameRenderer.getCamera();
      cameraPosition = var2 != null && var2.isReady() ? var2.getCameraPos() : null;
      l.frustum = var1.worldRenderer != null && var1.worldRenderer.getCapturedFrustum() != null ? null : frustum;
   }

   public static void endFrame() {
      frustum = null;
   }

   public static boolean outOfView(Box box) {
      Frustum var1 = frustum;
      return var1 != null && box != null && !var1.isVisible(box);
   }

   public static boolean columnOutOfView(int i, int j, int k, int l) {
      Frustum var4 = frustum;
      if (var4 == null) {
         return false;
      } else {
         int var5 = i << 4;
         int var6 = j << 4;
         return !var4.isVisible(new Box(var5, k, var6, var5 + 16.0, l + 1.0, var6 + 16.0));
      }
   }

   public static boolean isSafeCuboid(Box box) {
      return isSafeCuboid(box, cameraPosition);
   }

   public static boolean isSafeCuboid(Box box, Vec3d vec3d) {
      if (box == null || !a(box.minX) || !a(box.minY) || !a(box.minZ) || !a(box.maxX) || !a(box.maxY) || !a(box.maxZ)) {
         return false;
      } else if (box.minX > box.maxX || box.minY > box.maxY || box.minZ > box.maxZ) {
         return false;
      } else {
         return vec3d != null && a(box, vec3d, 0.2) ? box.getLengthX() >= 4.0 && box.getLengthY() >= 4.0 && box.getLengthZ() >= 4.0 : true;
      }
   }

   private static boolean a(Box box, Vec3d vec3d, double d) {
      return vec3d.x >= box.minX - d
         && vec3d.x < box.maxX + d
         && vec3d.y >= box.minY - d
         && vec3d.y < box.maxY + d
         && vec3d.z >= box.minZ - d
         && vec3d.z < box.maxZ + d;
   }

   public static boolean isSafeLine(Vec3d vec3d, Vec3d vec3d2) {
      return isSafeLine(vec3d, vec3d2, cameraPosition);
   }

   public static boolean isSafeLine(Vec3d vec3d, Vec3d vec3d2, Vec3d vec3d3) {
      return a(vec3d) && a(vec3d2) && !(vec3d.squaredDistanceTo(vec3d2) < 1.0E-8)
         ? vec3d3 == null || distanceToSegmentSquared(vec3d3, vec3d, vec3d2) >= 0.04
         : false;
   }

   static double distanceToSegmentSquared(Vec3d vec3d, Vec3d vec3d2, Vec3d vec3d3) {
      Vec3d var3 = vec3d3.subtract(vec3d2);
      double var4 = var3.lengthSquared();
      if (var4 < 1.0E-12) {
         return vec3d.squaredDistanceTo(vec3d2);
      } else {
         double var6 = vec3d.subtract(vec3d2).dotProduct(var3) / var4;
         var6 = Math.max(0.0, Math.min(1.0, var6));
         return vec3d.squaredDistanceTo(vec3d2.add(var3.multiply(var6)));
      }
   }

   private static boolean a(Vec3d vec3d) {
      return vec3d != null && a(vec3d.x) && a(vec3d.y) && a(vec3d.z);
   }

   private static boolean a(double d) {
      return Double.isFinite(d);
   }
}
