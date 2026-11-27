package net.agartha.client.util.net;

import net.agartha.client.util.net.ChatSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.MutableText;

@Environment(EnvType.CLIENT)
public final class ProtectMessages {
   private ProtectMessages() {
   }

   public static MutableText buildMessage(String string) {
      return ChatSender.build("Protect", string);
   }

   public static void send(String string) {
      ChatSender.send("Protect", string);
   }
}
