package net.agartha.client.util.world;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.misc.BudEspModule;
import net.agartha.client.module.impl.misc.CakeEspModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public final class BlockCoverage {
   private BlockCoverage() {
   }

   public static boolean covers(BlockPos blockPos) {
      if (blockPos == null) {
         return false;
      } else {
         CakeEspModule var1 = ModuleManager.get(CakeEspModule.class);
         if (var1 != null && var1.coversBlock(blockPos)) {
            return true;
         } else {
            BudEspModule var2 = ModuleManager.get(BudEspModule.class);
            return var2 != null && var2.coversBlock(blockPos);
         }
      }
   }
}
