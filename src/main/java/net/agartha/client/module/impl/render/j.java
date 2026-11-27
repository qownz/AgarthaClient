package net.agartha.client.module.impl.render;

import java.util.ArrayDeque;
import java.util.Iterator;
import net.agartha.client.util.ac;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

@Environment(EnvType.CLIENT)
public final class j extends net.agartha.client.module.b {
   private static final int MAX_TRACKED = 64;
   private static final double GROUND_OFFSET = 0.02;
   private static final int MIN_SEGMENTS = 24;
   private static final int MAX_SEGMENTS = 72;
   private final net.agartha.client.module.a_pkg.j radius = this.addSetting(new net.agartha.client.module.a_pkg.j("Radius", 0.7F, 0.3F, 4.0F));
   private final net.agartha.client.module.a_pkg.j lifetime = this.addSetting(new net.agartha.client.module.a_pkg.j("Lifetime", 0.7F, 0.2F, 3.0F));
   private final net.agartha.client.module.a_pkg.j thickness = this.addSetting(new net.agartha.client.module.a_pkg.j("Thickness", 2.5F, 0.5F, 8.0F));
   private final net.agartha.client.module.a_pkg.b themeColor = this.addSetting(new net.agartha.client.module.a_pkg.b("Theme Color", true));
   private final net.agartha.client.module.a_pkg.b customColor = this.addSetting(new net.agartha.client.module.a_pkg.b("Custom Color", false));
   private final net.agartha.client.module.a_pkg.c color = this.addSetting(new net.agartha.client.module.a_pkg.c("Color", -11549705));
   private final net.agartha.client.module.a_pkg.b logo = this.addSetting(new net.agartha.client.module.a_pkg.b("Logo", true));
   private final net.agartha.client.module.a_pkg.b ring = this.addSetting(new net.agartha.client.module.a_pkg.b("Ring", true));
   private final net.agartha.client.module.a_pkg.b shockwave = this.addSetting(new net.agartha.client.module.a_pkg.b("Shockwave", false));
   private final net.agartha.client.module.a_pkg.j maxRings = this.addSetting(new net.agartha.client.module.a_pkg.j("Max Rings", 8.0F, 1.0F, 32.0F));
   private final ArrayDeque<j.a> rings = new ArrayDeque<>();

   public Iterable<j.a> ringsView() {
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
      return ac.argb(this.themeColor, this.customColor, this.color, -1) & 16777215;
   }

   public j() {
      super("Jump Rings", "Spawns an expanding ring where you jump.", net.agartha.client.module.c.RENDER);
      ac.wireExclusiveColorSource(this.customColor, this.themeColor, this.color, true);
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

         this.rings.addLast(new j.a(entity.getX(), entity.getY() + 0.02, entity.getZ(), System.nanoTime()));
      }
   }

   @net.agartha.client.d.f
   public void onRender3D(net.agartha.client.d.a_pkg.o arg) {
      if (!this.rings.isEmpty()) {
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (var2.player != null && var2.world != null) {
            long var3 = System.nanoTime();
            float var5 = Math.max(0.05F, this.lifetime.get());
            float var6 = this.radius.get();
            float var7 = this.thickness.get();
            Iterator var8 = this.rings.iterator();

            while (var8.hasNext()) {
               j.a var9 = (j.a)var8.next();
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

   private void a(j.a arg, float f, int i, float g, float h) {
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

   private static int a(j.a arg, float f) {
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
