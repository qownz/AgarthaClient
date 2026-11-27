package net.agartha.client.module.impl.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.agartha.client.d.f;
import net.agartha.client.d.a_pkg.o;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.module.b;
import net.agartha.client.module.a_pkg.c;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.util.af;
import net.agartha.client.util.l;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.LightType;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.VisibilityConfigurable;

@Environment(EnvType.CLIENT)
public class LightEspModule extends b {
   private static final long SCAN_COOLDOWN_MS = 1000L;
   private static final long OPTIMIZE_COOLDOWN_MS = 1750L;
   private static final int MAX_BOXES = 12000;
   private static final float LINE_WIDTH = 1.5F;
   private volatile ExecutorService worker;
   private final AtomicBoolean scanning = new AtomicBoolean(false);
   private volatile long lastScanTime;
   private volatile long lastOptimizeTime;
   private volatile Map<Integer, List<Box>> renderData = new HashMap<>();
   private final Set<BlockPos> skip = ConcurrentHashMap.newKeySet();
   private final j sMinLight = (j)this.addSetting(new j("Min Light", 5.0F, 1.0F, 15.0F));
   private final net.agartha.client.module.a_pkg.b sDenoise = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Denoise", true));
   private final c sColor = (c)this.addSetting(new c("Color", -2130719744));

   public LightEspModule() {
      super("Light Debug", "Finds player-made spots underground.", net.agartha.client.module.c.DONUT);
   }

   protected void onEnable() {
      this.renderData = new HashMap<>();
      this.skip.clear();
      this.lastScanTime = 0L;
      this.lastOptimizeTime = 0L;
      this.worker = Executors.newSingleThreadExecutor(runnable -> {
         Thread var1 = new Thread(runnable, "Agartha-LightDebug-Worker");
         var1.setDaemon(true);
         return var1;
      });
   }

   protected void onDisable() {
      ExecutorService var1 = this.worker;
      this.worker = null;
      if (var1 != null) {
         var1.shutdownNow();
      }

      this.renderData = new HashMap<>();
      this.skip.clear();
      this.scanning.set(false);
   }

