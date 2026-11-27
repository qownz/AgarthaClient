package net.agartha.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.agartha.client.AgarthaClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

@Environment(EnvType.CLIENT)
public final class x {
   private static final String[] WATCHED = new String[]{
      "Dough4",
      "FallerFly",
      "Evxn",
      "Ryuui_",
      "Shyalyy",
      "ItsDefRealMe",
      "LzouZMp5",
      "Munkerlich",
      "Chaon",
      "Showered",
      "PastaGamer08",
      "ArchivePedro",
      "Evify",
      "Noah",
      "Zababi",
      "Nathan",
      "Owen1212055",
      "DrDonutt",
      "Bautiegar",
      "BloodSplulse",
      "GsMusie",
      "Frwost",
      "FluffyMaster07",
      "W1zox_",
      "ItsZDeath",
      "0Gsummer",
      "Frenk_btw",
      "Napooo_",
      "auzzitech",
      "u_vv"
   };
   private static final Set<String> NOT_STAFF = Set.of("noahincc");
   private static final String STARS = "★☆✦✧✩✪✫✬✭✮✯✰✡⭐✵✶✷✸✹✺";
   private static final Pattern COLOR_CODES = Pattern.compile("(?i)§x(?:§[0-9a-f]){6}|§.|&[0-9a-fk-or]");
   private static final long REFRESH_MS = 1000L;
   private static final Comparator<PlayerListEntry> TAB_ORDER = Comparator.<PlayerListEntry>comparingInt(playerListEntry -> -playerListEntry.getListOrder())
      .thenComparingInt(playerListEntry -> playerListEntry.getGameMode() == GameMode.SPECTATOR ? 1 : 0)
      .thenComparing(playerListEntry -> {
         Team var1 = playerListEntry.getScoreboardTeam();
         return var1 == null ? "" : var1.getName();
      })
      .thenComparing(playerListEntry -> {
         String var1 = playerListEntry.getProfile().name();
         return var1 == null ? "" : var1;
      }, String::compareToIgnoreCase);
   private static final String[] NEEDLES = b();
   private static List<x.a> cached = List.of();
   private static long cachedAt;
   private static final Set<String> learnedBadges = new HashSet<>();
   private static final Set<String> loggedBadges = new HashSet<>();
   private static ClientPlayNetworkHandler learnedOn;
   private static Map<PlayerListEntry, x.b> rows = new IdentityHashMap<>();

   public static boolean notStaff(String string) {
      return string != null && NOT_STAFF.contains(string.toLowerCase(Locale.ROOT));
   }

   private x() {
   }

   public static List<x.a> online() {
      long var0 = System.currentTimeMillis();
      if (var0 - cachedAt < 1000L) {
         return cached;
      } else {
         cachedAt = var0;
         cached = a();
         return cached;
      }
   }

