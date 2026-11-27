package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.WorldBorderRendering;
import net.minecraft.client.render.state.WorldBorderRenderState;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({WorldBorderRendering.class})
public abstract class MixinNoRenderWorldBorder {
   @Inject(
      method = {"updateRenderState"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noWorldBorder(
      WorldBorder worldBorder, float f, Vec3d vec3d, double d, WorldBorderRenderState worldBorderRenderState, CallbackInfo callbackInfo
   ) {
      NoRenderModule var8 = ModuleManager.get(NoRenderModule.class);
      if (var8 != null && var8.noWorldBorder()) {
         worldBorderRenderState.alpha = 0.0;
         callbackInfo.cancel();
      }
   }
}
