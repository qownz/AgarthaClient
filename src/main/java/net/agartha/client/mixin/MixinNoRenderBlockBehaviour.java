package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.NoRenderModule;
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
   private static NoRenderModule agartha$noRenderCache;

   @Inject(
      method = {"getRenderingSeed"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noTextureRotations(BlockState blockState, BlockPos blockPos, CallbackInfoReturnable<Long> callbackInfoReturnable) {
      NoRenderModule var4 = agartha$noRender();
      if (var4 != null && var4.noTextureRotations()) {
         callbackInfoReturnable.setReturnValue(0L);
      }
   }

   @Unique
   private static NoRenderModule agartha$noRender() {
      NoRenderModule var0 = agartha$noRenderCache;
      if (var0 == null) {
         var0 = ModuleManager.get(NoRenderModule.class);
         agartha$noRenderCache = var0;
      }

      return var0;
   }
}
