package net.agartha.client.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class v extends net.agartha.client.module.b {
   private static final int BASE_ZOOM_PER_STEP = 130;
   private final net.agartha.client.module.a_pkg.j zoom = this.addSetting(new net.agartha.client.module.a_pkg.j("Zoom", 6.0F, 1.0F, 20.0F));
   private final net.agartha.client.module.a_pkg.j scrollSensitivity = this.addSetting(new net.agartha.client.module.a_pkg.j("Scroll Sensitivity", 1.0F, 0.0F, 5.0F));
   private final net.agartha.client.module.a_pkg.b smooth = this.addSetting(new net.agartha.client.module.a_pkg.b("Smooth", true));
   private final net.agartha.client.module.a_pkg.b cinematic = this.addSetting(new net.agartha.client.module.a_pkg.b("Cinematic", true));
   private final net.agartha.client.module.a_pkg.b hideHud = this.addSetting(new net.agartha.client.module.a_pkg.b("Hide HUD", false));
   private final net.agartha.client.module.a_pkg.b showHands = this.addSetting(new net.agartha.client.module.a_pkg.b("Show Hands", true));
   private final net.agartha.client.p.a helper = new net.agartha.client.p.a();
   private boolean preCinematic;
   private boolean hudManualToggled;

   public v() {
      super("Zoom", "Zooms your view.", net.agartha.client.module.c.RENDER);
      this.setHoldMode(true);
      this.showHands.visibleWhen(() -> !this.hideHud.get());
      net.agartha.client.d.a_pkg.q.TYPE.listen(this, this::a);
      net.agartha.client.d.a_pkg.f.TYPE.listen(this, this::a);
      net.agartha.client.d.a_pkg.h.TYPE.listen(this, this::a);
   }

   @Override
   protected void onEnable() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      this.preCinematic = var1.options.smoothCameraEnabled;
      if (this.hideHud.get() && !var1.options.hudHidden) {
         this.hudManualToggled = false;
         var1.options.hudHidden = true;
      }

      if (var1.world != null && var1.player != null) {
         this.helper.tick(true, this.smooth.get());
      }
   }

   @Override
   protected void onDisable() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.world != null && var1.player != null) {
         this.helper.tick(false, this.smooth.get());
      }

      var1.options.smoothCameraEnabled = this.preCinematic;
      if (this.hideHud.get() && !this.hudManualToggled) {
         var1.options.hudHidden = false;
      }

      this.helper.resetScroll();
      this.helper.clearScrollSteps();
   }

   private void a(net.agartha.client.d.a_pkg.q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world == null || var2.player == null) {
         this.helper.forceReset();
         var2.options.smoothCameraEnabled = this.preCinematic;
      } else if (this.isEnabled() || this.helper.isAnimating()) {
         boolean var3 = this.isEnabled();
         this.helper.tick(var3, this.smooth.get());
         if (var3 && this.cinematic.get()) {
            var2.options.smoothCameraEnabled = true;
         } else if (!var3 && !this.helper.isAnimating()) {
            var2.options.smoothCameraEnabled = this.preCinematic;
         }
      }
   }

   public float getZoomDivisor(float f) {
      return !this.a() ? 1.0F : this.helper.getZoomDivisor(f, Math.max(1, Math.round(this.zoom.get())), this.b(), this.smooth.get());
   }

   public double getPreviousZoomDivisor() {
      return this.helper.getPreviousDivisor();
   }

   public boolean affectHandFov() {
      return this.showHands.get();
   }

   public boolean isZooming() {
      return this.isEnabled();
   }

   public boolean isZoomActive() {
      return this.a() && (this.isEnabled() || this.helper.isAnimating());
   }

   public boolean cinematicCamera() {
      return this.cinematic.get() && this.isEnabled() && this.a();
   }

   public int relativeSensitivityPercent() {
      return 100;
   }

   private boolean a() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      return var1.world != null && var1.player != null && !var1.isPaused();
   }

   private int b() {
      return 130 + Math.round(this.scrollSensitivity.get() * 8.0F);
   }

   private void a(net.agartha.client.d.a_pkg.f arg) {
      if (arg.key == 290) {
         this.hudManualToggled = true;
      }
   }

   private void a(net.agartha.client.d.a_pkg.h arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.currentScreen == null && !(this.scrollSensitivity.get() <= 0.0F) && this.isEnabled() && this.a()) {
         int var3 = arg.value > 0.0 ? 1 : (arg.value < 0.0 ? -1 : 0);
         if (var3 != 0) {
            this.helper.addScrollStep(var3);
            arg.setCancelled(true);
         }
      }
   }
}
