package net.agartha.client.e;

import imgui.ImDrawList;
import imgui.ImFont;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
final class aa {
   private static final String[] COUNTS = new String[100];

   private aa() {
   }

   static void item(ImDrawList imDrawList, ItemStack itemStack, float f, float g, float h, float i, float j, boolean bl) {
      if (itemStack != null && !itemStack.isEmpty()) {
         float var8 = Math.round(h * 0.82F);
         float var9 = f + (h - var8) * 0.5F;
         float var10 = g + (h - var8) * 0.5F;
         ab.draw(imDrawList, itemStack, var9, var10, var8, j);
         if (bl) {
            if (itemStack.isItemBarVisible()) {
               float var11 = var8 * 13.0F / 16.0F;
               float var12 = var9 + var8 * 2.0F / 16.0F;
               float var13 = var10 + var8 * 13.0F / 16.0F;
               float var14 = var8 / 16.0F;
               imDrawList.addRectFilled(var12, var13, var12 + var11, var13 + var14 * 2.0F, c.f(c.alpha(-16777216, j)));
               int var15 = itemStack.getItemBarStep() <= 3 ? -11908353 : at.accent();
               imDrawList.addRectFilled(var12, var13, var12 + var11 * itemStack.getItemBarStep() / 13.0F, var13 + var14, c.f(c.alpha(var15, j)));
            }

            int var16 = itemStack.getCount();
            if (var16 > 1) {
               ImFont var17 = net.agartha.client.f.a.getInstance().hudValueFont();
               float var18 = 11.0F * i;
               String var19 = var16 < COUNTS.length ? COUNTS[var16] : String.valueOf(var16);
               float var20 = c.textWidth(var17, var18, var19);
               c.textShadowed(imDrawList, var17, var18, var9 + var8 - var20 + i, var10 + var8 - var18 + 1.5F * i, c.alpha(-526345, j), var19);
            }
         }
      }
   }

   static {
      for (int var0 = 0; var0 < COUNTS.length; var0++) {
         COUNTS[var0] = String.valueOf(var0);
      }
   }
}
