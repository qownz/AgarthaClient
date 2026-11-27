package net.agartha.client.module.a_pkg;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class i extends k {
   private final List<String> modes;
   private final int defaultIndex;
   private int selectedIndex;
   private Map<String, String> aliases = Map.of();

   public i(String string, String string2, String... strings) {
      super(string);
      this.modes = List.copyOf(Arrays.asList(strings));
      this.defaultIndex = Math.max(0, this.modes.indexOf(string2));
      this.selectedIndex = this.defaultIndex;
   }

   public String get() {
      return this.modes.get(this.selectedIndex);
   }

   public boolean is(String string) {
      return this.get().equals(string);
   }

   public i alias(String string, String string2) {
      HashMap var3 = new HashMap<>(this.aliases);
      var3.put(string, string2);
      this.aliases = Map.copyOf(var3);
      return this;
   }

   private int a(String string) {
      int var2 = this.modes.indexOf(string);
      return var2 >= 0 ? var2 : this.modes.indexOf(this.aliases.getOrDefault(string, ""));
   }

   public void set(String string) {
      int var2 = this.a(string);
      if (var2 >= 0 && var2 != this.selectedIndex) {
         this.selectedIndex = var2;
         this.notifyChanged();
      }
   }

   public String value() {
      return this.get();
   }

   public List<String> modes() {
      return this.modes;
   }

   public void next() {
      this.selectedIndex = (this.selectedIndex + 1) % this.modes.size();
      this.notifyChanged();
   }

   @Override
   public void resetToDefault() {
      this.selectedIndex = this.defaultIndex;
   }

   @Override
   public JsonElement serialize() {
      return new JsonPrimitive(this.get());
   }

   @Override
   protected void deserializeValue(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonPrimitive()) {
         try {
            int var2 = this.a(jsonElement.getAsString());
            if (var2 >= 0) {
               this.selectedIndex = var2;
            }
         } catch (RuntimeException var3) {
         }
      }
   }
}
