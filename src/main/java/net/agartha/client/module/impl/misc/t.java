package net.agartha.client.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class t extends net.agartha.client.module.b {
   private final net.agartha.client.module.a_pkg.j speed = this.addSetting(new net.agartha.client.module.a_pkg.j("Speed", 0.25F, 0.1F, 2.0F));

   public t() {
      super("Swing Speed", "Speeds up or slows down the hand swing animation.", net.agartha.client.module.c.MISC);
   }

   public int getModifiedSwingDuration(int i) {
      float var2 = this.speed.get();
      if (var2 <= 0.0F) {
         var2 = 0.1F;
      }

      return Math.max(1, Math.round(i / var2));
   }
}
