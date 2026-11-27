package net.agartha.client.gui;

import net.agartha.client.config.ModuleConfigManager;
import net.agartha.client.gui.ClickGui;
import net.agartha.client.gui.ClickGuiLayout;
import net.agartha.client.gui.CrosshairHud;
import net.agartha.client.render.imgui.ImGuiManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ClickGuiScreen extends Screen {
   private final ClickGui clickGui = new ClickGui();
   @Nullable
   private final Screen returnTo;

   public ClickGuiScreen(@Nullable Screen screen) {
      super(Text.literal("AGARTHA"));
      this.returnTo = screen;
   }

   @Nullable
   public Screen returnTo() {
      return this.returnTo;
   }

   public void requestClose() {
      this.clickGui.requestClose(() -> {
         if (this.client != null && this.client.currentScreen == this) {
            this.client.setScreen(this.returnTo);
         }
      });
   }

   protected void init() {
      super.init();
      this.clickGui.resetGuiIntro();
      ImGuiManager.getInstance().activate(this.clickGui::render);
   }

   public void removed() {
      super.removed();
      this.clickGui.dismissTransientState();
      ImGuiManager.getInstance().deactivate();
      ModuleConfigManager.getInstance().save();
      ClickGuiLayout.getInstance().saveIfDirty();
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      if (this.returnTo != null) {
         this.returnTo.renderBackground(context, mouseX, mouseY, deltaTicks);
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      if (this.returnTo != null) {
         this.returnTo.render(context, mouseX, mouseY, deltaTicks);
      }

      CrosshairHud.renderFromScreen(context);
      context.drawDeferredElements();
   }

   public boolean keyPressed(KeyInput input) {
      if (input.key() == 256) {
         if (!this.clickGui.handleEscapeKey()) {
            this.requestClose();
         }

         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   public boolean shouldPause() {
      return false;
   }
}