   @f
   public void onTick(q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null) {
         long var3 = System.currentTimeMillis();
         if (var3 - this.lastScanTime > 1000L) {
            ExecutorService var5 = this.worker;
            if (var5 != null && !var5.isShutdown()) {
               if (this.scanning.compareAndSet(false, true)) {
                  this.lastScanTime = var3;

                  try {
                     var5.execute(this::scan);
                  } catch (RuntimeException var7) {
                     this.scanning.set(false);
                  }
               }
            }
         }
      }
   }

   private void scan() {
      try {
         MinecraftClient var1 = MinecraftClient.getInstance();
         ClientWorld var2 = var1.world;
         if (var2 == null || var1.player == null) {
            return;
         }

         int var3 = af.chunks();
         int var4 = clampInt(num(this.sMinLight, 5), 1, 15);
         int var5 = var2.getBottomY();
         byte var6 = -1;
         if (var5 <= var6) {
            ChunkPos var7 = var1.player.getChunkPos();
            long var8 = System.currentTimeMillis();
            if (this.isDenoise() && var8 - this.lastOptimizeTime >= 1750L) {
               this.optimizeWeakLights(var2, var7, var3, var4, var5, var6);
               this.lastOptimizeTime = var8;
            }

            HashSet<BlockPos> var10 = new HashSet<>();
            HashMap<Integer, List<BlockPos>> var11 = new HashMap<>();
            Mutable var12 = new Mutable();

            for (int var13 = var7.x - var3; var13 <= var7.x + var3; var13++) {
               for (int var14 = var7.z - var3; var14 <= var7.z + var3; var14++) {
                  if (var2.getChunkManager().isChunkLoaded(var13, var14)) {
                     int var15 = var13 << 4;
                     int var16 = var14 << 4;

                     for (int var17 = 0; var17 < 16; var17++) {
                        for (int var18 = 0; var18 < 16; var18++) {
                           for (int var19 = var5; var19 <= var6; var19++) {
                              var12.set(var15 + var17, var19, var16 + var18);
                              if (!this.skip.contains(var12)) {
                                 int var20 = var2.getLightLevel(LightType.BLOCK, var12);
                                 if (var20 >= var4 && var20 > var2.getLightLevel(LightType.SKY, var12)) {
                                    BlockPos var21 = var12.toImmutable();
                                    var10.add(var21);
                                    var11.computeIfAbsent(var20, integer -> new ArrayList<>()).add(var21);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            HashMap<Integer, List<Box>> var27 = new HashMap<>();
            Mutable var28 = new Mutable();
            int var29 = 0;

            label153:
            for (Entry<Integer, List<BlockPos>> var31 : var11.entrySet()) {
               List<Box> var32 = var27.computeIfAbsent((Integer)var31.getKey(), integer -> new ArrayList<>());

               for (BlockPos var34 : (Iterable<BlockPos>) (List)var31.getValue()) {
                  if (isExposed(var34, var10, var28)) {
                     var32.add(new Box(var34));
                     if (++var29 >= 12000) {
                        break label153;
                     }
                  }
               }
            }

            var27.values().removeIf(List::isEmpty);
            this.renderData = var27;
            return;
         }
      } catch (Throwable var25) {
         return;
      } finally {
         this.scanning.set(false);
      }
   }

   private static boolean isExposed(BlockPos blockPos, Set<BlockPos> set, Mutable mutable) {
      for (Direction var6 : Direction.values()) {
         mutable.set(blockPos, var6);
         if (!set.contains(mutable)) {
            return true;
         }
      }

      return false;
   }

   private void optimizeWeakLights(ClientWorld clientWorld, ChunkPos chunkPos, int i, int j, int k, int l) {
      this.clearFarSkips(chunkPos, i + 1);
      HashSet var7 = new HashSet();
      Mutable var8 = new Mutable();

      for (int var9 = chunkPos.x - i; var9 <= chunkPos.x + i; var9++) {
         for (int var10 = chunkPos.z - i; var10 <= chunkPos.z + i; var10++) {
            if (clientWorld.getChunkManager().isChunkLoaded(var9, var10)) {
               int var11 = var9 << 4;
               int var12 = var10 << 4;

               for (int var13 = 0; var13 < 16; var13++) {
                  for (int var14 = 0; var14 < 16; var14++) {
                     for (int var15 = k; var15 <= l; var15++) {
                        var8.set(var11 + var13, var15, var12 + var14);
                        int var16 = clientWorld.getLightLevel(LightType.BLOCK, var8);
                        if (var16 >= j + 1 && var16 <= j + 2 && var16 > clientWorld.getLightLevel(LightType.SKY, var8)) {
                           var7.add(var8.toImmutable());
                        }
                     }
                  }
               }
            }
         }
      }

      HashSet var17 = new HashSet();

      for (BlockPos var19 : (Iterable<BlockPos>) var7) {
         if (!var17.contains(var19)) {
            int var20 = clientWorld.getLightLevel(LightType.BLOCK, var19);
            if (!this.hasStrongerNeighbour(clientWorld, var19, var20 + 1, k, l, var8)) {
               this.floodSuppress(clientWorld, var19, var17, var20, j, chunkPos, i, k, l);
            }
         }
      }
   }

   private boolean hasStrongerNeighbour(ClientWorld clientWorld, BlockPos blockPos, int i, int j, int k, Mutable mutable) {
      for (int var7 = blockPos.getX() - 1; var7 <= blockPos.getX() + 1; var7++) {
         for (int var8 = Math.max(j, blockPos.getY() - 1); var8 <= Math.min(k, blockPos.getY() + 1); var8++) {
            for (int var9 = blockPos.getZ() - 1; var9 <= blockPos.getZ() + 1; var9++) {
               if (var7 != blockPos.getX() || var8 != blockPos.getY() || var9 != blockPos.getZ()) {
                  mutable.set(var7, var8, var9);
                  int var10 = clientWorld.getLightLevel(LightType.BLOCK, mutable);
                  if (var10 >= i && var10 > clientWorld.getLightLevel(LightType.SKY, mutable)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private void floodSuppress(ClientWorld clientWorld, BlockPos blockPos, Set<BlockPos> set, int i, int j, ChunkPos chunkPos, int k, int l, int m) {
      ArrayDeque var10 = new ArrayDeque();
      var10.add(blockPos);
      set.add(blockPos);

      while (!var10.isEmpty()) {
         BlockPos var11 = (BlockPos)var10.poll();
         this.skip.add(var11);

         for (Direction var15 : Direction.values()) {
            BlockPos var16 = var11.offset(var15);
            if (!set.contains(var16) && !this.skip.contains(var16) && inArea(var16, chunkPos, k, l, m)) {
               int var17 = clientWorld.getLightLevel(LightType.BLOCK, var16);
               if (var17 >= j && var17 <= i && var17 > clientWorld.getLightLevel(LightType.SKY, var16)) {
                  set.add(var16);
                  var10.add(var16);
               }
            }
         }
      }
   }

   private void clearFarSkips(ChunkPos chunkPos, int i) {
      this.skip.removeIf(blockPos -> Math.abs((blockPos.getX() >> 4) - chunkPos.x) > i || Math.abs((blockPos.getZ() >> 4) - chunkPos.z) > i);
   }

   private static boolean inArea(BlockPos blockPos, ChunkPos chunkPos, int i, int j, int k) {
      int var5 = chunkPos.x - i << 4;
      int var6 = (chunkPos.x + i + 1 << 4) - 1;
      int var7 = chunkPos.z - i << 4;
      int var8 = (chunkPos.z + i + 1 << 4) - 1;
      return blockPos.getX() >= var5
         && blockPos.getX() <= var6
         && blockPos.getZ() >= var7
         && blockPos.getZ() <= var8
         && blockPos.getY() >= j
         && blockPos.getY() <= k;
   }

   @f
   public void onRender3D(o arg) {
      Map var2 = this.renderData;
      if (!var2.isEmpty() && this.sColor != null) {
         int var3 = this.sColor.argb();
         int var4 = var3 >>> 24 & 0xFF;
         int var5 = var3 >> 16 & 0xFF;
         int var6 = var3 >> 8 & 0xFF;
         int var7 = var3 & 0xFF;

         for (Entry var9 : (Iterable<Entry>) var2.entrySet()) {
            float var10 = clamp01(((Integer)var9.getKey()).intValue() / 15.0F);
            int var11 = lerp(var5, 255, var10);
            int var12 = lerp(var6, 255, var10);
            int var13 = lerp(var7, 255, var10);
            int var14 = clampByte(Math.round(var4 * (0.5F + 0.5F * var10)));
            int var15 = var14 << 24 | var11 << 16 | var12 << 8 | var13;
            int var16 = 0xFF000000 | Math.round(var11 * 0.45F) << 16 | Math.round(var12 * 0.45F) << 8 | Math.round(var13 * 0.45F);
            DrawStyle var17 = DrawStyle.filledAndStroked(var16, 1.5F, var15);

            for (Box var19 : (Iterable<Box>) (List)var9.getValue()) {
               if (net.agartha.client.util.l.isSafeCuboid(var19)) {
                  VisibilityConfigurable var20 = GizmoDrawing.box(var19, var17);
                  var20.ignoreOcclusion();
               }
            }
         }
      }
   }

   private boolean isDenoise() {
      return this.sDenoise != null && this.sDenoise.value().get();
   }

   private static int num(j arg, int i) {
      return arg == null ? i : Math.round(arg.value()[0]);
   }

   private static int clampInt(int i, int j, int k) {
      return Math.max(j, Math.min(k, i));
   }

   private static int clampByte(int i) {
      return Math.max(0, Math.min(255, i));
   }

   private static float clamp01(float f) {
      return Math.max(0.0F, Math.min(1.0F, f));
   }

   private static int lerp(int i, int j, float f) {
      return Math.round(i + (j - i) * f);
   }
}
