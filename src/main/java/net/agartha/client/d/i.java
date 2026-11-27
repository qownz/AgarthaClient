package net.agartha.client.d;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum i {
   LOWEST(-100),
   LOW(-50),
   NORMAL(0),
   HIGH(50),
   HIGHEST(100);

   public final int value;

   private i(int j) {
      this.value = j;
   }

   public static i fromInt(int i) {
      if (i >= 75) {
         return HIGHEST;
      } else if (i >= 25) {
         return HIGH;
      } else if (i >= -25) {
         return NORMAL;
      } else {
         return i >= -75 ? LOW : LOWEST;
      }
   }
}
