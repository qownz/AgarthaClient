package net.agartha.client.e;

import net.agartha.client.module.impl.client.AgarthaModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class at {
   private static final float BLEND_SECONDS = 0.22F;
   private static k blendTarget = null;
   private static int blendFrom = -1;
   private static long blendStartNanos;
   private static int lastBlended = -1;

   private at() {
   }

   public static k current() {
      AgarthaModule var0 = net.agartha.client.module.e.get(AgarthaModule.class);
      return var0 == null ? k.WHITE : k.byName(var0.theme().get());
   }

   public static int accent() {
      k var0 = current();
      long var1 = System.nanoTime();
      if (blendTarget == null) {
         blendTarget = var0;
         blendFrom = var0.abgr();
         blendStartNanos = var1 - 219999998L;
      } else if (var0 != blendTarget) {
         blendFrom = lastBlended;
         blendTarget = var0;
         blendStartNanos = var1;
      }

      float var3 = (float)(var1 - blendStartNanos) / 2.2E8F;
      if (var3 >= 1.0F) {
         lastBlended = var0.abgr();
      } else {
         float var4 = 1.0F - (1.0F - var3) * (1.0F - var3);
         lastBlended = d.lerpColor(blendFrom, var0.abgr(), Math.max(0.0F, var4));
      }

      return lastBlended;
   }

   public static int accentWithAlpha(int i) {
      return accent() & 16777215 | (i & 0xFF) << 24;
   }

   public static int hoverBg() {
      return accentWithAlpha(20);
   }

   public static int borderAccent() {
      return accentWithAlpha(51);
   }

   public static float[] accentF() {
      int var0 = accent();
      return new float[]{(var0 & 0xFF) / 255.0F, (var0 >> 8 & 0xFF) / 255.0F, (var0 >> 16 & 0xFF) / 255.0F, 1.0F};
   }

   public static float[] borderF() {
      float[] var0 = accentF();
      var0[3] = 0.28F;
      return var0;
   }
}
