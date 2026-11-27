package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.misc.o;
import net.agartha.client.module.impl.render.l;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo.TextureAsset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({PlayerEntityRenderer.class})
public abstract class MixinPlayerRenderer {
   @Inject(
      method = {"updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V"},
      at = {@At("TAIL")}
   )
   private void agartha$injectSkin(PlayerLikeEntity playerLikeEntity, PlayerEntityRenderState playerEntityRenderState, float f, CallbackInfo callbackInfo) {
      o var5 = e.get(o.class);
      if (var5 != null && var5.isEnabled()) {
         TextureAsset var6 = var5.getSkinTextureFor(playerEntityRenderState.id);
         if (var6 != null) {
            playerEntityRenderState.skinTextures = new SkinTextures(
               var6,
               playerEntityRenderState.skinTextures.cape(),
               playerEntityRenderState.skinTextures.elytra(),
               playerEntityRenderState.skinTextures.model(),
               playerEntityRenderState.skinTextures.secure()
            );
         }
      }
   }

   @Inject(
      method = {"renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$suppressVanillaNameTag(
      PlayerEntityRenderState playerEntityRenderState,
      MatrixStack matrixStack,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      CameraRenderState cameraRenderState,
      CallbackInfo callbackInfo
   ) {
      if (l.hideVanillaNametags()) {
         callbackInfo.cancel();
      }
   }
}
