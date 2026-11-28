package net.agartha.client.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class PlatformSupport {
   private PlatformSupport() {
   }

   public static String describe() {
      return System.getProperty("os.name", "unknown") + " " + System.getProperty("os.arch", "unknown");
   }
}
