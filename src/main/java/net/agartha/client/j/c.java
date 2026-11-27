package net.agartha.client.j;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class c {
   private c() {
   }

   public static void sendPlayerMsg(String string) {
      sendPlayerMsg(string, true);
   }

   public static void sendPlayerMsg(String string, boolean bl) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null) {
         if (net.agartha.client.util.f.allowAutomated(string, "Chat Macros")) {
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
