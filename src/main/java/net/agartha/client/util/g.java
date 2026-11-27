package net.agartha.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public final class g {
   private g() {
   }

   public static boolean covers(BlockPos blockPos) {
      if (blockPos == null) {
         return false;
      } else {
         net.agartha.client.module.impl.misc.i var1 = net.agartha.client.module.e.get(net.agartha.client.module.impl.misc.i.class);
         if (var1 != null && var1.coversBlock(blockPos)) {
            return true;
         } else {
            net.agartha.client.module.impl.misc.h var2 = net.agartha.client.module.e.get(net.agartha.client.module.impl.misc.h.class);
            return var2 != null && var2.coversBlock(blockPos);
         }
      }
   }
}
