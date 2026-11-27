package net.agartha.client.gui;

import imgui.ImGui;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class UiScale {
   private static final float REF_WIDTH = 1920.0F;
   private static final float REF_HEIGHT = 1080.0F;
   private static final float MAX_UPSCALE = 1.75F;

   private UiScale() {
   }

   public static int physicalWidth(MinecraftClient minecraftClient) {
      return minecraftClient != null && minecraftClient.getWindow() != null
         ? Math.max(1, minecraftClient.getWindow().getFramebufferWidth())
         : Math.max(1, Math.round(ImGui.getIO().getDisplaySizeX()));
   }

   public static int physicalHeight(MinecraftClient minecraftClient) {
      return minecraftClient != null && minecraftClient.getWindow() != null
         ? Math.max(1, minecraftClient.getWindow().getFramebufferHeight())
         : Math.max(1, Math.round(ImGui.getIO().getDisplaySizeY()));
   }

   public static float uiScale() {
      int var0 = physicalWidth(null);
      int var1 = physicalHeight(null);
      return uiScale(var0, var1);
   }

   public static float uiScale(int i, int j) {
      if (i > 0 && j > 0) {
         float var2 = i / 1920.0F;
         float var3 = j / 1080.0F;
         float var4 = Math.min(var2, var3);
         return var4 <= 1.0F ? 1.0F : Math.min(var4, 1.75F);
      } else {
         return 1.0F;
      }
   }

   public static float width(MinecraftClient minecraftClient) {
      int var1 = physicalWidth(minecraftClient);
      return var1 / uiScale(var1, physicalHeight(minecraftClient));
   }

   public static float height(MinecraftClient minecraftClient) {
      int var1 = physicalHeight(minecraftClient);
      return var1 / uiScale(physicalWidth(minecraftClient), var1);
   }
}
