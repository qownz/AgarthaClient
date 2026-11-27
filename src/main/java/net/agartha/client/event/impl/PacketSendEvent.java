package net.agartha.client.event.impl;

import net.agartha.client.event.CancellableEvent;
import net.agartha.client.event.PooledCancellableEventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;

@Environment(EnvType.CLIENT)
public final class PacketSendEvent extends CancellableEvent {
   public static final PooledCancellableEventType<PacketSendEvent> TYPE = new PooledCancellableEventType<>("PacketSendEvent", PacketSendEvent::new, 1024);
   public Packet<?> packet;

   private PacketSendEvent() {
   }

   public static boolean dispatchSync(Packet<?> packet) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         PacketSendEvent var1 = TYPE.pool.acquire();
         var1.packet = packet;
         var1.resetCancellation();

         boolean var2;
         try {
            TYPE.dispatch(var1);
            var2 = var1.isCancelled();
         } finally {
            var1.packet = null;
            TYPE.pool.release(var1);
         }

         return var2;
      }
   }
}
