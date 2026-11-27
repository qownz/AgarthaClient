package net.agartha.client.event.impl;

import net.agartha.client.event.Event;
import net.agartha.client.event.EventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public final class BlockUpdateEvent extends Event {
   public static final EventType<BlockUpdateEvent> TYPE = new EventType<>("BlockUpdateEvent");
   public BlockPos pos;
   public BlockState oldState;
   public BlockState newState;
   private static final BlockUpdateEvent INSTANCE = new BlockUpdateEvent();

   private BlockUpdateEvent() {
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
