package net.agartha.client.mixin;

import net.agartha.client.f.a;
import net.agartha.client.k.b_o;
import net.agartha.client.util.j;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({Window.class})
public abstract class MixinGameRenderer {
   @Inject(
      method = {"swapBuffers"},
      at = {@At("HEAD")}
   )
   private void agartha$beforeSwap(CallbackInfo callbackInfo) {
      b_o.framePresented();
      a.getInstance().renderIfActive();
      j.captureIfPending();
   }
}
