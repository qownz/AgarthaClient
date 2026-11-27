package net.agartha.client.util.render;

import java.util.List;
import net.agartha.client.util.render.TracerLineBatch;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3fc;

@Environment(EnvType.CLIENT)
public final class WorldLineProjector {
   private static final double NEAR = 0.05;
   private static double camX;
   private static double camY;
   private static double camZ;
   private static double fwdX;
   private static double fwdY;
   private static double fwdZ;
   private static double rightX;
   private static double rightY;
   private static double rightZ;
   private static double upX;
   private static double upY;
   private static double upZ;
   private static double centreX;
   private static double centreY;
   private static double scaleX;
   private static double scaleY;
   private static float screenW;
   private static float screenH;
   private static boolean ready;
   private static final float[] A = new float[2];
   private static final float[] B = new float[2];

   private WorldLineProjector() {
   }

   public static boolean begin(MinecraftClient minecraftClient, float f, float g) {
      Camera var3 = minecraftClient.gameRenderer.getCamera();
      ready = false;
      if (var3 != null && var3.isReady()) {
         Vec3d var4 = var3.getCameraPos();
         Vector3fc var5 = var3.getHorizontalPlane();
         Vector3fc var6 = var3.getDiagonalPlane();
         Vector3fc var7 = var3.getVerticalPlane();
         camX = var4.x;
         camY = var4.y;
         camZ = var4.z;
         fwdX = var5.x();
         fwdY = var5.y();
         fwdZ = var5.z();
         rightX = -var6.x();
         rightY = -var6.y();
         rightZ = -var6.z();
         upX = var7.x();
         upY = var7.y();
         upZ = var7.z();
         Vec3d var8 = minecraftClient.gameRenderer.project(new Vec3d(camX + fwdX, camY + fwdY, camZ + fwdZ));
         Vec3d var9 = minecraftClient.gameRenderer.project(new Vec3d(camX + fwdX + rightX, camY + fwdY + rightY, camZ + fwdZ + rightZ));
         Vec3d var10 = minecraftClient.gameRenderer.project(new Vec3d(camX + fwdX + upX, camY + fwdY + upY, camZ + fwdZ + upZ));
         if (a(var8) && a(var9) && a(var10)) {
            centreX = var8.x;
            centreY = var8.y;
            scaleX = var9.x - var8.x;
            scaleY = var10.y - var8.y;
            screenW = f;
            screenH = g;
            ready = Math.abs(scaleX) > 1.0E-6 && Math.abs(scaleY) > 1.0E-6;
            return ready;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static void draw(List<Vec3d> list, int i, double d, int j, float f, float g, float h) {
      if (ready) {
         int var8 = Math.min(list.size() - 1, (int)Math.ceil(d));
         int var9 = var8 - i;
         if (var9 >= 1) {
            for (int var10 = i + 1; var10 <= var8; var10++) {
               Vec3d var11 = (Vec3d)list.get(var10 - 1);
               Vec3d var12 = (Vec3d)list.get(var10);
               double var13 = var12.x;
               double var15 = var12.y;
               double var17 = var12.z;
               if (var10 > d) {
                  double var19 = d - (var10 - 1);
                  var13 = var11.x + (var12.x - var11.x) * var19;
                  var15 = var11.y + (var12.y - var11.y) * var19;
                  var17 = var11.z + (var12.z - var11.z) * var19;
               }

               float var21 = (float)(var10 - i) / var9;
               float var20 = g + (h - g) * var21;
               segment(var11.x, var11.y, var11.z, var13, var15, var17, a(j, var20), f);
            }
         }
      }
   }

   public static void segment(double d, double e, double f, double g, double h, double i, int j, float k) {
      if (ready && j >>> 24 != 0) {
         double var14 = a(d, e, f);
         double var16 = a(g, h, i);
         if (!(var14 < 0.05) || !(var16 < 0.05)) {
            if (var14 < 0.05) {
               double var18 = (0.05 - var14) / (var16 - var14);
               d += (g - d) * var18;
               e += (h - e) * var18;
               f += (i - f) * var18;
               var14 = 0.05;
            } else if (var16 < 0.05) {
               double var20 = (0.05 - var16) / (var14 - var16);
               g += (d - g) * var20;
               h += (e - h) * var20;
               i += (f - i) * var20;
               var16 = 0.05;
            }

            a(d, e, f, var14, A);
            a(g, h, i, var16, B);
            TracerLineBatch.segment(A[0], A[1], B[0], B[1], j, k);
         }
      }
   }

   private static double a(double d, double e, double f) {
      return (d - camX) * fwdX + (e - camY) * fwdY + (f - camZ) * fwdZ;
   }

   private static void a(double d, double e, double f, double g, float[] fs) {
      double var9 = d - camX;
      double var11 = e - camY;
      double var13 = f - camZ;
      double var15 = centreX + scaleX * (var9 * rightX + var11 * rightY + var13 * rightZ) / g;
      double var17 = centreY + scaleY * (var9 * upX + var11 * upY + var13 * upZ) / g;
      fs[0] = (float)((var15 + 1.0) * 0.5 * screenW);
      fs[1] = (float)((1.0 - var17) * 0.5 * screenH);
   }

   private static int a(int i, float f) {
      int var2 = Math.round((i >>> 24 & 0xFF) * Math.max(0.0F, Math.min(1.0F, f)));
      return var2 << 24 | i & 16777215;
   }

   private static boolean a(Vec3d vec3d) {
      return vec3d != null && Double.isFinite(vec3d.x) && Double.isFinite(vec3d.y);
   }
}
