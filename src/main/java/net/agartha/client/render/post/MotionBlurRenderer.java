package net.agartha.client.render.post;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.GpuBuffer.MappedView;
import com.mojang.blaze3d.systems.RenderSystem;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.MotionBlurModule;
import net.agartha.client.render.post.FrameExposure;
import net.agartha.client.render.post.MotionBlurAlgorithm;
import net.agartha.client.render.post.MotionBlurMath;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.memory.ObjectAllocator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public final class MotionBlurRenderer {
   private static final Identifier SCENE_CHAIN = a("motion_blur_scene");
   private static final Identifier FULL_CHAIN = a("motion_blur_full");
   private static final Identifier CAMERA_CHAIN = a("motion_blur_camera");
   private static final Identifier FRAMES_CHAIN = a("motion_blur_frames");
   private static final Identifier TRAIL_CHAIN = a("motion_blur_trail");
   private static final String VELOCITY_BLOCK = "MotionBlurConfig";
   private static final String FRAMES_BLOCK = "FrameBlendConfig";
   private static final String TRAIL_BLOCK = "TrailConfig";
   private static final int MODE_SCENE = 0;
   private static final int MODE_FULL = 1;
   private static final int MODE_CAMERA = 2;
   private static final double MAX_CAMERA_STEP = 64.0;
   private static final PostUniformBuffer sceneUniforms = new PostUniformBuffer("MotionBlurConfig");
   private static final PostUniformBuffer fullUniforms = new PostUniformBuffer("MotionBlurConfig");
   private static final PostUniformBuffer cameraUniforms = new PostUniformBuffer("MotionBlurConfig");
   private static final PostUniformBuffer frameUniforms = new PostUniformBuffer("FrameBlendConfig");
   private static final PostUniformBuffer trailUniforms = new PostUniformBuffer("TrailConfig");
   private static final Matrix4f inverseModelView = new Matrix4f();
   private static final Matrix4f inverseProjection = new Matrix4f();
   private static final Matrix4f previousModelView = new Matrix4f();
   private static final Matrix4f previousProjection = new Matrix4f();
   private static final Matrix4f lastModelView = new Matrix4f();
   private static final Matrix4f lastProjection = new Matrix4f();
   private static double lastX;
   private static double lastY;
   private static double lastZ;
   private static boolean hasLastCamera;
   private static float cameraDx;
   private static float cameraDy;
   private static float cameraDz;
   private static ObjectAllocator allocator;
   private static MotionBlurModule module;
   private static MotionBlurAlgorithm algorithm = MotionBlurAlgorithm.VELOCITY;
   private static boolean singlePass;
   private static long lastFrameNanos;
   private static float fps;
   private static float smoothedFps;
   private static final FrameExposure exposure = new FrameExposure();
   private static final Framebuffer[] history = new Framebuffer[12];
   private static final PostEffectSampler[] historySlots = new PostEffectSampler[12];
   private static final PostEffectSampler trailSlot = new PostEffectSampler("Prev");
   private static Framebuffer trailPrevious;
   private static boolean trailPrimed;
   private static boolean warned;

   private MotionBlurRenderer() {
   }

   public static void beginLevel(ObjectAllocator objectAllocator, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2) {
      a();
      a(camera, matrix4f, matrix4f2);
      MotionBlurModule var4 = ModuleManager.get(MotionBlurModule.class);
      if (var4 != null && var4.isEnabled() && !(var4.strength() <= 0.0F) && !MotionBlurModule.naturalMotionBlurInstalled()) {
         allocator = objectAllocator;
         module = var4;
         algorithm = var4.algorithm();
         MinecraftClient var5 = MinecraftClient.getInstance();
         singlePass = !var5.options.getPerspective().isFirstPerson() || var5.player != null && var5.player.hasVehicle();
         if (!algorithm.usesFrameHistory()) {
            d();
         }

         if (!algorithm.usesTrail()) {
            e();
         }
      } else {
         allocator = null;
         module = null;
         d();
         e();
      }
   }

   public static void beforeEntities() {
      if (module != null && algorithm.usesVelocity()) {
         if (singlePass) {
            a(FULL_CHAIN, fullUniforms, 1);
         } else {
            a(SCENE_CHAIN, sceneUniforms, 0);
         }
      }
   }

   public static void afterLevel() {
      if (module != null && algorithm.usesVelocity() && !singlePass) {
         a(CAMERA_CHAIN, cameraUniforms, 2);
      }
   }

   public static void afterHand() {
      try {
         if (module == null || allocator == null) {
            return;
         }

         if (algorithm.usesFrameHistory()) {
            b();
         } else if (algorithm.usesTrail()) {
            c();
         }
      } finally {
         allocator = null;
         module = null;
      }
   }

   public static void release() {
      sceneUniforms.release();
      fullUniforms.release();
      cameraUniforms.release();
      frameUniforms.release();
      trailUniforms.release();
      d();
      e();
   }

   private static void a() {
      long var0 = System.nanoTime();
      float var2 = lastFrameNanos == 0L ? 0.0F : (float)(var0 - lastFrameNanos) / 1.0E9F;
      lastFrameNanos = var0;
      fps = var2 > 0.0F && var2 < 1.0F ? 1.0F / var2 : 0.0F;
      smoothedFps = MotionBlurMath.smoothFps(smoothedFps, fps);
   }

   private static void a(Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2) {
      Vec3d var3 = camera.getCameraPos();
      double var4 = var3.x - lastX;
      double var6 = var3.y - lastY;
      double var8 = var3.z - lastZ;
      if (!hasLastCamera || var4 * var4 + var6 * var6 + var8 * var8 > 4096.0) {
         lastModelView.set(matrix4f);
         lastProjection.set(matrix4f2);
         var8 = 0.0;
         var6 = 0.0;
         var4 = 0.0;
         hasLastCamera = true;
      }

      previousModelView.set(lastModelView);
      previousProjection.set(lastProjection);
      cameraDx = (float)var4;
      cameraDy = (float)var6;
      cameraDz = (float)var8;
      matrix4f.invert(inverseModelView);
      matrix4f2.invert(inverseProjection);
      lastModelView.set(matrix4f);
      lastProjection.set(matrix4f2);
      lastX = var3.x;
      lastY = var3.y;
      lastZ = var3.z;
   }

   private static void a(Identifier identifier, PostUniformBuffer arg, int i) {
      if (allocator != null) {
         PostEffectProcessor var3 = PostEffectLoader.load(identifier);
         if (var3 != null) {
            float var4 = algorithm == MotionBlurAlgorithm.HYBRID ? 1.0F : module.strength();
            MotionBlurMath.a var5 = MotionBlurMath.shutter(var4, fps, RefreshRateProvider.current(), module.refreshScaling());

            try {
               GpuBuffer var6 = arg.next(var3);
               if (var6 == null) {
                  return;
               }

               MappedView var7 = RenderSystem.getDevice().createCommandEncoder().mapBuffer(var6, false, true);

               try {
                  Std140Builder.intoBuffer(var7.data())
                     .putMat4f(inverseModelView)
                     .putMat4f(inverseProjection)
                     .putMat4f(previousModelView)
                     .putMat4f(previousProjection)
                     .putVec4(cameraDx, cameraDy, cameraDz, 0.0F)
                     .putVec4(var5.strength(), var5.samples(), i, 0.0F);
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

               var3.render(MinecraftClient.getInstance().getFramebuffer(), allocator);
            } catch (RuntimeException var12) {
               a(arg, var12);
            }
         }
      }
   }

   private static void b() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      Framebuffer var1 = var0.getFramebuffer();
      b(var1.textureWidth, var1.textureHeight);
      double var2 = System.nanoTime() * 1.0E-9;
      a(var1, history[exposure.push(var2)]);
      int var4 = exposure.weigh(var2, RefreshRateProvider.current(), smoothedFps);
      if (var4 > 1) {
         PostEffectProcessor var5 = PostEffectLoader.load(FRAMES_CHAIN);
         PostEffectPass var6 = var5 == null ? null : PostUniformBuffer.passWith(var5, "FrameBlendConfig");
         if (var6 != null) {
            try {
               GpuBuffer var7 = frameUniforms.next(var5);
               MappedView var8 = RenderSystem.getDevice().createCommandEncoder().mapBuffer(var7, false, true);

               try {
                  Std140Builder var9 = Std140Builder.intoBuffer(var8.data());

                  for (int var10 = 0; var10 < 3; var10++) {
                     int var11 = var10 * 4;
                     var9.putVec4(a(var11, var4), a(var11 + 1, var4), a(var11 + 2, var4), a(var11 + 3, var4));
                  }

                  var9.putVec4(exposure.inverseTotalWeight(), var4, 0.0F, 0.0F);
               } catch (Throwable var13) {
                  if (var8 != null) {
                     try {
                        var8.close();
                     } catch (Throwable var12) {
                        var13.addSuppressed(var12);
                     }
                  }

                  throw var13;
               }

               if (var8 != null) {
                  var8.close();
               }

               for (int var15 = 0; var15 < historySlots.length; var15++) {
                  int var16 = exposure.slot(Math.min(var15, var4 - 1));
                  historySlots[var15].point(history[var16]).installInto(var6);
               }

               var5.render(var1, allocator);
            } catch (RuntimeException var14) {
               a(frameUniforms, var14);
            }
         }
      }
   }

   private static float a(int i, int j) {
      return i < j ? exposure.weight(i) : 0.0F;
   }

   private static void c() {
      Framebuffer var0 = MinecraftClient.getInstance().getFramebuffer();
      c(var0.textureWidth, var0.textureHeight);
      if (!trailPrimed) {
         a(var0, trailPrevious);
         trailPrimed = true;
      } else {
         PostEffectProcessor var1 = PostEffectLoader.load(TRAIL_CHAIN);
         PostEffectPass var2 = var1 == null ? null : PostUniformBuffer.passWith(var1, "TrailConfig");
         if (var2 != null) {
            boolean var3 = algorithm == MotionBlurAlgorithm.ACCUMULATION_MAX;

            try {
               GpuBuffer var4 = trailUniforms.next(var1);
               MappedView var5 = RenderSystem.getDevice().createCommandEncoder().mapBuffer(var4, false, true);

               try {
                  Std140Builder.intoBuffer(var5.data()).putVec4(MotionBlurMath.trailBlend(module.strength(), var3), var3 ? 0.0F : 1.0F, 0.0F, 0.0F);
               } catch (Throwable var9) {
                  if (var5 != null) {
                     try {
                        var5.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (var5 != null) {
                  var5.close();
               }

               trailSlot.point(trailPrevious).installInto(var2);
               var1.render(var0, allocator);
               a(var0, trailPrevious);
            } catch (RuntimeException var10) {
               a(trailUniforms, var10);
            }
         }
      }
   }

   private static void a(PostUniformBuffer arg, RuntimeException runtimeException) {
      arg.release();
      if (!warned) {
         warned = true;
         AgarthaClientMod.LOGGER.warn("[Agartha] Motion blur pass failed, retrying next frame", runtimeException);
      }
   }

   private static void b(int i, int j) {
      Framebuffer var2 = history[0];
      if (var2 == null || var2.textureWidth != i || var2.textureHeight != j) {
         d();

         for (int var3 = 0; var3 < history.length; var3++) {
            history[var3] = new SimpleFramebuffer("agartha/motion-blur/history-" + var3, i, j, false);
         }
      }
   }

   private static void c(int i, int j) {
      if (trailPrevious == null || trailPrevious.textureWidth != i || trailPrevious.textureHeight != j) {
         e();
         trailPrevious = new SimpleFramebuffer("agartha/motion-blur/trail", i, j, false);
      }
   }

   private static void d() {
      for (int var0 = 0; var0 < history.length; var0++) {
         if (history[var0] != null) {
            history[var0].delete();
            history[var0] = null;
         }
      }

      exposure.clear();
   }

   private static void e() {
      if (trailPrevious != null) {
         trailPrevious.delete();
         trailPrevious = null;
      }

      trailPrimed = false;
   }

   private static void a(Framebuffer framebuffer, Framebuffer framebuffer2) {
      RenderSystem.getDevice()
         .createCommandEncoder()
         .copyTextureToTexture(
            framebuffer.getColorAttachment(), framebuffer2.getColorAttachment(), 0, 0, 0, 0, 0, framebuffer2.textureWidth, framebuffer2.textureHeight
         );
   }

   private static Identifier a(String string) {
      return Identifier.of("agartha", string);
   }

   static {
      for (int var0 = 0; var0 < historySlots.length; var0++) {
         historySlots[var0] = new PostEffectSampler("Sample" + var0);
      }
   }
}
