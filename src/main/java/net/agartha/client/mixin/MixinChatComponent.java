package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.misc.m;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ChatHud.class})
public abstract class MixinChatComponent {
   @ModifyVariable(
      method = {"addMessage(Lnet/minecraft/text/Text;)V", "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"},
      at = @At("HEAD"),
      argsOnly = true,
      require = 0
   )
   private Text agartha$protectChat(Text text) {
      try {
         m var2 = e.get(m.class);
         if (var2 != null && var2.isEnabled()) {
            return var2.censorChat(text);
         }
      } catch (Throwable var3) {
      }

      return text;
   }

   @Inject(
      method = {"addMessage(Lnet/minecraft/text/Text;)V"},
      at = {@At("HEAD")}
   )
   private void agartha$onAddMessage(Text text, CallbackInfo callbackInfo) {
      net.agartha.client.util.m.publish(text.getString());
   }

   @Inject(
      method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"},
      at = {@At("HEAD")}
   )
   private void agartha$onAddMessageSigned(Text text, MessageSignatureData messageSignatureData, MessageIndicator messageIndicator, CallbackInfo callbackInfo) {
      net.agartha.client.util.m.publish(text.getString());
   }
}
