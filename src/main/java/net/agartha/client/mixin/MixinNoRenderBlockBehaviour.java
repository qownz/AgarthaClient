package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.render.m;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({AbstractBlock.class})
public abstract class MixinNoRenderBlockBehaviour {
   @Unique
   private static m agartha$noRenderCache;

   @Inject(
      method = {"getRenderingSeed"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noTextureRotations(BlockState blockState, BlockPos blockPos, CallbackInfoReturnable<Long> callbackInfoReturnable) {
      m var4 = agartha$noRender();
      if (var4 != null && var4.noTextureRotations()) {
         callbackInfoReturnable.setReturnValue(0L);
      }
   }

   @Unique
   private static m agartha$noRender() {
      m var0 = agartha$noRenderCache;
      if (var0 == null) {
         var0 = e.get(m.class);
         agartha$noRenderCache = var0;
      }

      return var0;
   }
}
