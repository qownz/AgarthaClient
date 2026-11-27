package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.misc.p;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Environment(EnvType.CLIENT)
@Mixin({InGameHud.class})
public abstract class MixinGuiSmoothHotbar {
   @Unique
   private float agartha$currentX = Float.NaN;
   @Unique
   private long agartha$lastNanos = 0L;

   @ModifyArg(
      method = {"renderHotbar"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V",
         ordinal = 1
      ),
      index = 2,
      require = 0
   )
   private int agartha$smoothSelectionX(int i) {
      p var2 = e.get(p.class);
      if (var2 != null && var2.isEnabled()) {
         ClientPlayerEntity var3 = MinecraftClient.getInstance().player;
         if (var3 == null) {
            return i;
         } else {
            int var4 = var3.getInventory().getSelectedSlot();
            int var5 = i - var4 * 20;
            float var6 = var4 * 20;
            long var7 = System.nanoTime();
            if (Float.isNaN(this.agartha$currentX)) {
               this.agartha$currentX = var6;
               this.agartha$lastNanos = var7;
            }

            float var9 = (float)(var7 - this.agartha$lastNanos) / 1.0E9F;
            this.agartha$lastNanos = var7;
            if (var9 > 0.1F) {
               var9 = 0.1F;
            }

            float var10 = var6 - this.agartha$currentX;
            if (Math.abs(var10) > 0.1F) {
               float var11 = var9 * var2.speed();
               if (var11 > 1.0F) {
                  var11 = 1.0F;
               }

               this.agartha$currentX += var10 * var11;
            } else {
               this.agartha$currentX = var6;
            }

            return var5 + Math.round(this.agartha$currentX);
         }
      } else {
         this.agartha$currentX = Float.NaN;
         return i;
      }
   }
}
