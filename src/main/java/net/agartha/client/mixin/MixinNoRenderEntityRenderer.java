package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({EntityRenderer.class})
public abstract class MixinNoRenderEntityRenderer<T extends Entity> {
   @Inject(
      method = {"shouldRender"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noEntity(T entity, Frustum frustum, double d, double e, double f, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
      NoRenderModule var10 = ModuleManager.get(NoRenderModule.class);
      if (var10 != null) {
         if (var10.noFallingBlocks() && entity instanceof FallingBlockEntity) {
            callbackInfoReturnable.setReturnValue(false);
         } else {
            if (var10.noDeadEntities() && entity instanceof LivingEntity var11 && var11.isDead()) {
               callbackInfoReturnable.setReturnValue(false);
            }
         }
      }
   }

   @Inject(
      method = {"hasLabel"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noNametags(T entity, double d, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
      NoRenderModule var5 = ModuleManager.get(NoRenderModule.class);
      if (var5 != null && var5.noNametags()) {
         callbackInfoReturnable.setReturnValue(false);
      }
   }
}
