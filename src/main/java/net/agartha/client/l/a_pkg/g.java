package net.agartha.client.l.a_pkg;

import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.List;
import java.util.Map;
import net.agartha.client.mixin.AccessorPostPass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectPass.Sampler;
import net.minecraft.client.render.FramePass;
import net.minecraft.client.util.Handle;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
final class g implements Sampler {
   private final String samplerName;
   private Framebuffer target;

   g(String string) {
      this.samplerName = string;
   }

   g point(Framebuffer framebuffer) {
      this.target = framebuffer;
      return this;
   }

   boolean installInto(PostEffectPass postEffectPass) {
      List var2 = ((AccessorPostPass)postEffectPass).agartha$getInputs();

      for (int var3 = 0; var3 < var2.size(); var3++) {
         if (this.samplerName.equals(((Sampler)var2.get(var3)).samplerName())) {
            if (var2.get(var3) != this) {
               var2.set(var3, this);
            }

            return true;
         }
      }

      return false;
   }

   public void preRender(FramePass pass, Map<Identifier, Handle<Framebuffer>> internalTargets) {
   }

   public GpuTextureView getTexture(Map<Identifier, Handle<Framebuffer>> internalTargets) {
      return this.target.getColorAttachmentView();
   }

   public String samplerName() {
      return this.samplerName;
   }

   public boolean bilinear() {
      return false;
   }
}
