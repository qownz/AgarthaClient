package net.agartha.client.config;

import net.agartha.client.gui.ClickGuiLayout;
import net.agartha.client.gui.HudLayout;
import net.agartha.client.gui.UiScale;
import net.agartha.client.macro.MacroManager;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.client.AgarthaModule;
import net.agartha.client.module.impl.client.ChatMacrosModule;
import net.agartha.client.module.impl.client.HudModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class ConfigResetService {
   private ConfigResetService() {
   }

   public static void resetAll(MinecraftClient minecraftClient) {
      if (minecraftClient == null) {
         minecraftClient = MinecraftClient.getInstance();
      }

      for (Module var2 : ModuleManager.all()) {
         var2.setEnabled(false);
         var2.resetToDefaults();
      }

      Module var4 = ModuleManager.get(AgarthaModule.class);
      if (var4 != null) {
         var4.setEnabled(true);
      }

      Module var5 = ModuleManager.get(HudModule.class);
      if (var5 != null) {
         var5.setEnabled(true);
      }

      Module var3 = ModuleManager.get(ChatMacrosModule.class);
      if (var3 != null) {
         var3.setEnabled(true);
      }

      MacroManager.getInstance().resetToDefaults();
      ModuleConfigManager.getInstance().save();
      HudLayout.getInstance().resetToDefaults(UiScale.width(minecraftClient), UiScale.height(minecraftClient));
      ClickGuiLayout.getInstance().resetToDefaults();
   }
}
