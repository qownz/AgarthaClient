package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({InGameOverlayRenderer.class})
public abstract class MixinNoRenderScreenEffect {
   private static NoRenderModule noRender() {
      return ModuleManager.get(NoRenderModule.class);
   }

   @Inject(
      method = {"renderFireOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void agartha$noFire(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, Sprite sprite, CallbackInfo callbackInfo) {
      NoRenderModule var4 = noRender();
      if (var4 != null && var4.noFireOverlay()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"renderUnderwaterOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void agartha$noWater(
      MinecraftClient minecraftClient, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, CallbackInfo callbackInfo
   ) {
      NoRenderModule var4 = noRender();
      if (var4 != null && var4.noLiquidOverlay()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"renderInWallOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void agartha$noInWall(Sprite sprite, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, CallbackInfo callbackInfo) {
      NoRenderModule var4 = noRender();
      if (var4 != null && var4.noInWallOverlay()) {
         callbackInfo.cancel();
      }
   }
}
