package net.agartha.client.module.impl.render;

import imgui.ImDrawList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.BlockUpdateEvent;
import net.agartha.client.event.impl.EndClientTickEvent;
import net.agartha.client.event.impl.PacketReceiveEvent;
import net.agartha.client.event.impl.Render3DEvent;
import net.agartha.client.event.impl.ScanFrameEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.TracerOverlay;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ColorSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.util.color.ColorSettingWiring;
import net.agartha.client.util.perf.TimeSlice;
import net.agartha.client.util.render.FadeTracker;
import net.agartha.client.util.render.FrustumCulling;
import net.agartha.client.util.render.LabelStacker;
import net.agartha.client.util.render.OffscreenIndicator;
import net.agartha.client.util.render.PlateRenderer;
import net.agartha.client.util.world.DiggerColumns;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

@Environment(EnvType.CLIENT)
public final class DiggerEspModule extends Module implements TracerOverlay {
   private static final int MIN_CHUNK = 4;
   private static final int MAX_CHUNK = 10;
   private static final long SLICE_NS = 1500000L;
   private static final int MARK_MIN = 3;
   private static final int MARK_MAX = 8;
   private static final String LOW = "Low";
   private static final String HIGH = "High";
   private static final String ULTRA_HIGH = "Ultra High";
   private static final int CLEARANCE_LOW = 16;
   private static final int CLEARANCE_HIGH = 32;
   private static final int CLEARANCE_ULTRA_HIGH = 48;
   private static final int ICE = -8397825;
   private static final float LINE_WIDTH = 1.5F;
   private static final double PLATE_HEIGHT = 0.35;
   private static final double PLATE_HALF = 4.0;
   private static final int SAME_FIND = 2;
   private final Map<Long, List<DiggerEspModule.Column>> found = new HashMap<>();
   private final TimeSlice slice = new TimeSlice();
   private final Set<Long> done = new HashSet<>();
   private final ArrayDeque<Long> queue = new ArrayDeque<>();
   private final Set<Long> queued = new HashSet<>();
   private final boolean[] pierce = new boolean[384];
   private final boolean[] air = new boolean[384];
   private final boolean[] stoneAt = new boolean[384];
   private final boolean[] sectionStone = new boolean[32];
   private final boolean[] sectionWall = new boolean[32];
   private final Mutable cursorPos = new Mutable();
   private long cursorKey = Long.MIN_VALUE;
   private int refillX = Integer.MIN_VALUE;
   private int refillZ = Integer.MIN_VALUE;
   private int cursor;
   private final List<DiggerEspModule.Column> building = new ArrayList<>();
   private final ArrayDeque<DiggerEspModule.Column> reviews = new ArrayDeque<>();
   private final ArrayDeque<DiggerEspModule.Column> deferred = new ArrayDeque<>();
   private boolean reviewDeferred;
   private final Map<Long, DiggerEspModule.TunnelChunk> tunnelChunks = new HashMap<>();
   private DiggerEspModule.Column reviewing;
   private boolean reviewHit;
   private ClientWorld lastLevel;
   private final Set<Long> nearSeed = new HashSet<>();
   private final EnumSetting sSensitivity = (EnumSetting)this.addSetting(new EnumSetting("Sensitivity", "Low", new String[]{"Low", "High", "Ultra High"}));
   private final BooleanSetting sCustomColor = (BooleanSetting)this.addSetting(
      new BooleanSetting("Custom Color", true)
   );
   private final ColorSetting sColor = (ColorSetting)this.addSetting(new ColorSetting("Color", -8397825));
   private final BooleanSetting sThemeColor = (BooleanSetting)this.addSetting(
      new BooleanSetting("Theme Color", false)
   );
   private final NumberSetting sPlateY = (NumberSetting)this.addSetting(new NumberSetting("Plate Y", 60.0F, -64.0F, 320.0F).whole());
   private final BooleanSetting sTracers = (BooleanSetting)this.addSetting(new BooleanSetting("Tracers", false));
   private final FadeTracker fade = new FadeTracker(0.7F, 0.5F);
   private final Long2ObjectOpenHashMap<DiggerEspModule.Column> lastColumns = new Long2ObjectOpenHashMap();
   private final FadeTracker.a fadingPlate = this::drawPlate;
   private int frameColor;
   private int frameFill;
   private int pruneFrames;
   private ImDrawList tracerList;
   private MinecraftClient tracerMc;
   private float tracerW;
   private float tracerH;
   private float tracerOriginX;
   private float tracerOriginY;
   private int tracerColor;
   private final FadeTracker.a plateTracer = this::tracerTo;

