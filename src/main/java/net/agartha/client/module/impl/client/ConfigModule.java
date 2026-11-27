package net.agartha.client.module.impl.client;

import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ConfigModule extends Module {
   public ConfigModule() {
      super("Config", "Save, load, import and export complete client configs.", ModuleCategory.CLIENT);
   }

   public boolean persistEnabled() {
      return false;
   }
}
