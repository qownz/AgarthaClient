package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({Screen.class})
public abstract class MixinNoRenderScreen {
   @Inject(
      method = {"renderBackground"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noGuiBackground(DrawContext drawContext, int i, int j, float f, CallbackInfo callbackInfo) {
      NoRenderModule var6 = ModuleManager.get(NoRenderModule.class);
      if (var6 != null && var6.noGuiBackground()) {
         callbackInfo.cancel();
      }
   }
}
