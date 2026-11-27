package net.agartha.client.mixin;

import java.io.File;
import java.util.function.Consumer;
import net.agartha.client.util.io.ScreenshotCapture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ScreenshotRecorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ScreenshotRecorder.class})
public class MixinScreenshot {
   @Inject(
      method = {"saveScreenshot(Ljava/io/File;Lnet/minecraft/client/gl/Framebuffer;Ljava/util/function/Consumer;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void agartha$deferGrab(File file, Framebuffer framebuffer, Consumer<?> consumer, CallbackInfo callbackInfo) {
      ScreenshotCapture.request();
      callbackInfo.cancel();
   }
}
