package net.agartha.client.module.impl.client;

import net.agartha.client.event.impl.MouseScrollEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public final class UtilitiesModule extends Module {
   private final BooleanSetting sScreenshotCopy = (BooleanSetting)this.addSetting(
      new BooleanSetting("Screenshot Copy", false)
   );
   private final BooleanSetting sF5Zoom = (BooleanSetting)this.addSetting(new BooleanSetting("F5 Zoom", false));
   private final BooleanSetting sSmoothF5Zoom = (BooleanSetting)this.addSetting(
      new BooleanSetting("Smooth F5 Zoom", true)
   );
   private final BooleanSetting sSmoothChat = (BooleanSetting)this.addSetting(
      new BooleanSetting("Smooth Chat", false)
   );
   private static final float DEFAULT_DISTANCE = 4.0F;
   private static final float MIN_DISTANCE = 1.5F;
   private static final float MAX_DISTANCE = 16.0F;
   private static final float STEP = 1.0F;
   private float distance = 4.0F;
   private static final float ZOOM_GLIDE = 14.0F;
   private float shown = Float.NaN;
   private long shownAt;

   public UtilitiesModule() {
      super("Utilities", "Small quality-of-life toggles: screenshot copy, F5 zoom, smooth chat.", ModuleCategory.CLIENT);
      this.sSmoothF5Zoom.visibleWhen(this.sF5Zoom::get);
   }

   protected void onEnable() {
      this.distance = 4.0F;
      this.on(MouseScrollEvent.TYPE, this::onScroll);
   }

   public boolean screenshotCopy() {
      return this.isEnabled() && this.sScreenshotCopy.get();
   }

   public boolean f5Zoom() {
      return this.isEnabled() && this.sF5Zoom.get();
   }

   public boolean smoothChat() {
      return this.isEnabled() && this.sSmoothChat.get();
   }

   public float f5Distance() {
      long var1 = System.nanoTime();
      float var3 = this.shownAt == 0L ? 1.0F : Math.min((float)(var1 - this.shownAt) / 1.0E9F, 0.1F);
      this.shownAt = var1;
      if (!Float.isNaN(this.shown) && !(var3 >= 1.0F) && this.sSmoothF5Zoom.get()) {
         this.shown = this.shown + (this.distance - this.shown) * (1.0F - (float)Math.exp(-14.0F * var3));
      } else {
         this.shown = this.distance;
      }

      return this.shown;
   }

   private void onScroll(MouseScrollEvent arg) {
      if (this.f5Zoom()) {
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (var2.player != null && !var2.options.getPerspective().isFirstPerson()) {
            float var3 = Math.max(0.5F, this.distance / 4.0F);
            this.distance = MathHelper.clamp(this.distance - (float)(arg.value * 1.0 * var3), 1.5F, 16.0F);
            arg.setCancelled(true);
         }
      }
   }
}
