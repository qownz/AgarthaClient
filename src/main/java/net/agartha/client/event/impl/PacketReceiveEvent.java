package net.agartha.client.event.impl;

import net.agartha.client.event.CancellableEvent;
import net.agartha.client.event.EventBus;
import net.agartha.client.event.PooledCancellableEventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;

@Environment(EnvType.CLIENT)
public final class PacketReceiveEvent extends CancellableEvent implements Runnable {
   public static final PooledCancellableEventType<PacketReceiveEvent> TYPE = new PooledCancellableEventType<>("PacketReceiveEvent", PacketReceiveEvent::new, 1024);
   public Packet<?> packet;

   private PacketReceiveEvent() {
   }

   public static void dispatchAsync(Packet<?> packet) {
      if (TYPE.hasPreListeners()) {
         PacketReceiveEvent var1 = TYPE.pool.acquire();
         var1.packet = packet;
         var1.resetCancellation();
         if (!EventBus.INSTANCE.postAsync(var1)) {
            var1.packet = null;
            TYPE.pool.release(var1);
         }
      }
   }

   @Override
   public void run() {
      try {
         TYPE.dispatch(this);
      } finally {
         this.packet = null;
         TYPE.pool.release(this);
      }
   }
}
