package net.agartha.client.platform.input;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.LONG;
import com.sun.jna.platform.win32.WinDef.WORD;
import com.sun.jna.platform.win32.WinUser.INPUT;
import java.util.concurrent.locks.LockSupport;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.platform.input.MouseButton;
import net.agartha.client.platform.input.OsInputBridge;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class WindowsInputBridge implements OsInputBridge {
   private static final int KEYEVENTF_SCANCODE = 8;
   private static final int KEYEVENTF_KEYUP = 2;
   private static final int MOUSEEVENTF_MOVE = 1;
   private static final int MOUSEEVENTF_LEFTDOWN = 2;
   private static final int MOUSEEVENTF_LEFTUP = 4;
   private static final int MOUSEEVENTF_RIGHTDOWN = 8;
   private static final int MOUSEEVENTF_RIGHTUP = 16;
   private static final int MOUSEEVENTF_MIDDLEDOWN = 32;
   private static final int MOUSEEVENTF_MIDDLEUP = 64;
   private static final int MOUSEEVENTF_XDOWN = 128;
   private static final int MOUSEEVENTF_XUP = 256;
   private static final int XBUTTON1 = 1;
   private static final int XBUTTON2 = 2;
   private static final int SCANCODE_SLOT_1 = 2;
   private final boolean available;

   public WindowsInputBridge() {
      boolean var1 = false;
      if (Platform.isWindows()) {
         try {
            User32.INSTANCE.GetForegroundWindow();
            var1 = true;
         } catch (Throwable var3) {
            AgarthaClientMod.LOGGER.warn("[Macro] JNA user32 unavailable — OS input disabled", var3);
         }
      }

      this.available = var1;
   }

   @Override
   public boolean isAvailable() {
      return this.available;
   }

   @Override
   public void tapSlot(int i, int j) {
      if (this.available && i >= 1 && i <= 9) {
         int var3 = 2 + (i - 1);
         this.a(var3, false);
         a(j);
         this.a(var3, true);
      }
   }

   @Override
   public void mouseDown(MouseButton arg) {
      this.a(arg, false);
   }

   @Override
   public void mouseUp(MouseButton arg) {
      this.a(arg, true);
   }

   @Override
   public void moveRelative(int i, int j) {
      if (this.available && (i != 0 || j != 0)) {
         INPUT var3 = new INPUT();
         var3.type = new DWORD(0L);
         var3.input.setType("mi");
         var3.input.mi.dx = new LONG(i);
         var3.input.mi.dy = new LONG(j);
         var3.input.mi.mouseData = new DWORD(0L);
         var3.input.mi.dwFlags = new DWORD(1L);
         var3.input.mi.time = new DWORD(0L);
         this.a(var3);
      }
   }

   @Override
   public void click(MouseButton arg, int i) {
      if (this.available) {
         this.a(arg, false);
         a(i);
         this.a(arg, true);
      }
   }

   private void a(int i, boolean bl) {
      if (this.available) {
         INPUT var3 = new INPUT();
         var3.type = new DWORD(1L);
         var3.input.setType("ki");
         var3.input.ki.wVk = new WORD(0L);
         var3.input.ki.wScan = new WORD(i & 65535);
         var3.input.ki.dwFlags = new DWORD(8 | (bl ? 2 : 0));
         var3.input.ki.time = new DWORD(0L);
         this.a(var3);
      }
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private void a(MouseButton arg, boolean bl) {
      if (this.available) {
         byte var4 = 0;
         int var3;
         switch (arg) {
            case LEFT:
               var3 = bl ? 4 : 2;
               break;
            case RIGHT:
               var3 = bl ? 16 : 8;
               break;
            case MIDDLE:
               var3 = bl ? 64 : 32;
               break;
            case X1:
               var3 = bl ? 256 : 128;
               var4 = 1;
               break;
            case X2:
               var3 = bl ? 256 : 128;
               var4 = 2;
               break;
            default:
               return;
         }

         INPUT var5 = new INPUT();
         var5.type = new DWORD(0L);
         var5.input.setType("mi");
         var5.input.mi.dx = new LONG(0L);
         var5.input.mi.dy = new LONG(0L);
         var5.input.mi.mouseData = new DWORD(var4);
         var5.input.mi.dwFlags = new DWORD(var3);
         var5.input.mi.time = new DWORD(0L);
         this.a(var5);
      }
   }

   private void a(INPUT iNPUT) {
      try {
         DWORD var2 = User32.INSTANCE.SendInput(new DWORD(1L), new INPUT[]{iNPUT}, iNPUT.size());
         if (var2.intValue() != 1) {
            AgarthaClientMod.LOGGER.warn("[Macro] SendInput dropped event (returned {})", var2.intValue());
         }
      } catch (Throwable var3) {
         AgarthaClientMod.LOGGER.warn("[Macro] SendInput failed", var3);
      }
   }

   private static void a(int i) {
      if (i > 0) {
         long var1 = System.nanoTime() + i * 1000000L;

         while (System.nanoTime() < var1) {
            LockSupport.parkNanos(200000L);
         }
      }
   }
}
