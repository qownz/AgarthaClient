package net.agartha.client.util.render;

import imgui.ImDrawList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3fc;

@Environment(EnvType.CLIENT)
public final class OffscreenIndicator {
   private static final int SHADOW_COLOR = -2013265920;
   private static final float ORIGIN_EPSILON_SQ = 9.0F;
   private static final float EDGE_MARGIN = 0.0F;
   private static final float WIDTH_SCALE = 0.49F;

   private OffscreenIndicator() {
   }

   public static float originX(float f) {
      return f * 0.5F;
   }

   public static float originY(String string, float f) {
      return "Feet".equals(string) ? f - 2.0F : f * 0.5F;
   }

   public static boolean draw(ImDrawList imDrawList, MinecraftClient minecraftClient, float f, float g, float h, float i, Vec3d vec3d, int j, float k) {
      return drawWithFade(imDrawList, minecraftClient, f, g, h, i, vec3d, j, k, null, 0.0);
   }

   public static boolean drawWithFade(
      ImDrawList imDrawList, MinecraftClient minecraftClient, float f, float g, float h, float i, Vec3d vec3d, int j, float k, Vec3d vec3d2, double d
   ) {
      Camera var12 = minecraftClient.gameRenderer.getCamera();
      if (var12 != null && var12.isReady()) {
         OffscreenIndicator.a var13 = isInFront(var12, vec3d) ? a(var12, minecraftClient, f, g, vec3d) : a(var12, f, g, vec3d);
         if (var13 == null) {
            return false;
         } else {
            float var14 = var13.x();
            float var15 = var13.y();
            float var16 = var14 - h;
            float var17 = var15 - i;
            if (var16 * var16 + var17 * var17 < 9.0F) {
               return false;
            } else {
               int var18 = j >> 16 & 0xFF;
               int var19 = j >> 8 & 0xFF;
               int var20 = j & 0xFF;
               int var21 = j >>> 24 & 0xFF;
               int var22 = var21 << 24 | var20 << 16 | var19 << 8 | var18;
               float var23 = Math.max(0.5F, Math.min(6.0F, k * 0.49F));
               float var24 = var23 + 0.735F;
               TracerLineBatch.add(h, i, var14, var15, j, var23, -2013265920, var24);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private static OffscreenIndicator.a a(Camera camera, MinecraftClient minecraftClient, float f, float g, Vec3d vec3d) {
      Vec3d var5 = minecraftClient.gameRenderer.project(vec3d);
      if (!a(var5)) {
         return null;
      } else {
         float var6 = (float)var5.x;
         float var7 = (float)var5.y;
         float var8 = (var6 + 1.0F) * 0.5F * f;
         float var9 = (1.0F - var7) * 0.5F * g;
         float var10 = 0.0F;
         float var11 = 0.0F;
         float var12 = Math.max(var10, f - 0.0F);
         float var13 = Math.max(var11, g - 0.0F);
         boolean var14 = var8 >= var10 && var8 <= var12 && var9 >= var11 && var9 <= var13;
         if (var14) {
            return new OffscreenIndicator.a(var8, var9);
         } else {
            float var15 = f * 0.5F;
            float var16 = g * 0.5F;
            float var17 = var8 - var15;
            float var18 = var9 - var16;
            if (Math.abs(var17) < 1.0E-4F && Math.abs(var18) < 1.0E-4F) {
               return null;
            } else {
               float var19 = var17 > 0.0F ? (var12 - var15) / var17 : (var10 - var15) / var17;
               float var20 = var18 > 0.0F ? (var13 - var16) / var18 : (var11 - var16) / var18;
               float var21 = Math.min(Math.abs(var19), Math.abs(var20));
               float var22 = var15 + var17 * var21;
               float var23 = var16 + var18 * var21;
               var22 = Math.max(var10, Math.min(var12, var22));
               var23 = Math.max(var11, Math.min(var13, var23));
               return new OffscreenIndicator.a(var22, var23);
            }
         }
      }
   }

   private static OffscreenIndicator.a a(Camera camera, float f, float g, Vec3d vec3d) {
      Vec3d var4 = vec3d.subtract(camera.getCameraPos());
      Vector3fc var5 = camera.getDiagonalPlane();
      Vector3fc var6 = camera.getVerticalPlane();
      float var7 = (float)(-(var4.x * var5.x() + var4.y * var5.y() + var4.z * var5.z()));
      float var8 = (float)(-(var4.x * var6.x() + var4.y * var6.y() + var4.z * var6.z()));
      if (Math.abs(var7) < 1.0E-4F && Math.abs(var8) < 1.0E-4F) {
         var8 = 1.0F;
      }

      return a(f, g, var7, var8);
   }

   private static OffscreenIndicator.a a(float f, float g, float h, float i) {
      float var4 = 0.0F;
      float var5 = 0.0F;
      float var6 = Math.max(var4, f - 0.0F);
      float var7 = Math.max(var5, g - 0.0F);
      float var8 = f * 0.5F;
      float var9 = g * 0.5F;
      float var10 = Math.abs(h) < 1.0E-6F ? Float.MAX_VALUE : (h > 0.0F ? (var6 - var8) / h : (var4 - var8) / h);
      float var11 = Math.abs(i) < 1.0E-6F ? Float.MAX_VALUE : (i > 0.0F ? (var7 - var9) / i : (var5 - var9) / i);
      float var12 = Math.min(Math.abs(var10), Math.abs(var11));
      float var13 = Math.max(var4, Math.min(var6, var8 + h * var12));
      float var14 = Math.max(var5, Math.min(var7, var9 + i * var12));
      return new OffscreenIndicator.a(var13, var14);
   }

   public static boolean isInFront(Camera camera, Vec3d vec3d) {
      Vec3d var2 = vec3d.subtract(camera.getCameraPos());
      Vector3fc var3 = camera.getHorizontalPlane();
      return var2.x * var3.x() + var2.y * var3.y() + var2.z * var3.z() > 0.05;
   }

   private static boolean a(Vec3d vec3d) {
      return Double.isFinite(vec3d.x) && Double.isFinite(vec3d.y) && Double.isFinite(vec3d.z);
   }

   @Environment(EnvType.CLIENT)
   private record a(float x, float y) {
   }
}
