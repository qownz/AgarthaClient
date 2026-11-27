package net.agartha.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import imgui.type.ImString;
import net.agartha.client.module.setting.Setting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class StringSetting extends Setting {
   private final ImString imString;
   private final String defaultValue;

   public StringSetting(String string, String string2) {
      this(string, string2, 512);
   }

   public StringSetting(String string, String string2, int i) {
      super(string);
      this.defaultValue = string2;
      this.imString = new ImString(string2, Math.max(512, i));
   }

   public String get() {
      return this.imString.get();
   }

   public void set(String string) {
      if (!this.imString.get().equals(string)) {
         this.imString.set(string);
         this.notifyChanged();
      }
   }

   public String getValue() {
      return this.get();
   }

   public ImString getImString() {
      return this.imString;
   }

   @Override
   public void resetToDefault() {
      this.imString.set(this.defaultValue);
   }

   @Override
   public JsonElement serialize() {
      return new JsonPrimitive(this.imString.get());
   }

   @Override
   protected void deserializeValue(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonPrimitive()) {
         try {
            this.imString.set(jsonElement.getAsString());
         } catch (RuntimeException var3) {
         }
      }
   }
}
