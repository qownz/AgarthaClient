package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.render.m;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.DarknessEffectFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({DarknessEffectFogModifier.class})
public abstract class MixinNoRenderDarknessFog {
   @Inject(
      method = {"applyStartEndModifier"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noDarkness(
      FogData fogData, Camera camera, ClientWorld clientWorld, float f, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo
   ) {
      m var7 = e.get(m.class);
      if (var7 != null && var7.noDarkness()) {
         callbackInfo.cancel();
      }
   }
}
