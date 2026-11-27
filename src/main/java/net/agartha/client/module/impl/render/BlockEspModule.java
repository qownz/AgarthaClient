package net.agartha.client.module.impl.render;

import imgui.ImDrawList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.BlockUpdateEvent;
import net.agartha.client.event.impl.PacketReceiveEvent;
import net.agartha.client.event.impl.Render3DEvent;
import net.agartha.client.event.impl.ScanFrameEvent;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.TracerOverlay;
import net.agartha.client.module.setting.BlockColorSetting;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.util.perf.TickBudget;
import net.agartha.client.util.perf.TimeSlice;
import net.agartha.client.util.render.BlockBox;
import net.agartha.client.util.render.FrustumCulling;
import net.agartha.client.util.render.GizmoBatch;
import net.agartha.client.util.render.OffscreenIndicator;
import net.agartha.client.util.world.BlockCoverage;
import net.agartha.client.util.world.ChunkRescanRouter;
import net.agartha.client.util.world.ChunkScanCache;
import net.agartha.client.util.world.ViewDistance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

@Environment(EnvType.CLIENT)
public class BlockEspModule extends Module implements TracerOverlay {
   private static final float TRACER_WIDTH = 1.5F;
   private final ChunkScanCache<BlockEspModule.a> scan = new ChunkScanCache<>();
   private final Map<Long, Set<Long>> chunkPosIndex = new ConcurrentHashMap<>();
   private int clientTick;
   private int pruneTimer;
   private int queueCooldown;
   private int queuedCenterX = Integer.MIN_VALUE;
   private int queuedCenterZ = Integer.MIN_VALUE;
   private static final int CHUNKS_PER_TICK = 48;
   private final TimeSlice scanSlice = new TimeSlice();
   private static final int QUEUE_INTERVAL_TICKS = 10;
   private final BooleanSetting sBox = this.addSetting(new BooleanSetting("Box", true));
   private final BooleanSetting sTracers = this.addSetting(new BooleanSetting("Tracers", false));
   private final NumberSetting sBoxSize = this.addSetting(new NumberSetting("Box Size", 1.0F, 0.1F, 2.0F));
   private final NumberSetting sFillAlpha = this.addSetting(new NumberSetting("Fill Alpha", 0.2F, 0.0F, 1.0F));
   private final NumberSetting sLineWidth = this.addSetting(new NumberSetting("Line Width", 1.5F, 0.5F, 5.0F));
   private final BlockColorSetting sBlockColors = this.addSetting(BlockColorSetting.createDefault());

   public BlockEspModule() {
      super("Block ESP", "Highlights the blocks you pick with boxes and optional tracers.", ModuleCategory.RENDER);
      this.sBlockColors.onChange(this::a);
   }

   private void a() {
      this.scan.clear();
      this.chunkPosIndex.clear();
      this.clientTick = 0;
   }

   @Override
   protected void onDisable() {
      this.scan.clear();
      this.chunkPosIndex.clear();
      this.clientTick = 0;
      this.pruneTimer = 0;
      this.queueCooldown = 0;
      this.queuedCenterX = Integer.MIN_VALUE;
      this.queuedCenterZ = Integer.MIN_VALUE;
   }

