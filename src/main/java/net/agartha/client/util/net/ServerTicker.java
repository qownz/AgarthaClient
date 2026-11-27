package net.agartha.client.util.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class ServerTicker {
   private static final String HOST = "donutsmp.net";
   private static boolean done;

   private ServerTicker() {
   }

   public static void tick(MinecraftClient minecraftClient) {
   }
}
