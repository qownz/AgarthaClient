package net.agartha.client.util;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class o {
   public static final int SUS = 1;
   public static final int BYPASS = 2;
   public static final int MOBS = 3;
   public static final int DIGGER = 4;
   public static final int BYPASS2 = 5;
   private static final double GAP = 0.06;
   private static final double OWNER_GAP = 1.0;
   private static long[] ids = new long[48];
   private static int[] owners = new int[48];
   private static double[] box = new double[288];
   private static int count;
   private static final double GLIDE_RATE = 9.0;
   private static final double GLIDE_SNAP = 24.0;
   private static final Long2DoubleOpenHashMap GLIDE = new Long2DoubleOpenHashMap();
   private static final Long2IntOpenHashMap GLIDE_FRAME = new Long2IntOpenHashMap();
   private static int frame;
   private static long lastNanos;
   private static double follow = 1.0;

   private o() {
   }

   public static void beginFrame() {
      count = 0;
      long var0 = System.nanoTime();
      double var2 = lastNanos == 0L ? 0.016 : Math.min((var0 - lastNanos) / 1.0E9, 0.1);
      lastNanos = var0;
      follow = 1.0 - Math.exp(-9.0 * var2);
      frame++;
      if ((frame & 127) == 0) {
         GLIDE_FRAME.long2IntEntrySet().removeIf(entry -> {
            if (frame - entry.getIntValue() < 60) {
               return false;
            } else {
               GLIDE.remove(entry.getLongKey());
               return true;
            }
         });
      }
   }

   public static double y(int i, long l, double d, double e, double f, double g, double h, double j) {
      double var15 = y(i, d, e, f, g, h, j);
      long var17 = l * 31L + i * -7046029254386353131L;
      double var19 = GLIDE.get(var17);
      if (GLIDE_FRAME.get(var17) == frame) {
         return var19;
      } else {
         if (!Double.isNaN(var19) && !(Math.abs(var15 - var19) > 24.0)) {
            var19 += (var15 - var19) * follow;
         } else {
            var19 = var15;
         }

         GLIDE.put(var17, var19);
         GLIDE_FRAME.put(var17, frame);
         return var19;
      }
   }

   public static double y(int i, double d, double e, double f, double g, double h, double j) {
      long var13 = a(i, d, f, g, h);

      for (int var15 = 0; var15 < count; var15++) {
         if (ids[var15] == var13) {
            return box[var15 * 6 + 4];
         }
      }

      double var21 = e;

      for (int var17 = 0; var17 < 8; var17++) {
         boolean var18 = false;

         for (int var19 = 0; var19 < count; var19++) {
            int var20 = var19 * 6;
            if (!(g <= box[var20])
               && !(d >= box[var20 + 2])
               && !(h <= box[var20 + 1])
               && !(f >= box[var20 + 3])
               && var21 < box[var20 + 5]
               && var21 + j > box[var20 + 4]) {
               var21 = box[var20 + 5] + (owners[var19] == i ? 0.06 : 1.0);
               var18 = true;
            }
         }

         if (!var18) {
            break;
         }
      }

      if (count == ids.length) {
         int var22 = count * 2;
         long[] var24 = new long[var22];
         owners = Arrays.copyOf(owners, var22);
         double[] var25 = new double[var22 * 6];
         System.arraycopy(ids, 0, var24, 0, count);
         System.arraycopy(box, 0, var25, 0, count * 6);
         ids = var24;
         box = var25;
      }

      ids[count] = var13;
      owners[count] = i;
      int var23 = count * 6;
      box[var23] = d;
      box[var23 + 1] = f;
      box[var23 + 2] = g;
      box[var23 + 3] = h;
      box[var23 + 4] = var21;
      box[var23 + 5] = var21 + j;
      count++;
      return var21;
   }

   private static long a(int i, double d, double e, double f, double g) {
      long var9 = Math.round(d * 2.0);
      long var11 = Math.round(e * 2.0);
      long var13 = Math.round(f * 2.0);
      long var15 = Math.round(g * 2.0);
      return i * 42470972311L ^ var9 << 48 ^ var11 << 32 ^ var13 << 16 ^ var15;
   }

   static {
      GLIDE.defaultReturnValue(Double.NaN);
      GLIDE_FRAME.defaultReturnValue(Integer.MIN_VALUE);
   }
}
