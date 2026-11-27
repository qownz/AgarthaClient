package net.agartha.client.util.a_pkg;

import net.agartha.client.d.a_pkg.i;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

@Environment(EnvType.CLIENT)
public final class a {
   private a() {
   }

   public static void apply(i arg, b<?> arg2) {
      if (arg.packet instanceof ChunkDataS2CPacket var2) {
         arg2.requestRescan(ChunkPos.toLong(var2.getChunkX(), var2.getChunkZ()));
      } else if (arg.packet instanceof BlockEntityUpdateS2CPacket var3) {
         BlockPos var7 = var3.getPos();
         arg2.requestRescan(ChunkPos.toLong(var7.getX() >> 4, var7.getZ() >> 4));
      } else if (arg.packet instanceof UnloadChunkS2CPacket var4) {
         arg2.remove(var4.pos().toLong());
      }
   }
}
