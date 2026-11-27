package net.agartha.client.module.impl.render;

import java.util.ArrayDeque;
import java.util.Iterator;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.Render3DEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ColorSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.util.color.ColorSettingWiring;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

@Environment(EnvType.CLIENT)
public final class JumpRingsModule extends Module {
   private static final int MAX_TRACKED = 64;
   private static final double GROUND_OFFSET = 0.02;
   private static final int MIN_SEGMENTS = 24;
   private static final int MAX_SEGMENTS = 72;
   private final NumberSetting radius = this.addSetting(new NumberSetting("Radius", 0.7F, 0.3F, 4.0F));
   private final NumberSetting lifetime = this.addSetting(new NumberSetting("Lifetime", 0.7F, 0.2F, 3.0F));
   private final NumberSetting thickness = this.addSetting(new NumberSetting("Thickness", 2.5F, 0.5F, 8.0F));
   private final BooleanSetting themeColor = this.addSetting(new BooleanSetting("Theme Color", true));
   private final BooleanSetting customColor = this.addSetting(new BooleanSetting("Custom Color", false));
   private final ColorSetting color = this.addSetting(new ColorSetting("Color", -11549705));
   private final BooleanSetting logo = this.addSetting(new BooleanSetting("Logo", true));
   private final BooleanSetting ring = this.addSetting(new BooleanSetting("Ring", true));
   private final BooleanSetting shockwave = this.addSetting(new BooleanSetting("Shockwave", false));
   private final NumberSetting maxRings = this.addSetting(new NumberSetting("Max Rings", 8.0F, 1.0F, 32.0F));
   private final ArrayDeque<JumpRingsModule.a> rings = new ArrayDeque<>();

   public Iterable<JumpRingsModule.a> ringsView() {
      return this.rings;
   }

   public boolean hasRings() {
      return this.isEnabled() && !this.rings.isEmpty();
   }

   public boolean logo() {
      return this.logo.get();
   }

   public float lifetimeSeconds() {
      return Math.max(0.05F, this.lifetime.get());
   }

   public float baseRadius() {
      return this.radius.get();
   }

   public int currentRgb() {
      return ColorSettingWiring.argb(this.themeColor, this.customColor, this.color, -1) & 16777215;
   }

   public JumpRingsModule() {
      super("Jump Rings", "Spawns an expanding ring where you jump.", ModuleCategory.RENDER);
      ColorSettingWiring.wireExclusiveColorSource(this.customColor, this.themeColor, this.color, true);
   }

   @Override
   protected void onDisable() {
      this.rings.clear();
   }

   public void onJump(Entity entity) {
      if (this.isEnabled()) {
         int var2 = Math.min(64, Math.max(1, this.maxRings.getInt()));

         while (this.rings.size() >= var2) {
            this.rings.pollFirst();
         }

         this.rings.addLast(new JumpRingsModule.a(entity.getX(), entity.getY() + 0.02, entity.getZ(), System.nanoTime()));
      }
   }

   @EventListener
   public void onRender3D(Render3DEvent arg) {
      if (!this.rings.isEmpty()) {
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (var2.player != null && var2.world != null) {
            long var3 = System.nanoTime();
            float var5 = Math.max(0.05F, this.lifetime.get());
            float var6 = this.radius.get();
            float var7 = this.thickness.get();
            Iterator var8 = this.rings.iterator();

            while (var8.hasNext()) {
               JumpRingsModule.a var9 = (JumpRingsModule.a)var8.next();
               float var10 = (float)(var3 - var9.spawnNanos) / 1.0E9F;
               if (var10 >= var5) {
                  var8.remove();
               } else {
                  float var11 = a(var10 / var5);
                  int var12 = this.currentRgb();
                  if (this.ring.get()) {
                     this.a(var9, var6 * (0.25F + b(var11) * 0.95F), var12, 1.0F - var11, var7);
                  }

                  if (this.shockwave.get()) {
                     float var13 = a(var11 * 1.6F);
                     if (var13 < 1.0F) {
                        this.a(var9, var6 * (0.3F + b(var13) * 1.8F), var12, (1.0F - var13) * 0.45F, var7 * 0.6F);
                     }
                  }
               }
            }
         } else {
            this.rings.clear();
         }
      }
   }

   private void a(JumpRingsModule.a arg, float f, int i, float g, float h) {
      if (!(f <= 0.01F) && !(g <= 0.01F)) {
         int var6 = a(i, g);
         int var7 = a(arg, f);
         double var8 = (Math.PI * 2) / var7;
         double var10 = arg.x + f;
         double var12 = arg.z;

         for (int var14 = 1; var14 <= var7; var14++) {
            double var15 = var8 * var14;
            double var17 = arg.x + f * Math.cos(var15);
            double var19 = arg.z + f * Math.sin(var15);
            GizmoDrawing.line(new Vec3d(var10, arg.y, var12), new Vec3d(var17, arg.y, var19), var6, h).ignoreOcclusion();
            var10 = var17;
            var12 = var19;
         }
      }
   }

   private static int a(JumpRingsModule.a arg, float f) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      double var3 = 8.0;
      if (var2.gameRenderer != null && var2.gameRenderer.getCamera() != null) {
         Vec3d var5 = var2.gameRenderer.getCamera().getCameraPos();
         var3 = Math.max(0.5, var5.distanceTo(new Vec3d(arg.x, arg.y, arg.z)));
      }

      int var6 = (int)Math.round(f / var3 * 260.0);
      return Math.max(24, Math.min(72, var6));
   }

   private static int a(int i, float f) {
      int var2 = Math.round(a(f) * 255.0F);
      return var2 << 24 | i & 16777215;
   }

   private static float a(float f) {
      return f < 0.0F ? 0.0F : (f > 1.0F ? 1.0F : f);
   }

   private static float b(float f) {
      float var1 = 1.0F - f;
      return 1.0F - var1 * var1 * var1;
   }

   @Environment(EnvType.CLIENT)
   public record a(double x, double y, double z, long spawnNanos) {
   }
}
