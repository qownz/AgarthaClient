package net.agartha.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.agartha.client.input.Keybind;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class KeybindSetting extends Setting {
   private final int defaultValue;
   private int value;

   public KeybindSetting(String string, int i) {
      super(string);
      this.defaultValue = i;
      this.value = i;
   }

   public int value() {
      return this.value;
   }

   public void setValue(int i) {
      if (Keybind.isClickButton(i)) {
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
            this.value = Keybind.isClickButton(var2) ? this.defaultValue : var2;
         } catch (RuntimeException var3) {
         }
      }
   }
}
