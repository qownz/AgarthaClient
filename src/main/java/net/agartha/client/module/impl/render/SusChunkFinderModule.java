package net.agartha.client.module.impl.render;

import imgui.ImDrawList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.d.a_pkg.i;
import net.agartha.client.d.a_pkg.o;
import net.agartha.client.d.a_pkg.p;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.module.b;
import net.agartha.client.module.f;
import net.agartha.client.module.a_pkg.c;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.util.ac;
import net.agartha.client.util.ad;
import net.agartha.client.util.l;
import net.agartha.client.util.u;
import net.agartha.client.util.w;
import net.agartha.client.util.y;
import net.agartha.client.util.u.a;
import net.agartha.client.util.y.e;
import net.agartha.client.util.y.g;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

@Environment(EnvType.CLIENT)
public class SusChunkFinderModule extends b implements f {
   private static final float TRACER_WIDTH = 1.5F;
   private static final boolean DEBUG = Boolean.getBoolean("agartha.sus.debug");
   private static final long SCAN_BUDGET_NS = 2000000L;
   private static final int REBUILD_INTERVAL_TICKS = 20;
   private static final int FORCE_REBUILD_TICKS = 100;
   private static final int REFILL_HYSTERESIS = 3;
   private static final int IDLE_REFILL_TICKS = 100;
   private static final int MAX_QUEUE_ATTEMPTS = 128;
   private static final int MAX_SCAN_RADIUS = 16;
   private static final long SETTLE_MS = 10000L;
   private static final int SIMULATION_MARGIN = 1;
   private static final long ACTIVE_WINDOW_MS = 600000L;
   private static final int SCAN_SPEED = 10;
   private static final int MAX_EMITTERS_PER_CHUNK = 1024;
   private static final int MERGE_RADIUS = 3;
   private static final int FILL_ALPHA = 90;
   private static final int OUTLINE_ALPHA = 200;
   private static final float FADE_IN_SECONDS = 0.7F;
   private static final float FADE_OUT_SECONDS = 0.5F;
   private static final float MOVE_RATE = 7.0F;
   private static final double PLATE_HEIGHT = 0.35;
   private static final double MAX_ZONE_SIZE = 48.0;
   private static final float LINE_WIDTH = 1.5F;
   private static final long PIN_RELOAD_GRACE_MS = 20000L;
   private final j sSensitivity = (j)this.addSetting(new j("Sensitivity", 3.0F, 1.0F, 10.0F).whole());
   private final net.agartha.client.module.a_pkg.b sThemeColor = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Theme Color", true)
   );
   private final net.agartha.client.module.a_pkg.b sCustomColor = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Custom Color", false)
   );
   private final c sColor = (c)this.addSetting(new c("Color", -11549705));
   private final j sPlateY = (j)this.addSetting(new j("Plate Y", 60.0F, -64.0F, 320.0F).whole());
   private final net.agartha.client.module.a_pkg.b sTracers = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Tracers", false));
   private final u light = new u();
   private final net.agartha.client.k.c slice = new net.agartha.client.k.c();
   private final Map<Long, List<net.agartha.client.util.y.b>> scanned = new HashMap<>();
   private final ArrayDeque<Long> queue = new ArrayDeque<>();
   private final Set<Long> queued = new HashSet<>();
   private final Set<Long> lightRescans = new LinkedHashSet<>();
   private final Map<Long, Long> arrivedAt = new HashMap<>();
   private final Map<Long, ArrayDeque<double[]>> events = new HashMap<>();
   private boolean dirty = true;
   private final Set<Long> dirtyChunks = new LinkedHashSet<>();
   private final List<e> cachedGeodes = new ArrayList<>();
   private final ArrayDeque<Long> deferred = new ArrayDeque<>();
   private final ArrayDeque<SusChunkFinderModule.LightJob> lightJobs = new ArrayDeque<>();
   private final Map<Long, Integer> lightJobCount = new HashMap<>();
   private final Map<Long, Integer> lightEpoch = new HashMap<>();
   private final Set<Long> indexing = new HashSet<>();
   private final ArrayDeque<Long> seedQueue = new ArrayDeque<>();
   private boolean chunksChanged = true;
   private int sinceRebuild;
   private final List<net.agartha.client.util.y.b> rebuildScratch = new ArrayList<>();
   private static final ExecutorService GROUPER = Executors.newSingleThreadExecutor(runnable -> {
      Thread var1 = new Thread(runnable, "Agartha Sus Grouper");
      var1.setDaemon(true);
      var1.setPriority(1);
      return var1;
   });
   private CompletableFuture<List<e>> pendingGroups;
   private Set<Long> pendingCollect;
   private int groupEpoch;
   private int pendingEpoch;
   private ClientWorld lastLevel;
   private ChunkPos lastQueueCenter;
   private int ticks;
   private int idleTicks;
   private List<g> zones = List.of();
   private final Map<Long, g> pinnedByChunk = new HashMap<>();
   private final Map<Long, Long> pinHiddenAt = new HashMap<>();
   private final Map<Long, double[]> zoneFades = new HashMap<>();
   private final Set<Long> liveScratch = new HashSet<>();
   private long lastFrameNanos;

   public SusChunkFinderModule() {
      this("Sus Chunk Finder", "Marks chunks where players have been: dig down under the plate.");
   }

   protected SusChunkFinderModule(String string, String string2) {
      super(string, string2, net.agartha.client.module.c.DONUT);
      ac.wireExclusiveColorSource(this.sCustomColor, this.sThemeColor, this.sColor, true);
      this.sSensitivity.onChange(this::reset);
   }

   protected List<g> filterZones(List<g> list, ClientWorld clientWorld) {
      return list;
   }

   protected final int sensitivity() {
      return this.sSensitivity.getInt();
   }

   protected final void markDirty() {
      this.dirty = true;
   }

   public List<g> zones() {
      return this.zones;
   }

   protected void onEnable() {
      this.reset();
   }

   protected void onDisable() {
      this.reset();
   }

   private void reset() {
      this.light.clear();
      this.scanned.clear();
      this.queue.clear();
      this.queued.clear();
      this.deferred.clear();
      this.lightJobs.clear();
      this.lightJobCount.clear();
      this.lightEpoch.clear();
      this.indexing.clear();
      this.seedQueue.clear();
      this.lightRescans.clear();
      this.dirtyChunks.clear();
      this.cachedGeodes.clear();
      this.pendingGroups = null;
      this.pendingCollect = null;
      this.groupEpoch++;
      this.arrivedAt.clear();
      this.events.clear();
      this.pinnedByChunk.clear();
      this.pinHiddenAt.clear();
      this.zoneFades.clear();
      this.zones = List.of();
      this.lastLevel = null;
      this.lastQueueCenter = null;
      this.ticks = 0;
      this.idleTicks = 0;
      this.lastFrameNanos = 0L;
      this.dirty = true;
      this.chunksChanged = true;
      this.sinceRebuild = 0;
      this.rebuildScratch.clear();
   }

   @net.agartha.client.d.f
   public void onPacketReceive(i arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientWorld var3 = var2.world;
      if (var3 != null && isOverworld(var3)) {
         int var4 = var3.getLightingProvider().getBottomY();
         if (arg.packet instanceof LightUpdateS2CPacket var5) {
            long var10 = ChunkPos.toLong(var5.getChunkX(), var5.getChunkZ());
            this.enqueueLight(var5.getChunkX(), var5.getChunkZ(), var10, var5.getData(), var4, true);
         } else if (arg.packet instanceof ChunkDataS2CPacket var6) {
            long var12 = ChunkPos.toLong(var6.getChunkX(), var6.getChunkZ());
            if (this.pinnedByChunk.containsKey(var12)) {
               this.pinHiddenAt.remove(var12);
               this.dirty = true;
               return;
            }

            this.light.forget(var6.getChunkX(), var6.getChunkZ());
            this.cancelLightJobs(var12);
            this.arrivedAt.put(var12, System.currentTimeMillis());
            this.scanned.remove(var12);
            this.dirtyChunks.add(var12);
            this.enqueueLight(var6.getChunkX(), var6.getChunkZ(), var12, var6.getLightData(), var4, false);
            this.chunksChanged = true;
            this.dirty = true;
         } else if (arg.packet instanceof UnloadChunkS2CPacket var7) {
            long var14 = var7.pos().toLong();
            this.light.forget(var7.pos().x, var7.pos().z);
            this.cancelLightJobs(var14);
            this.scanned.remove(var14);
            this.dirtyChunks.add(var14);
            this.arrivedAt.remove(var14);
            this.queued.remove(var14);
            this.seedQueue.remove(var14);
            this.chunksChanged = true;
            this.dirty = true;
         }
      }
   }

   private void recordChange(MinecraftClient minecraftClient, ClientWorld clientWorld, long l, int i, int j, int k, int m, int n) {
      if (minecraftClient.player != null && y.classify(m, n) != net.agartha.client.util.y.c.NONE) {
         if (this.atKnownGeode(l, i, j, k)) {
            long var10 = System.currentTimeMillis();
            Long var12 = this.arrivedAt.get(l);
            if (var12 != null && var10 - var12 >= 10000L) {
               int var13 = clientWorld.getSimulationDistance() + 1;
               if (y.chunkDistance(l, minecraftClient.player.getChunkPos().toLong()) > var13) {
                  this.events.computeIfAbsent(l, long_ -> new ArrayDeque<>()).addLast(new double[]{var10, i + 0.5, k + 0.5});
                  this.dirty = true;
                  if (DEBUG) {
                     AgarthaClientMod.LOGGER.info("[sus] {} at {},{} light {} -> {}", new Object[]{y.classify(m, n), i, k, m, n});
                  }
               }
            }
         }
      }
   }

   private static boolean isOverworld(ClientWorld clientWorld) {
      return clientWorld.getRegistryKey() == World.OVERWORLD;
   }

   private boolean atKnownGeode(long l, int i, int j, int k) {
      List var6 = this.scanned.get(l);
      if (var6 == null) {
         return false;
      } else {
         byte var7 = 5;

         for (net.agartha.client.util.y.b var9 : (Iterable<net.agartha.client.util.y.b>) var6) {
            if (Math.abs(var9.x() - i) <= var7 && Math.abs(var9.y() - j) <= var7 && Math.abs(var9.z() - k) <= var7) {
               return true;
            }
         }

         return false;
      }
   }

   @net.agartha.client.d.f
   public void onTick(q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientWorld var3 = var2.world;
      if (var3 != null && var2.player != null) {
         if (!isOverworld(var3)) {
            if (!this.scanned.isEmpty() || !this.zones.isEmpty() || this.lastLevel != null) {
               this.reset();
            }
         } else {
            try {
               ChunkPos var4 = var2.player.getChunkPos();
               int var5 = Math.min(var2.options.getClampedViewDistance() + 1, 16);
               if (var3 != this.lastLevel) {
                  this.reset();
                  this.lastLevel = var3;
                  this.seedLoadedChunks(var3, var4, var5);
               }

               this.refillQueue(var3, var4, var5);
               this.promoteDeferred(var3);
               this.ticks++;
               this.sinceRebuild++;
               boolean var6 = this.sinceRebuild >= 100 && !this.events.isEmpty();
               if (var6) {
                  this.dirty = true;
                  this.sinceRebuild = 0;
               }

               boolean var7 = this.pendingGroups != null && this.pendingGroups.isDone();
               if (var7 || !var6 && this.ticks % 20 == 0 && this.dirty) {
                  this.dirty = false;
                  this.rebuild(var2, var3);
                  this.sinceRebuild = 0;
               }
            } catch (RuntimeException var8) {
               AgarthaClientMod.LOGGER.warn("[Agartha] Sus Chunk Finder scan failed: {}", var8.toString());
            }
         }
      }
   }

   @net.agartha.client.d.f
   public void onScanFrame(p arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientWorld var3 = var2.world;
      if (var3 != null && var2.player != null && var3 == this.lastLevel && isOverworld(var3)) {
         if (this.slice.begin(2000000L, 10)) {
            try {
               long var4 = this.slice.deadline();
               this.drainLight(var2, var3, var4);
               this.drainSeed(var3, var4);
               int var6 = this.slice.unitsLeft();
               int var7 = this.rescanLitChunks(var3, var6, var4);
               var7 += this.scanQueue(var3, var6 - var7, var4);
               this.slice.useUnits(var7);
            } catch (RuntimeException var11) {
               AgarthaClientMod.LOGGER.warn("[Agartha] Sus Chunk Finder scan failed: {}", var11.toString());
            } finally {
               this.slice.end();
            }
         }
      }
   }

   private void seedLoadedChunks(ClientWorld clientWorld, ChunkPos chunkPos, int i) {
      long var4 = System.currentTimeMillis();
      this.seedQueue.clear();

      for (int var6 = -i; var6 <= i; var6++) {
         for (int var7 = -i; var7 <= i; var7++) {
            if (clientWorld.getChunkManager().isChunkLoaded(chunkPos.x + var6, chunkPos.z + var7)) {
               long var8 = ChunkPos.toLong(chunkPos.x + var6, chunkPos.z + var7);
               this.arrivedAt.put(var8, var4);
               this.seedQueue.addLast(var8);
            }
         }
      }
   }

   private void enqueueLight(int i, int j, long l, LightData lightData, int k, boolean bl) {
      int var8 = ChunkSectionPos.getSectionCoord(-64);
      int var9 = ChunkSectionPos.getSectionCoord(128);
      int[] var10 = new int[]{0};
      int var11 = this.lightEpoch.getOrDefault(l, 0);
      this.light.collectSections(i, j, lightData, k, var8, var9, (jx, kx, m, bs) -> {
         this.lightJobs.addLast(new SusChunkFinderModule.LightJob(jx, kx, m, l, bs, bl, var11));
         var10[0]++;
      });
      if (var10[0] == 0) {
         this.indexing.remove(l);
         if (!bl && !this.scanned.containsKey(l) && this.queued.add(l)) {
            this.queue.addFirst(l);
         }
      } else {
         this.indexing.add(l);
         this.lightJobCount.merge(l, var10[0], Integer::sum);
      }
   }

   private void cancelLightJobs(long l) {
      if (this.indexing.contains(l) || this.lightJobCount.containsKey(l)) {
         this.lightEpoch.merge(l, 1, Integer::sum);
         this.lightJobCount.remove(l);
         this.indexing.remove(l);
      }
   }

   private void drainLight(MinecraftClient minecraftClient, ClientWorld clientWorld, long l) {
      while (!this.lightJobs.isEmpty() && System.nanoTime() < l) {
         SusChunkFinderModule.LightJob var5 = this.lightJobs.pollFirst();
         if (var5.epoch != this.lightEpoch.getOrDefault(var5.chunkKey, 0)) {
            this.light.releaseBuffer(var5.nibbles);
         } else {
            a var6 = null;
            if (var5.listen) {
               long var7 = var5.chunkKey;
               var6 = (i, j, k, m, n) -> this.recordChange(minecraftClient, clientWorld, var7, i, j, k, m, n);
            }

            if (this.light.applySection(var5.cx, var5.sy, var5.cz, var5.nibbles, var6) && var5.listen) {
               this.lightRescans.add(var5.chunkKey);
            }

            this.light.releaseBuffer(var5.nibbles);
            int var9 = this.lightJobCount.merge(var5.chunkKey, -1, Integer::sum);
            if (var9 <= 0) {
               this.lightJobCount.remove(var5.chunkKey);
               this.indexing.remove(var5.chunkKey);
               if (!this.scanned.containsKey(var5.chunkKey) && this.queued.add(var5.chunkKey)) {
                  this.queue.addFirst(var5.chunkKey);
               }
            }
         }
      }
   }

   private void drainSeed(ClientWorld clientWorld, long l) {
      while (!this.seedQueue.isEmpty() && System.nanoTime() < l) {
         long var4 = this.seedQueue.pollFirst();
         if (!this.indexing.contains(var4) && !this.scanned.containsKey(var4)) {
            int var6 = ChunkPos.getPackedX(var4);
            int var7 = ChunkPos.getPackedZ(var4);
            if (clientWorld.getChunkManager().isChunkLoaded(var6, var7)) {
               this.light.seed(clientWorld, var6, var7, -64, 128);
               this.dirtyChunks.add(var4);
               if (this.queued.add(var4)) {
                  this.queue.addFirst(var4);
               }
            }
         }
      }
   }

   private void refillQueue(ClientWorld clientWorld, ChunkPos chunkPos, int i) {
      boolean var4 = this.lastQueueCenter == null
         || Math.max(Math.abs(chunkPos.x - this.lastQueueCenter.x), Math.abs(chunkPos.z - this.lastQueueCenter.z)) >= 3;
      if (var4 || this.queue.isEmpty() && ++this.idleTicks >= 100) {
         this.idleTicks = 0;
         this.chunksChanged = false;
         this.lastQueueCenter = chunkPos;
         if (var4) {
            this.queue.clear();
            this.queued.clear();
         }

         ArrayList<long[]> var5 = new ArrayList<>();

         for (int var6 = -i; var6 <= i; var6++) {
            for (int var7 = -i; var7 <= i; var7++) {
               long var8 = ChunkPos.toLong(chunkPos.x + var6, chunkPos.z + var7);
               if (!this.scanned.containsKey(var8)
                  && !this.queued.contains(var8)
                  && !this.indexing.contains(var8)
                  && clientWorld.getChunkManager().isChunkLoaded(chunkPos.x + var6, chunkPos.z + var7)) {
                  var5.add(new long[]{var8, (long)var6 * var6 + (long)var7 * var7});
               }
            }
         }

         var5.sort((ls, ms) -> Long.compare(ls[1], ms[1]));

         for (long[] var11 : (Iterable<long[]>) var5) {
            if (this.queued.add(var11[0])) {
               this.queue.addLast(var11[0]);
            }
         }
      }
   }

   private int rescanLitChunks(ClientWorld clientWorld, int i, long l) {
      int var5 = 0;
      Iterator var6 = this.lightRescans.iterator();

      while (var6.hasNext() && var5 < i && System.nanoTime() < l) {
         long var7 = (Long)var6.next();
         var6.remove();
         if (!this.pinnedByChunk.containsKey(var7)) {
            WorldChunk var9 = loadedChunk(clientWorld, var7);
            if (var9 != null && this.scanned.containsKey(var7)) {
               List var10 = this.scan(clientWorld, var9);
               List var11 = this.scanned.put(var7, var10);
               var5++;
               if (var11 == null || !var11.equals(var10)) {
                  this.dirtyChunks.add(var7);
                  this.dirty = true;
               }
            }
         }
      }

      return var5;
   }

   private int scanQueue(ClientWorld clientWorld, int i, long l) {
      int var5 = 0;
      int var6 = 0;

      while (var5 < i && var6 < 128 && !this.queue.isEmpty() && System.nanoTime() < l) {
         var6++;
         long var7 = this.queue.pollFirst();
         this.queued.remove(var7);
         if (!this.scanned.containsKey(var7) && !this.indexing.contains(var7)) {
            WorldChunk var9 = loadedChunk(clientWorld, var7);
            if (var9 != null) {
               this.scanned.put(var7, this.scan(clientWorld, var9));
               this.dirtyChunks.add(var7);
               var5++;
               this.dirty = true;
            } else {
               this.deferred.addLast(var7);
            }
         }
      }

      return var5;
   }

   private void promoteDeferred(ClientWorld clientWorld) {
      int var2 = Math.min(this.deferred.size(), 32);

      for (int var3 = 0; var3 < var2; var3++) {
         long var4 = this.deferred.pollFirst();
         if (!this.scanned.containsKey(var4) && !this.indexing.contains(var4)) {
            if (loadedChunk(clientWorld, var4) == null) {
               this.deferred.addLast(var4);
            } else if (this.queued.add(var4)) {
               this.queue.addFirst(var4);
            }
         }
      }
   }

   private List<net.agartha.client.util.y.b> scan(ClientWorld clientWorld, WorldChunk worldChunk) {
      ChunkPos var3 = worldChunk.getPos();
      List var4 = this.light.emitters(var3.x, var3.z, -64, 128);
      return var4.size() > 1024 ? List.of() : y.buds(clientWorld, var4);
   }

   private static WorldChunk loadedChunk(ClientWorld clientWorld, long l) {
      int var3 = ChunkPos.getPackedX(l);
      int var4 = ChunkPos.getPackedZ(l);
      return clientWorld.getChunkManager().isChunkLoaded(var3, var4) ? clientWorld.getChunkManager().getWorldChunk(var3, var4) : null;
   }

   private void regroupDirty() {
      HashSet var1 = new HashSet();

      for (long var3 : this.dirtyChunks) {
         int var5 = ChunkPos.getPackedX(var3);
         int var6 = ChunkPos.getPackedZ(var3);

         for (int var7 = -1; var7 <= 1; var7++) {
            for (int var8 = -1; var8 <= 1; var8++) {
               var1.add(ChunkPos.toLong(var5 + var7, var6 + var8));
            }
         }
      }

      this.dirtyChunks.clear();
      boolean var9 = true;

      while (var9) {
         var9 = false;

         for (e var4 : this.cachedGeodes) {
            if (touches(var4, var1)) {
               for (long var15 : var4.chunks()) {
                  if (var1.add(var15)) {
                     var9 = true;
                  }
               }
            }
         }
      }

      ArrayList var11 = new ArrayList();

      for (long var14 : (Iterable<Long>) var1) {
         List var16 = this.scanned.get(var14);
         if (var16 != null && !var16.isEmpty()) {
            var11.addAll(var16);
         }
      }

      this.pendingCollect = var1;
      this.pendingEpoch = this.groupEpoch;
      this.pendingGroups = CompletableFuture.supplyAsync(() -> y.geodes(var11), GROUPER);
   }

   private boolean applyPendingGroups() {
      if (this.pendingGroups == null) {
         return true;
      } else if (!this.pendingGroups.isDone()) {
         return false;
      } else {
         List var1;
         try {
            var1 = this.pendingGroups.join();
         } catch (RuntimeException var3) {
            var1 = null;
         }

         Set var2 = this.pendingCollect;
         this.pendingGroups = null;
         this.pendingCollect = null;
         if (var1 != null && this.pendingEpoch == this.groupEpoch) {
            this.cachedGeodes.removeIf(arg -> touches(arg, var2));
            this.cachedGeodes.addAll(var1);
         }

         return true;
      }
   }

   private static boolean touches(e arg, Set<Long> set) {
      for (long var3 : arg.chunks()) {
         if (set.contains(var3)) {
            return true;
         }
      }

      return false;
   }

   private void rebuild(MinecraftClient minecraftClient, ClientWorld clientWorld) {
      this.scanned.keySet().removeIf(long_ -> {
         if (loadedChunk(clientWorld, long_) != null) {
            return false;
         } else {
            this.dirtyChunks.add(long_);
            return true;
         }
      });
      if (minecraftClient.player != null) {
         int var3 = Math.min(minecraftClient.options.getClampedViewDistance() + 1, 16);
         this.dropPinsLeftBehind(clientWorld, minecraftClient.player.getChunkPos(), var3);
      }

      if (!this.applyPendingGroups()) {
         this.dirty = true;
      } else {
         if (!this.dirtyChunks.isEmpty()) {
            this.regroupDirty();
            this.dirty = true;
         }

         List var11 = this.cachedGeodes;
         int var4 = this.sSensitivity.getInt();
         int var5 = y.minClusters(var4);
         int var6 = y.minYoungBuds(var4);
         int var7 = y.minEvents(var4);
         if (DEBUG) {
            for (e var9 : (Iterable<e>) var11) {
               net.agartha.client.util.y.f var10 = y.tierOf(var9, var5, var6, true);
               AgarthaClientMod.LOGGER
                  .info(
                     "[sus] geode at {},{}: small {} medium {} large {} cluster {} -> {}",
                     new Object[]{
                        (int)var9.centerX(), (int)var9.centerZ(), var9.small(), var9.medium(), var9.large(), var9.clusters(), var10 == null ? "natural" : var10
                     }
                  );
            }
         }

         Map var12 = y.flags(var11, this.activity(), var5, var6, true, var7);
         this.zones = this.lockFoundZones(this.filterZones(y.zones(var12, 3), clientWorld));
      }
   }

   private List<g> lockFoundZones(List<g> list) {
      HashSet var2 = new HashSet();
      ArrayList var3 = new ArrayList(list.size());

      for (g var5 : list) {
         if (var2.add(var5.id())) {
            for (g var7 : this.overlappingPins(var5)) {
               this.adoptFade(var7.id(), var5.id());
               this.pinnedByChunk.entrySet().removeIf(entry -> entry.getValue() == var7);
            }

            var3.add(var5);

            for (long var14 : var5.members()) {
               this.pinnedByChunk.put(var14, var5);
               this.pinHiddenAt.remove(var14);
            }
         }
      }

      for (g var11 : new HashSet<>(this.pinnedByChunk.values())) {
         if (var2.add(var11.id())) {
            boolean var13 = false;

            for (long var8 : var11.members()) {
               if (this.pinnedByChunk.get(var8) == var11) {
                  var13 = true;
                  break;
               }
            }

            if (var13) {
               var3.add(var11);
            }
         }
      }

      return var3;
   }

   private static boolean overlaps(g arg, g arg2) {
      if (arg.id() == arg2.id()) {
         return true;
      } else {
         for (long var3 : arg.members()) {
            int var5 = y.chunkX(var3);
            int var6 = y.chunkZ(var3);

            for (long var8 : arg2.members()) {
               int var10 = Math.abs(var5 - y.chunkX(var8));
               int var11 = Math.abs(var6 - y.chunkZ(var8));
               if (Math.max(var10, var11) <= 3) {
                  return true;
               }
            }
         }

         double var12 = arg.centroidX() - arg2.centroidX();
         double var4 = arg.centroidZ() - arg2.centroidZ();
         return var12 * var12 + var4 * var4 <= 576.0;
      }
   }

   private List<g> overlappingPins(g arg) {
      ArrayList var2 = new ArrayList();
      HashSet var3 = new HashSet();

      for (g var5 : this.pinnedByChunk.values()) {
         if (var3.add(var5) && overlaps(var5, arg)) {
            var2.add(var5);
         }
      }

      return var2;
   }

   private void adoptFade(long l, long m) {
      if (l != m) {
         double[] var5 = this.zoneFades.remove(l);
         if (var5 != null) {
            this.zoneFades.putIfAbsent(m, var5);
         }
      }
   }

   private void dropPinsLeftBehind(ClientWorld clientWorld, ChunkPos chunkPos, int i) {
      long var4 = System.currentTimeMillis();
      long var6 = chunkPos.toLong();
      ArrayList var8 = new ArrayList();

      for (long var10 : this.pinnedByChunk.keySet()) {
         if (loadedChunk(clientWorld, var10) != null) {
            this.pinHiddenAt.remove(var10);
         } else if (y.chunkDistance(var10, var6) > i) {
            var8.add(var10);
         } else {
            long var12 = this.pinHiddenAt.computeIfAbsent(var10, long_ -> var4);
            if (var4 - var12 > 20000L) {
               var8.add(var10);
            }
         }
      }

      for (long var15 : (Iterable<Long>) var8) {
         this.pinnedByChunk.remove(var15);
         this.pinHiddenAt.remove(var15);
      }
   }

   private Map<Long, net.agartha.client.util.y.a> activity() {
      long var1 = System.currentTimeMillis() - 600000L;
      HashMap var3 = new HashMap();
      Iterator var4 = this.events.entrySet().iterator();

      while (var4.hasNext()) {
         Entry var5 = (Entry)var4.next();
         ArrayDeque var6 = (ArrayDeque)var5.getValue();

         while (!var6.isEmpty() && ((double[])var6.peekFirst())[0] < var1) {
            var6.pollFirst();
         }

         if (var6.isEmpty()) {
            var4.remove();
         } else {
            double var7 = 0.0;
            double var9 = 0.0;

            for (double[] var12 : (Iterable<double[]>) var6) {
               var7 += var12[1];
               var9 += var12[2];
            }

            var3.put((Long)var5.getKey(), new net.agartha.client.util.y.a(var6.size(), var7, var9));
         }
      }

      return var3;
   }

   @net.agartha.client.d.f
   public void onRender3D(o arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null) {
         long var3 = System.nanoTime();
         float var5 = this.lastFrameNanos == 0L ? 0.016F : Math.min((float)(var3 - this.lastFrameNanos) / 1.0E9F, 0.1F);
         this.lastFrameNanos = var3;
         double var6 = 1.0 - Math.exp(-7.0F * var5);
         int var8 = this.zoneRgb();
         Set var9 = this.liveScratch;
         var9.clear();

         for (g var11 : this.zones) {
            var9.add(var11.id());
            double var12 = Math.min(48.0, 16.0 + (var11.members().size() - 1) * 8.0);
            double var14 = Math.floor(var11.centroidX()) + 0.5;
            double var16 = Math.floor(var11.centroidZ()) + 0.5;
            double[] var18 = this.zoneFades.computeIfAbsent(var11.id(), long_ -> new double[]{var14, var16, var12, 0.0});
            double var19 = var18[2] / 2.0;
            if (Math.abs(var14 - var18[0]) > var19 || Math.abs(var16 - var18[1]) > var19) {
               var18[0] = var14;
               var18[1] = var16;
            }

            var18[2] += (var12 - var18[2]) * var6;
         }

         double var23 = this.sPlateY.get();
         Iterator var24 = this.zoneFades.entrySet().iterator();

         while (var24.hasNext()) {
            Entry var13 = (Entry)var24.next();
            double[] var25 = (double[])var13.getValue();
            boolean var15 = var9.contains(var13.getKey());
            var25[3] = step((float)var25[3], var15, var5);
            if (!var15 && var25[3] <= 0.0) {
               var24.remove();
            } else {
               float var26 = net.agartha.client.util.a_o.ease((float)var25[3]);
               double var17 = var25[2] / 2.0;
               double var27 = net.agartha.client.util.o.y(
                     1, (Long)var13.getKey(), var25[0] - var17, var23, var25[1] - var17, var25[0] + var17, var25[1] + var17, 0.35
                  )
                  + net.agartha.client.util.a_o.rise(var26);
               double var21 = var17 * net.agartha.client.util.a_o.pop(var26);
               this.draw(new Box(var25[0] - var21, var27, var25[1] - var21, var25[0] + var21, var27 + 0.35, var25[1] + var21), var8, var26);
            }
         }
      }
   }

   private int zoneRgb() {
      return ac.argb(this.sThemeColor, this.sCustomColor, this.sColor, -1) & 16777215;
   }

   public boolean isTracerOverlayEnabled() {
      return this.isEnabled() && this.sTracers.get() && !this.zoneFades.isEmpty();
   }

   public void renderTracerOverlay(ImDrawList imDrawList, float f, float g) {
      if (imDrawList != null && this.isTracerOverlayEnabled()) {
         MinecraftClient var4 = MinecraftClient.getInstance();
         if (var4.world != null && var4.player != null) {
            float var5 = ad.originX(f);
            float var6 = ad.originY("Crosshair", g);
            float var7 = 1.5F;
            int var8 = this.zoneRgb();

            for (Entry var10 : this.zoneFades.entrySet()) {
               double[] var11 = (double[])var10.getValue();
               float var12 = (float)var11[3];
               if (!(var12 <= 0.01F)) {
                  double var13 = var11[2] / 2.0;
                  double var15 = net.agartha.client.util.o.y(
                        1, (Long)var10.getKey(), var11[0] - var13, this.sPlateY.get(), var11[1] - var13, var11[0] + var13, var11[1] + var13, 0.35
                     )
                     + net.agartha.client.util.a_o.rise(net.agartha.client.util.a_o.ease(var12));
                  ad.draw(imDrawList, var4, f, g, var5, var6, new Vec3d(var11[0], var15 + 0.175, var11[1]), argb(255.0F * var12, var8), var7);
               }
            }
         }
      }
   }

   private static float step(float f, boolean bl, float g) {
      float var3 = f + (bl ? g / 0.7F : -g / 0.5F);
      return Math.max(0.0F, Math.min(1.0F, var3));
   }

   private void draw(Box box, int i, float f) {
      if (!(f <= 0.0F) && net.agartha.client.util.l.isSafeCuboid(box)) {
         w.plate(1, box, argb(200.0F * f, i), 1.5F, argb(90.0F * f, i));
      }
   }

   private static int argb(float f, int i) {
      int var2 = Math.max(0, Math.min(255, Math.round(f)));
      return var2 << 24 | i & 16777215;
   }

   @Environment(EnvType.CLIENT)
   private static final class LightJob {
      private final int cx;
      private final int sy;
      private final int cz;
      private final long chunkKey;
      private final byte[] nibbles;
      private final boolean listen;
      private final int epoch;

      private LightJob(int i, int j, int k, long l, byte[] bs, boolean bl, int m) {
         this.cx = i;
         this.sy = j;
         this.cz = k;
         this.chunkKey = l;
         this.nibbles = bs;
         this.listen = bl;
         this.epoch = m;
      }
   }
}
