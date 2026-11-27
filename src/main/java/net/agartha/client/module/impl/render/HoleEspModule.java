package net.agartha.client.module.impl.render;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.BlockUpdateEvent;
import net.agartha.client.event.impl.PacketReceiveEvent;
import net.agartha.client.event.impl.Render3DEvent;
import net.agartha.client.event.impl.ScanFrameEvent;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ColorSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.module.setting.Setting;
import net.agartha.client.util.perf.TimeSlice;
import net.agartha.client.util.render.FrustumCulling;
import net.agartha.client.util.render.GizmoBatch;
import net.agartha.client.util.world.ViewDistance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

@Environment(EnvType.CLIENT)
public class HoleEspModule extends Module {
   private static final String MODE_ALL = "All";
   private static final String MODE_HOLES_TUNNELS = "Holes + Tunnels";
   private static final String MODE_HOLES_STAIRS = "Holes + Stairs";
   private static final String MODE_TUNNELS_STAIRS = "Tunnels + Stairs";
   private static final String MODE_HOLES = "Holes";
   private static final String MODE_TUNNELS = "Tunnels";
   private static final String MODE_STAIRS = "Stairs";
   private static final int TYPE_HOLE = 0;
   private static final int TYPE_HOLE_3X1 = 1;
   private static final int TYPE_TUNNEL = 2;
   private static final int TYPE_STAIR = 3;
   private static final int MAX_HITS_PER_CHUNK = 1024;
   private static final int MAX_RENDER_BOXES = 8192;
   private static final int HOLE_GRID_W = 20;
   private static final float ELYTRA_SCAN_BUDGET_MS = 8.0F;
   private static final int[][] HORIZONTAL_DIRS = new int[][]{{1, 0}, {-1, 0}, {0, -1}, {0, 1}};
   private static final ThreadLocal<Mutable> PASSABLE_POS = ThreadLocal.withInitial(Mutable::new);
   private final ConcurrentHashMap<Long, List<HoleEspModule.a>> chunkCache = new ConcurrentHashMap<>();
   private final Queue<Long> scanQueue = new ConcurrentLinkedQueue<>();
   private final Set<Long> queued = ConcurrentHashMap.newKeySet();
   private long lastPlayerChunk = Long.MIN_VALUE;
   private final TimeSlice scanSlice = new TimeSlice();
   private boolean tickFastTravel;
   private int lastScanRadius = Integer.MIN_VALUE;
   private final EnumSetting sMode = this.addSetting(
      new EnumSetting("Detection", "Holes", "Holes", "All", "Holes + Tunnels", "Holes + Stairs", "Tunnels + Stairs", "Tunnels", "Stairs")
   );
   private final NumberSetting sChunksPerTick = this.addSetting(new NumberSetting("Chunks/Tick", 64.0F, 1.0F, 100.0F));
   private final NumberSetting sScanBudgetMs = this.addSetting(new NumberSetting("Scan Budget ms", 3.0F, 1.0F, 10.0F));
   private final BooleanSetting sAirOnly = this.addSetting(new BooleanSetting("Only Air Blocks", true));
   private final NumberSetting sMinYOffset = this.addSetting(new NumberSetting("Min Y Offset", 0.0F, 0.0F, 319.0F));
   private final NumberSetting sMaxYOffset = this.addSetting(new NumberSetting("Max Y Offset", 0.0F, 0.0F, 319.0F));
   private final NumberSetting sMinHoleDepth = this.addSetting(new NumberSetting("Min Hole Depth", 4.0F, 1.0F, 20.0F));
   private final NumberSetting sMinTunnelLength = this.addSetting(new NumberSetting("Min Tunnel Length", 3.0F, 1.0F, 20.0F));
   private final NumberSetting sMinTunnelHeight = this.addSetting(new NumberSetting("Min Tunnel Height", 2.0F, 1.0F, 10.0F));
   private final NumberSetting sMaxTunnelHeight = this.addSetting(new NumberSetting("Max Tunnel Height", 3.0F, 2.0F, 10.0F));
   private final NumberSetting sMinStairLength = this.addSetting(new NumberSetting("Min Stair Length", 3.0F, 1.0F, 20.0F));
   private final NumberSetting sMinStairHeight = this.addSetting(new NumberSetting("Min Stair Height", 3.0F, 2.0F, 10.0F));
   private final NumberSetting sMaxStairHeight = this.addSetting(new NumberSetting("Max Stair Height", 5.0F, 2.0F, 10.0F));
   private final BooleanSetting sFill = this.addSetting(new BooleanSetting("Fill", true));
   private final BooleanSetting sOutline = this.addSetting(new BooleanSetting("Outline", true));
   private final NumberSetting sLineWidth = this.addSetting(new NumberSetting("Line Width", 1.5F, 0.5F, 5.0F));
   private static final int WHITE_LINE = 1610612735;
   private static final int WHITE_FILL = 654311423;
   private final ColorSetting sHoleLine = this.addSetting(new ColorSetting("1x1 Line", 1610612735));
   private final ColorSetting sHoleFill = this.addSetting(new ColorSetting("1x1 Fill", 654311423));
   private final ColorSetting sHole3Line = this.addSetting(new ColorSetting("3x1 Line", 1610612735));
   private final ColorSetting sHole3Fill = this.addSetting(new ColorSetting("3x1 Fill", 654311423));
   private final ColorSetting sTunnelLine = this.addSetting(new ColorSetting("Tunnel Line", 1610612735));
   private final ColorSetting sTunnelFill = this.addSetting(new ColorSetting("Tunnel Fill", 654311423));
   private final ColorSetting sStairLine = this.addSetting(new ColorSetting("Stair Line", 1610612735));
   private final ColorSetting sStairFill = this.addSetting(new ColorSetting("Stair Fill", 654311423));
   private final NumberSetting sColorScheme = this.addSetting(new NumberSetting("Color Scheme", 1.0F, 0.0F, 10.0F));

