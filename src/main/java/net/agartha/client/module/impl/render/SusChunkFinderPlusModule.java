package net.agartha.client.module.impl.render;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.EndClientTickEvent;
import net.agartha.client.event.impl.PacketReceiveEvent;
import net.agartha.client.event.impl.ScanFrameEvent;
import net.agartha.client.util.perf.TimeSlice;
import net.agartha.client.util.world.GeodeAnalyzer.g;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

@Environment(EnvType.CLIENT)
public final class SusChunkFinderPlusModule extends SusChunkFinderModule {
   private static final long GROWTH_BUDGET_NS = 1000000L;
   private static final int GROWTH_CHUNKS_PER_TICK = 6;
   private static final int REFILL_TICKS = 20;
   private static final int GROWTH_LIGHT = 9;
   private static final int TALL_COLUMN = 3;
   private static final int BAMBOO_TOP = 12;
   private final Long2IntOpenHashMap overdue = new Long2IntOpenHashMap();
   private final LongArrayFIFOQueue growthQueue = new LongArrayFIFOQueue();
   private final LongOpenHashSet growthQueued = new LongOpenHashSet();
   private final TimeSlice growthSlice = new TimeSlice();
   private final Mutable cursor = new Mutable();
   private ClientWorld growthLevel;
   private int refill;

   public SusChunkFinderPlusModule() {
      super("Sus Chunk Finder+", "Sus Chunk Finder, with every find checked against the plants in its chunk.");
   }

   @Override
   protected void onEnable() {
      super.onEnable();
      this.resetGrowth();
   }

   @Override
   protected void onDisable() {
      super.onDisable();
      this.resetGrowth();
   }

   private void resetGrowth() {
      this.overdue.clear();
      this.growthQueue.clear();
      this.growthQueued.clear();
      this.growthLevel = null;
      this.refill = 20;
   }

   @Override
   protected List<g> filterZones(List<g> list, ClientWorld clientWorld) {
      int var3 = this.sensitivity();
      int var4 = var3 <= 4 ? 3 : 1;
      int var5 = var3 >= 8 ? 1 : 0;
      ArrayList var6 = new ArrayList(list.size());

      for (g var8 : list) {
         int var9 = 0;
         boolean var10 = false;

         for (long var12 : var8.members()) {
            int var14 = ChunkPos.getPackedX(var12);
            int var15 = ChunkPos.getPackedZ(var12);

            for (int var16 = -var5; var16 <= var5 && !var10; var16++) {
               for (int var17 = -var5; var17 <= var5; var17++) {
                  int var18 = var14 + var16;
                  int var19 = var15 + var17;
                  if (clientWorld.getChunkManager().isChunkLoaded(var18, var19)) {
                     long var20 = ChunkPos.toLong(var18, var19);
                     if (!this.overdue.containsKey(var20)) {
                        var10 = true;
                        this.enqueueGrowth(var20);
                        break;
                     }

                     var9 += this.overdue.get(var20);
                  }
               }
            }

            if (var10) {
               break;
            }
         }

         if (!var10 && var9 < var4) {
            var6.add(var8);
         }
      }

      return var6;
   }

   @EventListener
   public void onGrowthPacket(PacketReceiveEvent arg) {
      if (arg.packet instanceof ChunkDataS2CPacket var2) {
         long var6 = ChunkPos.toLong(var2.getChunkX(), var2.getChunkZ());
         this.overdue.remove(var6);
         this.enqueueGrowth(var6);
      } else if (arg.packet instanceof UnloadChunkS2CPacket var3) {
         long var8 = var3.pos().toLong();
         this.overdue.remove(var8);
         this.growthQueued.remove(var8);
      }
   }

   @EventListener
   public void onGrowthTick(EndClientTickEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientWorld var3 = var2.world;
      if (var3 != null && var2.player != null && var3.getRegistryKey() == World.OVERWORLD) {
         if (var3 != this.growthLevel) {
            this.resetGrowth();
            this.growthLevel = var3;
         }

         if (++this.refill >= 20) {
            this.refill = 0;
            ChunkPos var4 = var2.player.getChunkPos();
            int var5 = var2.options.getClampedViewDistance();

            for (int var6 = var4.x - var5; var6 <= var4.x + var5; var6++) {
               for (int var7 = var4.z - var5; var7 <= var4.z + var5; var7++) {
                  long var8 = ChunkPos.toLong(var6, var7);
                  if (!this.overdue.containsKey(var8) && var3.getChunkManager().isChunkLoaded(var6, var7)) {
                     this.enqueueGrowth(var8);
                  }
               }
            }
         }
      } else {
         if (this.growthLevel != null) {
            this.resetGrowth();
         }
      }
   }

   private void enqueueGrowth(long l) {
      if (this.growthQueued.add(l)) {
         this.growthQueue.enqueue(l);
      }
   }

