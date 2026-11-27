package net.agartha.client.mixin;

import net.agartha.client.d.a_pkg.l;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({PlayerEntity.class})
public abstract class MixinPlayer {
   @Inject(
      method = {"attack"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$onAttack(Entity entity, CallbackInfo callbackInfo) {
      if (l.dispatch(entity)) {
         callbackInfo.cancel();
      }
   }
}
