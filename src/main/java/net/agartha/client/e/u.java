package net.agartha.client.e;

import imgui.ImDrawList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class u {
   private static final float WASH_ALPHA = 0.45F;

   private u() {
   }

   public static void backdrop(ImDrawList imDrawList, float f, float g, float h, float i, float j, float k, float l) {
      imDrawList.addRectFilled(f, g, f + h, g + i, c.alpha(-234091766, l * 0.45F), j);
      imDrawList.addRect(f, g, f + h, g + i, c.alpha(620756991, l), j, 0, 1.0F);
      if (k > 0.0F) {
         imDrawList.addRectFilled(f, g + j, f + k, g + i - j, c.accent(l * 0.85F), 0.0F);
      }
   }
}
