package net.agartha.client.module.impl.misc;

import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SwingSpeedModule extends Module {
   private final NumberSetting speed = this.addSetting(new NumberSetting("Speed", 0.25F, 0.1F, 2.0F));

   public SwingSpeedModule() {
      super("Swing Speed", "Speeds up or slows down the hand swing animation.", ModuleCategory.MISC);
   }

   public int getModifiedSwingDuration(int i) {
      float var2 = this.speed.get();
      if (var2 <= 0.0F) {
         var2 = 0.1F;
      }

      return Math.max(1, Math.round(i / var2));
   }
}
