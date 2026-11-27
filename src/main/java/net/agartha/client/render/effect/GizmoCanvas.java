package net.agartha.client.render.effect;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.Gizmo;
import net.minecraft.world.debug.gizmo.GizmoDrawer;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.VisibilityConfigurable;
import org.joml.Vector3fc;

@Environment(EnvType.CLIENT)
public final class GizmoCanvas implements Gizmo {
   private static final int MAX_QUADS = 12000;
   private static final int MAX_LINES = 4000;
   private static final int MIN_ALPHA = 3;
   private final Vec3d[] vertices = new Vec3d[4096];
   private double[] quadPos = new double[3072];
   private int[] quadColor = new int[256];
   private int quads;
   private double[] linePos = new double[384];
   private int[] lineColor = new int[64];
   private float[] lineWidth = new float[64];
   private int lines;
   private double camX;
   private double camY;
   private double camZ;
   private double rightX;
   private double rightY;
   private double rightZ;
   private double upX;
   private double upY;
   private double upZ;

   public void begin(Camera camera) {
      this.quads = 0;
      this.lines = 0;
      Vec3d var2 = camera.getCameraPos();
      this.camX = var2.x;
      this.camY = var2.y;
      this.camZ = var2.z;
      Vector3fc var3 = camera.getDiagonalPlane();
      Vector3fc var4 = camera.getVerticalPlane();
      this.rightX = -var3.x();
      this.rightY = -var3.y();
      this.rightZ = -var3.z();
      this.upX = var4.x();
      this.upY = var4.y();
      this.upZ = var4.z();
   }

   public void submit(boolean bl) {
      if (this.quads != 0 || this.lines != 0) {
         VisibilityConfigurable var2 = GizmoDrawing.collect(this);
         if (bl) {
            var2.ignoreOcclusion();
         }
      }
   }

   public double cameraX() {
      return this.camX;
   }

   public double cameraY() {
      return this.camY;
   }

   public double cameraZ() {
      return this.camZ;
   }

   public double distanceSqr(double d, double e, double f) {
      double var7 = d - this.camX;
      double var9 = e - this.camY;
      double var11 = f - this.camZ;
      return var7 * var7 + var9 * var9 + var11 * var11;
   }

   public void quad(double d, double e, double f, double g, double h, double i, double j, double k, double l, double m, double n, double o, int p) {
      if (ColorHelper.getAlpha(p) >= 3 && this.a()) {
         int var26 = this.quads * 12;
         double[] var27 = this.quadPos;
         var27[var26] = d;
         var27[var26 + 1] = e;
         var27[var26 + 2] = f;
         var27[var26 + 3] = g;
         var27[var26 + 4] = h;
         var27[var26 + 5] = i;
         var27[var26 + 6] = j;
         var27[var26 + 7] = k;
         var27[var26 + 8] = l;
         var27[var26 + 9] = m;
         var27[var26 + 10] = n;
         var27[var26 + 11] = o;
         this.quadColor[this.quads++] = p;
      }
   }

   public void line(double d, double e, double f, double g, double h, double i, int j, float k) {
      if (ColorHelper.getAlpha(j) >= 3 && this.b()) {
         int var15 = this.lines * 6;
         double[] var16 = this.linePos;
         var16[var15] = d;
         var16[var15 + 1] = e;
         var16[var15 + 2] = f;
         var16[var15 + 3] = g;
         var16[var15 + 4] = h;
         var16[var15 + 5] = i;
         this.lineColor[this.lines] = j;
         this.lineWidth[this.lines++] = k;
      }
   }

   public void shape(float[] fs, double d, double e, double f, double g, double h, double i, double j, double k, double l, int m) {
      if (ColorHelper.getAlpha(m) >= 3) {
         for (int var21 = 0; var21 + 7 < fs.length; var21 += 8) {
            float var22 = fs[var21];
            float var23 = fs[var21 + 1];
            float var24 = fs[var21 + 2];
            float var25 = fs[var21 + 3];
            float var26 = fs[var21 + 4];
            float var27 = fs[var21 + 5];
            float var28 = fs[var21 + 6];
            float var29 = fs[var21 + 7];
            this.quad(
               d + g * var22 + j * var23,
               e + h * var22 + k * var23,
               f + i * var22 + l * var23,
               d + g * var24 + j * var25,
               e + h * var24 + k * var25,
               f + i * var24 + l * var25,
               d + g * var26 + j * var27,
               e + h * var26 + k * var27,
               f + i * var26 + l * var27,
               d + g * var28 + j * var29,
               e + h * var28 + k * var29,
               f + i * var28 + l * var29,
               m
            );
         }
      }
   }

   public void sprite(float[] fs, double d, double e, double f, float g, float h, int i) {
      this.sprite(fs, d, e, f, g, g, h, i);
   }

