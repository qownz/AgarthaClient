package net.agartha.client.util.net;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

@Environment(EnvType.CLIENT)
public final class ServerCheck {
   private static final String DOMAIN = "donutsmp.net";

   private ServerCheck() {
   }

   public static boolean connected() {
      ServerInfo var0 = MinecraftClient.getInstance().getCurrentServerEntry();
      if (var0 != null && var0.address != null) {
         String var1 = var0.address.trim().toLowerCase(Locale.ROOT);
         int var2 = var1.lastIndexOf(58);
         if (var2 >= 0) {
            var1 = var1.substring(0, var2);
         }

         while (var1.endsWith(".")) {
            var1 = var1.substring(0, var1.length() - 1);
         }

         return var1.equals("donutsmp.net") || var1.endsWith(".donutsmp.net");
      } else {
         return false;
      }
   }
}
