package net.agartha.client.mixin;

import net.agartha.client.compat.AgarthaDisconnectTick;
import net.agartha.client.event.EventBus;
import net.agartha.client.event.impl.EndClientTickEvent;
import net.agartha.client.event.impl.ScanFrameEvent;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.util.perf.Profiler;
import net.agartha.client.util.perf.TickBudget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({MinecraftClient.class})
public abstract class MixinMinecraft {
   @Unique
   private long agartha$tickStarted;

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void agartha$onTickStart(CallbackInfo callbackInfo) {
      this.agartha$tickStarted = Profiler.begin();
      TickBudget.beginTick();
      EventBus.INSTANCE.drainAsync();
      TickEvent.dispatch();
   }

   @Inject(
      method = {"render"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/systems/RenderSystem;executePendingTasks()V"
      )}
   )
   private void agartha$scanFrame(boolean bl, CallbackInfo callbackInfo) {
      if (((MinecraftClient)(Object)this).world != null) {
         TickBudget.beginFrame();
         ScanFrameEvent.dispatch();
      }
   }

   @Inject(
      method = {"tick"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/screen/Screen;tick()V"
      )}
   )
   private void agartha$disconnectTick(CallbackInfo callbackInfo) {
      if (((MinecraftClient)(Object)this).currentScreen instanceof AgarthaDisconnectTick var2) {
         var2.agartha$tickReconnect();
      }
   }

   @Inject(
      method = {"tick"},
      at = {@At("RETURN")}
   )
   private void agartha$onTickEnd(CallbackInfo callbackInfo) {
      EndClientTickEvent.dispatch();
      Profiler.record("client.tick", this.agartha$tickStarted);
      Profiler.flushIfDue();
   }
}
