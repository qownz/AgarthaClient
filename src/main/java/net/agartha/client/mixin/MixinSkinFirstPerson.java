package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.misc.o;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin({HeldItemRenderer.class})
public abstract class MixinSkinFirstPerson {
   @ModifyVariable(
      method = {"renderArmHoldingItem", "renderArm"},
      at = @At("STORE"),
      require = 0
   )
   private Identifier agartha$armSkin(Identifier identifier) {
      o var2 = e.get(o.class);
      if (var2 == null) {
         return identifier;
      } else {
         Identifier var3 = var2.selfArmSkinPath();
         return var3 != null ? var3 : identifier;
      }
   }
}
