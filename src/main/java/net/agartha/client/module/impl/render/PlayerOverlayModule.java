package net.agartha.client.module.impl.render;

import java.util.function.BooleanSupplier;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.Render3DEvent;
import net.agartha.client.gui.AccentColor;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ColorSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.render.effect.EffectSprites;
import net.agartha.client.render.effect.GizmoCanvas;
import net.agartha.client.util.color.ColorSettingWiring;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public final class PlayerOverlayModule extends Module {
   private static final String REACH_THEME = "Theme";
   private static final String REACH_CUSTOM = "Custom";
   private static final float FADE_IN = 0.18F;
   private static final float FADE_OUT = 0.3F;
   private static final float COLOR_RATE = 12.0F;
   private static final double SNAP_DISTANCE = 8.0;
   private static final double LIFT = 0.004;
   private static final String MODE_GLOW = "Glow";
   private static final String MODE_BOX = "Box";
   private static final float GLOW_LINGER = 0.25F;
   private final EnumSetting mode = this.addSetting(new EnumSetting("Mode", "Glow", "Glow", "Box"));
   private final BooleanSetting customColor = this.addSetting(new BooleanSetting("Custom Color", true));
   private final ColorSetting color = this.addSetting(new ColorSetting("Color", -16777216));
   private final BooleanSetting themeColor = this.addSetting(new BooleanSetting("Theme Color", false));
   private final BooleanSetting reachHighlight = this.addSetting(new BooleanSetting("Reach Highlight", true));
   private final EnumSetting reachColorMode = this.addSetting(new EnumSetting("Reach Color", "Theme", "Theme", "Custom"));
   private final ColorSetting reachColor = this.addSetting(new ColorSetting("Reach Custom Color", -50373));
   private final NumberSetting boxSize = this.addSetting(new NumberSetting("Box Size", 1.0F, 0.6F, 1.6F));
   private final NumberSetting lineWidth = this.addSetting(new NumberSetting("Line Width", 2.5F, 0.5F, 8.0F));
   private final BooleanSetting glow = this.addSetting(new BooleanSetting("Box Glow", true));
   private final BooleanSetting glints = this.addSetting(new BooleanSetting("Corner Glints", true));
   private final NumberSetting fillOpacity = this.addSetting(new NumberSetting("Fill Opacity", 8.0F, 0.0F, 60.0F));
   private final BooleanSetting playersOnly = this.addSetting(new BooleanSetting("Players Only", true));
   private final BooleanSetting smooth = this.addSetting(new BooleanSetting("Smooth Movement", true));
   private final NumberSetting smoothSpeed = this.addSetting(new NumberSetting("Smooth Speed", 35.0F, 0.0F, 70.0F));
   private final BooleanSetting seeThrough = this.addSetting(new BooleanSetting("See Through", false));
   private final GizmoCanvas canvas = new GizmoCanvas();
   private final PlayerOverlayModule.a current = new PlayerOverlayModule.a();
   private final PlayerOverlayModule.a previous = new PlayerOverlayModule.a();
   private long lastFrame;
   private static PlayerOverlayModule instance;
   private LivingEntity glowTarget;
   private float glowLeft;
   private int glowRgb;
   private static final int[] CORNER_ORDER = new int[]{0, 1, 4, 5, 3, 2, 7, 6};

   public PlayerOverlayModule() {
      super(
         "Player Overlay",
         "Smoothly lights up the player you look at - a glow around them or their hitbox - in another colour once they are within your reach.",
         ModuleCategory.RENDER
      );
      instance = this;
      ColorSettingWiring.wireExclusiveColorSource(this.customColor, this.themeColor, this.color);
      this.reachColorMode.visibleWhen(this.reachHighlight::get);
      BooleanSupplier var1 = () -> this.mode.is("Box");
      this.boxSize.visibleWhen(var1);
      this.lineWidth.visibleWhen(var1);
      this.glow.visibleWhen(var1);
      this.glints.visibleWhen(var1);
      this.fillOpacity.visibleWhen(var1);
      this.seeThrough.visibleWhen(var1);
      this.smoothSpeed.visibleWhen(this.smooth::get);
      this.reachColor.visibleWhen(() -> this.reachHighlight.get() && this.reachColorMode.is("Custom"));
   }

   @Override
   protected void onDisable() {
      this.current.clear();
      this.previous.clear();
      this.lastFrame = 0L;
      this.glowTarget = null;
      this.glowLeft = 0.0F;
   }

   public static boolean isGlowPathActive() {
      PlayerOverlayModule var0 = instance;
      return var0 != null && var0.isEnabled() && var0.mode.is("Glow") && var0.glowTarget != null;
   }

   public static boolean shouldGlow(Entity entity) {
      PlayerOverlayModule var1 = instance;
      return var1 != null && entity == var1.glowTarget;
   }

   public static int glowColor(Entity entity) {
      PlayerOverlayModule var1 = instance;
      return var1 == null ? 0 : var1.glowRgb;
   }

   private void a(float f) {
      if (!this.mode.is("Glow")) {
         this.glowTarget = null;
      } else {
         if (this.current.entity != null) {
            this.glowTarget = this.current.entity;
            this.glowLeft = 0.25F;
         } else if (this.glowTarget != null) {
            this.glowLeft -= f;
            if (this.glowLeft <= 0.0F || this.glowTarget.isRemoved()) {
               this.glowTarget = null;
            }
         }

         PlayerOverlayModule.a var2 = this.current.entity != null ? this.current : this.previous;
         float var3 = var2.entity == this.glowTarget ? var2.reach : 0.0F;
         this.glowRgb = ColorHelper.lerp(var3, this.a(), this.b()) & 16777215;
      }
   }

   private int a() {
      return ColorSettingWiring.argb(this.themeColor, this.customColor, this.color, -16777216);
   }

   @EventListener
   public void onRender3D(Render3DEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientPlayerEntity var3 = var2.player;
      if (var2.world != null && var3 != null) {
         long var4 = System.nanoTime();
         float var6 = this.lastFrame == 0L ? 0.0F : Math.min((float)(var4 - this.lastFrame) / 1.0E9F, 0.1F);
         this.lastFrame = var4;
         float var7 = arg.partialTick;
         LivingEntity var8 = this.a(var2);
         if (var8 != this.current.entity) {
            if (this.current.entity != null && this.current.presence > 0.02F) {
               this.previous.copyFrom(this.current);
            }

            boolean var9 = this.smooth.get() && this.smoothSpeed.get() > 0.0F && this.current.entity != null && this.current.presence > 0.02F;
            this.current.entity = var8;
            this.current.presence = var9 ? this.current.presence : 0.0F;
            this.current.hasBox = var9 && this.current.hasBox;
         }

         this.current.presence = this.current.entity != null ? Math.min(1.0F, this.current.presence + var6 / 0.18F) : 0.0F;
         this.previous.presence = Math.max(0.0F, this.previous.presence - var6 / 0.3F);
         if (this.previous.presence <= 0.0F) {
            this.previous.entity = null;
         }

         float var13 = this.smooth.get() ? this.smoothSpeed.get() : 0.0F;
         this.current.follow(var7, var6, var13);
         this.previous.follow(var7, var6, 0.0F);
         double var10 = var3.getEntityInteractionRange();
         Vec3d var12 = var3.getCameraPosVec(var7);
         this.a(this.current, var12, var10, var6);
         this.a(this.previous, var12, var10, var6);
         this.a(var6);
         if (this.current.entity != null || this.previous.entity != null) {
            if (this.mode.is("Box")) {
               this.canvas.begin(var2.gameRenderer.getCamera());
               if (this.previous.entity != null) {
                  this.a(this.previous, var12, true);
               }

               if (this.current.entity != null) {
                  this.a(this.current, var12, false);
               }

               this.canvas.submit(this.seeThrough.get());
            }
         }
      } else {
         this.onDisable();
      }
   }

   private LivingEntity a(MinecraftClient minecraftClient) {
      if (!(
         minecraftClient.targetedEntity instanceof LivingEntity var3
            && var3.isAlive()
            && var3 != minecraftClient.player
            && !var3.isSpectator()
            && !(var3 instanceof ArmorStandEntity)
      )) {
         return null;
      } else {
         return this.playersOnly.get() && !(var3 instanceof PlayerEntity) ? null : var3;
      }
   }

   private void a(PlayerOverlayModule.a arg, Vec3d vec3d, double d, float f) {
      if (arg.entity != null && arg.hasBox) {
         boolean var6 = this.reachHighlight.get() && arg.distanceSqr(vec3d) <= d * d;
         arg.reach = arg.reach + ((var6 ? 1.0F : 0.0F) - arg.reach) * (1.0F - (float)Math.exp(-12.0F * f));
      }
   }

   private void a(PlayerOverlayModule.a arg, Vec3d vec3d, boolean bl) {
      if (arg.hasBox) {
         float var4 = arg.presence;
         float var5 = bl ? var4 * var4 : var4 * var4 * (3.0F - 2.0F * var4);
         if (!(var5 < 0.01F)) {
            double var6 = bl ? 1.0 + 0.06 * (1.0F - var4) : 0.92 + 0.08 * var5;
            double var8 = (arg.box[0] + arg.box[3]) * 0.5;
            double var10 = (arg.box[1] + arg.box[4]) * 0.5;
            double var12 = (arg.box[2] + arg.box[5]) * 0.5;
            double var14 = this.boxSize.get();
            double var16 = (arg.box[3] - arg.box[0]) * 0.5 * var6 * var14;
            double var18 = (arg.box[4] - arg.box[1]) * 0.5 * var6 * var14;
            double var20 = (arg.box[5] - arg.box[2]) * 0.5 * var6 * var14;
            double var22 = var8 - var16;
            double var24 = var10 - var18;
            double var26 = var12 - var20;
            double var28 = var8 + var16;
            double var30 = var10 + var18;
            double var32 = var12 + var20;
            int var34 = ColorHelper.lerp(arg.reach, this.a(), this.b());
            float var35 = this.fillOpacity.get() / 100.0F * var5;
            if (var35 > 0.01F) {
               this.a(var22, var24, var26, var28, var30, var32, ColorHelper.scaleAlpha(var34, var35));
            }

            float var36 = this.lineWidth.get();
            if (this.glow.get()) {
               float var37 = 0.85F + 0.15F * (float)(0.5 + 0.5 * Math.sin(System.nanoTime() / 1.0E9 * 3.0));
               float var38 = 1.0F + 0.5F * arg.reach;
               this.a(var22, var24, var26, var28, var30, var32, vec3d, ColorHelper.scaleAlpha(var34, 0.32F * var5 * var37), var36 + 3.0F * var38);
               this.a(var22, var24, var26, var28, var30, var32, vec3d, ColorHelper.scaleAlpha(var34, 0.12F * var5 * var37), var36 + 8.0F * var38);
            }

            this.a(var22, var24, var26, var28, var30, var32, vec3d, ColorHelper.scaleAlpha(var34, var5), var36);
            if (this.glints.get()) {
               int var44 = ColorHelper.lerp(0.55F, var34 | 0xFF000000, -1);
               double var45 = System.nanoTime() % 3600000000000L / 1.0E9;

               for (int var40 = 0; var40 < 8; var40++) {
                  double var41 = var45 * 2.4 - CORNER_ORDER[var40] * 0.78;
                  float var43 = (float)Math.pow(Math.max(0.0, Math.sin(var41)), 6.0);
                  this.canvas
                     .sprite(
                        EffectSprites.SPARK,
                        (var40 & 1) == 0 ? var22 : var28,
                        (var40 & 2) == 0 ? var24 : var30,
                        (var40 & 4) == 0 ? var26 : var32,
                        0.09F * (0.75F + 0.6F * var43),
                        (float)(var45 * 0.6),
                        ColorHelper.scaleAlpha(var44, var5 * (0.2F + 0.8F * var43))
                     );
               }
            }
         }
      }
   }

   private int b() {
      if (this.reachColorMode.is("Custom")) {
         return this.reachColor.argb();
      } else {
         int var1 = AccentColor.accent();
         return var1 & -16711936 | (var1 & 0xFF) << 16 | var1 >>> 16 & 0xFF;
      }
   }

   private void a(double d, double e, double f, double g, double h, double i, Vec3d vec3d, int j, float k) {
      this.b(d, e, f, g, e, f, vec3d, j, k);
      this.b(g, e, f, g, e, i, vec3d, j, k);
      this.b(g, e, i, d, e, i, vec3d, j, k);
      this.b(d, e, i, d, e, f, vec3d, j, k);
      this.b(d, h, f, g, h, f, vec3d, j, k);
      this.b(g, h, f, g, h, i, vec3d, j, k);
      this.b(g, h, i, d, h, i, vec3d, j, k);
      this.b(d, h, i, d, h, f, vec3d, j, k);
      this.b(d, e, f, d, h, f, vec3d, j, k);
      this.b(g, e, f, g, h, f, vec3d, j, k);
      this.b(g, e, i, g, h, i, vec3d, j, k);
      this.b(d, e, i, d, h, i, vec3d, j, k);
   }

   private void b(double d, double e, double f, double g, double h, double i, Vec3d vec3d, int j, float k) {
      double var16 = 0.004 / Math.max(1.0E-6, Math.sqrt(a(vec3d.x - d) + a(vec3d.y - e) + a(vec3d.z - f)));
      double var18 = 0.004 / Math.max(1.0E-6, Math.sqrt(a(vec3d.x - g) + a(vec3d.y - h) + a(vec3d.z - i)));
      this.canvas
         .line(
            d + (vec3d.x - d) * var16,
            e + (vec3d.y - e) * var16,
            f + (vec3d.z - f) * var16,
            g + (vec3d.x - g) * var18,
            h + (vec3d.y - h) * var18,
            i + (vec3d.z - i) * var18,
            j,
            k
         );
   }

   private void a(double d, double e, double f, double g, double h, double i, int j) {
      this.canvas.quad(g, e, f, g, h, f, g, h, i, g, e, i, j);
      this.canvas.quad(d, e, f, d, e, i, d, h, i, d, h, f, j);
      this.canvas.quad(d, e, f, d, h, f, g, h, f, g, e, f, j);
      this.canvas.quad(d, e, i, g, e, i, g, h, i, d, h, i, j);
      this.canvas.quad(d, h, f, d, h, i, g, h, i, g, h, f, j);
      this.canvas.quad(d, e, f, g, e, f, g, e, i, d, e, i, j);
   }

   private static double a(double d) {
      return d * d;
   }

   @Environment(EnvType.CLIENT)
   private static final class a {
      LivingEntity entity;
      final double[] box = new double[6];
      boolean hasBox;
      float presence;
      float reach;

      void clear() {
         this.entity = null;
         this.hasBox = false;
         this.presence = 0.0F;
         this.reach = 0.0F;
      }

      void copyFrom(PlayerOverlayModule.a arg) {
         this.entity = arg.entity;
         System.arraycopy(arg.box, 0, this.box, 0, 6);
         this.hasBox = arg.hasBox;
         this.presence = arg.presence;
         this.reach = arg.reach;
      }

      void follow(float f, float g, float h) {
         boolean var4 = h > 0.0F;
         LivingEntity var5 = this.entity;
         if (var5 != null && !var5.isRemoved()) {
            Box var6 = var5.getBoundingBox();
            double var7 = MathHelper.lerp(f, var5.lastX, var5.getX()) - var5.getX();
            double var9 = MathHelper.lerp(f, var5.lastY, var5.getY()) - var5.getY();
            double var11 = MathHelper.lerp(f, var5.lastZ, var5.getZ()) - var5.getZ();
            double var13 = var6.minX + var7;
            double var15 = var6.minY + var9;
            double var17 = var6.minZ + var11;
            double var19 = var6.maxX + var7;
            double var21 = var6.maxY + var9;
            double var23 = var6.maxZ + var11;
            boolean var25 = !this.hasBox
               || !var4
               || Math.abs((this.box[0] + this.box[3]) * 0.5 - (var13 + var19) * 0.5) + Math.abs((this.box[2] + this.box[5]) * 0.5 - (var17 + var23) * 0.5)
                  > 8.0;
            if (var25) {
               this.box[0] = var13;
               this.box[1] = var15;
               this.box[2] = var17;
               this.box[3] = var19;
               this.box[4] = var21;
               this.box[5] = var23;
               this.hasBox = true;
            } else {
               float var26 = 1.0F - (float)Math.exp(-h * g);
               this.box[0] = this.box[0] + (var13 - this.box[0]) * var26;
               this.box[1] = this.box[1] + (var15 - this.box[1]) * var26;
               this.box[2] = this.box[2] + (var17 - this.box[2]) * var26;
               this.box[3] = this.box[3] + (var19 - this.box[3]) * var26;
               this.box[4] = this.box[4] + (var21 - this.box[4]) * var26;
               this.box[5] = this.box[5] + (var23 - this.box[5]) * var26;
            }
         }
      }

      double distanceSqr(Vec3d vec3d) {
         double var2 = Math.max(Math.max(this.box[0] - vec3d.x, 0.0), vec3d.x - this.box[3]);
         double var4 = Math.max(Math.max(this.box[1] - vec3d.y, 0.0), vec3d.y - this.box[4]);
         double var6 = Math.max(Math.max(this.box[2] - vec3d.z, 0.0), vec3d.z - this.box[5]);
         return var2 * var2 + var4 * var4 + var6 * var6;
      }
   }
}
