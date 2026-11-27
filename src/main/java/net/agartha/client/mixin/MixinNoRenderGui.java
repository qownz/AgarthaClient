package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({InGameHud.class})
public abstract class MixinNoRenderGui {
   private static NoRenderModule noRender() {
      return ModuleManager.get(NoRenderModule.class);
   }

   @Inject(
      method = {"renderCrosshair"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noCrosshair(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
      NoRenderModule var4 = noRender();
      if (var4 != null && var4.noCrosshair()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noScoreboardDisplay(DrawContext drawContext, ScoreboardObjective scoreboardObjective, CallbackInfo callbackInfo) {
      NoRenderModule var4 = noRender();
      if (var4 != null && var4.noScoreboard()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noScoreboard(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
      NoRenderModule var4 = noRender();
      if (var4 != null && var4.noScoreboard()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"renderTitleAndSubtitle"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noTitle(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
      NoRenderModule var4 = noRender();
      if (var4 != null && var4.noTitle()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"renderVignetteOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noVignette(DrawContext drawContext, Entity entity, CallbackInfo callbackInfo) {
      NoRenderModule var4 = noRender();
      if (var4 != null && var4.noVignette()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"renderPortalOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noPortal(DrawContext drawContext, float f, CallbackInfo callbackInfo) {
      NoRenderModule var4 = noRender();
      if (var4 != null && var4.noPortalOverlay()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"renderSpyglassOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noSpyglass(DrawContext drawContext, float f, CallbackInfo callbackInfo) {
      NoRenderModule var4 = noRender();
      if (var4 != null && var4.noSpyglassOverlay()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"renderNauseaOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noNausea(DrawContext drawContext, float f, CallbackInfo callbackInfo) {
      NoRenderModule var4 = noRender();
      if (var4 != null && var4.noNausea()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"renderHeldItemTooltip"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noHeldItemName(DrawContext drawContext, CallbackInfo callbackInfo) {
      NoRenderModule var3 = noRender();
      if (var3 != null && var3.noHeldItemName()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"renderStatusEffectOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noPotionIcons(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
      NoRenderModule var4 = noRender();
      if (var4 != null && var4.noPotionIcons()) {
         callbackInfo.cancel();
      }
   }

   @Inject(
      method = {"renderOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$noTextureOverlays(DrawContext drawContext, Identifier identifier, float f, CallbackInfo callbackInfo) {
      NoRenderModule var5 = noRender();
      if (var5 != null) {
         String var6 = identifier.getPath();
         if (var5.noPumpkinOverlay() && var6.contains("pumpkin")) {
            callbackInfo.cancel();
         } else {
            if (var5.noPowderedSnowOverlay() && var6.contains("powder_snow")) {
               callbackInfo.cancel();
            }
         }
      }
   }
}
