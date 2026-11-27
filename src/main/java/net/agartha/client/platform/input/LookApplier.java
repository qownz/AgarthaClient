package net.agartha.client.platform.input;

import net.agartha.client.platform.input.LookMath;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

@Environment(EnvType.CLIENT)
public final class LookApplier {
   private LookApplier() {
   }

   public static boolean applyDegrees(MinecraftClient minecraftClient, float f, float g, int i) {
      ClientPlayerEntity var4 = minecraftClient.player;
      if (var4 == null) {
         return false;
      } else {
         float var5 = ((Double)minecraftClient.options.getMouseSensitivity().getValue()).floatValue();
         float var6 = LookMath.quantize(var5, f);
         float var7 = LookMath.quantize(var5, g);
         int var8 = LookMath.clampCounts(LookMath.yawCountsForDegrees(var5, var6), i);
         int var9 = LookMath.clampCounts(LookMath.pitchCountsForDegrees(var5, var7), i);
         if (var8 == 0 && var9 == 0) {
            return false;
         } else {
            float var10 = var5 * 0.6F + 0.2F;
            float var11 = var10 * var10 * var10 * 8.0F;
            var4.changeLookDirection(var8 * var11, var9 * var11);
            return true;
         }
      }
   }

   public static int appliedYawCounts(MinecraftClient minecraftClient, float f, int i) {
      float var3 = ((Double)minecraftClient.options.getMouseSensitivity().getValue()).floatValue();
      float var4 = LookMath.quantize(var3, f);
      return LookMath.clampCounts(LookMath.yawCountsForDegrees(var3, var4), i);
   }

   public static int appliedPitchCounts(MinecraftClient minecraftClient, float f, int i) {
      float var3 = ((Double)minecraftClient.options.getMouseSensitivity().getValue()).floatValue();
      float var4 = LookMath.quantize(var3, f);
      return LookMath.clampCounts(LookMath.pitchCountsForDegrees(var3, var4), i);
   }
}
