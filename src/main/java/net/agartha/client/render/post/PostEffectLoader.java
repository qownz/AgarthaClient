package net.agartha.client.render.post;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class PostEffectLoader {
   private static boolean loading;

   private PostEffectLoader() {
   }

   public static boolean isLoading() {
      return loading;
   }

   public static PostEffectProcessor load(Identifier identifier) {
      loading = true;

      PostEffectProcessor var1;
      try {
         var1 = MinecraftClient.getInstance().getShaderLoader().loadPostEffect(identifier, DefaultFramebufferSet.MAIN_ONLY);
      } finally {
         loading = false;
      }

      return var1;
   }
}
