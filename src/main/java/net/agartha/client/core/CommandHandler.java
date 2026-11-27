package net.agartha.client.core;

import java.util.Locale;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.misc.StaffAlertModule;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.module.setting.Setting;
import net.agartha.client.module.setting.StringSetting;
import net.agartha.client.util.net.ChatSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;

@Environment(EnvType.CLIENT)
public final class CommandHandler {
   private CommandHandler() {
   }

   public static boolean handle(String string, String string2) {
      if (string == null || string2 == null || string2.isEmpty()) {
         return false;
      } else if (!string.startsWith(string2)) {
         return false;
      } else {
         String var2 = string.substring(string2.length()).trim();
         if (var2.isEmpty()) {
            a();
            return true;
         } else {
            int var3 = var2.indexOf(32);
            String var4 = (var3 < 0 ? var2 : var2.substring(0, var3)).toLowerCase(Locale.ROOT);
            String var5 = var3 < 0 ? "" : var2.substring(var3 + 1).trim();
            switch (var4) {
               case "help":
               case "?":
                  a();
                  break;
               case "toggle":
               case "t":
                  a(var5);
                  break;
               case "on":
                  a(var5, true);
                  break;
               case "off":
                  a(var5, false);
                  break;
               case "renderset":
               case "render":
               case "rd":
                  b(var5);
                  break;
               case "simset":
               case "sim":
               case "sd":
                  c(var5);
                  break;
               case "fov":
                  d(var5);
                  break;
               case "set":
               case "s":
                  e(var5);
                  break;
               case "stafftest":
                  b();
                  break;
               default:
                  Module var8 = Module(var2, true);
                  if (var8 != null) {
                     a(var8);
                  } else {
                     k("Unknown command. " + string2 + "help");
                  }
            }

            return true;
         }
      }
   }

   private static void a() {
      k("toggle <module>  on <module>  off <module>");
      k("renderset <2-32>  simset <5-32>  fov <30-110>");
      k("set <module> <setting> <value>");
      k("stafftest");
      k("Or just the module name to toggle it.");
   }

   private static void b() {
      StaffAlertModule var0 = ModuleManager.get(StaffAlertModule.class);
      if (var0 == null) {
         k("Staff Alert is missing.");
      } else {
         var0.playTest();
      }
   }

   private static void a(String string) {
      Module var1 = f(string);
      if (var1 != null) {
         a(var1);
      }
   }

   private static void a(String string, boolean bl) {
      Module var2 = f(string);
      if (var2 != null) {
         if (var2.isEnabled() == bl) {
            k(var2.name() + " is already " + (bl ? "on" : "off") + ".");
         } else {
            var2.setEnabled(bl);
            if (var2.isEnabled() != bl) {
               k(var2.name() + " stays on.");
            } else {
               k(var2.name() + (bl ? " on." : " off."));
            }
         }
      }
   }

   private static void a(Module arg) {
      boolean var1 = !arg.isEnabled();
      arg.toggle();
      if (arg.isEnabled() == var1) {
         k(arg.name() + (var1 ? " on." : " off."));
      } else {
         k(arg.name() + " stays on.");
      }
   }

   private static void b(String string) {
      Integer var1 = h(string);
      if (var1 == null) {
         k("Usage: renderset <2-32>");
      } else {
         int var2 = Math.max(2, Math.min(32, var1));
         GameOptions var3 = c();
         if (var3 != null) {
            var3.getViewDistance().setValue(var2);
            a("Chunk Debug", "Render Distance", var2);
            k("Render distance " + var2 + ".");
         }
      }
   }

   private static void c(String string) {
      Integer var1 = h(string);
      if (var1 == null) {
         k("Usage: simset <5-32>");
      } else {
         int var2 = Math.max(5, Math.min(32, var1));
         GameOptions var3 = c();
         if (var3 != null) {
            var3.getSimulationDistance().setValue(var2);
            a("Chunk Debug", "Sim Distance", var2);
            k("Simulation distance " + var2 + ".");
         }
      }
   }

   private static void d(String string) {
      Integer var1 = h(string);
      if (var1 == null) {
         k("Usage: fov <30-110>");
      } else {
         int var2 = Math.max(30, Math.min(110, var1));
         GameOptions var3 = c();
         if (var3 != null) {
            var3.getFov().setValue(var2);
            k("FOV " + var2 + ".");
         }
      }
   }

   private static void e(String string) {
      if (string.isEmpty()) {
         k("Usage: set <module> <setting> <value>");
      } else {
         CommandHandler.a var1 = g(string);
         if (var1 == null) {
            k("No module named \"" + string + "\".");
         } else {
            String var2 = string.substring(var1.end).trim();
            if (var2.isEmpty()) {
               k("Usage: set " + var1.module.name() + " <setting> <value>");
            } else {
               CommandHandler.a var3 = a(var1.module, var2);
               if (var3 == null) {
                  k("No setting \"" + var2 + "\" on " + var1.module.name() + ".");
               } else {
                  String var4 = var2.substring(var3.end).trim();
                  a(var1.module, var3.setting, var4);
               }
            }
         }
      }
   }

