package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.misc.q;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({KeyboardInput.class})
public abstract class MixinKeyboardInput extends Input {
   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void agartha$sneakVanilla(CallbackInfo callbackInfo) {
      q var2 = e.get(q.class);
      if (var2 != null && var2.doVanilla()) {
         this.playerInput = q.forceSneak(this.playerInput);
      }
   }
}
