package net.agartha.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.agartha.client.module.impl.render.HandOverlayModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin({HeldItemRenderer.class})
public abstract class MixinHandOverlay {
   @WrapOperation(
      method = {"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/command/RenderDispatcher;render()V"
      )}
   )
   private void agartha$handOverlay(RenderDispatcher renderDispatcher, Operation<Void> operation) {
      if (HandOverlayModule.active != null && HandOverlayModule.beginCapture()) {
         boolean var3 = false;

         try {
            operation.call(new Object[]{renderDispatcher});
            MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().draw();
            var3 = true;
         } finally {
            HandOverlayModule.endCapture(var3);
         }
      } else {
         operation.call(new Object[]{renderDispatcher});
      }
   }
}
