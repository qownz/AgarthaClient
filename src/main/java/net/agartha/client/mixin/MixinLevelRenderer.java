package net.agartha.client.mixin;

import net.agartha.client.event.impl.Render3DEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Environment(EnvType.CLIENT)
@Mixin({WorldRenderer.class})
public abstract class MixinLevelRenderer {
   @ModifyArg(
      method = {"render"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/debug/DebugRenderer;render(Lnet/minecraft/client/render/Frustum;DDDF)V"
      ),
      index = 0
   )
   private Frustum agartha$onRenderLevel(Frustum frustum) {
      float var2 = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false);
      Render3DEvent.dispatch(null, var2, null, frustum);
      return frustum;
   }
}
