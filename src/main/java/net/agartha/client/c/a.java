package net.agartha.client.c;

import net.agartha.client.e.h;
import net.agartha.client.e.r;
import net.agartha.client.e.t;
import net.agartha.client.module.impl.client.AgarthaModule;
import net.agartha.client.module.impl.client.ChatMacrosModule;
import net.agartha.client.module.impl.client.HudModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class a {
   private a() {
   }

   public static void resetAll(MinecraftClient minecraftClient) {
      if (minecraftClient == null) {
         minecraftClient = MinecraftClient.getInstance();
      }

      for (net.agartha.client.module.b var2 : net.agartha.client.module.e.all()) {
         var2.setEnabled(false);
         var2.resetToDefaults();
      }

      net.agartha.client.module.b var4 = net.agartha.client.module.e.get(AgarthaModule.class);
      if (var4 != null) {
         var4.setEnabled(true);
      }

      net.agartha.client.module.b var5 = net.agartha.client.module.e.get(HudModule.class);
      if (var5 != null) {
         var5.setEnabled(true);
      }

      net.agartha.client.module.b var3 = net.agartha.client.module.e.get(ChatMacrosModule.class);
      if (var3 != null) {
         var3.setEnabled(true);
      }

      net.agartha.client.j.d.getInstance().resetToDefaults();
      e.getInstance().save();
      t.getInstance().resetToDefaults(r.width(minecraftClient), r.height(minecraftClient));
      h.getInstance().resetToDefaults();
   }
}
