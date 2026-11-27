package net.agartha.client.module.a_pkg;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import imgui.type.ImBoolean;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class b extends k {
   private final ImBoolean value;
   private final boolean defaultValue;

   public b(String string, boolean bl) {
      super(string);
      this.defaultValue = bl;
      this.value = new ImBoolean(bl);
   }

   public boolean get() {
      return this.value.get();
   }

   public void set(boolean bl) {
      if (this.value.get() != bl) {
         this.value.set(bl);
         this.notifyChanged();
      }
   }

   public ImBoolean value() {
      return this.value;
   }

   @Override
   public void resetToDefault() {
      this.value.set(this.defaultValue);
   }

   @Override
   public JsonElement serialize() {
      return new JsonPrimitive(this.value.get());
   }

   @Override
   protected void deserializeValue(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonPrimitive()) {
         try {
            this.value.set(jsonElement.getAsBoolean());
         } catch (RuntimeException var3) {
         }
      }
   }
}
