package net.agartha.client.module.impl.render;

import imgui.ImDrawList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.BlockUpdateEvent;
import net.agartha.client.event.impl.PacketReceiveEvent;
import net.agartha.client.event.impl.Render3DEvent;
import net.agartha.client.event.impl.ScanFrameEvent;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.TracerOverlay;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ColorSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.module.setting.Setting;
import net.agartha.client.util.perf.Profiler;
import net.agartha.client.util.perf.TickBudget;
import net.agartha.client.util.perf.TimeSlice;
import net.agartha.client.util.render.BlockBox;
import net.agartha.client.util.render.FrustumCulling;
import net.agartha.client.util.render.GizmoBatch;
import java.util.Map.Entry;
import net.agartha.client.util.render.OffscreenIndicator;
import net.agartha.client.util.world.BlockCoverage;
import net.agartha.client.util.world.BlockEntityTracker;
import net.agartha.client.util.world.ChunkScanCache;
import net.agartha.client.util.world.ViewDistance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.WorldChunk;

@Environment(EnvType.CLIENT)
public final class StorageEspModule extends Module implements TracerOverlay {
   private static final float TRACER_WIDTH = 1.5F;
   private final Map<Long, Map<Long, String>> remembered = new HashMap<>();
   private final ArrayDeque<Long> blockScanQueue = new ArrayDeque<>();
   private final ArrayDeque<StorageEspModule.b> sectionQueue = new ArrayDeque<>();
   private boolean seedBlockScan = true;
   private final ChunkScanCache<StorageEspModule.c> scan = new ChunkScanCache<>();
   private int pruneTimer = 0;
   private final TimeSlice scanSlice = new TimeSlice();
   private boolean visibleDirty;
   private int tickCounter;
   private int rebuiltAtTick = -1;
   private int sweepIndex;
   private final ArrayDeque<Long> pending = new ArrayDeque<>();
   private final Set<Long> pendingKeys = new HashSet<>();
   private final Map<Long, Long> fingerprints = new HashMap<>();
   private static final int CACHE_RADIUS = 128;
   private static final int MAX_TRACERS = 3000;
   private static final int MIN_VIEW_CHUNKS = 8;
   private static final int MAX_VIEW_CHUNKS = 32;
   private static final int MAX_CHUNKS_PER_TICK = 64;
   private static final int INDEX_BITS = 20;
   private static final int INDEX_MASK = 1048575;
   private final List<StorageEspModule.c> visible = new ArrayList<>();
   private final List<Box> visibleBoxes = new ArrayList<>();
   private ClientWorld lastLevel;
   private float cachedBoxSize = Float.NaN;
   private final HashMap<String, Integer> colorCache = new HashMap<>();
   private StorageEspModule.c[] candidates = new StorageEspModule.c[0];
   private long[] order = new long[0];
   private final BooleanSetting sTracers = this.addSetting(new BooleanSetting("Tracers", false));
   private final NumberSetting sBoxSize = this.addSetting(new NumberSetting("Box Size", 1.0F, 0.1F, 2.0F));
   private final NumberSetting sFillAlpha = this.addSetting(new NumberSetting("Fill Alpha", 0.25F, 0.0F, 1.0F));
   private final NumberSetting sLineWidth = this.addSetting(new NumberSetting("Line Width", 1.5F, 0.5F, 5.0F));
   private final BooleanSetting sChests = this.addSetting(new BooleanSetting("Chests", true));
   private final BooleanSetting sEnderChests = this.addSetting(new BooleanSetting("Ender Chests", true));
   private final BooleanSetting sShulkerBoxes = this.addSetting(new BooleanSetting("Shulker Boxes", true));
   private final BooleanSetting sSpawners = this.addSetting(new BooleanSetting("Spawners", true));
   private final BooleanSetting sFurnaces = this.addSetting(new BooleanSetting("Furnaces", false));
   private final BooleanSetting sBarrels = this.addSetting(new BooleanSetting("Barrels", true));
   private final BooleanSetting sEnchantingTables = this.addSetting(new BooleanSetting("Enchanting Tables", true));
   private final BooleanSetting sHoppers = this.addSetting(new BooleanSetting("Hoppers", false));
   private final BooleanSetting sYFilter = this.addSetting(new BooleanSetting("Y Filter", false));
   private final NumberSetting sMinY = this.addSetting(new NumberSetting("Min Y", -64.0F, -64.0F, 320.0F).whole());
   private final NumberSetting sMaxY = this.addSetting(new NumberSetting("Max Y", 320.0F, -64.0F, 320.0F).whole());
   private final ColorSetting sChestColor = this.addSetting(new ColorSetting("Chest Color", -6530304));
   private final ColorSetting sEnderChestColor = this.addSetting(new ColorSetting("Ender Chest Color", -9109249));
   private final ColorSetting sShulkerBoxColor = this.addSetting(new ColorSetting("Shulker Box Color", -7995234));
   private final ColorSetting sSpawnerColor = this.addSetting(new ColorSetting("Spawner Color", -7700826));
   private final ColorSetting sFurnaceColor = this.addSetting(new ColorSetting("Furnace Color", -10197916));
   private final ColorSetting sBarrelColor = this.addSetting(new ColorSetting("Barrel Color", -10204366));
   private final ColorSetting sEnchantColor = this.addSetting(new ColorSetting("Enchanting Table Color", -3670016));
   private final ColorSetting sHopperColor = this.addSetting(new ColorSetting("Hopper Color", -11513776));
   private final BooleanSetting sFadeHidden = this.addSetting(new BooleanSetting("Fade Hidden", true));
   private static final Map<Object, String> PATH_CACHE = new IdentityHashMap<>();
   private static final String NOT_STORAGE = "";

