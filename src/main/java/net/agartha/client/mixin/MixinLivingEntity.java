package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.misc.SwingSpeedModule;
import net.agartha.client.module.impl.render.JumpRingsModule;
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
      SwingSpeedModule var2 = ModuleManager.get(SwingSpeedModule.class);
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
         JumpRingsModule var2 = ModuleManager.get(JumpRingsModule.class);
         if (var2 != null && var2.isEnabled()) {
            var2.onJump((LivingEntity)(Object)this);
         }
      }
   }
}
