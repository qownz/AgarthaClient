package net.agartha.client.j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.agartha.client.AgarthaClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class d {
   private static final d INSTANCE = new d();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final int CONFIG_VERSION = 1;
   private final List<a> macros = new ArrayList<>();
   private boolean loaded;

   private d() {
   }

   public static d getInstance() {
      return INSTANCE;
   }

   public synchronized List<a> all() {
      this.a();
      return Collections.unmodifiableList(this.macros);
   }

   public synchronized a get(String string) {
      this.a();
      if (string == null) {
         return null;
      } else {
         for (a var3 : this.macros) {
            if (var3.name().equalsIgnoreCase(string)) {
               return var3;
            }
         }

         return null;
      }
   }

   public synchronized void add(a arg) {
      this.a();
      this.macros.add(arg);
      this.save();
   }

   public synchronized void remove(a arg) {
      this.a();
      if (this.macros.remove(arg)) {
         this.save();
      }
   }

   public synchronized JsonObject exportJsonObject() {
      this.a();
      JsonObject var1 = new JsonObject();
      var1.addProperty("version", 1);
      JsonArray var2 = new JsonArray();

      for (a var4 : this.macros) {
         var2.add(a(var4));
      }

      var1.add("macros", var2);
      return var1;
   }

   public synchronized void importJsonObject(JsonObject jsonObject) {
      this.a();
      this.macros.clear();

      for (JsonElement var4 : jsonObject != null && jsonObject.has("macros") && jsonObject.get("macros").isJsonArray()
         ? jsonObject.getAsJsonArray("macros")
         : new JsonArray()) {
         if (var4.isJsonObject()) {
            a var5 = a(var4.getAsJsonObject());
            if (var5 != null && !var5.name().isBlank()) {
               this.macros.add(var5);
            }
         }
      }

      this.save();
   }

   public synchronized void save() {
      JsonObject var1 = this.exportJsonObject();
      Path var2 = b();

      try {
         net.agartha.client.c.d.writeAtomic(var2, GSON.toJson(var1));
      } catch (IOException var4) {
         AgarthaClientMod.LOGGER.warn("[Agartha] Failed to save chat macros", var4);
      }
   }

   public synchronized void resetToDefaults() {
      this.macros.clear();
      this.loaded = true;
      this.save();
   }

   private void a() {
      if (!this.loaded) {
         this.loaded = true;
         Path var1 = b();
         if (Files.isRegularFile(var1)) {
            try (BufferedReader var2 = Files.newBufferedReader(var1)) {
               JsonObject var3 = JsonParser.parseReader(var2).getAsJsonObject();

               for (JsonElement var6 : var3.has("macros") ? var3.getAsJsonArray("macros") : new JsonArray()) {
                  if (var6.isJsonObject()) {
                     a var7 = a(var6.getAsJsonObject());
                     if (var7 != null && !var7.name().isBlank()) {
                        this.macros.add(var7);
                     }
                  }
               }
            } catch (RuntimeException | IOException var10) {
               AgarthaClientMod.LOGGER.warn("[Agartha] Failed to load chat macros; using empty list", var10);
               this.macros.clear();
            }
         }
      }
   }

   private static JsonObject a(a arg) {
      JsonObject var1 = new JsonObject();
      var1.addProperty("name", arg.name());
      JsonArray var2 = new JsonArray();

      for (String var4 : arg.messages()) {
         var2.add(var4);
      }

      var1.add("messages", var2);
      e var5 = arg.keybind();
      JsonObject var6 = new JsonObject();
      var6.addProperty("isKey", var5.isKey());
      var6.addProperty("value", var5.value());
      var6.addProperty("modifiers", var5.modifiers());
      var1.add("keybind", var6);
      return var1;
   }

   private static a a(JsonObject jsonObject) {
      if (jsonObject == null) {
         return null;
      } else {
         a var1 = new a();
         var1.setName(jsonObject.has("name") ? jsonObject.get("name").getAsString() : "");
         ArrayList var2 = new ArrayList();
         if (jsonObject.has("messages") && jsonObject.get("messages").isJsonArray()) {
            for (JsonElement var4 : jsonObject.getAsJsonArray("messages")) {
               if (var4.isJsonPrimitive()) {
                  var2.add(var4.getAsString());
               }
            }
         }

         var1.setMessages(var2);
         if (jsonObject.has("keybind") && jsonObject.get("keybind").isJsonObject()) {
            JsonObject var6 = jsonObject.getAsJsonObject("keybind");
            boolean var7 = var6.has("isKey") && var6.get("isKey").getAsBoolean();
            int var5 = var6.has("value") ? var6.get("value").getAsInt() : 0;
            var1.setKeybind(new e(var7, var5, 0));
         }

         return var1;
      }
   }

   private static Path b() {
      return FabricLoader.getInstance().getConfigDir().resolve("agartha").resolve("chat-macros.json");
   }
}
