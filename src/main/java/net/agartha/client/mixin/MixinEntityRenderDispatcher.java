package net.agartha.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin({EntityRenderManager.class})
public abstract class MixinEntityRenderDispatcher {
   @WrapOperation(
      method = {"shouldRender"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/entity/EntityRenderer;shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z"
      )}
   )
   private <T extends Entity> boolean agartha$guardNullRenderer(
      EntityRenderer<? super T, ?> entityRenderer, T entity, Frustum frustum, double d, double e, double f, Operation<Boolean> operation
   ) {
      return entityRenderer == null ? false : (Boolean)operation.call(new Object[]{entityRenderer, entity, frustum, d, e, f});
   }
}
