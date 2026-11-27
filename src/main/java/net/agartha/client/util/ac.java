package net.agartha.client.util;

import net.agartha.client.e.at;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ac {
   private ac() {
   }

   public static void wireExclusiveColorSource(net.agartha.client.module.a_pkg.b arg, net.agartha.client.module.a_pkg.b arg2, net.agartha.client.module.a_pkg.c arg3) {
      wireExclusiveColorSource(arg, arg2, arg3, false);
   }

   public static void wireExclusiveColorSource(
      net.agartha.client.module.a_pkg.b arg, net.agartha.client.module.a_pkg.b arg2, net.agartha.client.module.a_pkg.c arg3, boolean bl
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

   public static int argb(net.agartha.client.module.a_pkg.b arg, net.agartha.client.module.a_pkg.b arg2, net.agartha.client.module.a_pkg.c arg3, int i) {
      if (arg != null && arg.get()) {
         return a(arg3);
      } else {
         return arg2 != null && arg2.get() && arg3 != null ? arg3.argb() : i;
      }
   }

   private static int a(net.agartha.client.module.a_pkg.c arg) {
      int var1 = 0xFF000000 | at.current().rgb();
      if (arg == null) {
         return var1;
      } else {
         int var2 = arg.argb() >>> 24 & 0xFF;
         return var1 & 16777215 | var2 << 24;
      }
   }
}
