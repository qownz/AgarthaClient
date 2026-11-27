package net.agartha.client.module.impl.render;

import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.render.post.MotionBlurAlgorithm;
import net.agartha.client.render.post.MotionBlurRenderer;
import net.agartha.client.util.net.ChatSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class MotionBlurModule extends Module {
   private final EnumSetting sAlgorithm = this.addSetting(
      new EnumSetting("Algorithm", MotionBlurAlgorithm.VELOCITY.label(), MotionBlurAlgorithm.labels())
   );
   private final NumberSetting sStrength = this.addSetting(new NumberSetting("Strength", 1.0F, 0.0F, 2.0F));
   private final BooleanSetting sRefreshScaling = this.addSetting(new BooleanSetting("Refresh Rate Scaling", true));

   public MotionBlurModule() {
      super("Motion Blur", "Blurs the world with motion: velocity, frame blending, hybrid or accumulation trails.", ModuleCategory.RENDER);
      this.sStrength.visibleWhen(() -> this.algorithm().hasStrength());
      this.sRefreshScaling.visibleWhen(() -> this.algorithm().allowsRefreshScaling());
   }

   public MotionBlurAlgorithm algorithm() {
      return MotionBlurAlgorithm.byLabel(this.sAlgorithm.get());
   }

   public float strength() {
      return this.algorithm().hasStrength() ? this.sStrength.get() : 1.0F;
   }

   public boolean refreshScaling() {
      return this.algorithm().allowsRefreshScaling() && this.sRefreshScaling.get();
   }

   public static boolean naturalMotionBlurInstalled() {
      return FabricLoader.getInstance().isModLoaded("naturalmotionblur");
   }

   @Override
   protected boolean canEnable() {
      if (naturalMotionBlurInstalled()) {
         ChatSender.send("Motion Blur", "Natural Motion Blur is installed, so this stays off. Remove it to use Motion Blur.");
         return false;
      } else {
         return true;
      }
   }

   @Override
   protected void onDisable() {
      MotionBlurRenderer.release();
   }
}
