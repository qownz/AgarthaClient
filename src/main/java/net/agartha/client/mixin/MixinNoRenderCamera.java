package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.render.m;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({Camera.class})
public abstract class MixinNoRenderCamera {
   @Inject(
      method = {"getSubmersionType"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noLiquidFog(CallbackInfoReturnable<CameraSubmersionType> callbackInfoReturnable) {
      m var2 = e.get(m.class);
      if (var2 != null && var2.noLiquidOverlay()) {
         callbackInfoReturnable.setReturnValue(CameraSubmersionType.NONE);
      }
   }
}
