package net.agartha.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class b {
   private static final String HOST = "donutsmp.net";
   private static boolean done;

   private b() {
   }

   public static void tick(MinecraftClient minecraftClient) {
   }
}
