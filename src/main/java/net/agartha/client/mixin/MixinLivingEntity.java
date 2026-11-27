package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.misc.t;
import net.agartha.client.module.impl.render.j;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({LivingEntity.class})
public abstract class MixinLivingEntity {
   @Inject(
      method = {"getHandSwingDuration"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void agartha$modifySwingDuration(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
      t var2 = e.get(t.class);
      if (var2 != null && var2.isEnabled()) {
         if ((Object)this == MinecraftClient.getInstance().player) {
            callbackInfoReturnable.setReturnValue(var2.getModifiedSwingDuration((Integer)callbackInfoReturnable.getReturnValue()));
         }
      }
   }

   @Inject(
      method = {"jump"},
      at = {@At("HEAD")}
   )
   private void agartha$onJumpFromGround(CallbackInfo callbackInfo) {
      if ((Object)this == MinecraftClient.getInstance().player) {
         j var2 = e.get(j.class);
         if (var2 != null && var2.isEnabled()) {
            var2.onJump((LivingEntity)(Object)this);
         }
      }
   }
}
