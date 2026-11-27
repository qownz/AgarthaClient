package net.agartha.client.mixin;

import net.agartha.client.render.post.PostEffectLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.ShaderLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ShaderLoader.class})
public abstract class MixinShaderManager {
   @Inject(
      method = {"handleError"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$keepPacksForOwnChains(Exception exception, CallbackInfo callbackInfo) {
      if (PostEffectLoader.isLoading()) {
         callbackInfo.cancel();
      }
   }
}
