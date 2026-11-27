package net.agartha.client.module.impl.client;

import net.agartha.client.module.b;
import net.agartha.client.module.a_pkg.c;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.util.ac;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class CrosshairModule extends b {
   private static final float VANILLA_SPRITE_SIZE = 15.0F;
   private static final int DEFAULT_TINT = -1;
   private final j size = (j)this.addSetting(new j("Size", 0.65F, 0.25F, 2.0F));
   private final net.agartha.client.module.a_pkg.b customColor = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Custom Color", true)
   );
   private final net.agartha.client.module.a_pkg.b themeColor = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Theme Color", false)
   );
   private final c color = (c)this.addSetting(new c("Color", -1));

   public CrosshairModule() {
      super("Crosshair", "Resize and recolor the HUD crosshair.", net.agartha.client.module.c.CLIENT);
      ac.wireExclusiveColorSource(this.customColor, this.themeColor, this.color);
   }

   public float scale() {
      return this.size.get();
   }

   public int drawSizePx() {
      return Math.max(1, Math.round(15.0F * this.scale()));
   }

   public int tintArgb() {
      return ac.argb(this.themeColor, this.customColor, this.color, -1);
   }
}
