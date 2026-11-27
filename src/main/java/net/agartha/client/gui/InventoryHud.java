package net.agartha.client.gui;

import imgui.ImDrawList;
import net.agartha.client.gui.DrawUtils;
import net.agartha.client.gui.Hud;
import net.agartha.client.gui.HudItemSlot;
import net.agartha.client.gui.HudLayout;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class InventoryHud {
   private static final int COLUMNS = 9;
   private static final int ROWS = 3;
   private static final int FIRST_SLOT = 9;
   private static final float CELL = 25.0F;
   private static final float GAP = 2.0F;
   private static final float PAD = 6.0F;
   private static final float RADIUS = 5.0F;
   private static final float CUT = 6.0F;

   private InventoryHud() {
   }

   public static InventoryHud.a measure(float f) {
      float var1 = 225.0F * f + 16.0F * f + 6.0F * f * 2.0F;
      float var2 = 75.0F * f + 4.0F * f + 6.0F * f * 2.0F;
      return new InventoryHud.a(var1, var2);
   }

   public static void render(ImDrawList imDrawList, HudLayout.c arg, float f, float g, float h) {
      float var5 = DrawUtils.clamp01(h);
      if (!(var5 <= 0.002F)) {
         ClientPlayerEntity var6 = MinecraftClient.getInstance().player;
         float var7 = Hud.scaleOf(arg);
         float var8 = arg.x + f;
         float var9 = arg.y + g;
         DrawUtils.card(imDrawList, var8, var9, arg.width, arg.height, 5.0F * var7, 6.0F * var7, var5, true);
         float var10 = 25.0F * var7;
         float var11 = var10 + 2.0F * var7;
         float var12 = DrawUtils.smooth() ? 5.0F * var7 : 2.0F * var7;
         int var13 = DrawUtils.f(DrawUtils.alpha(318767103, var5));

         for (int var14 = 0; var14 < 3; var14++) {
            for (int var15 = 0; var15 < 9; var15++) {
               float var16 = var8 + 6.0F * var7 + var15 * var11;
               float var17 = var9 + 6.0F * var7 + var14 * var11;
               imDrawList.addRectFilled(var16, var17, var16 + var10, var17 + var10, var13, var12);
               if (var6 != null) {
                  int var18 = 9 + var14 * 9 + var15;
                  ItemStack var19 = var6.getInventory().getStack(var18);
                  HudItemSlot.item(imDrawList, var19, var16, var17, var10, var7, var5, true);
               }
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public record a(float width, float height) {
   }
}
