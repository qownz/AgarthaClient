package net.agartha.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.config.ConfigFileIO;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.client.NotificationsModule;
import net.agartha.client.module.impl.render.HandOverlayModule;
import net.agartha.client.module.setting.Setting;
import net.agartha.client.module.setting.StringSetting;
import net.agartha.client.util.perf.Profiler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class ModuleConfigManager {
   private static final ModuleConfigManager INSTANCE = new ModuleConfigManager();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final int CONFIG_VERSION = 1;
   private final Map<String, JsonObject> orphanModules = new LinkedHashMap<>();
   private static final Set<String> REMOVED_MODULES = Set.of(
      "Stream Protection", "Low Health Warning", "Floating Stars", "Target Mark", "Box ESP", "Vault Finder", "Player Bypass 2"
   );
   private final Map<String, Boolean> pendingEnabled = new LinkedHashMap<>();
   private boolean loaded;
   private Path configPathOverride;
   private static final String[][] STALE_DEFAULTS = new String[][]{
      {"Block Overlay", "Line Width", "3.0"}, {"Block Overlay", "Smooth Speed", "16.0"}, {"Player Overlay", "Smooth Speed", "16.0"}
   };

   private ModuleConfigManager() {
   }

   public static ModuleConfigManager getInstance() {
      return INSTANCE;
   }

   public synchronized void load() {
      if (!this.loaded) {
         this.loaded = true;
         this.orphanModules.clear();
         this.pendingEnabled.clear();
         ModuleManager.all();
         Path var1 = this.b();
         if (Files.isRegularFile(var1)) {
            if (this.a(var1)) {
               ConfigFileIO.keepBackup(var1);
            } else {
               Path var2 = ConfigFileIO.quarantine(var1);
               AgarthaClientMod.LOGGER.warn("[Agartha] Module config unusable; kept a copy at {}", var2);
               this.orphanModules.clear();
               this.pendingEnabled.clear();
               Path var3 = ConfigFileIO.backupOf(var1);
               if (Files.isRegularFile(var3) && this.a(var3)) {
                  AgarthaClientMod.LOGGER.warn("[Agartha] Restored module config from {}", var3);
               } else {
                  this.orphanModules.clear();
                  this.pendingEnabled.clear();
               }
            }
         }
      }
   }

   private boolean a(Path path) {
      try {
         boolean var5;
         try (BufferedReader var2 = Files.newBufferedReader(path)) {
            JsonObject var3 = JsonParser.parseReader(var2).getAsJsonObject();
            int var4 = var3.has("version") ? var3.get("version").getAsInt() : 0;
            if (var4 > 1) {
               AgarthaClientMod.LOGGER.warn("[Agartha] Module config version {} is newer than supported {}", var4, 1);
               return false;
            }

            this.applyConfigRoot(var3, ModuleManager::byName);
            var5 = true;
         }

         return var5;
      } catch (RuntimeException | IOException var8) {
         AgarthaClientMod.LOGGER.warn("[Agartha] Failed to read module config {}", path, var8);
         return false;
      }
   }

   public synchronized void applyPendingEnabled() {
      this.applyPendingEnabled(ModuleManager::byName);
   }

   synchronized void applyPendingEnabled(Function<String, Module> function) {
      if (!this.pendingEnabled.isEmpty()) {
         for (Entry var3 : this.pendingEnabled.entrySet()) {
            if (Boolean.TRUE.equals(var3.getValue())) {
               Module var4 = (Module)function.apply((String)var3.getKey());
               if (var4 != null && var4.persistEnabled()) {
                  var4.setEnabled(true);
               }
            }
         }

         this.pendingEnabled.clear();
      }
   }

   public synchronized void save() {
      long var1 = Profiler.begin();
      long var3 = Profiler.allocatedBytes();
      this.a();
      JsonObject var5 = new JsonObject();
      var5.addProperty("version", 1);
      JsonObject var6 = new JsonObject();

      for (Module var8 : ModuleManager.all()) {
         var6.add(var8.name(), a(var8));
      }

      for (Entry var16 : this.orphanModules.entrySet()) {
         if (!var6.has((String)var16.getKey())) {
            var6.add((String)var16.getKey(), ((JsonObject)var16.getValue()).deepCopy());
         }
      }

      var5.add("modules", var6);
      Path var15 = this.b();

      try {
         ConfigFileIO.writeAtomic(var15, GSON.toJson(var5));
         if (Profiler.enabled()) {
            Profiler.recordCount("config.size-bytes", Files.size(var15));
         }
      } catch (IOException var12) {
         AgarthaClientMod.LOGGER.warn("[Agartha] Failed to save module config", var12);
      } finally {
         Profiler.record("config.save", var1);
         Profiler.recordAllocatedBytes("config.save", var3);
      }
   }

   public synchronized JsonObject exportModules() {
      this.a();
      return this.buildConfigRoot(ModuleManager.all());
   }

   public synchronized void importModules(JsonObject jsonObject) {
      if (jsonObject != null) {
         IdentityHashMap var2 = new IdentityHashMap();

         for (Module var4 : ModuleManager.all()) {
            for (Setting var6 : var4.settings()) {
               if (var6 instanceof StringSetting var7) {
                  var2.put(var7, var7.get());
               }
            }
         }

         for (Module var9 : ModuleManager.all()) {
            var9.resetToDefaults();
            if (var9.persistEnabled()) {
               var9.setEnabled(false);
            }
         }

         this.pendingEnabled.clear();
         this.applyConfigRoot(jsonObject, ModuleManager::byName);
         this.applyPendingEnabled(ModuleManager::byName);
         var2.forEach((setting, value) -> ((StringSetting)setting).set((String)value));
      }
   }

   static ModuleConfigManager createForTests(Path path) {
      ModuleConfigManager var1 = new ModuleConfigManager();
      var1.configPathOverride = path;
      return var1;
   }

   synchronized void applyConfigRoot(JsonObject jsonObject, Function<String, Module> function) {
      if (jsonObject != null) {
         int var3 = jsonObject.has("version") ? jsonObject.get("version").getAsInt() : 0;
         if (var3 > 1) {
            AgarthaClientMod.LOGGER.warn("[Agartha] Module config version {} is newer than supported {}; skipping load", var3, 1);
         } else if (jsonObject.has("modules") && jsonObject.get("modules").isJsonObject()) {
            JsonObject var4 = jsonObject.getAsJsonObject("modules");

            for (Entry var6 : var4.entrySet()) {
               String var7 = (String)var6.getKey();
               if ("Fake Scoreboard".equals(var7)) {
                  var7 = "Fake Stats";
               }

               if ("Hitboxes".equals(var7)) {
                  var7 = "Player Overlay";
               }

               if ("Storage ESP Bypass".equals(var7) && !var4.has("Storage ESP")) {
                  var7 = "Storage ESP";
               }

               if (((JsonElement)var6.getValue()).isJsonObject()) {
                  JsonObject var8 = ((JsonElement)var6.getValue()).getAsJsonObject();
                  Module var9 = (Module)function.apply(var7);
                  if (var9 == null) {
                     if (!REMOVED_MODULES.contains(var7)) {
                        this.orphanModules.put(var7, var8.deepCopy());
                     }
                  } else {
                     a(var7, var8);
                     a(var9, var8, this.pendingEnabled);
                  }
               }
            }

            a(var4);
         }
      }
   }

   private static void a(String string, JsonObject jsonObject) {
      if (jsonObject.has("settings") && jsonObject.get("settings").isJsonObject()) {
         JsonObject var2 = jsonObject.getAsJsonObject("settings");

         for (String[] var6 : STALE_DEFAULTS) {
            if (var6[0].equals(string) && var2.has(var6[1])) {
               JsonElement var7 = var2.get(var6[1]);

               try {
                  if (var7.isJsonPrimitive() && var7.getAsJsonPrimitive().isNumber()
                     ? var7.getAsDouble() == Double.parseDouble(var6[2])
                     : var6[2].equals(var7.getAsString())) {
                     var2.remove(var6[1]);
                  }
               } catch (RuntimeException var9) {
               }
            }
         }

         if ("Block Overlay".equals(string) && var2.has("Style") && var2.get("Style").isJsonPrimitive() && "Both".equals(var2.get("Style").getAsString())) {
            var2.addProperty("Style", "Fill");
         }
      }
   }

   private static void a(JsonObject jsonObject) {
      NotificationsModule var1 = ModuleManager.get(NotificationsModule.class);
      if (var1 != null) {
         JsonObject var2 = jsonObject.getAsJsonObject("Notifications");
         if (var2 != null && var2.has("settings")) {
            JsonObject var3 = var2.getAsJsonObject("settings");
            if (var3.has("Notification Position")) {
               return;
            }
         }

         JsonObject var7 = jsonObject.getAsJsonObject("HUD");
         if (var7 != null && var7.has("settings")) {
            JsonObject var4 = var7.getAsJsonObject("settings");
            if (var4.has("Notification Position") && var4.get("Notification Position").isJsonPrimitive()) {
               try {
                  var1.notificationCornerSetting().set(var4.get("Notification Position").getAsString());
               } catch (RuntimeException var6) {
               }
            }
         }
      }
   }

   synchronized JsonObject buildConfigRoot(List<Module> list) {
      JsonObject var2 = new JsonObject();
      var2.addProperty("version", 1);
      JsonObject var3 = new JsonObject();

      for (Module var5 : list) {
         var3.add(var5.name(), a(var5));
      }

      for (Entry var7 : this.orphanModules.entrySet()) {
         if (!var3.has((String)var7.getKey())) {
            var3.add((String)var7.getKey(), ((JsonObject)var7.getValue()).deepCopy());
         }
      }

      var2.add("modules", var3);
      return var2;
   }

   Map<String, JsonObject> orphanModules() {
      return this.orphanModules;
   }

   Map<String, Boolean> pendingEnabled() {
      return this.pendingEnabled;
   }

   private void a() {
      if (!this.loaded) {
         this.load();
      }
   }

   private Path b() {
      return this.configPathOverride != null ? this.configPathOverride : FabricLoader.getInstance().getConfigDir().resolve("agartha").resolve("modules.json");
   }

   private static void a(Module arg, JsonObject jsonObject, Map<String, Boolean> map) {
      if (jsonObject.has("keybind") && jsonObject.get("keybind").isJsonPrimitive()) {
         try {
            arg.setKeybind(jsonObject.get("keybind").getAsInt());
         } catch (RuntimeException var13) {
         }
      }

      if (jsonObject.has("holdMode") && jsonObject.get("holdMode").isJsonPrimitive()) {
         try {
            arg.setHoldMode(jsonObject.get("holdMode").getAsBoolean());
         } catch (RuntimeException var12) {
         }
      }

      if (jsonObject.has("settings") && jsonObject.get("settings").isJsonObject()) {
         JsonObject var3 = jsonObject.getAsJsonObject("settings");

         for (Setting var5 : arg.settings()) {
            if (!var5.isSessionOnly()) {
               JsonElement var6 = var3.get(var5.name());

               for (String var10 : var5.aliases()) {
                  if (var6 != null) {
                     break;
                  }

                  var6 = var3.get(var10);
               }

               if (var6 != null) {
                  var5.deserialize(var6);
               }
            }
         }

         if (arg instanceof HandOverlayModule var15) {
            var15.applyShippedDefaults();
         }

         arg.afterSettingsLoaded(var3);
      }

      if (jsonObject.has("enabled") && jsonObject.get("enabled").isJsonPrimitive()) {
         boolean var14;
         try {
            var14 = jsonObject.get("enabled").getAsBoolean();
         } catch (RuntimeException var11) {
            return;
         }

         if (arg.persistEnabled()) {
            String var16 = arg.name();
            if (var14) {
               map.put(var16, true);
            } else {
               arg.setEnabled(false);
            }
         }
      }
   }

   private static JsonObject a(Module arg) {
      JsonObject var1 = new JsonObject();
      var1.addProperty("enabled", arg.isEnabled());
      var1.addProperty("keybind", arg.keybind());
      var1.addProperty("holdMode", arg.isHoldMode());
      JsonObject var2 = new JsonObject();

      for (Setting var4 : arg.settings()) {
         if (!var4.isSessionOnly()) {
            var2.add(var4.name(), var4.serialize());
         }
      }

      var1.add("settings", var2);
      return var1;
   }
}
