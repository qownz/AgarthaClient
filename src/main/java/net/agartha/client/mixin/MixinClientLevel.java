package net.agartha.client.mixin;

import net.agartha.client.event.impl.BlockUpdateEvent;
import net.agartha.client.event.impl.WorldEntityAddEvent;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.misc.SpawnerProtectModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ClientWorld.class})
public abstract class MixinClientLevel {
   @Inject(
      method = {"addEntity"},
      at = {@At("HEAD")}
   )
   private void agartha$onEntityAdd(Entity entity, CallbackInfo callbackInfo) {
      WorldEntityAddEvent.dispatch(entity);
   }

   @Inject(
      method = {"handleBlockUpdate"},
      at = {@At("HEAD")}
   )
   private void agartha$onServerVerifiedBlockState(BlockPos blockPos, BlockState blockState, int i, CallbackInfo callbackInfo) {
      ClientWorld var5 = (ClientWorld)(Object)this;
      BlockState var6 = var5.getBlockState(blockPos);
      SpawnerProtectModule var7 = ModuleManager.get(SpawnerProtectModule.class);
      if (var7 != null) {
         var7.handleServerVerifiedBlockUpdate(blockPos, var6, blockState);
      }

      BlockUpdateEvent.dispatch(blockPos, var6, blockState);
   }

   @Inject(
      method = {"updateListeners"},
      at = {@At("HEAD")}
   )
   private void agartha$onBlockUpdated(BlockPos blockPos, BlockState blockState, BlockState blockState2, int i, CallbackInfo callbackInfo) {
      BlockUpdateEvent.dispatch(blockPos, blockState, blockState2);
   }
}
