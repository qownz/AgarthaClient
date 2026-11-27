package net.agartha.client.platform;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class PlatformSupport {
   private PlatformSupport() {
   }

   public static boolean isSupported() {
      return Boolean.getBoolean("agartha.platform.force")
         || System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
   }

   public static String describe() {
      return System.getProperty("os.name", "unknown") + " " + System.getProperty("os.arch", "unknown");
   }
}
