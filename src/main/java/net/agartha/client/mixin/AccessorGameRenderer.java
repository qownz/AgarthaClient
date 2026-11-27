package net.agartha.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin({GameRenderer.class})
public interface AccessorGameRenderer {
   @Invoker("getFov")
   float agartha$invokeGetFov(Camera camera, float f, boolean bl);

   @Invoker("setPostProcessor")
   void agartha$setPostEffect(Identifier identifier);

   @Accessor("postProcessorEnabled")
   boolean agartha$isEffectActive();

   @Invoker("shouldRenderBlockOutline")
   boolean agartha$shouldRenderBlockOutline();
}
