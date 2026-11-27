package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.render.NetheriteFinderModule;
import net.agartha.client.module.impl.render.b;
import net.agartha.client.module.impl.render.c;
import net.agartha.client.module.impl.render.i;
import net.agartha.client.module.impl.render.m;
import net.agartha.client.module.impl.render.q;
import net.agartha.client.module.impl.render.r;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({Entity.class})
public class MixinEntity {
   @Inject(
      method = {"isGlowing"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$isCurrentlyGlowing(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
      if (!b.anyActive()) {
         m var4 = e.get(m.class);
         if (var4 != null && var4.noGlowing()) {
            callbackInfoReturnable.setReturnValue(false);
         }
      } else {
         Entity var2 = (Entity)(Object)this;
         if ((!b.overlayActive() || !r.shouldGlow(var2))
            && (!b.playerActive() || !q.shouldGlow(var2))
            && (!b.espActive() || !c.shouldGlow(var2))
            && (!b.itemActive() || !i.shouldGlow(var2))
            && (!b.netheriteActive() || !NetheriteFinderModule.shouldGlow(var2))) {
            m var3 = e.get(m.class);
            if (var3 != null && var3.noGlowing()) {
               callbackInfoReturnable.setReturnValue(false);
            }
         } else {
            callbackInfoReturnable.setReturnValue(true);
         }
      }
   }

   @Inject(
      method = {"getTeamColorValue"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$getTeamColor(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
      if (b.anyActive()) {
         Entity var2 = (Entity)(Object)this;
         if (b.overlayActive() && r.shouldGlow(var2)) {
            callbackInfoReturnable.setReturnValue(r.glowColor(var2));
         } else if (b.playerActive() && q.shouldGlow(var2)) {
            callbackInfoReturnable.setReturnValue(q.glowColor(var2));
         } else if (b.espActive() && c.shouldGlow(var2)) {
            callbackInfoReturnable.setReturnValue(c.glowColor(var2));
         } else if (b.itemActive() && i.shouldGlow(var2)) {
            callbackInfoReturnable.setReturnValue(i.glowColor(var2));
         } else if (b.netheriteActive() && NetheriteFinderModule.shouldGlow(var2)) {
            callbackInfoReturnable.setReturnValue(NetheriteFinderModule.glowColor(var2));
         }
      }
   }

   @Inject(
      method = {"isInvisibleTo"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noInvisibility(PlayerEntity playerEntity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
      m var3 = e.get(m.class);
      if (var3 != null && var3.noInvisibility()) {
         callbackInfoReturnable.setReturnValue(false);
      }
   }
}
