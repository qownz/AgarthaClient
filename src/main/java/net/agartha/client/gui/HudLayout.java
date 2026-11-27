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
import net.agartha.client.gui.RegionMapHud;
import net.agartha.client.gui.UiScale;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.client.RegionMapModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class HudLayout {
   static final String LOGO_ID = "logo";
   static final String WATERMARK_ID = "watermark";
   static final String INFO_BAR_ID = "info_bar";
   static final String FPS_ID = "fps";
   static final String COORDINATES_ID = "coordinates";
   static final String NETHER_COORDINATES_ID = "nether_coordinates";
   static final String POTION_TRACKER_ID = "potion_tracker";
   static final String REGION_MAP_ID = "region_map";
   static final String SPOTIFY_HUD_ID = "spotify_hud";
   static final String KEYBINDS_ID = "keybinds";
   static final String STAFF_LIST_ID = "staff_list";
   static final String ARMOR_ID = "armor_hud";
   static final String INVENTORY_ID = "inventory_hud";
   private static final HudLayout INSTANCE = new HudLayout();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final int CONFIG_VERSION = 27;
   private static final float DEFAULT_MARGIN = 5.0F;
   static final float MIN_SCALE = 0.5F;
   static final float MAX_SCALE = 3.0F;
   private static final float DEFAULT_GAP = 2.0F;
   static final float LIST_GAP = 6.0F;
   private static final float CENTER_BAND = 0.1F;
   private static final float COORDINATES_DEFAULT_SCALE = 0.91F;
   private static final String[] KNOWN_IDS = new String[]{
      "info_bar",
      "coordinates",
      "potion_tracker",
      "region_map",
      "spotify_hud",
      "keybinds",
      "staff_list",
      "armor_hud",
      "inventory_hud",
      "box_brand",
      "box_fps",
      "box_ping",
      "box_tps",
      "box_clock",
      "box_memory",
      "box_speed",
      "box_position",
      "box_nether",
      "box_facing",
      "box_server",
      "box_players"
   };
   private static final float BOX_GAP = 4.0F;
   private static final float BOX_ROW_SHARE = 0.55F;
   private static final float HOTBAR_HALF_GUI = 91.0F;
   private static final float OFFHAND_GUI = 29.0F;
   private final Map<String, HudLayout.c> elements = new LinkedHashMap<>();
   private final Map<String, HudLayout.c> otherMode = new LinkedHashMap<>();
   private boolean modeTwoActive;
   private boolean loaded;
   private float flowRight;
   private final List<HudLayout.c> settled = new ArrayList<>();

   private HudLayout() {
   }

   public static HudLayout getInstance() {
      return INSTANCE;
   }

   synchronized void setModeTwo(boolean bl) {
      this.a();
      if (bl != this.modeTwoActive) {
         LinkedHashMap var2 = new LinkedHashMap<>(this.elements);
         this.elements.clear();
         if (this.otherMode.isEmpty()) {
            for (HudLayout.c var4 : (Iterable<HudLayout.c>) var2.values()) {
               HudLayout.c var5 = (HudLayout.c)GSON.fromJson(GSON.toJson(var4), HudLayout.c.class);
               var5.positioned = var4.positioned;
               if ("region_map".equals(var5.id) || "keybinds".equals(var5.id) || "staff_list".equals(var5.id)) {
                  var5.userMoved = false;
               }

               this.elements.put(var5.id, var5);
            }
         } else {
            this.elements.putAll(this.otherMode);
         }

         this.d();
         this.otherMode.clear();
         this.otherMode.putAll(var2);
         this.modeTwoActive = bl;
      }
   }

   synchronized HudLayout.c element(String string) {
      this.a();
      HudLayout.c var2 = this.elements.get(string);
      if (var2 == null) {
         var2 = a(string);
         this.elements.put(string, var2);
      }

      return var2;
   }

   synchronized List<HudLayout.c> orderedElements() {
      this.a();
      return List.copyOf(this.elements.values());
   }

   synchronized void updateBounds(String string, float f, float g, float h, float i) {
      HudLayout.c var6 = this.element(string);
      var6.width = Math.max(1.0F, f);
      var6.height = Math.max(1.0F, g);
      if ("info_bar".equals(string) && !var6.userMoved && h > 0.0F) {
         var6.x = Math.max(0.0F, (h - var6.width) * 0.5F);
         if (!var6.positioned) {
            var6.y = 5.0F;
            var6.positioned = true;
         }

         c(var6, h, i);
         f(var6, h, i);
      } else {
         if (!var6.positioned) {
            this.e(var6, h, i);
         } else {
            if (!var6.hasAnchor) {
               c(var6, h, i);
            }

            a(var6);
            float var7 = Math.max(0.0F, h - var6.width);
            float var8 = Math.max(0.0F, i - var6.height);
            if (var6.x > var7 || var6.y > var8) {
               var6.positioned = false;
               var6.hasAnchor = false;
               this.e(var6, h, i);
            }
         }

         f(var6, h, i);
      }
   }

   synchronized void setScale(String string, float f) {
      this.element(string).scale = a(b(f) ? f : 1.0F, 0.5F, 3.0F);
   }

   synchronized float scaleOf(String string) {
      HudLayout.c var2 = this.element(string);
      return b(var2.scale) ? a(var2.scale, 0.5F, 3.0F) : 1.0F;
   }

   synchronized void dockToRegionMap(boolean bl, boolean bl2, float f, float g) {
      HudLayout.c var5 = this.elements.get("region_map");
      HudLayout.c var6 = this.elements.get("keybinds");
      HudLayout.c var7 = this.elements.get("staff_list");
      boolean var8 = var5 != null && var5.positioned;
      if (var6 != null && !var6.userMoved) {
         var6.x = var8 ? var5.x : 5.0F;
         var6.y = var8 ? var5.y + var5.height + 6.0F : 5.0F;
         this.b(var6, f, g);
      }

      if (var7 != null && !var7.userMoved) {
         this.a(var7, f, g);
         this.b(var7, f, g);
      }
   }

   private void a(HudLayout.c arg, float f, float g) {
      HudLayout.c var4 = this.elements.get("spotify_hud");
      float var5 = var4 != null && var4.positioned ? var4.y : g - 5.0F - c("spotify_hud");
      arg.x = Math.max(5.0F, f - 5.0F - Math.max(1.0F, arg.width));
      arg.y = Math.max(5.0F, var5 - 6.0F - Math.max(1.0F, arg.height));
   }

   synchronized float flowBoxes(String[] strings, boolean[] bls, int i, List<String> list, float f, float g) {
      float var7 = 4.0F;
      float var8 = Math.max(200.0F, f * 0.55F);
      float var9 = 5.0F;
      float var10 = 5.0F;
      this.flowRight = 0.0F;
      float var11 = 0.0F;
      float var12 = 5.0F;
      boolean var13 = false;
      boolean var14 = false;

      for (int var15 = 0; var15 < strings.length; var15++) {
         if (bls[var15]) {
            HudLayout.c var16 = this.elements.get(strings[var15]);
            if (var16 != null && !var16.userMoved) {
               boolean var17 = var15 >= i && !var14;
               if (var17) {
                  var14 = true;
               }

               if (var17 && var11 > 0.0F || var9 > 5.0F && var9 + var16.width > var8) {
                  var9 = 5.0F;
                  var10 += var11 + var7;
                  var11 = 0.0F;
               }

               for (int var18 = 0; var18 < 8; var18++) {
                  HudLayout.c var19 = this.a(list, var9, var10, var16.width, var16.height);
                  if (var19 == null) {
                     break;
                  }

                  var9 = var19.x + var19.width + var7;
                  if (var9 + var16.width > f - 5.0F) {
                     var9 = 5.0F;
                     var10 += Math.max(var11, var16.height) + var7;
                     var11 = 0.0F;
                  }
               }

               var16.x = var9;
               var16.y = var10;
               this.b(var16, f, g);
               this.flowRight = Math.max(this.flowRight, var9 + var16.width);
               var9 += var16.width + var7;
               var11 = Math.max(var11, var16.height);
               var12 = Math.max(var12, var10 + var16.height);
               var13 = true;
            }
         }
      }

      return var13 ? var12 + 6.0F : 5.0F;
   }

   synchronized void keepRegionUnderBoxes(float f) {
      HudLayout.c var2 = this.elements.get("region_map");
      if (var2 != null && var2.positioned && var2.userMoved && !(this.flowRight <= 0.0F)) {
         boolean var3 = var2.x < this.flowRight && var2.x + var2.width > 5.0F;
         if (var3 && !(var2.y >= f)) {
            var2.userMoved = false;
         }
      }
   }

   private HudLayout.c a(List<String> list, float f, float g, float h, float i) {
      for (String var7 : list) {
         HudLayout.c var8 = this.elements.get(var7);
         if (var8 != null && a(var8, f, g, h, i)) {
            return var8;
         }
      }

      return null;
   }

   private static boolean a(HudLayout.c arg, float f, float g, float h, float i) {
      return f < arg.x + arg.width && f + h > arg.x && g < arg.y + arg.height && g + i > arg.y;
   }

   synchronized void dockRegionAt(float f, float g, float h) {
      HudLayout.c var4 = this.elements.get("region_map");
      if (var4 != null && var4.positioned && !var4.userMoved) {
         if (!(Math.abs(var4.x - 5.0F) < 0.01F) || !(Math.abs(var4.y - f) < 0.01F)) {
            var4.x = 5.0F;
            var4.y = f;
            this.b(var4, g, h);
         }
      }
   }

   synchronized void separate(List<String> list, float f, float g) {
      this.settled.clear();

      for (int var4 = 0; var4 < list.size(); var4++) {
         HudLayout.c var5 = this.elements.get(list.get(var4));
         if (var5 != null && var5.userMoved) {
            this.settled.add(var5);
         }
      }

      for (int var10 = 0; var10 < list.size(); var10++) {
         HudLayout.c var11 = this.elements.get(list.get(var10));
         if (var11 != null && !var11.userMoved) {
            for (int var6 = 0; var6 < 10; var6++) {
               HudLayout.c var7 = null;

               for (int var8 = 0; var8 < this.settled.size(); var8++) {
                  HudLayout.c var9 = this.settled.get(var8);
                  if (a(var9, var11.x, var11.y, var11.width, var11.height)) {
                     var7 = var9;
                     break;
                  }
               }

               if (var7 == null) {
                  break;
               }

               a(var11, var7, f, g);
            }

            this.settled.add(var11);
         }
      }
   }

   private static void a(HudLayout.c arg, HudLayout.c arg2, float f, float g) {
      float var4 = 3.0F;
      float var5 = arg.x + arg.width - arg2.x + var4;
      float var6 = arg2.x + arg2.width - arg.x + var4;
      float var7 = arg.y + arg.height - arg2.y + var4;
      float var8 = arg2.y + arg2.height - arg.y + var4;
      float var9 = Float.MAX_VALUE;
      byte var10 = -1;
      if (arg.x - var5 >= 0.0F && var5 < var9) {
         var9 = var5;
         var10 = 0;
      }

      if (arg.x + var6 + arg.width <= f && var6 < var9) {
         var9 = var6;
         var10 = 1;
      }

      if (arg.y - var7 >= 0.0F && var7 < var9) {
         var9 = var7;
         var10 = 2;
      }

      if (arg.y + var8 + arg.height <= g && var8 < var9) {
         var10 = 3;
      }

      switch (var10) {
         case 0:
            arg.x -= var5;
            break;
         case 1:
            arg.x += var6;
            break;
         case 2:
            arg.y -= var7;
            break;
         case 3:
            arg.y += var8;
            break;
         default:
            arg.y += var8;
      }

      f(arg, f, g);
   }

   synchronized void resolveOverlap(String string, List<String> list, float f, float g) {
      HudLayout.c var5 = this.elements.get(string);
      if (var5 != null) {
         for (int var6 = 0; var6 < 12; var6++) {
            HudLayout.c var7 = null;

            for (String var9 : list) {
               if (!var9.equals(string)) {
                  HudLayout.c var10 = this.elements.get(var9);
                  if (var10 != null && a(var10, var5.x, var5.y, var5.width, var5.height)) {
                     var7 = var10;
                     break;
                  }
               }
            }

            if (var7 == null) {
               break;
            }

            float var14 = var5.x + var5.width - var7.x;
            float var15 = var7.x + var7.width - var5.x;
            float var16 = var5.y + var5.height - var7.y;
            float var11 = var7.y + var7.height - var5.y;
            float var12 = Math.min(Math.min(var14, var15), Math.min(var16, var11));
            float var13 = 2.0F;
            if (var12 == var14 && var5.x - var14 - var13 >= 0.0F) {
               var5.x -= var14 + var13;
            } else if (var12 == var15 && var5.x + var15 + var13 + var5.width <= f) {
               var5.x += var15 + var13;
            } else if (var12 == var16 && var5.y - var16 - var13 >= 0.0F) {
               var5.y -= var16 + var13;
            } else if (var5.y + var11 + var13 + var5.height <= g) {
               var5.y += var11 + var13;
            } else {
               var5.x += var15 + var13;
            }

            f(var5, f, g);
         }

         c(var5, f, g);
      }
   }

   synchronized void undockLists(float f, float g) {
      for (String var6 : new String[]{"keybinds"}) {
         HudLayout.c var7 = this.elements.get(var6);
         if (var7 != null && var7.positioned && !var7.userMoved) {
            var7.userMoved = true;
            c(var7, f, g);
         }
      }
   }

   private void b(HudLayout.c arg, float f, float g) {
      arg.positioned = true;
      f(arg, f, g);
      c(arg, f, g);
   }

   synchronized void moveTo(String string, float f, float g, float h, float i) {
      HudLayout.c var6 = this.element(string);
      var6.x = f;
      var6.y = g;
      var6.positioned = true;
      var6.userMoved = true;
      f(var6, h, i);
      c(var6, h, i);
   }

   synchronized void setRect(String string, float f, float g, float h, float i, float j, float k) {
      HudLayout.c var8 = this.element(string);
      var8.width = Math.max(1.0F, h);
      var8.height = Math.max(1.0F, i);
      var8.x = f;
      var8.y = g;
      var8.positioned = true;
      if (!var8.hasAnchor) {
         c(var8, j, k);
      }

      f(var8, j, k);
   }

   synchronized float[] targetOrigin(String string, float f, float g, float h, float i) {
      HudLayout.c var6 = this.element(string);
      f = Math.max(1.0F, f);
      g = Math.max(1.0F, g);
      if (!var6.positioned) {
         this.e(var6, h, i);
      } else if (!var6.hasAnchor) {
         d(var6, h, i);
         c(var6, h, i);
      }

      float var7 = a(var6, f);
      float var8 = b(var6, g);
      float var9 = Math.max(0.0F, h - f);
      float var10 = Math.max(0.0F, i - g);
      return new float[]{a(var7, 0.0F, var9), a(var8, 0.0F, var10)};
   }

   synchronized void refreshAnchor(String string, float f, float g) {
      HudLayout.c var4 = this.element(string);
      if (var4.positioned) {
         c(var4, f, g);
      }
   }

   private static void c(HudLayout.c arg, float f, float g) {
      float var3 = arg.x;
      float var4 = Math.max(0.0F, f - (arg.x + arg.width));
      float var5 = arg.y;
      float var6 = Math.max(0.0F, g - (arg.y + arg.height));
      if ("potion_tracker".equals(arg.id)) {
         arg.anchorRight = var4 <= var3;
      } else {
         arg.anchorRight = var4 < var3;
      }

      if (!"potion_tracker".equals(arg.id) && !"coordinates".equals(arg.id)) {
         arg.anchorBottom = var6 < var5;
      } else {
         arg.anchorBottom = var6 <= var5;
      }

      float var7 = arg.x + arg.width * 0.5F;
      float var8 = arg.y + arg.height * 0.5F;
      arg.anchorCenterX = f > 0.0F && Math.abs(var7 - f * 0.5F) <= f * 0.1F;
      arg.anchorCenterY = g > 0.0F && Math.abs(var8 - g * 0.5F) <= g * 0.1F;
      arg.anchorEdgeX = arg.anchorCenterX ? var7 : (arg.anchorRight ? arg.x + arg.width : arg.x);
      arg.anchorEdgeY = arg.anchorCenterY ? var8 : (arg.anchorBottom ? arg.y + arg.height : arg.y);
      arg.hasAnchor = true;
   }

   private static float a(HudLayout.c arg, float f) {
      if (arg.anchorCenterX) {
         return arg.anchorEdgeX - f * 0.5F;
      } else {
         return arg.anchorRight ? arg.anchorEdgeX - f : arg.anchorEdgeX;
      }
   }

   private static float b(HudLayout.c arg, float f) {
      if (arg.anchorCenterY) {
         return arg.anchorEdgeY - f * 0.5F;
      } else {
         return arg.anchorBottom ? arg.anchorEdgeY - f : arg.anchorEdgeY;
      }
   }

   private static void d(HudLayout.c arg, float f, float g) {
      if ("potion_tracker".equals(arg.id)) {
         float var3 = arg.x + arg.width;
         float var4 = f - var3;
         if (var4 > 5.5F && var3 > f * 0.45F) {
            arg.x = Math.max(5.0F, f - 5.0F - arg.width);
         }

         if ("potion_tracker".equals(arg.id) || "coordinates".equals(arg.id)) {
            float var5 = arg.y + arg.height;
            float var6 = g - var5;
            if (var6 > 5.5F && var5 > g * 0.45F) {
               arg.y = Math.max(5.0F, g - 5.0F - arg.height);
            }
         }
      }
   }

   private static void a(HudLayout.c arg) {
      if (arg.hasAnchor) {
         arg.x = a(arg, arg.width);
         arg.y = b(arg, arg.height);
      }
   }

   public synchronized void resetToDefaults(float f, float g) {
      if (f <= 0.0F || g <= 0.0F) {
         MinecraftClient var3 = MinecraftClient.getInstance();
         f = UiScale.width(var3);
         g = UiScale.height(var3);
      }

      this.elements.clear();
      this.otherMode.clear();

      for (String var6 : KNOWN_IDS) {
         HudLayout.c var7 = a(var6);
         var7.userMoved = false;
         var7.width = d(var6);
         var7.height = c(var6);
         var7.scale = b(var6);
         var7.visible = true;
         this.elements.put(var7.id, var7);
      }

      for (HudLayout.c var10 : this.elements.values()) {
         this.e(var10, f, g);
         f(var10, f, g);
      }

      this.save();
   }

   public synchronized void save() {
      this.a();
      HudLayout.a var1 = this.b();
      Path var2 = f();

      try {
         ConfigFileIO.writeAtomic(var2, GSON.toJson(var1));
      } catch (IOException var4) {
         AgarthaClientMod.LOGGER.warn("[Agartha] Failed to save HUD layout config", var4);
      }
   }

   public synchronized JsonObject exportJsonObject() {
      this.a();
      return GSON.toJsonTree(this.b()).getAsJsonObject();
   }

   public synchronized boolean importJsonObject(JsonObject jsonObject, float f, float g) {
      if (jsonObject == null) {
         return false;
      } else {
         try {
            HudLayout.a var4 = (HudLayout.a)GSON.fromJson(jsonObject, HudLayout.a.class);
            if (var4 != null && var4.elements != null && var4.version <= 27) {
               this.loaded = true;
               this.a(var4, f, g);
               this.save();
               return true;
            } else {
               return false;
            }
         } catch (RuntimeException var5) {
            AgarthaClientMod.LOGGER.warn("[Agartha] Failed to import HUD layout config", var5);
            return false;
         }
      }
   }

   private void a() {
      if (!this.loaded) {
         this.loaded = true;
         this.c();
         Path var1 = f();
         if (Files.isRegularFile(var1)) {
            try {
               try (BufferedReader var2 = Files.newBufferedReader(var1)) {
                  HudLayout.a var3 = (HudLayout.a)GSON.fromJson(var2, HudLayout.a.class);
                  if (var3 != null && var3.elements != null) {
                     float var4 = UiScale.width(MinecraftClient.getInstance());
                     float var5 = UiScale.height(MinecraftClient.getInstance());
                     boolean var6 = this.a(var3, var4, var5);
                     if (var6) {
                        this.save();
                     }

                     return;
                  }
               }
            } catch (RuntimeException | IOException var9) {
               AgarthaClientMod.LOGGER.warn("[Agartha] Failed to load HUD layout config; using defaults", var9);
               this.elements.clear();
               this.otherMode.clear();
               this.modeTwoActive = false;
               this.c();
            }
         }
      }
   }

   private HudLayout.a b() {
      HudLayout.a var1 = new HudLayout.a();
      var1.version = 27;
      Map var2 = this.modeTwoActive ? this.otherMode : this.elements;
      Map var3 = this.modeTwoActive ? this.elements : this.otherMode;
      var1.elements = a(var2);
      var1.elementsModeTwo = var3.isEmpty() ? null : a(var3);
      return var1;
   }

   private static List<HudLayout.c> a(Map<String, HudLayout.c> map) {
      ArrayList var1 = new ArrayList();

      for (HudLayout.c var3 : map.values()) {
         if (var3.positioned) {
            var1.add(var3);
         }
      }

      return var1;
   }

   private boolean a(HudLayout.a arg, float f, float g) {
      this.modeTwoActive = false;
      this.otherMode.clear();
      if (arg.elementsModeTwo != null) {
         for (HudLayout.c var5 : arg.elementsModeTwo) {
            if (var5 != null && var5.id != null && e(var5.id)) {
               b(var5);
               this.otherMode.put(var5.id, var5);
            }
         }
      }

      this.elements.clear();
      this.c();

      for (HudLayout.c var13 : arg.elements) {
         if (var13 != null && var13.id != null && !var13.id.isBlank() && e(var13.id)) {
            b(var13);
            this.elements.put(var13.id, var13);
         }
      }

      this.d();
      boolean var12 = false;
      if (arg.version < 18) {
         for (String var8 : new String[]{"region_map", "spotify_hud", "coordinates"}) {
            HudLayout.c var9 = this.elements.get(var8);
            if (var9 != null) {
               var9.positioned = false;
               var9.hasAnchor = false;
            }
         }

         var12 = true;
      }

      if (arg.version < 19) {
         for (String var36 : new String[]{"potion_tracker", "spotify_hud"}) {
            HudLayout.c var41 = this.elements.get(var36);
            if (var41 != null) {
               var41.positioned = false;
               var41.hasAnchor = false;
            }
         }

         var12 = true;
      }

      if (arg.version < 20) {
         HudLayout.c var16 = this.elements.get("potion_tracker");
         if (var16 != null) {
            var16.positioned = false;
            var16.hasAnchor = false;
         }

         var12 = true;
      }

      if (arg.version < 22) {
         for (String var37 : new String[]{"coordinates", "keybinds", "staff_list"}) {
            HudLayout.c var42 = this.elements.get(var37);
            if (var42 != null) {
               var42.positioned = false;
               var42.hasAnchor = false;
               var42.userMoved = false;
               var42.scale = b(var37);
            }
         }

         var12 = true;
      }

      if (arg.version < 27) {
         HudLayout.c var18 = this.elements.get("info_bar");
         if (var18 != null && !var18.userMoved) {
            var18.positioned = false;
            var18.hasAnchor = false;
         }

         var12 = true;
      }

      if (arg.version < 26) {
         HudLayout.c var19 = this.elements.get("region_map");
         if (var19 != null) {
            var19.positioned = false;
            var19.hasAnchor = false;
            var19.userMoved = false;
         }

         for (String var43 : new String[]{"keybinds", "staff_list"}) {
            HudLayout.c var10 = this.elements.get(var43);
            if (var10 != null) {
               var10.userMoved = false;
            }
         }

         var12 = true;
      }

      if (arg.version < 25) {
         HudLayout.c var20 = this.elements.get("info_bar");
         if (var20 != null) {
            var20.hasAnchor = false;
         }

         var12 = true;
      }

      if (arg.version < 24) {
         for (String var39 : new String[]{"keybinds", "staff_list"}) {
            HudLayout.c var44 = this.elements.get(var39);
            if (var44 != null) {
               var44.userMoved = false;
            }
         }

         var12 = true;
      }

      if (arg.version < 23) {
         HudLayout.c var22 = this.elements.get("coordinates");
         if (var22 != null) {
            var22.scale = 0.91F;
         }

         var12 = true;
      }

      if (arg.version < 21) {
         for (String var40 : new String[]{"potion_tracker", "info_bar", "spotify_hud", "coordinates"}) {
            HudLayout.c var45 = this.elements.get(var40);
            if (var45 != null) {
               var45.positioned = false;
               var45.hasAnchor = false;
            }
         }

         var12 = true;
      }

      if (f > 0.0F && g > 0.0F) {
         for (HudLayout.c var30 : this.elements.values()) {
            if (!var30.positioned) {
               this.e(var30, f, g);
            } else if (!var30.hasAnchor) {
               c(var30, f, g);
            }

            f(var30, f, g);
         }
      }

      return var12 || arg.version < 27;
   }

   private void c() {
      for (String var4 : KNOWN_IDS) {
         this.elements.put(var4, a(var4));
      }
   }

   private void d() {
      for (String var4 : KNOWN_IDS) {
         this.elements.computeIfAbsent(var4, HudLayout::a);
      }
   }

   private void e() {
      for (String var4 : KNOWN_IDS) {
         HudLayout.c var5 = this.elements.get(var4);
         if (var5 != null) {
            var5.positioned = false;
         }
      }
   }

   private static HudLayout.c a(String string) {
      HudLayout.c var1 = new HudLayout.c();
      var1.id = string;
      var1.x = 0.0F;
      var1.y = 0.0F;
      var1.width = d(string);
      var1.height = c(string);
      var1.visible = true;
      var1.scale = b(string);
      var1.positioned = false;
      return var1;
   }

   private static float b(String string) {
      return "coordinates".equals(string) ? 0.91F : 1.0F;
   }

   private static void b(HudLayout.c arg) {
      arg.scale = b(arg.scale) ? a(arg.scale, 0.5F, 3.0F) : 1.0F;
      arg.width = b(arg.width) && arg.width > 0.0F ? arg.width : 1.0F;
      arg.height = b(arg.height) && arg.height > 0.0F ? arg.height : 1.0F;
      arg.positioned = b(arg.x) && b(arg.y);
   }

   private void e(HudLayout.c arg, float f, float g) {
      String var4 = arg.id;
      switch (var4) {
         case "potion_tracker":
            arg.x = Math.max(5.0F, f - 5.0F - Math.max(1.0F, arg.width));
            arg.y = 5.0F;
            break;
         case "info_bar":
            arg.x = Math.max(5.0F, (f - arg.width) * 0.5F);
            arg.y = 0.0F;
            break;
         case "coordinates":
            arg.x = 5.0F;
            arg.y = Math.max(5.0F, g - 5.0F - Math.max(1.0F, arg.height));
            break;
         case "region_map":
            c(arg);
            arg.x = 5.0F;
            arg.y = 5.0F;
            break;
         case "spotify_hud":
            arg.x = Math.max(5.0F, f - 5.0F - Math.max(1.0F, arg.width));
            arg.y = Math.max(5.0F, g - 5.0F - Math.max(1.0F, arg.height));
            break;
         case "keybinds":
            HudLayout.c var9 = this.a(f, g);
            arg.x = var9 != null ? var9.x : 5.0F;
            arg.y = (var9 != null ? var9.y + Math.max(1.0F, var9.height) : 5.0F + c("region_map")) + 6.0F;
            break;
         case "armor_hud":
            float var8 = a(f);
            float var7 = f * 0.5F - 124.0F * var8;
            arg.x = Math.max(5.0F, var7 - Math.max(1.0F, arg.width));
            arg.y = Math.max(5.0F, g - 5.0F - Math.max(1.0F, arg.height));
            break;
         case "inventory_hud":
            float var6 = a(f);
            arg.x = f * 0.5F + 124.0F * var6;
            arg.y = Math.max(5.0F, g - 5.0F - Math.max(1.0F, arg.height));
            break;
         case "staff_list":
            this.a(arg, f, g);
            break;
         default:
            arg.x = 5.0F;
            arg.y = 5.0F;
      }

      arg.positioned = true;
      f(arg, f, g);
      c(arg, f, g);
   }

   private static float a(float f) {
      int var1 = MinecraftClient.getInstance().getWindow().getScaledWidth();
      return var1 > 0 ? f / var1 : 2.0F;
   }

   private HudLayout.c a(float f, float g) {
      HudLayout.c var3 = this.elements.get("region_map");
      if (var3 == null) {
         return null;
      } else {
         if (!var3.positioned) {
            this.e(var3, f, g);
         }

         c(var3);
         return var3;
      }
   }

   private static float c(String string) {
      return switch (string) {
         case "coordinates" -> 56.0F;
         case "spotify_hud" -> 76.0F;
         case "region_map" -> 280.0F;
         case "armor_hud" -> 48.0F;
         case "inventory_hud" -> 85.0F;
         default -> 26.0F;
      };
   }

   private static float d(String string) {
      return switch (string) {
         case "info_bar" -> 320.0F;
         case "coordinates" -> 240.0F;
         case "potion_tracker" -> 190.0F;
         case "region_map" -> 234.0F;
         case "spotify_hud" -> 280.0F;
         case "armor_hud" -> 144.0F;
         case "inventory_hud" -> 233.0F;
         default -> 110.0F;
      };
   }

   private static void c(HudLayout.c arg) {
      if (arg != null) {
         RegionMapModule var1 = ModuleManager.get(RegionMapModule.class);
         if (var1 != null) {
            RegionMapHud.b var2 = RegionMapHud.measureSize(var1);
            arg.width = var2.width();
            arg.height = var2.height();
         } else {
            arg.width = Math.max(arg.width, d("region_map"));
            arg.height = Math.max(arg.height, c("region_map"));
         }
      }
   }

   private static void f(HudLayout.c arg, float f, float g) {
      float var3 = Math.max(0.0F, f - Math.max(1.0F, arg.width));
      float var4 = Math.max(0.0F, g - Math.max(1.0F, arg.height));
      arg.x = a(arg.x, 0.0F, var3);
      arg.y = a(arg.y, 0.0F, var4);
   }

   private float a(String string, float f) {
      HudLayout.c var3 = this.elements.get(string);
      return var3 != null && b(var3.width) && var3.width > 0.0F ? var3.width : f;
   }

   private float b(String string, float f) {
      HudLayout.c var3 = this.elements.get(string);
      return var3 != null && b(var3.height) && var3.height > 0.0F ? var3.height : f;
   }

   private static Path f() {
      return FabricLoader.getInstance().getConfigDir().resolve("agartha").resolve("hud.json");
   }

   private static boolean e(String string) {
      for (String var4 : KNOWN_IDS) {
         if (var4.equals(string)) {
            return true;
         }
      }

      return false;
   }

   private static boolean b(float f) {
      return !Float.isNaN(f) && !Float.isInfinite(f);
   }

   private static float a(float f, float g, float h) {
      return Math.max(g, Math.min(h, f));
   }

   @Environment(EnvType.CLIENT)
   private static final class a {
      int version;
      List<HudLayout.c> elements;
      List<HudLayout.c> elementsModeTwo;
   }

   @Environment(EnvType.CLIENT)
   private record b(float width, float height) {
   }

   @Environment(EnvType.CLIENT)
   static final class c {
      String id;
      float x;
      float y;
      float width;
      float height;
      boolean visible;
      float scale;
      boolean userMoved;
      boolean hasAnchor;
      boolean anchorRight;
      boolean anchorBottom;
      boolean anchorCenterX;
      boolean anchorCenterY;
      float anchorEdgeX;
      float anchorEdgeY;
      transient boolean positioned;
   }
}
