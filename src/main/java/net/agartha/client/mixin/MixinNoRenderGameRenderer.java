package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.render.m;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({GameRenderer.class})
public abstract class MixinNoRenderGameRenderer {
   @Inject(
      method = {"showFloatingItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noTotem(ItemStack itemStack, CallbackInfo callbackInfo) {
      m var3 = e.get(m.class);
      if (var3 != null && var3.noTotemAnimation() && itemStack.isOf(Items.TOTEM_OF_UNDYING)) {
         callbackInfo.cancel();
      }
   }
}