   public DiggerEspModule() {
      super(
         "Agartha Finder",
         "Shows up less often than Sus Chunk Finder, but the marks are more reliable. Dig under the plate.",
         ModuleCategory.DONUT
      );
      ColorSettingWiring.wireExclusiveColorSource(this.sCustomColor, this.sThemeColor, this.sColor, false);
      this.sSensitivity.onChange(this::clear);
   }

   protected void onEnable() {
      this.clear();
   }

   protected void onDisable() {
      this.clear();
   }

   private void clear() {
      this.found.clear();
      this.lastColumns.clear();
      this.fade.clear();
      this.done.clear();
      this.queue.clear();
      this.queued.clear();
      this.building.clear();
      this.reviews.clear();
      this.deferred.clear();
      this.tunnelChunks.clear();
      this.nearSeed.clear();
      this.reviewing = null;
      this.cursorKey = Long.MIN_VALUE;
      this.cursor = 0;
      this.refillX = Integer.MIN_VALUE;
      this.refillZ = Integer.MIN_VALUE;
      this.lastLevel = null;
   }

   @EventListener
   public void onPacketReceive(PacketReceiveEvent arg) {
      if (arg.packet instanceof ChunkDataS2CPacket var2) {
         this.enqueue(ChunkPos.toLong(var2.getChunkX(), var2.getChunkZ()));
         if (!this.deferred.isEmpty()) {
            this.reviews.addAll(this.deferred);
            this.deferred.clear();
         }
      } else if (arg.packet instanceof UnloadChunkS2CPacket var3) {
         long var8 = var3.pos().toLong();
         MinecraftClient var6 = MinecraftClient.getInstance();
         if (var6.player == null || chunkDist(var8, var6.player.getChunkPos()) > var6.options.getClampedViewDistance()) {
            this.drop(var8);
         }
      }
   }

   @EventListener
   public void onBlockUpdate(BlockUpdateEvent arg) {
      if (arg.pos != null) {
         if (affectsScan(arg.oldState, arg.newState)) {
            this.enqueue(ChunkPos.toLong(arg.pos.getX() >> 4, arg.pos.getZ() >> 4));
         }
      }
   }

   private static boolean affectsScan(BlockState blockState, BlockState blockState2) {
      if (blockState != null && blockState2 != null) {
         return blockState.isAir() != blockState2.isAir() ? true : scanBlock(blockState) || scanBlock(blockState2);
      } else {
         return true;
      }
   }

   private static boolean scanBlock(BlockState blockState) {
      return isStone(blockState) || isWallState(blockState);
   }

   @EventListener
   public void onEndClientTick(EndClientTickEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientWorld var3 = var2.world;
      if (var3 != null && var2.player != null && var3.getRegistryKey() == World.OVERWORLD) {
         ChunkPos var4 = var2.player.getChunkPos();
         int var5 = var2.options.getClampedViewDistance();
         if (var3 != this.lastLevel) {
            this.clear();
            this.lastLevel = var3;
            this.seedLoaded(var3, var4, Math.min(var5, 10));
         }

         this.prune(var4, var5);
         this.refill(var3, var4);
      }
   }

   @EventListener
   public void onScanFrame(ScanFrameEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientWorld var3 = var2.world;
      if (var3 != null && var2.player != null && var3 == this.lastLevel && var3.getRegistryKey() == World.OVERWORLD) {
         if (this.slice.begin(1500000L, Integer.MAX_VALUE)) {
            try {
               this.scanSlice(var3, var2.player.getChunkPos(), this.slice.deadline());
            } finally {
               this.slice.end();
            }
         }
      }
   }

   private void scanSlice(ClientWorld clientWorld, ChunkPos chunkPos, long l) {
      this.drainReviews(clientWorld, l);

      while (System.nanoTime() < l) {
         if (this.cursorKey == Long.MIN_VALUE) {
            if (this.queue.isEmpty()) {
               return;
            }

            long var5 = this.queue.pollFirst();
            if (!this.queued.remove(var5)) {
               continue;
            }

            if (!inScanRing(var5, chunkPos) && !this.nearSeed.contains(var5) || !loaded(clientWorld, var5)) {
               this.nearSeed.remove(var5);
               continue;
            }

            this.cursorKey = var5;
            this.cursor = 0;
            this.building.clear();
         }

         if (!loaded(clientWorld, this.cursorKey)) {
            this.drop(this.cursorKey);
            this.cursorKey = Long.MIN_VALUE;
         } else {
            WorldChunk var7 = clientWorld.getChunkManager().getWorldChunk(ChunkPos.getPackedX(this.cursorKey), ChunkPos.getPackedZ(this.cursorKey));
            if (var7 == null) {
               this.cursorKey = Long.MIN_VALUE;
            } else {
               if (!this.advance(clientWorld, var7, l)) {
                  return;
               }

               this.reviews.addAll(this.building);
               this.done.add(this.cursorKey);
               this.nearSeed.remove(this.cursorKey);
               this.cursorKey = Long.MIN_VALUE;
            }
         }
      }
   }

