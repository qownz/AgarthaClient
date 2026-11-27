package net.agartha.client.n;

import com.mojang.blaze3d.opengl.GlStateManager;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import net.agartha.client.AgarthaClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public final class a_o {
   private static final net.agartha.client.n.a_o INSTANCE = new net.agartha.client.n.a_o();
   private static final int MAX_SIDE = 128;
   private static final ExecutorService DECODER = Executors.newSingleThreadExecutor(runnable -> {
      Thread var1 = new Thread(runnable, "agartha-spotify-art");
      var1.setDaemon(true);
      var1.setPriority(1);
      return var1;
   });
   private final AtomicReference<net.agartha.client.n.a_o.a> finished = new AtomicReference<>();
   private int textureId = -1;
   private int width;
   private int height;
   private long boundArtHash = Long.MIN_VALUE;
   private long requestedArtHash = Long.MIN_VALUE;
   private String lastError = "";
   private boolean ready;
   private boolean needsGlValidation;

   private a_o() {
   }

   public static net.agartha.client.n.a_o getInstance() {
      return INSTANCE;
   }

   public boolean isReady() {
      return this.ready;
   }

   public int textureId() {
      return this.textureId;
   }

   public int width() {
      return this.width;
   }

   public int height() {
      return this.height;
   }

   public String lastError() {
      return this.lastError;
   }

   public synchronized void invalidate() {
      this.b();
      this.width = 0;
      this.height = 0;
      this.boundArtHash = Long.MIN_VALUE;
      this.requestedArtHash = Long.MIN_VALUE;
      this.finished.set(null);
      this.needsGlValidation = false;
   }

   public void markNeedsValidation() {
      this.needsGlValidation = true;
   }

   public synchronized void syncFrom(c arg) {
      this.syncFrom(arg, false);
   }

   public synchronized void syncFrom(c arg, boolean bl) {
      if (arg != null && arg.present() && arg.albumArtBytes() != null && arg.albumArtBytes().length != 0) {
         if (this.textureId >= 0 && this.needsGlValidation) {
            this.needsGlValidation = false;
            if (!GL11.glIsTexture(this.textureId)) {
               AgarthaClientMod.LOGGER.info("[Agartha] Spotify album art GL texture lost; re-uploading");
               this.textureId = -1;
               this.ready = false;
               this.boundArtHash = Long.MIN_VALUE;
               this.requestedArtHash = Long.MIN_VALUE;
            }
         }

         long var3 = arg.artHash();
         if (var3 != this.requestedArtHash && var3 != this.boundArtHash) {
            this.requestedArtHash = var3;
            byte[] var5 = Arrays.copyOf(arg.albumArtBytes(), arg.albumArtBytes().length);
            DECODER.execute(() -> this.finished.set(a(var3, var5)));
         }

         net.agartha.client.n.a_o.a var6 = this.finished.getAndSet(null);
         if (var6 != null && var6.hash() == this.requestedArtHash) {
            if (var6.rgba() == null) {
               this.lastError = var6.error();
               this.boundArtHash = var6.hash();
               if (bl) {
                  AgarthaClientMod.LOGGER.warn("[Agartha] Spotify album art failed: {}", var6.error());
               }
            } else {
               this.a(var6, bl);
            }
         }
      } else {
         if (this.textureId >= 0) {
            this.b();
            this.width = 0;
            this.height = 0;
            this.boundArtHash = Long.MIN_VALUE;
         }

         this.requestedArtHash = Long.MIN_VALUE;
         if (bl && arg != null && arg.present()) {
            this.lastError = "no album art bytes from SMTC";
         }
      }
   }

   private static net.agartha.client.n.a_o.a a(long l, byte[] bs) {
      try {
         BufferedImage var3 = ImageIO.read(new ByteArrayInputStream(bs));
         if (var3 == null) {
            return new net.agartha.client.n.a_o.a(l, 0, 0, null, "ImageIO.read returned null (magic=" + a(bs) + ", len=" + bs.length + ")");
         } else {
            int var4 = var3.getWidth();
            int var5 = var3.getHeight();
            if (var4 > 0 && var5 > 0) {
               float var6 = Math.min(1.0F, 128.0F / Math.max(var4, var5));
               int var7 = Math.max(1, Math.round(var4 * var6));
               int var8 = Math.max(1, Math.round(var5 * var6));
               BufferedImage var9 = new BufferedImage(var7, var8, 2);
               Graphics2D var10 = var9.createGraphics();

               try {
                  var10.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                  var10.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                  var10.drawImage(var3, 0, 0, var7, var8, null);
               } finally {
                  var10.dispose();
               }

               int[] var11 = ((DataBufferInt)var9.getRaster().getDataBuffer()).getData();
               byte[] var12 = new byte[var7 * var8 * 4];
               int var13 = 0;

               for (int var14 = 0; var13 < var11.length; var14 += 4) {
                  int var15 = var11[var13];
                  var12[var14] = (byte)(var15 >>> 16);
                  var12[var14 + 1] = (byte)(var15 >>> 8);
                  var12[var14 + 2] = (byte)var15;
                  var12[var14 + 3] = (byte)(var15 >>> 24);
                  var13++;
               }

               return new net.agartha.client.n.a_o.a(l, var7, var8, var12, "");
            } else {
               return new net.agartha.client.n.a_o.a(l, 0, 0, null, "decoded image has invalid size " + var4 + "x" + var5);
            }
         }
      } catch (Throwable var19) {
         return new net.agartha.client.n.a_o.a(l, 0, 0, null, "decode threw: " + var19.getClass().getSimpleName() + ": " + var19.getMessage());
      }
   }

   private void a(net.agartha.client.n.a_o.a arg, boolean bl) {
      int var3 = arg.width();
      int var4 = arg.height();
      ByteBuffer var5 = MemoryUtil.memAlloc(arg.rgba().length);

      try {
         var5.put(arg.rgba()).flip();
         int var6 = GlStateManager._genTexture();
         GlStateManager._bindTexture(var6);
         a();
         GL11.glTexParameteri(3553, 10241, 9987);
         GL11.glTexParameteri(3553, 10240, 9729);
         GL11.glTexParameteri(3553, 10242, 33071);
         GL11.glTexParameteri(3553, 10243, 33071);
         GL11.glTexImage2D(3553, 0, 32856, var3, var4, 0, 6408, 5121, var5);
         GL30.glGenerateMipmap(3553);
         GlStateManager._bindTexture(0);
         this.b();
         this.textureId = var6;
         this.width = var3;
         this.height = var4;
         this.boundArtHash = arg.hash();
         this.ready = true;
         this.lastError = "";
         if (bl) {
            AgarthaClientMod.LOGGER.info("[Agartha] Spotify album art OK {}x{} gl={}", new Object[]{var3, var4, var6});
         }
      } finally {
         MemoryUtil.memFree(var5);
      }
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

   private static String a(byte[] bs) {
      if (bs != null && bs.length != 0) {
         int var1 = Math.min(8, bs.length);
         StringBuilder var2 = new StringBuilder(var1 * 2);

         for (int var3 = 0; var3 < var1; var3++) {
            var2.append(String.format("%02X", bs[var3] & 255));
         }

         return var2.toString();
      } else {
         return "empty";
      }
   }

   private void b() {
      if (this.textureId >= 0) {
         if (GL11.glIsTexture(this.textureId)) {
            GlStateManager._deleteTexture(this.textureId);
         }

         this.textureId = -1;
      }

      this.ready = false;
   }

   @Environment(EnvType.CLIENT)
   private record a(long hash, int width, int height, byte[] rgba, String error) {
   }
}