   public HoleEspModule() {
      super("Hole ESP", "Highlights holes, tunnels, and staircases.", ModuleCategory.RENDER);
      this.sColorScheme.visibleWhen(() -> false);

      for (Setting var4 : new Setting[]{
         this.sMode,
         this.sAirOnly,
         this.sMinYOffset,
         this.sMaxYOffset,
         this.sMinHoleDepth,
         this.sMinTunnelLength,
         this.sMinTunnelHeight,
         this.sMaxTunnelHeight,
         this.sMinStairLength,
         this.sMinStairHeight,
         this.sMaxStairHeight
      }) {
         var4.onChange(this::a);
      }
   }

   private void a() {
      this.chunkCache.clear();
      this.scanQueue.clear();
      this.queued.clear();
      this.lastPlayerChunk = Long.MIN_VALUE;
      this.lastScanRadius = Integer.MIN_VALUE;
   }

   @Override
   protected void onEnable() {
      this.lastPlayerChunk = Long.MIN_VALUE;
      this.lastScanRadius = Integer.MIN_VALUE;
      this.chunkCache.clear();
      this.scanQueue.clear();
      this.queued.clear();
   }

   @Override
   protected void onDisable() {
      this.chunkCache.clear();
      this.scanQueue.clear();
      this.queued.clear();
      this.lastPlayerChunk = Long.MIN_VALUE;
      this.lastScanRadius = Integer.MIN_VALUE;
   }