   @EventListener
   public void onRender3D(Render3DEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null && (!this.found.isEmpty() || !this.lastColumns.isEmpty())) {
         this.frameColor = this.color();
         this.frameFill = this.frameColor & 16777215 | 1073741824;
         this.fade.begin();
         ChunkPos var3 = var2.player.getChunkPos();
         int var4 = var2.options.getClampedViewDistance();

         for (Entry var6 : this.found.entrySet()) {
            long var7 = (Long)var6.getKey();
            if (chunkDist(var7, var3) <= var4) {
               boolean var9 = !FrustumCulling.columnOutOfView(ChunkPos.getPackedX(var7), ChunkPos.getPackedZ(var7), -64, 320);

               for (DiggerEspModule.Column var11 : (Iterable<DiggerEspModule.Column>) (List)var6.getValue()) {
                  long var12 = columnKey(var11);
                  if (this.lastColumns.get(var12) != var11) {
                     this.lastColumns.put(var12, var11);
                  }

                  float var14 = this.fade.show(var12);
                  if (var9) {
                     this.drawPlate(var12, var14);
                  }
               }
            }
         }

         this.fade.sweep(this.fadingPlate);
         if (++this.pruneFrames >= 60) {
            this.pruneFrames = 0;
            this.lastColumns.keySet().removeIf(l -> !this.fade.contains(l));
         }
      }
   }

   private void drawPlate(long l, float f) {
      if (!(f <= 0.01F)) {
         DiggerEspModule.Column var4 = (DiggerEspModule.Column)this.lastColumns.get(l);
         if (var4 != null) {
            int var5 = f >= 0.999F ? this.frameColor : scaleAlpha(this.frameColor, f);
            int var6 = f >= 0.999F ? this.frameFill : scaleAlpha(this.frameFill, f);
            double var7 = FadeTracker.pop(f);
            double var9 = FadeTracker.rise(f);
            Box var11 = this.plate(var4);
            if (var7 < 0.999) {
               var11 = var11.expand(var11.getLengthX() * (var7 - 1.0) * 0.5, 0.0, var11.getLengthZ() * (var7 - 1.0) * 0.5);
            }

            if (var9 > 0.001) {
               var11 = var11.offset(0.0, var9, 0.0);
            }

            if (FrustumCulling.isSafeCuboid(var11)) {
               PlateRenderer.plate(4, var11, var5, 1.5F, var6);
            }
         }
      }
   }

   private static int scaleAlpha(int i, float f) {
      int var2 = Math.round((i >>> 24 & 0xFF) * f);
      return var2 << 24 | i & 16777215;
   }

   public boolean isTracerOverlayEnabled() {
      return this.isEnabled() && this.sTracers.get() && !this.fade.isEmpty();
   }

   public void renderTracerOverlay(ImDrawList imDrawList, float f, float g) {
      if (this.isTracerOverlayEnabled() && imDrawList != null) {
         MinecraftClient var4 = MinecraftClient.getInstance();
         if (var4.world != null && var4.player != null) {
            this.tracerList = imDrawList;
            this.tracerMc = var4;
            this.tracerW = f;
            this.tracerH = g;
            this.tracerOriginX = OffscreenIndicator.originX(f);
            this.tracerOriginY = OffscreenIndicator.originY("Crosshair", g);
            this.tracerColor = this.color();
            this.fade.forEachVisible(this.plateTracer);
            this.tracerList = null;
         }
      }
   }

   private void tracerTo(long l, float f) {
      DiggerEspModule.Column var4 = (DiggerEspModule.Column)this.lastColumns.get(l);
      if (var4 != null) {
         double var5 = var4.x + 0.5;
         double var7 = var4.z + 0.5;
         double var9 = LabelStacker.y(4, l, var5 - 4.0, this.sPlateY.get(), var7 - 4.0, var5 + 4.0, var7 + 4.0, 0.35) + FadeTracker.rise(f);
         OffscreenIndicator.draw(
            this.tracerList,
            this.tracerMc,
            this.tracerW,
            this.tracerH,
            this.tracerOriginX,
            this.tracerOriginY,
            new Vec3d(var5, var9 + 0.175, var7),
            scaleAlpha(this.tracerColor, f),
            1.5F
         );
      }
   }

   private int color() {
      return ColorSettingWiring.argb(this.sThemeColor, this.sCustomColor, this.sColor, -8397825);
   }

   private static long columnKey(DiggerEspModule.Column column) {
      return (long)column.x << 32 ^ column.z & 4294967295L;
   }

   private Box plate(DiggerEspModule.Column column) {
      double var2 = column.x + 0.5;
      double var4 = column.z + 0.5;
      double var6 = LabelStacker.y(4, columnKey(column), var2 - 4.0, this.sPlateY.get(), var4 - 4.0, var2 + 4.0, var4 + 4.0, 0.35);
      return new Box(var2 - 4.0, var6, var4 - 4.0, var2 + 4.0, var6 + 0.35, var4 + 4.0);
   }

   private void refill(ClientWorld clientWorld, ChunkPos chunkPos) {
      if (this.queue.isEmpty() && this.cursorKey == Long.MIN_VALUE) {
         if (chunkPos.x != this.refillX || chunkPos.z != this.refillZ) {
            this.refillX = chunkPos.x;
            this.refillZ = chunkPos.z;

            for (int var3 = -10; var3 <= 10; var3++) {
               for (int var4 = -10; var4 <= 10; var4++) {
                  int var5 = Math.max(Math.abs(var3), Math.abs(var4));
                  if (var5 >= 4 && var5 <= 10) {
                     long var6 = ChunkPos.toLong(chunkPos.x + var3, chunkPos.z + var4);
                     if (!this.found.containsKey(var6)
                        && !this.done.contains(var6)
                        && !this.queued.contains(var6)
                        && clientWorld.getChunkManager().isChunkLoaded(chunkPos.x + var3, chunkPos.z + var4)) {
                        this.enqueue(var6);
                     }
                  }
               }
            }
         }
      }
   }

   private void enqueue(long l) {
      this.tunnelChunks.remove(l);
      if (!this.found.containsKey(l)) {
         this.forgetPending(l);
         this.done.remove(l);
         if (this.cursorKey == l) {
            this.cursorKey = Long.MIN_VALUE;
            this.building.clear();
         }

         if (this.queued.add(l)) {
            this.queue.addFirst(l);
         }
      }
   }

   private void drop(long l) {
      this.tunnelChunks.remove(l);
      this.forgetPending(l);
      this.found.remove(l);
      this.done.remove(l);
      this.queued.remove(l);
      this.nearSeed.remove(l);
      if (this.cursorKey == l) {
         this.cursorKey = Long.MIN_VALUE;
         this.building.clear();
      }
   }

   private void prune(ChunkPos chunkPos, int i) {
      this.found.keySet().removeIf(long_ -> {
         if (chunkDist(long_, chunkPos) <= i) {
            return false;
         } else {
            this.done.remove(long_);
            return true;
         }
      });
      this.reviews.removeIf(column -> chunkDist(chunkOf(column), chunkPos) > i);
      this.deferred.removeIf(column -> chunkDist(chunkOf(column), chunkPos) > i);
      if (this.reviewing != null && chunkDist(chunkOf(this.reviewing), chunkPos) > i) {
         this.reviewing = null;
         this.reviewHit = false;
      }

      int var3 = i + 1;
      this.tunnelChunks.keySet().removeIf(long_ -> chunkDist(long_, chunkPos) > var3);
   }

   private void forgetPending(long l) {
      this.reviews.removeIf(column -> chunkOf(column) == l);
      this.deferred.removeIf(column -> chunkOf(column) == l);
      if (this.reviewing != null && chunkOf(this.reviewing) == l) {
         this.reviewing = null;
         this.reviewHit = false;
      }
   }

   private static long chunkOf(DiggerEspModule.Column column) {
      return ChunkPos.toLong(column.x >> 4, column.z >> 4);
   }

   private static boolean sameFind(DiggerEspModule.Column column, DiggerEspModule.Column column2) {
      return Math.max(Math.abs(column.x - column2.x), Math.abs(column.z - column2.z)) <= 2;
   }

   private boolean alreadyMarked(DiggerEspModule.Column column) {
      for (List var3 : this.found.values()) {
         for (int var4 = 0; var4 < var3.size(); var4++) {
            if (sameFind(column, (DiggerEspModule.Column)var3.get(var4))) {
               return true;
            }
         }
      }

      for (int var5 = 0; var5 < this.building.size(); var5++) {
         if (sameFind(column, this.building.get(var5))) {
            return true;
         }
      }

      for (DiggerEspModule.Column var7 : this.reviews) {
         if (sameFind(column, var7)) {
            return true;
         }
      }

      return this.reviewing != null && this.reviewing != column && sameFind(column, this.reviewing);
   }

   private void collapseTwins() {
      ArrayList var1 = new ArrayList();
      ArrayList var2 = new ArrayList();

      for (Entry var4 : this.found.entrySet()) {
         List var5 = (List)var4.getValue();
         ArrayList var6 = null;

         for (int var7 = 0; var7 < var5.size(); var7++) {
            DiggerEspModule.Column var8 = (DiggerEspModule.Column)var5.get(var7);
            boolean var9 = false;

            for (int var10 = 0; var10 < var1.size(); var10++) {
               if (sameFind(var8, (DiggerEspModule.Column)var1.get(var10))) {
                  var9 = true;
                  break;
               }
            }

            if (!var9) {
               var1.add(var8);
               if (var6 != null) {
                  var6.add(var8);
               }
            } else if (var6 == null) {
               var6 = new ArrayList(var5.subList(0, var7));
            }
         }

         if (var6 != null) {
            if (var6.isEmpty()) {
               var2.add((Long)var4.getKey());
            } else {
               var4.setValue(List.copyOf(var6));
            }
         }
      }

      for (int var11 = 0; var11 < var2.size(); var11++) {
         this.found.remove(var2.get(var11));
      }
   }

   private static boolean inScanRing(long l, ChunkPos chunkPos) {
      int var3 = chunkDist(l, chunkPos);
      return var3 >= 4 && var3 <= 10;
   }

   private void seedLoaded(ClientWorld clientWorld, ChunkPos chunkPos, int i) {
      for (int var4 = -i; var4 <= i; var4++) {
         for (int var5 = -i; var5 <= i; var5++) {
            int var6 = Math.max(Math.abs(var4), Math.abs(var5));
            if (var6 <= 10) {
               int var7 = chunkPos.x + var4;
               int var8 = chunkPos.z + var5;
               if (clientWorld.getChunkManager().isChunkLoaded(var7, var8)) {
                  long var9 = ChunkPos.toLong(var7, var8);
                  this.nearSeed.add(var9);
                  this.enqueue(var9);
               }
            }
         }
      }
   }

   private static int chunkDist(long l, ChunkPos chunkPos) {
      return Math.max(Math.abs(ChunkPos.getPackedX(l) - chunkPos.x), Math.abs(ChunkPos.getPackedZ(l) - chunkPos.z));
   }

   private static boolean loaded(ClientWorld clientWorld, long l) {
      return clientWorld.getChunkManager().isChunkLoaded(ChunkPos.getPackedX(l), ChunkPos.getPackedZ(l));
   }

   private boolean advance(ClientWorld clientWorld, WorldChunk worldChunk, long l) {
      ChunkPos var5 = worldChunk.getPos();
      int var6 = var5.getStartX();
      int var7 = var5.getStartZ();
      int var8 = clientWorld.getBottomY();
      if (this.cursor == 0) {
         this.markSections(worldChunk);
      }

      while (this.cursor < 256 && System.nanoTime() < l) {
         int var9 = this.cursor & 15;
         int var10 = this.cursor >> 4;
         int var11 = Math.min(worldChunk.sampleHeightmap(Type.WORLD_SURFACE, var9, var10), var8 + this.pierce.length);
         this.scanColumn(clientWorld, worldChunk, var6 + var9, var7 + var10, var8, var11);
         this.cursor++;
      }

      return this.cursor >= 256;
   }

   private void scanColumn(ClientWorld clientWorld, WorldChunk worldChunk, int i, int j, int k, int l) {
      int var7 = Math.max(0, l - k);
      if (var7 > this.pierce.length) {
         var7 = this.pierce.length;
      }

      ChunkSection[] var8 = worldChunk.getSectionArray();
      int var9 = worldChunk.getPos().x;
      int var10 = worldChunk.getPos().z;

      for (int var11 = 0; var11 < var7; var11++) {
         int var12 = k + var11;
         int var13 = var12 - k >> 4;
         this.pierce[var11] = false;
         this.air[var11] = false;
         this.stoneAt[var11] = false;
         if (var13 >= 0 && var13 < var8.length && this.sectionStone[var13]) {
            ChunkSection var14 = var8[var13];
            int var15 = i & 15;
            int var16 = j & 15;
            BlockState var17 = var14.getBlockState(var15, var12 & 15, var16);
            if (isStone(var17)) {
               this.stoneAt[var11] = true;
               if (var15 <= 0 || var15 >= 15 || var16 <= 0 || var16 >= 15 || this.sectionWall[var13]) {
                  BlockState var18 = this.blockAt(clientWorld, worldChunk, var9, var10, i, var12, j - 1);
                  BlockState var19 = this.blockAt(clientWorld, worldChunk, var9, var10, i, var12, j + 1);
                  BlockState var20 = this.blockAt(clientWorld, worldChunk, var9, var10, i + 1, var12, j);
                  BlockState var21 = this.blockAt(clientWorld, worldChunk, var9, var10, i - 1, var12, j);
                  this.pierce[var11] = DiggerColumns.piercing(false, var17.getBlock(), var18.getBlock(), var19.getBlock(), var20.getBlock(), var21.getBlock());
               }
            }
         }
      }

      int var22 = var7;
      DiggerColumns.collect(this.pierce, this.air, var7, 3, 8, (o, p) -> {
         if (this.cappedWithStone(clientWorld, worldChunk, var9, var10, i, j, k, var22, o, p)) {
            DiggerEspModule.Column var11x = new DiggerEspModule.Column(i, j, k + o, p);
            if (!this.alreadyMarked(var11x)) {
               this.building.add(var11x);
            }
         }
      });
   }

   private int clearance() {
      if (this.sSensitivity.is("Ultra High")) {
         return 48;
      } else {
         return this.sSensitivity.is("High") ? 32 : 16;
      }
   }

   private void drainReviews(ClientWorld clientWorld, long l) {
      int var4 = this.clearance();

      for (int var5 = var4 * var4; System.nanoTime() < l; this.reviewing = null) {
         if (this.reviewing == null) {
            if (this.reviews.isEmpty()) {
               return;
            }

            this.reviewing = this.reviews.poll();
            this.reviewHit = false;
         }

         if (!this.stepReview(clientWorld, this.reviewing, var4, var5, l)) {
            return;
         }

         if (this.reviewDeferred) {
            this.deferred.add(this.reviewing);
         } else if (!this.reviewHit) {
            this.accept(this.reviewing);
         }
      }
   }

   private boolean stepReview(ClientWorld clientWorld, DiggerEspModule.Column column, int i, int j, long l) {
      this.reviewDeferred = false;
      int var7 = column.x - i >> 4;
      int var8 = column.x + i >> 4;
      int var9 = column.z - i >> 4;
      int var10 = column.z + i >> 4;

      for (int var11 = var9; var11 <= var10; var11++) {
         for (int var12 = var7; var12 <= var8; var12++) {
            if (!clientWorld.getChunkManager().isChunkLoaded(var12, var11)) {
               this.reviewDeferred = true;
               return true;
            }
         }
      }

      for (int var15 = var9; var15 <= var10; var15++) {
         for (int var17 = var7; var17 <= var8; var17++) {
            DiggerEspModule.TunnelChunk var13 = this.chunkTunnels(clientWorld, var17, var15);
            if (!var13.ready && !this.indexTunnels(clientWorld, var13, var17, var15, l)) {
               return false;
            }
         }
      }

      for (int var16 = var9; var16 <= var10; var16++) {
         for (int var18 = var7; var18 <= var8; var18++) {
            DiggerEspModule.TunnelChunk var19 = this.tunnelChunks.get(ChunkPos.toLong(var18, var16));
            if (var19 != null) {
               for (int var14 = 0; var14 < var19.boxes.size(); var14++) {
                  if (nearColumn(column, var19.boxes.get(var14), j)) {
                     this.reviewHit = true;
                     return true;
                  }
               }
            }
         }
      }

      return true;
   }

   private static boolean nearColumn(DiggerEspModule.Column column, int[] is, int i) {
      int var3 = axisGap(column.x, column.x + 1, is[0], is[3]);
      int var4 = axisGap(column.y, column.y + column.length, is[1], is[4]);
      int var5 = axisGap(column.z, column.z + 1, is[2], is[5]);
      return var3 * var3 + var4 * var4 + var5 * var5 <= i;
   }

   private static int axisGap(int i, int j, int k, int l) {
      if (j <= k) {
         return k - (j - 1);
      } else {
         return l <= i ? i - (l - 1) : 0;
      }
   }

   private DiggerEspModule.TunnelChunk chunkTunnels(ClientWorld clientWorld, int i, int j) {
      long var4 = ChunkPos.toLong(i, j);
      DiggerEspModule.TunnelChunk var6 = this.tunnelChunks.get(var4);
      if (var6 != null) {
         return var6;
      } else {
         var6 = new DiggerEspModule.TunnelChunk();
         WorldChunk var7 = clientWorld.getChunkManager().getWorldChunk(i, j);
         if (var7 == null) {
            var6.ready = true;
            return var6;
         } else {
            ChunkSection[] var8 = var7.getSectionArray();
            int var9 = Math.min(var8.length, var6.air.length);

            for (int var10 = 0; var10 < var9; var10++) {
               ChunkSection var11 = var8[var10];
               var6.air[var10] = var11 != null && (var11.isEmpty() || var11.hasAny(AbstractBlockState::isAir));
               if (var6.air[var10]) {
                  var6.anyAir = true;
               }
            }

            if (!var6.anyAir) {
               var6.ready = true;
            }

            this.tunnelChunks.put(var4, var6);
            return var6;
         }
      }
   }

   private boolean indexTunnels(ClientWorld clientWorld, DiggerEspModule.TunnelChunk tunnelChunk, int i, int j, long l) {
      if (tunnelChunk.ready) {
         return true;
      } else {
         WorldChunk var7 = clientWorld.getChunkManager().getWorldChunk(i, j);
         if (var7 == null) {
            tunnelChunk.ready = true;
            return true;
         } else {
            int var8 = clientWorld.getBottomY();
            int var9 = i << 4;
            int var10 = j << 4;
            ChunkSection[] var11 = var7.getSectionArray();

            while (tunnelChunk.cursor < 256 && System.nanoTime() < l) {
               int var12 = tunnelChunk.cursor & 15;
               int var13 = tunnelChunk.cursor >> 4;
               int var14 = var9 + var12;
               int var15 = var10 + var13;
               if (!this.indexShaft(clientWorld, var7, tunnelChunk, var11, var8, var14, var15, var12, var13, l)) {
                  return false;
               }

               tunnelChunk.cursor++;
               tunnelChunk.si = 0;
               tunnelChunk.ly = 0;
               this.flushHole(tunnelChunk, var14, var15);
            }

            tunnelChunk.ready = tunnelChunk.cursor >= 256;
            return tunnelChunk.ready;
         }
      }
   }

   private boolean indexShaft(
      ClientWorld clientWorld,
      WorldChunk worldChunk,
      DiggerEspModule.TunnelChunk tunnelChunk,
      ChunkSection[] chunkSections,
      int i,
      int j,
      int k,
      int l,
      int m,
      long n
   ) {
      for (int var12 = tunnelChunk.si; var12 < chunkSections.length && var12 < tunnelChunk.air.length; var12++) {
         if (!tunnelChunk.air[var12]) {
            this.flushHole(tunnelChunk, j, k);
         } else {
            int var13 = i + (var12 << 4);
            ChunkSection var14 = chunkSections[var12];

            for (int var15 = var12 == tunnelChunk.si ? tunnelChunk.ly : 0; var15 < 16; var15++) {
               if ((var15 & 3) == 0 && System.nanoTime() >= n) {
                  tunnelChunk.si = var12;
                  tunnelChunk.ly = var15;
                  return false;
               }

               int var16 = var13 + var15;
               boolean var17 = var14 == null || var14.isEmpty() || var14.getBlockState(l, var15, m).isAir();
               if (var17 && this.enclosedHole(clientWorld, worldChunk, j, var16, k)) {
                  if (tunnelChunk.hole == 0) {
                     tunnelChunk.holeY = var16;
                  }

                  tunnelChunk.hole++;
               } else if (tunnelChunk.hole > 0) {
                  this.flushHole(tunnelChunk, j, k);
               }

               if (var17
                  && !this.airOnly(clientWorld, j, var16 - 1, k)
                  && (this.tunnelRun(clientWorld, j, var16, k, 1, 0) || this.tunnelRun(clientWorld, j, var16, k, 0, 1))) {
                  int var18 = 0;

                  while (var18 < 3 && this.airOnly(clientWorld, j, var16 + var18, k)) {
                     var18++;
                  }

                  tunnelChunk.boxes.add(new int[]{j, var16, k, j + 1, var16 + var18, k + 1});
               }
            }
         }
      }

      return true;
   }

   private void flushHole(DiggerEspModule.TunnelChunk tunnelChunk, int i, int j) {
      if (tunnelChunk.hole >= 4) {
         tunnelChunk.boxes.add(new int[]{i, tunnelChunk.holeY, j, i + 1, tunnelChunk.holeY + tunnelChunk.hole, j + 1});
      }

      tunnelChunk.hole = 0;
   }

   private boolean enclosedHole(ClientWorld clientWorld, WorldChunk worldChunk, int i, int j, int k) {
      return !this.airOnly(clientWorld, i, j, k - 1)
         && !this.airOnly(clientWorld, i, j, k + 1)
         && !this.airOnly(clientWorld, i + 1, j, k)
         && !this.airOnly(clientWorld, i - 1, j, k);
   }

   private boolean tunnelRun(ClientWorld clientWorld, int i, int j, int k, int l, int m) {
      int var7 = 0;
      int var8 = l == 0 ? 1 : 0;
      int var9 = m == 0 ? 1 : 0;

      for (int var10 = 0; var10 < 3 && this.tunnelSection(clientWorld, i + l * var10, j, k + m * var10, var8, var9); var10++) {
         var7++;
      }

      if (var7 >= 3) {
         return true;
      } else {
         for (int var11 = 1; var7 < 3 && var11 < 3 && this.tunnelSection(clientWorld, i - l * var11, j, k - m * var11, var8, var9); var11++) {
            var7++;
         }

         return var7 >= 3;
      }
   }

   private boolean tunnelSection(ClientWorld clientWorld, int i, int j, int k, int l, int m) {
      int var7 = 0;

      while (var7 < 3 && this.airAt(clientWorld, i, j + var7, k)) {
         var7++;
      }

      if (var7 >= 2 && var7 <= 3) {
         if (!this.airAt(clientWorld, i, j - 1, k) && !this.airAt(clientWorld, i, j + var7, k)) {
            for (int var8 = 0; var8 < var7; var8++) {
               if (this.airAt(clientWorld, i + l, j + var8, k + m) || this.airAt(clientWorld, i - l, j + var8, k - m)) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean airAt(ClientWorld clientWorld, int i, int j, int k) {
      return j >= clientWorld.getBottomY() && j < clientWorld.getTopYInclusive() ? this.airOnly(clientWorld, i, j, k) : false;
   }

   private boolean airOnly(ClientWorld clientWorld, int i, int j, int k) {
      return j >= clientWorld.getBottomY() && j < clientWorld.getTopYInclusive() ? clientWorld.getBlockState(this.cursorPos.set(i, j, k)).isAir() : false;
   }

   private void accept(DiggerEspModule.Column column) {
      if (!this.alreadyMarked(column)) {
         long var2 = ChunkPos.toLong(column.x >> 4, column.z >> 4);
         List var4 = this.found.get(var2);
         if (var4 == null) {
            this.found.put(var2, List.of(column));
         } else {
            ArrayList var5 = new ArrayList(var4.size() + 1);
            var5.addAll(var4);
            var5.add(column);
            this.found.put(var2, List.copyOf(var5));
         }

         this.collapseTwins();
      }
   }

   private boolean cappedWithStone(ClientWorld clientWorld, WorldChunk worldChunk, int i, int j, int k, int l, int m, int n, int o, int p) {
      return this.stoneBeside(clientWorld, worldChunk, i, j, k, l, m, n, o - 1) && this.stoneBeside(clientWorld, worldChunk, i, j, k, l, m, n, o + p);
   }

   private boolean stoneBeside(ClientWorld clientWorld, WorldChunk worldChunk, int i, int j, int k, int l, int m, int n, int o) {
      if (o >= 0 && o < n) {
         return this.stoneAt[o];
      } else {
         int var10 = m + o;
         return var10 >= clientWorld.getBottomY() && var10 < clientWorld.getTopYInclusive()
            ? isStone(this.blockAt(clientWorld, worldChunk, i, j, k, var10, l))
            : false;
      }
   }

   private void markSections(WorldChunk worldChunk) {
      ChunkSection[] var2 = worldChunk.getSectionArray();
      int var3 = Math.min(var2.length, this.sectionStone.length);

      for (int var4 = 0; var4 < var3; var4++) {
         ChunkSection var5 = var2[var4];
         boolean var6 = var5 != null && !var5.isEmpty();
         this.sectionStone[var4] = var6 && var5.hasAny(DiggerEspModule::isStone);
         this.sectionWall[var4] = var6 && var5.hasAny(DiggerEspModule::isWallState);
      }

      for (int var7 = var3; var7 < this.sectionStone.length; var7++) {
         this.sectionStone[var7] = false;
         this.sectionWall[var7] = false;
      }
   }

   private static boolean isStone(BlockState blockState) {
      return blockState.isOf(Blocks.STONE);
   }

   private static boolean isWallState(BlockState blockState) {
      return DiggerColumns.isWall(blockState.getBlock());
   }

   private BlockState blockAt(ClientWorld clientWorld, WorldChunk worldChunk, int i, int j, int k, int l, int m) {
      if (k >> 4 == i && m >> 4 == j) {
         ChunkSection[] var8 = worldChunk.getSectionArray();
         int var9 = l - clientWorld.getBottomY() >> 4;
         if (var9 >= 0 && var9 < var8.length) {
            ChunkSection var10 = var8[var9];
            if (var10 != null && !var10.isEmpty()) {
               return var10.getBlockState(k & 15, l & 15, m & 15);
            }
         }
      }

      return clientWorld.getBlockState(this.cursorPos.set(k, l, m));
   }

   @Environment(EnvType.CLIENT)
   private record Column(int x, int z, int y, int length) {
   }

   @Environment(EnvType.CLIENT)
   private static final class TunnelChunk {
      private boolean ready;
      private boolean anyAir;
      private int cursor;
      private int si;
      private int ly;
      private int hole;
      private int holeY;
      private final boolean[] air = new boolean[32];
      private final ArrayList<int[]> boxes = new ArrayList<>();
   }
}
