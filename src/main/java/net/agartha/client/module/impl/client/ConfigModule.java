package net.agartha.client.module.impl.client;

import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ConfigModule extends b {
   public ConfigModule() {
      super("Config", "Save, load, import and export complete client configs.", c.CLIENT);
   }

   public boolean persistEnabled() {
      return false;
   }
}
