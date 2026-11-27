package net.agartha.client.module.impl.client;

import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.module.e;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MonsterModeModule extends b {
   public MonsterModeModule() {
      super("Monster Mode", "Gives the HUD icon and the theme picker an energy-drink can look.", c.CLIENT);
   }

   public static boolean isActive() {
      MonsterModeModule var0 = (MonsterModeModule)e.get(MonsterModeModule.class);
      return var0 != null && var0.isEnabled();
   }
}
