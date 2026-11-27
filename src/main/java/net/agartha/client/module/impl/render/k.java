package net.agartha.client.module.impl.render;

import net.agartha.client.util.ab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class k extends net.agartha.client.module.b {
   private final net.agartha.client.module.a_pkg.i sAlgorithm = this.addSetting(
      new net.agartha.client.module.a_pkg.i("Algorithm", net.agartha.client.l.a_pkg.c.VELOCITY.label(), net.agartha.client.l.a_pkg.c.labels())
   );
   private final net.agartha.client.module.a_pkg.j sStrength = this.addSetting(new net.agartha.client.module.a_pkg.j("Strength", 1.0F, 0.0F, 2.0F));
   private final net.agartha.client.module.a_pkg.b sRefreshScaling = this.addSetting(new net.agartha.client.module.a_pkg.b("Refresh Rate Scaling", true));

   public k() {
      super("Motion Blur", "Blurs the world with motion: velocity, frame blending, hybrid or accumulation trails.", net.agartha.client.module.c.RENDER);
      this.sStrength.visibleWhen(() -> this.algorithm().hasStrength());
      this.sRefreshScaling.visibleWhen(() -> this.algorithm().allowsRefreshScaling());
   }

   public net.agartha.client.l.a_pkg.c algorithm() {
      return net.agartha.client.l.a_pkg.c.byLabel(this.sAlgorithm.get());
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
         ab.send("Motion Blur", "Natural Motion Blur is installed, so this stays off. Remove it to use Motion Blur.");
         return false;
      } else {
         return true;
      }
   }

   @Override
   protected void onDisable() {
      net.agartha.client.l.a_pkg.d.release();
   }
}
