package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.AbstractSignBlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.SignBlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({AbstractSignBlockEntityRenderer.class})
public abstract class MixinNoRenderSign {
   @Inject(
      method = {"renderText"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noSignText(
      SignBlockEntityRenderState signBlockEntityRenderState,
      MatrixStack matrixStack,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      boolean bl,
      CallbackInfo callbackInfo
   ) {
      NoRenderModule var6 = ModuleManager.get(NoRenderModule.class);
      if (var6 != null && var6.noSignText()) {
         callbackInfo.cancel();
      }
   }
}
