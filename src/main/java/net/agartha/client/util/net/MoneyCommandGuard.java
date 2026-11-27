package net.agartha.client.util.net;

import java.util.Locale;
import java.util.Set;
import net.agartha.client.util.net.ChatSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MoneyCommandGuard {
   private static final Set<String> TRANSFER = Set.of(
      "pay", "epay", "payto", "mpay", "paymoney", "moneypay", "sendmoney", "givemoney", "moneysend", "transfer", "wire", "tip", "donate"
   );
   private static final Set<String> WALLETS = Set.of(
      "money",
      "eco",
      "economy",
      "bal",
      "balance",
      "bank",
      "cmi",
      "essentials",
      "ess",
      "coins",
      "coin",
      "shards",
      "shard",
      "gems",
      "gem",
      "tokens",
      "token",
      "points",
      "credits",
      "wallet"
   );
   private static final Set<String> WALLET_MOVES = Set.of("pay", "give", "send", "transfer");

   private MoneyCommandGuard() {
   }

   public static boolean allowAutomated(String string, String string2) {
      if (!isMoneyTransfer(string)) {
         return true;
      } else {
         ChatSender.send("Blocked " + (string2 == null ? "an automated" : string2) + " from sending a money command. Type it yourself if you meant it.");
         return false;
      }
   }

   public static boolean isMoneyTransfer(String string) {
      if (string == null) {
         return false;
      } else {
         String var1 = string.strip();
         if (!var1.startsWith("/")) {
            return false;
         } else {
            while (var1.startsWith("/")) {
               var1 = var1.substring(1).strip();
            }

            String[] var2 = var1.toLowerCase(Locale.ROOT).split("\\s+");
            if (var2.length != 0 && !var2[0].isEmpty()) {
               String var3 = a(var2[0]);
               return TRANSFER.contains(var3) ? true : WALLETS.contains(var3) && var2.length > 1 && WALLET_MOVES.contains(a(var2[1]));
            } else {
               return false;
            }
         }
      }
   }

   private static String a(String string) {
      int var1 = string.lastIndexOf(58);
      return var1 >= 0 ? string.substring(var1 + 1) : string;
   }
}
