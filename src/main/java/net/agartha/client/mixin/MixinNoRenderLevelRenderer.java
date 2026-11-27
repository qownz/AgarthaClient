package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.render.m;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.WorldRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({WorldRenderer.class})
public abstract class MixinNoRenderLevelRenderer {
   @Inject(
      method = {"fillBlockBreakingProgressRenderState"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noBlockBreak(Camera camera, WorldRenderState worldRenderState, CallbackInfo callbackInfo) {
      m var4 = e.get(m.class);
      if (var4 != null && var4.noBlockBreakOverlay()) {
         callbackInfo.cancel();
      }
   }
}
