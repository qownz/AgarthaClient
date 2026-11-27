package net.agartha.client.util.time;

import net.agartha.client.event.impl.PacketReceiveNettyEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

@Environment(EnvType.CLIENT)
public final class TpsMeter {
   private static long lastGameTime = -1L;
   private static long lastNanos;
   private static volatile float tps = 20.0F;
   private static volatile boolean known;
   private static Object level;

   private TpsMeter() {
   }

   public static void install() {
      PacketReceiveNettyEvent.TYPE.listen(TpsMeter.class, arg -> {
         if (arg.packet instanceof WorldTimeUpdateS2CPacket var1) {
            a(var1.time());
         }
      });
   }

   private static void a(long l) {
      long var2 = System.nanoTime();
      ClientWorld var4 = MinecraftClient.getInstance().world;
      if (var4 != level) {
         level = var4;
         lastGameTime = -1L;
         known = false;
      }

      if (lastGameTime >= 0L && l > lastGameTime) {
         float var5 = (float)(var2 - lastNanos) / 1.0E9F;
         if (var5 > 0.05F) {
            float var6 = Math.min(20.0F, (float)(l - lastGameTime) / var5);
            tps = known ? tps + (var6 - tps) * 0.5F : var6;
            known = true;
         }
      }

      lastGameTime = l;
      lastNanos = var2;
   }

   public static float tps() {
      return known ? tps : 20.0F;
   }
}
