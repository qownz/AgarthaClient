package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.render.t;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({HeldItemRenderer.class})
public abstract class MixinSwingAnimationItemInHand {
   @Inject(
      method = {"swingArm"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void agartha$swing(float f, MatrixStack matrixStack, int i, Arm arm, CallbackInfo callbackInfo) {
      t var6 = e.get(t.class);
      if (var6 != null && var6.shouldApply(arm) && !var6.skipWhileIdle(f)) {
         var6.apply(matrixStack, arm, f);
         callbackInfo.cancel();
      }
   }
}
