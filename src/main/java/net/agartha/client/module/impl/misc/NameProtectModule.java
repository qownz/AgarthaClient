package net.agartha.client.module.impl.misc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.StringSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.PlainTextContent.Literal;

@Environment(EnvType.CLIENT)
public final class NameProtectModule extends Module {
   private final StringSetting ownName = this.addSetting(new StringSetting("Own Name", "You", 16));
   private final EnumSetting others = this.addSetting(new EnumSetting("Others", "Number", "Number", "Name", "Hidden"));
   private final BooleanSetting selfOnly = this.addSetting(new BooleanSetting("Self Only", false));
   private static final int SEEN_CAP = 256;
   private final LinkedHashMap<String, String> seen = new LinkedHashMap<String, String>(16, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, String> entry) {
         return this.size() > 256;
      }
   };
   private int cacheBucket = Integer.MIN_VALUE;
   private int cacheTab = -1;
   private boolean cacheSelfOnly;
   private String cacheOthers;
   private String cacheOwnName;
   private Map<String, String> cachedTargets = Map.of();
   private Pattern cachedPattern;
   private Object lastConnection;

   public NameProtectModule() {
      super("Name Protect", "Hides your name (and optionally others') in chat and nametags.", ModuleCategory.MISC);
   }

   private String a() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      return var1.player == null ? null : var1.player.getGameProfile().name();
   }

   private boolean a(String string) {
      String var2 = this.a();
      return var2 != null && var2.equalsIgnoreCase(string);
   }

   private String b(String string) {
      if (!this.a(string)) {
         if (this.others.is("Hidden")) {
            return "";
         } else {
            return this.others.is("Number") ? "Player " + (Math.floorMod(string.toLowerCase(Locale.ROOT).hashCode(), 99) + 1) : "Player";
         }
      } else {
         String var2 = this.ownName.get();
         return var2 != null && !var2.isBlank() ? var2 : "You";
      }
   }

   private Map<String, String> b() {
      LinkedHashMap<String, String> var1 = new LinkedHashMap<>();
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientPlayNetworkHandler var3 = var2.getNetworkHandler();
      if (var3 != this.lastConnection) {
         this.seen.clear();
         this.lastConnection = var3;
      }

      String var4 = this.a();
      if (var4 != null && !var4.isEmpty()) {
         this.seen.put(var4.toLowerCase(Locale.ROOT), var4);
         var1.put(var4, this.b(var4));
      }

      boolean var5 = !this.selfOnly.get();
      if (var3 != null) {
         for (PlayerListEntry var7 : var3.getPlayerList()) {
            String var8 = var7.getProfile() == null ? null : var7.getProfile().name();
            if (var8 != null && !var8.isEmpty()) {
               this.seen.put(var8.toLowerCase(Locale.ROOT), var8);
               if (var5) {
                  var1.putIfAbsent(var8, this.b(var8));
               }
            }
         }
      }

      if (var5) {
         for (String var10 : new ArrayList<>(this.seen.values())) {
            var1.putIfAbsent(var10, this.b(var10));
         }
      }

      var1.entrySet().removeIf(entry -> ((String)entry.getKey()).equalsIgnoreCase((String)entry.getValue()));
      return var1;
   }

   private void c() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      int var2 = var1.player == null ? -1 : var1.player.age / 20;
      int var3 = var1.getNetworkHandler() == null ? 0 : var1.getNetworkHandler().getPlayerList().size();
      boolean var4 = this.selfOnly.get();
      String var5 = this.others.get();
      String var6 = this.ownName.get();
      if (var2 != this.cacheBucket
         || var3 != this.cacheTab
         || var4 != this.cacheSelfOnly
         || !Objects.equals(var5, this.cacheOthers)
         || !Objects.equals(var6, this.cacheOwnName)) {
         this.cacheBucket = var2;
         this.cacheTab = var3;
         this.cacheSelfOnly = var4;
         this.cacheOthers = var5;
         this.cacheOwnName = var6;
         this.cachedTargets = this.b();
         this.cachedPattern = a(this.cachedTargets);
      }
   }

   private static Pattern a(Map<String, String> map) {
      if (map.isEmpty()) {
         return null;
      } else {
         ArrayList<String> var1 = new ArrayList<>(map.keySet());
         var1.sort((string, string2) -> Integer.compare(string2.length(), string.length()));
         StringBuilder var2 = new StringBuilder("(?<![A-Za-z0-9_])(");

         for (int var3 = 0; var3 < var1.size(); var3++) {
            if (var3 > 0) {
               var2.append('|');
            }

            var2.append(Pattern.quote((String)var1.get(var3)));
         }

         var2.append(")(?![A-Za-z0-9_])");
         return Pattern.compile(var2.toString());
      }
   }

   private String c(String string) {
      String var2 = this.cachedTargets.get(string);
      if (var2 != null) {
         return var2;
      } else {
         for (Entry var4 : this.cachedTargets.entrySet()) {
            if (((String)var4.getKey()).equalsIgnoreCase(string)) {
               return (String)var4.getValue();
            }
         }

         return null;
      }
   }

   private String d(String string) {
      if (string != null && !string.isEmpty() && this.cachedPattern != null) {
         Matcher var2 = this.cachedPattern.matcher(string);
         if (!var2.find()) {
            return string;
         } else {
            StringBuilder var3 = new StringBuilder(string.length());
            int var4 = 0;

            do {
               String var5 = var2.group(1);
               String var6 = this.c(var5);
               var3.append(string, var4, var2.start());
               var3.append(var6 != null ? var6 : var5);
               var4 = var2.end();
            } while (var2.find());

            var3.append(string, var4, string.length());
            return var3.toString();
         }
      } else {
         return string;
      }
   }

   public String protect(String string) {
      if (!this.isEnabled()) {
         return string;
      } else {
         this.c();
         return this.d(string);
      }
   }

   public Text censorChat(Text text) {
      if (this.isEnabled() && text != null) {
         this.c();
         return this.cachedPattern == null ? text : this.a(text);
      } else {
         return text;
      }
   }

   public Text censorScoreboardLine(Text text) {
      if (this.isEnabled() && text != null) {
         this.c();
         if (this.cachedPattern == null) {
            return text;
         } else {
            String var2 = text.getString();
            String var3 = this.d(var2);
            return (Text)(var3.equals(var2) ? text : MutableText.of(PlainTextContent.of(var3)).setStyle(text.getStyle()));
         }
      } else {
         return text;
      }
   }

   private Text a(Text text) {
      MutableText var2;
      if (text.getContent() instanceof Literal var3) {
         var2 = MutableText.of(PlainTextContent.of(this.d(var3.string())));
      } else {
         var2 = MutableText.of(text.getContent());
      }

      var2.setStyle(text.getStyle());

      for (Text var6 : text.getSiblings()) {
         var2.append(this.a(var6));
      }

      return var2;
   }
}
