package net.agartha.client.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum c {
   MISC("Misc", "[+]"),
   DONUT("Donut", "[D]"),
   RENDER("Render", "[*]"),
   CLIENT("Client", "[C]");

   private final String displayName;
   private final String icon;

   private c(String string2, String string3) {
      this.displayName = string2;
      this.icon = string3;
   }

   public String displayName() {
      return this.displayName;
   }

   public String icon() {
      return this.icon;
   }
}
