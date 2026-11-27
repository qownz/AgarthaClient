package net.agartha.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.agartha.client.module.impl.misc.FakeScoreboardModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public final class aa {
   private static final Pattern PING = Pattern.compile("(\\d{1,4})\\s*ms", 2);
   private static final Pattern NUMBER = Pattern.compile("\\d[\\d.,]*(?:\\s?(?:[qQ][tT]|[kKmMbBtTqQ]))?");
   private static final long FRESH_MS = 3000L;
   private static Text original;
   private static int ping = -1;
   private static long seenAt;
   private static boolean applying;

   private aa() {
   }

   public static void reapply() {
      if (original != null && !applying) {
         MinecraftClient var0 = MinecraftClient.getInstance();
         if (var0.inGameHud != null) {
            applying = true;
            var0.inGameHud.getPlayerListHud().setFooter(a());
            applying = false;
         }
      }
   }

   public static Text accept(Text text) {
      if (applying) {
         return text;
      } else {
         original = text;
         a(text);
         return a();
      }
   }

   public static int ping() {
      return ping >= 0 && System.currentTimeMillis() - seenAt <= 3000L ? ping : -1;
   }

   private static void a(Text text) {
      ping = -1;
      seenAt = System.currentTimeMillis();
      if (text != null) {
         Matcher var1 = PING.matcher(text.getString());
         int var2 = -1;

         while (var1.find()) {
            var2 = Integer.parseInt(var1.group(1));
         }

         ping = var2;
      }
   }

   private static Text a() {
      if (original == null) {
         return null;
      } else {
         String var0 = FakeScoreboardModule.tabMoney();
         return var0 == null ? original : a(original, var0);
      }
   }

   private static Text a(Text text, String string) {
      ArrayList var2 = new ArrayList();
      ArrayList var3 = new ArrayList();
      text.visit((style, stringx) -> {
         var2.add(style);
         var3.add(stringx);
         return Optional.empty();
      }, Style.EMPTY);
      if (var3.isEmpty()) {
         return text;
      } else {
         ArrayList var4 = new ArrayList();
         MutableText var5 = Text.empty();
         StringBuilder var6 = new StringBuilder();
         boolean var7 = false;

         for (int var8 = 0; var8 < var3.size(); var8++) {
            String var9 = (String)var3.get(var8);
            Style var10 = (Style)var2.get(var8);
            int var11 = 0;

            for (int var12 = var9.indexOf(10); var12 >= 0; var12 = var9.indexOf(10, var11)) {
               if (var12 > var11) {
                  String var13 = var9.substring(var11, var12);
                  var5.append(Text.literal(var13).setStyle(var10));
                  var6.append(var13);
               }

               if (a(var4, var5, var6, string)) {
                  var7 = true;
               }

               var5 = Text.empty();
               var6.setLength(0);
               var11 = var12 + 1;
            }

            if (var11 < var9.length()) {
               String var19 = var9.substring(var11);
               var5.append(Text.literal(var19).setStyle(var10));
               var6.append(var19);
            }
         }

         if ((var6.length() > 0 || var4.isEmpty()) && a(var4, var5, var6, string)) {
            var7 = true;
         }

         if (!var7) {
            return text;
         } else {
            int var14 = a(text.getString());
            int var15 = 0;

            for (int var16 = var4.size() - 1; var16 >= 0 && ((MutableText)var4.get(var16)).getString().isEmpty(); var16--) {
               var15++;
            }

            while (var15 < var14) {
               var4.add(Text.empty());
               var15++;
            }

            MutableText var17 = Text.empty();

            for (int var18 = 0; var18 < var4.size(); var18++) {
               if (var18 > 0) {
                  var17.append(Text.literal("\n"));
               }

               var17.append((Text)var4.get(var18));
            }

            return var17;
         }
      }
   }

   static Text show(Text text, String string) {
      return a(text, string);
   }

   private static int a(String string) {
      int var1 = 0;

      for (int var2 = string.length() - 1; var2 >= 0 && string.charAt(var2) == '\n'; var2--) {
         var1++;
      }

      return var1;
   }

   private static boolean a(List<MutableText> list, MutableText mutableText, StringBuilder stringBuilder, String string) {
      int[] var4 = moneySpan(stringBuilder.toString());
      if (var4 == null) {
         list.add(mutableText);
         return false;
      } else {
         list.add((MutableText)q.replaceRange(mutableText, var4[0], var4[1], string));
         return true;
      }
   }

   static int[] moneySpan(String string) {
      if (string != null && !string.isEmpty()) {
         int var1 = -1;
         int var2 = -1;

         for (Matcher var3 = PING.matcher(string); var3.find(); var2 = var3.end(1)) {
            var1 = var3.start(1);
         }

         Matcher var4 = NUMBER.matcher(string);
         int var5 = -1;
         int var6 = -1;
         int var7 = 0;
         int var8 = -1;
         int var9 = -1;
         int var10 = 0;

         while (var4.find()) {
            int var11 = var4.start();
            int var12 = var4.end();
            if (var1 < 0 || var11 >= var2 || var12 <= var1) {
               var10++;
               var8 = var11;
               var9 = var12;
               int var13 = a(string, var11, var12);
               if (var13 > var7) {
                  var7 = var13;
                  var5 = var11;
                  var6 = var12;
               }
            }
         }

         if (var7 > 0) {
            return new int[]{var5, var6};
         } else {
            return var10 != 1 || a(string, var8, var9) < 0 || var1 < 0 && !b(string) ? null : new int[]{var8, var9};
         }
      } else {
         return null;
      }
   }

   private static int a(String string, int i, int j) {
      byte var3 = 0;
      int var4 = i - 1;

      while (var4 >= 0 && Character.isWhitespace(string.charAt(var4))) {
         var4--;
      }

      if (var4 >= 0 && (string.charAt(var4) == '$' || string.charAt(var4) == 8364 || string.charAt(var4) == 163)) {
         var3 += 8;
      }

      String var5 = string.substring(i, j);
      char var6 = var5.charAt(var5.length() - 1);
      if (var6 == 'k' || var6 == 'K' || var6 == 'm' || var6 == 'M' || var6 == 'b' || var6 == 'B' || var6 == 't' || var6 == 'T' || var6 == 'q' || var6 == 'Q') {
         var3 += 3;
      }

      int var7 = Math.max(0, i - 18);
      String var8 = string.substring(var7, i).toLowerCase(Locale.ROOT);
      if (var8.contains("shard")) {
         var3 -= 12;
      }

      if (var8.contains("money") || var8.contains("balance") || var8.contains("bal")) {
         var3 += 6;
      }

      return var3;
   }

   private static boolean b(String string) {
      String var1 = string.toLowerCase(Locale.ROOT);
      return var1.contains("money") || var1.contains("balance") || string.indexOf(36) >= 0;
   }
}
