package net.agartha.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.agartha.client.module.e;
import net.agartha.client.module.impl.render.m;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin({FogRenderer.class})
public abstract class MixinNoRenderFogRenderer {
   @ModifyExpressionValue(
      method = {"getFogBuffer"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/client/render/fog/FogRenderer;fogEnabled:Z",
         opcode = 178
      )}
   )
   private boolean agartha$respectNoFog(boolean bl) {
      m var2 = e.get(m.class);
      return var2 != null && var2.noFog() ? false : bl;
   }
}
