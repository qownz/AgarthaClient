package net.agartha.client.j;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.agartha.client.util.ab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class b {
   private static final List<b.a> scheduleQueue = new ArrayList<>();
   private static final List<b.a> scheduledMacros = new ArrayList<>();

   private b() {
   }

   public static boolean tryHandleCommand(String string) {
      if (string != null && string.startsWith(".macro")) {
         String var1 = string.substring(".macro".length()).trim();
         if (var1.isEmpty()) {
            a("Usage: .macro <name> [delay]", true);
            return true;
         } else {
            String[] var2 = var1.split("\\s+");
            if (var2[0].equalsIgnoreCase("clear")) {
               if (var2.length != 1) {
                  net.agartha.client.j.a var6 = d.getInstance().get(var2[1]);
                  if (var6 == null) {
                     a("Macro '" + var2[1] + "' not found.", true);
                     return true;
                  } else if (!isScheduled(var6)) {
                     a("This macro is not currently scheduled.", true);
                     return true;
                  } else {
                     clear(var6);
                     a("Cleared scheduled macro.", false);
                     return true;
                  }
               } else if (scheduleQueue.isEmpty() && scheduledMacros.isEmpty()) {
                  a("No macros are currently scheduled.", true);
                  return true;
               } else {
                  clearAll();
                  a("Cleared all scheduled macros.", false);
                  return true;
               }
            } else {
               net.agartha.client.j.a var3 = d.getInstance().get(var2[0]);
               if (var3 == null) {
                  a("Macro '" + var2[0] + "' not found.", true);
                  return true;
               } else {
                  int var4 = 0;
                  if (var2.length >= 2) {
                     Integer var5 = a(var2[1]);
                     if (var5 == null) {
                        a("Invalid delay: " + var2[1], true);
                        return true;
                     }

                     var4 = var5;
                  }

                  scheduleQueue.add(new b.a(var4, var3));
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   public static void tick() {
      if (!scheduleQueue.isEmpty()) {
         scheduledMacros.addAll(scheduleQueue);
         scheduleQueue.clear();
      }

      if (!scheduledMacros.isEmpty()) {
         scheduledMacros.removeIf(b.a::b);
      }

      for (b.a var1 : scheduledMacros) {
         var1.a();
      }
   }

   public static void clearAll() {
      scheduleQueue.clear();
      scheduledMacros.clear();
   }

   public static boolean isScheduled(net.agartha.client.j.a arg) {
      return scheduleQueue.stream().anyMatch(arg2 -> arg2.macro == arg) || scheduledMacros.stream().anyMatch(arg2 -> arg2.macro == arg);
   }

   public static void clear(net.agartha.client.j.a arg) {
      scheduleQueue.removeIf(arg2 -> arg2.macro == arg);
      scheduledMacros.removeIf(arg2 -> arg2.macro == arg);
   }

   private static Integer a(String string) {
      if (string != null && !string.isBlank()) {
         String var1 = string.toLowerCase(Locale.ROOT);

         try {
            if (var1.endsWith("t")) {
               return Integer.parseInt(var1.substring(0, var1.length() - 1));
            } else if (var1.endsWith("s")) {
               return Integer.parseInt(var1.substring(0, var1.length() - 1)) * 20;
            } else {
               return var1.endsWith("m") ? Integer.parseInt(var1.substring(0, var1.length() - 1)) * 20 * 60 : Integer.parseInt(var1);
            }
         } catch (NumberFormatException var3) {
            return null;
         }
      } else {
         return 0;
      }
   }

   private static void a(String string, boolean bl) {
      if (MinecraftClient.getInstance().player != null) {
         ab.send("Chat Macros", string);
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class a {
      private int delay;
      private final net.agartha.client.j.a macro;

      private a(int i, net.agartha.client.j.a arg) {
         this.delay = Math.max(0, i);
         this.macro = arg;
      }

      private void a() {
         this.delay--;
      }

      private boolean b() {
         if (this.delay > 0) {
            return false;
         } else {
            if (MinecraftClient.getInstance().player != null) {
               this.macro.onAction();
            }

            return true;
         }
      }
   }
}
