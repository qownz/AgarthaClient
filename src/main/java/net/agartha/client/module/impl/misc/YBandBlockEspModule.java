package net.agartha.client.module.impl.misc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.agartha.client.d.f;
import net.agartha.client.d.a_pkg.i;
import net.agartha.client.d.a_pkg.o;
import net.agartha.client.d.a_pkg.p;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.e.aj;
import net.agartha.client.k.c;
import net.agartha.client.module.b;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.util.ab;
import net.agartha.client.util.k;
import net.agartha.client.util.l;
import net.agartha.client.util.a_pkg.a;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

@Environment(EnvType.CLIENT)
abstract class YBandBlockEspModule extends b {
   static final int Y_MIN = -64;
   static final int Y_MAX = -60;
   private static final int SCAN_INTERVAL_TICKS = 1;
   private final net.agartha.client.util.a_pkg.b<BlockPos> scan = new net.agartha.client.util.a_pkg.b();
   private final c scanSlice = new c();
   private final Set<Long> coveredPosKeys = new HashSet<>();
   private int tickCounter;
   private int clientTick;
   private int pruneTimer;
   private int queueCooldown;
   private int queuedCenterX = Integer.MIN_VALUE;
   private int queuedCenterZ = Integer.MIN_VALUE;
   private final net.agartha.client.module.a_pkg.b sChatNotify;
   private final net.agartha.client.module.a_pkg.b sRender;
   private final net.agartha.client.module.a_pkg.c sColor;
   private final j sFillAlpha;
   private final j sLineWidth;
   private final j sBoxSize;
   private final Predicate<BlockState> matcher;
   private final net.agartha.client.module.impl.misc.u.b target;
   private final String chatPrefix;
   private final String detectedLabel;

   YBandBlockEspModule(
      String string, String string2, Predicate<BlockState> predicate, String string3, String string4, int i, net.agartha.client.module.impl.misc.u.b arg
   ) {
      super(string, string2, net.agartha.client.module.c.DONUT);
      this.matcher = predicate;
      this.chatPrefix = string3;
      this.detectedLabel = string4;
      this.target = arg;
      this.sChatNotify = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Chat Notify", true));
      this.sRender = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Render", true));
      this.sColor = (net.agartha.client.module.a_pkg.c)this.addSetting(new net.agartha.client.module.a_pkg.c("Color", i));
      this.sFillAlpha = (j)this.addSetting(new j("Fill Alpha", 0.25F, 0.0F, 1.0F));
      this.sLineWidth = (j)this.addSetting(new j("Line Width", 1.5F, 0.5F, 5.0F));
      this.sBoxSize = (j)this.addSetting(new j("Box Size", 1.0F, 0.1F, 2.0F));
   }

   public boolean coversBlock(BlockPos blockPos) {
      return this.isEnabled() && blockPos != null && this.coveredPosKeys.contains(blockPos.asLong());
   }

   protected void onEnable() {
      this.clearState();
   }

   protected void onDisable() {
      this.clearState();
   }

   private void clearState() {
      this.scan.clear();
      this.coveredPosKeys.clear();
      this.tickCounter = 0;
      this.clientTick = 0;
      this.pruneTimer = 0;
      this.queueCooldown = 0;
      this.queuedCenterX = Integer.MIN_VALUE;
      this.queuedCenterZ = Integer.MIN_VALUE;
   }

