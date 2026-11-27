package net.agartha.client.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class p extends net.agartha.client.module.b {
   private final net.agartha.client.module.a_pkg.j sSpeed = this.addSetting(new net.agartha.client.module.a_pkg.j("Speed", 50.0F, 5.0F, 100.0F));

   public p() {
      super("Smooth Hotbar", "Slides the hotbar selection box smoothly between slots.", net.agartha.client.module.c.MISC);
   }

   public float speed() {
      return this.sSpeed.get();
   }
}
