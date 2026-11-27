package net.agartha.client.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.gui.ClickGuiLayout;
import net.agartha.client.gui.HudLayout;
import net.agartha.client.gui.UiScale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class ConfigProfileManager {
   public static final int SCHEMA_VERSION = 1;
   public static final int MAX_PROFILES = 30;
   public static final int MAX_NAME_CHARS = 40;
   private static final String CODE_PREFIX = "AGARTHA-CFG-1:";
   private static final int MAX_CODE_CHARS = 200000;
   private static final Gson GSON = new Gson();
   private static final ConfigProfileManager INSTANCE = new ConfigProfileManager();
   private final List<ConfigProfileManager.a> profiles = new ArrayList<>();
   private boolean loaded;

   private ConfigProfileManager() {
   }

   public static ConfigProfileManager getInstance() {
      return INSTANCE;
   }

   public synchronized List<ConfigProfileManager.a> all() {
      this.c();
      return List.copyOf(this.profiles);
   }

   public synchronized ConfigProfileManager.a byName(String string) {
      this.c();

      for (ConfigProfileManager.a var3 : this.profiles) {
         if (var3.name().equalsIgnoreCase(string)) {
            return var3;
         }
      }

      return null;
   }

   public static JsonObject snapshotCurrent() {
      JsonObject var0 = new JsonObject();
      var0.addProperty("schema", 1);
      var0.add("modules", ModuleConfigManager.getInstance().exportModules());
      var0.add("hud", HudLayout.getInstance().exportJsonObject());
      var0.add("clickgui", ClickGuiLayout.getInstance().exportJsonObject());
      return var0;
   }

   public static boolean applySnapshot(JsonObject jsonObject) {
      if (jsonObject != null && a(jsonObject) <= 1) {
         if (jsonObject.has("modules") && jsonObject.get("modules").isJsonObject()) {
            ModuleConfigManager.getInstance().importModules(jsonObject.getAsJsonObject("modules"));
         }

         if (jsonObject.has("hud") && jsonObject.get("hud").isJsonObject()) {
            MinecraftClient var1 = MinecraftClient.getInstance();
            HudLayout.getInstance().importJsonObject(jsonObject.getAsJsonObject("hud"), UiScale.width(var1), UiScale.height(var1));
         }

         if (jsonObject.has("clickgui") && jsonObject.get("clickgui").isJsonObject()) {
            ClickGuiLayout.getInstance().importJsonObject(jsonObject.getAsJsonObject("clickgui"));
         }

         return true;
      } else {
         if (jsonObject != null) {
            AgarthaClientMod.LOGGER.warn("[Agartha] Config profile schema {} is newer than {}", a(jsonObject), 1);
         }

         return false;
      }
   }

   private static int a(JsonObject jsonObject) {
      return jsonObject.has("schema") ? jsonObject.get("schema").getAsInt() : 1;
   }

   public synchronized String uniqueName(String string) {
      this.c();
      String var2 = b(string);
      if (var2.isEmpty()) {
         var2 = "Config";
      }

      if (this.byName(var2) == null) {
         return var2;
      } else {
         for (int var3 = 2; var3 <= 31; var3++) {
            String var4 = b(var2 + " " + var3);
            if (this.byName(var4) == null) {
               return var4;
            }
         }

         return var2;
      }
   }

   public synchronized String save(String string) {
      return this.a(string, "Config", snapshotCurrent());
   }

   public synchronized String load(String string) {
      ConfigProfileManager.a var2 = this.byName(string);
      if (var2 == null) {
         return "Profile not found";
      } else if (!applySnapshot(var2.data())) {
         return "Profile was saved by a newer version";
      } else {
         ModuleConfigManager.getInstance().save();
         return null;
      }
   }

   public synchronized void delete(String string) {
      this.c();
      this.profiles.removeIf(arg -> arg.name().equalsIgnoreCase(string));
      this.d();
   }

   public synchronized void deleteAll() {
      this.c();
      this.profiles.clear();
      this.d();
   }

   public synchronized String importCode(String string, String string2) {
      JsonObject var3 = a(string);
      if (var3 == null) {
         return "Code is not valid";
      } else if (a(var3) > 1) {
         return "Code was made by a newer version";
      } else if (string2 != null && !string2.isBlank()) {
         return this.a(string2, "Imported", var3);
      } else {
         return applySnapshot(var3) ? null : "Code was made by a newer version";
      }
   }

   private String a(String string, String string2, JsonObject jsonObject) {
      this.c();
      String var4 = b(string);
      if (var4.isEmpty()) {
         var4 = this.uniqueName(string2);
      }

      ConfigProfileManager.a var5 = this.byName(var4);
      if (var5 == null && this.profiles.size() >= 30) {
         return "All 30 slots are full";
      } else {
         ConfigProfileManager.a var6 = new ConfigProfileManager.a(var4, Instant.now().toEpochMilli(), a(), jsonObject);
         if (var5 != null) {
            this.profiles.set(this.profiles.indexOf(var5), var6);
         } else {
            this.profiles.add(var6);
         }

         this.d();
         return null;
      }
   }

   public static String exportCurrentCode() {
      return b(snapshotCurrent());
   }

   public synchronized String exportCode(String string) {
      ConfigProfileManager.a var2 = this.byName(string);
      return var2 == null ? null : b(var2.data());
   }

   private static String b(JsonObject jsonObject) {
      try {
         ByteArrayOutputStream var1 = new ByteArrayOutputStream();

         try (GZIPOutputStream var2 = new GZIPOutputStream(var1)) {
            var2.write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
         }

         return "AGARTHA-CFG-1:" + Base64.getEncoder().encodeToString(var1.toByteArray());
      } catch (IOException var7) {
         AgarthaClientMod.LOGGER.warn("[Agartha] Failed to build config code", var7);
         return null;
      }
   }

   private static JsonObject a(String string) {
      if (string == null) {
         return null;
      } else {
         String var1 = string.trim();
         if (var1.length() > 200000) {
            return null;
         } else {
            if (var1.toUpperCase(Locale.ROOT).startsWith("AGARTHA-CFG-1:")) {
               var1 = var1.substring("AGARTHA-CFG-1:".length());
            }

            var1 = var1.replaceAll("\\s", "");
            if (var1.isEmpty()) {
               return null;
            } else {
               try {
                  byte[] var2 = Base64.getDecoder().decode(var1);

                  JsonObject var5;
                  try (GZIPInputStream var3 = new GZIPInputStream(new ByteArrayInputStream(var2))) {
                     String var4 = new String(var3.readAllBytes(), StandardCharsets.UTF_8);
                     var5 = JsonParser.parseString(var4).getAsJsonObject();
                  }

                  return var5;
               } catch (RuntimeException | IOException var8) {
                  return null;
               }
            }
         }
      }
   }

   static String serialize(List<ConfigProfileManager.a> list) {
      ConfigProfileManager.b var1 = new ConfigProfileManager.b();
      var1.schema = 1;
      var1.profiles = List.copyOf(list);
      return GSON.toJson(var1);
   }

   static List<ConfigProfileManager.a> deserialize(String string) {
      ArrayList var1 = new ArrayList();
      ConfigProfileManager.b var2 = (ConfigProfileManager.b)GSON.fromJson(string, ConfigProfileManager.b.class);
      if (var2 != null && var2.profiles != null) {
         for (ConfigProfileManager.a var4 : var2.profiles) {
            if (var4 != null && var4.name() != null && !var4.name().isBlank() && var4.data() != null) {
               var1.add(var4);
               if (var1.size() >= 30) {
                  break;
               }
            }
         }

         return var1;
      } else {
         return var1;
      }
   }

   private static String b(String string) {
      if (string == null) {
         return "";
      } else {
         String var1 = string.trim().replaceAll("\\p{Cntrl}", "");
         return var1.length() > 40 ? var1.substring(0, 40) : var1;
      }
   }

   private static String a() {
      return FabricLoader.getInstance()
         .getModContainer("agartha")
         .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
         .orElse("unknown");
   }

   private static Path b() {
      return FabricLoader.getInstance().getConfigDir().resolve("agartha").resolve("profiles.json");
   }

   private void c() {
      if (!this.loaded) {
         this.loaded = true;
         Path var1 = b();
         if (Files.isRegularFile(var1)) {
            if (this.a(var1)) {
               ConfigFileIO.keepBackup(var1);
            } else {
               Path var2 = ConfigFileIO.quarantine(var1);
               AgarthaClientMod.LOGGER.warn("[Agartha] Config profiles unreadable; kept a copy at {}", var2);
               Path var3 = ConfigFileIO.backupOf(var1);
               if (Files.isRegularFile(var3) && this.a(var3)) {
                  AgarthaClientMod.LOGGER.warn("[Agartha] Restored config profiles from {}", var3);
               }
            }
         }
      }
   }

   private boolean a(Path path) {
      try {
         List var2 = deserialize(Files.readString(path));
         this.profiles.clear();
         this.profiles.addAll(var2);
         return true;
      } catch (RuntimeException | IOException var3) {
         AgarthaClientMod.LOGGER.warn("[Agartha] Failed to read config profiles {}", path, var3);
         this.profiles.clear();
         return false;
      }
   }

   private void d() {
      try {
         ConfigFileIO.writeAtomic(b(), serialize(this.profiles));
      } catch (IOException var2) {
         AgarthaClientMod.LOGGER.warn("[Agartha] Failed to save config profiles", var2);
      }
   }

   @Environment(EnvType.CLIENT)
   public record a(String name, long savedAt, String clientVersion, JsonObject data) {
   }

   @Environment(EnvType.CLIENT)
   private static final class b {
      int schema;
      List<ConfigProfileManager.a> profiles;
   }
}
