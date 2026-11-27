package net.agartha.client.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectPass.Sampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin({PostEffectPass.class})
public interface AccessorPostPass {
   @Accessor("uniformBuffers")
   Map<String, GpuBuffer> agartha$getCustomUniforms();

   @Accessor("samplers")
   List<Sampler> agartha$getInputs();
}
