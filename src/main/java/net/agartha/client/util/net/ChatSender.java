package net.agartha.client.util.net;

import net.agartha.client.gui.AccentColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public final class ChatSender {
   public static final String PREFIX = "[Agartha+]";
   private static final int SILVER = 12107206;

   private ChatSender() {
   }

   public static MutableText build(String string, String string2) {
      String var2 = string2 == null ? "" : Formatting.strip(string2);
      if (string != null && !string.isBlank() && !"Agartha".equalsIgnoreCase(string.trim())) {
         var2 = string.trim() + ": " + var2;
      }

      return build(Text.literal(var2));
   }

   public static MutableText build(Text text) {
      return Text.literal("[")
         .append(Text.literal("Agartha+").fillStyle(Style.EMPTY.withColor(TextColor.fromRgb(a()))))
         .append(Text.literal("] "))
         .append(text.copy().styled(style -> style.getColor() == null ? style.withColor(Formatting.WHITE) : style));
   }

   private static int a() {
      int var0 = AccentColor.current().rgb() & 16777215;
      int var1 = var0 >> 16 & 0xFF;
      int var2 = var0 >> 8 & 0xFF;
      int var3 = var0 & 0xFF;
      return Math.min(var1, Math.min(var2, var3)) >= 200 ? 12107206 : var0;
   }

   public static void send(String string, String string2) {
      send(build(string, string2));
   }

   public static void send(String string) {
      send(build(null, string));
   }

   public static void send(Text text) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (!var1.isOnThread()) {
         var1.execute(() -> send(text));
      } else if (var1.inGameHud != null) {
         var1.inGameHud.getChatHud().addMessage(text);
      }
   }
}
