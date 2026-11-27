package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.render.m;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.ChunkRenderingDataPreparer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin({ChunkRenderingDataPreparer.class})
public abstract class MixinNoRenderCaveCulling {
   @ModifyVariable(
      method = {"update"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private boolean agartha$noCaveCulling(boolean bl) {
      m var2 = e.get(m.class);
      return var2 != null && var2.noCaveCulling() ? false : bl;
   }
}
