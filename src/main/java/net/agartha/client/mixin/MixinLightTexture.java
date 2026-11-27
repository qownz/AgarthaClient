package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.FullbrightModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Environment(EnvType.CLIENT)
@Mixin({LightmapTextureManager.class})
public abstract class MixinLightTexture {
   @ModifyArg(
      method = {"update"},
      at = @At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",
         ordinal = 6
      ),
      index = 0
   )
   private float agartha$fullbrightGamma(float f) {
      FullbrightModule var2 = ModuleManager.get(FullbrightModule.class);
      return var2 == null ? f : var2.blend(f);
   }
}
