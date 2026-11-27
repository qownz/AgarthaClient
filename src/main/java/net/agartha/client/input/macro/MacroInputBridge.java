package net.agartha.client.input.macro;

import com.sun.jna.Platform;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.macro.task.TaskScheduler;
import net.agartha.client.platform.input.OsInputBridge;
import net.agartha.client.platform.input.WindowFocusGuard;
import net.agartha.client.platform.input.WindowsInputBridge;
import net.agartha.client.platform.input.X11InputBridge;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MacroInputBridge {
   private static final OsInputBridge INPUT = OsInputBridge();
   private static final WindowFocusGuard FOCUS_GUARD = new WindowFocusGuard();
   private static final TaskScheduler SCHEDULER = new TaskScheduler();

   private MacroInputBridge() {
   }

   private static OsInputBridge OsInputBridge() {
      Object var0;
      if (Platform.isWindows()) {
         var0 = new WindowsInputBridge();
      } else if (Platform.isLinux()) {
         var0 = new X11InputBridge();
      } else {
         var0 = new FallbackInputBridge();
      }

      AgarthaClientMod.LOGGER
         .info(
            "[Macro] Input bridge: {} (available={})",
            ((OsInputBridge)var0).getClass().getSimpleName(),
            ((OsInputBridge)var0).isAvailable()
         );
      return (OsInputBridge)var0;
   }

   public static OsInputBridge input() {
      return INPUT;
   }

   public static WindowFocusGuard focusGuard() {
      return FOCUS_GUARD;
   }

   public static TaskScheduler scheduler() {
      return SCHEDULER;
   }
}
