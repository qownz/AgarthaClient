package net.agartha.client.module.impl.render;

import imgui.ImDrawList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.agartha.client.d.a_pkg.o;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.e.aj;
import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.module.f;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.util.ab;
import net.agartha.client.util.ad;
import net.agartha.client.util.af;
import net.agartha.client.util.i;
import net.agartha.client.util.l;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.VisibilityConfigurable;

@Environment(EnvType.CLIENT)
public final class SusEspModule extends b implements f {
   private static final int CACHE_RADIUS = 96;
   private static final int TRADER_COLOR = -16738561;
   private static final int LLAMA_COLOR = -16728065;
   private static final float LINE_WIDTH = 1.5F;
   private final net.agartha.client.module.a_pkg.b sWanderingEsp = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Wandering ESP", true)
   );
   private final net.agartha.client.module.a_pkg.b sLlamaEsp = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Llama ESP", true));
   private final net.agartha.client.module.a_pkg.b sTracers = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Tracers", false));
   private final j sAlpha = (j)this.addSetting(new j("Alpha", 140.0F, 0.0F, 255.0F));
   private final Set<UUID> detectedTraders = new HashSet<>();
   private final Set<UUID> detectedLlamas = new HashSet<>();
   private final Set<UUID> notifiedTraders = new HashSet<>();
   private final Set<UUID> notifiedLlamas = new HashSet<>();
   private final Map<UUID, Entity> entityByUuid = new HashMap<>();

   public SusEspModule() {
      super("Sus ESP", "Detects wandering traders and trader llamas.", c.DONUT);
   }

   protected void onEnable() {
      this.clearState();
   }

   protected void onDisable() {
      this.clearState();
   }

   @net.agartha.client.d.f
   public void onTick(q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null) {
         this.scanEntities(var2);
      }
   }

   @net.agartha.client.d.f
   public void onRender3D(o arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null) {
         int var3 = clampAlpha(this.sAlpha.getInt());
         int var4 = withAlpha(-16738561, var3);
         int var5 = withAlpha(-16728065, var3);
         if (this.sWanderingEsp.get()) {
            for (UUID var7 : this.detectedTraders) {
               Entity var8 = this.findEntityByUuid(var2, var7);
               if (marked(var2, var8, arg.partialTick)) {
                  drawBox(interpolatedBox(var8, arg.partialTick), -16738561, var4);
               }
            }
         }

         if (this.sLlamaEsp.get()) {
            for (UUID var10 : this.detectedLlamas) {
               Entity var11 = this.findEntityByUuid(var2, var10);
               if (marked(var2, var11, arg.partialTick)) {
                  drawBox(interpolatedBox(var11, arg.partialTick), -16728065, var5);
               }
            }
         }
      }
   }

   public boolean isTracerOverlayEnabled() {
      return this.isEnabled() && this.sTracers.get() && (this.sWanderingEsp.get() || this.sLlamaEsp.get());
   }

   public void renderTracerOverlay(ImDrawList imDrawList, float f, float g) {
      if (this.isTracerOverlayEnabled() && imDrawList != null) {
         MinecraftClient var4 = MinecraftClient.getInstance();
         if (var4.world != null && var4.player != null) {
            float var5 = var4.getRenderTickCounter().getTickProgress(false);
            Vec3d var6 = var4.player.getCameraPosVec(var5);
            float var7 = ad.originX(f);
            float var8 = ad.originY("Crosshair", g);
            double var9 = af.blocks();
            if (this.sWanderingEsp.get()) {
               for (UUID var12 : this.detectedTraders) {
                  Entity var13 = this.findEntityByUuid(var4, var12);
                  if (marked(var4, var13, var5)) {
                     Vec3d var14 = centerOf(interpolatedBox(var13, var5));
                     ad.drawWithFade(imDrawList, var4, f, g, var7, var8, var14, -16738561, 1.5F, var6, var9);
                  }
               }
            }

            if (this.sLlamaEsp.get()) {
               for (UUID var16 : this.detectedLlamas) {
                  Entity var17 = this.findEntityByUuid(var4, var16);
                  if (marked(var4, var17, var5)) {
                     Vec3d var18 = centerOf(interpolatedBox(var17, var5));
                     ad.drawWithFade(imDrawList, var4, f, g, var7, var8, var18, -16728065, 1.5F, var6, var9);
                  }
               }
            }
         }
      }
   }

   private static boolean marked(MinecraftClient minecraftClient, Entity entity, float f) {
      if (entity != null && minecraftClient.player != null) {
         double var3 = af.blocks();
         Vec3d var5 = minecraftClient.player.getCameraPosVec(f);
         return entity.getLerpedPos(f).squaredDistanceTo(var5) <= var3 * var3 && i.drawnByGame(entity);
      } else {
         return false;
      }
   }

   private void scanEntities(MinecraftClient minecraftClient) {
      this.detectedTraders.clear();
      this.detectedLlamas.clear();
      this.entityByUuid.clear();
      if (minecraftClient.world != null) {
         boolean var2 = this.sWanderingEsp.get();
         boolean var3 = this.sLlamaEsp.get();
         if (var2 || var3) {
            for (Entity var5 : minecraftClient.world.getEntities()) {
               if (!var5.isRemoved()) {
                  EntityType var6 = var5.getType();
                  boolean var7 = var2 && var6 == EntityType.WANDERING_TRADER;
                  boolean var8 = var3 && var6 == EntityType.TRADER_LLAMA;
                  if (var7 || var8) {
                     this.entityByUuid.put(var5.getUuid(), var5);
                     if (var7) {
                        UUID var9 = var5.getUuid();
                        this.detectedTraders.add(var9);
                        if (this.notifiedTraders.add(var9)) {
                           this.notify(minecraftClient, "Wandering ESP", "Found wandering trader");
                        }
                     } else if (this.sLlamaEsp.get() && var5.getType() == EntityType.TRADER_LLAMA) {
                        UUID var10 = var5.getUuid();
                        this.detectedLlamas.add(var10);
                        if (this.notifiedLlamas.add(var10)) {
                           this.notify(minecraftClient, "Llama ESP", "Found trader llama");
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void notify(MinecraftClient minecraftClient, String string, String string2) {
      ab.send(string, string2);
      aj.getInstance().pushAlert(string + " — " + string2);
   }

   private static void drawBox(Box box, int i, int j) {
      if (net.agartha.client.util.l.isSafeCuboid(box)) {
         VisibilityConfigurable var3 = GizmoDrawing.box(box, DrawStyle.filledAndStroked(i, 1.5F, j));
         var3.ignoreOcclusion();
      }
   }

   private Entity findEntityByUuid(MinecraftClient minecraftClient, UUID uUID) {
      return this.entityByUuid.get(uUID);
   }

   private void clearState() {
      this.detectedTraders.clear();
      this.detectedLlamas.clear();
      this.notifiedTraders.clear();
      this.notifiedLlamas.clear();
      this.entityByUuid.clear();
   }

   private static Box interpolatedBox(Entity entity, float f) {
      Vec3d var2 = entity.getEntityPos();
      Vec3d var3 = entity.getLerpedPos(f);
      return entity.getBoundingBox().offset(var3.x - var2.x, var3.y - var2.y, var3.z - var2.z);
   }

   private static Vec3d centerOf(Box box) {
      return new Vec3d((box.minX + box.maxX) * 0.5, (box.minY + box.maxY) * 0.5, (box.minZ + box.maxZ) * 0.5);
   }

   private static int withAlpha(int i, int j) {
      return i & 16777215 | clampAlpha(j) << 24;
   }

   private static int clampAlpha(int i) {
      return Math.min(255, Math.max(0, i));
   }
}