   private static List<x.a> a() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      ClientPlayNetworkHandler var1 = var0.getNetworkHandler();
      if (var1 == null) {
         return List.of();
      } else {
         if (var1 != learnedOn) {
            learnedOn = var1;
            learnedBadges.clear();
            loggedBadges.clear();
         }

         Collection var2 = var1.getPlayerList();
         if (var2.isEmpty()) {
            return List.of();
         } else {
            ArrayList var3 = new ArrayList(var2);
            var3.sort(TAB_ORDER);
            int var4 = var3.size();
            String[] var5 = new String[var4];
            String[] var6 = new String[var4];
            String[] var7 = new String[var4];
            IdentityHashMap var8 = new IdentityHashMap(var4 * 2);

            for (int var9 = 0; var9 < var4; var9++) {
               PlayerListEntry var10 = (PlayerListEntry)var3.get(var9);
               x.b var11 = a(var10);
               var8.put(var10, var11);
               String var12 = var11.name;
               var5[var9] = var12;
               var6[var9] = var11.badge;
               if (notStaff(var12)) {
                  var6[var9] = "";
               } else {
                  String var13 = var11.matched;
                  boolean var14 = var13 != null && var12 != null && var12.equalsIgnoreCase(var13);
                  boolean var15 = var11.star;
                  if (var13 != null || var15) {
                     var7[var9] = var13 != null ? var13 : var12;
                  }

                  if ((var14 || var15) && !var6[var9].isEmpty()) {
                     learnedBadges.add(var6[var9]);
                  }

                  a(var6[var9], var12);
               }
            }

            rows = var8;
            ArrayList var16 = new ArrayList();

            for (int var17 = 0; var17 < var4; var17++) {
               if (var7[var17] == null && !var6[var17].isEmpty() && learnedBadges.contains(var6[var17])) {
                  var7[var17] = var5[var17];
               }

               if (var7[var17] != null) {
                  String var18 = var5[var17] == null ? var7[var17] : var5[var17];
                  var16.add(new x.a(var18, var7[var17], ((PlayerListEntry)var3.get(var17)).getProfile().id()));
               }
            }

            return List.copyOf(var16);
         }
      }
   }

   private static x.b a(PlayerListEntry playerListEntry) {
      String var1 = playerListEntry.getProfile().name();
      Text var2 = playerListEntry.getDisplayName();
      x.b var3 = rows.get(playerListEntry);
      if (var3 != null && var3.display == var2 && Objects.equals(var3.name, var1)) {
         return var3;
      } else {
         String var4 = var2 == null ? "" : c(var2.getString());
         String var5 = badgeOf(var1, var4);
         return new x.b(var2, var1, var5, b(b(var1, var4)), a(var5));
      }
   }

   static String badgeOf(String string, String string2) {
      if (string2 != null && !string2.isEmpty()) {
         int var2 = -1;
         if (string != null && !string.isEmpty()) {
            var2 = string2.toLowerCase(Locale.ROOT).indexOf(string.toLowerCase(Locale.ROOT));
         }

         if (var2 < 0) {
            var2 = 0;

            while (var2 < string2.length() && !Character.isLetterOrDigit(string2.charAt(var2))) {
               var2++;
            }
         }

         StringBuilder var3 = new StringBuilder();

         for (int var4 = 0; var4 < var2; var4++) {
            char var5 = string2.charAt(var4);
            if (var5 > 127 && !Character.isWhitespace(var5)) {
               var3.append(var5);
            }
         }

         return var3.toString();
      } else {
         return "";
      }
   }

   private static boolean a(String string) {
      for (int var1 = 0; var1 < string.length(); var1++) {
         if ("★☆✦✧✩✪✫✬✭✮✯✰✡⭐✵✶✷✸✹✺".indexOf(string.charAt(var1)) >= 0) {
            return true;
         }
      }

      return false;
   }

   private static void a(String string, String string2) {
      if (!string.isEmpty() && loggedBadges.add(string)) {
         StringBuilder var2 = new StringBuilder();
         string.codePoints().forEach(i -> var2.append(String.format("U+%04X ", i)));
         AgarthaClientMod.LOGGER.info("[staff] TAB badge {}on {} (staff: {})", new Object[]{var2, string2, learnedBadges.contains(string) || a(string)});
      }
   }

   private static String b(String string, String string2) {
      String var2 = normalise(string);
      String var3 = normalise(string2);
      if (!var3.isEmpty() && !var3.equals(var2)) {
         return var2.isEmpty() ? var3 : var2 + " " + var3;
      } else {
         return var2;
      }
   }

   public static boolean watchesAccount(String string) {
      if (notStaff(string)) {
         return false;
      } else {
         String var1 = normalise(string);
         if (var1.isEmpty()) {
            return false;
         } else {
            for (String var5 : NEEDLES) {
               if (var1.equals(var5)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   private static String b(String string) {
      if (string.isEmpty()) {
         return null;
      } else {
         String var1 = null;

         for (int var2 = 0; var2 < NEEDLES.length; var2++) {
            String var3 = NEEDLES[var2];
            if (!var3.isEmpty() && string.contains(var3) && (var1 == null || var3.length() > var1.length())) {
               var1 = WATCHED[var2];
            }
         }

         return var1;
      }
   }

   private static String c(String string) {
      return string == null ? "" : COLOR_CODES.matcher(string).replaceAll("");
   }

   public static String normalise(String string) {
      return string != null && !string.isEmpty() ? c(string).trim().toLowerCase(Locale.ROOT) : "";
   }

   private static String[] b() {
      String[] var0 = new String[WATCHED.length];

      for (int var1 = 0; var1 < WATCHED.length; var1++) {
         var0[var1] = normalise(WATCHED[var1]);
      }

      return var0;
   }

   @Environment(EnvType.CLIENT)
   public record a(String name, String matched, UUID id) {
   }

   @Environment(EnvType.CLIENT)
   private static final class b {
      final Text display;
      final String name;
      final String badge;
      final String matched;
      final boolean star;

      b(Text text, String string, String string2, String string3, boolean bl) {
         this.display = text;
         this.name = string;
         this.badge = string2;
         this.matched = string3;
         this.star = bl;
      }
   }
}
