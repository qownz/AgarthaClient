package net.agartha.client.feature.auction;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MoneyParser {
   private static final Pattern MONEY_PATTERN = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)([kmbt]?)", 2);

   private MoneyParser() {
   }

   public static long parse(String string) {
      if (string == null) {
         return 0L;
      } else {
         Matcher var1 = MONEY_PATTERN.matcher(string.trim());
         return var1.find() ? a(var1.group(1), var1.group(2)) : 0L;
      }
   }

   public static long parseSuffixedFromText(String string) {
      if (string == null) {
         return -1L;
      } else {
         Matcher var1 = MONEY_PATTERN.matcher(string);

         while (var1.find()) {
            String var2 = var1.group(2);
            if (var2 != null && !var2.isEmpty()) {
               return a(var1.group(1), var2);
            }
         }

         return -1L;
      }
   }

   public static String format(long l) {
      long[] var2 = new long[]{1000000000000L, 1000000000L, 1000000L, 1000L};
      String[] var3 = new String[]{"t", "b", "m", "k"};

      for (int var4 = 0; var4 < var2.length; var4++) {
         if (l >= var2[var4]) {
            double var5 = (double)l / var2[var4];
            String var7 = var5 == Math.floor(var5) ? String.valueOf((long)var5) : String.valueOf(Math.round(var5 * 100.0) / 100.0);
            return var7 + var3[var4];
         }
      }

      return String.valueOf(l);
   }

   private static long a(String string, String string2) {
      double var2;
      try {
         var2 = Double.parseDouble(string.replace(",", "."));
      } catch (NumberFormatException var8) {
         return 0L;
      }

      String var6 = string2 == null ? "" : string2.toLowerCase(Locale.ROOT);

      long var4 = switch (var6) {
         case "k" -> 1000L;
         case "m" -> 1000000L;
         case "b" -> 1000000000L;
         case "t" -> 1000000000000L;
         default -> 1L;
      };
      return (long)(var2 * var4);
   }
}
