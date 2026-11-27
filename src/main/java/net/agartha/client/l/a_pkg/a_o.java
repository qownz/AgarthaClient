package net.agartha.client.l.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class a_o {
   public static final int BASE_SAMPLES = 100;

   private a_o() {
   }

   public static net.agartha.client.l.a_pkg.a_o.a shutter(float f, float g, int i, boolean bl) {
      if (bl && i > 0 && !(g <= i)) {
         float var4 = g / i;
         return new net.agartha.client.l.a_pkg.a_o.a(f * var4, Math.round(100.0F * var4));
      } else {
         return new net.agartha.client.l.a_pkg.a_o.a(f, 100);
      }
   }

   public static float trailBlend(float f, boolean bl) {
      double var2 = f * (bl ? 7.0 : 5.0);
      return (float)(1.0 - Math.pow(0.5, var2 / 3.0));
   }

   public static float smoothFps(float f, float g) {
      if (g <= 0.0F) {
         return f;
      } else {
         return f <= 0.0F ? g : f * 0.85F + g * 0.15F;
      }
   }

   @Environment(EnvType.CLIENT)
   public record a(float strength, int samples) {
   }
}
