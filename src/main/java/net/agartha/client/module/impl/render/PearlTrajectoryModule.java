package net.agartha.client.module.impl.render;

import imgui.ImDrawList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.Render3DEvent;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.TracerOverlay;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ColorSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.render.effect.GizmoCanvas;
import net.agartha.client.render.effect.ParticleSystem;
import net.agartha.client.util.color.ColorSettingWiring;
import net.agartha.client.util.perf.Profiler;
import net.agartha.client.util.player.PearlTrajectory;
import net.agartha.client.util.render.FrustumCulling;
import net.agartha.client.util.render.GizmoBatch;
import net.agartha.client.util.render.WorldLineProjector;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

@Environment(EnvType.CLIENT)
public final class PearlTrajectoryModule extends Module implements TracerOverlay {
   private static final float ANIM_SPEED = 1.35F;
   private static final float SHOW_SECONDS = 0.16296296F;
   private static final float HIDE_SECONDS = 0.2074074F;
   private static final float TRAIL_FADE_SECONDS = 0.44444445F;
   private static final float LANDING_SWAP_SECONDS = 0.1037037F;
   private static final int MAX_RENDER_SEGMENTS = 64;
   private static final int MAX_TRAIL_POINTS = 200;
   private static final double MIN_TRAIL_DISTANCE_SQ = 1.0E-4;
   private static final double PREDICTION_RECALC_DISTANCE_SQ = 4.0E-4;
   private static final float PREDICTION_RECALC_ROTATION_DEGREES = 1.0F;
   private static final double LANDING_FACE_THICKNESS = 0.03;
   private static final int LANDING_FILL_ALPHA = 60;
   private static final int LANDING_LINE_ALPHA = 185;
   private final Map<Integer, List<Vec3d>> trails = new HashMap<>();
   private PearlTrajectory.a cachedPrediction;
   private List<Vec3d> cachedRenderPoints = List.of();
   private Vec3d cachedPredictionOrigin;
   private float cachedPredictionYaw = Float.NaN;
   private float cachedPredictionPitch = Float.NaN;
   private boolean cachedHoldingPearl;
   private float predictPresence;
   private List<Vec3d> shownPoints = List.of();
   private BlockHitResult shownHit;
   private BlockPos landingPos;
   private Direction landingFace;
   private float landingShown;
   private BlockPos oldLandingPos;
   private Direction oldLandingFace;
   private float oldLandingShown;
   private final Map<Integer, List<Vec3d>> leaving = new HashMap<>();
   private final Map<Integer, float[]> leavingFade = new HashMap<>();
   private long lastFrame;
   private final BooleanSetting sPredict = this.addSetting(new BooleanSetting("Predict", true));
   private final BooleanSetting sTrail = this.addSetting(new BooleanSetting("Trail", true));
   private final BooleanSetting sThemeColor = this.addSetting(new BooleanSetting("Theme Color", true));
   private final BooleanSetting sCustomColor = this.addSetting(new BooleanSetting("Custom Color", false));
   private final ColorSetting sColor = this.addSetting(new ColorSetting("Color", -1));
   private final NumberSetting sIgnoreFirstTicks = this.addSetting(new NumberSetting("Ignore First Ticks", 3.0F, 0.0F, 20.0F));
   private final NumberSetting sLineWidth = this.addSetting(new NumberSetting("Line Width", 2.0F, 0.5F, 5.0F));
   private final BooleanSetting sParticles = this.addSetting(new BooleanSetting("Pearl Particles", true));
   private final ParticleSystem sparks = new ParticleSystem(600);
   private final GizmoCanvas sparkCanvas = new GizmoCanvas();
   private final Random sparkRandom = Random.create();
   private static final float PATH_PIXELS = 1.25F;
   private boolean predDraw;
   private int predStart;
   private double predEnd;
   private float predAlpha;
   private int frameColor;

   public PearlTrajectoryModule() {
      super("Pearl Trajectory", "Predicts pearl path while holding one and leaves a trail behind thrown pearls.", ModuleCategory.RENDER);
      ColorSettingWiring.wireExclusiveColorSource(this.sCustomColor, this.sThemeColor, this.sColor, true);
   }

   @Override
   protected void onDisable() {
      this.trails.clear();
      this.sparks.clear();
      this.leaving.clear();
      this.leavingFade.clear();
      this.predictPresence = 0.0F;
      this.shownPoints = List.of();
      this.shownHit = null;
      this.landingPos = null;
      this.oldLandingPos = null;
      this.lastFrame = 0L;
      this.cachedPrediction = null;
      this.cachedRenderPoints = List.of();
      this.cachedPredictionOrigin = null;
      this.cachedPredictionYaw = Float.NaN;
      this.cachedPredictionPitch = Float.NaN;
      this.cachedHoldingPearl = false;
   }

