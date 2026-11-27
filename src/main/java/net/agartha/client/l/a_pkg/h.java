package net.agartha.client.l.a_pkg;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import net.agartha.client.mixin.AccessorPostChain;
import net.agartha.client.mixin.AccessorPostPass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;

@Environment(EnvType.CLIENT)
final class h {
   private static final int SIZE = 3;
   private static final int USAGE = 130;
   private final String block;
   private final GpuBuffer[] buffers = new GpuBuffer[3];
   private PostEffectProcessor owner;
   private int index;

   h(String string) {
      this.block = string;
   }

   GpuBuffer next(PostEffectProcessor postEffectProcessor) {
      PostEffectPass var2 = passWith(postEffectProcessor, this.block);
      if (var2 == null) {
         return null;
      } else {
         if (postEffectProcessor != this.owner) {
            this.release();
            this.owner = postEffectProcessor;
         }

         Map<String, GpuBuffer> var3 = ((AccessorPostPass)var2).agartha$getCustomUniforms();
         GpuBuffer var4 = (GpuBuffer)var3.get(this.block);
         int var5 = this.index;
         this.index = (this.index + 1) % 3;
         GpuBuffer var6 = this.buffers[var5];
         if (var6 == null || var6.isClosed()) {
            long var7 = var4.size();
            var6 = RenderSystem.getDevice().createBuffer(() -> "agartha/motion-blur/" + this.block, 130, var7);
            this.buffers[var5] = var6;
         }

         GpuBuffer var9 = var3.put(this.block, var6);
         if (var9 != null && !this.a(var9)) {
            var9.close();
         }

         return var6;
      }
   }

   void release() {
      for (int var1 = 0; var1 < 3; var1++) {
         if (this.buffers[var1] != null) {
            this.buffers[var1].close();
            this.buffers[var1] = null;
         }
      }

      this.owner = null;
      this.index = 0;
   }

   private boolean a(GpuBuffer gpuBuffer) {
      for (GpuBuffer var5 : this.buffers) {
         if (var5 == gpuBuffer) {
            return true;
         }
      }

      return false;
   }

   static PostEffectPass passWith(PostEffectProcessor postEffectProcessor, String string) {
      for (PostEffectPass var3 : ((AccessorPostChain)postEffectProcessor).agartha$getPasses()) {
         if (((AccessorPostPass)var3).agartha$getCustomUniforms().containsKey(string)) {
            return var3;
         }
      }

      return null;
   }
}
