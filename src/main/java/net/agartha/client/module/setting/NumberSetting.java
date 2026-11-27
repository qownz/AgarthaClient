package net.agartha.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Locale;
import java.util.function.IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class NumberSetting extends Setting {
   private final float[] value;
   private final float defaultValue;
   private final float min;
   private final float max;
   private boolean whole;
   private IntFunction<String> label;

   public NumberSetting(String string, float f, float g, float h) {
      super(string);
      this.defaultValue = f;
      this.value = new float[]{f};
      this.min = g;
      this.max = h;
   }

   public NumberSetting whole() {
      this.whole = true;
      this.value[0] = this.a(this.value[0]);
      return this;
   }

   public NumberSetting label(IntFunction<String> intFunction) {
      this.label = intFunction;
      return this.whole();
   }

   public boolean isWhole() {
      return this.whole;
   }

   public String display() {
      if (this.label != null) {
         return this.label.apply(this.getInt());
      } else {
         return this.whole ? Integer.toString(this.getInt()) : String.format(Locale.ROOT, "%.1f", this.value[0]);
      }
   }

   public String editText() {
      return this.whole ? Integer.toString(this.getInt()) : String.format(Locale.ROOT, "%.1f", this.value[0]);
   }

   public void settle() {
      this.value[0] = this.a(this.value[0]);
   }

   public float get() {
      return this.value[0];
   }

   public int getInt() {
      return Math.round(this.value[0]);
   }

   public void set(float f) {
      float var2 = this.a(f);
      if (this.value[0] != var2) {
         this.value[0] = var2;
         this.notifyChanged();
      }
   }

   public float[] value() {
      return this.value;
   }

   public float min() {
      return this.min;
   }

   public float max() {
      return this.max;
   }

   private float a(float f) {
      if (Float.isNaN(f)) {
         return this.value[0];
      } else {
         if (this.whole) {
            f = Math.round(f);
         }

         if (f < this.min) {
            return this.min;
         } else {
            return f > this.max ? this.max : f;
         }
      }
   }

   @Override
   public void resetToDefault() {
      this.value[0] = this.defaultValue;
   }

   @Override
   public JsonElement serialize() {
      return new JsonPrimitive(this.value[0]);
   }

   @Override
   protected void deserializeValue(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonPrimitive()) {
         try {
            this.value[0] = this.a(jsonElement.getAsFloat());
         } catch (RuntimeException var3) {
         }
      }
   }
}
