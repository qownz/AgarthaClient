package net.agartha.client.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.agartha.client.l.a_pkg.d;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.memory.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({WorldRenderer.class})
public abstract class MixinMotionBlurLevelRenderer {
   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void agartha$motionBlurBegin(
      ObjectAllocator objectAllocator,
      RenderTickCounter renderTickCounter,
      boolean bl,
      Camera camera,
      Matrix4f matrix4f,
      Matrix4f matrix4f2,
      Matrix4f matrix4f3,
      GpuBufferSlice gpuBufferSlice,
      Vector4f vector4f,
      boolean bl2,
      CallbackInfo callbackInfo
   ) {
      d.beginLevel(objectAllocator, camera, matrix4f, matrix4f2);
   }

   @Inject(
      method = {"pushEntityRenders"},
      at = {@At("HEAD")}
   )
   private void agartha$motionBlurBeforeEntities(CallbackInfo callbackInfo) {
      d.beforeEntities();
   }

   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   private void agartha$motionBlurAfterLevel(CallbackInfo callbackInfo) {
      d.afterLevel();
   }
}
