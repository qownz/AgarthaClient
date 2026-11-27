package net.agartha.client.e;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class x extends Screen {
   private final j clickGui = new j();
   @Nullable
   private final Screen returnTo;

   public x(@Nullable Screen screen) {
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
      net.agartha.client.f.a.getInstance().activate(this.clickGui::render);
   }

   public void removed() {
      super.removed();
      this.clickGui.dismissTransientState();
      net.agartha.client.f.a.getInstance().deactivate();
      net.agartha.client.c.e.getInstance().save();
      h.getInstance().saveIfDirty();
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

      m.renderFromScreen(context);
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