   public StorageEspModule() {
      super("Storage ESP", "Highlights storage blocks and spawners with per-type colours.", ModuleCategory.RENDER);
      this.sMinY.visibleWhen(this.sYFilter::get);
      this.sMaxY.visibleWhen(this.sYFilter::get);

      for (Setting var4 : new Setting[]{
         this.sYFilter,
         this.sMinY,
         this.sMaxY,
         this.sChests,
         this.sEnderChests,
         this.sShulkerBoxes,
         this.sSpawners,
         this.sFurnaces,
         this.sBarrels,
         this.sEnchantingTables,
         this.sHoppers
      }) {
         var4.onChange(() -> this.visibleDirty = true);
      }
   }

   public boolean isTypeSetting(Setting arg) {
      return arg == this.sChests
         || arg == this.sEnderChests
         || arg == this.sShulkerBoxes
         || arg == this.sSpawners
         || arg == this.sFurnaces
         || arg == this.sBarrels
         || arg == this.sEnchantingTables
         || arg == this.sHoppers;
   }

   public boolean isStorageColorSetting(Setting arg) {
      return arg instanceof ColorSetting;
   }

   public boolean hasVisibleStorageColors() {
      return this.sChestColor.isVisible()
         || this.sEnderChestColor.isVisible()
         || this.sShulkerBoxColor.isVisible()
         || this.sSpawnerColor.isVisible()
         || this.sFurnaceColor.isVisible()
         || this.sBarrelColor.isVisible()
         || this.sEnchantColor.isVisible()
         || this.sHopperColor.isVisible();
   }

   @Override
   protected void onEnable() {
      this.a();
   }

   @Override
   protected void onDisable() {
      this.a();
   }

   private void a() {
      this.visible.clear();
      this.visibleBoxes.clear();
      this.scan.clear();
      this.pending.clear();
      this.pendingKeys.clear();
      this.fingerprints.clear();
      this.sweepIndex = 0;
      this.pruneTimer = 0;
      this.visibleDirty = false;
      this.remembered.clear();
      this.blockScanQueue.clear();
      this.sectionQueue.clear();
      this.seedBlockScan = true;
   }

