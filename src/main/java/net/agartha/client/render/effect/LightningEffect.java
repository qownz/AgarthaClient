package net.agartha.client.render.effect;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.util.function.LongPredicate;
import net.agartha.client.render.effect.EffectSprites;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public final class LightningEffect {
   private static final int BOLT_POINTS = 28;
   private static final int BRANCH_POINTS = 5;
   private static final float STRIKE_SECONDS = 0.45F;
   private static final double BOLT_HEIGHT = 16.0;
   private static final int BEAM_STEPS = 10;
   private static final int BOLTS = 3;
   private final Long2ObjectOpenHashMap<LightningEffect.a[]> strikes = new Long2ObjectOpenHashMap();
   private static final int[] CORNER_ORDER = new int[]{0, 1, 4, 5, 3, 2, 7, 6};

   public void retain(LongPredicate longPredicate) {
      LongIterator var2 = this.strikes.keySet().iterator();

      while (var2.hasNext()) {
         if (!longPredicate.test(var2.nextLong())) {
            var2.remove();
         }
      }
   }

   public void clear() {
      this.strikes.clear();
   }

   public void draw(GizmoCanvas arg, long l, double d, double e, double f, double g, float h, double i, int j, boolean bl, boolean bl2) {
      if (!(h <= 0.01F)) {
         double var18 = d + 0.5;
         double var20 = f + 0.5;
         if (bl) {
            beam(arg, var18, e + 1.0, var20, g, h, i, j);
         }

         if (bl2) {
            this.a(arg, l, var18, e + 1.0, var20, g - (e + 1.0), h, i, j);
         }
      }
   }

   public static void selection(GizmoCanvas arg, double d, double e, double f, double g, double h, double i, int j, float k, double l) {
      int var17 = ColorHelper.scaleAlpha(j, 0.2F * k);
      arg.quad(g, e, f, g, h, f, g, h, i, g, e, i, var17);
      arg.quad(d, e, f, d, e, i, d, h, i, d, h, f, var17);
      arg.quad(d, e, f, d, h, f, g, h, f, g, e, f, var17);
      arg.quad(d, e, i, g, e, i, g, h, i, d, h, i, var17);
      arg.quad(d, h, f, d, h, i, g, h, i, g, h, f, var17);
      arg.quad(d, e, f, g, e, f, g, e, i, d, e, i, var17);
      a(arg, d, e, f, g, h, i, ColorHelper.scaleAlpha(j, 0.13F * k), 9.0F);
      a(arg, d, e, f, g, h, i, ColorHelper.scaleAlpha(j, 0.32F * k), 4.5F);
      a(arg, d, e, f, g, h, i, ColorHelper.scaleAlpha(j, 0.95F * k), 2.2F);
      int var18 = ColorHelper.lerp(0.55F, j | 0xFF000000, -1);

      for (int var19 = 0; var19 < 8; var19++) {
         float var20 = (float)Math.pow(Math.max(0.0, Math.sin(l * 2.4 - CORNER_ORDER[var19] * 0.78)), 6.0);
         arg.sprite(
            EffectSprites.SPARK,
            (var19 & 1) == 0 ? d : g,
            (var19 & 2) == 0 ? e : h,
            (var19 & 4) == 0 ? f : i,
            0.1F * (0.75F + 0.6F * var20),
            (float)(l * 0.6),
            ColorHelper.scaleAlpha(var18, k * (0.2F + 0.8F * var20))
         );
      }
   }

   private static void a(GizmoCanvas arg, double d, double e, double f, double g, double h, double i, int j, float k) {
      arg.line(d, e, f, g, e, f, j, k);
      arg.line(g, e, f, g, e, i, j, k);
      arg.line(g, e, i, d, e, i, j, k);
      arg.line(d, e, i, d, e, f, j, k);
      arg.line(d, h, f, g, h, f, j, k);
      arg.line(g, h, f, g, h, i, j, k);
      arg.line(g, h, i, d, h, i, j, k);
      arg.line(d, h, i, d, h, f, j, k);
      arg.line(d, e, f, d, h, f, j, k);
      arg.line(g, e, f, g, h, f, j, k);
      arg.line(g, e, i, g, h, i, j, k);
      arg.line(d, e, i, d, h, i, j, k);
   }

   public static void beam(GizmoCanvas arg, double d, double e, double f, double g, float h, double i, int j) {
      if (!(g <= e)) {
         double var13 = d - a(arg);
         double var15 = f - b(arg);
         double var17 = Math.sqrt(var13 * var13 + var15 * var15);
         if (!(var17 < 1.0E-4)) {
            double var19 = -var15 / var17;
            double var21 = var13 / var17;
            float var23 = 0.85F + 0.15F * (float)Math.sin(i * 2.4);
            double var24 = (g - e) / 10.0;

            for (int var26 = 0; var26 < 10; var26++) {
               double var27 = e + var24 * var26;
               double var29 = var27 + var24;
               float var31 = 1.0F - var26 / 10.0F;
               float var32 = h * var23 * var31 * var31;
               a(arg, d, f, var19, var21, var27, var29, 0.12, ColorHelper.scaleAlpha(-1, 0.55F * var32));
               a(arg, d, f, var19, var21, var27, var29, 0.3, ColorHelper.scaleAlpha(j, 0.42F * var32));
               a(arg, d, f, var19, var21, var27, var29, 0.75, ColorHelper.scaleAlpha(j, 0.14F * var32));
            }
         }
      }
   }

   private static void a(GizmoCanvas arg, double d, double e, double f, double g, double h, double i, double j, int k) {
      double var16 = d - f * j;
      double var18 = e - g * j;
      double var20 = d + f * j;
      double var22 = e + g * j;
      arg.quad(var16, h, var18, var20, h, var22, var20, i, var22, var16, i, var18, k);
      arg.quad(var20, h, var22, var16, h, var18, var16, i, var18, var20, i, var22, k);
   }

   private void a(GizmoCanvas arg, long l, double d, double e, double f, double g, float h, double i, int j) {
      LightningEffect.a[] var16 = (LightningEffect.a[])this.strikes.get(l);
      if (var16 == null) {
         var16 = new LightningEffect.a[3];

         for (int var17 = 0; var17 < 3; var17++) {
            var16[var17] = new LightningEffect.a(l + var17 * 5369127L);
         }

         this.strikes.put(l, var16);
      }

      for (LightningEffect.a var20 : var16) {
         if (i >= var20.next || i < var20.at - 10.0) {
            var20.strike(i, Math.max(16.0, g));
         }

         float var21 = (float)(i - var20.at);
         if (!(var21 < 0.0F) && !(var21 > 0.45F)) {
            float var22 = 1.0F - var21 / 0.45F;
            float var23 = var21 < 0.05F ? 1.0F : 0.5F + 0.5F * (float)Math.abs(Math.sin(var21 * 60.0));
            float var24 = h * var22 * var23;
            a(arg, var20.boltX, var20.boltY, var20.boltZ, 28, d, e, f, var24, j);

            for (int var25 = 0; var25 < var20.branches; var25++) {
               a(arg, var20.branchX, var20.branchY, var20.branchZ, var25 * 5, 5, d, e, f, var24 * 0.7F, j);
            }
         }
      }
   }

   private static void a(GizmoCanvas arg, float[] fs, float[] gs, float[] hs, int i, double d, double e, double f, float g, int j) {
      a(arg, fs, gs, hs, 0, i, d, e, f, g, j);
   }

   private static void a(GizmoCanvas arg, float[] fs, float[] gs, float[] hs, int i, int j, double d, double e, double f, float g, int k) {
      int var14 = ColorHelper.scaleAlpha(k, 0.45F * g);
      int var15 = ColorHelper.scaleAlpha(-1, 0.95F * g);

      for (int var16 = i + 1; var16 < i + j; var16++) {
         double var17 = d + fs[var16 - 1];
         double var19 = e + gs[var16 - 1];
         double var21 = f + hs[var16 - 1];
         double var23 = d + fs[var16];
         double var25 = e + gs[var16];
         double var27 = f + hs[var16];
         arg.line(var17, var19, var21, var23, var25, var27, var14, 9.0F);
         arg.line(var17, var19, var21, var23, var25, var27, var15, 2.6F);
      }
   }

   private static double a(GizmoCanvas arg) {
      return arg.cameraX();
   }

   private static double b(GizmoCanvas arg) {
      return arg.cameraZ();
   }

   @Environment(EnvType.CLIENT)
   private static final class a {
      final float[] boltX = new float[28];
      final float[] boltY = new float[28];
      final float[] boltZ = new float[28];
      final float[] branchX = new float[10];
      final float[] branchY = new float[10];
      final float[] branchZ = new float[10];
      int branches;
      double at = -100.0;
      double next;
      long seed;

      a(long l) {
         this.seed = l * -7046029254386353131L + 7146057691288625177L;
         this.next = 0.1 + this.rand() * 1.0;
      }

      void strike(double d, double e) {
         this.at = d;
         this.next = d + 0.7 + this.rand() * 1.1;

         for (int var5 = 0; var5 < 28; var5++) {
            float var6 = var5 / 27.0F;
            float var7 = (float)Math.min(4.0, 0.9 + e / 60.0);
            float var8 = var5 != 0 && var5 != 27 ? var7 * MathHelper.sin(var6 * (float) Math.PI) : 0.0F;
            this.boltX[var5] = (float)((this.rand() - 0.5) * 2.0 * var8);
            this.boltZ[var5] = (float)((this.rand() - 0.5) * 2.0 * var8);
            this.boltY[var5] = (float)(e * (1.0 - var6));
         }

         this.branches = this.rand() < 0.65 ? 2 : 1;

         for (int var11 = 0; var11 < this.branches; var11++) {
            int var12 = 3 + (int)(this.rand() * 20.0);
            float var13 = (float)(this.rand() - 0.5) * 2.0F;
            float var14 = (float)(this.rand() - 0.5) * 2.0F;
            int var9 = var11 * 5;
            this.branchX[var9] = this.boltX[var12];
            this.branchY[var9] = this.boltY[var12];
            this.branchZ[var9] = this.boltZ[var12];

            for (int var10 = 1; var10 < 5; var10++) {
               this.branchX[var9 + var10] = this.branchX[var9 + var10 - 1] + var13 * 0.5F + (float)(this.rand() - 0.5) * 0.5F;
               this.branchY[var9 + var10] = this.branchY[var9 + var10 - 1] - (float)(e / 28.0) * (1.0F + (float)this.rand() * 0.6F);
               this.branchZ[var9 + var10] = this.branchZ[var9 + var10 - 1] + var14 * 0.5F + (float)(this.rand() - 0.5) * 0.5F;
            }
         }
      }

      double rand() {
         this.seed = this.seed ^ this.seed << 13;
         this.seed = this.seed ^ this.seed >>> 7;
         this.seed = this.seed ^ this.seed << 17;
         return (this.seed >>> 11) * 1.110223E-16F;
      }
   }
}
