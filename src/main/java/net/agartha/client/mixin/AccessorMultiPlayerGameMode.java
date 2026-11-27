package net.agartha.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin({ClientPlayerInteractionManager.class})
public interface AccessorMultiPlayerGameMode {
   @Accessor("currentBreakingPos")
   BlockPos agartha$destroyBlockPos();

   @Accessor("currentBreakingProgress")
   float agartha$destroyProgress();
}
