package net.agartha.client.module.impl.client;

import java.util.Locale;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class NotificationsModule extends Module {
   public static final String SOUND_POP_1 = "Pop 1";
   public static final String SOUND_POP_2 = "Pop 2";
   public static final String SOUND_FAHH = "Fahh";
   private static final float VOLUME_UI_MAX = 5.0F;
   private static final float VOLUME_STEP = 0.2F;
   private final BooleanSetting moduleToggles = (BooleanSetting)this.addSetting(
      new BooleanSetting("Module Toggles", false)
   );
   private final BooleanSetting alerts = (BooleanSetting)this.addSetting(new BooleanSetting("Alerts", false));
   private final BooleanSetting sound = (BooleanSetting)this.addSetting(new BooleanSetting("Sound", false));
   private final NumberSetting duration = (NumberSetting)this.addSetting(new NumberSetting("Duration", 2.0F, 1.0F, 10.0F));
   private final EnumSetting soundEffect = (EnumSetting)this.addSetting(new EnumSetting("Sound Effect", "Pop 2", new String[]{"Pop 2", "Pop 1", "Fahh"}));
   private final NumberSetting volume = (NumberSetting)this.addSetting(new NumberSetting("Volume", 1.0F, 0.0F, 5.0F));
   private final EnumSetting notificationCorner = (EnumSetting)this.addSetting(
      new EnumSetting("Notification Position", "Bottom Left", new String[]{"Top Left", "Top Right", "Bottom Left", "Bottom Right"})
   );

   public NotificationsModule() {
      super("Notifications", "Toast cards, module alerts, and notification sounds.", ModuleCategory.CLIENT);
      this.soundEffect.visibleWhen(this.sound::get);
      this.volume.visibleWhen(this.sound::get);
      this.notificationCorner.visibleWhen(() -> false);
   }

   public boolean overlay() {
      return this.isEnabled();
   }

   public boolean moduleToggles() {
      return this.moduleToggles.get();
   }

   public boolean alerts() {
      return this.alerts.get();
   }

   public float durationSeconds() {
      return this.duration.get();
   }

   public boolean sound() {
      return this.sound.get();
   }

   public String soundEffect() {
      return this.soundEffect.get();
   }

   public String notificationCorner() {
      return this.notificationCorner.get();
   }

   public EnumSetting notificationCornerSetting() {
      return this.notificationCorner;
   }

   public static String formatCornerLabel(String string) {
      return string != null && !string.isBlank() ? string.toLowerCase(Locale.ROOT) : "bottom left";
   }

   public float effectiveVolume() {
      return Math.max(0.0F, Math.min(1.0F, this.volume.get() * 0.2F));
   }
}
