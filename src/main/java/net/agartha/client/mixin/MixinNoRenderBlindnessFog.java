package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.BlindnessEffectFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({BlindnessEffectFogModifier.class})
public abstract class MixinNoRenderBlindnessFog {
   @Inject(
      method = {"applyStartEndModifier"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noBlindness(
      FogData fogData, Camera camera, ClientWorld clientWorld, float f, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo
   ) {
      NoRenderModule var7 = ModuleManager.get(NoRenderModule.class);
      if (var7 != null && var7.noBlindness()) {
         callbackInfo.cancel();
      }
   }
}
