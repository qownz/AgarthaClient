package net.agartha.client.util.perf;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class TickBudget {
   public static final int STORAGE = 0;
   public static final int SPAWNER = 1;
   public static final int Y_BAND = 2;
   public static final int BLOCK = 3;
   public static final int HIVE = 4;
   private static final int SLOTS = 5;
   private static final long[] SLICE_NS = new long[]{2400000L, 1200000L, 1200000L, 1500000L, 1200000L};
   private static final long[] DEADLINES = new long[5];
   private static final long TICK_NS = 50000000L;
   private static long tickCount;
   private static long lastFrameNs;
   private static double frameShare = 1.0;

   private TickBudget() {
   }

   public static void beginTick() {
      tickCount++;
      long var0 = System.nanoTime();

      for (int var2 = 0; var2 < 5; var2++) {
         DEADLINES[var2] = var0 + SLICE_NS[var2];
      }
   }

   public static boolean hasTime(int i) {
      return System.nanoTime() < DEADLINES[i];
   }

   public static long sliceNs(int i) {
      return SLICE_NS[i];
   }

   public static long tickCount() {
      return tickCount;
   }

   public static void beginFrame() {
      long var0 = System.nanoTime();
      long var2 = lastFrameNs == 0L ? 50000000L : var0 - lastFrameNs;
      lastFrameNs = var0;
      frameShare = Math.max(0.0, Math.min(1.0, var2 / 5.0E7));
   }

   public static long frameShareOf(long l) {
      return (long)(l * frameShare);
   }
}