   @EventListener
   public void onTick(TickEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null) {
         if (this.sBlockColors.hasAnyEnabled()) {
            this.clientTick++;
            int var3 = var2.player.getBlockX() >> 4;
            int var4 = var2.player.getBlockZ() >> 4;
            int var5 = ViewDistance.chunks();
            if (++this.pruneTimer >= 20) {
               this.pruneTimer = 0;
               this.scan.prune(l -> {
                  int var6 = (int)l;
                  int var7 = (int)(l >>> 32);
                  return Math.abs(var6 - var3) > var5 || Math.abs(var7 - var4) > var5 || !var2.world.getChunkManager().isChunkLoaded(var6, var7);
               });
               this.chunkPosIndex.keySet().retainAll(this.scan.cacheView().keySet());
            }

            if (var3 != this.queuedCenterX || var4 != this.queuedCenterZ || ++this.queueCooldown >= 10) {
               this.queueCooldown = 0;
               this.queuedCenterX = var3;
               this.queuedCenterZ = var4;
               this.scan.queueInView(var3, var4, var5, this.clientTick, l -> {
                  int var3x = (int)l;
                  int var4x = (int)(l >>> 32);
                  return var2.world.getChunkManager().isChunkLoaded(var3x, var4x);
               });
            }
         }
      }
   }

   @EventListener
   public void onScanFrame(ScanFrameEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null && this.sBlockColors != null && this.sBlockColors.hasAnyEnabled()) {
         if (this.scanSlice.begin(TickBudget.sliceNs(3), 48)) {
            try {
               long var3 = this.scanSlice.deadline();
               int var5 = this.scanSlice.unitsLeft();
               ArrayList var6 = null;

               int var7;
               for (var7 = 0; var7 < var5 && System.nanoTime() < var3; var7++) {
                  Long var8 = this.scan.poll();
                  if (var8 == null) {
                     break;
                  }

                  int var9 = (int)var8.longValue();
                  int var10 = (int)(var8 >>> 32);
                  if (!var2.world.getChunkManager().isChunkLoaded(var9, var10)) {
                     if (var6 == null) {
                        var6 = new ArrayList();
                     }

                     var6.add(var8);
                  } else {
                     this.a(var2, var9, var10, var8);
                  }
               }

               this.scanSlice.useUnits(var7);
               if (var6 != null) {
                  for (int var14 = var6.size() - 1; var14 >= 0; var14--) {
                     this.scan.requestRescan((Long)var6.get(var14));
                  }
               }
            } finally {
               this.scanSlice.end();
            }
         }
      }
   }

   @EventListener
   public void onPacketReceive(PacketReceiveEvent arg) {
      ChunkRescanRouter.apply(arg, this.scan);
   }

   @EventListener
   public void onBlockUpdate(BlockUpdateEvent arg) {
      if (this.sBlockColors != null) {
         long var2 = ChunkPos.toLong(arg.pos.getX() >> 4, arg.pos.getZ() >> 4);
         List var4 = this.scan.get(var2);
         if (var4 == null) {
            var4 = List.of();
         }

         Block var5 = arg.newState.getBlock();
         boolean var6 = !arg.newState.isAir() && this.sBlockColors.isEnabled(var5);
         if (var6 || this.a(arg.pos)) {
            BlockPos var7 = arg.pos.toImmutable();
            ArrayList var8 = new ArrayList(var4.size() + 1);
            boolean var9 = false;

            for (BlockEspModule.a var11 : (Iterable<BlockEspModule.a>) var4) {
               if (var11.pos().equals(var7)) {
                  var9 = true;
               } else {
                  var8.add(var11);
               }
            }

            if (var6) {
               var8.add(new BlockEspModule.a(var7, this.sBlockColors.colorArgb(var5)));
               var9 = true;
            }

            if (var9) {
               this.a(var2, var8);
            }
         }
      }
   }

   @EventListener
   public void onRender3D(Render3DEvent arg) {
      if (this.sBox != null && this.sBox.value().get()) {
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (var2.player != null) {
            float var3 = this.sLineWidth != null ? this.sLineWidth.value()[0] : 1.5F;
            int var4 = Math.round((this.sFillAlpha != null ? this.sFillAlpha.value()[0] : 0.2F) * 255.0F);
            int var5 = ViewDistance.chunks();
            int var6 = var2.player.getBlockX() >> 4;
            int var7 = var2.player.getBlockZ() >> 4;

            for (long var9 : this.scan.nonemptyChunkKeys()) {
               int var11 = (int)var9;
               int var12 = (int)(var9 >>> 32);
               if (Math.abs(var11 - var6) <= var5 && Math.abs(var12 - var7) <= var5 && !FrustumCulling.columnOutOfView(var11, var12, -64, 320)) {
                  List var13 = this.scan.get(var9);
                  if (var13 != null && !var13.isEmpty()) {
                     for (BlockEspModule.a var15 : (Iterable<BlockEspModule.a>) var13) {
                        if (!BlockCoverage.covers(var15.pos())) {
                           int var16 = var15.argb();
                           int var17 = a(var16, var4);
                           float var18 = this.sBoxSize != null ? this.sBoxSize.value()[0] : 1.0F;
                           Box var19 = BlockBox.scaledBlockBox(var15.pos(), var18);
                           GizmoBatch.submitCuboid(var15.pos(), var19, var16, var3, var4 > 0 ? var17 : null, true);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public boolean coversBlock(BlockPos blockPos) {
      return this.isEnabled() && this.sBox != null && this.sBox.value().get() && this.a(blockPos);
   }

   public boolean wouldDrawTracer(BlockPos blockPos) {
      return this.isTracerOverlayEnabled() && this.a(blockPos);
   }

   private boolean a(BlockPos blockPos) {
      long var2 = ChunkPos.toLong(blockPos.getX() >> 4, blockPos.getZ() >> 4);
      Set var4 = this.chunkPosIndex.get(var2);
      return var4 != null && var4.contains(blockPos.asLong());
   }

   private void a(long l, List<BlockEspModule.a> list) {
      if (list != null && !list.isEmpty()) {
         this.scan.store(l, list, this.clientTick);
         HashSet var4 = new HashSet(list.size() * 2);

         for (BlockEspModule.a var6 : list) {
            var4.add(var6.pos().asLong());
         }

         this.chunkPosIndex.put(l, Collections.unmodifiableSet(var4));
      } else {
         this.scan.store(l, List.of(), this.clientTick);
         this.chunkPosIndex.put(l, Set.of());
      }
   }

   @Override
   public boolean isTracerOverlayEnabled() {
      return this.isEnabled() && this.sTracers != null && this.sTracers.value().get() && this.sBlockColors != null && this.sBlockColors.hasAnyEnabled();
   }

   @Override
   public void renderTracerOverlay(ImDrawList imDrawList, float f, float g) {
      if (this.isTracerOverlayEnabled() && imDrawList != null) {
         MinecraftClient var4 = MinecraftClient.getInstance();
         if (var4.player != null) {
            float var5 = var4.getRenderTickCounter().getTickProgress(false);
            float var6 = OffscreenIndicator.originX(f);
            float var7 = OffscreenIndicator.originY("Crosshair", g);
            float var8 = 1.5F;
            int var9 = ViewDistance.chunks();
            int var10 = var4.player.getBlockX() >> 4;
            int var11 = var4.player.getBlockZ() >> 4;
            Vec3d var12 = var4.player.getCameraPosVec(var5);
            double var13 = var9 * 16.0;

            for (long var16 : this.scan.nonemptyChunkKeys()) {
               int var18 = (int)var16;
               int var19 = (int)(var16 >>> 32);
               if (Math.abs(var18 - var10) <= var9 && Math.abs(var19 - var11) <= var9) {
                  List var20 = this.scan.get(var16);
                  if (var20 != null && !var20.isEmpty()) {
                     for (BlockEspModule.a var22 : (Iterable<BlockEspModule.a>) var20) {
                        if (!BlockCoverage.covers(var22.pos())) {
                           int var23 = var22.argb();
                           if (var23 >>> 24 != 0) {
                              Vec3d var24 = Vec3d.ofCenter(var22.pos());
                              OffscreenIndicator.drawWithFade(imDrawList, var4, f, g, var6, var7, var24, var23, var8, var12, var13);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void a(MinecraftClient minecraftClient, int i, int j, long l) {
      if (minecraftClient.world != null) {
         WorldChunk var6 = minecraftClient.world.getChunkManager().getWorldChunk(i, j);
         if (var6 != null) {
            int var7 = i << 4;
            int var8 = j << 4;
            ArrayList var9 = new ArrayList();
            Mutable var10 = new Mutable();
            ChunkSection[] var11 = var6.getSectionArray();
            int var12 = minecraftClient.world.getDimension().minY();
            int var13 = var12 >> 4;

            for (int var14 = 0; var14 < var11.length; var14++) {
               ChunkSection var15 = var11[var14];
               if (var15 != null && !var15.isEmpty() && var15.hasAny(blockState -> this.sBlockColors.isEnabled(blockState.getBlock()))) {
                  int var16 = var13 + var14 << 4;

                  for (int var17 = var7; var17 < var7 + 16; var17++) {
                     for (int var18 = var8; var18 < var8 + 16; var18++) {
                        for (int var19 = var16; var19 < var16 + 16; var19++) {
                           var10.set(var17, var19, var18);
                           BlockState var20 = var15.getBlockState(var17 & 15, var19 & 15, var18 & 15);
                           if (!var20.isAir()) {
                              Block var21 = var20.getBlock();
                              if (this.sBlockColors != null && this.sBlockColors.isEnabled(var21)) {
                                 var9.add(new BlockEspModule.a(var10.toImmutable(), this.sBlockColors.colorArgb(var21)));
                              }
                           }
                        }
                     }
                  }
               }
            }

            this.a(l, var9);
         }
      }
   }

   private static int a(int i, int j) {
      return i & 16777215 | Math.min(255, Math.max(0, j)) << 24;
   }

   @Environment(EnvType.CLIENT)
   private record a(BlockPos pos, int argb) {
   }
}
