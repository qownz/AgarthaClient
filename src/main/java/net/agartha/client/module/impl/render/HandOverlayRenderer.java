package net.agartha.client.module.impl.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import java.util.function.Supplier;
import net.agartha.client.AgarthaClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.BufferUtils;

@Environment(EnvType.CLIENT)
public final class HandOverlayRenderer {
   private static final RenderPipeline PIPELINE = RenderPipeline.builder(new Snippet[0])
      .withLocation(Identifier.of("agartha", "pipeline/hand_overlay"))
      .withVertexShader("core/screenquad")
      .withFragmentShader(Identifier.of("agartha", "core/hand_overlay"))
      .withSampler("CaptureSampler")
      .withSampler("HaloSampler")
      .withUniform("Overlay", UniformType.UNIFORM_BUFFER)
      .withBlend(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA)
      .withColorWrite(true, false)
      .withDepthWrite(false)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withVertexFormat(VertexFormats.EMPTY, DrawMode.TRIANGLES)
      .build();
   private static final int USAGE = 136;
   private static final Supplier<String> PASS_LABEL = () -> "Agartha hand overlay";
   private static final MethodHandle SHADER_PACK = b();
   private static SimpleFramebuffer hand;
   private static BlurSpreadPass blur;
   private static GpuBuffer config;
   private static GpuBufferSlice configSlice;
   private static ByteBuffer configData;
   private static boolean capturing;
   private static boolean invalidLogged;
   private static boolean shadersReady;

   private HandOverlayRenderer() {
   }

   static boolean begin() {
      if (!capturing && RenderSystem.outputColorTextureOverride == null && RenderSystem.outputDepthTextureOverride == null) {
         MinecraftClient var0 = MinecraftClient.getInstance();
         Framebuffer var1 = var0.getFramebuffer();
         if (var0.world != null
            && !var0.gameRenderer.isRenderingPanorama()
            && var1.getColorAttachment() != null
            && var1.textureWidth > 0
            && var1.textureHeight > 0
            && !a()) {
            GpuDevice var2 = RenderSystem.getDevice();
            if (!a(var2)) {
               return false;
            } else {
               if (hand == null) {
                  a(var1.textureWidth, var1.textureHeight);
               } else if (hand.textureWidth != var1.textureWidth || hand.textureHeight != var1.textureHeight) {
                  hand.resize(var1.textureWidth, var1.textureHeight);
               }

               var2.createCommandEncoder().clearColorAndDepthTextures(hand.getColorAttachment(), 0, hand.getDepthAttachment(), 1.0);
               RenderSystem.outputColorTextureOverride = hand.getColorAttachmentView();
               RenderSystem.outputDepthTextureOverride = hand.getDepthAttachmentView();
               capturing = true;
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   static boolean end() {
      if (!capturing) {
         return false;
      } else {
         capturing = false;
         RenderSystem.outputColorTextureOverride = null;
         RenderSystem.outputDepthTextureOverride = null;
         return true;
      }
   }

   static void compose(int i, float f, float g, float h, float j, float k) {
      Framebuffer var6 = MinecraftClient.getInstance().getFramebuffer();
      CommandEncoder var7 = RenderSystem.getDevice().createCommandEncoder();
      GpuTextureView var8 = f > 0.0F
         ? blur.spread(var7, hand.getColorAttachmentView(), hand.textureWidth, hand.textureHeight, g)
         : hand.getColorAttachmentView();
      configData.clear();
      a(0, i, f);
      a(16, HandOverlayModule.highlight(i), f * 0.35F);
      configData.putFloat(32, h);
      configData.putFloat(36, j);
      configData.putFloat(40, k);
      configData.putFloat(44, 0.0F);
      var7.writeToBuffer(configSlice, configData);
      RenderPass var9 = var7.createRenderPass(PASS_LABEL, var6.getColorAttachmentView(), OptionalInt.empty());

      try {
         var9.setPipeline(PIPELINE);
         var9.setUniform("Overlay", configSlice);
         var9.bindTexture("CaptureSampler", hand.getColorAttachmentView(), RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
         var9.bindTexture("HaloSampler", var8, RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
         var9.draw(0, 3);
      } catch (Throwable var13) {
         if (var9 != null) {
            try {
               var9.close();
            } catch (Throwable var12) {
               var13.addSuppressed(var12);
            }
         }

         throw var13;
      }

      if (var9 != null) {
         var9.close();
      }
   }

   private static void a(int i, int j, float f) {
      configData.putFloat(i, (j >> 16 & 0xFF) / 255.0F);
      configData.putFloat(i + 4, (j >> 8 & 0xFF) / 255.0F);
      configData.putFloat(i + 8, (j & 0xFF) / 255.0F);
      configData.putFloat(i + 12, f);
   }

   private static boolean a(GpuDevice gpuDevice) {
      if (shadersReady) {
         return true;
      } else if (gpuDevice.precompilePipeline(PIPELINE).isValid() && BlurSpreadPass.compiled(gpuDevice)) {
         shadersReady = true;
         invalidLogged = false;
         return true;
      } else {
         if (!invalidLogged) {
            invalidLogged = true;
            AgarthaClientMod.LOGGER.warn("[Agartha] Hand Overlay shaders did not compile; the hand is drawn as usual");
         }

         return false;
      }
   }

   private static void a(int i, int j) {
      hand = new SimpleFramebuffer("Agartha hand overlay", i, j, true);
      blur = new BlurSpreadPass("Agartha hand overlay");
      config = RenderSystem.getDevice().createBuffer(() -> "Agartha hand overlay config", 136, 48L);
      configSlice = config.slice();
      configData = BufferUtils.createByteBuffer(48);
   }

   static void release() {
      if (capturing) {
         end();
      }

      if (hand != null) {
         hand.delete();
         hand = null;
      }

      if (blur != null) {
         blur.release();
         blur = null;
      }

      if (config != null) {
         config.close();
         config = null;
      }

      configSlice = null;
      configData = null;
      shadersReady = false;
   }

   private static boolean a() {
      if (SHADER_PACK == null) {
         return false;
      } else {
         try {
            return (boolean)SHADER_PACK.invokeExact();
         } catch (Throwable var1) {
            return false;
         }
      }
   }

   private static MethodHandle b() {
      if (!FabricLoader.getInstance().isModLoaded("iris")) {
         return null;
      } else {
         try {
            Class var0 = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object var1 = var0.getMethod("getInstance").invoke(null);
            return MethodHandles.publicLookup().findVirtual(var0, "isShaderPackInUse", MethodType.methodType(boolean.class)).bindTo(var1);
         } catch (LinkageError | ReflectiveOperationException var2) {
            return null;
         }
      }
   }
}
