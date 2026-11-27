package net.agartha.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.agartha.client.module.e;
import net.agartha.client.module.impl.misc.k;
import net.agartha.client.module.impl.render.d;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin({GameRenderer.class})
public abstract class MixinFreeCameraHands {
   @ModifyExpressionValue(
      method = {"renderHand"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z"
      )},
      require = 0
   )
   private boolean agartha$showHandsWhileDetached(boolean bl) {
      k var2 = e.get(k.class);
      d var3 = e.get(d.class);
      boolean var4 = var2 != null && var2.isEnabled();
      boolean var5 = var3 != null && var3.isEnabled();
      return !var4 && !var5 ? bl : var4 && var2.showHands() || var5 && var3.showHands();
   }
}
