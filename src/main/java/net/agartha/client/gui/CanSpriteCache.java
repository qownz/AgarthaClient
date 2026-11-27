package net.agartha.client.gui;

import com.mojang.blaze3d.opengl.GlStateManager;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Map;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.module.impl.client.MonsterModeModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public final class CanSpriteCache {
   private static final CanSpriteCache INSTANCE = new CanSpriteCache("can");
   private static final CanSpriteCache LEGACY = new CanSpriteCache("legacy/can");
   private static final long RETRY_COOLDOWN_MS = 2000L;
   private final Map<Theme, CanSpriteCache.a> entries = new EnumMap<>(Theme.class);
   private final Map<Theme, Long> retryAt = new EnumMap<>(Theme.class);
   private final String folder;

   public static CanSpriteCache getInstance() {
      return INSTANCE;
   }

   public static CanSpriteCache legacy() {
      return LEGACY;
   }

   public static CanSpriteCache current() {
      return MonsterModeModule.isActive() ? LEGACY : INSTANCE;
   }

   public static void invalidateAll() {
      INSTANCE.invalidate();
      LEGACY.invalidate();
   }

   private CanSpriteCache(String string) {
      this.folder = string;
   }

   public boolean isReady(Theme arg) {
      this.ensureLoaded(arg);
      CanSpriteCache.a var2 = this.entries.get(arg);
      return var2 != null && GL11.glIsTexture(var2.id);
   }

   public int textureId(Theme arg) {
      CanSpriteCache.a var2 = this.entries.get(arg);
      return var2 == null ? -1 : var2.id;
   }

   public float aspect(Theme arg) {
      CanSpriteCache.a var2 = this.entries.get(arg);
      return var2 != null && var2.height > 0 ? (float)var2.width / var2.height : 0.4F;
   }

   public synchronized void invalidate() {
      for (CanSpriteCache.a var2 : this.entries.values()) {
         if (GL11.glIsTexture(var2.id)) {
            GlStateManager._deleteTexture(var2.id);
         }
      }

      this.entries.clear();
      this.retryAt.clear();
   }

   public synchronized void ensureLoaded(Theme arg) {
      CanSpriteCache.a var2 = this.entries.get(arg);
      if (var2 != null) {
         if (GL11.glIsTexture(var2.id)) {
            return;
         }

         this.entries.remove(arg);
      }

      long var3 = System.currentTimeMillis();
      if (var3 >= this.retryAt.getOrDefault(arg, 0L)) {
         CanSpriteCache.a var5 = this.a(arg);
         if (var5 == null) {
            this.retryAt.put(arg, var3 + 2000L);
         } else {
            this.entries.put(arg, var5);
            this.retryAt.remove(arg);
         }
      }
   }

   private CanSpriteCache.a a(Theme arg) {
      String var2 = "/assets/agartha/" + this.folder + "/" + arg.displayName().toLowerCase() + ".png";

      try {
         CanSpriteCache.a var25;
         try (InputStream var3 = CanSpriteCache.class.getResourceAsStream(var2)) {
            NativeImage var4;
            label179: {
               if (var3 == null) {
                  return null;
               }

               var4 = NativeImage.read(var3);

               try {
                  int var5 = var4.getWidth();
                  int var6 = var4.getHeight();
                  int var7 = var5 * var6 * 4;
                  ByteBuffer var8 = MemoryUtil.memAlloc(var7);

                  try {
                     MemoryUtil.memCopy(var4.imageId(), MemoryUtil.memAddress(var8), var7);
                     var8.limit(var7);
                     a(var8, var5, var6);
                     int var9 = GlStateManager._genTexture();
                     GlStateManager._bindTexture(var9);
                     GL11.glPixelStorei(3317, 1);
                     GL11.glPixelStorei(3314, 0);
                     GL11.glPixelStorei(3316, 0);
                     GL11.glPixelStorei(3315, 0);
                     GL11.glTexParameteri(3553, 10241, 9987);
                     GL11.glTexParameteri(3553, 10240, 9729);
                     GL11.glTexParameteri(3553, 10242, 33071);
                     GL11.glTexParameteri(3553, 10243, 33071);
                     GL11.glTexParameterf(3553, 34049, -0.35F);
                     GL11.glTexImage2D(3553, 0, 32856, var5, var6, 0, 6408, 5121, var8);
                     GL30.glGenerateMipmap(3553);
                     GlStateManager._bindTexture(0);
                     if (GL11.glIsTexture(var9)) {
                        AgarthaClientMod.LOGGER.info("[Agartha] Can sprite '{}' ready {}x{} (gl={})", new Object[]{arg.displayName(), var5, var6, var9});
                        var25 = new CanSpriteCache.a(var9, var5, var6);
                        break label179;
                     }

                     var25 = null;
                  } finally {
                     MemoryUtil.memFree(var8);
                  }
               } catch (Throwable var21) {
                  if (var4 != null) {
                     try {
                        var4.close();
                     } catch (Throwable var19) {
                        var21.addSuppressed(var19);
                     }
                  }

                  throw var21;
               }

               if (var4 != null) {
                  var4.close();
               }

               return var25;
            }

            if (var4 != null) {
               var4.close();
            }
         }

         return var25;
      } catch (Exception var23) {
         AgarthaClientMod.LOGGER.warn("[Agartha] Can sprite '{}' failed to load", arg.displayName(), var23);
         return null;
      }
   }

   private static void a(ByteBuffer byteBuffer, int i, int j) {
      boolean[] var3 = new boolean[i * j];

      for (int var4 = 0; var4 < i * j; var4++) {
         var3[var4] = (byteBuffer.get(var4 * 4 + 3) & 255) > 0;
      }

      int[] var20 = new int[]{-1, 1, 0, 0, -1, 1, -1, 1};
      int[] var5 = new int[]{0, 0, -1, 1, -1, -1, 1, 1};

      for (int var6 = 0; var6 < 16; var6++) {
         boolean[] var7 = (boolean[])var3.clone();
         boolean var8 = false;

         for (int var9 = 0; var9 < j; var9++) {
            for (int var10 = 0; var10 < i; var10++) {
               int var11 = var9 * i + var10;
               if (!var3[var11]) {
                  int var12 = 0;
                  int var13 = 0;
                  int var14 = 0;
                  int var15 = 0;

                  for (int var16 = 0; var16 < 8; var16++) {
                     int var17 = var10 + var20[var16];
                     int var18 = var9 + var5[var16];
                     if (var17 >= 0 && var18 >= 0 && var17 < i && var18 < j) {
                        int var19 = var18 * i + var17;
                        if (var3[var19]) {
                           var12 += byteBuffer.get(var19 * 4) & 255;
                           var13 += byteBuffer.get(var19 * 4 + 1) & 255;
                           var14 += byteBuffer.get(var19 * 4 + 2) & 255;
                           var15++;
                        }
                     }
                  }

                  if (var15 != 0) {
                     byteBuffer.put(var11 * 4, (byte)(var12 / var15));
                     byteBuffer.put(var11 * 4 + 1, (byte)(var13 / var15));
                     byteBuffer.put(var11 * 4 + 2, (byte)(var14 / var15));
                     var7[var11] = true;
                     var8 = true;
                  }
               }
            }
         }

         var3 = var7;
         if (!var8) {
            break;
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private record a(int id, int width, int height) {
   }
}
