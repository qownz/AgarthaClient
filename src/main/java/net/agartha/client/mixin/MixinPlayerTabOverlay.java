package net.agartha.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.agartha.client.util.aa;
import net.agartha.client.util.z;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({PlayerListHud.class})
public abstract class MixinPlayerTabOverlay {
   @ModifyVariable(
      method = {"setFooter"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private Text agartha$footer(Text text) {
      return aa.accept(text);
   }

   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void agartha$tabBegin(CallbackInfo callbackInfo) {
      z.begin();
   }

   @WrapOperation(
      method = {"render"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"
      )}
   )
   private void agartha$tabPanel(DrawContext drawContext, int i, int j, int k, int l, int m, Operation<Void> operation) {
      if (m == Integer.MIN_VALUE) {
         z.panel(i, j, k, l);
      }

      operation.call(new Object[]{drawContext, i, j, k, l, m});
   }

   @Inject(
      method = {"render"},
      at = {@At("RETURN")}
   )
   private void agartha$tabEnd(CallbackInfo callbackInfo) {
      z.end();
   }
}