   @EventListener
   public void onTick(TickEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null) {
         if (var2.world != this.lastLevel) {
            this.a();
            this.lastLevel = var2.world;
         }

         this.tickCounter++;
         if (this.seedBlockScan) {
            this.seedBlockScan = false;
            int var3 = var2.player.getBlockX() >> 4;
            int var4 = var2.player.getBlockZ() >> 4;
            int var5 = b(var2);

            for (int var6 = -var5; var6 <= var5; var6++) {
               for (int var7 = -var5; var7 <= var5; var7++) {
                  if (var2.world.getChunkManager().isChunkLoaded(var3 + var6, var4 + var7)) {
                     this.blockScanQueue.add(ChunkPos.toLong(var3 + var6, var4 + var7));
                  }
               }
            }
         }

         if (++this.pruneTimer >= 20) {
            this.pruneTimer = 0;
            int var8 = var2.player.getBlockX() >> 4;
            int var9 = var2.player.getBlockZ() >> 4;
            int var10 = b(var2) * 2;
            if (this.remembered.keySet().removeIf(long_ -> Math.abs((int)long_.longValue() - var8) > var10 || Math.abs((int)(long_ >>> 32) - var9) > var10)) {
               this.visibleDirty = true;
            }

            this.scan.prune(l -> {
               int var6x = (int)l;
               int var7x = (int)(l >>> 32);
               boolean var8x = Math.abs(var6x - var8) > 128 || Math.abs(var7x - var9) > 128 || !var2.world.getChunkManager().isChunkLoaded(var6x, var7x);
               if (var8x) {
                  this.fingerprints.remove(l);
               }

               return var8x;
            });
         }
      }
   }

   @EventListener
   public void onPacketReceive(PacketReceiveEvent arg) {
      if (arg.packet instanceof ChunkDataS2CPacket var2) {
         this.a(ChunkPos.toLong(var2.getChunkX(), var2.getChunkZ()));
      } else if (arg.packet instanceof BlockEntityUpdateS2CPacket var3) {
         BlockPos var8 = var3.getPos();
         this.a(var8, var3.getBlockEntityType());
         this.a(ChunkPos.toLong(var8.getX() >> 4, var8.getZ() >> 4));
      } else if (arg.packet instanceof UnloadChunkS2CPacket var4) {
         long var10 = var4.pos().toLong();
         this.pendingKeys.remove(var10);
         this.fingerprints.remove(var10);
         this.scan.remove(var10);
      }
   }

   @EventListener
   public void onScanFrame(ScanFrameEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null && var2.world == this.lastLevel) {
         if (this.scanSlice.begin(TickBudget.sliceNs(0), 64)) {
            try {
               if (this.a(var2, this.scanSlice.deadline())) {
                  this.visibleDirty = true;
               }
            } finally {
               this.scanSlice.end();
            }
         }

         if (this.visibleDirty && this.rebuiltAtTick != this.tickCounter) {
            this.visibleDirty = false;
            this.rebuiltAtTick = this.tickCounter;
            this.a(var2);
         }
      }
   }

   private boolean a(MinecraftClient minecraftClient, long l) {
      long var4 = Profiler.begin();
      long var6 = l - (l - System.nanoTime()) / 2L;
      boolean var8 = false;
      int var9 = 0;

      while (!this.pending.isEmpty() && System.nanoTime() < var6) {
         long var10 = this.pending.pollFirst();
         if (this.pendingKeys.remove(var10)) {
            int var12 = (int)var10;
            int var13 = (int)(var10 >>> 32);
            var9++;
            if (minecraftClient.world.getChunkManager().isChunkLoaded(var12, var13) && this.a(minecraftClient, var12, var13, var10)) {
               var8 = true;
            }
         }
      }

      while (!this.blockScanQueue.isEmpty() && System.nanoTime() < var6) {
         long var14 = this.blockScanQueue.pollFirst();
         WorldChunk var16 = minecraftClient.world.getChunkManager().getWorldChunk((int)var14, (int)(var14 >>> 32));
         if (var16 != null) {
            this.a(var16);
         }
      }

      while (!this.sectionQueue.isEmpty() && System.nanoTime() < var6) {
         if (this.a(this.sectionQueue.pollFirst())) {
            var8 = true;
         }
      }

      int var15 = minecraftClient.player.getBlockX() >> 4;
      int var11 = minecraftClient.player.getBlockZ() >> 4;
      int var17 = b(minecraftClient);
      if (this.a(minecraftClient, var15, var11, var17, l)) {
         var8 = true;
      }

      Profiler.record("scanner.storage", var4);
      Profiler.recordCount("scanner.storage.chunks", var9);
      return var8;
   }

   @EventListener
   public void onBlockUpdate(BlockUpdateEvent arg) {
      if (arg.pos != null && arg.newState != null) {
         long var2 = arg.pos.asLong();
         long var4 = ChunkPos.toLong(arg.pos.getX() >> 4, arg.pos.getZ() >> 4);
         String var6 = a(arg.newState.getBlock());
         boolean var7 = arg.oldState == null || a(arg.oldState.getBlock()) != null;
         Map<Long, String> var8 = this.remembered.get(var4);
         if (var6 != null) {
            if (var8 == null) {
               this.remembered.put(var4, (var8 = new HashMap<Long, String>()));
            }

            if (!var6.equals(var8.put(var2, var6)) || !var7) {
               this.visibleDirty = true;
            }
         } else if (var8 != null && var8.containsKey(var2)) {
            if (!StorageEspModule.a.BLOCKS.contains(arg.newState.getBlock())) {
               var8.remove(var2);
               this.visibleDirty = true;
            } else if (var7) {
               this.visibleDirty = true;
            }
         }
      }

      boolean var9 = arg.oldState != null && arg.oldState.hasBlockEntity();
      boolean var3 = arg.newState != null && arg.newState.hasBlockEntity();
      if (var9 || var3) {
         MinecraftClient var10 = MinecraftClient.getInstance();
         if (var10.world != null) {
            long var5 = ChunkPos.toLong(arg.pos.getX() >> 4, arg.pos.getZ() >> 4);
            int var11 = arg.pos.getX() >> 4;
            int var12 = arg.pos.getZ() >> 4;
            if (var10.world.getChunkManager().isChunkLoaded(var11, var12) && this.a(var10, var11, var12, var5)) {
               this.visibleDirty = true;
            }
         }
      }
   }

   private void a(long l) {
      if (this.pendingKeys.add(l)) {
         this.pending.addFirst(l);
      }
   }

   private boolean a(MinecraftClient minecraftClient, int i, int j, int k, long l) {
      int var7 = k * 2 + 1;
      int var8 = var7 * var7;
      int var9 = 0;
      int var10 = 0;
      boolean var11 = false;
      int var12 = this.scanSlice.unitsLeft();

      while (var10 < var8 && var9 < var12 && System.nanoTime() < l) {
         int var13 = Math.floorMod(this.sweepIndex + var10, var8);
         var10++;
         int var14 = i + (var13 % var7 - k);
         int var15 = j + (var13 / var7 - k);
         if (minecraftClient.world.getChunkManager().isChunkLoaded(var14, var15)) {
            long var16 = ChunkPos.toLong(var14, var15);
            if (!this.pendingKeys.contains(var16)) {
               if (this.a(minecraftClient, var14, var15, var16)) {
                  var11 = true;
               }

               var9++;
            }
         }
      }

      this.sweepIndex = Math.floorMod(this.sweepIndex + var10, var8);
      this.scanSlice.useUnits(var9);
      return var11;
   }

   private boolean a(MinecraftClient minecraftClient, int i, int j, long l) {
      if (minecraftClient.world == null) {
         return false;
      } else {
         WorldChunk var6 = minecraftClient.world.getChunkManager().getWorldChunk(i, j);
         if (var6 == null) {
            return false;
         } else {
            long var7 = a(var6, l);
            Long var9 = this.fingerprints.get(l);
            if (var9 == null || var9 != var7 || var7 != 0L && this.scan.get(l) == null) {
               this.fingerprints.put(l, var7);
               ArrayList var10 = null;

               for (Entry var12 : var6.getBlockEntities().entrySet()) {
                  String var13 = a((BlockEntity)var12.getValue());
                  if (var13 != null) {
                     if (var10 == null) {
                        var10 = new ArrayList(4);
                     }

                     var10.add(new StorageEspModule.c((BlockPos)var12.getKey(), var13));
                  }
               }

               for (Entry var16 : BlockEntityTracker.inChunk(l).entrySet()) {
                  String var17 = a(((BlockEntityTracker.a)var16.getValue()).block());
                  if (var17 != null && !a(var10, (BlockPos)var16.getKey())) {
                     if (var10 == null) {
                        var10 = new ArrayList(4);
                     }

                     var10.add(new StorageEspModule.c((BlockPos)var16.getKey(), var17));
                  }
               }

               List var15 = this.scan.get(l);
               if (var10 == null) {
                  if (var15 != null && !var15.isEmpty()) {
                     this.scan.remove(l);
                     return true;
                  } else {
                     if (var15 != null) {
                        this.scan.remove(l);
                     }

                     return false;
                  }
               } else if (a(var15, var10)) {
                  return false;
               } else {
                  this.scan.overwrite(l, var10);
                  return true;
               }
            } else {
               return false;
            }
         }
      }
   }

   private static long a(WorldChunk worldChunk, long l) {
      long var3 = 0L;

      for (Entry var6 : worldChunk.getBlockEntities().entrySet()) {
         String var7 = a((BlockEntity)var6.getValue());
         if (var7 != null) {
            var3 = var3 * 31L + ((BlockPos)var6.getKey()).asLong();
            var3 = var3 * 31L + var7.hashCode();
            if (var3 == 0L) {
               var3 = 1L;
            }
         }
      }

      for (Entry var11 : BlockEntityTracker.inChunk(l).entrySet()) {
         String var12 = a(((BlockEntityTracker.a)var11.getValue()).block());
         if (var12 != null) {
            var3 = var3 * 31L + ((BlockPos)var11.getKey()).asLong();
            var3 = var3 * 31L + var12.hashCode();
            if (var3 == 0L) {
               var3 = 1L;
            }
         }
      }

      return var3;
   }

   private static boolean a(List<StorageEspModule.c> list, BlockPos blockPos) {
      if (list == null) {
         return false;
      } else {
         for (int var2 = 0; var2 < list.size(); var2++) {
            if (((StorageEspModule.c)list.get(var2)).pos().equals(blockPos)) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean a(List<StorageEspModule.c> list, List<StorageEspModule.c> list2) {
      if (list != null && list.size() == list2.size()) {
         for (int var2 = 0; var2 < list2.size(); var2++) {
            StorageEspModule.c var3 = (StorageEspModule.c)list2.get(var2);
            if (!a(list, var3)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean a(List<StorageEspModule.c> list, StorageEspModule.c arg) {
      for (int var2 = 0; var2 < list.size(); var2++) {
         StorageEspModule.c var3 = (StorageEspModule.c)list.get(var2);
         if (var3.pos().equals(arg.pos()) && var3.typePath().equals(arg.typePath())) {
            return true;
         }
      }

      return false;
   }

   @EventListener
   public void onRender3D(Render3DEvent arg) {
      if (!this.visible.isEmpty()) {
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (var2.player != null) {
            float var3 = this.sLineWidth.value()[0];
            int var4 = Math.round(this.sFillAlpha.value()[0] * 255.0F);
            float var5 = this.sBoxSize != null ? this.sBoxSize.value()[0] : 1.0F;
            BlockEspModule var6 = ModuleManager.get(BlockEspModule.class);
            if (var5 != this.cachedBoxSize || this.visibleBoxes.size() != this.visible.size()) {
               this.b();
            }

            this.colorCache.clear();
            int var7 = ViewDistance.chunks();
            ChunkPos var8 = var2.player.getChunkPos();
            int var9 = Integer.MIN_VALUE;
            int var10 = Integer.MIN_VALUE;
            boolean var11 = false;

            for (int var12 = 0; var12 < this.visible.size(); var12++) {
               StorageEspModule.c var13 = this.visible.get(var12);
               BlockPos var14 = var13.pos();
               int var15 = var14.getX() >> 4;
               int var16 = var14.getZ() >> 4;
               if (Math.max(Math.abs(var15 - var8.x), Math.abs(var16 - var8.z)) <= var7) {
                  if (var15 != var9 || var16 != var10) {
                     var9 = var15;
                     var10 = var16;
                     var11 = FrustumCulling.columnOutOfView(var15, var16, -64, 320);
                  }

                  if (!var11 && !this.b(var14.getY()) && !a(var6, var14)) {
                     Integer var17 = this.colorCache.get(var13.typePath());
                     int var18;
                     if (var17 == null) {
                        var18 = this.a(var13.typePath());
                        this.colorCache.put(var13.typePath(), var18);
                     } else {
                        var18 = var17;
                     }

                     if (var18 != -1) {
                        if (var13.hidden() && this.sFadeHidden.get()) {
                           var18 = a(var18, Math.max(40, (var18 >>> 24 & 0xFF) * 2 / 5));
                        }

                        int var19 = var13.hidden() && this.sFadeHidden.get() ? var4 * 2 / 5 : var4;
                        int var20 = a(var18, var19);
                        GizmoBatch.submitCuboid(var14, this.visibleBoxes.get(var12), var18, var3, var19 > 0 ? var20 : null, true);
                     }
                  }
               }
            }
         }
      }
   }

   public static void onChunkLoaded(ChunkDataS2CPacket chunkDataS2CPacket) {
      StorageEspModule var1 = ModuleManager.get(StorageEspModule.class);
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var1 != null && var1.isEnabled() && var2.world != null) {
         if (var2.world != var1.lastLevel) {
            var1.a();
            var1.lastLevel = var2.world;
         }

         int var3 = chunkDataS2CPacket.getChunkX();
         int var4 = chunkDataS2CPacket.getChunkZ();

         try {
            chunkDataS2CPacket.getChunkData()
               .getBlockEntities(var3, var4)
               .accept((blockPos, blockEntityType, nbtCompound) -> var1.a(blockPos, blockEntityType));
         } catch (RuntimeException var6) {
         }

         WorldChunk var5 = var2.world.getChunkManager().getWorldChunk(var3, var4);
         if (var5 != null) {
            var1.a(var5);
         }
      }
   }

   private void a(BlockPos blockPos, BlockEntityType<?> blockEntityType) {
      if (blockPos != null && blockEntityType != null) {
         String var3 = a(blockEntityType);
         if (var3 != null) {
            long var4 = ChunkPos.toLong(blockPos.getX() >> 4, blockPos.getZ() >> 4);
            if (!var3.equals(this.remembered.computeIfAbsent(var4, long_ -> new HashMap<>()).put(blockPos.asLong(), var3))) {
               this.visibleDirty = true;
            }
         }
      }
   }

   private void a(WorldChunk worldChunk) {
      ChunkSection[] var2 = worldChunk.getSectionArray();
      int var3 = worldChunk.getPos().getStartX();
      int var4 = worldChunk.getPos().getStartZ();
      int var5 = worldChunk.getBottomY();
      long var6 = worldChunk.getPos().toLong();

      for (int var8 = 0; var8 < var2.length; var8++) {
         ChunkSection var9 = var2[var8];
         if (var9 != null && !var9.isEmpty() && var9.hasAny(blockState -> a(blockState.getBlock()) != null)) {
            this.sectionQueue.addLast(new StorageEspModule.b(var6, var3, var5 + (var8 << 4), var4, var9.getBlockStateContainer().copy()));
         }
      }
   }

   private boolean a(StorageEspModule.b arg) {
      boolean var2 = false;
      Map var3 = null;

      for (int var4 = 0; var4 < 16; var4++) {
         for (int var5 = 0; var5 < 16; var5++) {
            for (int var6 = 0; var6 < 16; var6++) {
               String var7 = a(((BlockState)arg.states().get(var6, var4, var5)).getBlock());
               if (var7 != null) {
                  if (var3 == null) {
                     var3 = this.remembered.computeIfAbsent(arg.chunkKey(), long_ -> new HashMap<>());
                  }

                  long var8 = BlockPos.asLong(arg.baseX() + var6, arg.baseY() + var4, arg.baseZ() + var5);
                  if (!var7.equals(var3.put(var8, var7))) {
                     var2 = true;
                  }
               }
            }
         }
      }

      return var2;
   }

   private void b() {
      float var1 = this.sBoxSize != null ? this.sBoxSize.value()[0] : 1.0F;
      this.visibleBoxes.clear();

      for (StorageEspModule.c var3 : this.visible) {
         this.visibleBoxes.add(BlockBox.scaledBlockBox(var3.pos(), var1));
      }

      this.cachedBoxSize = var1;
   }

   private void a(MinecraftClient minecraftClient) {
      this.visible.clear();
      this.visibleBoxes.clear();
      if ((this.scan.nonemptyChunkCount() != 0 || !this.remembered.isEmpty()) && minecraftClient.player != null) {
         Vec3d var2 = minecraftClient.player.getCameraPosVec(1.0F);
         int var3 = minecraftClient.player.getBlockX() >> 4;
         int var4 = minecraftClient.player.getBlockZ() >> 4;
         int var5 = b(minecraftClient);
         int var6 = 0;
         HashSet var7 = new HashSet();

         label111:
         for (long var9 : this.scan.nonemptyChunkKeys()) {
            int var11 = (int)var9;
            int var12 = (int)(var9 >>> 32);
            if (Math.abs(var11 - var3) <= var5 && Math.abs(var12 - var4) <= var5) {
               List var13 = this.scan.get(var9);
               if (var13 != null && !var13.isEmpty()) {
                  for (StorageEspModule.c var15 : (Iterable<StorageEspModule.c>) var13) {
                     if (this.a(var15.typePath()) != -1) {
                        BlockPos var16 = var15.pos();
                        if (!this.b(var16.getY())) {
                           double var17 = var16.getX() + 0.5 - var2.x;
                           double var19 = var16.getZ() + 0.5 - var2.z;
                           double var21 = var17 * var17 + var19 * var19;
                           if (var6 == 1048575) {
                              break label111;
                           }

                           if (var6 == this.candidates.length) {
                              this.a(var6);
                           }

                           this.candidates[var6] = var15;
                           this.order[var6] = a(var21, var6);
                           var6++;
                           var7.add(var16.asLong());
                        }
                     }
                  }
               }
            }
         }

         Mutable var25 = new Mutable();

         label89:
         for (Entry var10 : this.remembered.entrySet()) {
            long var28 = (Long)var10.getKey();
            if (Math.abs((int)var28 - var3) <= var5 && Math.abs((int)(var28 >>> 32) - var4) <= var5) {
               for (Entry var30 : (Iterable<Entry>) ((Map)var10.getValue()).entrySet()) {
                  if (var6 == 1048575) {
                     break label89;
                  }

                  long var31 = (Long)var30.getKey();
                  if (this.a((String)var30.getValue()) != -1) {
                     int var32 = BlockPos.unpackLongY(var31);
                     if (!this.b(var32) && !var7.contains(var31)) {
                        int var18 = BlockPos.unpackLongX(var31);
                        int var33 = BlockPos.unpackLongZ(var31);
                        boolean var20 = a(minecraftClient.world.getBlockState(var25.set(var18, var32, var33)).getBlock()) == null;
                        double var34 = var18 + 0.5 - var2.x;
                        double var23 = var33 + 0.5 - var2.z;
                        if (var6 == this.candidates.length) {
                           this.a(var6);
                        }

                        this.candidates[var6] = new StorageEspModule.c(new BlockPos(var18, var32, var33), (String)var30.getValue(), var20);
                        this.order[var6] = a(var34 * var34 + var23 * var23, var6);
                        var6++;
                     }
                  }
               }
            }
         }

         if (var6 != 0) {
            if (var6 > 3000) {
               Arrays.sort(this.order, 0, var6);
            }

            for (int var27 = 0; var27 < var6; var27++) {
               this.visible.add(this.candidates[(int)(this.order[var27] & 1048575L)]);
            }

            Arrays.fill(this.candidates, 0, var6, null);
            this.b();
         }
      }
   }

   private static int b(MinecraftClient minecraftClient) {
      int var1 = minecraftClient.options.getClampedViewDistance();
      return Math.max(8, Math.min(32, var1));
   }

   private static long a(double d, int i) {
      return (long)Float.floatToRawIntBits((float)d) << 20 | i;
   }

   private void a(int i) {
      int var2 = Math.max(64, Math.min(1048576, Math.max(i, 1) * 2));
      this.candidates = Arrays.copyOf(this.candidates, var2);
      this.order = Arrays.copyOf(this.order, var2);
   }

   @Override
   public boolean isTracerOverlayEnabled() {
      return this.isEnabled() && this.sTracers.value().get() && this.c() && !this.visible.isEmpty();
   }

   private boolean c() {
      return this.sChests.get()
         || this.sEnderChests.get()
         || this.sShulkerBoxes.get()
         || this.sSpawners.get()
         || this.sFurnaces.get()
         || this.sBarrels.get()
         || this.sEnchantingTables.get()
         || this.sHoppers.get();
   }

   @Override
   public void renderTracerOverlay(ImDrawList imDrawList, float f, float g) {
      if (this.isTracerOverlayEnabled() && imDrawList != null) {
         MinecraftClient var4 = MinecraftClient.getInstance();
         if (var4.world != null && var4.player != null) {
            float var5 = var4.getRenderTickCounter().getTickProgress(false);
            float var6 = OffscreenIndicator.originX(f);
            float var7 = OffscreenIndicator.originY("Crosshair", g);
            float var8 = 1.5F;
            Vec3d var9 = var4.player.getCameraPosVec(var5);
            double var10 = ViewDistance.blocks();
            BlockEspModule var12 = ModuleManager.get(BlockEspModule.class);
            int var13 = ViewDistance.chunks();
            ChunkPos var14 = var4.player.getChunkPos();
            int var15 = 0;

            for (StorageEspModule.c var17 : this.visible) {
               if (var15 >= 3000) {
                  break;
               }

               BlockPos var18 = var17.pos();
               if (Math.max(Math.abs((var18.getX() >> 4) - var14.x), Math.abs((var18.getZ() >> 4) - var14.z)) <= var13
                  && !this.b(var18.getY())
                  && !b(var12, var18)) {
                  int var19 = this.a(var17.typePath());
                  if (var19 != -1 && var19 >>> 24 != 0 && OffscreenIndicator.drawWithFade(imDrawList, var4, f, g, var6, var7, Vec3d.ofCenter(var18), var19, var8, var9, var10)) {
                     var15++;
                  }
               }
            }
         }
      }
   }

   private boolean b(int i) {
      if (!this.sYFilter.get()) {
         return false;
      } else {
         int var2 = this.sMinY.getInt();
         int var3 = this.sMaxY.getInt();
         return i < Math.min(var2, var3) || i > Math.max(var2, var3);
      }
   }

   private static String a(Block block) {
      String var1 = PATH_CACHE.get(block);
      if (var1 == null) {
         var1 = b(block);
         PATH_CACHE.put(block, var1 == null ? "" : var1);
         return var1;
      } else {
         return var1 == "" ? null : var1;
      }
   }

   private static String a(BlockEntity blockEntity) {
      return a(blockEntity.getType());
   }

   private static String a(BlockEntityType<?> blockEntityType) {
      String var1 = PATH_CACHE.get(blockEntityType);
      if (var1 == null) {
         var1 = b(blockEntityType);
         PATH_CACHE.put(blockEntityType, var1 == null ? "" : var1);
         return var1;
      } else {
         return var1 == "" ? null : var1;
      }
   }

   private static String b(Block block) {
      Identifier var1 = Registries.BLOCK.getId(block);
      if (var1 == null) {
         return null;
      } else {
         String var2 = var1.getPath();
         if (var2.contains("shulker_box")) {
            return "shulker_box";
         } else {
            return switch (var2) {
               case "chest", "trapped_chest", "ender_chest", "spawner", "furnace", "blast_furnace", "smoker", "barrel", "enchanting_table", "hopper" -> "spawner"
                     .equals(var2)
                  ? "mob_spawner"
                  : var2;
               default -> null;
            };
         }
      }
   }

   private static String b(BlockEntityType<?> blockEntityType) {
      Identifier var1 = Registries.BLOCK_ENTITY_TYPE.getId(blockEntityType);
      if (var1 == null) {
         return null;
      } else {
         String var2 = var1.getPath();
         if (var2.contains("shulker_box")) {
            return "shulker_box";
         } else {
            return switch (var2) {
               case "chest", "trapped_chest", "ender_chest", "mob_spawner", "furnace", "blast_furnace", "smoker", "barrel", "enchanting_table", "hopper" -> var2;
               default -> null;
            };
         }
      }
   }

   private int a(String string) {
      return switch (string) {
         case "chest", "trapped_chest" -> this.sChests.value().get() ? this.sChestColor.argb() : -1;
         case "ender_chest" -> this.sEnderChests.value().get() ? this.sEnderChestColor.argb() : -1;
         case "shulker_box" -> this.sShulkerBoxes.value().get() ? this.sShulkerBoxColor.argb() : -1;
         case "mob_spawner" -> this.sSpawners.value().get() ? this.sSpawnerColor.argb() : -1;
         case "furnace", "blast_furnace", "smoker" -> this.sFurnaces.value().get() ? this.sFurnaceColor.argb() : -1;
         case "barrel" -> this.sBarrels.value().get() ? this.sBarrelColor.argb() : -1;
         case "enchanting_table" -> this.sEnchantingTables.value().get() ? this.sEnchantColor.argb() : -1;
         case "hopper" -> this.sHoppers.value().get() ? this.sHopperColor.argb() : -1;
         default -> -1;
      };
   }

   private static boolean a(BlockEspModule arg, BlockPos blockPos) {
      return BlockCoverage.covers(blockPos) || arg != null && arg.coversBlock(blockPos);
   }

   private static boolean b(BlockEspModule arg, BlockPos blockPos) {
      return BlockCoverage.covers(blockPos) || arg != null && arg.wouldDrawTracer(blockPos);
   }

   private static int a(int i, int j) {
      return i & 16777215 | Math.min(255, Math.max(0, j)) << 24;
   }

   @Environment(EnvType.CLIENT)
   private static final class a {
      static final Set<Block> BLOCKS = Set.of(
         Blocks.AIR,
         Blocks.CAVE_AIR,
         Blocks.VOID_AIR,
         Blocks.STONE,
         Blocks.DEEPSLATE,
         Blocks.DIRT,
         Blocks.GRASS_BLOCK,
         Blocks.NETHERRACK,
         Blocks.END_STONE,
         Blocks.SAND,
         Blocks.GRAVEL,
         Blocks.MOSSY_COBBLESTONE_WALL
      );
   }

   @Environment(EnvType.CLIENT)
   private record b(long chunkKey, int baseX, int baseY, int baseZ, PalettedContainer<BlockState> states) {
   }

   @Environment(EnvType.CLIENT)
   private record c(BlockPos pos, String typePath, boolean hidden) {
      c(BlockPos blockPos, String string) {
         this(blockPos, string, false);
      }
   }
}
