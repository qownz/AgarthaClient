package net.agartha.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.client.UtilitiesModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHud.Backend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ChatHud.class})
public abstract class MixinChatComponentSmooth {
   @Shadow
   private int scrolledLines;
   @Unique
   private long agartha$lastMessageTime = 0L;
   @Unique
   private static final float AGARTHA_FADE_MS = 150.0F;

   @Shadow
   private int getLineHeight() {
      return 0;
   }

   @Inject(
      method = {"addMessage(Lnet/minecraft/text/Text;)V", "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"},
      at = {@At("TAIL")},
      require = 0
   )
   private void agartha$stampMessage(CallbackInfo callbackInfo) {
      this.agartha$lastMessageTime = System.currentTimeMillis();
   }

   @Unique
   private float agartha$displacement() {
      if (this.scrolledLines != 0) {
         return 0.0F;
      } else {
         float var1 = this.getLineHeight() * 0.8F;
         long var2 = System.currentTimeMillis() - this.agartha$lastMessageTime;
         float var4 = Math.min((float)var2 / 150.0F, 1.0F);
         return var1 - var4 * var1;
      }
   }

   @WrapOperation(
      method = {"render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;IIIZZ)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/gui/hud/ChatHud$Backend;IIZ)V"
      )},
      require = 0
   )
   private void agartha$smoothChat(
      ChatHud chatHud, Backend backend, int i, int j, boolean bl, Operation<Void> operation, @Local(argsOnly = true) DrawContext drawContext
   ) {
      UtilitiesModule var8 = ModuleManager.get(UtilitiesModule.class);
      float var9 = var8 != null && var8.smoothChat() ? this.agartha$displacement() : 0.0F;
      if (var9 != 0.0F) {
         drawContext.getMatrices().pushMatrix();
         drawContext.getMatrices().translate(0.0F, var9);
         operation.call(new Object[]{chatHud, backend, i, j, bl});
         drawContext.getMatrices().popMatrix();
      } else {
         operation.call(new Object[]{chatHud, backend, i, j, bl});
      }
   }
}
