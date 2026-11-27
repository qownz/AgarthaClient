package net.agartha.client.render.effect;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public final class ParticleSystem {
   public static final byte ORBIT = 0;
   public static final byte SHARD = 1;
   public static final byte FLARE = 2;
   public static final byte TWINKLE = 3;
   public static final byte EMBER = 4;
   private static final double CULL_DISTANCE_SQR = 9216.0;
   private static final double DETAIL_DISTANCE_SQR = 576.0;
   private static final float GRAVITY = 9.5F;
   private final int capacity;
   private final double[] x;
   private final double[] y;
   private final double[] z;
   private final float[] vx;
   private final float[] vy;
   private final float[] vz;
   private final float[] age;
   private final float[] life;
   private final float[] size;
   private final float[] rot;
   private final float[] spin;
   private final byte[] kind;
   private final int[] color;
   private final int[] light;
   private final double[] ax;
   private final double[] ay;
   private final double[] az;
   private final float[] radius;
   private final float[] angle;
   private final float[] turn;
   private final float[] rise;
   private final byte[] ring;
   private int count;

   public ParticleSystem(int i) {
      this.capacity = i;
      this.x = new double[i];
      this.y = new double[i];
      this.z = new double[i];
      this.vx = new float[i];
      this.vy = new float[i];
      this.vz = new float[i];
      this.age = new float[i];
      this.life = new float[i];
      this.size = new float[i];
      this.rot = new float[i];
      this.spin = new float[i];
      this.kind = new byte[i];
      this.color = new int[i];
      this.light = new int[i];
      this.ax = new double[i];
      this.ay = new double[i];
      this.az = new double[i];
      this.radius = new float[i];
      this.angle = new float[i];
      this.turn = new float[i];
      this.rise = new float[i];
      this.ring = new byte[i];
   }

   public int count() {
      return this.count;
   }

   public void clear() {
      this.count = 0;
   }

   public void spawn(byte b, double d, double e, double f, float g, float h, float i, float j, float k, float l, float m, int n, int o, int p) {
      int var18 = this.a(p);
      this.x[var18] = d;
      this.y[var18] = e;
      this.z[var18] = f;
      this.vx[var18] = g;
      this.vy[var18] = h;
      this.vz[var18] = i;
      this.age[var18] = 0.0F;
      this.life[var18] = Math.max(0.05F, j);
      this.size[var18] = k;
      this.rot[var18] = l;
      this.spin[var18] = m;
      this.kind[var18] = b;
      this.color[var18] = n;
      this.light[var18] = o;
      this.ring[var18] = 0;
   }

   public void orbit(double d, double e, double f, float g, float h, float i, float j, int k, float l, float m, int n, int o, int p) {
      int var17 = this.a(p);
      this.ax[var17] = d;
      this.ay[var17] = e;
      this.az[var17] = f;
      this.radius[var17] = g;
      this.angle[var17] = h;
      this.turn[var17] = i;
      this.rise[var17] = j;
      this.ring[var17] = (byte)Math.max(0, Math.min(64, k));
      this.age[var17] = 0.0F;
      this.life[var17] = Math.max(0.05F, l);
      this.size[var17] = m;
      this.rot[var17] = h;
      this.spin[var17] = i * 0.5F;
      this.kind[var17] = 0;
      this.color[var17] = n;
      this.light[var17] = o;
      this.b(var17);
   }

   private int a(int i) {
      int var2 = Math.min(this.capacity, Math.max(1, i));
      return this.count < var2 ? this.count++ : this.a();
   }

   public void trim(int i) {
      while (this.count > Math.max(0, i)) {
         this.c(this.a());
      }
   }

   public void step(float f) {
      if (!(f <= 0.0F)) {
         for (int var2 = this.count - 1; var2 >= 0; var2--) {
            float var3 = this.age[var2] + f;
            if (var3 >= this.life[var2]) {
               this.c(var2);
            } else {
               this.age[var2] = var3;
               this.rot[var2] = this.rot[var2] + this.spin[var2] * f;
               switch (this.kind[var2]) {
                  case 0:
                     this.b(var2);
                     break;
                  case 1:
                     float var6 = Math.max(0.0F, 1.0F - 1.8F * f);
                     this.vx[var2] = this.vx[var2] * var6;
                     this.vz[var2] = this.vz[var2] * var6;
                     this.vy[var2] = this.vy[var2] * var6 - 5.225F * f;
                     this.a(var2, f);
                  case 2:
                  default:
                     break;
                  case 3:
                     float var5 = Math.max(0.0F, 1.0F - 4.0F * f);
                     this.vx[var2] = this.vx[var2] * var5;
                     this.vy[var2] = this.vy[var2] * var5;
                     this.vz[var2] = this.vz[var2] * var5;
                     this.a(var2, f);
                     break;
                  case 4:
                     float var4 = Math.max(0.0F, 1.0F - 2.2F * f);
                     this.vx[var2] = this.vx[var2] * var4;
                     this.vz[var2] = this.vz[var2] * var4;
                     this.vy[var2] = this.vy[var2] * var4 + 0.9F * f;
                     this.a(var2, f);
               }
            }
         }
      }
   }

   private void a(int i, float f) {
      this.x[i] = this.x[i] + this.vx[i] * f;
      this.y[i] = this.y[i] + this.vy[i] * f;
      this.z[i] = this.z[i] + this.vz[i] * f;
   }

   private void b(int i) {
      float var2 = this.age[i] / this.life[i];
      float var3 = this.angle[i] + this.turn[i] * this.age[i] * (1.0F - 0.35F * var2);
      float var4 = this.radius[i] * (1.0F + 0.85F * a(var2));
      this.x[i] = this.ax[i] + MathHelper.cos(var3) * var4;
      this.z[i] = this.az[i] + MathHelper.sin(var3) * var4;
      this.y[i] = this.ay[i] + this.rise[i] * this.age[i] * (1.0F - 0.3F * var2);
   }

   public void draw(GizmoCanvas arg, float f) {
      for (int var3 = 0; var3 < this.count; var3++) {
         double var4 = this.x[var3];
         double var6 = this.y[var3];
         double var8 = this.z[var3];
         double var10 = arg.distanceSqr(var4, var6, var8);
         if (!(var10 > 9216.0)) {
            boolean var12 = var10 < 576.0;
            float var13 = this.age[var3] / this.life[var3];
            int var14 = this.color[var3];
            switch (this.kind[var3]) {
               case 0:
                  this.a(arg, var3, var13, f, var12);
                  break;
               case 1:
                  float var21 = f * Math.min(1.0F, var13 * 12.0F) * (1.0F - var13 * var13);
                  if (!(var21 < 0.012F)) {
                     float var24 = 0.075F;
                     arg.line(
                        var4 - this.vx[var3] * var24,
                        var6 - this.vy[var3] * var24,
                        var8 - this.vz[var3] * var24,
                        var4,
                        var6,
                        var8,
                        ColorHelper.scaleAlpha(var14, var21 * 0.8F),
                        2.4F
                     );
                     if (var12) {
                        arg.sprite(EffectSprites.DIAMOND, var4, var6, var8, this.size[var3] * 0.55F, this.rot[var3], ColorHelper.scaleAlpha(this.light[var3], var21));
                     }
                  }
                  break;
               case 2:
                  float var20 = var13 < 0.18F ? a(var13 / 0.18F) : 1.0F - b((var13 - 0.18F) / 0.82F);
                  float var23 = f * var20;
                  if (!(var23 < 0.012F)) {
                     float var25 = this.size[var3] * (0.5F + 0.9F * var20);
                     arg.sprite(EffectSprites.HALO_LOW, var4, var6, var8, var25 * 1.9F, -this.rot[var3], ColorHelper.scaleAlpha(var14, var23 * 0.4F));
                     arg.sprite(EffectSprites.STAR, var4, var6, var8, var25, this.rot[var3], ColorHelper.scaleAlpha(var14, var23));
                     arg.sprite(EffectSprites.STAR, var4, var6, var8, var25 * 0.45F, this.rot[var3], ColorHelper.scaleAlpha(this.light[var3], var23));
                  }
                  break;
               case 3:
                  float var19 = MathHelper.sin(var13 * (float) Math.PI * 2.0F);
                  float var22 = Math.abs(var19) * (var13 < 0.5F ? 1.0F : 0.6F);
                  float var17 = f * var22 * (1.0F - var13 * 0.4F);
                  if (!(var17 < 0.012F)) {
                     float var18 = this.size[var3] * (0.35F + 0.65F * var22);
                     arg.sprite(
                        var12 ? EffectSprites.SPARK : EffectSprites.DIAMOND,
                        var4,
                        var6,
                        var8,
                        var18,
                        this.rot[var3],
                        ColorHelper.scaleAlpha(ColorHelper.lerp(var22, var14, this.light[var3]), var17)
                     );
                  }
                  break;
               default:
                  float var15 = f * Math.min(1.0F, var13 * 8.0F) * (1.0F - var13);
                  if (!(var15 < 0.012F)) {
                     int var16 = ColorHelper.lerp(Math.min(1.0F, var13 * 2.5F), this.light[var3], var14);
                     arg.sprite(EffectSprites.DIAMOND, var4, var6, var8, this.size[var3] * (1.0F - 0.5F * var13), this.rot[var3], ColorHelper.scaleAlpha(var16, var15));
                  }
            }
         }
      }
   }

   private void a(GizmoCanvas arg, int i, float f, float g, boolean bl) {
      float var6 = g * Math.min(1.0F, f * 6.0F) * (1.0F - f * f * f);
      if (!(var6 < 0.012F)) {
         double var7 = this.x[i];
         double var9 = this.y[i];
         double var11 = this.z[i];
         float var13 = 0.8F + 0.2F * MathHelper.sin(this.age[i] * 14.0F + this.angle[i] * 3.0F);
         float var14 = this.size[i] * var13 * (1.0F - 0.3F * f);
         int var15 = this.color[i];
         arg.sprite(bl ? EffectSprites.STAR : EffectSprites.DIAMOND, var7, var9, var11, var14, this.rot[i], ColorHelper.scaleAlpha(var15, var6));
         if (bl) {
            arg.sprite(EffectSprites.SPARK, var7, var9, var11, var14 * 0.5F, -this.rot[i], ColorHelper.scaleAlpha(this.light[i], var6 * 0.9F));
            byte var16 = this.ring[i];
            if (var16 >= 3) {
               float var17 = this.age[i];
               float var18 = var17 / this.life[i];
               float var19 = this.angle[i] + (float) (Math.PI * 2) / var16 + this.turn[i] * var17 * (1.0F - 0.35F * var18);
               float var20 = this.radius[i] * (1.0F + 0.85F * a(var18));
               double var21 = this.ax[i] + MathHelper.cos(var19) * var20;
               double var23 = this.az[i] + MathHelper.sin(var19) * var20;
               arg.line(var7, var9, var11, var21, var9, var23, ColorHelper.scaleAlpha(this.light[i], var6 * 0.32F), 1.4F);
            }
         }
      }
   }

   private static float a(float f) {
      float var1 = 1.0F - Math.max(0.0F, Math.min(1.0F, f));
      return 1.0F - var1 * var1 * var1;
   }

   private static float b(float f) {
      float var1 = Math.max(0.0F, Math.min(1.0F, f));
      return var1 * var1;
   }

   private int a() {
      int var1 = 0;
      float var2 = -1.0F;

      for (int var3 = 0; var3 < this.count; var3++) {
         float var4 = this.age[var3] / this.life[var3];
         if (var4 > var2) {
            var2 = var4;
            var1 = var3;
         }
      }

      return var1;
   }

   private void c(int i) {
      int var2 = --this.count;
      if (i != var2) {
         this.x[i] = this.x[var2];
         this.y[i] = this.y[var2];
         this.z[i] = this.z[var2];
         this.vx[i] = this.vx[var2];
         this.vy[i] = this.vy[var2];
         this.vz[i] = this.vz[var2];
         this.age[i] = this.age[var2];
         this.life[i] = this.life[var2];
         this.size[i] = this.size[var2];
         this.rot[i] = this.rot[var2];
         this.spin[i] = this.spin[var2];
         this.kind[i] = this.kind[var2];
         this.color[i] = this.color[var2];
         this.light[i] = this.light[var2];
         this.ax[i] = this.ax[var2];
         this.ay[i] = this.ay[var2];
         this.az[i] = this.az[var2];
         this.radius[i] = this.radius[var2];
         this.angle[i] = this.angle[var2];
         this.turn[i] = this.turn[var2];
         this.rise[i] = this.rise[var2];
         this.ring[i] = this.ring[var2];
      }
   }
}
