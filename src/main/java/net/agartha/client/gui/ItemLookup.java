package net.agartha.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class ItemLookup {
   private static List<ItemLookup.a> rows;
   private static final Map<String, Item> ITEMS = new HashMap<>();
   private static final Map<String, Item> BLOCKS = new HashMap<>();
   private static final Map<String, Item> EGGS = new HashMap<>();
   private static final Map<Item, String> NAMES = new IdentityHashMap<>();

   private ItemLookup() {
   }

   static List<ItemLookup.a> rows() {
      if (rows == null) {
         ArrayList<ItemLookup.a> var0 = new ArrayList<>();

         for (Item var2 : Registries.ITEM) {
            if (var2 != Items.AIR) {
               Identifier var3 = Registries.ITEM.getId(var2);
               String var4 = var3.toString();
               String var5 = var2.getName().getString();
               var0.add(new ItemLookup.a(var4, var5, (var5 + " " + var3.getPath()).toLowerCase(Locale.ROOT), var2));
            }
         }

         var0.sort((arg, arg2) -> arg.name().compareToIgnoreCase(arg2.name()));
         rows = Collections.unmodifiableList(var0);
      }

      return rows;
   }

   static String name(Item item) {
      return NAMES.computeIfAbsent(item, itemx -> itemx.getName().getString());
   }

   static Item item(String string) {
      Item var1 = ITEMS.computeIfAbsent(string, stringx -> {
         Identifier var1x = Identifier.tryParse(stringx);
         return var1x == null ? Items.AIR : Registries.ITEM.getOptionalValue(var1x).orElse(Items.AIR);
      });
      return var1 == Items.AIR ? null : var1;
   }

   static Item blockIcon(String string) {
      Item var1 = BLOCKS.computeIfAbsent(string, stringx -> {
         Identifier var1x = Identifier.tryParse(stringx);
         return var1x == null ? Items.AIR : Registries.BLOCK.getOptionalValue(var1x).map(block -> block.asItem()).orElse(Items.AIR);
      });
      return var1 == Items.AIR ? null : var1;
   }

   static Item mobIcon(String string) {
      Item var1 = EGGS.computeIfAbsent(string, stringx -> {
         Identifier var1x = Identifier.tryParse(stringx);
         return var1x == null ? Items.AIR : Registries.ENTITY_TYPE.getOptionalValue(var1x).map(SpawnEggItem::forEntity).map(item -> (Item)item).orElse(Items.AIR);
      });
      return var1 == Items.AIR ? null : var1;
   }

   @Environment(EnvType.CLIENT)
   record a(String id, String name, String search, Item item) {
   }
}
