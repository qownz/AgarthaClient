package net.agartha.client.macro;

import net.agartha.client.util.net.MoneyCommandGuard;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class ChatSender {
   private ChatSender() {
   }

   public static void sendPlayerMsg(String string) {
      sendPlayerMsg(string, true);
   }

   public static void sendPlayerMsg(String string, boolean bl) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null) {
         if (MoneyCommandGuard.allowAutomated(string, "Chat Macros")) {
            if (bl) {
               var2.inGameHud.getChatHud().addToMessageHistory(string);
            }

            if (string.startsWith("/")) {
               var2.player.networkHandler.sendChatCommand(string.substring(1));
            } else {
               var2.player.networkHandler.sendChatMessage(string);
            }
         }
      }
   }
}
