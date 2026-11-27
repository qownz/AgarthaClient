package net.agartha.client.mixin;

import net.agartha.client.module.e;
import net.agartha.client.module.impl.render.m;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({BossBarHud.class})
public abstract class MixinNoRenderBossBar {
   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noBossBar(DrawContext drawContext, CallbackInfo callbackInfo) {
      m var3 = e.get(m.class);
      if (var3 != null && var3.noBossBar()) {
         callbackInfo.cancel();
      }
   }
}
