package net.agartha.client.gui;

import imgui.ImDrawList;
import net.agartha.client.gui.DrawUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class Backdrop {
   private static final float WASH_ALPHA = 0.45F;

   private Backdrop() {
   }

   public static void backdrop(ImDrawList imDrawList, float f, float g, float h, float i, float j, float k, float l) {
      imDrawList.addRectFilled(f, g, f + h, g + i, DrawUtils.alpha(-234091766, l * 0.45F), j);
      imDrawList.addRect(f, g, f + h, g + i, DrawUtils.alpha(620756991, l), j, 0, 1.0F);
      if (k > 0.0F) {
         imDrawList.addRectFilled(f, g + j, f + k, g + i - j, DrawUtils.accent(l * 0.85F), 0.0F);
      }
   }
}
