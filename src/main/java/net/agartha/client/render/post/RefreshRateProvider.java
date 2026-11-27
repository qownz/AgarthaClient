package net.agartha.client.render.post;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

@Environment(EnvType.CLIENT)
public final class RefreshRateProvider {
   private static final long CHECK_INTERVAL_NS = 1000000000L;
   private static final int FALLBACK_HZ = 60;
   private static long lastCheck;
   private static long lastMonitor;
   private static int hz = 60;

   private RefreshRateProvider() {
   }

   static int current() {
      long var0 = System.nanoTime();
      if (lastCheck != 0L && var0 - lastCheck < 1000000000L) {
         return hz;
      } else {
         lastCheck = var0;

         try {
            long var2 = MinecraftClient.getInstance().getWindow().getHandle();
            long var4 = GLFW.glfwGetWindowMonitor(var2);
            if (var4 == 0L) {
               var4 = a(var2);
            }

            if (var4 != lastMonitor) {
               lastMonitor = var4;
               GLFWVidMode var6 = var4 == 0L ? null : GLFW.glfwGetVideoMode(var4);
               hz = var6 != null && var6.refreshRate() > 0 ? var6.refreshRate() : 60;
            }
         } catch (RuntimeException var7) {
            hz = 60;
         }

         return hz;
      }
   }

   private static long a(long l) {
      int[] var2 = new int[1];
      int[] var3 = new int[1];
      int[] var4 = new int[1];
      int[] var5 = new int[1];
      GLFW.glfwGetWindowPos(l, var2, var3);
      GLFW.glfwGetWindowSize(l, var4, var5);
      int var6 = var2[0] + var4[0] / 2;
      int var7 = var3[0] + var5[0] / 2;
      PointerBuffer var8 = GLFW.glfwGetMonitors();
      if (var8 != null) {
         for (int var9 = 0; var9 < var8.limit(); var9++) {
            long var10 = var8.get(var9);
            GLFWVidMode var12 = GLFW.glfwGetVideoMode(var10);
            if (var12 != null) {
               GLFW.glfwGetMonitorPos(var10, var2, var3);
               if (var6 >= var2[0] && var6 < var2[0] + var12.width() && var7 >= var3[0] && var7 < var3[0] + var12.height()) {
                  return var10;
               }
            }
         }
      }

      return GLFW.glfwGetPrimaryMonitor();
   }
}
