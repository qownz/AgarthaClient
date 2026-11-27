package net.agartha.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ClientConstants {
   public static final boolean DEBUG = false;
   public static final String LICENSE_URL = "-";
   public static final String LICENSE_PUBLIC_KEY = "-";
   public static final String LICENSE_KEY = "";
   public static final String MOD_VERSION = "0.8.276+mc1.21.11";
   public static final boolean AUTO_JOIN_DONUT = false;

   private ClientConstants() {
   }
}
