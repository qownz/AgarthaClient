package net.agartha.client.mixin;

import io.netty.channel.ChannelHandlerContext;
import net.agartha.client.d.a_pkg.i;
import net.agartha.client.d.a_pkg.j;
import net.agartha.client.d.a_pkg.k;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ClientConnection.class})
public abstract class MixinClientConnection {
   @Inject(
      method = {"channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$onPacketReceive(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo callbackInfo) {
      if (j.dispatchSync(packet)) {
         callbackInfo.cancel();
      } else {
         i.dispatchAsync(packet);
      }
   }

   @Inject(
      method = {"send(Lnet/minecraft/network/packet/Packet;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$onPacketSend(Packet<?> packet, CallbackInfo callbackInfo) {
      if (k.dispatchSync(packet)) {
         callbackInfo.cancel();
      }
   }
}
