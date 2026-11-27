package net.agartha.client.module.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import imgui.type.ImBoolean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class EntityTypeListSetting extends Setting {
   private List<EntityTypeListSetting.a> entries;
   private boolean built;
   private Set<EntityType<?>> disabled = Collections.emptySet();

   public EntityTypeListSetting(String string) {
      super(string);
      this.onChange(this::b);
   }

   public List<EntityTypeListSetting.a> entries() {
      this.a();
      return this.entries;
   }

   public boolean allows(EntityType<?> entityType) {
      return entityType != null && !this.disabled.contains(entityType);
   }

   public String summary() {
      if (!this.built) {
         return "All";
      } else {
         int var1 = 0;

         for (EntityTypeListSetting.a var3 : this.entries) {
            if (var3.enabled.get()) {
               var1++;
            }
         }

         return var1 + " on";
      }
   }

   private void a() {
      if (!this.built) {
         ArrayList var1 = new ArrayList();

         try {
            for (EntityType var3 : Registries.ENTITY_TYPE) {
               if (a(var3)) {
                  Identifier var4 = Registries.ENTITY_TYPE.getId(var3);
                  if (var4 != null) {
                     var1.add(new EntityTypeListSetting.a(var4.toString(), a(var4.getPath()), new ImBoolean(true)));
                  }
               }
            }
         } catch (Throwable var5) {
            return;
         }

         var1.sort(Comparator.comparing(EntityTypeListSetting.a::name));
         this.entries = var1;
         this.built = true;
      }
   }

   private static boolean a(EntityType<?> entityType) {
      if (entityType == EntityType.PLAYER) {
         return false;
      } else {
         SpawnGroup var1 = entityType.getSpawnGroup();
         return var1 != SpawnGroup.MISC
            ? true
            : entityType == EntityType.VILLAGER
               || entityType == EntityType.WANDERING_TRADER
               || entityType == EntityType.IRON_GOLEM
               || entityType == EntityType.SNOW_GOLEM
               || entityType == EntityType.ARMOR_STAND;
      }
   }

   private static String a(String string) {
      String[] var1 = string.split("_");
      StringBuilder var2 = new StringBuilder();

      for (String var6 : var1) {
         if (!var6.isEmpty()) {
            if (var2.length() > 0) {
               var2.append(' ');
            }

            var2.append(Character.toUpperCase(var6.charAt(0)));
            if (var6.length() > 1) {
               var2.append(var6.substring(1));
            }
         }
      }

      return var2.toString();
   }

   private static Identifier b(String string) {
      int var1 = string.indexOf(58);
      if (var1 > 0 && var1 < string.length() - 1) {
         try {
            return Identifier.of(string.substring(0, var1), string.substring(var1 + 1));
         } catch (RuntimeException var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   private void b() {
      if (!this.built) {
         this.disabled = Collections.emptySet();
      } else {
         Set var1 = Collections.newSetFromMap(new IdentityHashMap());

         for (EntityTypeListSetting.a var3 : this.entries) {
            if (!var3.enabled.get()) {
               Identifier var4 = b(var3.id);
               if (var4 != null) {
                  Registries.ENTITY_TYPE.getOptionalValue(var4).ifPresent(var1::add);
               }
            }
         }

         this.disabled = var1;
      }
   }

   @Override
   public void resetToDefault() {
      if (this.built) {
         for (EntityTypeListSetting.a var2 : this.entries) {
            var2.enabled.set(true);
         }
      }

      this.disabled = Collections.emptySet();
   }

   @Override
   public JsonElement serialize() {
      JsonArray var1 = new JsonArray();
      if (this.built) {
         for (EntityTypeListSetting.a var3 : this.entries) {
            if (!var3.enabled.get()) {
               var1.add(var3.id);
            }
         }
      }

      return var1;
   }

   @Override
   protected void deserializeValue(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonArray()) {
         this.a();
         if (this.built) {
            HashSet var2 = new HashSet();

            for (JsonElement var4 : jsonElement.getAsJsonArray()) {
               if (var4.isJsonPrimitive()) {
                  var2.add(var4.getAsString().toLowerCase(Locale.ROOT));
               }
            }

            for (EntityTypeListSetting.a var6 : this.entries) {
               var6.enabled.set(!var2.contains(var6.id.toLowerCase(Locale.ROOT)));
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public record a(String id, String name, ImBoolean enabled) {
   }
}
