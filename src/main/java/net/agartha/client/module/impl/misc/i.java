package net.agartha.client.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;

@Environment(EnvType.CLIENT)
public final class i extends YBandBlockEspModule {
   public i() {
      super("Cake ESP", "Highlights cakes between Y -64 and -60.", blockState -> blockState.isOf(Blocks.CAKE), "CakeESP", "Cake detected", -38476, u.b.CAKE);
   }
}
