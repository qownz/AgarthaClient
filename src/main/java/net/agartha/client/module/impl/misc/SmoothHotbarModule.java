package net.agartha.client.module.impl.misc;

import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SmoothHotbarModule extends Module {
   private final NumberSetting sSpeed = this.addSetting(new NumberSetting("Speed", 50.0F, 5.0F, 100.0F));

   public SmoothHotbarModule() {
      super("Smooth Hotbar", "Slides the hotbar selection box smoothly between slots.", ModuleCategory.MISC);
   }

   public float speed() {
      return this.sSpeed.get();
   }
}
