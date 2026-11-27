package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({MapRenderer.class})
public abstract class MixinNoRenderMap {
   @Inject(
      method = {"draw"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noMapContents(
      MapRenderState mapRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, boolean bl, int i, CallbackInfo callbackInfo
   ) {
      NoRenderModule var7 = ModuleManager.get(NoRenderModule.class);
      if (var7 != null && var7.noMapContents()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"update"},
      at = {@At("RETURN")}
   )
   private void agartha$noMapMarkers(MapIdComponent mapIdComponent, MapState mapState, MapRenderState mapRenderState, CallbackInfo callbackInfo) {
      NoRenderModule var5 = ModuleManager.get(NoRenderModule.class);
      if (var5 != null && var5.noMapMarkers()) {
         mapRenderState.decorations.clear();
      }
   }
}
