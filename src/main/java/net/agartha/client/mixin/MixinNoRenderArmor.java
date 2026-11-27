package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.render.m;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ArmorFeatureRenderer.class})
public abstract class MixinNoRenderArmor {
   @Inject(
      method = {"renderArmor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noArmor(
      MatrixStack matrixStack,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      ItemStack itemStack,
      EquipmentSlot equipmentSlot,
      int i,
      BipedEntityRenderState bipedEntityRenderState,
      CallbackInfo callbackInfo
   ) {
      m var8 = e.get(m.class);
      if (var8 != null && var8.noArmor()) {
         callbackInfo.cancel();
      }
   }
}
