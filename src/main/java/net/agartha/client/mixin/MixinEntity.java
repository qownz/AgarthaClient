package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.EspActiveCache;
import net.agartha.client.module.impl.render.EspModule;
import net.agartha.client.module.impl.render.ItemEspModule;
import net.agartha.client.module.impl.render.NetheriteFinderModule;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.agartha.client.module.impl.render.PlayerEspModule;
import net.agartha.client.module.impl.render.PlayerOverlayModule;
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
      if (!EspActiveCache.anyActive()) {
         NoRenderModule var4 = ModuleManager.get(NoRenderModule.class);
         if (var4 != null && var4.noGlowing()) {
            callbackInfoReturnable.setReturnValue(false);
         }
      } else {
         Entity var2 = (Entity)(Object)this;
         if ((!EspActiveCache.overlayActive() || !PlayerOverlayModule.shouldGlow(var2))
            && (!EspActiveCache.playerActive() || !PlayerEspModule.shouldGlow(var2))
            && (!EspActiveCache.espActive() || !EspModule.shouldGlow(var2))
            && (!EspActiveCache.itemActive() || !ItemEspModule.shouldGlow(var2))
            && (!EspActiveCache.netheriteActive() || !NetheriteFinderModule.shouldGlow(var2))) {
            NoRenderModule var3 = ModuleManager.get(NoRenderModule.class);
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
      if (EspActiveCache.anyActive()) {
         Entity var2 = (Entity)(Object)this;
         if (EspActiveCache.overlayActive() && PlayerOverlayModule.shouldGlow(var2)) {
            callbackInfoReturnable.setReturnValue(PlayerOverlayModule.glowColor(var2));
         } else if (EspActiveCache.playerActive() && PlayerEspModule.shouldGlow(var2)) {
            callbackInfoReturnable.setReturnValue(PlayerEspModule.glowColor(var2));
         } else if (EspActiveCache.espActive() && EspModule.shouldGlow(var2)) {
            callbackInfoReturnable.setReturnValue(EspModule.glowColor(var2));
         } else if (EspActiveCache.itemActive() && ItemEspModule.shouldGlow(var2)) {
            callbackInfoReturnable.setReturnValue(ItemEspModule.glowColor(var2));
         } else if (EspActiveCache.netheriteActive() && NetheriteFinderModule.shouldGlow(var2)) {
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
      NoRenderModule var3 = ModuleManager.get(NoRenderModule.class);
      if (var3 != null && var3.noInvisibility()) {
         callbackInfoReturnable.setReturnValue(false);
      }
   }
}
