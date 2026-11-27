package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public final class a extends net.agartha.client.d.c {
   public static final net.agartha.client.d.j<a> TYPE = new net.agartha.client.d.j<>("BlockUpdateEvent");
   public BlockPos pos;
   public BlockState oldState;
   public BlockState newState;
   private static final a INSTANCE = new a();

   private a() {
   }

   public static void dispatch(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
      if (TYPE.hasPreListeners()) {
         INSTANCE.pos = blockPos;
         INSTANCE.oldState = blockState;
         INSTANCE.newState = blockState2;
         TYPE.dispatch(INSTANCE);
         INSTANCE.pos = null;
         INSTANCE.oldState = null;
         INSTANCE.newState = null;
      }
   }
}
