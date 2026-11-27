package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.render.m;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ChunkOcclusionDataBuilder.class})
public abstract class MixinNoRenderVisGraph {
   @Unique
   private static m agartha$noRenderCache;

   @Inject(
      method = {"markClosed"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noCaveCulling(BlockPos blockPos, CallbackInfo callbackInfo) {
      m var3 = agartha$noRender();
      if (var3 != null && var3.noCaveCulling()) {
         callbackInfo.cancel();
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