   @EventListener
   public void onGrowthScanFrame(ScanFrameEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientWorld var3 = var2.world;
      if (var3 != null && var3 == this.growthLevel && !this.growthQueue.isEmpty()) {
         if (this.growthSlice.begin(1000000L, 6)) {
            try {
               long var4 = this.growthSlice.deadline();
               int var6 = 0;
               int var7 = this.growthSlice.unitsLeft();
               boolean var8 = false;

               while (var6 < var7 && !this.growthQueue.isEmpty() && System.nanoTime() < var4) {
                  long var9 = this.growthQueue.dequeueLong();
                  if (this.growthQueued.remove(var9)) {
                     WorldChunk var11 = var3.getChunkManager().getWorldChunk(ChunkPos.getPackedX(var9), ChunkPos.getPackedZ(var9));
                     if (var11 != null) {
                        this.overdue.put(var9, this.overdueIn(var3, var11));
                        var8 = true;
                        var6++;
                     }
                  }
               }

               this.growthSlice.useUnits(var6);
               if (var8) {
                  this.markDirty();
               }
            } catch (RuntimeException var15) {
               AgarthaClientMod.LOGGER.warn("[Agartha] Sus Chunk Finder+ plant scan failed: {}", var15.toString());
            } finally {
               this.growthSlice.end();
            }
         }
      }
   }

   private static boolean grows(BlockState blockState) {
      Block var1 = blockState.getBlock();
      return var1 == Blocks.KELP
         || var1 == Blocks.CAVE_VINES
         || var1 == Blocks.WEEPING_VINES
         || var1 == Blocks.TWISTING_VINES
         || var1 == Blocks.BAMBOO
         || var1 == Blocks.SUGAR_CANE
         || var1 == Blocks.CACTUS
         || var1 == Blocks.COCOA
         || var1 == Blocks.SWEET_BERRY_BUSH
         || var1 == Blocks.NETHER_WART
         || var1 instanceof CropBlock
         || var1 == Blocks.MELON_STEM
         || var1 == Blocks.PUMPKIN_STEM;
   }

   private int overdueIn(ClientWorld clientWorld, WorldChunk worldChunk) {
      ChunkSection[] var3 = worldChunk.getSectionArray();
      int var4 = worldChunk.getPos().getStartX();
      int var5 = worldChunk.getPos().getStartZ();
      int var6 = 0;

      for (int var7 = 0; var7 < var3.length; var7++) {
         ChunkSection var8 = var3[var7];
         if (var8 != null && !var8.isEmpty() && var8.hasAny(SusChunkFinderPlusModule::grows)) {
            int var9 = worldChunk.getBottomY() + (var7 << 4);

            for (int var10 = 0; var10 < 16; var10++) {
               for (int var11 = 0; var11 < 16; var11++) {
                  for (int var12 = 0; var12 < 16; var12++) {
                     BlockState var13 = var8.getBlockState(var12, var10, var11);
                     if (grows(var13) && this.behind(clientWorld, var13, var4 + var12, var9 + var10, var5 + var11)) {
                        var6++;
                     }
                  }
               }
            }
         }
      }

      return var6;
   }

   private boolean behind(ClientWorld clientWorld, BlockState blockState, int i, int j, int k) {
      Block var6 = blockState.getBlock();
      if (var6 == Blocks.KELP) {
         return (Integer)blockState.get(Properties.AGE_25) < 25 && this.at(clientWorld, i, j + 1, k).isOf(Blocks.WATER);
      } else if (var6 == Blocks.CAVE_VINES || var6 == Blocks.WEEPING_VINES) {
         return (Integer)blockState.get(Properties.AGE_25) < 25 && this.at(clientWorld, i, j - 1, k).isAir();
      } else if (var6 == Blocks.TWISTING_VINES) {
         return (Integer)blockState.get(Properties.AGE_25) < 25 && this.at(clientWorld, i, j + 1, k).isAir();
      } else if (var6 == Blocks.SUGAR_CANE || var6 == Blocks.CACTUS) {
         return this.at(clientWorld, i, j + 1, k).isAir() && this.columnBelow(clientWorld, i, j, k, var6) + 1 < 3;
      } else if (var6 == Blocks.BAMBOO) {
         return (Integer)blockState.get(Properties.STAGE) == 0
            && this.at(clientWorld, i, j + 1, k).isAir()
            && this.columnBelow(clientWorld, i, j, k, var6) + 1 < 12
            && this.light(clientWorld, i, j + 1, k) >= 9;
      } else if (var6 == Blocks.COCOA) {
         return (Integer)blockState.get(Properties.AGE_2) < 2;
      } else if (var6 == Blocks.SWEET_BERRY_BUSH) {
         return (Integer)blockState.get(Properties.AGE_3) < 3 && this.light(clientWorld, i, j + 1, k) >= 9;
      } else if (var6 == Blocks.NETHER_WART) {
         return (Integer)blockState.get(Properties.AGE_3) < 3;
      } else if (var6 instanceof CropBlock var7) {
         return !var7.isMature(blockState) && this.light(clientWorld, i, j, k) >= 9;
      } else {
         return var6 != Blocks.MELON_STEM && var6 != Blocks.PUMPKIN_STEM
            ? false
            : (Integer)blockState.get(Properties.AGE_7) < 7 && this.light(clientWorld, i, j, k) >= 9;
      }
   }

   private BlockState at(ClientWorld clientWorld, int i, int j, int k) {
      return clientWorld.getBlockState(this.cursor.set(i, j, k));
   }

   private int light(ClientWorld clientWorld, int i, int j, int k) {
      return clientWorld.getLightLevel(this.cursor.set(i, j, k));
   }

   private int columnBelow(ClientWorld clientWorld, int i, int j, int k, Block block) {
      int var6 = 0;

      while (var6 < 12 && this.at(clientWorld, i, j - 1 - var6, k).isOf(block)) {
         var6++;
      }

      return var6;
   }
}
