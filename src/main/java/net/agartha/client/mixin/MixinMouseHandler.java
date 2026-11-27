package net.agartha.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.agartha.client.d.a_pkg.g;
import net.agartha.client.d.a_pkg.h;
import net.agartha.client.module.e;
import net.agartha.client.module.impl.misc.k;
import net.agartha.client.module.impl.render.d;
import net.agartha.client.util.b_pkg.a;
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
      if (g.dispatch(l, mouseInput.button(), i, mouseInput.modifiers())) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"onMouseScroll"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$onScroll(long l, double d, double e, CallbackInfo callbackInfo) {
      if (h.dispatch(e)) {
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
      a.record(d, e);
      net.agartha.client.module.impl.misc.k var7 = net.agartha.client.module.e.get(net.agartha.client.module.impl.misc.k.class);
      if (var7 != null && var7.isEnabled()) {
         var7.changeLookDirection(d, e);
      } else {
         net.agartha.client.module.impl.render.d var8 = net.agartha.client.module.e.get(net.agartha.client.module.impl.render.d.class);
         if (var8 != null && var8.isEnabled()) {
            var8.changeLookDirection(d, e);
         } else {
            operation.call(new Object[]{clientPlayerEntity, d, e});
         }
      }
   }
}
