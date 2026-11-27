package net.agartha.client.l;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class b {
   public static final float[] STAR = a(8, 1.0F, 0.68F, 0.16F);
   public static final float[] SPARK = a(4, 1.0F, 1.0F, 0.13F);
   public static final float[] DIAMOND = new float[]{0.0F, -1.0F, 1.0F, 0.0F, 0.0F, 1.0F, -1.0F, 0.0F};
   public static final float[] DISK = a(16, 0.0F);
   public static final float[] DISK_LOW = a(8, 0.0F);
   public static final float[] HALO = a(24, 0.82F);
   public static final float[] HALO_LOW = a(12, 0.8F);

   private b() {
   }

   private static float[] a(int i, float f, float g, float h) {
      float[] var4 = new float[i * 8];
      double var5 = (Math.PI * 2) / i;

      for (int var7 = 0; var7 < i; var7++) {
         double var8 = (Math.PI / 2) - var7 * var5;
         float var10 = (var7 & 1) == 0 ? f : g;
         double var11 = var5 * 0.5;
         int var13 = var7 * 8;
         var4[var13] = 0.0F;
         var4[var13 + 1] = 0.0F;
         var4[var13 + 2] = (float)(Math.cos(var8 + var11) * h);
         var4[var13 + 3] = (float)(Math.sin(var8 + var11) * h);
         var4[var13 + 4] = (float)(Math.cos(var8) * var10);
         var4[var13 + 5] = (float)(Math.sin(var8) * var10);
         var4[var13 + 6] = (float)(Math.cos(var8 - var11) * h);
         var4[var13 + 7] = (float)(Math.sin(var8 - var11) * h);
      }

      return var4;
   }

   private static float[] a(int i, float f) {
      float[] var2 = new float[i * 8];

      for (int var3 = 0; var3 < i; var3++) {
         double var4 = (Math.PI * 2) * var3 / i;
         double var6 = (Math.PI * 2) * (var3 + 1) / i;
         float var8 = (float)Math.cos(var4);
         float var9 = (float)Math.sin(var4);
         float var10 = (float)Math.cos(var6);
         float var11 = (float)Math.sin(var6);
         int var12 = var3 * 8;
         var2[var12] = var8 * f;
         var2[var12 + 1] = var9 * f;
         var2[var12 + 2] = var8;
         var2[var12 + 3] = var9;
         var2[var12 + 4] = var10;
         var2[var12 + 5] = var11;
         var2[var12 + 6] = var10 * f;
         var2[var12 + 7] = var11 * f;
      }

      return var2;
   }
}
