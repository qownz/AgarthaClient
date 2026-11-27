package net.agartha.client.g.a_pkg;

import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import java.util.concurrent.locks.LockSupport;
import net.agartha.client.AgarthaClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class b implements net.agartha.client.g.b_pkg.a {
   private static final NativeLong NO_DELAY = new NativeLong(0L);
   private static final int PRESS = 1;
   private static final int RELEASE = 0;
   private final boolean available;
   private final Pointer display;

   public b() {
      boolean var1 = false;
      Pointer var2 = null;
      if (Platform.isLinux()) {
         try {
            var2 = c.INSTANCE.XOpenDisplay(null);
            if (var2 != null) {
               var1 = true;
            } else {
               AgarthaClientMod.LOGGER.warn("[Macro] XOpenDisplay returned null — no X11 display, OS input disabled");
            }
         } catch (Throwable var4) {
            AgarthaClientMod.LOGGER.warn("[Macro] X11/XTest unavailable — OS input disabled", var4);
         }
      }

      this.display = var2;
      this.available = var1;
   }

   @Override
   public boolean isAvailable() {
      return this.available;
   }

   @Override
   public void tapSlot(int i, int j) {
      if (this.available && i >= 1 && i <= 9) {
         int var3 = this.a(i);
         if (var3 != 0) {
            this.a(var3, true);
            b(j);
            this.a(var3, false);
         }
      }
   }

   @Override
   public void mouseDown(net.agartha.client.g.b_pkg.c arg) {
      this.a(arg, true);
   }

   @Override
   public void mouseUp(net.agartha.client.g.b_pkg.c arg) {
      this.a(arg, false);
   }

   @Override
   public void moveRelative(int i, int j) {
      if (this.available && (i != 0 || j != 0)) {
         try {
            d.INSTANCE.XTestFakeRelativeMotionEvent(this.display, i, j, NO_DELAY);
            c.INSTANCE.XFlush(this.display);
         } catch (Throwable var4) {
            AgarthaClientMod.LOGGER.warn("[Macro] XTestFakeRelativeMotionEvent failed", var4);
         }
      }
   }

   @Override
   public void click(net.agartha.client.g.b_pkg.c arg, int i) {
      if (this.available) {
         this.a(arg, true);
         b(i);
         this.a(arg, false);
      }
   }

   private int a(int i) {
      try {
         NativeLong var2 = new NativeLong(48 + i);
         return c.INSTANCE.XKeysymToKeycode(this.display, var2) & 0xFF;
      } catch (Throwable var3) {
         return 0;
      }
   }

   private void a(int i, boolean bl) {
      if (this.available) {
         try {
            d.INSTANCE.XTestFakeKeyEvent(this.display, i, bl ? 1 : 0, NO_DELAY);
            c.INSTANCE.XFlush(this.display);
         } catch (Throwable var4) {
            AgarthaClientMod.LOGGER.warn("[Macro] XTestFakeKeyEvent failed", var4);
         }
      }
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private void a(net.agartha.client.g.b_pkg.c arg, boolean bl) {
      if (this.available) {
         byte var3 = switch (arg) {
            case LEFT -> 1;
            case MIDDLE -> 2;
            case RIGHT -> 3;
            case X1 -> 8;
            case X2 -> 9;
         };

         try {
            d.INSTANCE.XTestFakeButtonEvent(this.display, var3, bl ? 1 : 0, NO_DELAY);
            c.INSTANCE.XFlush(this.display);
         } catch (Throwable var5) {
            AgarthaClientMod.LOGGER.warn("[Macro] XTestFakeButtonEvent failed", var5);
         }
      }
   }

   private static void b(int i) {
      if (i > 0) {
         long var1 = System.nanoTime() + i * 1000000L;

         while (System.nanoTime() < var1) {
            LockSupport.parkNanos(200000L);
         }
      }
   }
}
