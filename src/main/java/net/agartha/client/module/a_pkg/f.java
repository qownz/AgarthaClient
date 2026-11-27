package net.agartha.client.module.a_pkg;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class f extends k {
   private final String defaultId;
   private String id;

   public f(String string, String string2) {
      super(string);
      this.defaultId = e.normalize(string2);
      this.id = this.defaultId;
   }

   public String id() {
      return this.id;
   }

   public Item item() {
      Identifier var1 = Identifier.tryParse(this.id);
      return var1 == null ? Items.AIR : Registries.ITEM.getOptionalValue(var1).orElse(Items.AIR);
   }

   public void set(String string) {
      String var2 = e.normalize(string);
      if (!var2.isEmpty() && !var2.equals(this.id)) {
         this.id = var2;
         this.notifyChanged();
      }
   }

   @Override
   public void resetToDefault() {
      this.id = this.defaultId;
      this.notifyChanged();
   }

   @Override
   public JsonElement serialize() {
      return new JsonPrimitive(this.id);
   }

   @Override
   protected void deserializeValue(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonPrimitive()) {
         String var2 = e.normalize(jsonElement.getAsString());
         if (!var2.isEmpty() && Identifier.tryParse(var2) != null) {
            this.id = var2;
         }
      }
   }
}
