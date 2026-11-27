package net.agartha.client.gui;

import com.mojang.blaze3d.opengl.GlStateManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.imageio.ImageIO;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public final class ImageLoader {
   private ImageLoader() {
   }

   static BufferedImage readImage(InputStream inputStream) throws Exception {
      BufferedImage var1 = ImageIO.read(inputStream);
      if (var1 == null) {
         throw new IllegalStateException("ImageIO.read returned null");
      } else {
         int var2 = var1.getWidth();
         int var3 = var1.getHeight();
         if (var2 > 0 && var3 > 0) {
            if (var1.getType() == 2) {
               return var1;
            } else {
               BufferedImage var4 = new BufferedImage(var2, var3, 2);
               Graphics2D var5 = var4.createGraphics();

               try {
                  var5.drawImage(var1, 0, 0, null);
               } finally {
                  var5.dispose();
               }

               return var4;
            }
         } else {
            throw new IllegalStateException("Invalid image size " + var2 + "x" + var3);
         }
      }
   }

   static int uploadArgbImage(BufferedImage bufferedImage) {
      int var1 = bufferedImage.getWidth();
      int var2 = bufferedImage.getHeight();
      int var3 = var1 * var2 * 4;
      ByteBuffer var4 = MemoryUtil.memAlloc(var3);

      byte var12;
      try {
         var4.order(ByteOrder.nativeOrder());

         for (int var5 = 0; var5 < var2; var5++) {
            for (int var6 = 0; var6 < var1; var6++) {
               int var7 = bufferedImage.getRGB(var6, var5);
               var4.put((byte)(var7 >>> 16 & 0xFF));
               var4.put((byte)(var7 >>> 8 & 0xFF));
               var4.put((byte)(var7 & 0xFF));
               var4.put((byte)(var7 >>> 24 & 0xFF));
            }
         }

         var4.flip();
         int var11 = GlStateManager._genTexture();
         GlStateManager._bindTexture(var11);
         a();
         GL11.glTexParameteri(3553, 10241, 9729);
         GL11.glTexParameteri(3553, 10240, 9729);
         GL11.glTexParameteri(3553, 10242, 33071);
         GL11.glTexParameteri(3553, 10243, 33071);
         GL11.glTexImage2D(3553, 0, 32856, var1, var2, 0, 6408, 5121, var4);
         GlStateManager._bindTexture(0);
         if (GL11.glIsTexture(var11)) {
            return var11;
         }

         GlStateManager._deleteTexture(var11);
         var12 = -1;
      } finally {
         MemoryUtil.memFree(var4);
      }

      return var12;
   }

   private static void a() {
      GL11.glPixelStorei(3317, 1);
      GL11.glPixelStorei(3314, 0);
      GL11.glPixelStorei(3316, 0);
      GL11.glPixelStorei(3315, 0);
      GL11.glPixelStorei(3312, 0);
      GL11.glPixelStorei(3313, 0);
      GL11.glPixelStorei(32878, 0);
      GL11.glPixelStorei(32877, 0);
   }
}
