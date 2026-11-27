package net.agartha.client.mixin;

import net.agartha.client.d.a_pkg.f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({Keyboard.class})
public abstract class MixinKeyboardHandler {
   @Inject(
      method = {"onKey"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$onKeyPress(long l, int i, KeyInput keyInput, CallbackInfo callbackInfo) {
      if (f.dispatch(keyInput.key(), keyInput.scancode(), i, keyInput.modifiers())) {
         callbackInfo.cancel();
      }
   }
}
