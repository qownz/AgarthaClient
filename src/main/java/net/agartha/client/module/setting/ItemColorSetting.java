package net.agartha.client.module.setting;

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
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class ItemColorSetting extends Setting {
   private List<ItemColorSetting.a> entries;
   private Supplier<List<ItemColorSetting.a>> entriesSupplier;
   private final float[] defaultColor;
   private List<ItemColorSetting.a> defaultSnapshot;
   private final float[] defaultColorBaseline;
   private Map<String, Integer> enabledLookup;

   public ItemColorSetting(String string, List<ItemColorSetting.a> list, float[] fs) {
      super(string);
      this.entries = new ArrayList<>(list);
      this.defaultColor = fs;
      this.defaultColorBaseline = (float[])fs.clone();
      this.defaultSnapshot = a(this.entries);
      this.onChange(this::invalidateLookup);
   }

   public ItemColorSetting(String string, Supplier<List<ItemColorSetting.a>> supplier, float[] fs) {
      super(string);
      this.entriesSupplier = supplier;
      this.defaultColor = fs;
      this.defaultColorBaseline = (float[])fs.clone();
      this.onChange(this::invalidateLookup);
   }

   private List<ItemColorSetting.a> a() {
      if (this.entries == null) {
         this.entries = new ArrayList<>(this.entriesSupplier.get());
         this.entriesSupplier = null;
         this.defaultSnapshot = a(this.entries);
      }

      return this.entries;
   }

   public List<ItemColorSetting.a> entries() {
      return Collections.unmodifiableList(this.a());
   }

   public float[] defaultColor() {
      return this.defaultColor;
   }

   public void resetColorToDefault(ItemColorSetting.a arg) {
      this.d();
      if (arg == null) {
         System.arraycopy(this.defaultColorBaseline, 0, this.defaultColor, 0, this.defaultColor.length);
      } else {
         for (ItemColorSetting.a var3 : this.defaultSnapshot) {
            if (var3.id().equals(arg.id())) {
               System.arraycopy(var3.color(), 0, arg.color(), 0, arg.color().length);
               break;
            }
         }
      }

      this.notifyChanged();
   }

   public void invalidateLookup() {
      this.enabledLookup = null;
   }

   private Map<String, Integer> b() {
      if (this.enabledLookup == null) {
         this.c();
      }

      return this.enabledLookup;
   }

   private void c() {
      HashMap var1 = new HashMap();

      for (ItemColorSetting.a var3 : this.a()) {
         if (var3.enabled().get()) {
            var1.put(var3.id(), rgbaToArgb(var3.color()));
         }
      }

      this.enabledLookup = var1;
   }

   public boolean isEnabled(Item item) {
      Identifier var2 = Registries.ITEM.getId(item);
      return var2 == null ? false : this.b().containsKey(var2.toString());
   }

   public int colorArgb(Item item) {
      Identifier var2 = Registries.ITEM.getId(item);
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

   private static List<ItemColorSetting.a> a(List<ItemColorSetting.a> list) {
      ArrayList var1 = new ArrayList(list.size());

      for (ItemColorSetting.a var3 : list) {
         var1.add(new ItemColorSetting.a(var3.id(), var3.displayName(), new ImBoolean(var3.enabled().get()), (float[])var3.color().clone()));
      }

      return var1;
   }

   @Override
   public JsonElement serialize() {
      JsonObject var1 = new JsonObject();
      JsonArray var2 = new JsonArray();

      for (float var6 : this.defaultColor) {
         var2.add(a(var6));
      }

      var1.add("defaultColor", var2);
      JsonArray var12 = new JsonArray();

      for (ItemColorSetting.a var14 : this.a()) {
         JsonObject var15 = new JsonObject();
         var15.addProperty("id", var14.id());
         var15.addProperty("enabled", var14.enabled().get());
         JsonArray var7 = new JsonArray();

         for (float var11 : var14.color()) {
            var7.add(a(var11));
         }

         var15.add("color", var7);
         var12.add(var15);
      }

      var1.add("items", var12);
      return var1;
   }

   @Override
   protected void deserializeValue(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonObject()) {
         try {
            JsonObject var2 = jsonElement.getAsJsonObject();
            if (var2.has("defaultColor")) {
               JsonArray var3 = var2.getAsJsonArray("defaultColor");

               for (int var4 = 0; var4 < Math.min(4, var3.size()); var4++) {
                  this.defaultColor[var4] = a(var3.get(var4).getAsFloat());
               }
            }

            if (var2.has("items")) {
               for (JsonElement var5 : var2.getAsJsonArray("items")) {
                  if (var5.isJsonObject()) {
                     JsonObject var6 = var5.getAsJsonObject();
                     if (var6.has("id")) {
                        String var7 = var6.get("id").getAsString();

                        for (ItemColorSetting.a var9 : this.a()) {
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

   public static ItemColorSetting createDefault() {
      return new ItemColorSetting("Item Colors", ItemColorSetting::e, new float[]{1.0F, 1.0F, 1.0F, 0.8F});
   }

   private static List<ItemColorSetting.a> e() {
      ArrayList<a> var0 = new ArrayList<>();

      for (Item var2 : Registries.ITEM) {
         Identifier var3 = Registries.ITEM.getId(var2);
         if (var3 != null) {
            String var4 = var3.toString();
            String var5 = var3.getPath();
            String var6 = a(var5);
            float[] var7 = c(var5);
            boolean var8 = b(var5);
            var0.add(a(var4, var6, var8, var7));
         }
      }

      var0.sort((arg, arg2) -> arg.displayName().compareToIgnoreCase(arg2.displayName()));
      return var0;
   }

   private static String a(String string) {
      String[] var1 = string.split("_");
      StringBuilder var2 = new StringBuilder();

      for (int var3 = 0; var3 < var1.length; var3++) {
         if (!var1[var3].isEmpty()) {
            var2.append(Character.toUpperCase(var1[var3].charAt(0)));
            var2.append(var1[var3].substring(1));
            if (var3 < var1.length - 1) {
               var2.append(" ");
            }
         }
      }

      return var2.toString();
   }

   private static boolean b(String string) {
      return switch (string) {
         case "diamond", "emerald", "netherite_ingot", "ancient_debris", "elytra", "totem_of_undying", "trident", "enchanted_golden_apple", "golden_apple", "nether_star", "heart_of_the_sea", "end_crystal", "shulker_box" -> true;
         case "gold_ingot", "gold_block", "gold_nugget" -> true;
         case "iron_ingot" -> true;
         default -> false;
      };
   }

   private static float[] c(String string) {
      return switch (string) {
         case "diamond", "diamond_sword", "diamond_pickaxe", "diamond_axe", "diamond_shovel", "diamond_hoe", "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots" -> a(
            0, 255, 255
         );
         case "emerald" -> a(0, 255, 68);
         case "netherite_ingot", "ancient_debris", "netherite_sword", "netherite_pickaxe", "netherite_axe", "netherite_shovel", "netherite_hoe", "netherite_helmet", "netherite_chestplate", "netherite_leggings", "netherite_boots" -> a(
            128, 96, 144
         );
         case "gold_ingot", "gold_block", "gold_nugget", "golden_sword", "golden_pickaxe", "golden_axe", "golden_shovel", "golden_hoe", "golden_helmet", "golden_chestplate", "golden_leggings", "golden_boots", "golden_apple", "enchanted_golden_apple" -> a(
            255, 215, 0
         );
         case "iron_ingot" -> a(204, 204, 204);
         case "elytra" -> a(85, 85, 255);
         case "totem_of_undying" -> a(255, 170, 0);
         case "trident" -> a(0, 136, 255);
         case "nether_star" -> a(255, 255, 0);
         case "heart_of_the_sea" -> a(0, 255, 221);
         case "end_crystal" -> a(255, 0, 255);
         case "shulker_box" -> a(204, 68, 204);
         default -> a(255, 255, 255);
      };
   }

   private static float[] a(int i, int j, int k) {
      return new float[]{i / 255.0F, j / 255.0F, k / 255.0F, 1.0F};
   }

   private static ItemColorSetting.a a(String string, String string2, boolean bl, float[] fs) {
      return new ItemColorSetting.a(string, string2, new ImBoolean(bl), fs);
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
   public record a(String id, String displayName, ImBoolean enabled, float[] color) {
   }
}
