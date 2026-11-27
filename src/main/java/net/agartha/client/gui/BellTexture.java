package net.agartha.client.gui;

import com.mojang.blaze3d.opengl.GlStateManager;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Optional;
import net.agartha.client.AgarthaClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public final class BellTexture {
   private static final BellTexture INSTANCE = new BellTexture();
   private static final String RESOURCE = "/assets/agartha/bell.png";
   private static final long RETRY_COOLDOWN_MS = 2000L;
   private int textureId = -1;
   private int width;
   private int height;
   private long nextRetryAtMs;
   private boolean ready;

   private BellTexture() {
   }

   public static BellTexture getInstance() {
      return INSTANCE;
   }

   public boolean isReady() {
      this.a();
      return this.ready;
   }

   public int textureId() {
      this.a();
      return this.textureId;
   }

   public int width() {
      this.a();
      return this.width;
   }

   public int height() {
      this.a();
      return this.height;
   }

   public synchronized void invalidate() {
      this.c();
      this.width = 0;
      this.height = 0;
      this.ready = false;
      this.nextRetryAtMs = 0L;
   }

   private synchronized void a() {
      if (!this.ready || this.textureId < 0 || !GL11.glIsTexture(this.textureId)) {
         if (this.ready) {
            this.c();
         }

         long var1 = System.currentTimeMillis();
         if (var1 >= this.nextRetryAtMs) {
            if (!this.b()) {
               this.nextRetryAtMs = var1 + 2000L;
            } else {
               this.nextRetryAtMs = 0L;
            }
         }
      }
   }

   private boolean b() {
      try {
         boolean var4;
         try (InputStream var1 = d()) {
            if (var1 == null) {
               AgarthaClientMod.LOGGER.warn("[Agartha] Notification bell missing at {}", "/assets/agartha/bell.png");
               return false;
            }

            BufferedImage var2 = ImageLoader.readImage(var1);
            int var3 = ImageLoader.uploadArgbImage(var2);
            if (var3 < 0) {
               AgarthaClientMod.LOGGER.warn("[Agartha] Notification bell upload produced invalid GL texture");
               return false;
            }

            this.textureId = var3;
            this.width = var2.getWidth();
            this.height = var2.getHeight();
            this.ready = true;
            AgarthaClientMod.LOGGER.info("[Agartha] Notification bell ready {}x{} (gl={})", new Object[]{this.width, this.height, var3});
            var4 = true;
         }

         return var4;
      } catch (Exception var7) {
         AgarthaClientMod.LOGGER.warn("[Agartha] Failed to load notification bell texture", var7);
         this.c();
         return false;
      }
   }

   private void c() {
      if (this.textureId >= 0) {
         if (GL11.glIsTexture(this.textureId)) {
            GlStateManager._deleteTexture(this.textureId);
         }

         this.textureId = -1;
      }

      this.ready = false;
   }

   private static InputStream d() {
      InputStream var0 = BellTexture.class.getResourceAsStream("/assets/agartha/bell.png");
      if (var0 != null) {
         return var0;
      } else {
         var0 = AgarthaClientMod.class.getResourceAsStream("/assets/agartha/bell.png");
         if (var0 != null) {
            return var0;
         } else {
            MinecraftClient var1 = MinecraftClient.getInstance();
            if (var1 != null) {
               try {
                  Optional var2 = var1.getResourceManager().getResource(Identifier.of("agartha", "bell.png"));
                  if (var2.isPresent()) {
                     return ((Resource)var2.get()).getInputStream();
                  }
               } catch (Exception var3) {
               }
            }

            return null;
         }
      }
   }
}
