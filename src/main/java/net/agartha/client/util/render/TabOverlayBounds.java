package net.agartha.client.util.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class TabOverlayBounds {
   private static final long FRESH_NANOS = 150000000L;
   private static boolean collecting;
   private static int minX;
   private static int minY;
   private static int maxX;
   private static int maxY;
   private static boolean any;
   private static final int[] shown = new int[4];
   private static long shownAt;

   private TabOverlayBounds() {
   }

   public static void begin() {
      collecting = true;
      any = false;
   }

   public static void panel(int i, int j, int k, int l) {
      if (collecting) {
         int var4 = Math.min(i, k);
         int var5 = Math.max(i, k);
         int var6 = Math.min(j, l);
         int var7 = Math.max(j, l);
         if (!any) {
            minX = var4;
            minY = var6;
            maxX = var5;
            maxY = var7;
            any = true;
         } else {
            minX = Math.min(minX, var4);
            minY = Math.min(minY, var6);
            maxX = Math.max(maxX, var5);
            maxY = Math.max(maxY, var7);
         }
      }
   }

   public static void end() {
      collecting = false;
      if (any) {
         shown[0] = minX;
         shown[1] = minY;
         shown[2] = maxX;
         shown[3] = maxY;
         shownAt = System.nanoTime();
      }
   }

   public static int[] open() {
      return shownAt != 0L && System.nanoTime() - shownAt <= 150000000L ? shown : null;
   }
}
