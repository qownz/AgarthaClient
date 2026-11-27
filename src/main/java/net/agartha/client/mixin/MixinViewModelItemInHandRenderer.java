package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.ViewModelModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({HeldItemRenderer.class})
public abstract class MixinViewModelItemInHandRenderer {
   private static final String RENDER_ARM = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V";

   @Inject(
      method = {"renderFirstPersonItem"},
      at = {@At("HEAD")}
   )
   private void agartha$viewModelPush(
      AbstractClientPlayerEntity abstractClientPlayerEntity,
      float f,
      float g,
      Hand hand,
      float h,
      ItemStack itemStack,
      float i,
      MatrixStack matrixStack,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      int j,
      CallbackInfo callbackInfo
   ) {
      matrixStack.push();
      ViewModelModule var12 = ModuleManager.get(ViewModelModule.class);
      if (var12 != null && var12.isEnabled()) {
         boolean var13 = hand == Hand.MAIN_HAND;
         if (var12.appliesTo(var13)) {
            var12.applyTransform(matrixStack, var13);
         }
      }
   }

   @Inject(
      method = {"renderFirstPersonItem"},
      at = {@At("RETURN")}
   )
   private void agartha$viewModelPop(
      AbstractClientPlayerEntity abstractClientPlayerEntity,
      float f,
      float g,
      Hand hand,
      float h,
      ItemStack itemStack,
      float i,
      MatrixStack matrixStack,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      int j,
      CallbackInfo callbackInfo
   ) {
      matrixStack.pop();
   }
}
