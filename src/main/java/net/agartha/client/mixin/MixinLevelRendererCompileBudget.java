package net.agartha.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.agartha.client.util.t;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkRendererRegionBuilder;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({WorldRenderer.class})
public abstract class MixinLevelRendererCompileBudget {
   @Unique
   private static final long AGARTHA_BUDGET_NANOS = 4000000L;
   @Unique
   private static final int AGARTHA_MIN_PER_FRAME = 16;
   @Unique
   private long agartha$budgetEnds;
   @Unique
   private int agartha$started;
   @Unique
   private boolean agartha$deferred;

   @Inject(
      method = {"updateChunks"},
      at = {@At("HEAD")},
      require = 0
   )
   private void agartha$startBudget(CallbackInfo callbackInfo) {
      this.agartha$budgetEnds = System.nanoTime() + 4000000L;
      this.agartha$started = 0;
      this.agartha$deferred = false;
   }

   @Inject(
      method = {"updateChunks"},
      at = {@At("TAIL")},
      require = 0
   )
   private void agartha$prewarm(CallbackInfo callbackInfo) {
      t.run(((AccessorLevelRendererViewArea)(Object)this).agartha$viewArea());
   }

   @WrapOperation(
      method = {"updateChunks"},
      require = 0,
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;scheduleRebuild(Lnet/minecraft/client/render/chunk/ChunkRendererRegionBuilder;)V"
      )}
   )
   private void agartha$budgetRebuild(BuiltChunk builtChunk, ChunkRendererRegionBuilder chunkRendererRegionBuilder, Operation<Void> operation) {
      if (this.agartha$started >= 16 && System.nanoTime() > this.agartha$budgetEnds) {
         this.agartha$deferred = true;
      } else {
         this.agartha$deferred = false;
         this.agartha$started++;
         operation.call(new Object[]{builtChunk, chunkRendererRegionBuilder});
      }
   }

   @WrapOperation(
      method = {"updateChunks"},
      require = 0,
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;cancelRebuild()V"
      )}
   )
   private void agartha$keepDirtyIfDeferred(BuiltChunk builtChunk, Operation<Void> operation) {
      if (this.agartha$deferred) {
         this.agartha$deferred = false;
      } else {
         operation.call(new Object[]{builtChunk});
      }
   }
}
