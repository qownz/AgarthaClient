package net.agartha.client.module.impl.render;

import imgui.ImDrawList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.agartha.client.d.a_pkg.o;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.module.b;
import net.agartha.client.module.e;
import net.agartha.client.module.f;
import net.agartha.client.module.a_pkg.c;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.util.ac;
import net.agartha.client.util.ad;
import net.agartha.client.util.af;
import net.agartha.client.util.i;
import net.agartha.client.util.l;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

@Environment(EnvType.CLIENT)
public final class NetheriteFinderModule extends b implements f {
   private static final float TRACER_WIDTH = 1.5F;
   private static final int SCAN_INTERVAL_TICKS = 20;
   private static final int DEFAULT_COLOR = -4302081;
   private static final float LINE_WIDTH = 1.5F;
   private static final float SECTION_LINE_WIDTH = 1.0F;
   private static final int EXACT_FILL_ALPHA = 60;
   private static final long PULSE_PERIOD_MS = 2000L;
   private final net.agartha.client.module.a_pkg.b sSectionHints = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Section Hints", true)
   );
   private final net.agartha.client.module.a_pkg.b sItemEsp = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Item ESP", true));
   private final net.agartha.client.module.a_pkg.b sPulse = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Pulse", true));
   private final net.agartha.client.module.a_pkg.b sTracers = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Tracers", false));
   private final j sMaxShown = (j)this.addSetting(new j("Max Shown", 100.0F, 1.0F, 1000.0F));
   private final net.agartha.client.module.a_pkg.b sCustomColor = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Custom Color", false)
   );
   private final net.agartha.client.module.a_pkg.b sThemeColor = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Theme Color", true)
   );
   private final c sColor = (c)this.addSetting(new c("Color", -4302081));
   private volatile List<NetheriteFinderModule.Hit> hits = List.of();
   private final AtomicBoolean scanning = new AtomicBoolean();
   private volatile ExecutorService worker;
   private int tickCounter;

   public NetheriteFinderModule() {
      super("Netherite Finder", "Finds ancient debris.", net.agartha.client.module.c.DONUT);
      ac.wireExclusiveColorSource(this.sCustomColor, this.sThemeColor, this.sColor, true);
   }

   protected void onEnable() {
      this.hits = List.of();
      this.tickCounter = 0;
      this.scanning.set(false);
      this.worker = Executors.newSingleThreadExecutor(runnable -> {
         Thread var1 = new Thread(runnable, "Agartha-NetheriteFinder-Worker");
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

      this.hits = List.of();
      this.scanning.set(false);
   }

   public static boolean isGlowPathActive() {
      NetheriteFinderModule var0 = (NetheriteFinderModule)e.get(NetheriteFinderModule.class);
      return var0 != null && var0.isEnabled() && var0.sItemEsp.get();
   }

   public static boolean shouldGlow(Entity entity) {
      NetheriteFinderModule var1 = (NetheriteFinderModule)e.get(NetheriteFinderModule.class);
      return var1 != null && var1.outlines(entity);
   }

   public static int glowColor(Entity entity) {
      NetheriteFinderModule var1 = (NetheriteFinderModule)e.get(NetheriteFinderModule.class);
      return var1 == null ? 16777215 : var1.resolvedColor() & 16777215;
   }

   private boolean outlines(Entity entity) {
      if (this.isEnabled() && this.sItemEsp.get()) {
         if (!(entity instanceof ItemEntity var2 && !var2.isRemoved())) {
            return false;
         } else if (!var2.getStack().isOf(Items.ANCIENT_DEBRIS)) {
            return false;
         } else {
            MinecraftClient var3 = MinecraftClient.getInstance();
            if (var3.world == null || var3.player == null) {
               return false;
            } else if (var3.getEntityRenderDispatcher().getRenderer(entity) == null) {
               return false;
            } else {
               double var4 = af.blocks();
               return var2.getEntityPos().squaredDistanceTo(var3.player.getCameraPosVec(1.0F)) <= var4 * var4 && i.drawnByGame(var2);
            }
         }
      } else {
         return false;
      }
   }

   @net.agartha.client.d.f
   public void onTick(q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientWorld var3 = var2.world;
      if (var3 != null && var2.player != null) {
         if (++this.tickCounter >= 20) {
            this.tickCounter = 0;
            ExecutorService var4 = this.worker;
            if (var4 != null && !var4.isShutdown()) {
               if (this.scanning.compareAndSet(false, true)) {
                  NetheriteFinderModule.ScanRequest var5 = this.collectChunks(var2, var3);

                  try {
                     var4.execute(() -> this.runScan(var5));
                  } catch (RuntimeException var7) {
                     this.scanning.set(false);
                  }
               }
            }
         }
      }
   }

   private NetheriteFinderModule.ScanRequest collectChunks(MinecraftClient minecraftClient, ClientWorld clientWorld) {
      int var3 = af.chunks();
      ChunkPos var4 = minecraftClient.player.getChunkPos();
      ArrayList var5 = new ArrayList();

      for (int var6 = var4.x - var3; var6 <= var4.x + var3; var6++) {
         for (int var7 = var4.z - var3; var7 <= var4.z + var3; var7++) {
            if (clientWorld.getChunkManager().isChunkLoaded(var6, var7)) {
               WorldChunk var8 = clientWorld.getChunkManager().getWorldChunk(var6, var7);
               if (var8 != null) {
                  var5.add(var8);
               }
            }
         }
      }

      return new NetheriteFinderModule.ScanRequest(
         var5, clientWorld.getBottomY(), minecraftClient.player.getX(), minecraftClient.player.getY(), minecraftClient.player.getZ()
      );
   }

   private void runScan(NetheriteFinderModule.ScanRequest scanRequest) {
      try {
         boolean var2 = this.sSectionHints.get();
         ArrayList var3 = new ArrayList();

         for (WorldChunk var5 : scanRequest.chunks()) {
            scanChunk(var5, scanRequest, var2, var3);
         }

         var3.sort(Comparator.<NetheriteFinderModule.Hit>comparingInt(hit -> hit.exact() ? 0 : 1).thenComparingDouble(NetheriteFinderModule.Hit::distSq));
         int var11 = Math.min(var3.size(), this.sMaxShown.getInt());
         if (this.isEnabled()) {
            this.hits = List.copyOf(var3.subList(0, var11));
         }
      } catch (Throwable var9) {
      } finally {
         this.scanning.set(false);
      }
   }

   private static void scanChunk(WorldChunk worldChunk, NetheriteFinderModule.ScanRequest scanRequest, boolean bl, List<NetheriteFinderModule.Hit> list) {
      ChunkSection[] var4 = worldChunk.getSectionArray();
      ChunkPos var5 = worldChunk.getPos();
      int var6 = var5.getStartX();
      int var7 = var5.getStartZ();

      for (int var8 = 0; var8 < var4.length; var8++) {
         ChunkSection var9 = var4[var8];
         if (var9 != null && !var9.isEmpty() && var9.hasAny(blockState -> blockState.isOf(Blocks.ANCIENT_DEBRIS))) {
            int var10 = scanRequest.minY() + (var8 << 4);
            int var11 = list.size();

            for (int var12 = 0; var12 < 16; var12++) {
               for (int var13 = 0; var13 < 16; var13++) {
                  for (int var14 = 0; var14 < 16; var14++) {
                     BlockState var15 = var9.getBlockState(var14, var12, var13);
                     if (var15.isOf(Blocks.ANCIENT_DEBRIS)) {
                        int var16 = var6 + var14;
                        int var17 = var10 + var12;
                        int var18 = var7 + var13;
                        list.add(hit(var16, var17, var18, var16 + 1, var17 + 1, var18 + 1, true, scanRequest));
                     }
                  }
               }
            }

            if (bl && list.size() == var11) {
               list.add(hit(var6, var10, var7, var6 + 16, var10 + 16, var7 + 16, false, scanRequest));
            }
         }
      }
   }

   private static NetheriteFinderModule.Hit hit(int i, int j, int k, int l, int m, int n, boolean bl, NetheriteFinderModule.ScanRequest scanRequest) {
      double var8 = (i + l) * 0.5 - scanRequest.px();
      double var10 = (j + m) * 0.5 - scanRequest.py();
      double var12 = (k + n) * 0.5 - scanRequest.pz();
      return new NetheriteFinderModule.Hit(i, j, k, l, m, n, bl, var8 * var8 + var10 * var10 + var12 * var12);
   }

   @net.agartha.client.d.f
   public void onRender3D(o arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null) {
         List var3 = this.hits;
         if (!var3.isEmpty()) {
            Camera var4 = var2.gameRenderer.getCamera();
            if (var4 != null && var4.isReady()) {
               Vec3d var5 = var4.getCameraPos();
               double var6 = af.blocksSq();
               int var8 = this.resolvedColor();
               int var9 = withAlpha(var8, 255);
               DrawStyle var10 = DrawStyle.filledAndStroked(var9, 1.5F, withAlpha(var8, this.pulsedAlpha(60)));
               DrawStyle var11 = DrawStyle.stroked(var9, 1.0F);
               int var12 = this.sMaxShown.getInt();
               int var13 = 0;

               for (NetheriteFinderModule.Hit var15 : (Iterable<NetheriteFinderModule.Hit>) var3) {
                  if (var13 >= var12) {
                     break;
                  }

                  if (inRange(var15, var5, var6) && (!var15.exact() || ad.isInFront(var4, center(var15)))) {
                     Box var16 = var15.box();
                     if (net.agartha.client.util.l.isSafeCuboid(var16)) {
                        var13++;
                        GizmoDrawing.box(var16, var15.exact() ? var10 : var11).ignoreOcclusion();
                     }
                  }
               }
            }
         }
      }
   }

   private int resolvedColor() {
      return ac.argb(this.sThemeColor, this.sCustomColor, this.sColor, -4302081);
   }

   private static boolean inRange(NetheriteFinderModule.Hit hit, Vec3d vec3d, double d) {
      double var4 = hit.centerX() - vec3d.x;
      double var6 = hit.centerY() - vec3d.y;
      double var8 = hit.centerZ() - vec3d.z;
      return var4 * var4 + var6 * var6 + var8 * var8 <= d;
   }

   private static Vec3d center(NetheriteFinderModule.Hit hit) {
      return new Vec3d(hit.centerX(), hit.centerY(), hit.centerZ());
   }

   private int pulsedAlpha(int i) {
      if (!this.sPulse.get()) {
         return i;
      } else {
         long var2 = System.currentTimeMillis() % 2000L;
         long var4 = 1000L;
         double var6 = var2 < var4 ? (double)var2 / var4 : (double)(2000L - var2) / var4;
         return (int)Math.round(i * (0.55 + 0.9 * var6));
      }
   }

   private static int withAlpha(int i, int j) {
      return i & 16777215 | Math.max(0, Math.min(255, j)) << 24;
   }

   public boolean isTracerOverlayEnabled() {
      return this.isEnabled() && this.sTracers.get() && (!this.hits.isEmpty() || this.sItemEsp.get());
   }

   public void renderTracerOverlay(ImDrawList imDrawList, float f, float g) {
      if (this.isTracerOverlayEnabled() && imDrawList != null) {
         MinecraftClient var4 = MinecraftClient.getInstance();
         if (var4.world != null && var4.player != null) {
            float var5 = ad.originX(f);
            float var6 = ad.originY("Crosshair", g);
            int var7 = withAlpha(this.resolvedColor(), 255);
            float var8 = 1.5F;
            Vec3d var9 = var4.player.getCameraPosVec(var4.getRenderTickCounter().getTickProgress(false));
            double var10 = af.blocksSq();
            int var12 = this.sMaxShown.getInt();
            int var13 = 0;

            for (NetheriteFinderModule.Hit var15 : this.hits) {
               if (var13 >= var12) {
                  break;
               }

               if (var15.exact() && inRange(var15, var9, var10)) {
                  var13++;
                  ad.draw(imDrawList, var4, f, g, var5, var6, center(var15), var7, var8);
               }
            }

            if (this.sItemEsp.get()) {
               float var17 = var4.getRenderTickCounter().getTickProgress(false);

               for (Entity var16 : var4.world.getEntities()) {
                  if (this.outlines(var16)) {
                     ad.draw(imDrawList, var4, f, g, var5, var6, var16.getLerpedPos(var17), var7, var8);
                  }
               }
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private record Hit(int x1, int y1, int z1, int x2, int y2, int z2, boolean exact, double distSq) {
      Box box() {
         return new Box(this.x1, this.y1, this.z1, this.x2, this.y2, this.z2);
      }

      double centerX() {
         return (this.x1 + this.x2) * 0.5;
      }

      double centerY() {
         return (this.y1 + this.y2) * 0.5;
      }

      double centerZ() {
         return (this.z1 + this.z2) * 0.5;
      }
   }

   @Environment(EnvType.CLIENT)
   private record ScanRequest(List<WorldChunk> chunks, int minY, double px, double py, double pz) {
   }
}
