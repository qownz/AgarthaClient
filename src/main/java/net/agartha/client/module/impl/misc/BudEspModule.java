package net.agartha.client.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;

@Environment(EnvType.CLIENT)
public final class BudEspModule extends YBandEspModule {
   public BudEspModule() {
      super(
         "Bud ESP",
         "Highlights small, medium and large amethyst buds between Y -64 and -60.",
         blockState -> blockState.isOf(Blocks.SMALL_AMETHYST_BUD) || blockState.isOf(Blocks.MEDIUM_AMETHYST_BUD) || blockState.isOf(Blocks.LARGE_AMETHYST_BUD),
         "BudESP",
         "Bud detected",
         -5010177,
         YBandChunkScanner.b.BUD
      );
   }
}
