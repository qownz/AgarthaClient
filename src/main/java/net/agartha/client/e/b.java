package net.agartha.client.e;

import com.mojang.blaze3d.opengl.GlStateManager;
import imgui.ImDrawList;
import java.io.InputStream;
import java.nio.ByteBuffer;
import net.agartha.client.AgarthaClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public final class b {
   private static final b INSTANCE = new b();
   private static final String RESOURCE = "/assets/agartha/claw.png";
   private static final long RETRY_COOLDOWN_MS = 2000L;
   private int textureId = -1;
   private int width;
   private int height;
   private long nextRetryAtMs;

   private b() {
   }

   public static b getInstance() {
      return INSTANCE;
   }

   public boolean isReady() {
      this.a();
      return this.textureId >= 0;
   }

   public int textureId() {
      return this.textureId;
   }

   public float aspect() {
      return this.height <= 0 ? 0.7F : (float)this.width / this.height;
   }

   public synchronized void invalidate() {
      if (this.textureId >= 0 && GL11.glIsTexture(this.textureId)) {
         GlStateManager._deleteTexture(this.textureId);
      }

      this.textureId = -1;
      this.nextRetryAtMs = 0L;
   }

   public static float draw(ImDrawList imDrawList, float f, float g, float h, int i) {
      b var5 = getInstance();
      if (!var5.isReady()) {
         return c.claw(imDrawList, f, g, h, i);
      } else {
         float var6 = h * var5.aspect();
         imDrawList.addImage(var5.textureId, f, g, f + var6, g + h, 0.0F, 0.0F, 1.0F, 1.0F, c.f(i));
         return var6;
      }
   }

   private synchronized void a() {
      if (this.textureId >= 0) {
         if (GL11.glIsTexture(this.textureId)) {
            return;
         }

         this.textureId = -1;
      }

      long var1 = System.currentTimeMillis();
      if (var1 >= this.nextRetryAtMs) {
         if (!this.b()) {
            this.nextRetryAtMs = var1 + 2000L;
         }
      }
   }

   private boolean b() {
      try {
         boolean var23;
         try (InputStream var1 = b.class.getResourceAsStream("/assets/agartha/claw.png")) {
            NativeImage var2;
            label176: {
               if (var1 == null) {
                  return false;
               }

               var2 = NativeImage.read(var1);

               try {
                  int var3 = var2.getWidth();
                  int var4 = var2.getHeight();
                  int var5 = var3 * var4 * 4;
                  ByteBuffer var6 = MemoryUtil.memAlloc(var5);

                  try {
                     MemoryUtil.memCopy(var2.imageId(), MemoryUtil.memAddress(var6), var5);
                     var6.limit(var5);
                     int var7 = GlStateManager._genTexture();
                     GlStateManager._bindTexture(var7);
                     GL11.glPixelStorei(3317, 1);
                     GL11.glPixelStorei(3314, 0);
                     GL11.glPixelStorei(3316, 0);
                     GL11.glPixelStorei(3315, 0);
                     GL11.glTexParameteri(3553, 10241, 9987);
                     GL11.glTexParameteri(3553, 10240, 9729);
                     GL11.glTexParameteri(3553, 10242, 33071);
                     GL11.glTexParameteri(3553, 10243, 33071);
                     GL11.glTexImage2D(3553, 0, 32856, var3, var4, 0, 6408, 5121, var6);
                     GL30.glGenerateMipmap(3553);
                     GlStateManager._bindTexture(0);
                     if (GL11.glIsTexture(var7)) {
                        this.textureId = var7;
                        this.width = var3;
                        this.height = var4;
                        AgarthaClientMod.LOGGER.info("[Agartha] Claw logo ready {}x{} (gl={})", new Object[]{var3, var4, var7});
                        var23 = true;
                        break label176;
                     }

                     var23 = false;
                  } finally {
                     MemoryUtil.memFree(var6);
                  }
               } catch (Throwable var19) {
                  if (var2 != null) {
                     try {
                        var2.close();
                     } catch (Throwable var17) {
                        var19.addSuppressed(var17);
                     }
                  }

                  throw var19;
               }

               if (var2 != null) {
                  var2.close();
               }

               return var23;
            }

            if (var2 != null) {
               var2.close();
            }
         }

         return var23;
      } catch (Exception var21) {
         AgarthaClientMod.LOGGER.warn("[Agartha] Failed to load claw logo", var21);
         return false;
      }
   }
}
