package net.agartha.client.module;

import com.google.gson.JsonObject;
import imgui.type.ImBoolean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.agartha.client.d.i;
import net.agartha.client.d.j;
import net.agartha.client.d.n;
import net.agartha.client.d.r;
import net.agartha.client.e.aj;
import net.agartha.client.module.a_pkg.k;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class b {
   private final String name;
   private final String description;
   private final c category;
   private final ImBoolean enabled = new ImBoolean(false);
   private final List<k> settings = new ArrayList<>();
   private final List<k> settingsView = Collections.unmodifiableList(this.settings);
   private final List<n<?>> ownedListeners = new ArrayList<>();
   private int keybind = -1;
   private boolean holdMode;
   private boolean registeredOnEventBus;

   protected b(String string, String string2, c arg) {
      this.name = string;
      this.description = string2;
      this.category = arg;
   }

   protected final <S extends k> S addSetting(S arg) {
      this.settings.add(arg);
      return (S)arg;
   }

   public final String name() {
      return this.name;
   }

   public final String description() {
      return this.description;
   }

   public final c category() {
      return this.category;
   }

   public final List<k> settings() {
      return this.settingsView;
   }

   public boolean persistEnabled() {
      return true;
   }

   public final ImBoolean enabled() {
      return this.enabled;
   }

   public final boolean isEnabled() {
      return this.enabled.get();
   }

   protected boolean lockEnabledOn() {
      return false;
   }

   protected boolean canEnable() {
      return true;
   }

   public final void setEnabled(boolean bl) {
      if (!this.lockEnabledOn() || bl) {
         if (!bl || this.enabled.get() || this.canEnable()) {
            if (this.enabled.get() != bl) {
               this.enabled.set(bl);
               if (bl) {
                  this.registerEventListenersIfPresent();

                  try {
                     this.onEnable();
                  } catch (Throwable var4) {
                     var4.printStackTrace();
                  }
               } else {
                  try {
                     this.onDisable();
                  } catch (Throwable var3) {
                     var3.printStackTrace();
                  }

                  this.ownedListeners.forEach(net.agartha.client.d.d.INSTANCE::off);
                  this.ownedListeners.clear();
                  this.unregisterEventListenersIfNeeded();
               }

               aj.getInstance().onModuleToggle(this, bl);
            }
         }
      }
   }

   public final void toggle() {
      if (!this.lockEnabledOn()) {
         this.setEnabled(!this.enabled.get());
      }
   }

   public final int keybind() {
      return this.keybind;
   }

   public final void setKeybind(int i) {
      if (net.agartha.client.j.e.isShiftKey(i) || net.agartha.client.j.e.isClickButton(i)) {
         i = -1;
      }

      if (this.keybind != i) {
         this.keybind = i;
         e.invalidateKeybindCache();
      }
   }

   public final boolean isHoldMode() {
      return this.holdMode;
   }

   public final void setHoldMode(boolean bl) {
      this.holdMode = bl;
   }

   public final void resetToDefaults() {
      for (k var2 : this.settings) {
         var2.resetToDefault();
      }

      this.setKeybind(-1);
      this.setHoldMode(false);
   }

   protected void onEnable() {
   }

   protected void onDisable() {
   }

   public boolean keybindTriggersAction() {
      return false;
   }

   public void afterSettingsLoaded(JsonObject jsonObject) {
   }

   public void onKeybindAction() {
   }

   protected <E extends net.agartha.client.d.c> void on(j<E> arg, Consumer<E> consumer) {
      this.on(arg, r.PRE, i.NORMAL, consumer);
   }

   protected <E extends net.agartha.client.d.c> void on(j<E> arg, r arg2, Consumer<E> consumer) {
      this.on(arg, arg2, i.NORMAL, consumer);
   }

   protected <E extends net.agartha.client.d.c> void on(j<E> arg, r arg2, i arg3, Consumer<E> consumer) {
      this.ownedListeners.add(arg.listen(this, arg2, arg3, consumer));
   }

   private void registerEventListenersIfPresent() {
      this.registeredOnEventBus = net.agartha.client.d.d.INSTANCE.register(this) > 0;
   }

   private void unregisterEventListenersIfNeeded() {
      if (this.registeredOnEventBus) {
         net.agartha.client.d.d.INSTANCE.unregister(this);
         this.registeredOnEventBus = false;
      }
   }
}
