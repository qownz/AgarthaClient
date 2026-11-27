package net.agartha.client.util;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.Arrays;
import net.agartha.client.module.impl.client.ChunkBlendModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

@Environment(EnvType.CLIENT)
public final class w {
   private static final double EPS = 0.01;
   private static final double CELL = 8.0;
   private static int count;
   private static int[] owners = new int[64];
   private static double[] geo = new double[384];
   private static int[] strokes = new int[64];
   private static int[] fills = new int[64];
   private static float[] widths = new float[64];
   private static final Long2IntOpenHashMap INDEX = new Long2IntOpenHashMap();
   private static double[] px = new double[64];
   private static double[] pz = new double[64];
   private static boolean[] open = new boolean[64];
   private static int points;
   private static Vec3d camera = Vec3d.ZERO;

   private w() {
   }

   public static void beginFrame() {
      count = 0;
      Camera var0 = MinecraftClient.getInstance().gameRenderer.getCamera();
      if (var0 != null) {
         camera = var0.getCameraPos();
      }

      INDEX.clear();
   }

   public static void plate(int i, Box box, int j, float f, int k) {
      if (!l.outOfView(box)) {
         if (ChunkBlendModule.active() == null) {
            a(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, j, f, k);
         } else {
            if (count == owners.length) {
               a();
            }

            int var5 = count++;
            owners[var5] = i;
            int var6 = var5 * 6;
            geo[var6] = box.minX;
            geo[var6 + 1] = box.minY;
            geo[var6 + 2] = box.minZ;
            geo[var6 + 3] = box.maxX;
            geo[var6 + 4] = box.maxY;
            geo[var6 + 5] = box.maxZ;
            strokes[var5] = j;
            fills[var5] = k;
            widths[var5] = f;
            INDEX.put(a(i, box.minX, box.minZ, box.maxX, box.maxZ), var5);
         }
      }
   }

   public static void flush() {
      if (count != 0) {
         ChunkBlendModule var0 = ChunkBlendModule.active();

         for (int var1 = 0; var1 < count; var1++) {
            if (var0 == null) {
               int var2 = var1 * 6;
               a(geo[var2], geo[var2 + 1], geo[var2 + 2], geo[var2 + 3], geo[var2 + 4], geo[var2 + 5], strokes[var1], widths[var1], fills[var1]);
            } else {
               a(var1, var0);
            }
         }

         count = 0;
         INDEX.clear();
      }
   }

   private static void a(int i, ChunkBlendModule chunkBlendModule) {
      int var2 = i * 6;
      double var3 = geo[var2];
      double var5 = geo[var2 + 1];
      double var7 = geo[var2 + 2];
      double var9 = geo[var2 + 3];
      double var11 = geo[var2 + 4];
      double var13 = geo[var2 + 5];
      boolean var15 = chunkBlendModule.merge();
      boolean var16 = !var15 || !a(i, -1, 0);
      boolean var17 = !var15 || !a(i, 1, 0);
      boolean var18 = !var15 || !a(i, 0, -1);
      boolean var19 = !var15 || !a(i, 0, 1);
      double var20 = Math.max(0.0, Math.min(chunkBlendModule.radius(), Math.min(var9 - var3, var13 - var7) * 0.5));
      int var22 = chunkBlendModule.detail();
      points = 0;
      a(4 * (var22 + 1));
      a(var3, var7, var3 + var20, var7 + var20, 180.0, var18 && var16 ? var20 : 0.0, var22, var18);
      a(var9, var7, var9 - var20, var7 + var20, 270.0, var18 && var17 ? var20 : 0.0, var22, var17);
      a(var9, var13, var9 - var20, var13 - var20, 0.0, var19 && var17 ? var20 : 0.0, var22, var19);
      a(var3, var13, var3 + var20, var13 - var20, 90.0, var19 && var16 ? var20 : 0.0, var22, var16);
      int var23 = fills[i];
      if (var23 >>> 24 != 0) {
         DrawStyle var24 = DrawStyle.filled(var23);
         a(
            var3,
            var7,
            var9,
            var13,
            var11,
            var18 && var16 ? var20 : 0.0,
            var18 && var17 ? var20 : 0.0,
            var19 && var17 ? var20 : 0.0,
            var19 && var16 ? var20 : 0.0,
            var22,
            var24
         );
         if (chunkBlendModule.walls()) {
            DrawStyle var25 = DrawStyle.filled(a(var23, chunkBlendModule.shade()));

            for (int var26 = 0; var26 < points; var26++) {
               if (open[var26]) {
                  int var27 = (var26 + 1) % points;
                  a(
                     new Vec3d(px[var26], var11, pz[var26]),
                     new Vec3d(px[var27], var11, pz[var27]),
                     new Vec3d(px[var27], var5, pz[var27]),
                     new Vec3d(px[var26], var5, pz[var26]),
                     var25
                  );
               }
            }
         }
      }

      int var28 = strokes[i];
      if (chunkBlendModule.outline() && var28 >>> 24 != 0) {
         float var29 = widths[i];

         for (int var30 = 0; var30 < points; var30++) {
            if (open[var30]) {
               int var31 = (var30 + 1) % points;
               GizmoDrawing.line(new Vec3d(px[var30], var11, pz[var30]), new Vec3d(px[var31], var11, pz[var31]), var28, var29).ignoreOcclusion();
               GizmoDrawing.line(new Vec3d(px[var30], var5, pz[var30]), new Vec3d(px[var31], var5, pz[var31]), var28, var29).ignoreOcclusion();
            }
         }
      }
   }