   public void sprite(float[] fs, double d, double e, double f, float g, float h, float i, int j) {
      if (ColorHelper.getAlpha(j) >= 3) {
         float var12 = MathHelper.cos(i);
         float var13 = MathHelper.sin(i);
         double var14 = (this.rightX * var12 + this.upX * var13) * g;
         double var16 = (this.rightY * var12 + this.upY * var13) * g;
         double var18 = (this.rightZ * var12 + this.upZ * var13) * g;
         double var20 = (this.upX * var12 - this.rightX * var13) * h;
         double var22 = (this.upY * var12 - this.rightY * var13) * h;
         double var24 = (this.upZ * var12 - this.rightZ * var13) * h;
         this.shape(fs, d, e, f, var14, var16, var18, var20, var22, var24, j);
      }
   }

   public void band(double d, double e, double f, float g, float h, float i, float j, int k, int l) {
      if (!(h <= g) && k > 0 && ColorHelper.getAlpha(l) >= 3) {
         double var13 = this.camY >= e ? -1.0 : 1.0;
         float var15 = (j - i) / k;
         float var16 = MathHelper.cos(i);
         float var17 = MathHelper.sin(i);

         for (int var18 = 1; var18 <= k; var18++) {
            float var19 = i + var15 * var18;
            float var20 = MathHelper.cos(var19);
            float var21 = MathHelper.sin(var19);
            this.quad(
               d + var16 * g,
               e,
               f + var13 * var17 * g,
               d + var16 * h,
               e,
               f + var13 * var17 * h,
               d + var20 * h,
               e,
               f + var13 * var21 * h,
               d + var20 * g,
               e,
               f + var13 * var21 * g,
               l
            );
            var16 = var20;
            var17 = var21;
         }
      }
   }

   public void ring(double d, double e, double f, float g, float h, int i, int j) {
      this.band(d, e, f, g, h, 0.0F, (float) (Math.PI * 2), i, j);
   }

   public void draw(GizmoDrawer consumer, float opacity) {
      boolean var3 = opacity < 1.0F;
      double[] var4 = this.quadPos;
      int var5 = 0;

      for (int var6 = 0; var5 < this.quads; var6 += 12) {
         int var7 = var3 ? ColorHelper.scaleAlpha(this.quadColor[var5], opacity) : this.quadColor[var5];
         consumer.addQuad(
            this.a(var4[var6], var4[var6 + 1], var4[var6 + 2]),
            this.a(var4[var6 + 3], var4[var6 + 4], var4[var6 + 5]),
            this.a(var4[var6 + 6], var4[var6 + 7], var4[var6 + 8]),
            this.a(var4[var6 + 9], var4[var6 + 10], var4[var6 + 11]),
            var7
         );
         var5++;
      }

      double[] var9 = this.linePos;
      int var10 = 0;

      for (int var11 = 0; var10 < this.lines; var11 += 6) {
         int var8 = var3 ? ColorHelper.scaleAlpha(this.lineColor[var10], opacity) : this.lineColor[var10];
         consumer.addLine(
            this.a(var9[var11], var9[var11 + 1], var9[var11 + 2]), this.a(var9[var11 + 3], var9[var11 + 4], var9[var11 + 5]), var8, this.lineWidth[var10]
         );
         var10++;
      }
   }

   private Vec3d a(double d, double e, double f) {
      long var7 = Double.doubleToRawLongBits(d) * -7046029254386353131L
         ^ Double.doubleToRawLongBits(e) * -4417276706812531889L
         ^ Double.doubleToRawLongBits(f) * 1609587929392839161L;
      int var9 = (int)(var7 >>> 52);
      Vec3d var10 = this.vertices[var9];
      if (var10 != null && var10.x == d && var10.y == e && var10.z == f) {
         return var10;
      } else {
         Vec3d var11 = new Vec3d(d, e, f);
         this.vertices[var9] = var11;
         return var11;
      }
   }

   private boolean a() {
      if (this.quads < this.quadColor.length) {
         return true;
      } else if (this.quads >= 12000) {
         return false;
      } else {
         int var1 = Math.min(12000, this.quadColor.length * 2);
         this.quadColor = Arrays.copyOf(this.quadColor, var1);
         this.quadPos = Arrays.copyOf(this.quadPos, var1 * 12);
         return true;
      }
   }

   private boolean b() {
      if (this.lines < this.lineColor.length) {
         return true;
      } else if (this.lines >= 4000) {
         return false;
      } else {
         int var1 = Math.min(4000, this.lineColor.length * 2);
         this.lineColor = Arrays.copyOf(this.lineColor, var1);
         this.lineWidth = Arrays.copyOf(this.lineWidth, var1);
         this.linePos = Arrays.copyOf(this.linePos, var1 * 6);
         return true;
      }
   }
}
