package net.agartha.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.config.ConfigFileIO;
import net.agartha.client.module.ModuleCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class ClickGuiLayout {
   static final String SEARCH_PANEL_ID = "search";
   private static final ClickGuiLayout INSTANCE = new ClickGuiLayout();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final int CONFIG_VERSION = 3;
   private static final float MIN_X = 20.0F;
   private static final float MIN_Y = 30.0F;
   private final Map<String, ClickGuiLayout.b> positions = new LinkedHashMap<>();
   private boolean loaded;
   private boolean dirty;
   private int layoutEpoch;
   private boolean reapplyPending;

   private ClickGuiLayout() {
   }

   public static ClickGuiLayout getInstance() {
      return INSTANCE;
   }

   public synchronized int layoutEpoch() {
      return this.layoutEpoch;
   }

   public synchronized boolean isReapplyPending() {
      return this.reapplyPending;
   }

   public synchronized void markReapplyConsumed() {
      this.reapplyPending = false;
   }

   public synchronized float[] resolvePosition(String string, float f, float g, float h, float i, float j) {
      this.b();
      if (this.reapplyPending) {
         return a(f, g, h, i, j);
      } else {
         ClickGuiLayout.b var7 = this.positions.get(string);
         if (var7 != null && var7.positioned) {
            float[] var8 = a(var7.x, var7.y, h, i, j);
            boolean var9 = Math.abs(var7.x - var8[0]) > 0.5F || Math.abs(var7.y - var8[1]) > 0.5F;
            return !var9 ? var8 : a(f, g, h, i, j);
         } else {
            return a(f, g, h, i, j);
         }
      }
   }

   public synchronized void updatePosition(String string, float f, float g, float h, float i, float j) {
      this.b();
      if (!this.reapplyPending) {
         float[] var7 = a(f, g, h, i, j);
         ClickGuiLayout.b var8 = this.positions.get(string);
         if (var8 == null || !var8.positioned || !(Math.abs(var8.x - var7[0]) < 0.5F) || !(Math.abs(var8.y - var7[1]) < 0.5F)) {
            ClickGuiLayout.b var9 = new ClickGuiLayout.b();
            var9.id = string;
            var9.x = var7[0];
            var9.y = var7[1];
            var9.positioned = true;
            this.positions.put(string, var9);
            this.dirty = true;
         }
      }
   }

   public synchronized JsonObject exportJsonObject() {
      this.b();
      ClickGuiLayout.a var1 = new ClickGuiLayout.a();
      var1.version = 3;
      var1.panels = new ArrayList<>(this.positions.values());
      return GSON.toJsonTree(var1).getAsJsonObject();
   }

   public synchronized void importJsonObject(JsonObject jsonObject) {
      this.b();
      this.positions.clear();

      try {
         ClickGuiLayout.a var2 = (ClickGuiLayout.a)GSON.fromJson(jsonObject, ClickGuiLayout.a.class);
         if (var2 != null && var2.panels != null && var2.version >= 3) {
            for (ClickGuiLayout.b var4 : var2.panels) {
               if (var4 != null && var4.id != null && !var4.id.isBlank() && a(var4.id)) {
                  var4.positioned = a(var4.x) && a(var4.y);
                  this.positions.put(var4.id, var4);
               }
            }
         }
      } catch (RuntimeException var5) {
         AgarthaClientMod.LOGGER.warn("[Agartha] Could not apply click GUI layout from profile", var5);
      }

      this.layoutEpoch++;
      this.reapplyPending = true;
      this.dirty = true;
      this.a();
   }

   public synchronized void saveIfDirty() {
      if (this.dirty) {
         this.a();
      }
   }

   public synchronized void resetToDefaults() {
      this.positions.clear();
      this.layoutEpoch++;
      this.reapplyPending = true;
      this.dirty = true;
      this.a();
   }

   private synchronized void a() {
      this.b();
      Path var1 = c();

      try {
         ClickGuiLayout.a var2 = new ClickGuiLayout.a();
         var2.version = 3;
         var2.panels = new ArrayList<>(this.positions.values());
         ConfigFileIO.writeAtomic(var1, GSON.toJson(var2));
         this.dirty = false;
      } catch (IOException var3) {
         AgarthaClientMod.LOGGER.warn("[Agartha] Failed to save click GUI layout", var3);
      }
   }

   private void b() {
      if (!this.loaded) {
         this.loaded = true;
         Path var1 = c();
         if (Files.isRegularFile(var1)) {
            try {
               try (BufferedReader var2 = Files.newBufferedReader(var1)) {
                  ClickGuiLayout.a var3 = (ClickGuiLayout.a)GSON.fromJson(var2, ClickGuiLayout.a.class);
                  if (var3 != null && var3.panels != null) {
                     if (var3.version < 3) {
                        return;
                     }

                     for (ClickGuiLayout.b var5 : var3.panels) {
                        if (var5 != null && var5.id != null && !var5.id.isBlank() && a(var5.id)) {
                           var5.positioned = a(var5.x) && a(var5.y);
                           this.positions.put(var5.id, var5);
                        }
                     }

                     return;
                  }
               }
            } catch (RuntimeException | IOException var8) {
               AgarthaClientMod.LOGGER.warn("[Agartha] Failed to load click GUI layout; using defaults", var8);
            }
         }
      }
   }

   private static boolean a(String string) {
      if ("search".equals(string)) {
         return true;
      } else {
         for (ModuleCategory var4 : ModuleCategory.values()) {
            if (var4.name().equals(string)) {
               return true;
            }
         }

         return false;
      }
   }

   private static float[] a(float f, float g, float h, float i, float j) {
      float var5 = i > 0.0F ? Math.max(20.0F, i - h - 20.0F) : f;
      float var6 = j > 0.0F ? Math.max(30.0F, j - 120.0F) : g;
      return new float[]{Math.max(20.0F, Math.min(f, var5)), Math.max(30.0F, Math.min(g, var6))};
   }

   private static boolean a(float f) {
      return !Float.isNaN(f) && !Float.isInfinite(f);
   }

   private static Path c() {
      return FabricLoader.getInstance().getConfigDir().resolve("agartha").resolve("clickgui.json");
   }

   @Environment(EnvType.CLIENT)
   private static final class a {
      int version;
      List<ClickGuiLayout.b> panels;
   }

   @Environment(EnvType.CLIENT)
   static final class b {
      String id;
      float x;
      float y;
      boolean positioned;
   }
}
