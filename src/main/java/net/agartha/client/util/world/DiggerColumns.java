package net.agartha.client.util.world;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class DiggerColumns {
   public static final int MIN_LENGTH = 3;
   public static final int MAX_LENGTH = 8;

   private DiggerColumns() {
   }

   public static boolean isWall(Block block) {
      return block == Blocks.ANDESITE || block == Blocks.GRANITE || block == Blocks.DIORITE;
   }

   public static boolean ignoredCenter(BlockState blockState) {
      if (blockState == null) {
         return true;
      } else {
         Block var1 = blockState.getBlock();
         if (!isWall(var1) && !airish(blockState)) {
            String var2 = blockPath(blockState);
            return var2.contains("ore")
               || var2.contains("raw_")
               || var2.contains("amethyst")
               || var1 == Blocks.GRAVEL
               || var1 == Blocks.DIRT
               || var1 == Blocks.SAND
               || var1 == Blocks.DRIPSTONE_BLOCK
               || var1 == Blocks.LAVA
               || var1 == Blocks.NETHERRACK;
         } else {
            return false;
         }
      }
   }

   public static boolean airish(BlockState blockState) {
      return blockState == null
         ? true
         : blockState.isAir()
            || blockState.isOf(Blocks.CAVE_AIR)
            || blockState.isOf(Blocks.VOID_AIR)
            || blockState.isOf(Blocks.STRUCTURE_VOID)
            || blockState.isOf(Blocks.LIGHT)
            || blockState.isOf(Blocks.WATER)
            || blockState.isOf(Blocks.VINE);
   }

   public static boolean piercing(boolean bl, Block block, Block block2, Block block3, Block block4, Block block5) {
      if (bl || block != Blocks.STONE || block2 == null) {
         return false;
      } else {
         return block2 == block3 && block3 == block4 && block4 == block5 ? isWall(block2) : false;
      }
   }

   public static List<int[]> runs(boolean[] bls, boolean[] bls2) {
      return runs(bls, bls2, bls.length, 3, 8);
   }

   public static List<int[]> runs(boolean[] bls, boolean[] bls2, int i) {
      return runs(bls, bls2, i, 3, 8);
   }

   public static List<int[]> runs(boolean[] bls, boolean[] bls2, int i, int j, int k) {
      ArrayList var5 = new ArrayList();
      collect(bls, bls2, i, j, k, (ix, jx) -> var5.add(new int[]{ix, jx}));
      return var5;
   }

   public static boolean cappedByStone(boolean[] bls, int i, int j, int k) {
      int var4 = j - 1;
      int var5 = j + k;
      return var4 >= 0 && var5 < i && var5 < bls.length ? bls[var4] && bls[var5] : false;
   }

   public static void collect(boolean[] bls, boolean[] bls2, int i, int j, int k, DiggerColumns.a arg) {
      int var6 = Math.max(1, Math.min(j, k));
      int var7 = Math.max(j, k);
      int var8 = -1;
      boolean var9 = false;
      int var10 = Math.min(i, Math.min(bls.length, bls2.length));

      for (int var11 = 0; var11 <= var10; var11++) {
         boolean var12 = var11 < var10 && bls[var11];
         if (var12) {
            if (var8 < 0) {
               var8 = var11;
            }

            if (!bls2[var11]) {
               var9 = true;
            }
         } else if (var8 >= 0) {
            int var13 = var11 - var8;
            if (var9 && var13 >= var6 && var13 <= var7) {
               arg.accept(var8, var13);
            }

            var8 = -1;
            var9 = false;
         }
      }
   }

   private static String blockPath(BlockState blockState) {
      Identifier var1 = Registries.BLOCK.getId(blockState.getBlock());
      return var1 == null ? "" : var1.getPath();
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   public interface a {
      void accept(int i, int j);
   }
}
