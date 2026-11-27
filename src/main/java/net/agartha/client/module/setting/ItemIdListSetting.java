package net.agartha.client.module.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class ItemIdListSetting extends Setting {
   private final List<String> defaults;
   private final LinkedHashSet<String> selected = new LinkedHashSet<>();
   private Set<Item> items;

   public ItemIdListSetting(String string, String... strings) {
      super(string);
      ArrayList var3 = new ArrayList();

      for (String var7 : strings) {
         var3.add(normalize(var7));
      }

      this.defaults = List.copyOf(var3);
      this.selected.addAll(this.defaults);
      this.onChange(() -> this.items = null);
   }

   public Set<String> selectedIds() {
      return Collections.unmodifiableSet(this.selected);
   }

   public boolean isSelected(String string) {
      return this.selected.contains(string);
   }

   public boolean contains(Item item) {
      if (item != null && item != Items.AIR) {
         if (this.items == null) {
            Set var2 = Collections.newSetFromMap(new IdentityHashMap());

            for (String var4 : this.selected) {
               Identifier var5 = Identifier.tryParse(var4);
               if (var5 != null) {
                  Registries.ITEM.getOptionalValue(var5).ifPresent(var2::add);
               }
            }

            this.items = var2;
         }

         return this.items.contains(item);
      } else {
         return false;
      }
   }

   public void toggle(String string) {
      String var2 = normalize(string);
      if (!this.selected.remove(var2)) {
         this.selected.add(var2);
      }

      this.notifyChanged();
   }

   public void clear() {
      this.selected.clear();
      this.notifyChanged();
   }

   public int size() {
      return this.selected.size();
   }

   @Override
   public void resetToDefault() {
      this.selected.clear();
      this.selected.addAll(this.defaults);
      this.notifyChanged();
   }

   @Override
   public JsonElement serialize() {
      JsonArray var1 = new JsonArray();

      for (String var3 : this.selected) {
         var1.add(var3);
      }

      return var1;
   }

   @Override
   protected void deserializeValue(JsonElement jsonElement) {
      if (jsonElement != null) {
         try {
            LinkedHashSet var2 = new LinkedHashSet();
            if (jsonElement.isJsonArray()) {
               for (JsonElement var9 : jsonElement.getAsJsonArray()) {
                  if (var9.isJsonPrimitive()) {
                     a(var2, var9.getAsString());
                  }
               }
            } else {
               if (!jsonElement.isJsonPrimitive()) {
                  return;
               }

               for (String var6 : jsonElement.getAsString().split(",")) {
                  a(var2, var6);
               }
            }

            this.selected.clear();
            this.selected.addAll(var2);
         } catch (RuntimeException var7) {
         }
      }
   }

   private static void a(Set<String> set, String string) {
      String var2 = normalize(string);
      if (!var2.isEmpty() && Identifier.tryParse(var2) != null) {
         set.add(var2);
      }
   }

   static String normalize(String string) {
      if (string == null) {
         return "";
      } else {
         String var1 = string.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
         if (var1.isEmpty()) {
            return "";
         } else {
            return var1.indexOf(58) < 0 ? "minecraft:" + var1 : var1;
         }
      }
   }
}
