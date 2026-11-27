package net.agartha.client.module.impl.render;

import net.agartha.client.event.impl.MouseScrollEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public final class FreelookModule extends Module {
   private float yaw;
   private float pitch;
   private final NumberSetting sDistance = this.addSetting(new NumberSetting("Distance", 4.0F, 1.0F, 20.0F));
   private final BooleanSetting sScrollZoom = this.addSetting(new BooleanSetting("Scroll Zoom", true));
   private final BooleanSetting sSmoothOpen = this.addSetting(new BooleanSetting("Smooth Open", true));
   private final BooleanSetting sSmoothZoom = this.addSetting(new BooleanSetting("Smooth Zoom", true));
   private boolean opening;
   private float zoomed = 4.0F;
   private final BooleanSetting sShowHands = this.addSetting(new BooleanSetting("Show Hands", false));
   private final BooleanSetting sWallClip = this.addSetting(new BooleanSetting("Wall Clip", false));
   private static final float ZOOM_GLIDE = 12.0F;
   private static final float START_DISTANCE = 0.3F;
   private float shownDistance = 0.3F;
   private long shownAt;

   public FreelookModule() {
      super("Freelook", "Detach the camera view direction while your player keeps facing forward.", ModuleCategory.MISC);
      this.sSmoothZoom.visibleWhen(this.sScrollZoom::get);
   }

   @Override
   public boolean persistEnabled() {
      return false;
   }

   @Override
   protected void onEnable() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.player != null) {
         this.yaw = var1.player.getYaw();
         this.pitch = var1.player.getPitch();
      }

      this.zoomed = this.sDistance.get();
      this.shownDistance = 0.3F;
      this.shownAt = 0L;
      this.opening = true;
      this.on(MouseScrollEvent.TYPE, this::a);
   }

   public void changeLookDirection(double d, double e) {
      this.yaw = MathHelper.wrapDegrees(this.yaw + (float)(d * 0.15));
      this.pitch = MathHelper.clamp(this.pitch + (float)(e * 0.15), -90.0F, 90.0F);
   }

   public float yaw() {
      return this.yaw;
   }

   public float pitch() {
      return this.pitch;
   }

   public boolean showHands() {
      return this.sShowHands.get();
   }

   public static boolean hidesCrosshair() {
      FreelookModule var0 = ModuleManager.get(FreelookModule.class);
      return var0 != null && var0.isEnabled();
   }

   public boolean wallClip() {
      return this.sWallClip.get();
   }

   public float distance() {
      float var1 = this.sScrollZoom.get() ? this.zoomed : this.sDistance.get();
      long var2 = System.nanoTime();
      float var4 = this.shownAt == 0L ? 0.0F : Math.min((float)(var2 - this.shownAt) / 1.0E9F, 0.1F);
      this.shownAt = var2;
      boolean var5 = this.opening ? this.sSmoothOpen.get() : this.sSmoothZoom.get();
      if (!var5) {
         this.shownDistance = var1;
      } else {
         this.shownDistance = this.shownDistance + (var1 - this.shownDistance) * (1.0F - (float)Math.exp(-12.0F * var4));
      }

      if (this.opening && Math.abs(var1 - this.shownDistance) < 0.05F) {
         this.opening = false;
      }

      return this.shownDistance;
   }

   private void a(MouseScrollEvent arg) {
      if (this.sScrollZoom.get()) {
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (var2.player != null && var2.currentScreen == null) {
            float var3 = Math.max(0.35F, this.zoomed * 0.12F);
            this.zoomed = MathHelper.clamp(this.zoomed - (float)arg.value * var3, this.sDistance.min(), this.sDistance.max());
            arg.setCancelled(true);
         }
      }
   }
}
