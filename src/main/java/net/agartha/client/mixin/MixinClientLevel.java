package net.agartha.client.mixin;

import net.agartha.client.d.a_pkg.a;
import net.agartha.client.d.a_pkg.r;
import net.agartha.client.module.e;
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
      r.dispatch(entity);
   }

   @Inject(
      method = {"handleBlockUpdate"},
      at = {@At("HEAD")}
   )
   private void agartha$onServerVerifiedBlockState(BlockPos blockPos, BlockState blockState, int i, CallbackInfo callbackInfo) {
      ClientWorld var5 = (ClientWorld)(Object)this;
      BlockState var6 = var5.getBlockState(blockPos);
      SpawnerProtectModule var7 = e.get(SpawnerProtectModule.class);
      if (var7 != null) {
         var7.handleServerVerifiedBlockUpdate(blockPos, var6, blockState);
      }

      a.dispatch(blockPos, var6, blockState);
   }

   @Inject(
      method = {"updateListeners"},
      at = {@At("HEAD")}
   )
   private void agartha$onBlockUpdated(BlockPos blockPos, BlockState blockState, BlockState blockState2, int i, CallbackInfo callbackInfo) {
      a.dispatch(blockPos, blockState, blockState2);
   }
}
