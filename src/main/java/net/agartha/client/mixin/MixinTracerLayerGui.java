package net.agartha.client.mixin;

import net.agartha.client.util.render.TracerLineBatch;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({InGameHud.class})
public abstract class MixinTracerLayerGui {
   @Inject(
      method = {"render"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/hud/InGameHud;renderCrosshair(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"
      )}
   )
   private void agartha$tracers(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
      TracerLineBatch.render(drawContext);
   }
}
