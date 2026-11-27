package net.agartha.client.module.a_pkg;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class g extends k {
   private final int defaultValue;
   private int value;

   public g(String string, int i) {
      super(string);
      this.defaultValue = i;
      this.value = i;
   }

   public int value() {
      return this.value;
   }

   public void setValue(int i) {
      if (net.agartha.client.j.e.isClickButton(i)) {
         i = this.defaultValue;
      }

      if (this.value != i) {
         this.value = i;
         this.notifyChanged();
      }
   }

   @Override
   public void resetToDefault() {
      this.value = this.defaultValue;
   }

   @Override
   public JsonElement serialize() {
      return new JsonPrimitive(this.value);
   }

   @Override
   protected void deserializeValue(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonPrimitive()) {
         try {
            int var2 = jsonElement.getAsInt();
            this.value = net.agartha.client.j.e.isClickButton(var2) ? this.defaultValue : var2;
         } catch (RuntimeException var3) {
         }
      }
   }
}
