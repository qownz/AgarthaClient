package net.agartha.client.util.color;

import net.agartha.client.gui.AccentColor;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ColorSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ColorSettingWiring {
   private ColorSettingWiring() {
   }

   public static void wireExclusiveColorSource(BooleanSetting arg, BooleanSetting arg2, ColorSetting arg3) {
      wireExclusiveColorSource(arg, arg2, arg3, false);
   }

   public static void wireExclusiveColorSource(
      BooleanSetting arg, BooleanSetting arg2, ColorSetting arg3, boolean bl
   ) {
      arg3.visibleWhen(arg::get);
      arg.onChange(() -> {
         if (arg.get()) {
            if (arg2.get()) {
               arg2.set(false);
            }
         } else if (!arg2.get()) {
            arg.set(true);
         }
      });
      arg2.onChange(() -> {
         if (arg2.get()) {
            if (arg.get()) {
               arg.set(false);
            }
         } else if (!arg.get()) {
            arg2.set(true);
         }
      });
      if (arg.get() && arg2.get()) {
         if (bl) {
            arg.set(false);
         } else {
            arg2.set(false);
         }
      } else if (!arg.get() && !arg2.get()) {
         if (bl) {
            arg2.set(true);
         } else {
            arg.set(true);
         }
      }
   }

   public static int argb(BooleanSetting arg, BooleanSetting arg2, ColorSetting arg3, int i) {
      if (arg != null && arg.get()) {
         return a(arg3);
      } else {
         return arg2 != null && arg2.get() && arg3 != null ? arg3.argb() : i;
      }
   }

   private static int a(ColorSetting arg) {
      int var1 = 0xFF000000 | AccentColor.current().rgb();
      if (arg == null) {
         return var1;
      } else {
         int var2 = arg.argb() >>> 24 & 0xFF;
         return var1 & 16777215 | var2 << 24;
      }
   }
}
