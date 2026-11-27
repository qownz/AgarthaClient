package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.MobSpawnerBlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.MobSpawnerBlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({MobSpawnerBlockEntityRenderer.class})
public abstract class MixinNoRenderSpawner {
   @Inject(
      method = {"render(Lnet/minecraft/client/render/block/entity/state/MobSpawnerBlockEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noSpawner(
      MobSpawnerBlockEntityRenderState mobSpawnerBlockEntityRenderState,
      MatrixStack matrixStack,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      CameraRenderState cameraRenderState,
      CallbackInfo callbackInfo
   ) {
      NoRenderModule var6 = ModuleManager.get(NoRenderModule.class);
      if (var6 != null && var6.noMobInSpawner()) {
         callbackInfo.cancel();
      }
   }
}
