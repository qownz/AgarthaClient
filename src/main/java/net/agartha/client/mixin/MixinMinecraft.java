package net.agartha.client.mixin;

import net.agartha.client.compat.AgarthaDisconnectTick;
import net.agartha.client.d.d;
import net.agartha.client.d.a_pkg.c;
import net.agartha.client.d.a_pkg.p;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.k.a;
import net.agartha.client.k.b_o;
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
      this.agartha$tickStarted = b_o.begin();
      a.beginTick();
      d.INSTANCE.drainAsync();
      q.dispatch();
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
         a.beginFrame();
         p.dispatch();
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
      c.dispatch();
      b_o.record("client.tick", this.agartha$tickStarted);
      b_o.flushIfDue();
   }
}
