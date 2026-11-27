package net.agartha.client.mixin;

import net.agartha.client.render.imgui.ImGuiManager;
import net.agartha.client.util.io.ScreenshotCapture;
import net.agartha.client.util.perf.Profiler;
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
      Profiler.framePresented();
      ImGuiManager.getInstance().renderIfActive();
      ScreenshotCapture.captureIfPending();
   }
}
