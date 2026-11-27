package net.agartha.client.gui;

import net.agartha.client.config.ModuleConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public final class HudLayoutScreen extends Screen {
   private final Screen returnTo;

   public HudLayoutScreen(Screen screen) {
      super(Text.literal("AGARTHA HUD Layout"));
      this.returnTo = screen;
   }

   public void removed() {
      super.removed();
      HudLayout.getInstance().save();
      ModuleConfigManager.getInstance().save();
   }

   public void close() {
      HudLayout.getInstance().save();
      MinecraftClient.getInstance().setScreen(this.returnTo);
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      context.drawDeferredElements();
   }

   public boolean shouldPause() {
      return false;
   }
}
