package net.agartha.client.util.b_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public final class a {
   private static double yawDelta;
   private static double pitchDelta;

   private a() {
   }

   public static void record(double d, double e) {
      yawDelta = MathHelper.clamp(yawDelta + d, -10.0, 10.0);
      pitchDelta = MathHelper.clamp(pitchDelta + e, -10.0, 10.0);
   }

   public static double yawDelta() {
      return yawDelta;
   }

   public static double pitchDelta() {
      return pitchDelta;
   }

   public static void reset() {
      yawDelta = 0.0;
      pitchDelta = 0.0;
   }
}
