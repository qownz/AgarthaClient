package net.agartha.client.g;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public final class a {
   private a() {
   }

   public static float degreesPerCount(float f) {
      float var1 = f * 0.6F + 0.2F;
      return var1 * var1 * var1 * 8.0F * 0.15F;
   }

   public static float degreesPerCount(MinecraftClient minecraftClient) {
      return degreesPerCount(((Double)minecraftClient.options.getMouseSensitivity().getValue()).floatValue());
   }

   public static float quantize(float f, float g) {
      float var2 = degreesPerCount(f);
      return var2 <= 0.0F ? 0.0F : Math.round(g / var2) * var2;
   }

   public static int yawCountsForDegrees(float f, float g) {
      float var2 = degreesPerCount(f);
      return var2 <= 0.0F ? 0 : Math.round(g / var2);
   }

   public static int pitchCountsForDegrees(float f, float g) {
      float var2 = degreesPerCount(f);
      return var2 <= 0.0F ? 0 : Math.round(g / var2);
   }

   public static int clampCounts(int i, int j) {
      return MathHelper.clamp(i, -j, j);
   }
}
