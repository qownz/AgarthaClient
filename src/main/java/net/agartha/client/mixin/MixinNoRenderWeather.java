package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.client.render.state.WeatherRenderState;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({WeatherRendering.class})
public abstract class MixinNoRenderWeather {
   @Inject(
      method = {"buildPrecipitationPieces"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noWeather(World world, int i, float f, Vec3d vec3d, WeatherRenderState weatherRenderState, CallbackInfo callbackInfo) {
      NoRenderModule var7 = ModuleManager.get(NoRenderModule.class);
      if (var7 != null && var7.noWeather()) {
         weatherRenderState.intensity = 0.0F;
         weatherRenderState.rainPieces.clear();
         weatherRenderState.snowPieces.clear();
         callbackInfo.cancel();
      }
   }
}
