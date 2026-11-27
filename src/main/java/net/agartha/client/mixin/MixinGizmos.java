package net.agartha.client.mixin;

import net.agartha.client.util.l;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.util.math.Box;
import net.minecraft.world.debug.gizmo.GizmoCollector;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.VisibilityConfigurable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({GizmoDrawing.class})
public abstract class MixinGizmos {
   @Inject(
      method = {"box(Lnet/minecraft/util/math/Box;Lnet/minecraft/client/render/DrawStyle;Z)Lnet/minecraft/world/debug/gizmo/VisibilityConfigurable;"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static void agartha$skipOffscreen(Box box, DrawStyle drawStyle, boolean bl, CallbackInfoReturnable<VisibilityConfigurable> callbackInfoReturnable) {
      if (l.outOfView(box)) {
         callbackInfoReturnable.setReturnValue(GizmoCollector.NOOP_CONFIGURABLE);
      }
   }
}
