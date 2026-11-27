package net.agartha.client.module.impl.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkinTextureDownloader;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.AssetInfo.TextureAsset;

@Environment(EnvType.CLIENT)
public final class o extends net.agartha.client.module.b {
   private final net.agartha.client.module.a_pkg.l selfPlayerSetting = this.addSetting(new net.agartha.client.module.a_pkg.l("Self Player", ""));
   private final net.agartha.client.module.a_pkg.l othersPlayerSetting = this.addSetting(new net.agartha.client.module.a_pkg.l("Others Player", ""));
   public volatile TextureAsset selfBodyTexture;
   public volatile TextureAsset othersBodyTexture;
   private final AtomicInteger selfRequest = new AtomicInteger();
   private final AtomicInteger othersRequest = new AtomicInteger();
   private static final Map<String, TextureAsset> LOADED = new ConcurrentHashMap<>();
   private Object lastLevel;

   public o() {
      super("Skin Change", "Wear any player's skin client-side. Set the names, then press Apply.", net.agartha.client.module.c.MISC);
   }

   @net.agartha.client.d.f
   public void onTick(net.agartha.client.d.a_pkg.q arg) {
      ClientWorld var2 = MinecraftClient.getInstance().world;
      if (var2 != this.lastLevel) {
         this.lastLevel = var2;
         if (var2 != null) {
            this.applyNow();
         }
      }
   }

   @Override
   protected void onEnable() {
      this.applyNow();
   }

   @Override
   protected void onDisable() {
      this.selfRequest.incrementAndGet();
      this.othersRequest.incrementAndGet();
      this.selfBodyTexture = null;
      this.othersBodyTexture = null;
   }

   public void applyNow() {
      this.a(this.selfPlayerSetting.get().trim(), true);
      this.a(this.othersPlayerSetting.get().trim(), false);
   }

   private void a(String string, boolean bl) {
      AtomicInteger var3 = bl ? this.selfRequest : this.othersRequest;
      int var4 = var3.incrementAndGet();
      if (string.isEmpty()) {
         this.a(bl, null);
      } else {
         TextureAsset var5 = LOADED.get(string.toLowerCase(Locale.ROOT));
         if (var5 != null) {
            this.a(bl, var5);
         } else {
            this.a(string, bl, var3, var4);
         }
      }
   }

   private void a(boolean bl, TextureAsset textureAsset) {
      if (bl) {
         this.selfBodyTexture = textureAsset;
      } else {
         this.othersBodyTexture = textureAsset;
      }
   }

   private void a(String string, boolean bl, AtomicInteger atomicInteger, int i) {
      CompletableFuture.runAsync(() -> {
         try {
            String var5 = a(string);
            if (var5 == null || atomicInteger.get() != i) {
               return;
            }

            String var6 = b(var5);
            if (var6 == null || atomicInteger.get() != i) {
               return;
            }

            MinecraftClient var7 = MinecraftClient.getInstance();
            PlayerSkinTextureDownloader var8 = new PlayerSkinTextureDownloader(var7.getNetworkProxy(), var7.getTextureManager(), var7);
            Identifier var9 = Identifier.of("agartha", "skin/" + var5.toLowerCase(Locale.ROOT));
            Path var10 = var7.runDirectory.toPath().resolve("cache/agartha_skins");

            try {
               Files.createDirectories(var10);
            } catch (IOException var12) {
            }

            Path var11 = var10.resolve(var5 + ".png");
            var8.downloadAndRegisterTexture(var9, var11, var6, false).thenAccept(textureAsset -> {
               LOADED.put(string.toLowerCase(Locale.ROOT), textureAsset);
               if (this.isEnabled() && atomicInteger.get() == i) {
                  this.a(bl, textureAsset);
               }
            });
         } catch (Exception var13) {
         }
      });
   }

   private static String a(String string) throws Exception {
      HttpURLConnection var1 = c("https://api.mojang.com/users/profiles/minecraft/" + string);
      int var2 = var1.getResponseCode();
      if (var2 != 204 && var2 != 404) {
         String var7;
         try (BufferedReader var3 = new BufferedReader(new InputStreamReader(var1.getInputStream()))) {
            StringBuilder var4 = new StringBuilder();

            String var5;
            while ((var5 = var3.readLine()) != null) {
               var4.append(var5);
            }

            JsonObject var6 = JsonParser.parseString(var4.toString()).getAsJsonObject();
            var7 = var6.get("id").getAsString();
         }

         return var7;
      } else {
         return null;
      }
   }

   private static String b(String string) throws Exception {
      HttpURLConnection var1 = c("https://sessionserver.mojang.com/session/minecraft/profile/" + string + "?unsigned=false");
      if (var1.getResponseCode() != 200) {
         return null;
      } else {
         String var2;
         try (BufferedReader var3 = new BufferedReader(new InputStreamReader(var1.getInputStream()))) {
            StringBuilder var4 = new StringBuilder();

            String var5;
            while ((var5 = var3.readLine()) != null) {
               var4.append(var5);
            }

            var2 = var4.toString();
         }

         JsonObject var11 = JsonParser.parseString(var2).getAsJsonObject();

         for (JsonElement var13 : var11.getAsJsonArray("properties")) {
            JsonObject var6 = var13.getAsJsonObject();
            if ("textures".equals(var6.get("name").getAsString())) {
               String var7 = new String(Base64.getDecoder().decode(var6.get("value").getAsString()));
               JsonObject var8 = JsonParser.parseString(var7).getAsJsonObject().getAsJsonObject("textures");
               if (!var8.has("SKIN")) {
                  return null;
               }

               return var8.getAsJsonObject("SKIN").get("url").getAsString();
            }
         }

         return null;
      }
   }

   private static HttpURLConnection c(String string) throws Exception {
      HttpURLConnection var1 = (HttpURLConnection)URI.create(string).toURL().openConnection();
      var1.setConnectTimeout(5000);
      var1.setReadTimeout(5000);
      var1.setRequestProperty("User-Agent", "Agartha/1.0");
      return var1;
   }

   public TextureAsset getSkinTextureFor(int i) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      boolean var3 = var2.player != null && i == var2.player.getId();
      return var3 ? this.selfBodyTexture : this.othersBodyTexture;
   }

   public Identifier selfArmSkinPath() {
      if (!this.isEnabled()) {
         return null;
      } else {
         TextureAsset var1 = this.selfBodyTexture;
         return var1 != null ? var1.texturePath() : null;
      }
   }
}
