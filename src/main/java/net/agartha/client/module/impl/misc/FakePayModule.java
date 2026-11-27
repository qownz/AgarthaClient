package net.agartha.client.module.impl.misc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import net.agartha.client.d.f;
import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public final class FakePayModule extends b {
   private static final String COMMAND = "/pay";
   private static final String[] SUFFIXES = new String[]{"", "K", "M", "B", "T"};
   private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000L);
   private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999999999");
   private final net.agartha.client.module.a_pkg.b showUsage = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Usage Hints", true)
   );

   public FakePayModule() {
      super("Fake Pay", "Blocks every /pay and fakes the payment message in your own chat.", c.DONUT);
   }

   @f
   public void onChatSend(net.agartha.client.d.a_pkg.b arg) {
      if (this.isEnabled()) {
         String var2 = arg.message;
         if (var2 != null) {
            String var3 = var2.trim();
            if (var3.equalsIgnoreCase("/pay") || var3.toLowerCase(Locale.ROOT).startsWith("/pay ")) {
               arg.setCancelled(true);
               String[] var4 = var3.split("\\s+");
               if (var4.length < 3) {
                  this.usage();
               } else {
                  String var5 = resolvePlayerName(var4[1]);
                  String var6 = formatAmount(var4[2]);
                  if (var6 == null) {
                     this.usage();
                  } else {
                     MutableText var7 = Text.literal("You paid " + var5 + " ")
                        .append(Text.literal("$").formatted(Formatting.GREEN))
                        .append(Text.literal(" " + var6));
                     MinecraftClient var8 = MinecraftClient.getInstance();
                     if (var8.player != null) {
                        var8.player.sendMessage(var7, false);
                     }

                     FakeScoreboardModule.deductPaid(var4[2]);
                  }
               }
            }
         }
      }
   }

   private static String resolvePlayerName(String string) {
      ClientPlayNetworkHandler var1 = MinecraftClient.getInstance().getNetworkHandler();
      if (var1 == null) {
         return string;
      } else {
         for (PlayerListEntry var3 : var1.getPlayerList()) {
            String var4 = var3.getProfile().name();
            if (var4 != null && var4.equalsIgnoreCase(string)) {
               return var4;
            }
         }

         return string;
      }
   }

   private void usage() {
      if (this.showUsage.get()) {
         send("Usage: /pay <player> <amount>   e.g. /pay Notch 1.9m   (nothing is sent to the server)");
      }
   }

   private static void send(String string) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.player != null) {
         var1.player.sendMessage(Text.literal(string), false);
      }
   }

   static String formatAmount(String string) {
      if (string != null && !string.isBlank()) {
         String var1 = string.trim().replace(",", ".").replace("$", "");
         if (var1.isEmpty()) {
            return null;
         } else {
            int var2 = 0;
            char var3 = Character.toUpperCase(var1.charAt(var1.length() - 1));

            for (int var4 = 1; var4 < SUFFIXES.length; var4++) {
               if (SUFFIXES[var4].charAt(0) == var3) {
                  var2 = var4;
                  var1 = var1.substring(0, var1.length() - 1).trim();
                  break;
               }
            }

            if (var1.isEmpty()) {
               return null;
            } else {
               BigDecimal var9;
               try {
                  var9 = new BigDecimal(var1);
               } catch (NumberFormatException var8) {
                  return null;
               }

               if (var9.signum() < 0) {
                  return null;
               } else {
                  for (int var5 = 0; var5 < var2; var5++) {
                     var9 = var9.multiply(THOUSAND);
                  }

                  if (var9.compareTo(MAX_AMOUNT) > 0) {
                     var9 = MAX_AMOUNT;
                  }

                  int var10;
                  for (var10 = 0; var10 < SUFFIXES.length - 1 && var9.compareTo(THOUSAND) >= 0; var10++) {
                     var9 = var9.divide(THOUSAND, 6, RoundingMode.DOWN);
                  }

                  BigDecimal var6 = var9.setScale(1, RoundingMode.DOWN).stripTrailingZeros();
                  String var7 = var6.scale() <= 0 ? var6.toBigInteger().toString() : var6.toPlainString();
                  return var7 + SUFFIXES[var10];
               }
            }
         }
      } else {
         return null;
      }
   }
}
