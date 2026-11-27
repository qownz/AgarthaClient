package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.NoRenderModule;
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
   private static NoRenderModule agartha$noRenderCache;

   @Inject(
      method = {"markClosed"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noCaveCulling(BlockPos blockPos, CallbackInfo callbackInfo) {
      NoRenderModule var3 = agartha$noRender();
      if (var3 != null && var3.noCaveCulling()) {
         callbackInfo.cancel();
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
