package net.agartha.client.mixin;

import net.agartha.client.event.impl.ChatSendEvent;
import net.agartha.client.module.impl.render.StorageEspModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ClientPlayNetworkHandler.class})
public abstract class MixinClientPacketListener {
   @Inject(
      method = {"sendChatMessage"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$onSendChat(String string, CallbackInfo callbackInfo) {
      if (ChatSendEvent.dispatch(string)) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"sendChatCommand"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$onSendCommand(String string, CallbackInfo callbackInfo) {
      if (ChatSendEvent.dispatch("/" + string)) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"onChunkData"},
      at = {@At("TAIL")}
   )
   private void agartha$chunkLoaded(ChunkDataS2CPacket chunkDataS2CPacket, CallbackInfo callbackInfo) {
      StorageEspModule.onChunkLoaded(chunkDataS2CPacket);
   }
}