   @EventListener
   public void onTick(TickEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null) {
         HashSet var3 = new HashSet();

         for (Entity var5 : var2.world.getEntities()) {
            if (var5 instanceof EnderPearlEntity var6 && !var6.isRemoved() && a(var6, var2.player)) {
               int var7 = var6.getId();
               var3.add(var7);
               Vec3d var8 = var6.getEntityPos();
               List var9 = this.trails.computeIfAbsent(var7, integer -> new ArrayList<>());
               if (!var9.isEmpty()) {
                  Vec3d var10 = (Vec3d)var9.getLast();
                  if (var10.squaredDistanceTo(var8) < 1.0E-4) {
                     continue;
                  }
               }

               var9.add(var8);
               if (var9.size() > 200) {
                  var9.removeFirst();
               }
            }
         }

         Iterator var11 = this.trails.entrySet().iterator();

         while (var11.hasNext()) {
            Entry var12 = (Entry)var11.next();
            if (!var3.contains(var12.getKey())) {
               if (((List)var12.getValue()).size() >= 2) {
                  this.leaving.put((Integer)var12.getKey(), (List<Vec3d>)var12.getValue());
                  this.leavingFade.put((Integer)var12.getKey(), new float[]{1.0F});
               }

               var11.remove();
            }
         }

         boolean var13 = this.sPredict.get() && PearlTrajectory.isHoldingPearl(var2.player);
         if (!var13) {
            this.cachedPrediction = null;
            this.cachedRenderPoints = List.of();
            this.cachedPredictionOrigin = null;
            this.cachedPredictionYaw = Float.NaN;
            this.cachedPredictionPitch = Float.NaN;
         }

         this.cachedHoldingPearl = var13;
         if (this.sParticles.get()) {
            this.a(var2);
         }
      } else {
         this.trails.clear();
      }
   }

   private void a(MinecraftClient minecraftClient) {
      int var2 = ColorSettingWiring.argb(this.sThemeColor, this.sCustomColor, this.sColor, -1) | 0xFF000000;
      int var3 = ColorHelper.lerp(0.6F, var2, -1);

      for (Entity var5 : minecraftClient.world.getEntities()) {
         if (var5 instanceof EnderPearlEntity var6 && !var6.isRemoved() && !(var6.squaredDistanceTo(minecraftClient.player) > 6400.0)) {
            double var7 = var6.getX() - var6.lastX;
            double var9 = var6.getY() - var6.lastY;
            double var11 = var6.getZ() - var6.lastZ;
            if (!(var7 * var7 + var9 * var9 + var11 * var11 < 9.0E-4)) {
               for (int var13 = 0; var13 < 3; var13++) {
                  float var14 = (var13 + this.sparkRandom.nextFloat()) / 3.0F;
                  this.sparks
                     .spawn(
                        (byte)3,
                        var6.lastX + var7 * var14 + this.a(0.08F),
                        var6.lastY + var9 * var14 + 0.12 + this.a(0.08F),
                        var6.lastZ + var11 * var14 + this.a(0.08F),
                        this.a(0.18F),
                        this.a(0.18F),
                        this.a(0.18F),
                        1.1F,
                        0.14F,
                        this.sparkRandom.nextFloat() * 6.28F,
                        1.5F,
                        var2,
                        var3,
                        600
                     );
               }

               for (int var15 = 0; var15 < 2; var15++) {
                  this.sparks
                     .spawn(
                        (byte)4,
                        var6.lastX + var7 * var15 * 0.5 + this.a(0.06F),
                        var6.lastY + var9 * var15 * 0.5 + 0.12,
                        var6.lastZ + var11 * var15 * 0.5 + this.a(0.06F),
                        this.a(0.35F),
                        0.3F,
                        this.a(0.35F),
                        1.2F,
                        0.085F,
                        this.sparkRandom.nextFloat() * 6.28F,
                        3.0F,
                        var2,
                        var3,
                        600
                     );
               }
            }
         }
      }
   }

   private float a(float f) {
      return (this.sparkRandom.nextFloat() * 2.0F - 1.0F) * f;
   }

   @EventListener
   public void onRender3D(Render3DEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null && var2.world != null) {
         long var3 = System.nanoTime();
         float var5 = this.lastFrame == 0L ? 0.016F : Math.min((float)(var3 - this.lastFrame) / 1.0E9F, 0.1F);
         this.lastFrame = var3;
         boolean var6 = this.sPredict.get() && PearlTrajectory.isHoldingPearl(var2.player);
         if (var6) {
            this.a(var2, arg.partialTick);
            if (this.cachedPrediction != null) {
               this.shownPoints = this.cachedRenderPoints;
               this.shownHit = this.cachedPrediction.blockHit();
            }
         }

         boolean var7 = var6 && this.cachedPrediction != null;
         this.predictPresence = var7 ? Math.min(1.0F, this.predictPresence + var5 / 0.16296296F) : Math.max(0.0F, this.predictPresence - var5 / 0.2074074F);
         if (this.predictPresence <= 0.0F) {
            this.shownPoints = List.of();
            this.shownHit = null;
            this.landingPos = null;
            this.oldLandingPos = null;
         }

         int var8 = ColorSettingWiring.argb(this.sThemeColor, this.sCustomColor, this.sColor, -1);
         float var9 = this.sLineWidth.get();
         this.frameColor = var8;
         this.predDraw = false;
         if (this.predictPresence > 0.0F) {
            this.a(var8, var9, var7, var5);
         }

         if (this.sparks.count() > 0) {
            this.sparks.step(var2.isPaused() ? 0.0F : var5);
            this.sparkCanvas.begin(var2.gameRenderer.getCamera());
            this.sparks.draw(this.sparkCanvas, 1.0F);
            this.sparkCanvas.submit(false);
         }

         if (this.sTrail.get()) {
            this.c(var5);
         } else {
            this.leaving.clear();
            this.leavingFade.clear();
         }
      }
   }

   private void a(int i, float f, boolean bl, float g) {
      List var5 = this.shownPoints;
      int var6 = Math.min(Math.max(0, this.sIgnoreFirstTicks.getInt()), Math.max(0, var5.size() - 2));
      if (var5.size() - var6 >= 2) {
         float var7 = this.predictPresence * this.predictPresence * (3.0F - 2.0F * this.predictPresence);
         float var8 = bl ? var7 : 1.0F;
         float var9 = bl ? 1.0F : var7;
         this.predStart = var6;
         this.predEnd = var6 + (var5.size() - 1 - var6) * var8;
         this.predAlpha = var9;
         this.predDraw = true;
         this.a(var8 >= 0.999F ? this.shownHit : null, i, var9, g);
      }
   }

   private void a(BlockHitResult blockHitResult, int i, float f, float g) {
      BlockPos var5 = blockHitResult == null ? null : blockHitResult.getBlockPos();
      Direction var6 = blockHitResult == null ? null : blockHitResult.getSide();
      if (var5 != null && (!var5.equals(this.landingPos) || var6 != this.landingFace)) {
         if (this.landingPos != null && this.landingShown > 0.02F) {
            this.oldLandingPos = this.landingPos;
            this.oldLandingFace = this.landingFace;
            this.oldLandingShown = this.landingShown;
         }

         this.landingPos = var5.toImmutable();
         this.landingFace = var6;
         this.landingShown = this.oldLandingPos == null ? 1.0F : 0.0F;
      }

      float var7 = g / 0.1037037F;
      if (var5 == null) {
         this.landingShown = Math.max(0.0F, this.landingShown - var7);
      } else {
         this.landingShown = Math.min(1.0F, this.landingShown + var7);
      }

      if (this.oldLandingPos != null) {
         this.oldLandingShown -= var7;
         if (this.oldLandingShown <= 0.0F) {
            this.oldLandingPos = null;
         } else {
            this.a(this.oldLandingPos, this.oldLandingFace, i, f * b(this.oldLandingShown));
         }
      }

      if (this.landingPos != null && this.landingShown > 0.0F) {
         this.a(this.landingPos, this.landingFace, i, f * b(this.landingShown));
      }
   }

   private void a(BlockPos blockPos, Direction direction, int i, float f) {
      if (!(f <= 0.01F)) {
         GizmoBatch.submitCuboid(
            blockPos.offset(direction),
            a(blockPos, direction),
            b(i, Math.round(185.0F * f)),
            Math.max(1.0F, this.sLineWidth.get()),
            b(i, Math.round(60.0F * f)),
            true
         );
      }
   }

   private static float b(float f) {
      float var1 = Math.max(0.0F, Math.min(1.0F, f));
      return var1 * var1 * (3.0F - 2.0F * var1);
   }

   private void c(float f) {
      Iterator var2 = this.leaving.entrySet().iterator();

      while (var2.hasNext()) {
         Entry var3 = (Entry)var2.next();
         float[] var4 = this.leavingFade.get(var3.getKey());
         var4[0] -= f / 0.44444445F;
         if (var4[0] <= 0.0F) {
            this.leavingFade.remove(var3.getKey());
            var2.remove();
         }
      }
   }

   @Override
   public boolean isTracerOverlayEnabled() {
      return this.isEnabled() && (this.predDraw || this.sTrail.get() && (!this.trails.isEmpty() || !this.leaving.isEmpty()));
   }

   @Override
   public void renderTracerOverlay(ImDrawList imDrawList, float f, float g) {
      MinecraftClient var4 = MinecraftClient.getInstance();
      if (var4.world != null && var4.player != null) {
         if (WorldLineProjector.begin(var4, f, g)) {
            float var5 = this.sLineWidth.get() * 1.25F;
            int var6 = this.frameColor;
            if (this.predDraw && this.shownPoints.size() >= 2) {
               WorldLineProjector.draw(this.shownPoints, this.predStart, this.predEnd, var6, var5, this.predAlpha, this.predAlpha);
            }

            if (this.sTrail.get()) {
               float var7 = var4.getRenderTickCounter().getTickProgress(false);

               for (Entry var9 : this.trails.entrySet()) {
                  Entity var10 = var4.world.getEntityById((Integer)var9.getKey());
                  Vec3d var11 = var10 == null ? null : var10.getLerpedPos(var7);
                  this.a((List<Vec3d>)var9.getValue(), var11, var6, 1.0F, var5);
               }

               for (Entry var13 : this.leaving.entrySet()) {
                  float[] var14 = this.leavingFade.get(var13.getKey());
                  if (var14 != null) {
                     this.a((List<Vec3d>)var13.getValue(), null, var6, var14[0] * var14[0], var5);
                  }
               }
            }
         }
      }
   }

   private void a(List<Vec3d> list, Vec3d vec3d, int i, float f, float g) {
      if (list.size() >= 2) {
         WorldLineProjector.draw(list, 0, list.size() - 1, i, g, f * 0.25F, f);
         Vec3d var6 = (Vec3d)list.getLast();
         if (vec3d != null && var6.squaredDistanceTo(vec3d) > 1.0E-4) {
            WorldLineProjector.segment(var6.x, var6.y, var6.z, vec3d.x, vec3d.y, vec3d.z, a(i, f), g);
         }
      }
   }

   private static float a(int i, int j) {
      float var2 = j <= 0 ? 1.0F : (float)i / j;
      return 0.25F + 0.75F * var2;
   }

   private static int a(int i, float f) {
      int var2 = Math.round((i >>> 24 & 0xFF) * Math.max(0.0F, Math.min(1.0F, f)));
      return var2 << 24 | i & 16777215;
   }

   private static void a(Vec3d vec3d, Vec3d vec3d2, int i, float f) {
      if (FrustumCulling.isSafeLine(vec3d, vec3d2)) {
         GizmoDrawing.line(vec3d, vec3d2, i, f).ignoreOcclusion();
      }
   }

   private static boolean a(EnderPearlEntity enderPearlEntity, Entity entity) {
      Entity var2 = enderPearlEntity.getOwner();
      return var2 == null || var2 == entity;
   }

   private void a(MinecraftClient minecraftClient, float f) {
      Vec3d var3 = minecraftClient.player.getCameraPosVec(f);
      float var4 = minecraftClient.player.getYaw(f);
      float var5 = minecraftClient.player.getPitch(f);
      boolean var6 = this.cachedPrediction == null
         || !this.cachedHoldingPearl
         || this.cachedPredictionOrigin == null
         || this.cachedPredictionOrigin.squaredDistanceTo(var3) > 4.0E-4
         || a(this.cachedPredictionYaw, var4) > 0.02F
         || Math.abs(this.cachedPredictionPitch - var5) > 0.02F;
      if (var6) {
         long var7 = Profiler.begin();
         this.cachedPrediction = PearlTrajectory.simulate(minecraftClient.world, minecraftClient.player, f);
         this.cachedRenderPoints = this.cachedPrediction == null ? List.of() : this.cachedPrediction.points();
         this.cachedPredictionOrigin = var3;
         this.cachedPredictionYaw = var4;
         this.cachedPredictionPitch = var5;
         Profiler.record("pearl.prediction", var7);
         Profiler.recordCount("pearl.segments", Math.max(0, this.cachedRenderPoints.size() - 1));
      }
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private static Box a(BlockPos blockPos, Direction direction) {
      double var2 = blockPos.getX();
      double var4 = blockPos.getY();
      double var6 = blockPos.getZ();
      double var8 = var2 + 1.0;
      double var10 = var4 + 1.0;
      double var12 = var6 + 1.0;

      return switch (direction) {
         case DOWN -> new Box(var2, var4, var6, var8, var4 + 0.03, var12);
         case UP -> new Box(var2, var10 - 0.03, var6, var8, var10, var12);
         case NORTH -> new Box(var2, var4, var6, var8, var10, var6 + 0.03);
         case SOUTH -> new Box(var2, var4, var12 - 0.03, var8, var10, var12);
         case WEST -> new Box(var2, var4, var6, var2 + 0.03, var10, var12);
         case EAST -> new Box(var8 - 0.03, var4, var6, var8, var10, var12);
         default -> throw new MatchException(null, null);
      };
   }

   private static float a(float f, float g) {
      return Math.abs(MathHelper.wrapDegrees(f - g));
   }

   private static int b(int i, int j) {
      return i & 16777215 | Math.min(255, Math.max(0, j)) << 24;
   }
}
