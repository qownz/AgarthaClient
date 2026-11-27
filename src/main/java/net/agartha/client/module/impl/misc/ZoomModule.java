package net.agartha.client.module.impl.misc;

import net.agartha.client.event.impl.KeyInputEvent;
import net.agartha.client.event.impl.MouseScrollEvent;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.render.camera.ZoomInterpolator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class ZoomModule extends Module {
   private static final int BASE_ZOOM_PER_STEP = 130;
   private final NumberSetting zoom = this.addSetting(new NumberSetting("Zoom", 6.0F, 1.0F, 20.0F));
   private final NumberSetting scrollSensitivity = this.addSetting(new NumberSetting("Scroll Sensitivity", 1.0F, 0.0F, 5.0F));
   private final BooleanSetting smooth = this.addSetting(new BooleanSetting("Smooth", true));
   private final BooleanSetting cinematic = this.addSetting(new BooleanSetting("Cinematic", true));
   private final BooleanSetting hideHud = this.addSetting(new BooleanSetting("Hide HUD", false));
   private final BooleanSetting showHands = this.addSetting(new BooleanSetting("Show Hands", true));
   private final ZoomInterpolator helper = new ZoomInterpolator();
   private boolean preCinematic;
   private boolean hudManualToggled;

   public ZoomModule() {
      super("Zoom", "Zooms your view.", ModuleCategory.RENDER);
      this.setHoldMode(true);
      this.showHands.visibleWhen(() -> !this.hideHud.get());
      TickEvent.TYPE.listen(this, this::a);
      KeyInputEvent.TYPE.listen(this, this::a);
      MouseScrollEvent.TYPE.listen(this, this::a);
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

   private void a(TickEvent arg) {
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

   private void a(KeyInputEvent arg) {
      if (arg.key == 290) {
         this.hudManualToggled = true;
      }
   }

   private void a(MouseScrollEvent arg) {
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
