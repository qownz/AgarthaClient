package net.agartha.client.platform.input;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class WindowFocusGuard {
   private static final String[] CLIENT_TITLE_HINTS = new String[]{"minecraft", "lunar", "badlion", "feather", "fabric", "forge"};

   public boolean canRun() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.player == null || var1.world == null) {
         return false;
      } else if (var1.currentScreen != null) {
         return false;
      } else if (!var1.isWindowFocused()) {
         return false;
      } else {
         return Platform.isWindows() ? this.a() : true;
      }
   }

   public boolean canInjectLook() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      return var1.player != null && var1.world != null && var1.currentScreen == null;
   }

   private boolean a() {
      try {
         HWND var1 = User32.INSTANCE.GetForegroundWindow();
         if (var1 == null) {
            return false;
         } else {
            char[] var2 = new char[512];
            int var3 = User32.INSTANCE.GetWindowText(var1, var2, var2.length);
            if (var3 <= 0) {
               return false;
            } else {
               String var4 = new String(var2, 0, var3).toLowerCase(Locale.ROOT);

               for (String var8 : CLIENT_TITLE_HINTS) {
                  if (var4.contains(var8)) {
                     return true;
                  }
               }

               return false;
            }
         }
      } catch (Throwable var9) {
         return true;
      }
   }
}