   private static void a(double d, double e, double f, double g, double h, double i, int j, float k, int l) {
      if (l >>> 24 != 0) {
         DrawStyle var15 = DrawStyle.filled(l);
         if (camera.y > h) {
            a(d, f, g, i, h, var15);
         } else if (camera.y < e) {
            a(d, f, g, i, e, var15);
         }

         if (camera.z < f) {
            a(new Vec3d(d, h, f), new Vec3d(g, h, f), new Vec3d(g, e, f), new Vec3d(d, e, f), var15);
         }

         if (camera.x > g) {
            a(new Vec3d(g, h, f), new Vec3d(g, h, i), new Vec3d(g, e, i), new Vec3d(g, e, f), var15);
         }

         if (camera.z > i) {
            a(new Vec3d(g, h, i), new Vec3d(d, h, i), new Vec3d(d, e, i), new Vec3d(g, e, i), var15);
         }

         if (camera.x < d) {
            a(new Vec3d(d, h, i), new Vec3d(d, h, f), new Vec3d(d, e, f), new Vec3d(d, e, i), var15);
         }
      }

      if (j >>> 24 != 0) {
         double[] var19 = new double[]{d, g, g, d};
         double[] var16 = new double[]{f, f, i, i};

         for (int var17 = 0; var17 < 4; var17++) {
            int var18 = var17 + 1 & 3;
            GizmoDrawing.line(new Vec3d(var19[var17], h, var16[var17]), new Vec3d(var19[var18], h, var16[var18]), j, k).ignoreOcclusion();
            GizmoDrawing.line(new Vec3d(var19[var17], e, var16[var17]), new Vec3d(var19[var18], e, var16[var18]), j, k).ignoreOcclusion();
            GizmoDrawing.line(new Vec3d(var19[var17], e, var16[var17]), new Vec3d(var19[var17], h, var16[var17]), j, k).ignoreOcclusion();
         }
      }
   }

   private static void a(double d, double e, double f, double g, double h, double i, double j, double k, double l, int m, DrawStyle drawStyle) {
      double var20 = Math.max(i, j);
      double var22 = Math.max(l, k);
      a(d + i, e, f - j, e + var20, h, drawStyle);
      a(d, e + var20, f, g - var22, h, drawStyle);
      a(d + l, g - var22, f - k, g, h, drawStyle);
      a(d + i, e + i, i, 180.0, m, h, drawStyle);
      a(f - j, e + j, j, 270.0, m, h, drawStyle);
      a(f - k, g - k, k, 0.0, m, h, drawStyle);
      a(d + l, g - l, l, 90.0, m, h, drawStyle);
   }

   private static void a(double d, double e, double f, double g, double h, DrawStyle drawStyle) {
      if (!(f - d <= 0.01) && !(g - e <= 0.01)) {
         for (double var11 = Math.floor(d / 8.0) * 8.0; var11 < f - 0.01; var11 += 8.0) {
            double var13 = Math.max(d, var11);
            double var15 = Math.min(f, var11 + 8.0);
            if (!(var15 - var13 <= 0.01)) {
               for (double var17 = Math.floor(e / 8.0) * 8.0; var17 < g - 0.01; var17 += 8.0) {
                  double var19 = Math.max(e, var17);
                  double var21 = Math.min(g, var17 + 8.0);
                  if (!(var21 - var19 <= 0.01)) {
                     a(new Vec3d(var13, h, var19), new Vec3d(var15, h, var19), new Vec3d(var15, h, var21), new Vec3d(var13, h, var21), drawStyle);
                  }
               }
            }
         }
      }
   }

