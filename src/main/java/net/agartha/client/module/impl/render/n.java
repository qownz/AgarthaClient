package net.agartha.client.module.impl.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.BufferUtils;

@Environment(EnvType.CLIENT)
final class n {
   private static final RenderPipeline PIPELINE = RenderPipeline.builder(new Snippet[0])
      .withLocation(Identifier.of("agartha", "pipeline/overlay_spread"))
      .withVertexShader("core/screenquad")
      .withFragmentShader(Identifier.of("agartha", "core/overlay_spread"))
      .withSampler("InSampler")
      .withUniform("Spread", UniformType.UNIFORM_BUFFER)
      .withoutBlend()
      .withDepthWrite(false)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withVertexFormat(VertexFormats.EMPTY, DrawMode.TRIANGLES)
      .build();
   private static final int USAGE = 136;
   private final String name;
   private final Supplier<String> passLabel;
   private SimpleFramebuffer across;
   private SimpleFramebuffer down;
   private GpuBuffer acrossConfig;
   private GpuBuffer downConfig;
   private GpuBufferSlice acrossSlice;
   private GpuBufferSlice downSlice;
   private ByteBuffer configData;

   n(String string) {
      this.name = string;
      this.passLabel = () -> string + " spread";
   }

   static boolean compiled(GpuDevice gpuDevice) {
      return gpuDevice.precompilePipeline(PIPELINE).isValid();
   }

   GpuTextureView spread(CommandEncoder commandEncoder, GpuTextureView gpuTextureView, int i, int j, float f) {
      int var6 = Math.max(1, (i + 1) / 2);
      int var7 = Math.max(1, (j + 1) / 2);
      if (this.across == null) {
         this.a(var6, var7);
      } else if (this.across.textureWidth != var6 || this.across.textureHeight != var7) {
         this.across.resize(var6, var7);
         this.down.resize(var6, var7);
      }

      float var8 = Math.max(0.75F, f / 6.0F);
      this.a(commandEncoder, gpuTextureView, this.across, this.acrossSlice, var8 / i, 0.0F);
      this.a(commandEncoder, this.across.getColorAttachmentView(), this.down, this.downSlice, 0.0F, var8 / j);
      return this.down.getColorAttachmentView();
   }

   private void a(
      CommandEncoder commandEncoder, GpuTextureView gpuTextureView, SimpleFramebuffer simpleFramebuffer, GpuBufferSlice gpuBufferSlice, float f, float g
   ) {
      this.configData.clear();
      this.configData.putFloat(0, f);
      this.configData.putFloat(4, g);
      commandEncoder.writeToBuffer(gpuBufferSlice, this.configData);
      RenderPass var7 = commandEncoder.createRenderPass(this.passLabel, simpleFramebuffer.getColorAttachmentView(), OptionalInt.empty());

      try {
         var7.setPipeline(PIPELINE);
         var7.setUniform("Spread", gpuBufferSlice);
         var7.bindTexture("InSampler", gpuTextureView, RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
         var7.draw(0, 3);
      } catch (Throwable var11) {
         if (var7 != null) {
            try {
               var7.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }
         }

         throw var11;
      }

      if (var7 != null) {
         var7.close();
      }
   }

   private void a(int i, int j) {
      GpuDevice var3 = RenderSystem.getDevice();
      this.across = new SimpleFramebuffer(this.name + " across", i, j, false);
      this.down = new SimpleFramebuffer(this.name + " down", i, j, false);
      this.acrossConfig = var3.createBuffer(() -> this.name + " across config", 136, 16L);
      this.downConfig = var3.createBuffer(() -> this.name + " down config", 136, 16L);
      this.acrossSlice = this.acrossConfig.slice();
      this.downSlice = this.downConfig.slice();
      this.configData = BufferUtils.createByteBuffer(16);
   }

   void release() {
      if (this.across != null) {
         this.across.delete();
         this.across = null;
      }

      if (this.down != null) {
         this.down.delete();
         this.down = null;
      }

      if (this.acrossConfig != null) {
         this.acrossConfig.close();
         this.acrossConfig = null;
      }

      if (this.downConfig != null) {
         this.downConfig.close();
         this.downConfig = null;
      }

      this.acrossSlice = null;
      this.downSlice = null;
      this.configData = null;
   }
}
