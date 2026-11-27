package net.agartha.client.util.io;

import com.mojang.blaze3d.opengl.GlStateManager;
import java.io.File;
import java.nio.ByteBuffer;
import net.agartha.client.util.net.ChatSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.OpenFile;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public final class ScreenshotCapture {
   private static boolean pending;

   private ScreenshotCapture() {
   }

   public static void request() {
      pending = true;
   }

   public static void captureIfPending() {
      if (pending) {
         pending = false;
         MinecraftClient var0 = MinecraftClient.getInstance();
         int var1 = var0.getWindow().getFramebufferWidth();
         int var2 = var0.getWindow().getFramebufferHeight();
         if (var1 > 0 && var2 > 0) {
            int var3 = GlStateManager.getFrameBuffer(36008);
            int var4 = GlStateManager.getFrameBuffer(36009);
            int var5 = GL11.glGetInteger(3333);
            ByteBuffer var6 = ByteBuffer.allocateDirect(var1 * var2 * 4);

            try {
               GlStateManager._glBindFramebuffer(36008, 0);
               GlStateManager._pixelStore(3333, 1);
               GlStateManager._readPixels(0, 0, var1, var2, 6408, 5121, MemoryUtil.memAddress(var6));
               NativeImage var7 = new NativeImage(var1, var2, false);

               try {
                  for (int var8 = 0; var8 < var2; var8++) {
                     for (int var9 = 0; var9 < var1; var9++) {
                        int var10 = (var9 + var8 * var1) * 4;
                        int var11 = var6.get(var10) & 255;
                        int var12 = var6.get(var10 + 1) & 255;
                        int var13 = var6.get(var10 + 2) & 255;
                        var7.setColor(var9, var2 - var8 - 1, 0xFF000000 | var13 << 16 | var12 << 8 | var11);
                     }
                  }

                  File var23 = new File(var0.runDirectory, "screenshots");
                  var23.mkdirs();
                  File var24 = a(var23);
                  var7.writeTo(var24);
                  ClipboardUtil.copyIfEnabled(var24);
                  MutableText var25 = Text.literal(var24.getName())
                     .formatted(Formatting.UNDERLINE)
                     .styled(style -> style.withClickEvent(new OpenFile(var24.getAbsoluteFile())));
                  ChatSender.send(Text.translatable("screenshot.success", new Object[]{var25}));
               } catch (Throwable var20) {
                  try {
                     var7.close();
                  } catch (Throwable var19) {
                     var20.addSuppressed(var19);
                  }

                  throw var20;
               }

               var7.close();
            } catch (Exception var21) {
               ChatSender.send(Text.translatable("screenshot.failure", new Object[]{var21.getMessage()}));
            } finally {
               GlStateManager._pixelStore(3333, var5);
               GlStateManager._glBindFramebuffer(36008, var3);
               GlStateManager._glBindFramebuffer(36009, var4);
               GlStateManager._disableScissorTest();
               GlStateManager.clearGlErrors();
            }
         }
      }
   }

   private static File a(File file) {
      String var1 = Util.getFormattedCurrentTime();
      File var2 = new File(file, var1 + ".png");

      for (int var3 = 2; var2.exists(); var3++) {
         var2 = new File(file, var1 + "_" + var3 + ".png");
      }

      return var2;
   }
}
