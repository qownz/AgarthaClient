package net.agartha.client.module.impl.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.LongPredicate;
import net.agartha.client.d.f;
import net.agartha.client.d.a_pkg.i;
import net.agartha.client.d.a_pkg.o;
import net.agartha.client.d.a_pkg.p;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.e.aj;
import net.agartha.client.k.c;
import net.agartha.client.l.d;
import net.agartha.client.module.b;
import net.agartha.client.util.a_o;
import net.agartha.client.util.ab;
import net.agartha.client.util.ac;
import net.agartha.client.util.l;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

@Environment(EnvType.CLIENT)
public final class SpawnerNotifierModule extends b {
   private static final int SCAN_INTERVAL_TICKS = 1;
   private static final float LINE_WIDTH = 1.5F;
   private static final int MAX_CHUNKS_PER_TICK = 48;
   private final net.agartha.client.util.a_pkg.b<SpawnerNotifierModule.SpawnerEntry> scan = new net.agartha.client.util.a_pkg.b();
   private final Set<Long> notifiedPositions = new HashSet<>();
   private final ArrayDeque<Long> pending = new ArrayDeque<>();
   private final Set<Long> pendingKeys = new HashSet<>();
   private final Map<Long, Long> fingerprints = new HashMap<>();
   private int tickCounter = 0;
   private int pruneTimer = 0;
   private int sweepIndex;
   private final c scanSlice = new c();
   private final net.agartha.client.module.a_pkg.b sChatNotify = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Chat Notify", true)
   );
   private final net.agartha.client.module.a_pkg.b sRender = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Render", true));
   private final net.agartha.client.module.a_pkg.b sCustomColor = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Custom Color", true)
   );
   private final net.agartha.client.module.a_pkg.c sColor = (net.agartha.client.module.a_pkg.c)this.addSetting(new net.agartha.client.module.a_pkg.c("Color", -43691));
   private final net.agartha.client.module.a_pkg.b sThemeColor = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Theme Color", false)
   );
   private final net.agartha.client.module.a_pkg.b sBeam = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Light Beam", true));
   private final net.agartha.client.module.a_pkg.b sLightning = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Lightning", true));
   private final d canvas = new d();
   private final net.agartha.client.l.c fx = new net.agartha.client.l.c();
   private final a_o fade = new a_o(0.6F, 0.4F);
   private final LongPredicate stillShown = this.fade::contains;
   private final net.agartha.client.util.a_o.a fadingMark = this::drawMark;
   private int frameColor;
   private double frameTime;
   private double frameTop;
   private static final int FX_PER_FRAME = 32;
   private static final double FX_RANGE_SQ = 25600.0;
   private int fxLeft;

   public SpawnerNotifierModule() {
      super("Spawner Notifier", "Finds mob spawners and marks them with a light beam and lightning.", net.agartha.client.module.c.DONUT);
      ac.wireExclusiveColorSource(this.sCustomColor, this.sThemeColor, this.sColor);
   }

   protected void onEnable() {
      this.clearState();
   }

   protected void onDisable() {
      this.clearState();
   }

   private void clearState() {
      this.scan.clear();
      this.fx.clear();
      this.fade.clear();
      this.notifiedPositions.clear();
      this.pending.clear();
      this.pendingKeys.clear();
      this.fingerprints.clear();
      this.sweepIndex = 0;
      this.tickCounter = 0;
      this.pruneTimer = 0;
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
            if (++this.pruneTimer >= 20) {
               this.pruneTimer = 0;
               this.scan
                  .prune(
                     l -> {
                        int var6 = (int)l;
                        int var7 = (int)(l >>> 32);
                        boolean var8 = Math.abs(var6 - var4.x) > var5
                           || Math.abs(var7 - var4.z) > var5
                           || var3 == null
                           || !var3.getChunkManager().isChunkLoaded(var6, var7);
                        if (var8) {
                           this.fingerprints.remove(l);
                        }

                        return var8;
                     }
                  );
               this.notifiedPositions.removeIf(long_ -> {
                  long var2x = ChunkPos.toLong(BlockPos.unpackLongX(long_) >> 4, BlockPos.unpackLongZ(long_) >> 4);
                  return !this.scan.cacheView().containsKey(var2x);
               });
            }
         }
      }
   }

   @f
   public void onPacketReceive(i arg) {
      if (arg.packet instanceof ChunkDataS2CPacket var2) {
         this.enqueue(ChunkPos.toLong(var2.getChunkX(), var2.getChunkZ()));
      } else if (arg.packet instanceof BlockEntityUpdateS2CPacket var3) {
         BlockPos var8 = var3.getPos();
         this.enqueue(ChunkPos.toLong(var8.getX() >> 4, var8.getZ() >> 4));
      } else if (arg.packet instanceof UnloadChunkS2CPacket var4) {
         long var10 = var4.pos().toLong();
         this.pendingKeys.remove(var10);
         this.fingerprints.remove(var10);
         this.scan.remove(var10);
      }
   }

   @f
   public void onScanFrame(p arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null) {
         if (this.scanSlice.begin(net.agartha.client.k.a.sliceNs(1), 48)) {
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
      ChunkPos var5 = minecraftClient.player.getChunkPos();
      int var6 = Math.max(2, minecraftClient.options.getClampedViewDistance());
      long var7 = net.agartha.client.k.b_o.begin();
      long var9 = l - (l - System.nanoTime()) / 2L;
      int var11 = 0;

      while (!this.pending.isEmpty() && System.nanoTime() < var9) {
         long var12 = this.pending.pollFirst();
         if (this.pendingKeys.remove(var12)) {
            int var14 = (int)var12;
            int var15 = (int)(var12 >>> 32);
            var11++;
            if (var4.getChunkManager().isChunkLoaded(var14, var15)) {
               this.refreshSpawners(minecraftClient, var4, var14, var15);
            }
         }
      }

      var11 += this.sweepSpawners(minecraftClient, var4, var5.x, var5.z, var6, l);
      net.agartha.client.k.b_o.record("scanner.spawner", var7);
      net.agartha.client.k.b_o.recordCount("scanner.spawner.chunks", var11);
   }

   private void enqueue(long l) {
      if (this.pendingKeys.add(l)) {
         this.pending.addFirst(l);
      }
   }

   @f
   public void onBlockUpdate(net.agartha.client.d.a_pkg.a arg) {
      BlockPos var2 = arg.pos;
      if (var2 != null) {
         boolean var3 = arg.oldState != null && arg.oldState.isOf(Blocks.SPAWNER);
         boolean var4 = arg.newState != null && arg.newState.isOf(Blocks.SPAWNER);
         boolean var5 = arg.newState == null || arg.newState.isAir();
         if (var3 || var4 || var5) {
            if (!var4) {
               net.agartha.client.util.d.forget(var2);
               this.fade.drop(var2.asLong());
            }

            MinecraftClient var6 = MinecraftClient.getInstance();
            ClientWorld var7 = var6.world;
            if (var7 != null) {
               BlockPos var8 = var2.toImmutable();
               long var9 = ChunkPos.toLong(var2.getX() >> 4, var2.getZ() >> 4);
               this.fingerprints.remove(var9);
               List var11 = this.scan.get(var9);
               ArrayList var12 = new ArrayList(var11 == null ? 1 : var11.size() + 1);
               boolean var13 = false;
               if (var11 != null) {
                  for (SpawnerNotifierModule.SpawnerEntry var15 : (Iterable<SpawnerNotifierModule.SpawnerEntry>) var11) {
                     if (var15.pos().equals(var8)) {
                        var13 = true;
                     } else {
                        var12.add(var15);
                     }
                  }
               }

               if (var4) {
                  WorldChunk var16 = var7.getChunkManager().getWorldChunk(var2.getX() >> 4, var2.getZ() >> 4);
                  String var18 = var16 != null ? this.spawnerEntityName(var7, var16, var8) : "Unknown";
                  var12.add(new SpawnerNotifierModule.SpawnerEntry(var8, var18));
                  var13 = true;
               }

               if (var13) {
                  this.scan.overwrite(var9, var12);
                  if (var4 && !var3) {
                     SpawnerNotifierModule.SpawnerEntry var17 = (SpawnerNotifierModule.SpawnerEntry)var12.get(var12.size() - 1);
                     if (this.notifiedPositions.add(var17.pos().asLong())) {
                        this.fireNotification(var6, List.of(var17));
                     }
                  } else if (!var4) {
                     this.notifiedPositions.remove(var8.asLong());
                  }
               }
            }
         }
      }
   }

   private int sweepSpawners(MinecraftClient minecraftClient, ClientWorld clientWorld, int i, int j, int k, long l) {
      int var8 = k * 2 + 1;
      int var9 = var8 * var8;
      int var10 = 0;
      int var11 = 0;
      int var12 = this.scanSlice.unitsLeft();

      while (var11 < var9 && var10 < var12 && System.nanoTime() < l) {
         int var13 = Math.floorMod(this.sweepIndex + var11, var9);
         var11++;
         int var14 = i + (var13 % var8 - k);
         int var15 = j + (var13 / var8 - k);
         if (clientWorld.getChunkManager().isChunkLoaded(var14, var15)) {
            long var16 = ChunkPos.toLong(var14, var15);
            if (!this.pendingKeys.contains(var16)) {
               this.refreshSpawners(minecraftClient, clientWorld, var14, var15);
               var10++;
            }
         }
      }

      this.sweepIndex = Math.floorMod(this.sweepIndex + var11, var9);
      this.scanSlice.useUnits(var10);
      return var10;
   }

   private void refreshSpawners(MinecraftClient minecraftClient, ClientWorld clientWorld, int i, int j) {
      WorldChunk var5 = clientWorld.getChunkManager().getWorldChunk(i, j);
      if (var5 != null) {
         long var6 = ChunkPos.toLong(i, j);
         long var8 = spawnerFingerprint(var5, var6);
         Long var10 = this.fingerprints.get(var6);
         if (var10 == null || var10 != var8 || var8 != 0L && this.scan.get(var6) == null) {
            this.fingerprints.put(var6, var8);
            List var11 = this.scan.get(var6);
            List var12 = this.findSpawnersInChunk(clientWorld, var5, var6);
            if (!sameSpawners(var11, var12)) {
               this.scan.overwrite(var6, var12);
               ArrayList var13 = new ArrayList();

               for (SpawnerNotifierModule.SpawnerEntry var15 : (Iterable<SpawnerNotifierModule.SpawnerEntry>) var12) {
                  if (this.notifiedPositions.add(var15.pos().asLong())) {
                     var13.add(var15);
                  }
               }

               if (var11 != null) {
                  for (SpawnerNotifierModule.SpawnerEntry var20 : (Iterable<SpawnerNotifierModule.SpawnerEntry>) var11) {
                     boolean var16 = false;

                     for (SpawnerNotifierModule.SpawnerEntry var18 : (Iterable<SpawnerNotifierModule.SpawnerEntry>) var12) {
                        if (var18.pos().equals(var20.pos())) {
                           var16 = true;
                           break;
                        }
                     }

                     if (!var16) {
                        this.notifiedPositions.remove(var20.pos().asLong());
                     }
                  }
               }

               if (!var13.isEmpty()) {
                  this.fireNotification(minecraftClient, var13);
               }
            }
         }
      }
   }

   @f
   public void onRender3D(o arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (this.sRender.value().get() && var2.world != null) {
         this.frameColor = ac.argb(this.sThemeColor, this.sCustomColor, this.sColor, -43691);
         this.frameTime = System.nanoTime() % 3600000000000L / 1.0E9;
         this.frameTop = var2.world.getTopYInclusive() + 1.0;
         this.fade.begin();
         this.fxLeft = 32;
         this.canvas.begin(var2.gameRenderer.getCamera());
         if (this.scan.nonemptyChunkCount() > 0) {
            for (long var4 : this.scan.nonemptyChunkKeys()) {
               int var6 = (int)var4;
               int var7 = (int)(var4 >>> 32);
               List var8 = this.scan.get(var4);
               if (var8 != null && !var8.isEmpty()) {
                  for (SpawnerNotifierModule.SpawnerEntry var10 : (Iterable<SpawnerNotifierModule.SpawnerEntry>) var8) {
                     long var11 = var10.pos().asLong();
                     float var13 = this.fade.show(var11);
                     if (!l.columnOutOfView(var6, var7, -64, 320)) {
                        this.drawMark(var11, var13);
                     }
                  }
               }
            }
         }

         this.fade.sweep(this.fadingMark);
         this.canvas.submit(true);
         this.fx.retain(this.stillShown);
      }
   }

   private void drawMark(long l, float f) {
      if (!(f <= 0.01F)) {
         int var4 = BlockPos.unpackLongX(l);
         int var5 = BlockPos.unpackLongY(l);
         int var6 = BlockPos.unpackLongZ(l);
         double var7 = 0.5 * (1.0 - a_o.pop(f));
         net.agartha.client.l.c.selection(
            this.canvas, var4 + var7, var5 + var7, var6 + var7, var4 + 1.0 - var7, var5 + 1.0 - var7, var6 + 1.0 - var7, this.frameColor, f, this.frameTime
         );
         if (this.fxLeft > 0 && this.canvas.distanceSqr(var4 + 0.5, var5 + 0.5, var6 + 0.5) < 25600.0) {
            this.fxLeft--;
            this.fx.draw(this.canvas, l, var4, var5, var6, this.frameTop, f, this.frameTime, this.frameColor, this.sBeam.get(), this.sLightning.get());
         }
      }
   }

   private static boolean sameSpawners(List<SpawnerNotifierModule.SpawnerEntry> list, List<SpawnerNotifierModule.SpawnerEntry> list2) {
      if (list2.isEmpty()) {
         return list == null || list.isEmpty();
      } else if (list != null && list.size() == list2.size()) {
         for (int var2 = 0; var2 < list2.size(); var2++) {
            BlockPos var3 = ((SpawnerNotifierModule.SpawnerEntry)list2.get(var2)).pos();
            boolean var4 = false;

            for (int var5 = 0; var5 < list.size(); var5++) {
               if (((SpawnerNotifierModule.SpawnerEntry)list.get(var5)).pos().equals(var3)) {
                  var4 = true;
                  break;
               }
            }

            if (!var4) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static long spawnerFingerprint(WorldChunk worldChunk, long l) {
      long var3 = 0L;

      for (Entry var6 : worldChunk.getBlockEntities().entrySet()) {
         if (var6.getValue() instanceof MobSpawnerBlockEntity) {
            var3 = var3 * 31L + ((BlockPos)var6.getKey()).asLong();
            if (var3 == 0L) {
               var3 = 1L;
            }
         }
      }

      for (Entry var8 : net.agartha.client.util.d.inChunk(l).entrySet()) {
         if (((net.agartha.client.util.d.a)var8.getValue()).block().equals(Blocks.SPAWNER)) {
            var3 = var3 * 31L + ((BlockPos)var8.getKey()).asLong();
            if (var3 == 0L) {
               var3 = 1L;
            }
         }
      }

      return var3;
   }

   private List<SpawnerNotifierModule.SpawnerEntry> findSpawnersInChunk(ClientWorld clientWorld, WorldChunk worldChunk, long l) {
      ArrayList var5 = null;

      for (Entry var7 : worldChunk.getBlockEntities().entrySet()) {
         if (var7.getValue() instanceof MobSpawnerBlockEntity) {
            BlockPos var8 = ((BlockPos)var7.getKey()).toImmutable();
            if (var5 == null) {
               var5 = new ArrayList(2);
            }

            var5.add(new SpawnerNotifierModule.SpawnerEntry(var8, this.spawnerEntityName(clientWorld, worldChunk, var8)));
         }
      }

      for (Entry var13 : net.agartha.client.util.d.inChunk(l).entrySet()) {
         if (((net.agartha.client.util.d.a)var13.getValue()).block().equals(Blocks.SPAWNER)) {
            BlockPos var14 = (BlockPos)var13.getKey();
            boolean var9 = false;
            if (var5 != null) {
               for (SpawnerNotifierModule.SpawnerEntry var11 : (Iterable<SpawnerNotifierModule.SpawnerEntry>) var5) {
                  if (var11.pos().equals(var14)) {
                     var9 = true;
                     break;
                  }
               }
            }

            if (!var9) {
               if (var5 == null) {
                  var5 = new ArrayList(2);
               }

               var5.add(new SpawnerNotifierModule.SpawnerEntry(var14.toImmutable(), "Unknown"));
            }
         }
      }

      return (List<SpawnerNotifierModule.SpawnerEntry>)(var5 == null ? List.<SpawnerNotifierModule.SpawnerEntry>of() : var5);
   }

   private String spawnerEntityName(ClientWorld clientWorld, WorldChunk worldChunk, BlockPos blockPos) {
      if (worldChunk.getBlockEntity(blockPos) instanceof MobSpawnerBlockEntity var5) {
         Entity var6 = var5.getLogic().getRenderedEntity(clientWorld, blockPos);
         return var6 == null ? "Unknown" : var6.getType().getName().getString();
      } else {
         return "Unknown";
      }
   }

   private void fireNotification(MinecraftClient minecraftClient, List<SpawnerNotifierModule.SpawnerEntry> list) {
      if (list.size() == 1) {
         SpawnerNotifierModule.SpawnerEntry var3 = (SpawnerNotifierModule.SpawnerEntry)list.get(0);
         BlockPos var4 = var3.pos();
         aj.getInstance().pushAlert(String.format("Spawner detected — %s (%d %d %d)", var3.entityName(), var4.getX(), var4.getY(), var4.getZ()));
      } else if (!list.isEmpty()) {
         aj.getInstance().pushAlert("Spawner detected — " + list.size() + " found");
      }

      if (this.sChatNotify.value().get()) {
         for (SpawnerNotifierModule.SpawnerEntry var7 : list) {
            BlockPos var5 = var7.pos();
            ab.send("Spawner", String.format("%s at %d %d %d", var7.entityName(), var5.getX(), var5.getY(), var5.getZ()));
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private record SpawnerEntry(BlockPos pos, String entityName) {
   }
}
