package net.agartha.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.agartha.client.event.impl.MouseInputEvent;
import net.agartha.client.event.impl.MouseScrollEvent;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.misc.FreecamModule;
import net.agartha.client.module.impl.render.FreelookModule;
import net.agartha.client.util.player.LookDelta;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({Mouse.class})
public abstract class MixinMouseHandler {
   @Inject(
      method = {"onMouseButton"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$onMouseButton(long l, MouseInput mouseInput, int i, CallbackInfo callbackInfo) {
      if (MouseInputEvent.dispatch(l, mouseInput.button(), i, mouseInput.modifiers())) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"onMouseScroll"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$onScroll(long l, double d, double e, CallbackInfo callbackInfo) {
      if (MouseScrollEvent.dispatch(e)) {
         callbackInfo.cancel();
      }
   }

   @WrapOperation(
      method = {"updateMouse"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
      )}
   )
   private void agartha$freecamTurn(ClientPlayerEntity clientPlayerEntity, double d, double e, Operation<Void> operation) {
      LookDelta.record(d, e);
      FreecamModule var7 = ModuleManager.get(FreecamModule.class);
      if (var7 != null && var7.isEnabled()) {
         var7.changeLookDirection(d, e);
      } else {
         FreelookModule var8 = ModuleManager.get(FreelookModule.class);
         if (var8 != null && var8.isEnabled()) {
            var8.changeLookDirection(d, e);
         } else {
            operation.call(new Object[]{clientPlayerEntity, d, e});
         }
      }
   }
}