   @EventListener
   public void onTick(TickEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null) {
         int var3 = var2.player.getBlockX() >> 4;
         int var4 = var2.player.getBlockZ() >> 4;
         long var5 = ChunkPos.toLong(var3, var4);
         int var7 = this.a(var2);
         boolean var8 = b(var2);
         boolean var9 = var5 != this.lastPlayerChunk || var7 != this.lastScanRadius;
         if (var9) {
            this.lastPlayerChunk = var5;
            this.lastScanRadius = var7;
            this.chunkCache.keySet().removeIf(long_ -> {
               int var5x = a(long_);
               int var6 = b(long_);
               return Math.abs(var5x - var3) > var7 || Math.abs(var6 - var4) > var7 || !var2.world.getChunkManager().isChunkLoaded(var5x, var6);
            });
            ArrayList<long[]> var10 = new ArrayList<>();
            Vec3d var11 = var2.player.getVelocity();
            double var12 = var11.x;
            double var14 = var11.z;
            double var16 = Math.sqrt(var12 * var12 + var14 * var14);
            if (var16 > 1.0E-4) {
               var12 /= var16;
               var14 /= var16;
            }

            for (int var18 = var3 - var7; var18 <= var3 + var7; var18++) {
               for (int var19 = var4 - var7; var19 <= var4 + var7; var19++) {
                  long var20 = ChunkPos.toLong(var18, var19);
                  if (!this.queued.contains(var20) && !this.chunkCache.containsKey(var20) && var2.world.getChunkManager().isChunkLoaded(var18, var19)) {
                     int var22 = var18 - var3;
                     int var23 = var19 - var4;
                     long var24 = (var22 * var22 + var23 * var23) * 1024L;
                     long var26 = var8 ? Math.round((var22 * var12 + var23 * var14) * 1536.0) : 0L;
                     var10.add(new long[]{var20, var24 - var26});
                  }
               }
            }

            var10.sort(Comparator.comparingLong(ls -> ls[1]));

            for (long[] var29 : (Iterable<long[]>) var10) {
               this.scanQueue.add(var29[0]);
               this.queued.add(var29[0]);
            }
         }

         this.tickFastTravel = var8;
      }
   }

   @EventListener
   public void onScanFrame(ScanFrameEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null && !this.scanQueue.isEmpty()) {
         int var3 = Math.max(1, Math.round(a(this.sChunksPerTick, 4.0F)));
         float var4 = this.tickFastTravel ? Math.max(a(this.sScanBudgetMs, 3.0F), 8.0F) : a(this.sScanBudgetMs, 3.0F);
         long var5 = (long)(Math.max(1.0F, var4) * 1000000.0F);
         if (this.scanSlice.begin(var5, var3)) {
            try {
               long var7 = this.scanSlice.deadline();
               int var9 = this.scanSlice.unitsLeft();
               int var10 = 0;

               while (var10 < var9 && System.nanoTime() < var7) {
                  Long var11 = this.scanQueue.poll();
                  if (var11 == null) {
                     break;
                  }

                  if (this.queued.remove(var11)) {
                     var10++;
                     int var12 = a(var11);
                     int var13 = b(var11);
                     if (var2.world.getChunkManager().isChunkLoaded(var12, var13)) {
                        this.a(var2.world, var12, var13, var11);
                     }
                  }
               }

               this.scanSlice.useUnits(var10);
            } finally {
               this.scanSlice.end();
            }
         }
      }
   }

   @EventListener
   public void onPacketReceive(PacketReceiveEvent arg) {
      if (arg.packet instanceof ChunkDataS2CPacket var2) {
         long var6 = ChunkPos.toLong(var2.getChunkX(), var2.getChunkZ());
         if (this.queued.add(var6)) {
            this.scanQueue.add(var6);
         }
      } else if (arg.packet instanceof UnloadChunkS2CPacket var3) {
         long var8 = var3.pos().toLong();
         this.chunkCache.remove(var8);
         this.queued.remove(var8);
      }
   }

   @EventListener
   public void onBlockUpdate(BlockUpdateEvent arg) {
      if (arg.pos != null) {
         long var2 = ChunkPos.toLong(arg.pos.getX() >> 4, arg.pos.getZ() >> 4);
         if (this.queued.add(var2)) {
            this.scanQueue.add(var2);
         }
      }
   }

   @EventListener
   public void onRender3D(Render3DEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null) {
         boolean var3 = this.sFill == null || this.sFill.value().get();
         boolean var4 = this.sOutline == null || this.sOutline.value().get();
         if (var3 || var4) {
            int var5 = var2.player.getBlockX() >> 4;
            int var6 = var2.player.getBlockZ() >> 4;
            int var7 = this.a(var2);
            float var8 = a(this.sLineWidth, 1.5F);
            int var9 = 0;

            for (Entry var11 : this.chunkCache.entrySet()) {
               long var12 = (Long)var11.getKey();
               if (Math.abs(a(var12) - var5) <= var7
                  && Math.abs(b(var12) - var6) <= var7
                  && !FrustumCulling.columnOutOfView(a(var12), b(var12), -64, 320)) {
                  for (HoleEspModule.a var15 : (Iterable<HoleEspModule.a>) (List)var11.getValue()) {
                     if (++var9 > 8192) {
                        return;
                     }

                     int var16 = this.a(var15.type());
                     Integer var17 = var3 ? this.b(var15.type()) : null;
                     GizmoBatch.submitCuboid(var15.key(), var15.box(), var4 ? var16 : 0, var8, var17, true);
                  }
               }
            }
         }
      }
   }

   private void a(ClientWorld clientWorld, int i, int j, long l) {
      WorldChunk var6 = clientWorld.getChunkManager().getWorldChunk(i, j);
      if (var6 != null) {
         int var7 = i << 4;
         int var8 = j << 4;
         int var9 = clientWorld.getDimension().minY();
         int var10 = var9 + var6.getSectionArray().length * 16;
         int var11 = Math.min(var10 - 1, var9 + Math.round(a(this.sMinYOffset, 0.0F)));
         int var12 = Math.max(var11 + 1, var10 - Math.round(a(this.sMaxYOffset, 0.0F)));
         HoleEspModule.b var13 = this.b();
         boolean var14 = this.sAirOnly != null && this.sAirOnly.value().get();
         if (var13.holes() && !var13.tunnels() && !var13.stairs()) {
            this.a(clientWorld, l, var7, var8, var11, var12, var14);
         } else {
            ArrayList var15 = new ArrayList();
            HashSet var16 = new HashSet();
            Mutable var17 = new Mutable();
            ChunkSection[] var18 = var6.getSectionArray();

            for (int var19 = 0; var19 < var18.length; var19++) {
               ChunkSection var20 = var18[var19];
               if (var20 != null && !var20.isEmpty()) {
                  int var21 = var9 + var19 * 16;

                  for (int var22 = 0; var22 < 16; var22++) {
                     int var23 = var21 + var22;
                     if (var23 > var11 && var23 < var12) {
                        for (int var24 = 0; var24 < 16; var24++) {
                           for (int var25 = 0; var25 < 16; var25++) {
                              int var26 = var7 + var25;
                              int var27 = var8 + var24;
                              var17.set(var26, var23, var27);
                              if (f(clientWorld, var17, var14)) {
                                 if (var13.holes() && !a(clientWorld, var26, var23 - 1, var27, var14)) {
                                    this.a(clientWorld, var17, var14, var15, var16);
                                    this.b(clientWorld, var17, var14, var15, var16);
                                 }

                                 if (var13.tunnels()) {
                                    this.c(clientWorld, var17, var14, var15, var16);
                                 }

                                 if (var13.stairs()) {
                                    this.d(clientWorld, var17, var14, var15, var16);
                                 }

                                 if (var15.size() >= 1024) {
                                    this.chunkCache.put(l, List.copyOf(var15));
                                    return;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            this.chunkCache.put(l, var15.isEmpty() ? List.of() : List.copyOf(var15));
         }
      }
   }

   private void a(ClientWorld clientWorld, long l, int i, int j, int k, int m, boolean bl) {
      int var9 = k - 1;
      int var11 = m - var9 + 1;
      if (var11 <= 1) {
         this.chunkCache.put(l, List.of());
      } else {
         boolean[] var12 = new boolean[var11 * 20 * 20];
         Mutable var13 = new Mutable();

         for (int var14 = 0; var14 < var11; var14++) {
            int var15 = var9 + var14;
            int var16 = var14 * 20 * 20;

            for (int var17 = -1; var17 <= 18; var17++) {
               int var18 = var16 + (var17 + 1) * 20;

               for (int var19 = -1; var19 <= 18; var19++) {
                  var13.set(i + var19, var15, j + var17);
                  var12[var18 + var19 + 1] = f(clientWorld, var13, bl);
               }
            }
         }

         int var23 = Math.round(a(this.sMinHoleDepth, 4.0F));
         ArrayList var24 = new ArrayList();
         HashSet var25 = new HashSet();

         for (int var26 = 0; var26 < 16; var26++) {
            for (int var27 = 0; var27 < 16; var27++) {
               int var28 = k;

               while (var28 < m) {
                  if (a(var12, var28, var27, var26, var9) && !a(var12, var28 - 1, var27, var26, var9)) {
                     int var20 = a(var12, var28, var27, var26, var9, m);
                     if (var20 >= var23) {
                        a(var24, var25, new Box(i + var27, var28, j + var26, i + var27 + 1, var28 + var20, j + var26 + 1), 0);
                        if (var24.size() >= 1024) {
                           this.chunkCache.put(l, List.copyOf(var24));
                           return;
                        }
                     }

                     int var21 = b(var12, var28, var27, var26, var9, m);
                     if (var21 >= var23) {
                        a(var24, var25, new Box(i + var27, var28, j + var26, i + var27 + 3, var28 + var21, j + var26 + 1), 1);
                        if (var24.size() >= 1024) {
                           this.chunkCache.put(l, List.copyOf(var24));
                           return;
                        }
                     }

                     int var22 = c(var12, var28, var27, var26, var9, m);
                     if (var22 >= var23) {
                        a(var24, var25, new Box(i + var27, var28, j + var26, i + var27 + 1, var28 + var22, j + var26 + 3), 1);
                        if (var24.size() >= 1024) {
                           this.chunkCache.put(l, List.copyOf(var24));
                           return;
                        }
                     }

                     var28 += Math.max(1, Math.max(var20, Math.max(var21, var22)));
                  } else {
                     var28++;
                  }
               }
            }
         }

         this.chunkCache.put(l, var24.isEmpty() ? List.of() : List.copyOf(var24));
      }
   }

   private static int a(boolean[] bls, int i, int j, int k, int l, int m) {
      int var6 = i;

      while (
         var6 < m
            && a(bls, var6, j, k, l)
            && !a(bls, var6, j - 1, k, l)
            && !a(bls, var6, j + 1, k, l)
            && !a(bls, var6, j, k - 1, l)
            && !a(bls, var6, j, k + 1, l)
      ) {
         var6++;
      }

      return var6 - i;
   }

   private static int b(boolean[] bls, int i, int j, int k, int l, int m) {
      int var6 = i;

      while (
         var6 < m
            && a(bls, var6, j, k, l)
            && a(bls, var6, j + 1, k, l)
            && a(bls, var6, j + 2, k, l)
            && !a(bls, var6, j - 1, k, l)
            && !a(bls, var6, j + 3, k, l)
            && !a(bls, var6, j, k - 1, l)
            && !a(bls, var6, j, k + 1, l)
            && !a(bls, var6, j + 1, k - 1, l)
            && !a(bls, var6, j + 1, k + 1, l)
            && !a(bls, var6, j + 2, k - 1, l)
            && !a(bls, var6, j + 2, k + 1, l)
      ) {
         var6++;
      }

      return var6 - i;
   }

   private static int c(boolean[] bls, int i, int j, int k, int l, int m) {
      int var6 = i;

      while (
         var6 < m
            && a(bls, var6, j, k, l)
            && a(bls, var6, j, k + 1, l)
            && a(bls, var6, j, k + 2, l)
            && !a(bls, var6, j, k - 1, l)
            && !a(bls, var6, j, k + 3, l)
            && !a(bls, var6, j - 1, k, l)
            && !a(bls, var6, j + 1, k, l)
            && !a(bls, var6, j - 1, k + 1, l)
            && !a(bls, var6, j + 1, k + 1, l)
            && !a(bls, var6, j - 1, k + 2, l)
            && !a(bls, var6, j + 1, k + 2, l)
      ) {
         var6++;
      }

      return var6 - i;
   }

   private static boolean a(boolean[] bls, int i, int j, int k, int l) {
      int var5 = i - l;
      return bls[var5 * 20 * 20 + (k + 1) * 20 + j + 1];
   }

   private void a(ClientWorld clientWorld, BlockPos blockPos, boolean bl, List<HoleEspModule.a> list, Set<String> set) {
      if (this.a(clientWorld, blockPos, bl)) {
         Mutable var6 = new Mutable(blockPos.getX(), blockPos.getY(), blockPos.getZ());

         while (this.a(clientWorld, var6, bl)) {
            var6.move(0, 1, 0);
         }

         int var7 = var6.getY() - blockPos.getY();
         if (var7 >= Math.round(a(this.sMinHoleDepth, 4.0F))) {
            a(list, set, new Box(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, var6.getY(), blockPos.getZ() + 1), 0);
         }
      }
   }

   private void b(ClientWorld clientWorld, BlockPos blockPos, boolean bl, List<HoleEspModule.a> list, Set<String> set) {
      if (this.b(clientWorld, blockPos, bl)) {
         Mutable var6 = new Mutable(blockPos.getX(), blockPos.getY(), blockPos.getZ());

         while (this.b(clientWorld, var6, bl)) {
            var6.move(0, 1, 0);
         }

         int var7 = var6.getY() - blockPos.getY();
         if (var7 >= Math.round(a(this.sMinHoleDepth, 4.0F))) {
            a(list, set, new Box(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 3, var6.getY(), blockPos.getZ() + 1), 1);
         }
      }

      if (this.c(clientWorld, blockPos, bl)) {
         Mutable var8 = new Mutable(blockPos.getX(), blockPos.getY(), blockPos.getZ());

         while (this.c(clientWorld, var8, bl)) {
            var8.move(0, 1, 0);
         }

         int var9 = var8.getY() - blockPos.getY();
         if (var9 >= Math.round(a(this.sMinHoleDepth, 4.0F))) {
            a(list, set, new Box(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, var8.getY(), blockPos.getZ() + 3), 1);
         }
      }
   }

   private void c(ClientWorld clientWorld, BlockPos blockPos, boolean bl, List<HoleEspModule.a> list, Set<String> set) {
      int var6 = Math.round(a(this.sMinTunnelLength, 3.0F));

      for (int[] var10 : HORIZONTAL_DIRS) {
         Mutable var11 = new Mutable(blockPos.getX() - var10[0], blockPos.getY(), blockPos.getZ() - var10[1]);
         if (!this.a(clientWorld, var11, var10, bl)) {
            Mutable var12 = new Mutable(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            BlockPos var13 = null;
            BlockPos var14 = null;
            int var15 = 0;
            int var16 = 0;
            if (this.a(clientWorld, var12, var10, bl)) {
               var13 = var12.toImmutable();
            }

            while (this.a(clientWorld, var12, var10, bl)) {
               var16 = Math.max(var16, this.d(clientWorld, var12, bl));
               var14 = var12.toImmutable();
               var12.move(var10[0], 0, var10[1]);
               var15++;
            }

            if (var13 != null && var14 != null && var15 >= var6) {
               a(
                  list,
                  set,
                  new Box(
                     Math.min(var13.getX(), var14.getX()),
                     var13.getY(),
                     Math.min(var13.getZ(), var14.getZ()),
                     Math.max(var13.getX(), var14.getX()) + 1,
                     var13.getY() + var16,
                     Math.max(var13.getZ(), var14.getZ()) + 1
                  ),
                  2
               );
            }
         }
      }
   }

   private void d(ClientWorld clientWorld, BlockPos blockPos, boolean bl, List<HoleEspModule.a> list, Set<String> set) {
      int var6 = Math.round(a(this.sMinStairLength, 3.0F));

      for (int[] var10 : HORIZONTAL_DIRS) {
         Mutable var11 = new Mutable(blockPos.getX() - var10[0], blockPos.getY() - 1, blockPos.getZ() - var10[1]);
         if (!this.b(clientWorld, var11, var10, bl)) {
            Mutable var12 = new Mutable(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            int var13 = 0;

            ArrayList var14;
            for (var14 = new ArrayList(); this.b(clientWorld, var12, var10, bl); var13++) {
               int var15 = this.e(clientWorld, var12, bl);
               var14.add(new Box(var12.getX(), var12.getY(), var12.getZ(), var12.getX() + 1, var12.getY() + var15, var12.getZ() + 1));
               var12.move(var10[0], 1, var10[1]);
            }

            if (var13 >= var6) {
               for (Box var16 : (Iterable<Box>) var14) {
                  a(list, set, var16, 3);
               }
            }
         }
      }
   }

   private boolean a(ClientWorld clientWorld, BlockPos blockPos, boolean bl) {
      return f(clientWorld, blockPos, bl)
         && !a(clientWorld, blockPos.getX(), blockPos.getY(), blockPos.getZ() - 1, bl)
         && !a(clientWorld, blockPos.getX(), blockPos.getY(), blockPos.getZ() + 1, bl)
         && !a(clientWorld, blockPos.getX() + 1, blockPos.getY(), blockPos.getZ(), bl)
         && !a(clientWorld, blockPos.getX() - 1, blockPos.getY(), blockPos.getZ(), bl);
   }

   private boolean b(ClientWorld clientWorld, BlockPos blockPos, boolean bl) {
      int var4 = blockPos.getX();
      int var5 = blockPos.getY();
      int var6 = blockPos.getZ();
      return a(clientWorld, var4, var5, var6, bl)
         && a(clientWorld, var4 + 1, var5, var6, bl)
         && a(clientWorld, var4 + 2, var5, var6, bl)
         && !a(clientWorld, var4, var5, var6 - 1, bl)
         && !a(clientWorld, var4, var5, var6 + 1, bl)
         && !a(clientWorld, var4 + 1, var5, var6 - 1, bl)
         && !a(clientWorld, var4 + 1, var5, var6 + 1, bl)
         && !a(clientWorld, var4 + 2, var5, var6 - 1, bl)
         && !a(clientWorld, var4 + 2, var5, var6 + 1, bl)
         && !a(clientWorld, var4 - 1, var5, var6, bl)
         && !a(clientWorld, var4 + 3, var5, var6, bl);
   }

   private boolean c(ClientWorld clientWorld, BlockPos blockPos, boolean bl) {
      int var4 = blockPos.getX();
      int var5 = blockPos.getY();
      int var6 = blockPos.getZ();
      return a(clientWorld, var4, var5, var6, bl)
         && a(clientWorld, var4, var5, var6 + 1, bl)
         && a(clientWorld, var4, var5, var6 + 2, bl)
         && !a(clientWorld, var4 - 1, var5, var6, bl)
         && !a(clientWorld, var4 + 1, var5, var6, bl)
         && !a(clientWorld, var4 - 1, var5, var6 + 1, bl)
         && !a(clientWorld, var4 + 1, var5, var6 + 1, bl)
         && !a(clientWorld, var4 - 1, var5, var6 + 2, bl)
         && !a(clientWorld, var4 + 1, var5, var6 + 2, bl)
         && !a(clientWorld, var4, var5, var6 - 1, bl)
         && !a(clientWorld, var4, var5, var6 + 3, bl);
   }

   private boolean a(ClientWorld clientWorld, BlockPos blockPos, int[] is, boolean bl) {
      int var5 = this.d(clientWorld, blockPos, bl);
      if (var5 >= Math.round(a(this.sMinTunnelHeight, 2.0F)) && var5 <= Math.round(a(this.sMaxTunnelHeight, 3.0F))) {
         if (!a(clientWorld, blockPos.getX(), blockPos.getY() - 1, blockPos.getZ(), bl)
            && !a(clientWorld, blockPos.getX(), blockPos.getY() + var5, blockPos.getZ(), bl)) {
            boolean var6 = is[0] != 0;

            for (int var7 = 0; var7 < var5; var7++) {
               int var8 = blockPos.getY() + var7;
               if (var6) {
                  if (a(clientWorld, blockPos.getX(), var8, blockPos.getZ() - 1, bl) || a(clientWorld, blockPos.getX(), var8, blockPos.getZ() + 1, bl)) {
                     return false;
                  }
               } else if (a(clientWorld, blockPos.getX() - 1, var8, blockPos.getZ(), bl) || a(clientWorld, blockPos.getX() + 1, var8, blockPos.getZ(), bl)) {
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

   private boolean b(ClientWorld clientWorld, BlockPos blockPos, int[] is, boolean bl) {
      int var5 = this.e(clientWorld, blockPos, bl);
      if (var5 >= Math.round(a(this.sMinStairHeight, 3.0F)) && var5 <= Math.round(a(this.sMaxStairHeight, 5.0F))) {
         if (!a(clientWorld, blockPos.getX(), blockPos.getY() - 1, blockPos.getZ(), bl)
            && !a(clientWorld, blockPos.getX(), blockPos.getY() + var5, blockPos.getZ(), bl)) {
            boolean var6 = is[0] != 0;

            for (int var7 = 0; var7 < var5; var7++) {
               int var8 = blockPos.getY() + var7;
               if (var6) {
                  if (a(clientWorld, blockPos.getX(), var8, blockPos.getZ() - 1, bl) || a(clientWorld, blockPos.getX(), var8, blockPos.getZ() + 1, bl)) {
                     return false;
                  }
               } else if (a(clientWorld, blockPos.getX() - 1, var8, blockPos.getZ(), bl) || a(clientWorld, blockPos.getX() + 1, var8, blockPos.getZ(), bl)) {
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

   private int d(ClientWorld clientWorld, BlockPos blockPos, boolean bl) {
      int var4 = 0;
      int var5 = Math.round(a(this.sMaxTunnelHeight, 3.0F));

      while (var4 < var5 && a(clientWorld, blockPos.getX(), blockPos.getY() + var4, blockPos.getZ(), bl)) {
         var4++;
      }

      return var4;
   }

   private int e(ClientWorld clientWorld, BlockPos blockPos, boolean bl) {
      int var4 = 0;
      int var5 = Math.round(a(this.sMaxStairHeight, 5.0F));

      while (var4 < var5 && a(clientWorld, blockPos.getX(), blockPos.getY() + var4, blockPos.getZ(), bl)) {
         var4++;
      }

      return var4;
   }

   private static boolean a(ClientWorld clientWorld, int i, int j, int k, boolean bl) {
      return f(clientWorld, PASSABLE_POS.get().set(i, j, k), bl);
   }

   private static boolean f(ClientWorld clientWorld, BlockPos blockPos, boolean bl) {
      BlockState var3 = clientWorld.getBlockState(blockPos);
      if (var3.isAir()) {
         return true;
      } else if (bl) {
         return false;
      } else {
         VoxelShape var4 = var3.getCollisionShape(clientWorld, blockPos);
         return var4.isEmpty() || !VoxelShapes.fullCube().equals(var4);
      }
   }

   private static void a(List<HoleEspModule.a> list, Set<String> set, Box box, int i) {
      String var4 = i + ":" + box.minX + "," + box.minY + "," + box.minZ + "," + box.maxX + "," + box.maxY + "," + box.maxZ;
      if (set.add(var4)) {
         for (HoleEspModule.a var6 : list) {
            if (var6.type() == i && var6.box().intersects(box)) {
               return;
            }
         }

         list.add(new HoleEspModule.a(box, i, new BlockPos((int)box.minX, (int)box.minY, (int)box.minZ)));
      }
   }

   private HoleEspModule.b b() {
      String var1 = this.sMode == null ? "All" : this.sMode.value();

      return switch (var1) {
         case "Holes + Tunnels" -> new HoleEspModule.b(true, true, false);
         case "Holes + Stairs" -> new HoleEspModule.b(true, false, true);
         case "Tunnels + Stairs" -> new HoleEspModule.b(false, true, true);
         case "Holes" -> new HoleEspModule.b(true, false, false);
         case "Tunnels" -> new HoleEspModule.b(false, true, false);
         case "Stairs" -> new HoleEspModule.b(false, false, true);
         default -> new HoleEspModule.b(true, true, true);
      };
   }

   private int a(int i) {
      return switch (i) {
         case 1 -> a(this.sHole3Line, 1610589440);
         case 2 -> a(this.sTunnelLine, 1593835775);
         case 3 -> a(this.sStairLine, 1610547455);
         default -> a(this.sHoleLine, 1610547200);
      };
   }

   private int b(int i) {
      return switch (i) {
         case 1 -> a(this.sHole3Fill, 520070400);
         case 2 -> a(this.sTunnelFill, 503316735);
         case 3 -> a(this.sStairFill, 520028415);
         default -> a(this.sHoleFill, 520028160);
      };
   }

   private static int a(long l) {
      return (int)l;
   }

   private static int b(long l) {
      return (int)(l >> 32);
   }

   private int a(MinecraftClient minecraftClient) {
      int var2 = ViewDistance.chunks();
      return Math.min(var2, var2);
   }

   private static boolean b(MinecraftClient minecraftClient) {
      if (minecraftClient.player == null) {
         return false;
      } else {
         Vec3d var1 = minecraftClient.player.getVelocity();
         double var2 = var1.x * var1.x + var1.z * var1.z;
         return minecraftClient.player.isGliding() || var2 > 0.18;
      }
   }

   private static float a(NumberSetting arg, float f) {
      return arg == null ? f : arg.value()[0];
   }

   @Override
   public void afterSettingsLoaded(JsonObject jsonObject) {
      if (!jsonObject.has(this.sColorScheme.name()) || !(this.sColorScheme.get() >= 1.0F)) {
         for (ColorSetting var5 : new ColorSetting[]{this.sHoleLine, this.sHole3Line, this.sTunnelLine, this.sStairLine}) {
            var5.setArgb(1610612735);
         }

         for (ColorSetting var9 : new ColorSetting[]{this.sHoleFill, this.sHole3Fill, this.sTunnelFill, this.sStairFill}) {
            var9.setArgb(654311423);
         }

         this.sColorScheme.set(1.0F);
      }
   }

   private static int a(ColorSetting arg, int i) {
      return arg == null ? i : arg.argb();
   }

   @Environment(EnvType.CLIENT)
   private record a(Box box, int type, BlockPos key) {
   }

   @Environment(EnvType.CLIENT)
   private record b(boolean holes, boolean tunnels, boolean stairs) {
   }
}
