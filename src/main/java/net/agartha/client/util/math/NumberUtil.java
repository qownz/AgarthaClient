package net.agartha.client.util.math;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public final class NumberUtil {
   private static final String[] SUFFIX = new String[]{"qt", "q", "t", "b", "m", "k"};
   private static final String[] DISPLAY = new String[]{"QT", "Q", "T", "B", "M", "K"};
   private static final int[] POWER = new int[]{18, 15, 12, 9, 6, 3};
   private static final Pattern NUMBER = Pattern.compile("\\d[\\d.,]*(?:\\s?(?:[qQ][tT]|[kKmMbBtTqQ]))?");
   private static final Pattern STRIP = Pattern.compile("[,\\s_]");

   private NumberUtil() {
   }

   public static double parse(String string) {
      if (string == null) {
         return Double.NaN;
      } else {
         String var1 = STRIP.matcher(string.trim().toLowerCase(Locale.ROOT)).replaceAll("");

         while (!var1.isEmpty() && a(var1.charAt(0))) {
            var1 = var1.substring(1);
         }

         if (var1.isEmpty()) {
            return Double.NaN;
         } else {
            for (int var2 = 0; var2 < SUFFIX.length; var2++) {
               String var3 = SUFFIX[var2];
               if (var1.endsWith(var3) && var1.length() > var3.length()) {
                  try {
                     double var4 = Double.parseDouble(var1.substring(0, var1.length() - var3.length()));
                     return var4 * Math.pow(10.0, POWER[var2]);
                  } catch (NumberFormatException var6) {
                     return Double.NaN;
                  }
               }
            }

            try {
               return Double.parseDouble(var1);
            } catch (NumberFormatException var7) {
               return Double.NaN;
            }
         }
      }
   }

   public static String shortForm(double d) {
      double var2 = Math.abs(d);

      for (int var4 = 0; var4 < POWER.length; var4++) {
         double var5 = Math.pow(10.0, POWER[var4]);
         if (var2 >= var5) {
            double var7 = Math.round(d / var5 * 10.0) / 10.0;
            return a(var7) + DISPLAY[var4];
         }
      }

      return Long.toString(Math.round(d));
   }

   public static int[] lastNumberSpan(String string) {
      if (string == null) {
         return null;
      } else {
         Matcher var1 = NUMBER.matcher(string);
         int var2 = -1;

         int var3;
         for (var3 = -1; var1.find(); var3 = var1.end()) {
            var2 = var1.start();
         }

         return var2 < 0 ? null : new int[]{var2, var3};
      }
   }

   public static int[] valueSpan(String string) {
      if (string == null) {
         return null;
      } else {
         for (int var1 = 0; var1 < string.length(); var1++) {
            if (Character.isDigit(string.charAt(var1))) {
               return new int[]{var1, string.length()};
            }
         }

         return null;
      }
   }

   public static Text replaceNumber(Text text, String string) {
      return a(text, string, false);
   }

   public static Text replaceValue(Text text, String string) {
      return a(text, string, true);
   }

   public static Text replaceRange(Text text, int i, int j, String string) {
      return text != null && i >= 0 && j > i ? a(text, i, j, string) : text;
   }

   private static Text a(Text text, String string, boolean bl) {
      if (text == null) {
         return null;
      } else {
         String var3 = text.getString();
         int[] var4 = bl ? valueSpan(var3) : lastNumberSpan(var3);
         return var4 == null ? text : a(text, var4[0], var4[1], string);
      }
   }

   private static Text a(Text text, int i, int j, String string) {
      ArrayList var4 = new ArrayList();
      ArrayList var5 = new ArrayList();
      text.visit((style, stringx) -> {
         var4.add(style);
         var5.add(stringx);
         return Optional.empty();
      }, Style.EMPTY);
      if (var5.isEmpty()) {
         var4.add(text.getStyle());
         var5.add(text.getString());
      }

      MutableText var6 = Text.empty();
      int var7 = 0;
      boolean var8 = false;

      for (int var9 = 0; var9 < var5.size(); var9++) {
         String var10 = (String)var5.get(var9);
         Style var11 = (Style)var4.get(var9);
         int var13 = var7 + var10.length();
         int var14 = Math.min(var13, i);
         if (var14 > var7) {
            var6.append(Text.literal(var10.substring(0, var14 - var7)).setStyle(var11));
         }

         if (!var8 && i >= var7 && i < var13) {
            var6.append(Text.literal(string).setStyle(var11));
            var8 = true;
         }

         int var15 = Math.max(var7, j);
         if (var13 > var15) {
            var6.append(Text.literal(var10.substring(var15 - var7)).setStyle(var11));
         }

         var7 = var13;
      }

      if (!var8) {
         var6.append(Text.literal(string));
      }

      return var6;
   }

   private static boolean a(char c) {
      return c == '$' || c == 8364 || c == 163;
   }

   private static String a(double d) {
      return d == Math.floor(d) && !Double.isInfinite(d) ? Long.toString((long)d) : Double.toString(d);
   }
}
