package net.agartha.client.util.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

@Environment(EnvType.CLIENT)
public final class BlockBox {
   private static final double HALF_BLOCK = 0.5;
   private static final float MIN_SIZE = 0.1F;
   private static final float MAX_SIZE = 2.0F;

   private BlockBox() {
   }

   public static Box scaledBlockBox(BlockPos blockPos, float f) {
      float var2 = Math.max(0.1F, Math.min(2.0F, f));
      double var3 = var2 * 0.5;
      double var5 = blockPos.getX() + 0.5;
      double var7 = blockPos.getY() + 0.5;
      double var9 = blockPos.getZ() + 0.5;
      return new Box(var5 - var3, var7 - var3, var9 - var3, var5 + var3, var7 + var3, var9 + var3);
   }
}
