package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.NoRenderModule;
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
      NoRenderModule var2 = ModuleManager.get(NoRenderModule.class);
      return var2 != null && var2.noCaveCulling() ? false : bl;
   }
}
