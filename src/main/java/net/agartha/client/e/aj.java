package net.agartha.client.e;

import net.agartha.client.module.impl.client.NotificationsModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class aj {
   private static final aj INSTANCE = new aj();
   private static final float ENTER_DURATION = 0.38F;
   private static final float EXIT_DURATION = 0.38F;
   private static final String TOGGLE_TOKEN = " turned ";
   private String message = "";
   private String toggleModule = "";
   private aj.a activeKind = aj.a.ALERT;
   private aj.b phase = aj.b.NONE;
   private float phaseTime;
   private float holdDuration = 2.0F;

   private aj() {
   }

   public static aj getInstance() {
      return INSTANCE;
   }

   public void pushAlert(String string) {
      this.push(string, aj.a.ALERT);
   }

   public void push(String string, aj.a arg) {
      if (string != null && !string.isBlank()) {
         NotificationsModule var3 = net.agartha.client.module.e.get(NotificationsModule.class);
         if (var3 != null && var3.isEnabled()) {
            if (arg != aj.a.MODULE_TOGGLE || var3.moduleToggles()) {
               if (arg != aj.a.ALERT || var3.alerts()) {
                  String var4 = string.trim();
                  if (arg != aj.a.MODULE_TOGGLE || !this.a(var4)) {
                     this.holdDuration = Math.max(1.0F, var3.durationSeconds());
                     this.message = var4;
                     this.activeKind = arg;
                     this.toggleModule = arg == aj.a.MODULE_TOGGLE ? b(var4) : "";
                     this.phase = aj.b.ENTER;
                     this.phaseTime = 0.0F;
                     ak.playConfigured();
                  }
               }
            }
         }
      }
   }

   private boolean a(String string) {
      if (this.phase != aj.b.NONE && this.activeKind == aj.a.MODULE_TOGGLE) {
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

   public void onModuleToggle(net.agartha.client.module.b arg, boolean bl) {
      if (arg != null && arg.category() != net.agartha.client.module.c.CLIENT) {
         String var3 = bl ? "ON" : "OFF";
         this.push(arg.name() + " turned " + var3, aj.a.MODULE_TOGGLE);
      }
   }

   public void tick(float f) {
      if (this.phase != aj.b.NONE) {
         this.phaseTime += f;
         float var2 = this.a(this.phase);
         if (this.phaseTime >= var2) {
            this.a();
         }
      }
   }

   public aj.c snapshot() {
      if (this.phase != aj.b.NONE && !this.message.isEmpty()) {
         float var1 = this.a(this.phase);
         float var2 = var1 <= 0.0F ? 1.0F : Math.min(this.phaseTime / var1, 1.0F);
         float var3 = a(this.phase, var2);
         float var4 = this.phase == aj.b.HOLD ? 1.0F - var2 : (this.phase == aj.b.ENTER ? 1.0F : 0.0F);
         return new aj.c(this.message, this.phase, var2, var3, var4);
      } else {
         return null;
      }
   }

   private void a() {
      this.phaseTime = 0.0F;

      this.phase = switch (this.phase) {
         case ENTER -> aj.b.HOLD;
         case HOLD -> aj.b.EXIT;
         case EXIT -> aj.b.NONE;
         default -> aj.b.NONE;
      };
      if (this.phase == aj.b.NONE) {
         this.message = "";
         this.toggleModule = "";
         this.activeKind = aj.a.ALERT;
      }
   }

   private float a(aj.b arg) {
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

   private static float a(aj.b arg, float f) {
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
   public record c(String message, aj.b phase, float progress, float slide, float holdRemaining) {
   }
}
