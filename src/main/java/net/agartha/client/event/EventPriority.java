package net.agartha.client.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum EventPriority {
   LOWEST(-100),
   LOW(-50),
   NORMAL(0),
   HIGH(50),
   HIGHEST(100);

   public final int value;

   private EventPriority(int j) {
      this.value = j;
   }

   public static EventPriority fromInt(int i) {
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
