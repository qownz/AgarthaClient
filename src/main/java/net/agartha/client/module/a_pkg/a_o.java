package net.agartha.client.module.a_pkg;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import imgui.type.ImBoolean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class a_o extends k {
   private List<net.agartha.client.module.a_pkg.a_o.a> entries;
   private Supplier<List<net.agartha.client.module.a_pkg.a_o.a>> entriesSupplier;
   private final float[] defaultColor;
   private List<net.agartha.client.module.a_pkg.a_o.a> defaultSnapshot;
   private final float[] defaultColorBaseline;
   private Map<String, Integer> enabledLookup;
   private long lookupVersion = -1L;
   private long currentVersion = 0L;

   public a_o(String string, List<net.agartha.client.module.a_pkg.a_o.a> list, float[] fs) {
      super(string);
      this.entries = new ArrayList<>(list);
      this.defaultColor = fs;
      this.defaultColorBaseline = (float[])fs.clone();
      this.defaultSnapshot = a(this.entries);
      this.onChange(this::invalidateLookup);
   }

   public a_o(String string, Supplier<List<net.agartha.client.module.a_pkg.a_o.a>> supplier, float[] fs) {
      super(string);
      this.entriesSupplier = supplier;
      this.defaultColor = fs;
      this.defaultColorBaseline = (float[])fs.clone();
      this.onChange(this::invalidateLookup);
   }

   private List<net.agartha.client.module.a_pkg.a_o.a> a() {
      if (this.entries == null) {
         this.entries = new ArrayList<>(this.entriesSupplier.get());
         this.entriesSupplier = null;
         this.defaultSnapshot = a(this.entries);
      }

      return this.entries;
   }

   public List<net.agartha.client.module.a_pkg.a_o.a> entries() {
      return Collections.unmodifiableList(this.a());
   }

   public float[] defaultColor() {
      return this.defaultColor;
   }

   public void resetColorToDefault(net.agartha.client.module.a_pkg.a_o.a arg) {
      this.d();
      if (arg == null) {
         System.arraycopy(this.defaultColorBaseline, 0, this.defaultColor, 0, this.defaultColor.length);
      } else {
         for (net.agartha.client.module.a_pkg.a_o.a var3 : this.defaultSnapshot) {
            if (var3.id().equals(arg.id())) {
               System.arraycopy(var3.color(), 0, arg.color(), 0, arg.color().length);
               break;
            }
         }
      }

      this.notifyChanged();
   }

   public void invalidateLookup() {
      this.currentVersion++;
   }

   private Map<String, Integer> b() {
      if (this.enabledLookup == null || this.lookupVersion != this.currentVersion) {
         this.c();
      }

      return this.enabledLookup;
   }

   private void c() {
      HashMap var1 = new HashMap();

      for (net.agartha.client.module.a_pkg.a_o.a var3 : this.a()) {
         if (var3.enabled().get()) {
            var1.put(var3.id(), rgbaToArgb(var3.color()));
         }
      }

      this.enabledLookup = var1;
      this.lookupVersion = this.currentVersion;
   }

   public boolean isEnabled(Block block) {
      Identifier var2 = Registries.BLOCK.getId(block);
      return var2 == null ? false : this.b().containsKey(var2.toString());
   }

   public int colorArgb(Block block) {
      Identifier var2 = Registries.BLOCK.getId(block);
      if (var2 == null) {
         return rgbaToArgb(this.defaultColor);
      } else {
         Integer var3 = this.b().get(var2.toString());
         return var3 != null ? var3 : rgbaToArgb(this.defaultColor);
      }
   }

   public boolean hasAnyEnabled() {
      return !this.b().isEmpty();
   }

   @Override
   public void resetToDefault() {
      this.d();
      this.entries = a(this.defaultSnapshot);
      System.arraycopy(this.defaultColorBaseline, 0, this.defaultColor, 0, this.defaultColor.length);
      this.notifyChanged();
   }

   private void d() {
      if (this.defaultSnapshot == null) {
         this.defaultSnapshot = a(this.a());
      }
   }

   private static List<net.agartha.client.module.a_pkg.a_o.a> a(List<net.agartha.client.module.a_pkg.a_o.a> list) {
      ArrayList var1 = new ArrayList(list.size());

      for (net.agartha.client.module.a_pkg.a_o.a var3 : list) {
         var1.add(
            new net.agartha.client.module.a_pkg.a_o.a(
               var3.id(), var3.displayName(), var3.category(), new ImBoolean(var3.enabled().get()), (float[])var3.color().clone()
            )
         );
      }

      return var1;
   }

   @Override
   public JsonElement serialize() {
      this.d();
      JsonObject var1 = new JsonObject();
      var1.addProperty("sparse", true);
      if (!a(this.defaultColor, this.defaultColorBaseline)) {
         JsonArray var2 = new JsonArray();

         for (float var6 : this.defaultColor) {
            var2.add(a(var6));
         }

         var1.add("defaultColor", var2);
      }

      JsonArray var13 = new JsonArray();
      HashMap var14 = new HashMap();

      for (net.agartha.client.module.a_pkg.a_o.a var17 : this.defaultSnapshot) {
         var14.put(var17.id(), var17);
      }

      for (net.agartha.client.module.a_pkg.a_o.a var18 : this.a()) {
         net.agartha.client.module.a_pkg.a_o.a var19 = (net.agartha.client.module.a_pkg.a_o.a)var14.get(var18.id());
         if (var19 == null || !a(var18, var19)) {
            JsonObject var7 = new JsonObject();
            var7.addProperty("id", var18.id());
            var7.addProperty("enabled", var18.enabled().get());
            JsonArray var8 = new JsonArray();

            for (float var12 : var18.color()) {
               var8.add(a(var12));
            }

            var7.add("color", var8);
            var13.add(var7);
         }
      }

      var1.add("blocks", var13);
      return var1;
   }

   @Override
   protected void deserializeValue(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonObject()) {
         try {
            JsonObject var2 = jsonElement.getAsJsonObject();
            this.e();
            if (var2.has("defaultColor")) {
               JsonArray var3 = var2.getAsJsonArray("defaultColor");

               for (int var4 = 0; var4 < Math.min(4, var3.size()); var4++) {
                  this.defaultColor[var4] = a(var3.get(var4).getAsFloat());
               }
            }

            if (var2.has("blocks")) {
               for (JsonElement var5 : var2.getAsJsonArray("blocks")) {
                  if (var5.isJsonObject()) {
                     JsonObject var6 = var5.getAsJsonObject();
                     if (var6.has("id")) {
                        String var7 = var6.get("id").getAsString();

                        for (net.agartha.client.module.a_pkg.a_o.a var9 : this.a()) {
                           if (var9.id().equals(var7)) {
                              if (var6.has("enabled")) {
                                 var9.enabled().set(var6.get("enabled").getAsBoolean());
                              }

                              if (var6.has("color")) {
                                 JsonArray var10 = var6.getAsJsonArray("color");
                                 float[] var11 = var9.color();

                                 for (int var12 = 0; var12 < Math.min(4, var10.size()); var12++) {
                                    var11[var12] = a(var10.get(var12).getAsFloat());
                                 }
                              }
                              break;
                           }
                        }
                     }
                  }
               }
            }
         } catch (Exception var13) {
         }
      }
   }

   private void e() {
      this.d();
      this.entries = a(this.defaultSnapshot);
      System.arraycopy(this.defaultColorBaseline, 0, this.defaultColor, 0, this.defaultColor.length);
   }

   private static boolean a(net.agartha.client.module.a_pkg.a_o.a arg, net.agartha.client.module.a_pkg.a_o.a arg2) {
      return arg.enabled().get() == arg2.enabled().get() && a(arg.color(), arg2.color());
   }

   private static boolean a(float[] fs, float[] gs) {
      if (fs.length != gs.length) {
         return false;
      } else {
         for (int var2 = 0; var2 < fs.length; var2++) {
            if (Float.compare(a(fs[var2]), a(gs[var2])) != 0) {
               return false;
            }
         }

         return true;
      }
   }

   public static net.agartha.client.module.a_pkg.a_o createDefault() {
      return new net.agartha.client.module.a_pkg.a_o("Block Colors", net.agartha.client.module.a_pkg.a_o::f, new float[]{1.0F, 1.0F, 1.0F, 1.0F});
   }

   private static List<net.agartha.client.module.a_pkg.a_o.a> f() {
      ArrayList<a> var0 = new ArrayList<>();

      for (Block var2 : Registries.BLOCK) {
         if (!var2.getDefaultState().isAir()) {
            Identifier var3 = Registries.BLOCK.getId(var2);
            if (var3 != null) {
               String var4 = var3.toString();
               String var5 = var3.getPath();
               String[] var6 = var5.split("_");
               StringBuilder var7 = new StringBuilder();

               for (int var8 = 0; var8 < var6.length; var8++) {
                  if (!var6[var8].isEmpty()) {
                     var7.append(Character.toUpperCase(var6[var8].charAt(0)));
                     var7.append(var6[var8].substring(1));
                     if (var8 < var6.length - 1) {
                        var7.append(" ");
                     }
                  }
               }

               var0.add(a(var4, var7.toString(), "Blocks", false, 1.0F, 1.0F, 1.0F, 1.0F));
            }
         }
      }

      var0.sort((arg, arg2) -> arg.displayName().compareToIgnoreCase(arg2.displayName()));
      return var0;
   }

   public static net.agartha.client.module.a_pkg.a_o createForSeedOres() {
      return new net.agartha.client.module.a_pkg.a_o("Ores", net.agartha.client.module.a_pkg.a_o::g, new float[]{1.0F, 1.0F, 1.0F, 1.0F});
   }

   private static List<net.agartha.client.module.a_pkg.a_o.a> g() {
      ArrayList<a> var0 = new ArrayList<>();
      Object[][] var1 = new Object[][]{
         {"coal_ore", "Coal Ore", false, 40, 40, 40},
         {"deepslate_coal_ore", "Deepslate Coal Ore", true, 40, 40, 40},
         {"iron_ore", "Iron Ore", false, 216, 175, 147},
         {"deepslate_iron_ore", "Deepslate Iron Ore", false, 216, 175, 147},
         {"copper_ore", "Copper Ore", false, 224, 115, 77},
         {"deepslate_copper_ore", "Deepslate Copper Ore", false, 224, 115, 77},
         {"gold_ore", "Gold Ore", false, 255, 215, 0},
         {"deepslate_gold_ore", "Deepslate Gold Ore", false, 255, 215, 0},
         {"nether_gold_ore", "Nether Gold Ore", false, 255, 215, 0},
         {"redstone_ore", "Redstone Ore", false, 255, 30, 30},
         {"deepslate_redstone_ore", "Deepslate Redstone Ore", false, 255, 30, 30},
         {"lapis_ore", "Lapis Ore", false, 40, 80, 255},
         {"deepslate_lapis_ore", "Deepslate Lapis Ore", false, 40, 80, 255},
         {"diamond_ore", "Diamond Ore", true, 80, 240, 255},
         {"deepslate_diamond_ore", "Deepslate Diamond Ore", true, 80, 240, 255},
         {"emerald_ore", "Emerald Ore", false, 30, 230, 90},
         {"deepslate_emerald_ore", "Deepslate Emerald Ore", false, 30, 230, 90},
         {"nether_quartz_ore", "Nether Quartz Ore", false, 240, 235, 225},
         {"ancient_debris", "Ancient Debris", false, 130, 80, 60}
      };

      for (Object[] var5 : var1) {
         var0.add(
            a(
               "minecraft:" + var5[0],
               (String)var5[1],
               "Ores",
               (Boolean)var5[2],
               ((Integer)var5[3]).intValue() / 255.0F,
               ((Integer)var5[4]).intValue() / 255.0F,
               ((Integer)var5[5]).intValue() / 255.0F,
               1.0F
            )
         );
      }

      return var0;
   }

   public static net.agartha.client.module.a_pkg.a_o createForBlockEntityDebug() {
      return new net.agartha.client.module.a_pkg.a_o("Block Colors", net.agartha.client.module.a_pkg.a_o::h, new float[]{1.0F, 1.0F, 1.0F, 1.0F});
   }

   private static List<net.agartha.client.module.a_pkg.a_o.a> h() {
      ArrayList<a> var0 = new ArrayList<>();

      for (Block var2 : Registries.BLOCK) {
         if (!var2.getDefaultState().isAir()) {
            Identifier var3 = Registries.BLOCK.getId(var2);
            if (var3 != null) {
               String var4 = var3.toString();
               String var5 = var3.getPath();
               String[] var6 = var5.split("_");
               StringBuilder var7 = new StringBuilder();

               for (int var8 = 0; var8 < var6.length; var8++) {
                  if (!var6[var8].isEmpty()) {
                     var7.append(Character.toUpperCase(var6[var8].charAt(0)));
                     var7.append(var6[var8].substring(1));
                     if (var8 < var6.length - 1) {
                        var7.append(" ");
                     }
                  }
               }

               boolean var12 = false;
               float var9 = 1.0F;
               float var10 = 1.0F;
               float var11 = 1.0F;
               if (var5.equals("spawner")) {
                  var12 = true;
                  var9 = 0.0F;
                  var10 = 0.5F;
                  var11 = 1.0F;
               } else if (var5.equals("moving_piston")) {
                  var12 = true;
                  var9 = 1.0F;
                  var10 = 1.0F;
                  var11 = 0.0F;
               } else if (var5.contains("shulker_box")) {
                  var12 = true;
                  var9 = 0.8F;
                  var10 = 0.0F;
                  var11 = 0.8F;
               } else if (var5.contains("chest") || var5.equals("barrel")) {
                  var12 = true;
                  var9 = 1.0F;
                  var10 = 0.5F;
                  var11 = 0.0F;
               } else if (var5.contains("sign")) {
                  var12 = true;
                  var9 = 0.6F;
                  var10 = 0.3F;
                  var11 = 0.0F;
               } else if (var5.equals("furnace")
                  || var5.equals("blast_furnace")
                  || var5.equals("smoker")
                  || var5.equals("hopper")
                  || var5.equals("dropper")
                  || var5.equals("dispenser")
                  || var5.equals("brewing_stand")
                  || var5.equals("enchanting_table")
                  || var5.equals("beacon")
                  || var5.endsWith("_bed")
                  || var5.endsWith("_banner")
                  || var5.endsWith("_skull")
                  || var5.endsWith("_head")
                  || var5.equals("lectern")
                  || var5.equals("bell")
                  || var5.equals("campfire")
                  || var5.equals("soul_campfire")
                  || var5.equals("beehive")
                  || var5.equals("bee_nest")
                  || var5.equals("conduit")
                  || var5.equals("jigsaw")
                  || var5.equals("structure_block")
                  || var5.equals("command_block")
                  || var5.equals("chain_command_block")
                  || var5.equals("repeating_command_block")
                  || var5.equals("end_portal")
                  || var5.equals("end_gateway")
                  || var5.equals("sculk_sensor")
                  || var5.equals("calibrated_sculk_sensor")
                  || var5.equals("sculk_catalyst")
                  || var5.equals("sculk_shrieker")
                  || var5.equals("decorated_pot")
                  || var5.equals("suspicious_sand")
                  || var5.equals("suspicious_gravel")
                  || var5.equals("trial_spawner")
                  || var5.equals("vault")
                  || var5.equals("crafter")
                  || var5.equals("chiseled_bookshelf")
                  || var5.equals("daylight_detector")
                  || var5.equals("comparator")) {
                  var12 = true;
                  var9 = 0.35F;
                  var10 = 1.0F;
                  var11 = 0.45F;
               }

               var0.add(a(var4, var7.toString(), "Blocks", var12, var9, var10, var11, 1.0F));
            }
         }
      }

      var0.sort((arg, arg2) -> arg.displayName().compareToIgnoreCase(arg2.displayName()));
      return var0;
   }

   private static net.agartha.client.module.a_pkg.a_o.a a(String string, String string2, String string3, boolean bl, float f, float g, float h, float i) {
      float[] var8 = new float[]{f, g, h, i};
      return new net.agartha.client.module.a_pkg.a_o.a(string, string2, string3, new ImBoolean(bl), var8);
   }

   public static int rgbaToArgb(float[] fs) {
      int var1 = Math.round(a(fs[0]) * 255.0F);
      int var2 = Math.round(a(fs[1]) * 255.0F);
      int var3 = Math.round(a(fs[2]) * 255.0F);
      int var4 = Math.round(a(fs[3]) * 255.0F);
      return var4 << 24 | var1 << 16 | var2 << 8 | var3;
   }

   private static float a(float f) {
      return !Float.isNaN(f) && !Float.isInfinite(f) ? Math.max(0.0F, Math.min(1.0F, f)) : 1.0F;
   }

   @Environment(EnvType.CLIENT)
   public record a(String id, String displayName, String category, ImBoolean enabled, float[] color) {
   }
}
