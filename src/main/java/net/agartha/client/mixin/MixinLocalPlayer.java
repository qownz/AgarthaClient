package net.agartha.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.agartha.client.d.a_pkg.m;
import net.agartha.client.module.e;
import net.agartha.client.module.impl.misc.q;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ClientPlayerEntity.class})
public abstract class MixinLocalPlayer {
   @Inject(
      method = {"move"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$onMove(MovementType movementType, Vec3d vec3d, CallbackInfo callbackInfo) {
      if (m.dispatch(vec3d)) {
         callbackInfo.cancel();
      }
   }

   @ModifyExpressionValue(
      method = {"tick"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/client/input/Input;playerInput:Lnet/minecraft/util/PlayerInput;",
         opcode = 180
      )}
   )
   private PlayerInput agartha$sneakPacketInput(PlayerInput playerInput) {
      q var2 = e.get(q.class);
      return var2 != null && var2.doPacket() ? q.forceSneak(playerInput) : playerInput;
   }
}
