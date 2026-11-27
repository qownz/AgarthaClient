package net.agartha.client.module.impl.render;

import imgui.ImDrawList;
import java.util.ArrayList;
import java.util.List;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.Render3DEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.TracerOverlay;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ColorSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.util.render.EntityVisibility;
import net.agartha.client.util.render.FrustumCulling;
import net.agartha.client.util.render.OffscreenIndicator;
import net.agartha.client.util.world.ViewDistance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.VisibilityConfigurable;

@Environment(EnvType.CLIENT)
public class EspModule extends Module implements TracerOverlay {
   private static final float TRACER_WIDTH = 2.5F;
   private final BooleanSetting sMobs = this.addSetting(new BooleanSetting("Mobs", true));
   private final BooleanSetting sAnimals = this.addSetting(new BooleanSetting("Animals", false));
   private final BooleanSetting sBox = this.addSetting(new BooleanSetting("Box", true));
   private final BooleanSetting sTracers = this.addSetting(new BooleanSetting("Tracers", true));
   private final BooleanSetting sCustomTracerColor = this.addSetting(new BooleanSetting("Custom Tracer Color", false));
   private final ColorSetting sTracerColor = this.addSetting(new ColorSetting("Tracer Color", -1));
   private final EnumSetting sShape = this.addSetting(new EnumSetting("Shape", "Glow", "Glow", "Box", "Fill", "Both"));
   private final NumberSetting sMaxEntities = this.addSetting(new NumberSetting("Max Entities", 48.0F, 8.0F, 96.0F));
   private final ColorSetting sMobColor = this.addSetting(new ColorSetting("Mob Color", -52429));
   private final ColorSetting sAnimalColor = this.addSetting(new ColorSetting("Animal Color", -13369498));
   private final NumberSetting sFillAlpha = this.addSetting(new NumberSetting("Fill Alpha", 0.3F, 0.0F, 1.0F));
   private final NumberSetting sLineWidth = this.addSetting(new NumberSetting("Line Width", 1.5F, 0.5F, 5.0F));
   private static final int UNCAPPED = Integer.MAX_VALUE;
   private final List<Entity> frameCandidates = new ArrayList<>();
   private Entity[] pickEnts = new Entity[128];
   private double[] pickDist = new double[128];
   private long frameKeyTick = Long.MIN_VALUE;
   private int frameKeyPartial;
   private float frameKeyRange;
   private int frameKeyMax;

   public EspModule() {
      super(
         "ESP", "Outlines mobs and animals with a glow or a box, with optional tracers. Players have their own Player ESP.", ModuleCategory.RENDER
      );
      this.sCustomTracerColor.visibleWhen(this.sTracers::get);
      this.sTracerColor.visibleWhen(() -> this.sTracers.get() && this.sCustomTracerColor.get());
   }

   public static boolean shouldGlow(Entity entity) {
      EspModule var1 = ModuleManager.get(EspModule.class);
      if (var1 == null || !var1.isEnabled()) {
         return false;
      } else if (!"Glow".equals(var1.sShape.value())) {
         return false;
      } else {
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (var2.world != null && !entity.isRemoved()) {
            if (var2.getEntityRenderDispatcher().getRenderer(entity) == null) {
               return false;
            } else {
               float var3 = var2.getRenderTickCounter().getTickProgress(false);
               return var1.matchesFilter(entity) && var1.a(entity, var3) && EntityVisibility.drawnByGame(entity);
            }
         } else {
            return false;
         }
      }
   }

   public static boolean isGlowPathActive() {
      EspModule var0 = ModuleManager.get(EspModule.class);
      if (var0 == null || !var0.isEnabled()) {
         return false;
      } else {
         return !"Glow".equals(var0.sShape.value()) ? false : var0.sMobs.value().get() || var0.sAnimals.value().get();
      }
   }

   public static int glowColor(Entity entity) {
      EspModule var1 = ModuleManager.get(EspModule.class);
      return var1 == null ? 16777215 : var1.a(entity) & 16777215;
   }

