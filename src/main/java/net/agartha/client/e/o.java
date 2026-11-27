package net.agartha.client.e;

import com.mojang.blaze3d.opengl.GlStateManager;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.agartha.client.AgarthaClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public final class o {
   private static final int UPSCALE = 4;
   private static final Map<Identifier, Integer> TEXTURES = new HashMap<>();
   private static final Set<Identifier> MISSING = new HashSet<>();

   private o() {
   }

   public static int textureId(RegistryEntry<StatusEffect> registryEntry) {
      Identifier var1 = InGameHud.getEffectTexture(registryEntry);
      Integer var2 = TEXTURES.get(var1);
      if (var2 != null) {
         if (GL11.glIsTexture(var2)) {
            return var2;
         }

         TEXTURES.remove(var1);
      }

      if (MISSING.contains(var1)) {
         return -1;
      } else {
         int var3 = a(var1);
         if (var3 < 0) {
            MISSING.add(var1);
         } else {
            TEXTURES.put(var1, var3);
         }

         return var3;
      }
   }

   public static void invalidate() {
      for (int var1 : TEXTURES.values()) {
         if (GL11.glIsTexture(var1)) {
            GL11.glDeleteTextures(var1);
         }
      }

      TEXTURES.clear();
      MISSING.clear();
   }

   private static int a(Identifier identifier) {
      Identifier var1 = identifier.withPath(string -> "textures/" + string + ".png");
      Resource var2 = (Resource)MinecraftClient.getInstance().getResourceManager().getResource(var1).orElse(null);
      if (var2 == null) {
         return -1;
      } else {
         try {
            int var28;
            try (InputStream var3 = var2.getInputStream()) {
               NativeImage var4 = NativeImage.read(var3);

               label194: {
                  byte var6;
                  try {
                     int var5 = Math.min(var4.getWidth(), var4.getHeight());
                     if (var5 > 0) {
                        int[] var26 = var4.copyPixelsAbgr();
                        int var7 = var4.getWidth();
                        int var8 = var5 * 4;
                        IntBuffer var9 = MemoryUtil.memAllocInt(var8 * var8);

                        try {
                           for (int var10 = 0; var10 < var8; var10++) {
                              var28 = var10 / 4 * var7;

                              for (int var12 = 0; var12 < var8; var12++) {
                                 var9.put(var10 * var8 + var12, var26[var28 + var12 / 4]);
                              }
                           }

                           int var27 = GlStateManager._genTexture();
                           GlStateManager._bindTexture(var27);
                           GL11.glPixelStorei(3317, 1);
                           GL11.glPixelStorei(3314, 0);
                           GL11.glPixelStorei(3316, 0);
                           GL11.glPixelStorei(3315, 0);
                           GL11.glTexParameteri(3553, 10241, 9987);
                           GL11.glTexParameteri(3553, 10240, 9729);
                           GL11.glTexParameteri(3553, 10242, 33071);
                           GL11.glTexParameteri(3553, 10243, 33071);
                           GL11.glTexImage2D(3553, 0, 32856, var8, var8, 0, 6408, 5121, var9);
                           GL30.glGenerateMipmap(3553);
                           GlStateManager._bindTexture(0);
                           var28 = GL11.glIsTexture(var27) ? var27 : -1;
                           break label194;
                        } finally {
                           MemoryUtil.memFree(var9);
                        }
                     }

                     var6 = -1;
                  } catch (Throwable var23) {
                     if (var4 != null) {
                        try {
                           var4.close();
                        } catch (Throwable var21) {
                           var23.addSuppressed(var21);
                        }
                     }

                     throw var23;
                  }

                  if (var4 != null) {
                     var4.close();
                  }

                  return var6;
               }

               if (var4 != null) {
                  var4.close();
               }
            }

            return var28;
         } catch (Exception var25) {
            AgarthaClientMod.LOGGER.warn("[Agartha] Effect icon {} could not be loaded", var1, var25);
            return -1;
         }
      }
   }
}
