package net.agartha.client.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class b {
   private static long cachedTick = Long.MIN_VALUE;
   private static boolean espActive;
   private static boolean itemActive;
   private static boolean netheriteActive;
   private static boolean playerActive;

   private b() {
   }

   public static boolean anyActive() {
      a();
      return espActive || itemActive || netheriteActive || playerActive || r.isGlowPathActive();
   }

   public static boolean espActive() {
      a();
      return espActive;
   }

   public static boolean itemActive() {
      a();
      return itemActive;
   }

   public static boolean playerActive() {
      a();
      return playerActive;
   }

   public static boolean overlayActive() {
      return r.isGlowPathActive();
   }

   public static boolean netheriteActive() {
      a();
      return netheriteActive;
   }

   private static void a() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      if (var0.world == null) {
         cachedTick = Long.MIN_VALUE;
         espActive = false;
         itemActive = false;
         netheriteActive = false;
         playerActive = false;
      } else {
         long var1 = var0.world.getTime();
         if (var1 != cachedTick) {
            cachedTick = var1;
            espActive = c.isGlowPathActive();
            itemActive = i.isGlowPathActive();
            netheriteActive = NetheriteFinderModule.isGlowPathActive();
            playerActive = q.isGlowPathActive();
         }
      }
   }
}
