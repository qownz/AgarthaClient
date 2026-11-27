package net.agartha.client.module.impl.render;

import imgui.ImDrawList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.agartha.client.d.a_pkg.k;
import net.agartha.client.d.a_pkg.o;
import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.module.f;
import net.agartha.client.module.a_pkg.a_o;
import net.agartha.client.module.a_pkg.i;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.util.ad;
import net.agartha.client.util.af;
import net.agartha.client.util.d;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class BlockEntityDebugModule extends b implements f {
   private static final float TRACER_WIDTH = 1.5F;
   private final i sDetection = (i)this.addSetting(new i("Detection", "All", new String[]{"All", "Below Zero", "Below -20", "Below -30"}));
   private final net.agartha.client.module.a_pkg.b sBlockFilter = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Block Filter", false)
   );
   private final net.agartha.client.module.a_pkg.b sTracers = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Tracers", true));
   private final j sBoxSize = (j)this.addSetting(new j("Box Size", 1.0F, 0.1F, 2.0F));
   private final j sLineWidth = (j)this.addSetting(new j("Line Width", 1.5F, 0.5F, 5.0F));
   private final j sFillAlpha = (j)this.addSetting(new j("Fill Alpha", 0.3F, 0.0F, 1.0F));
   private final a_o sBlockColors = (a_o)this.addSetting(a_o.createForBlockEntityDebug());
   private static final long REBUILD_GAP_NS = 250000000L;
   private List<BlockEntityDebugModule.Shown> shown = List.of();
   private long shownVersion = -1L;
   private long shownAt;
   private boolean settingsDirty = true;
   private static final ConcurrentHashMap<Long, Long> OWN_BREAKS = new ConcurrentHashMap<>();
   private static final long OWN_BREAK_MS = 5000L;

   public BlockEntityDebugModule() {
      super("Block Entity Debug", "Highlights hidden block entities, including ones out of sight.", c.DONUT);
      Runnable var1 = () -> this.settingsDirty = true;
      this.sDetection.onChange(var1);
      this.sBlockFilter.onChange(var1);
      this.sBoxSize.onChange(var1);
      this.sBlockColors.onChange(var1);
   }

   private List<BlockEntityDebugModule.Shown> shown() {
      long var1 = d.version();
      long var3 = System.nanoTime();
      if (this.settingsDirty || var1 != this.shownVersion && var3 - this.shownAt >= 250000000L) {
         this.settingsDirty = false;
         this.shownVersion = var1;
         this.shownAt = var3;
         float var5 = this.sBoxSize != null ? this.sBoxSize.value()[0] : 1.0F;
         ArrayList var6 = new ArrayList();

         for (Entry var8 : d.view().entrySet()) {
            BlockPos var9 = (BlockPos)var8.getKey();
            Block var10 = ((net.agartha.client.util.d.a)var8.getValue()).block();
            if (this.shouldTrack(var9) && !this.shouldSkip(var10)) {
               int var11 = this.sBlockColors != null ? this.sBlockColors.colorArgb(var10) : -65536;
               Box var12 = null;
               if (var10 == Blocks.SPAWNER) {
                  double var13 = var9.getY() + 1.0;
                  var12 = new Box(var9.getX() + 0.3, var13, var9.getZ() + 0.3, var9.getX() + 0.7, var13 + 256.0, var9.getZ() + 0.7);
               }

               var6.add(
                  new BlockEntityDebugModule.Shown(
                     var9, var9.getX() + 0.5, var9.getY() + 0.5, var9.getZ() + 0.5, var11, net.agartha.client.util.c.scaledBlockBox(var9, var5), var12
                  )
               );
            }
         }

         this.shown = var6;
         return this.shown;
      } else {
         return this.shown;
      }
   }

   protected void onDisable() {
      OWN_BREAKS.clear();
   }

   @net.agartha.client.d.f
   public void onPacketSend(k arg) {
      if (arg.packet instanceof PlayerActionC2SPacket var2) {
         Action var6 = var2.getAction();
         if (var6 == Action.START_DESTROY_BLOCK || var6 == Action.STOP_DESTROY_BLOCK) {
            long var4 = System.currentTimeMillis();
            if (OWN_BREAKS.size() > 256) {
               OWN_BREAKS.values().removeIf(long_ -> var4 - long_ > 5000L);
            }

            OWN_BREAKS.put(var2.getPos().asLong(), var4);
         }
      }
   }

   public static boolean brokenByYou(BlockPos blockPos) {
      Long var1 = OWN_BREAKS.get(blockPos.asLong());
      return var1 != null && System.currentTimeMillis() - var1 <= 5000L;
   }

   private boolean shouldTrack(BlockPos blockPos) {
      String var2 = this.sDetection == null ? "All" : this.sDetection.value();

      return switch (var2) {
         case "All" -> true;
         case "Below -20" -> blockPos.getY() < -20;
         case "Below -30" -> blockPos.getY() < -30;
         case "Below Zero" -> blockPos.getY() < 0;
         default -> true;
      };
   }

   @net.agartha.client.d.f
   public void onPacketReceiveNetty(net.agartha.client.d.a_pkg.j arg) {
      if (arg.packet instanceof BlockUpdateS2CPacket var2) {
         BlockPos var5 = var2.getPos();
         if (d.contains(var5)) {
            if (!brokenByYou(var5)) {
               BlockState var4 = var2.getState();
               if (var4.isAir() || var4.isOf(Blocks.STONE) || var4.isOf(Blocks.DEEPSLATE)) {
                  arg.setCancelled(true);
               }
            }
         }
      }
   }

   @net.agartha.client.d.f
   public void onRender3D(o arg) {
      if (!d.isEmpty()) {
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (var2.player != null && var2.world != null) {
            float var3 = this.sLineWidth != null ? this.sLineWidth.value()[0] : 1.5F;
            int var4 = Math.round((this.sFillAlpha != null ? this.sFillAlpha.value()[0] : 0.3F) * 255.0F);
            float var5 = var2.getRenderTickCounter().getTickProgress(false);
            Vec3d var6 = var2.player.getCameraPosVec(var5);
            double var7 = this.renderDistance();
            double var9 = var7 * var7;
            List var11 = this.shown();
            int var12 = 0;

            for (int var13 = var11.size(); var12 < var13; var12++) {
               BlockEntityDebugModule.Shown var14 = (BlockEntityDebugModule.Shown)var11.get(var12);
               double var15 = var14.cx() - var6.x;
               double var17 = var14.cz() - var6.z;
               if (!(var15 * var15 + var17 * var17 > var9)) {
                  int var19 = var14.color();
                  Integer var20 = var4 > 0 ? withAlpha(var19, var4) : null;
                  net.agartha.client.util.k.submitCuboid(var14.pos(), var14.box(), var19, var3, var20, true);
                  if (var14.beam() != null) {
                     net.agartha.client.util.k.submitCuboid(var14.pos().up(), var14.beam(), var19, var3, var20, true);
                  }
               }
            }
         }
      }
   }

   public boolean isTracerOverlayEnabled() {
      return this.isEnabled() && !d.isEmpty() && this.sTracers != null && this.sTracers.value().get();
   }

   public void renderTracerOverlay(ImDrawList imDrawList, float f, float g) {
      if (this.isEnabled() && !d.isEmpty() && imDrawList != null) {
         if (this.sTracers != null && this.sTracers.value().get()) {
            MinecraftClient var4 = MinecraftClient.getInstance();
            if (var4.player != null && var4.world != null) {
               float var5 = var4.getRenderTickCounter().getTickProgress(false);
               float var6 = ad.originX(f);
               float var7 = ad.originY("Crosshair", g);
               float var8 = 1.5F;
               Vec3d var9 = var4.player.getCameraPosVec(var5);
               double var10 = this.renderDistance();
               List var12 = this.shown();
               int var13 = 0;

               for (int var14 = var12.size(); var13 < var14; var13++) {
                  BlockEntityDebugModule.Shown var15 = (BlockEntityDebugModule.Shown)var12.get(var13);
                  double var16 = var15.cx() - var9.x;
                  double var18 = var15.cz() - var9.z;
                  if (!(var16 * var16 + var18 * var18 > var10 * var10)) {
                     int var20 = var15.color();
                     if (var20 >>> 24 != 0) {
                        Vec3d var21 = new Vec3d(var15.cx(), var15.cy(), var15.cz());
                        ad.drawWithFade(imDrawList, var4, f, g, var6, var7, var21, var20, var8, var9, var10);
                     }
                  }
               }
            }
         }
      }
   }

   private double renderDistance() {
      return af.blocks();
   }

   private boolean shouldSkip(Block block) {
      return !this.isRenderableBlock(block);
   }

   private boolean isRenderableBlock(Block block) {
      if (this.sBlockFilter == null || !this.sBlockFilter.value().get()) {
         return true;
      } else {
         return block == Blocks.AIR ? false : this.sBlockColors == null || this.sBlockColors.isEnabled(block);
      }
   }

   private static int withAlpha(int i, int j) {
      return i & 16777215 | Math.min(255, Math.max(0, j)) << 24;
   }

   @Environment(EnvType.CLIENT)
   private record Shown(BlockPos pos, double cx, double cy, double cz, int color, Box box, Box beam) {
   }
}
