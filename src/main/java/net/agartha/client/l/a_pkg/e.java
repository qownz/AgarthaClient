package net.agartha.client.l.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class e {
   private static boolean loading;

   private e() {
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
