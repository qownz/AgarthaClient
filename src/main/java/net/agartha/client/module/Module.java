package net.agartha.client.module;

import com.google.gson.JsonObject;
import imgui.type.ImBoolean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.agartha.client.event.Event;
import net.agartha.client.event.EventBus;
import net.agartha.client.event.EventPriority;
import net.agartha.client.event.EventStage;
import net.agartha.client.event.EventType;
import net.agartha.client.event.ListenerHandle;
import net.agartha.client.gui.NotificationManager;
import net.agartha.client.input.Keybind;
import net.agartha.client.module.setting.Setting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Module {
   private final String name;
   private final String description;
   private final ModuleCategory category;
   private final ImBoolean enabled = new ImBoolean(false);
   private final List<Setting> settings = new ArrayList<>();
   private final List<Setting> settingsView = Collections.unmodifiableList(this.settings);
   private final List<ListenerHandle<?>> ownedListeners = new ArrayList<>();
   private int keybind = -1;
   private boolean holdMode;
   private boolean registeredOnEventBus;

   protected Module(String string, String string2, ModuleCategory arg) {
      this.name = string;
      this.description = string2;
      this.category = arg;
   }

   protected final <S extends Setting> S addSetting(S arg) {
      this.settings.add(arg);
      return (S)arg;
   }

   public final String name() {
      return this.name;
   }

   public final String description() {
      return this.description;
   }

   public final ModuleCategory category() {
      return this.category;
   }

   public final List<Setting> settings() {
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

                  this.ownedListeners.forEach(EventBus.INSTANCE::off);
                  this.ownedListeners.clear();
                  this.unregisterEventListenersIfNeeded();
               }

               NotificationManager.getInstance().onModuleToggle(this, bl);
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
      if (Keybind.isShiftKey(i) || Keybind.isClickButton(i)) {
         i = -1;
      }

      if (this.keybind != i) {
         this.keybind = i;
         ModuleManager.invalidateKeybindCache();
      }
   }

   public final boolean isHoldMode() {
      return this.holdMode;
   }

   public final void setHoldMode(boolean bl) {
      this.holdMode = bl;
   }

   public final void resetToDefaults() {
      for (Setting var2 : this.settings) {
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

   protected <E extends Event> void on(EventType<E> arg, Consumer<E> consumer) {
      this.on(arg, EventStage.PRE, EventPriority.NORMAL, consumer);
   }

   protected <E extends Event> void on(EventType<E> arg, EventStage arg2, Consumer<E> consumer) {
      this.on(arg, arg2, EventPriority.NORMAL, consumer);
   }

   protected <E extends Event> void on(EventType<E> arg, EventStage arg2, EventPriority arg3, Consumer<E> consumer) {
      this.ownedListeners.add(arg.listen(this, arg2, arg3, consumer));
   }

   private void registerEventListenersIfPresent() {
      this.registeredOnEventBus = EventBus.INSTANCE.register(this) > 0;
   }

   private void unregisterEventListenersIfNeeded() {
      if (this.registeredOnEventBus) {
         EventBus.INSTANCE.unregister(this);
         this.registeredOnEventBus = false;
      }
   }
}
