package net.agartha.client.gui;

import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.client.NotificationsModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class NotificationManager {
   private static final NotificationManager INSTANCE = new NotificationManager();
   private static final float ENTER_DURATION = 0.38F;
   private static final float EXIT_DURATION = 0.38F;
   private static final String TOGGLE_TOKEN = " turned ";
   private String message = "";
   private String toggleModule = "";
   private NotificationManager.a activeKind = NotificationManager.a.ALERT;
   private NotificationManager.b phase = NotificationManager.b.NONE;
   private float phaseTime;
   private float holdDuration = 2.0F;

   private NotificationManager() {
   }

   public static NotificationManager getInstance() {
      return INSTANCE;
   }

   public void pushAlert(String string) {
      this.push(string, NotificationManager.a.ALERT);
   }

   public void push(String string, NotificationManager.a arg) {
      if (string != null && !string.isBlank()) {
         NotificationsModule var3 = ModuleManager.get(NotificationsModule.class);
         if (var3 != null && var3.isEnabled()) {
            if (arg != NotificationManager.a.MODULE_TOGGLE || var3.moduleToggles()) {
               if (arg != NotificationManager.a.ALERT || var3.alerts()) {
                  String var4 = string.trim();
                  if (arg != NotificationManager.a.MODULE_TOGGLE || !this.a(var4)) {
                     this.holdDuration = Math.max(1.0F, var3.durationSeconds());
                     this.message = var4;
                     this.activeKind = arg;
                     this.toggleModule = arg == NotificationManager.a.MODULE_TOGGLE ? b(var4) : "";
                     this.phase = NotificationManager.b.ENTER;
                     this.phaseTime = 0.0F;
                     NotificationSounds.playConfigured();
                  }
               }
            }
         }
      }
   }

   private boolean a(String string) {
      if (this.phase != NotificationManager.b.NONE && this.activeKind == NotificationManager.a.MODULE_TOGGLE) {
         String var2 = b(string);
         if (!var2.isEmpty() && var2.equals(this.toggleModule)) {
            this.message = string;
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void onModuleToggle(Module arg, boolean bl) {
      if (arg != null && arg.category() != ModuleCategory.CLIENT) {
         String var3 = bl ? "ON" : "OFF";
         this.push(arg.name() + " turned " + var3, NotificationManager.a.MODULE_TOGGLE);
      }
   }

   public void tick(float f) {
      if (this.phase != NotificationManager.b.NONE) {
         this.phaseTime += f;
         float var2 = this.a(this.phase);
         if (this.phaseTime >= var2) {
            this.a();
         }
      }
   }

   public NotificationManager.c snapshot() {
      if (this.phase != NotificationManager.b.NONE && !this.message.isEmpty()) {
         float var1 = this.a(this.phase);
         float var2 = var1 <= 0.0F ? 1.0F : Math.min(this.phaseTime / var1, 1.0F);
         float var3 = a(this.phase, var2);
         float var4 = this.phase == NotificationManager.b.HOLD ? 1.0F - var2 : (this.phase == NotificationManager.b.ENTER ? 1.0F : 0.0F);
         return new NotificationManager.c(this.message, this.phase, var2, var3, var4);
      } else {
         return null;
      }
   }

   private void a() {
      this.phaseTime = 0.0F;

      this.phase = switch (this.phase) {
         case ENTER -> NotificationManager.b.HOLD;
         case HOLD -> NotificationManager.b.EXIT;
         case EXIT -> NotificationManager.b.NONE;
         default -> NotificationManager.b.NONE;
      };
      if (this.phase == NotificationManager.b.NONE) {
         this.message = "";
         this.toggleModule = "";
         this.activeKind = NotificationManager.a.ALERT;
      }
   }

   private float a(NotificationManager.b arg) {
      return switch (arg) {
         case ENTER -> 0.38F;
         case HOLD -> this.holdDuration;
         case EXIT -> 0.38F;
         default -> 0.0F;
      };
   }

   private static String b(String string) {
      int var1 = string.indexOf(" turned ");
      return var1 > 0 ? string.substring(0, var1) : "";
   }

   private static float a(NotificationManager.b arg, float f) {
      float var2 = a(f);

      return switch (arg) {
         case ENTER -> 1.0F - var2;
         case HOLD -> 0.0F;
         case EXIT -> var2;
         default -> 1.0F;
      };
   }

   private static float a(float f) {
      float var1 = 1.0F - f;
      return 1.0F - var1 * var1 * var1;
   }

   @Environment(EnvType.CLIENT)
   public static enum a {
      ALERT,
      MODULE_TOGGLE;
   }

   @Environment(EnvType.CLIENT)
   public static enum b {
      NONE,
      ENTER,
      HOLD,
      EXIT;
   }

   @Environment(EnvType.CLIENT)
   public record c(String message, NotificationManager.b phase, float progress, float slide, float holdRemaining) {
   }
}
