package net.agartha.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.BufferManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin({BufferManager.class})
public class MixinGlDevice {
   @Redirect(
      method = {"create"},
      at = @At(
         value = "FIELD",
         target = "Lnet/minecraft/client/gl/GlBackend;allowGlArbDirectAccess:Z",
         opcode = 178
      )
   )
   private static boolean agartha$blockDsaForAmd() {
      String var0 = GL11.glGetString(7937);
      return var0 == null || !var0.contains("AMD");
   }
}
