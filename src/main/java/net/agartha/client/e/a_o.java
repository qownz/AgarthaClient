package net.agartha.client.e;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.opengl.GlStateManager;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.agartha.client.AgarthaClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public final class a_o {
   private static final net.agartha.client.e.a_o INSTANCE = new net.agartha.client.e.a_o();
   private static final int FACE_SIZE = 8;
   private static final int OUTPUT_SIZE = 64;
   private static final long RETRY_BACKOFF_MS = 300000L;
   private final Map<UUID, net.agartha.client.e.a_o.a> cache = new ConcurrentHashMap<>();
   private final Map<UUID, BufferedImage> pendingFaces = new ConcurrentHashMap<>();
   private volatile UUID activeUuid;

   private a_o() {
   }

   public static net.agartha.client.e.a_o getInstance() {
      return INSTANCE;
   }

   public int textureIdFor(UUID uUID) {
      if (uUID == null) {
         return -1;
      } else {
         this.activeUuid = uUID;
         net.agartha.client.e.a_o.a var2 = this.cache.get(uUID);
         if (var2 != null && var2.ready && var2.textureId >= 0) {
            return var2.textureId;
         } else {
            if (var2 == null) {
               this.cache.put(uUID, new net.agartha.client.e.a_o.a());
               this.a(uUID);
            } else if (!var2.loading && System.currentTimeMillis() >= var2.retryAfter) {
               this.a(uUID);
            }

            return -1;
         }
      }
   }

   public void processPendingUploads() {
      if (!this.pendingFaces.isEmpty()) {
         for (UUID var3 : new ArrayList<>(this.pendingFaces.keySet())) {
            BufferedImage var4 = this.pendingFaces.remove(var3);
            if (var4 != null) {
               net.agartha.client.e.a_o.a var5 = this.cache.get(var3);
               if (var5 != null) {
                  try {
                     int var6 = y.uploadArgbImage(var4);
                     if (var6 >= 0) {
                        var5.textureId = var6;
                        var5.ready = true;
                     }
                  } finally {
                     var5.loading = false;
                  }
               }
            }
         }
      }
   }

   public void invalidate() {
      UUID var1 = this.activeUuid;
      MinecraftClient.getInstance().execute(() -> {
         synchronized (this) {
            this.pendingFaces.clear();

            for (net.agartha.client.e.a_o.a var4 : this.cache.values()) {
               var4.deleteGl();
            }

            this.cache.clear();
            if (var1 != null) {
               this.cache.put(var1, new net.agartha.client.e.a_o.a());
               this.a(var1);
            }
         }
      });
   }

   private void a(UUID uUID) {
      net.agartha.client.e.a_o.a var2 = this.cache.get(uUID);
      if (var2 != null && !var2.loading) {
         var2.loading = true;
         CompletableFuture.<BufferedImage>supplyAsync(() -> {
            try {
               String var1 = b(uUID);
               if (var1 == null) {
                  return null;
               } else {
                  BufferedImage var2x = a(var1);
                  return var2x == null ? null : a(var2x);
               }
            } catch (Exception var3) {
               AgarthaClientMod.LOGGER.debug("[Agartha] Avatar fetch failed for {}", uUID, var3);
               return null;
            }
         }).whenComplete((bufferedImage, throwable) -> {
            if (bufferedImage != null) {
               this.pendingFaces.put(uUID, bufferedImage);
            } else {
               net.agartha.client.e.a_o.a var4 = this.cache.get(uUID);
               if (var4 != null) {
                  var4.loading = false;
                  var4.retryAfter = System.currentTimeMillis() + 300000L;
               }
            }
         });
      }
   }

   private static String b(UUID uUID) throws Exception {
      String var1 = uUID.toString().replace("-", "");
      HttpURLConnection var2 = b("https://sessionserver.mojang.com/session/minecraft/profile/" + var1 + "?unsigned=false");
      if (var2.getResponseCode() != 200) {
         return null;
      } else {
         BufferedReader var4 = new BufferedReader(new InputStreamReader(var2.getInputStream(), StandardCharsets.UTF_8));

         String var3;
         try {
            StringBuilder var5 = new StringBuilder();

            String var6;
            while ((var6 = var4.readLine()) != null) {
               var5.append(var6);
            }

            var3 = var5.toString();
         } catch (Throwable var12) {
            try {
               var4.close();
            } catch (Throwable var11) {
               var12.addSuppressed(var11);
            }

            throw var12;
         }

         var4.close();
         JsonObject var13 = JsonParser.parseString(var3).getAsJsonObject();
         if (!var13.has("properties")) {
            return null;
         } else {
            for (JsonElement var7 : var13.getAsJsonArray("properties")) {
               JsonObject var8 = var7.getAsJsonObject();
               if ("textures".equals(var8.get("name").getAsString())) {
                  String var9 = new String(Base64.getDecoder().decode(var8.get("value").getAsString()));
                  JsonObject var10 = JsonParser.parseString(var9).getAsJsonObject().getAsJsonObject("textures");
                  if (var10.has("SKIN")) {
                     return var10.getAsJsonObject("SKIN").get("url").getAsString();
                  }
               }
            }

            return null;
         }
      }
   }

   private static BufferedImage a(String string) throws Exception {
      HttpURLConnection var1 = b(string);
      if (var1.getResponseCode() != 200) {
         return null;
      } else {
         BufferedImage var3;
         try (InputStream var2 = var1.getInputStream()) {
            var3 = y.readImage(var2);
         }

         return var3;
      }
   }

   private static BufferedImage a(BufferedImage bufferedImage) {
      int var1 = Math.max(1, bufferedImage.getWidth() / 64);
      int var2 = 8 * var1;
      BufferedImage var3 = new BufferedImage(var2, var2, 2);
      Graphics2D var4 = var3.createGraphics();

      try {
         var4.drawImage(bufferedImage, 0, 0, var2, var2, 8 * var1, 8 * var1, 16 * var1, 16 * var1, null);
         var4.drawImage(bufferedImage, 0, 0, var2, var2, 40 * var1, 8 * var1, 48 * var1, 16 * var1, null);
      } finally {
         var4.dispose();
      }

      BufferedImage var5 = new BufferedImage(64, 64, 2);
      Graphics2D var6 = var5.createGraphics();

      try {
         var6.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
         var6.drawImage(var3.getScaledInstance(64, 64, 2), 0, 0, null);
      } finally {
         var6.dispose();
      }

      return var5;
   }

   private static HttpURLConnection b(String string) throws Exception {
      HttpURLConnection var1 = (HttpURLConnection)URI.create(string).toURL().openConnection();
      var1.setConnectTimeout(8000);
      var1.setReadTimeout(8000);
      var1.setRequestProperty("User-Agent", "Agartha/1.0");
      return var1;
   }

   @Environment(EnvType.CLIENT)
   private static final class a {
      int textureId = -1;
      boolean ready;
      boolean loading;
      long retryAfter;

      void deleteGl() {
         if (this.textureId >= 0 && GL11.glIsTexture(this.textureId)) {
            GlStateManager._deleteTexture(this.textureId);
         }

         this.textureId = -1;
         this.ready = false;
         this.loading = false;
         this.retryAfter = 0L;
      }
   }
}