   @EventListener
   public void onRender3D(Render3DEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null) {
         String var3 = this.sShape.value();
         boolean var4 = "Glow".equals(var3);
         boolean var5 = this.sBox.value().get() && !var4;
         if (var5 && this.a()) {
            float var6 = this.sLineWidth.value()[0];
            int var7 = Math.round(this.sFillAlpha.value()[0] * 255.0F);
            float var8 = (float)ViewDistance.blocks();
            int var9 = this.b();
            int var10 = 0;

            for (Entity var12 : this.a(arg.partialTick, var8, var9)) {
               if (var10 >= var9) {
                  break;
               }

               int var13 = this.a(var12);
               int var14 = a(var13, var7);
               Box var15 = b(var12, arg.partialTick);
               if (FrustumCulling.isSafeCuboid(var15)) {
                  DrawStyle var16 = switch (var3) {
                     case "Fill" -> DrawStyle.filled(var14);
                     case "Both" -> DrawStyle.filledAndStroked(var13, var6, var14);
                     default -> DrawStyle.stroked(var13, var6);
                  };
                  VisibilityConfigurable var17 = GizmoDrawing.box(var15, var16);
                  var17.ignoreOcclusion();
                  var10++;
               }
            }
         }
      }
   }

   @Override
   public boolean isTracerOverlayEnabled() {
      return this.isEnabled() && this.sTracers.value().get() && this.a();
   }

   private boolean a() {
      return this.sMobs.value().get() || this.sAnimals.value().get();
   }

   @Override
   public void renderTracerOverlay(ImDrawList imDrawList, float f, float g) {
      if (this.isTracerOverlayEnabled() && imDrawList != null) {
         MinecraftClient var4 = MinecraftClient.getInstance();
         if (var4.world != null && var4.player != null) {
            float var5 = 2.5F;
            float var6 = (float)ViewDistance.blocks();
            int var7 = "Glow".equals(this.sShape.value()) ? Integer.MAX_VALUE : this.b();
            float var8 = var4.getRenderTickCounter().getTickProgress(false);
            float var9 = OffscreenIndicator.originX(f);
            float var10 = OffscreenIndicator.originY("Crosshair", g);
            Vec3d var11 = var4.player.getCameraPosVec(var8);
            int var12 = 0;

            for (Entity var14 : this.a(var8, var6, var7)) {
               if (var12 >= var7) {
                  break;
               }

               Vec3d var15 = var14.getLerpedPos(var8);
               Vec3d var16 = new Vec3d(var15.x, var15.y + var14.getHeight() * 0.5, var15.z);
               int var17 = this.sCustomTracerColor.value().get() ? this.sTracerColor.argb() : this.a(var14);
               if (var17 >>> 24 != 0 && OffscreenIndicator.drawWithFade(imDrawList, var4, f, g, var9, var10, var16, var17, var5, var11, var6)) {
                  var12++;
               }
            }
         }
      }
   }

   boolean matchesFilter(Entity entity) {
      if (entity instanceof PlayerEntity) {
         return false;
      } else {
         return entity instanceof HostileEntity && this.sMobs.value().get() ? true : entity instanceof PassiveEntity && this.sAnimals.value().get();
      }
   }

   private boolean a(Entity entity, float f) {
      return this.a(entity, f, (float)ViewDistance.blocks());
   }

   private boolean a(Entity entity, float f, float g) {
      MinecraftClient var4 = MinecraftClient.getInstance();
      return var4.player == null ? true : a(entity, a(var4, f), b(var4, f), c(var4, f)) <= (double)g * g;
   }

   private static double a(MinecraftClient minecraftClient, float f) {
      return minecraftClient.player.lastX + (minecraftClient.player.getX() - minecraftClient.player.lastX) * f;
   }

   private static double b(MinecraftClient minecraftClient, float f) {
      return minecraftClient.player.lastY + (minecraftClient.player.getY() - minecraftClient.player.lastY) * f + minecraftClient.player.getStandingEyeHeight();
   }

   private static double c(MinecraftClient minecraftClient, float f) {
      return minecraftClient.player.lastZ + (minecraftClient.player.getZ() - minecraftClient.player.lastZ) * f;
   }

   private static double a(Entity entity, double d, double e, double f) {
      Box var7 = entity.getBoundingBox();
      double var8 = Math.max(Math.max(var7.minX - d, 0.0), d - var7.maxX);
      double var10 = Math.max(Math.max(var7.minY - e, 0.0), e - var7.maxY);
      double var12 = Math.max(Math.max(var7.minZ - f, 0.0), f - var7.maxZ);
      return var8 * var8 + var10 * var10 + var12 * var12;
   }

   private static Box b(Entity entity, float f) {
      Vec3d var2 = entity.getEntityPos();
      Vec3d var3 = entity.getLerpedPos(f);
      return entity.getBoundingBox().offset(var3.x - var2.x, var3.y - var2.y, var3.z - var2.z);
   }

   private int a(Entity entity) {
      if (entity instanceof HostileEntity) {
         return this.sMobColor.argb();
      } else {
         return entity instanceof PassiveEntity ? this.sAnimalColor.argb() : -1;
      }
   }

   private static int a(int i, int j) {
      return i & 16777215 | Math.min(255, Math.max(0, j)) << 24;
   }

   private int b() {
      return Math.max(1, Math.min(96, this.sMaxEntities.getInt()));
   }

   private List<Entity> a(float f, float g, int i) {
      MinecraftClient var4 = MinecraftClient.getInstance();
      if (var4.world != null && var4.player != null) {
         long var5 = var4.world.getTime();
         int var7 = Float.floatToIntBits(f);
         if (var5 == this.frameKeyTick && var7 == this.frameKeyPartial && g == this.frameKeyRange && i == this.frameKeyMax) {
            return this.frameCandidates;
         } else {
            this.frameKeyTick = var5;
            this.frameKeyPartial = var7;
            this.frameKeyRange = g;
            this.frameKeyMax = i;
            double var8 = a(var4, f);
            double var10 = b(var4, f);
            double var12 = c(var4, f);
            double var14 = (double)g * g;
            if (i >= Integer.MAX_VALUE) {
               this.frameCandidates.clear();

               for (Entity var24 : var4.world.getEntities()) {
                  if (!var24.isRemoved()
                     && this.matchesFilter(var24)
                     && var4.getEntityRenderDispatcher().getRenderer(var24) != null
                     && EntityVisibility.drawnByGame(var24)
                     && a(var24, var8, var10, var12) <= var14) {
                     this.frameCandidates.add(var24);
                  }
               }

               return this.frameCandidates;
            } else {
               int var16 = Math.max(1, i);
               if (this.pickEnts.length < var16) {
                  this.pickEnts = new Entity[var16];
                  this.pickDist = new double[var16];
               }

               int var17 = 0;

               for (Entity var19 : var4.world.getEntities()) {
                  if (!var19.isRemoved()
                     && this.matchesFilter(var19)
                     && var4.getEntityRenderDispatcher().getRenderer(var19) != null
                     && EntityVisibility.drawnByGame(var19)) {
                     double var20 = a(var19, var8, var10, var12);
                     if (!(var20 > var14) && (var17 != var16 || !(var20 >= this.pickDist[var17 - 1]))) {
                        int var22;
                        for (var22 = var17 < var16 ? var17++ : var16 - 1; var22 > 0 && this.pickDist[var22 - 1] > var20; var22--) {
                           this.pickDist[var22] = this.pickDist[var22 - 1];
                           this.pickEnts[var22] = this.pickEnts[var22 - 1];
                        }

                        this.pickDist[var22] = var20;
                        this.pickEnts[var22] = var19;
                     }
                  }
               }

               this.frameCandidates.clear();

               for (int var25 = 0; var25 < var17; var25++) {
                  this.frameCandidates.add(this.pickEnts[var25]);
                  this.pickEnts[var25] = null;
               }

               return this.frameCandidates;
            }
         }
      } else {
         this.frameCandidates.clear();
         return this.frameCandidates;
      }
   }
}