   private static void a(Module arg, Setting arg2, String string) {
      if (arg2 instanceof BooleanSetting var9) {
         Boolean var11 = j(string);
         if (var11 == null) {
            k(arg2.name() + " is " + (var9.get() ? "on" : "off") + ". Use on or off.");
         } else {
            var9.set(var11);
            k(arg.name() + " " + arg2.name() + (var9.get() ? " on." : " off."));
         }
      } else if (arg2 instanceof NumberSetting var8) {
         if (string.isEmpty()) {
            k(arg.name() + " " + arg2.name() + " is " + var8.display() + ".");
         } else {
            Float var10 = i(string);
            if (var10 == null) {
               k("Not a number: " + string);
            } else {
               var8.set(var10);
               k(arg.name() + " " + arg2.name() + " " + var8.display() + ".");
            }
         }
      } else if (!(arg2 instanceof EnumSetting var3)) {
         if (arg2 instanceof StringSetting var7) {
            if (string.isEmpty()) {
               k(arg.name() + " " + arg2.name() + " is \"" + var7.get() + "\".");
            } else {
               var7.set(string);
               k(arg.name() + " " + arg2.name() + " set.");
            }
         } else {
            k(arg2.name() + " can't be changed from chat.");
         }
      } else if (string.isEmpty()) {
         k(arg.name() + " " + arg2.name() + " is " + var3.get() + ".");
      } else {
         String var4 = null;

         for (String var6 : var3.modes()) {
            if (var6.equalsIgnoreCase(string)) {
               var4 = var6;
               break;
            }
         }

         if (var4 == null) {
            k(arg2.name() + ": " + String.join(", ", var3.modes()));
         } else {
            var3.set(var4);
            k(arg.name() + " " + arg2.name() + " " + var3.get() + ".");
         }
      }
   }

   private static void a(String string, String string2, int i) {
      Module var3 = ModuleManager.byName(string);
      if (var3 != null && var3.isEnabled()) {
         for (Setting var5 : var3.settings()) {
            if (var5 instanceof NumberSetting var6 && var5.name().equals(string2)) {
               var6.set(i);
               return;
            }
         }
      }
   }

   private static Module f(String string) {
      if (string.isEmpty()) {
         k("Name the module.");
         return null;
      } else {
         Module var1 = Module(string, true);
         if (var1 == null) {
            k("No module named \"" + string + "\".");
         }

         return var1;
      }
   }

   private static Module Module(String string, boolean bl) {
      CommandHandler.a var2 = g(string);
      if (var2 == null) {
         return null;
      } else {
         return bl && !string.substring(var2.end).trim().isEmpty() ? null : var2.module;
      }
   }

   private static CommandHandler.a g(String string) {
      Module var1 = null;
      int var2 = -1;

      for (Module var4 : ModuleManager.all()) {
         int var5 = a(string, var4.name());
         if (var5 > var2) {
            var1 = var4;
            var2 = var5;
         }
      }

      return var1 == null ? null : new CommandHandler.a(var1, null, var2);
   }

   private static CommandHandler.a a(Module arg, String string) {
      Setting var2 = null;
      int var3 = -1;

      for (Setting var5 : arg.settings()) {
         int var6 = a(string, var5.name());
         if (var6 > var3) {
            var2 = var5;
            var3 = var6;
         }
      }

      return var2 == null ? null : new CommandHandler.a(null, var2, var3);
   }

   private static int a(String string, String string2) {
      int var2 = 0;
      int var3 = 0;
      int var4 = string.length();
      int var5 = string2.length();

      while (var2 < var4 && var3 < var5) {
         char var6 = string.charAt(var2);
         char var7 = string2.charAt(var3);
         if (var6 == ' ') {
            var2++;
         } else if (var7 == ' ') {
            var3++;
         } else {
            if (Character.toLowerCase(var6) != Character.toLowerCase(var7)) {
               return -1;
            }

            var2++;
            var3++;
         }
      }

      while (var3 < var5 && string2.charAt(var3) == ' ') {
         var3++;
      }

      return var3 != var5 ? -1 : var2;
   }

   private static GameOptions c() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      if (var0.options == null) {
         k("Not in a world.");
         return null;
      } else {
         return var0.options;
      }
   }

   private static Integer h(String string) {
      if (string.isEmpty()) {
         return null;
      } else {
         try {
            return Integer.parseInt(string);
         } catch (NumberFormatException var2) {
            return null;
         }
      }
   }

   private static Float i(String string) {
      try {
         return Float.parseFloat(string);
      } catch (NumberFormatException var2) {
         return null;
      }
   }

   private static Boolean j(String string) {
      if (string.isEmpty()) {
         return null;
      } else {
         String var1 = string.toLowerCase(Locale.ROOT);
         if (var1.equals("on") || var1.equals("true") || var1.equals("1")) {
            return true;
         } else {
            return !var1.equals("off") && !var1.equals("false") && !var1.equals("0") ? null : false;
         }
      }
   }

   private static void k(String string) {
      ChatSender.send("Agartha", string);
   }

   @Environment(EnvType.CLIENT)
   private record a(Module module, Setting setting, int end) {
   }
}
