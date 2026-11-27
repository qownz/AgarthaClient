package net.agartha.client.render.post;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum MotionBlurAlgorithm {
   VELOCITY("Velocity"),
   FRAME_BLENDING("Frame Blending"),
   HYBRID("Hybrid"),
   ACCUMULATION_MAX("Accumulation Max"),
   ACCUMULATION_MIX("Accumulation Mix");

   private final String label;

   private MotionBlurAlgorithm(String string2) {
      this.label = string2;
   }

   public String label() {
      return this.label;
   }

   public boolean usesVelocity() {
      return this == VELOCITY || this == HYBRID;
   }

   public boolean usesFrameHistory() {
      return this == FRAME_BLENDING || this == HYBRID;
   }

   public boolean usesTrail() {
      return this == ACCUMULATION_MAX || this == ACCUMULATION_MIX;
   }

   public boolean allowsRefreshScaling() {
      return this == VELOCITY;
   }

   public boolean hasStrength() {
      return this == VELOCITY || this.usesTrail();
   }

   public static MotionBlurAlgorithm byLabel(String string) {
      for (MotionBlurAlgorithm var4 : values()) {
         if (var4.label.equals(string)) {
            return var4;
         }
      }

      return VELOCITY;
   }

   public static String[] labels() {
      MotionBlurAlgorithm[] var0 = values();
      String[] var1 = new String[var0.length];

      for (int var2 = 0; var2 < var0.length; var2++) {
         var1[var2] = var0[var2].label;
      }

      return var1;
   }
}
