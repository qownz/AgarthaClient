package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;

@Environment(EnvType.CLIENT)
public final class i extends net.agartha.client.d.a implements Runnable {
   public static final net.agartha.client.d.p<i> TYPE = new net.agartha.client.d.p<>("PacketReceiveEvent", i::new, 1024);
   public Packet<?> packet;

   private i() {
   }

   public static void dispatchAsync(Packet<?> packet) {
      if (TYPE.hasPreListeners()) {
         i var1 = TYPE.pool.acquire();
         var1.packet = packet;
         var1.resetCancellation();
         if (!net.agartha.client.d.d.INSTANCE.postAsync(var1)) {
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
