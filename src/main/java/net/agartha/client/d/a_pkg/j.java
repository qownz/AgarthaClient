package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;

@Environment(EnvType.CLIENT)
public final class j extends net.agartha.client.d.a {
   public static final net.agartha.client.d.p<j> TYPE = new net.agartha.client.d.p<>("PacketReceiveNettyEvent", j::new, 1024);
   public Packet<?> packet;

   private j() {
   }

   public static boolean dispatchSync(Packet<?> packet) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         j var1 = TYPE.pool.acquire();
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