   private static void a(double d, double e, double f, double g, int i, double h, DrawStyle drawStyle) {
      if (!(f <= 0.01)) {
         Vec3d var12 = new Vec3d(d, h, e);
         Vec3d var13 = null;

         for (int var14 = 0; var14 <= i; var14++) {
            double var15 = Math.toRadians(g + 90.0 * var14 / i);
            Vec3d var17 = new Vec3d(d + f * Math.cos(var15), h, e + f * Math.sin(var15));
            if (var13 != null) {
               a(var12, var13, var17, var17, drawStyle);
            }

            var13 = var17;
         }
      }
   }

   private static void a(Vec3d vec3d, Vec3d vec3d2, Vec3d vec3d3, Vec3d vec3d4, DrawStyle drawStyle) {
      double var5 = vec3d2.x - vec3d.x;
      double var7 = vec3d2.y - vec3d.y;
      double var9 = vec3d2.z - vec3d.z;
      double var11 = vec3d3.x - vec3d.x;
      double var13 = vec3d3.y - vec3d.y;
      double var15 = vec3d3.z - vec3d.z;
      double var17 = (var7 * var15 - var9 * var13) * (camera.x - vec3d.x)
         + (var9 * var11 - var5 * var15) * (camera.y - vec3d.y)
         + (var5 * var13 - var7 * var11) * (camera.z - vec3d.z);
      if (var17 >= 0.0) {
         GizmoDrawing.quad(vec3d, vec3d2, vec3d3, vec3d4, drawStyle).ignoreOcclusion();
      } else {
         GizmoDrawing.quad(vec3d4, vec3d3, vec3d2, vec3d, drawStyle).ignoreOcclusion();
      }
   }

   private static void a(double d, double e, double f, double g, double h, double i, int j, boolean bl) {
      if (i <= 0.0) {
         px[points] = d;
         pz[points] = e;
         open[points++] = bl;
      } else {
         for (int var14 = 0; var14 <= j; var14++) {
            double var15 = Math.toRadians(h + 90.0 * var14 / j);
            px[points] = f + i * Math.cos(var15);
            pz[points] = g + i * Math.sin(var15);
            open[points++] = var14 < j || bl;
         }
      }
   }

   private static boolean a(int i, int j, int k) {
      int var3 = i * 6;
      double var4 = geo[var3 + 3] - geo[var3];
      double var6 = geo[var3 + 5] - geo[var3 + 2];
      double var8 = geo[var3] + j * var4;
      double var10 = geo[var3 + 2] + k * var6;
      int var12 = INDEX.get(a(owners[i], var8, var10, var8 + var4, var10 + var6));
      if (var12 >= 0 && var12 != i) {
         int var13 = var12 * 6;
         return Math.abs(geo[var13] - var8) < 0.01
            && Math.abs(geo[var13 + 2] - var10) < 0.01
            && Math.abs(geo[var13 + 3] - geo[var13] - var4) < 0.01
            && Math.abs(geo[var13 + 5] - geo[var13 + 2] - var6) < 0.01
            && Math.abs(geo[var13 + 1] - geo[var3 + 1]) < 0.01
            && Math.abs(geo[var13 + 4] - geo[var3 + 4]) < 0.01
            && strokes[var12] == strokes[i]
            && fills[var12] == fills[i];
      } else {
         return false;
      }
   }

   private static long a(int i, double d, double e, double f, double g) {
      long var9 = i * -7046029254386353131L;
      var9 = (var9 ^ Math.round(d * 8.0)) * -4658895280553007687L;
      var9 = (var9 ^ Math.round(e * 8.0)) * -7723592293110705685L;
      var9 = (var9 ^ Math.round(f * 8.0)) * -4658895280553007687L;
      var9 = (var9 ^ Math.round(g * 8.0)) * -7723592293110705685L;
      return var9 ^ var9 >>> 31;
   }

   private static int a(int i, float f) {
      int var2 = Math.round((i >> 16 & 0xFF) * f);
      int var3 = Math.round((i >> 8 & 0xFF) * f);
      int var4 = Math.round((i & 0xFF) * f);
      return i & 0xFF000000 | var2 << 16 | var3 << 8 | var4;
   }

   private static void a(int i) {
      if (px.length < i) {
         px = new double[i];
         pz = new double[i];
         open = new boolean[i];
      }
   }

   private static void a() {
      int var0 = owners.length * 2;
      owners = Arrays.copyOf(owners, var0);
      geo = Arrays.copyOf(geo, var0 * 6);
      strokes = Arrays.copyOf(strokes, var0);
      fills = Arrays.copyOf(fills, var0);
      widths = Arrays.copyOf(widths, var0);
   }

   static {
      INDEX.defaultReturnValue(-1);
   }
}
