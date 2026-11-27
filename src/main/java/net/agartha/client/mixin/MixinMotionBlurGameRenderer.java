package net.agartha.client.mixin;

import net.agartha.client.render.post.MotionBlurRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({GameRenderer.class})
public abstract class MixinMotionBlurGameRenderer {
   @Inject(
      method = {"renderWorld"},
      at = {@At("TAIL")}
   )
   private void agartha$motionBlurAfterHand(CallbackInfo callbackInfo) {
      MotionBlurRenderer.afterHand();
   }
}
