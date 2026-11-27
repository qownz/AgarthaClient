package net.agartha.client.module.impl.client;

import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.ModuleManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MonsterModeModule extends Module {
   public MonsterModeModule() {
      super("Monster Mode", "Gives the HUD icon and the theme picker an energy-drink can look.", ModuleCategory.CLIENT);
   }

   public static boolean isActive() {
      MonsterModeModule var0 = (MonsterModeModule)ModuleManager.get(MonsterModeModule.class);
      return var0 != null && var0.isEnabled();
   }
}
