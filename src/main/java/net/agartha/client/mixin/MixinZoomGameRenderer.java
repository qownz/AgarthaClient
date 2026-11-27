package net.agartha.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.agartha.client.module.e;
import net.agartha.client.module.impl.misc.k;
import net.agartha.client.module.impl.misc.v;
import net.agartha.client.module.impl.render.d;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({GameRenderer.class})
public abstract class MixinZoomGameRenderer {
   @ModifyReturnValue(
      method = {"getFov"},
      at = {@At("RETURN")}
   )
   private float agartha$zoomFov(float f, Camera camera, float g, boolean bl) {
      MinecraftClient var5 = MinecraftClient.getInstance();
      if (var5.world != null && var5.player != null) {
         v var6 = e.get(v.class);
         if (var6 == null || !var6.isZoomActive()) {
            return f;
         } else {
            return !bl && !var6.affectHandFov() ? f : f / var6.getZoomDivisor(g);
         }
      } else {
         return f;
      }
   }

   @Inject(
      method = {"renderHand"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$hideHandsWhileZooming(float f, boolean bl, Matrix4f matrix4f, CallbackInfo callbackInfo) {
      v var5 = e.get(v.class);
      if (var5 != null && var5.isZooming() && !var5.affectHandFov()) {
         callbackInfo.cancel();
      } else {
         k var6 = e.get(k.class);
         d var7 = e.get(d.class);
         if (var6 != null && var6.isEnabled() || var7 != null && var7.isEnabled()) {
            callbackInfo.cancel();
         }
      }
   }
}
