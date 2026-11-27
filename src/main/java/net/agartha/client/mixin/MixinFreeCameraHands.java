package net.agartha.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.misc.FreecamModule;
import net.agartha.client.module.impl.render.FreelookModule;
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
      FreecamModule var2 = ModuleManager.get(FreecamModule.class);
      FreelookModule var3 = ModuleManager.get(FreelookModule.class);
      boolean var4 = var2 != null && var2.isEnabled();
      boolean var5 = var3 != null && var3.isEnabled();
      return !var4 && !var5 ? bl : var4 && var2.showHands() || var5 && var3.showHands();
   }
}
