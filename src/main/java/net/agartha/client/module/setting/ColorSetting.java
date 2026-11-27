package net.agartha.client.module.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ColorSetting extends Setting {
   private final float[] value;
   private final float[] defaultValue;

   public ColorSetting(String string, int i) {
      super(string);
      this.value = new float[]{(i >> 16 & 0xFF) / 255.0F, (i >> 8 & 0xFF) / 255.0F, (i & 0xFF) / 255.0F, (i >>> 24 & 0xFF) / 255.0F};
      this.defaultValue = (float[])this.value.clone();
   }

   public float[] value() {
      return this.value;
   }

   public void setArgb(int i) {
      if (this.argb() != i) {
         this.a(i);
         this.notifyChanged();
      }
   }

   public int argb() {
      return this.argbWithAlpha(Math.round(a(this.value[3]) * 255.0F));
   }

   public int rgb() {
      return this.argb() & 16777215;
   }

   public int argbWithAlpha(int i) {
      int var2 = b(i);
      int var3 = Math.round(a(this.value[0]) * 255.0F);
      int var4 = Math.round(a(this.value[1]) * 255.0F);
      int var5 = Math.round(a(this.value[2]) * 255.0F);
      return var2 << 24 | var3 << 16 | var4 << 8 | var5;
   }

   @Override
   public void resetToDefault() {
      System.arraycopy(this.defaultValue, 0, this.value, 0, this.value.length);
   }

   @Override
   public JsonElement serialize() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("r", a(this.value[0]));
      var1.addProperty("g", a(this.value[1]));
      var1.addProperty("b", a(this.value[2]));
      var1.addProperty("a", a(this.value[3]));
      return var1;
   }

   @Override
   protected void deserializeValue(JsonElement jsonElement) {
      if (jsonElement != null) {
         try {
            if (jsonElement.isJsonObject()) {
               JsonObject var6 = jsonElement.getAsJsonObject();
               this.value[0] = a(var6, "r", this.value[0]);
               this.value[1] = a(var6, "g", this.value[1]);
               this.value[2] = a(var6, "b", this.value[2]);
               this.value[3] = a(var6, "a", this.value[3]);
               this.a();
               return;
            }

            if (jsonElement.isJsonArray()) {
               JsonArray var5 = jsonElement.getAsJsonArray();

               for (int var3 = 0; var3 < Math.min(4, var5.size()); var3++) {
                  this.value[var3] = a(var5.get(var3).getAsFloat());
               }

               return;
            }

            if (jsonElement.isJsonPrimitive()) {
               JsonPrimitive var2 = jsonElement.getAsJsonPrimitive();
               if (var2.isString()) {
                  this.a(var2.getAsString());
               } else if (var2.isNumber()) {
                  this.a(var2.getAsInt());
               }
            }
         } catch (RuntimeException var4) {
         }
      }
   }

   private void a(String string) {
      String var2 = string.trim();
      if (var2.startsWith("#")) {
         var2 = var2.substring(1);
      }

      if (var2.length() == 6) {
         var2 = "FF" + var2;
      }

      if (var2.length() == 8) {
         this.a((int)Long.parseLong(var2, 16));
      }
   }

   private void a(int i) {
      this.value[0] = (i >> 16 & 0xFF) / 255.0F;
      this.value[1] = (i >> 8 & 0xFF) / 255.0F;
      this.value[2] = (i & 0xFF) / 255.0F;
      this.value[3] = (i >>> 24 & 0xFF) / 255.0F;
   }

   private void a() {
      for (int var1 = 0; var1 < this.value.length; var1++) {
         this.value[var1] = a(this.value[var1]);
      }
   }

   private static float a(JsonObject jsonObject, String string, float f) {
      return !jsonObject.has(string) ? f : a(jsonObject.get(string).getAsFloat());
   }

   private static float a(float f) {
      return !Float.isNaN(f) && !Float.isInfinite(f) ? Math.max(0.0F, Math.min(1.0F, f)) : 1.0F;
   }

   private static int b(int i) {
      return Math.max(0, Math.min(255, i));
   }
}
