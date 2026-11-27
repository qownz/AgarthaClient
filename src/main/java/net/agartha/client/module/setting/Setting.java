package net.agartha.client.module.setting;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class Setting {
   private final String name;
   private final List<Runnable> changeListeners = new ArrayList<>(1);
   private BooleanSupplier visibility;
   private boolean sessionOnly;
   private String[] aliases = new String[0];

   protected Setting(String string) {
      this.name = string;
   }

   public final String name() {
      return this.name;
   }

   public final Setting onChange(Runnable runnable) {
      this.changeListeners.add(runnable);
      return this;
   }

   public final void notifyChanged() {
      for (Runnable var2 : this.changeListeners) {
         try {
            var2.run();
         } catch (Throwable var4) {
            var4.printStackTrace();
         }
      }
   }

   public final Setting visibleWhen(BooleanSupplier booleanSupplier) {
      this.visibility = booleanSupplier;
      return this;
   }

   public final boolean isVisible() {
      return this.visibility == null || this.visibility.getAsBoolean();
   }

   public final Setting sessionOnly() {
      this.sessionOnly = true;
      return this;
   }

   public final boolean isSessionOnly() {
      return this.sessionOnly;
   }

   public final Setting aliases(String... strings) {
      this.aliases = strings == null ? new String[0] : (String[])strings.clone();
      return this;
   }

   public final String[] aliases() {
      return this.aliases;
   }

   public abstract void resetToDefault();

   public abstract JsonElement serialize();

   public final void deserialize(JsonElement jsonElement) {
      this.deserializeValue(jsonElement);
      this.notifyChanged();
   }

   protected abstract void deserializeValue(JsonElement jsonElement);
}
