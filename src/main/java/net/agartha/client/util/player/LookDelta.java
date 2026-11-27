package net.agartha.client.util.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public final class LookDelta {
   private static double yawDelta;
   private static double pitchDelta;

   private LookDelta() {
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
