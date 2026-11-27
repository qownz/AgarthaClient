package net.agartha.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class af {
   private af() {
   }

   public static int chunks() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      int var1 = var0 == null ? 8 : var0.options.getClampedViewDistance();
      return Math.max(2, var1);
   }

   public static double blocks() {
      return chunks() * 16.0;
   }

   public static double blocksSq() {
      double var0 = blocks();
      return var0 * var0;
   }
}