   @f
   public void onTick(q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null) {
         if (++this.tickCounter >= 1) {
            this.tickCounter = 0;
            ClientWorld var3 = var2.world;
            ChunkPos var4 = var2.player.getChunkPos();
            int var5 = Math.max(2, var2.options.getClampedViewDistance());
            this.clientTick++;
            if (++this.pruneTimer >= 20) {
               this.pruneTimer = 0;
               this.scan.prune(l -> {
                  int var5x = (int)l;
                  int var6 = (int)(l >>> 32);
                  return Math.abs(var5x - var4.x) > var5 || Math.abs(var6 - var4.z) > var5 || !var3.getChunkManager().isChunkLoaded(var5x, var6);
               });
               this.coveredPosKeys.removeIf(long_ -> {
                  long var2x = ChunkPos.toLong(BlockPos.unpackLongX(long_) >> 4, BlockPos.unpackLongZ(long_) >> 4);
                  return !this.scan.cacheView().containsKey(var2x);
               });
               u.prune(var3, var4, var5);
            }

            if (var4.x != this.queuedCenterX || var4.z != this.queuedCenterZ || ++this.queueCooldown >= 10) {
               this.queueCooldown = 0;
               this.queuedCenterX = var4.x;
               this.queuedCenterZ = var4.z;
               this.scan.queueInView(var4.x, var4.z, var5, this.clientTick, l -> {
                  int var3x = (int)l;
                  int var4x = (int)(l >>> 32);
                  return var3.getChunkManager().isChunkLoaded(var3x, var4x);
               });
            }
         }
      }
   }

   @f
   public void onPacketReceive(i arg) {
      long var2 = chunkKeyOf(arg);
      if (var2 != Long.MIN_VALUE) {
         MinecraftClient var4 = MinecraftClient.getInstance();
         if (var4.world != null) {
            u.invalidate(var4.world, var2);
         }
      }

      a.apply(arg, this.scan);
   }

   private static long chunkKeyOf(i arg) {
      if (arg.packet instanceof ChunkDataS2CPacket var4) {
         return ChunkPos.toLong(var4.getChunkX(), var4.getChunkZ());
      } else if (arg.packet instanceof BlockEntityUpdateS2CPacket var3) {
         BlockPos var7 = var3.getPos();
         return ChunkPos.toLong(var7.getX() >> 4, var7.getZ() >> 4);
      } else {
         return arg.packet instanceof UnloadChunkS2CPacket var1 ? var1.pos().toLong() : Long.MIN_VALUE;
      }
   }

   @f
   public void onScanFrame(p arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null) {
         if (this.scanSlice.begin(net.agartha.client.k.a.sliceNs(2), Integer.MAX_VALUE)) {
            try {
               this.scanFrame(var2, this.scanSlice.deadline());
            } finally {
               this.scanSlice.end();
            }
         }
      }
   }

   private void scanFrame(MinecraftClient minecraftClient, long l) {
      ClientWorld var4 = minecraftClient.world;
      long var5 = net.agartha.client.k.b_o.begin();
      int var7 = 0;
      ArrayList var8 = new ArrayList(0);

      while (System.nanoTime() < l) {
         Long var9 = this.scan.poll();
         if (var9 == null) {
            break;
         }

         int var10 = (int)var9.longValue();
         int var11 = (int)(var9 >>> 32);
         if (!var4.getChunkManager().isChunkLoaded(var10, var11)) {
            var8.add(var9);
         } else {
            WorldChunk var12 = var4.getChunkManager().getWorldChunk(var10, var11);
            if (var12 == null) {
               var8.add(var9);
            } else {
               List var13 = this.scan.get(var9);
               if (var13 == null) {
                  var13 = List.of();
               }

               List var14 = this.findInChunk(var4, var12);
               this.replaceChunkResult(var9, var13, var14);
               this.notifyNewlyFound(minecraftClient, var13, var14);
               var7++;
            }
         }
      }

      for (int var15 = var8.size() - 1; var15 >= 0; var15--) {
         this.scan.requestRescan((Long)var8.get(var15));
      }

      net.agartha.client.k.b_o.record("scanner.y-band", var5);
      net.agartha.client.k.b_o.recordCount("scanner.y-band.chunks", var7);
   }

   @f
   public void onBlockUpdate(net.agartha.client.d.a_pkg.a arg) {
      BlockPos var2 = arg.pos;
      if (var2 != null) {
         int var3 = var2.getY();
         if (var3 >= -64 && var3 <= -60) {
            boolean var4 = arg.oldState != null && this.matcher.test(arg.oldState);
            boolean var5 = arg.newState != null && this.matcher.test(arg.newState);
            if (var4 || var5) {
               BlockPos var6 = var2.toImmutable();
               long var7 = ChunkPos.toLong(var2.getX() >> 4, var2.getZ() >> 4);
               List var9 = this.scan.get(var7);
               ArrayList var10 = new ArrayList(var9 == null ? 1 : var9.size() + 1);
               boolean var11 = false;
               if (var9 != null) {
                  for (BlockPos var13 : (Iterable<BlockPos>) var9) {
                     if (var13.equals(var6)) {
                        var11 = true;
                     } else {
                        var10.add(var13);
                     }
                  }
               }

               if (var5) {
                  var10.add(var6);
                  var11 = true;
               }

               if (var11) {
                  this.scan.store(var7, (List)(var10.isEmpty() ? List.of() : var10), this.clientTick);
                  this.coveredPosKeys.remove(var6.asLong());
                  if (var5) {
                     this.coveredPosKeys.add(var6.asLong());
                  }

                  u.invalidate(MinecraftClient.getInstance().world, var7);
                  if (var5 && !var4) {
                     this.notifyPositions(MinecraftClient.getInstance(), List.of(var6));
                  }
               }
            }
         }
      }
   }

   @f
   public void onRender3D(o arg) {
      if (this.sRender.value().get() && this.scan.nonemptyChunkCount() != 0) {
         int var2 = this.sColor.argb();
         int var3 = Math.round(this.sFillAlpha.value()[0] * 255.0F);
         int var4 = withAlpha(var2, var3);
         float var5 = this.sLineWidth.value()[0];
         float var6 = this.sBoxSize.value()[0];

         for (long var8 : this.scan.nonemptyChunkKeys()) {
            int var10 = (int)var8;
            int var11 = (int)(var8 >>> 32);
            if (!l.columnOutOfView(var10, var11, -64, 320)) {
               List var12 = this.scan.get(var8);
               if (var12 != null && !var12.isEmpty()) {
                  for (BlockPos var14 : (Iterable<BlockPos>) var12) {
                     Box var15 = net.agartha.client.util.c.scaledBlockBox(var14, var6);
                     k.submitCuboid(var14, var15, var2, var5, var3 > 0 ? var4 : null, true);
                  }
               }
            }
         }
      }
   }

   private void notifyNewlyFound(MinecraftClient minecraftClient, List<BlockPos> list, List<BlockPos> list2) {
      if (!list2.isEmpty()) {
         HashSet var4 = new HashSet(list.size() * 2);

         for (BlockPos var6 : list) {
            var4.add(var6.asLong());
         }

         ArrayList var8 = new ArrayList();

         for (BlockPos var7 : list2) {
            if (!var4.contains(var7.asLong())) {
               var8.add(var7);
            }
         }

         if (!var8.isEmpty()) {
            this.notifyPositions(minecraftClient, var8);
         }
      }
   }

   private void notifyPositions(MinecraftClient minecraftClient, List<BlockPos> list) {
      if (this.sChatNotify.value().get()) {
         for (BlockPos var4 : list) {
            ab.send(this.chatPrefix, this.detectedLabel + " at " + var4.getX() + " " + var4.getY() + " " + var4.getZ());
         }
      }

      if (!list.isEmpty()) {
         BlockPos var5 = (BlockPos)list.get(0);
         String var6 = list.size() == 1
            ? this.detectedLabel + " at " + var5.getX() + " " + var5.getY() + " " + var5.getZ()
            : this.detectedLabel + " — " + list.size() + " found";
         aj.getInstance().pushAlert(this.chatPrefix + " — " + var6);
      }
   }

   private void replaceChunkResult(long l, List<BlockPos> list, List<BlockPos> list2) {
      for (BlockPos var6 : list) {
         this.coveredPosKeys.remove(var6.asLong());
      }

      this.scan.store(l, list2.isEmpty() ? List.of() : list2, this.clientTick);

      for (BlockPos var8 : list2) {
         this.coveredPosKeys.add(var8.asLong());
      }
   }

   private List<BlockPos> findInChunk(ClientWorld clientWorld, WorldChunk worldChunk) {
      return u.scan(clientWorld, worldChunk).positions(this.target);
   }

   private static int withAlpha(int i, int j) {
      return i & 16777215 | Math.min(255, Math.max(0, j)) << 24;
   }
}
