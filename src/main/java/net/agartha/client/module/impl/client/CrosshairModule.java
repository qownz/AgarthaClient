package net.agartha.client.module.impl.client;

import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ColorSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.util.color.ColorSettingWiring;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class CrosshairModule extends Module {
   private static final float VANILLA_SPRITE_SIZE = 15.0F;
   private static final int DEFAULT_TINT = -1;
   private final NumberSetting size = (NumberSetting)this.addSetting(new NumberSetting("Size", 0.65F, 0.25F, 2.0F));
   private final BooleanSetting customColor = (BooleanSetting)this.addSetting(
      new BooleanSetting("Custom Color", true)
   );
   private final BooleanSetting themeColor = (BooleanSetting)this.addSetting(
      new BooleanSetting("Theme Color", false)
   );
   private final ColorSetting color = (ColorSetting)this.addSetting(new ColorSetting("Color", -1));

   public CrosshairModule() {
      super("Crosshair", "Resize and recolor the HUD crosshair.", ModuleCategory.CLIENT);
      ColorSettingWiring.wireExclusiveColorSource(this.customColor, this.themeColor, this.color);
   }

   public float scale() {
      return this.size.get();
   }

   public int drawSizePx() {
      return Math.max(1, Math.round(15.0F * this.scale()));
   }

   public int tintArgb() {
      return ColorSettingWiring.argb(this.themeColor, this.customColor, this.color, -1);
   }
}
