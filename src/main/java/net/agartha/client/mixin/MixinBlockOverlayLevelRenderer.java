package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.misc.BlockOverlayModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({WorldRenderer.class})
public abstract class MixinBlockOverlayLevelRenderer {
   @Inject(
      method = {"drawBlockOutline"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$blockOverlay(CallbackInfo callbackInfo) {
      BlockOverlayModule var2 = ModuleManager.get(BlockOverlayModule.class);
      if (var2 != null && var2.replacesVanillaOutline()) {
         callbackInfo.cancel();
      }
   }
}
