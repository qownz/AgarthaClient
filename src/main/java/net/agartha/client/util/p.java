package net.agartha.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.MutableText;

@Environment(EnvType.CLIENT)
public final class p {
   private p() {
   }

   public static MutableText buildMessage(String string) {
      return ab.build("Protect", string);
   }

   public static void send(String string) {
      ab.send("Protect", string);
   }
}
