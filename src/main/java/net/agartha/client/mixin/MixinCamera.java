package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.client.UtilitiesModule;
import net.agartha.client.module.impl.misc.k;
import net.agartha.client.module.impl.render.d;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({Camera.class})
public abstract class MixinCamera {
   @Mutable
   @Shadow
   private boolean thirdPerson;

   @Shadow
   protected abstract void setRotation(float f, float g);

   @Shadow
   protected abstract void setPos(double d, double e, double f);

   @Shadow
   protected abstract void moveBy(float f, float g, float h);

   @Shadow
   private float clipToSpace(float f) {
      throw new AssertionError();
   }

   @ModifyArg(
      method = {"update"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/Camera;clipToSpace(F)F"
      ),
      index = 0,
      require = 0
   )
   private float agartha$f5Zoom(float f) {
      UtilitiesModule var2 = e.get(UtilitiesModule.class);
      return var2 != null && var2.f5Zoom() ? var2.f5Distance() : f;
   }

   @Inject(
      method = {"update"},
      at = {@At("RETURN")}
   )
   private void agartha$cameraOverride(World world, Entity entity, boolean bl, boolean bl2, float f, CallbackInfo callbackInfo) {
      k var8 = e.get(k.class);
      if (var8 != null && var8.isEnabled()) {
         this.setPos(var8.getX(f), var8.getY(f), var8.getZ(f));
         this.setRotation(var8.getYaw(f), var8.getPitch(f));
         this.thirdPerson = true;
         net.agartha.client.util.e.update(world, ((Camera)(Object)this).getCameraPos());
      } else {
         d var9 = e.get(d.class);
         if (var9 != null && var9.isEnabled() && entity != null) {
            double var10 = MathHelper.lerp(f, entity.lastX, entity.getX());
            double var12 = MathHelper.lerp(f, entity.lastY, entity.getY()) + entity.getStandingEyeHeight();
            double var14 = MathHelper.lerp(f, entity.lastZ, entity.getZ());
            this.setRotation(var9.yaw(), var9.pitch());
            this.setPos(var10, var12, var14);
            float var16 = var9.distance();
            this.moveBy(-(var9.wallClip() ? this.clipToSpace(var16) : var16), 0.0F, 0.0F);
            this.thirdPerson = true;
            net.agartha.client.util.e.update(world, ((Camera)(Object)this).getCameraPos());
         } else {
            net.agartha.client.util.e.release();
         }
      }
   }
}
