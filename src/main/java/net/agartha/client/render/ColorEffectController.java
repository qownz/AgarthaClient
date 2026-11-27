package net.agartha.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.mixin.AccessorGameRenderer;
import net.agartha.client.mixin.AccessorPostChain;
import net.agartha.client.mixin.AccessorPostPass;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.client.ColorSettingsModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.UniformValue;
import net.minecraft.client.gl.UniformValue.Vec4fValue;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.util.Identifier;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

@Environment(EnvType.CLIENT)
public final class ColorEffectController {
   public static final Identifier POST_EFFECT_ID = Identifier.of("agartha", "color_adjust");
   private static final String UNIFORM_BLOCK = "ColorEditConfig";
   private static final Set<Identifier> TARGETS = DefaultFramebufferSet.MAIN_ONLY;
   private static boolean dirty = true;
   private static boolean warnedAboutReload = false;

   private ColorEffectController() {
   }

   public static void markDirty() {
      dirty = true;
   }

   public static void tick(MinecraftClient minecraftClient) {
      ColorSettingsModule var1 = ModuleManager.get(ColorSettingsModule.class);
      if (var1 == null || !var1.isEnabled()) {
         clearIfActive(minecraftClient);
      } else if (minecraftClient.gameRenderer != null) {
         if (minecraftClient.getOverlay() != null) {
            dirty = true;
         } else {
            AccessorGameRenderer var2 = (AccessorGameRenderer)minecraftClient.gameRenderer;
            boolean var3 = !POST_EFFECT_ID.equals(minecraftClient.gameRenderer.getPostProcessorId());
            if (var3 || !var2.agartha$isEffectActive()) {
               var2.agartha$setPostEffect(POST_EFFECT_ID);
               if (var3) {
                  dirty = true;
               }
            }

            if (dirty) {
               try {
                  a(minecraftClient, var1);
                  dirty = false;
                  warnedAboutReload = false;
               } catch (RuntimeException var5) {
                  dirty = true;
                  if (!warnedAboutReload) {
                     AgarthaClientMod.LOGGER.warn("Delaying color settings post effect update until resource reload finishes.");
                     warnedAboutReload = true;
                  }
               }
            }
         }
      }
   }

   public static void clearIfActive(MinecraftClient minecraftClient) {
      if (minecraftClient.gameRenderer != null && POST_EFFECT_ID.equals(minecraftClient.gameRenderer.getPostProcessorId())) {
         minecraftClient.gameRenderer.clearPostProcessor();
      }
   }

   private static void a(MinecraftClient minecraftClient, ColorSettingsModule colorSettingsModule) {
      PostEffectProcessor var2 = minecraftClient.getShaderLoader().loadPostEffect(POST_EFFECT_ID, TARGETS);
      if (var2 != null) {
         List var3 = ((AccessorPostChain)var2).agartha$getPasses();
         int var4 = colorSettingsModule.tintRgb();
         List var5 = List.of(
            new Vec4fValue(
               new Vector4f(colorSettingsModule.hue(), colorSettingsModule.saturation(), colorSettingsModule.brightness(), colorSettingsModule.contrast())
            ),
            new Vec4fValue(
               new Vector4f((var4 >>> 16 & 0xFF) / 255.0F, (var4 >>> 8 & 0xFF) / 255.0F, (var4 & 0xFF) / 255.0F, colorSettingsModule.tintStrength())
            )
         );

         for (PostEffectPass var7 : (Iterable<PostEffectPass>) var3) {
            Map<String, GpuBuffer> var8 = ((AccessorPostPass)var7).agartha$getCustomUniforms();
            if (var8.containsKey("ColorEditConfig")) {
               GpuBuffer var9 = var8.put("ColorEditConfig", a(var7, var5));
               if (var9 != null) {
                  var9.close();
               }
            }
         }
      }
   }

   private static GpuBuffer a(PostEffectPass postEffectPass, List<UniformValue> list) {
      Std140SizeCalculator var2 = new Std140SizeCalculator();

      for (UniformValue var4 : list) {
         var4.addSize(var2);
      }

      int var10 = var2.get();
      MemoryStack var11 = MemoryStack.stackPush();

      GpuBuffer var12;
      try {
         Std140Builder var5 = Std140Builder.onStack(var11, var10);

         for (UniformValue var7 : list) {
            var7.write(var5);
         }

         var12 = RenderSystem.getDevice()
            .createBuffer(() -> "agartha/color-settings/" + postEffectPass.getClass().getSimpleName() + "/ColorEditConfig", 128, var5.get());
      } catch (Throwable var9) {
         if (var11 != null) {
            try {
               var11.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }

      if (var11 != null) {
         var11.close();
      }

      return var12;
   }
}
