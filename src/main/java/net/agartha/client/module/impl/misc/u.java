package net.agartha.client.module.impl.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

@Environment(EnvType.CLIENT)
final class u {
   private static final Map<Long, u.a> RESULTS = new HashMap<>();
   private static ClientWorld cachedLevel;

   private u() {
   }

   static u.a scan(ClientWorld clientWorld, WorldChunk worldChunk) {
      if (cachedLevel != clientWorld) {
         RESULTS.clear();
         cachedLevel = clientWorld;
      }

      long var2 = worldChunk.getPos().toLong();
      u.a var4 = RESULTS.get(var2);
      if (var4 != null) {
         return var4;
      } else {
         ArrayList var5 = new ArrayList();
         ArrayList var6 = new ArrayList();
         ChunkPos var7 = worldChunk.getPos();
         int var8 = var7.getStartX();
         int var9 = var7.getStartZ();
         ChunkSection[] var10 = worldChunk.getSectionArray();

         for (int var11 = 0; var11 < var10.length; var11++) {
            ChunkSection var12 = var10[var11];
            if (var12 != null && !var12.isEmpty()) {
               int var13 = clientWorld.getBottomY() + (var11 << 4);
               if (var13 + 15 >= -64 && var13 <= -60 && var12.hasAny(u::a)) {
                  for (int var14 = 0; var14 < 16; var14++) {
                     int var15 = var13 + var14;
                     if (var15 >= -64 && var15 <= -60) {
                        for (int var16 = 0; var16 < 16; var16++) {
                           for (int var17 = 0; var17 < 16; var17++) {
                              BlockState var18 = var12.getBlockState(var16, var14, var17);
                              BlockPos var19 = null;
                              if (var18.isOf(Blocks.CAKE)) {
                                 var19 = new BlockPos(var8 + var16, var15, var9 + var17);
                                 var5.add(var19);
                              }

                              if (b(var18)) {
                                 if (var19 == null) {
                                    var19 = new BlockPos(var8 + var16, var15, var9 + var17);
                                 }

                                 var6.add(var19);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         u.a var20 = new u.a(a(var5), a(var6));
         if (!var5.isEmpty() || !var6.isEmpty()) {
            RESULTS.put(var2, var20);
         }

         return var20;
      }
   }

   static void invalidate(ClientWorld clientWorld, long l) {
      if (cachedLevel == clientWorld) {
         RESULTS.remove(l);
      }
   }

   static void prune(ClientWorld clientWorld, ChunkPos chunkPos, int i) {
      if (cachedLevel != clientWorld) {
         RESULTS.clear();
         cachedLevel = clientWorld;
      } else {
         RESULTS.keySet().removeIf(long_ -> {
            int var3 = (int)long_.longValue();
            int var4 = (int)(long_ >> 32);
            return Math.abs(var3 - chunkPos.x) > i || Math.abs(var4 - chunkPos.z) > i;
         });
      }
   }

   private static boolean a(BlockState blockState) {
      return blockState.isOf(Blocks.CAKE) || b(blockState);
   }

   private static boolean b(BlockState blockState) {
      return blockState.isOf(Blocks.SMALL_AMETHYST_BUD) || blockState.isOf(Blocks.MEDIUM_AMETHYST_BUD) || blockState.isOf(Blocks.LARGE_AMETHYST_BUD);
   }

   private static List<BlockPos> a(List<BlockPos> list) {
      return list.isEmpty() ? List.of() : Collections.unmodifiableList(list);
   }

   @Environment(EnvType.CLIENT)
   record a(List<BlockPos> cakes, List<BlockPos> buds) {
      List<BlockPos> positions(u.b arg) {
         return arg == u.b.CAKE ? this.cakes : this.buds;
      }
   }

   @Environment(EnvType.CLIENT)
   static enum b {
      CAKE,
      BUD;
   }
}
