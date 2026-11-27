package net.agartha.client.mixin;

import net.agartha.client.event.impl.Render2DEvent;
import net.agartha.client.gui.CrosshairHud;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.client.CrosshairModule;
import net.agartha.client.module.impl.misc.FakeScoreboardModule;
import net.agartha.client.module.impl.misc.NameProtectModule;
import net.agartha.client.module.impl.render.FreelookModule;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({InGameHud.class})
public abstract class MixinGui {
   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   private void agartha$onRender2D(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
      Render2DEvent.dispatch(drawContext, renderTickCounter.getTickProgress(false));
   }

   @Inject(
      method = {"renderStatusEffectOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$cancelVanillaEffects(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
      callbackInfo.cancel();
   }

   @Inject(
      method = {"renderCrosshair"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void agartha$customCrosshair(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
      if (FreelookModule.hidesCrosshair()) {
         callbackInfo.cancel();
      } else {
         CrosshairModule var4 = ModuleManager.get(CrosshairModule.class);
         if (var4 != null && var4.isEnabled()) {
            NoRenderModule var5 = ModuleManager.get(NoRenderModule.class);
            if (var5 == null || !var5.noCrosshair()) {
               if (CrosshairHud.shouldDraw(MinecraftClient.getInstance())) {
                  callbackInfo.cancel();
                  CrosshairHud.render(drawContext, var4);
               }
            }
         }
      }
   }

   @Inject(
      method = {"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"},
      at = {@At("HEAD")}
   )
   private void agartha$beginFakeScoreboard(DrawContext drawContext, ScoreboardObjective scoreboardObjective, CallbackInfo callbackInfo) {
      FakeScoreboardModule.beginPass();
   }

   @Redirect(
      method = {"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Lnet/minecraft/text/StringVisitable;)I",
         ordinal = 0
      )
   )
   private int agartha$fakeScoreboardTitleWidth(TextRenderer textRenderer, StringVisitable stringVisitable) {
      if (stringVisitable instanceof Text var3) {
         stringVisitable = FakeScoreboardModule.rewriteTitle(this.agartha$protectScoreLine(var3));
      }

      return textRenderer.getWidth(stringVisitable) + FakeScoreboardModule.sidebarGap();
   }

   @ModifyArg(
      method = {"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V",
         ordinal = 0
      ),
      index = 1
   )
   private Text agartha$fakeScoreboardTitle(Text text) {
      return FakeScoreboardModule.rewriteTitle(this.agartha$protectScoreLine(text));
   }

   @Redirect(
      method = {"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Lnet/minecraft/text/StringVisitable;)I",
         ordinal = 1
      )
   )
   private int agartha$fakeScoreboardWidth(TextRenderer textRenderer, StringVisitable stringVisitable) {
      if (stringVisitable instanceof Text var3) {
         stringVisitable = FakeScoreboardModule.rewriteForWidth(this.agartha$protectScoreLine(var3));
      }

      return textRenderer.getWidth(stringVisitable) + FakeScoreboardModule.sidebarGap();
   }

   @ModifyArg(
      method = {"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V",
         ordinal = 1
      ),
      index = 1
   )
   private Text agartha$fakeScoreboardLine(Text text) {
      return FakeScoreboardModule.rewriteForDraw(this.agartha$protectScoreLine(text));
   }

   private Text agartha$protectScoreLine(Text text) {
      FakeScoreboardModule var2 = ModuleManager.get(FakeScoreboardModule.class);
      if (var2 != null && var2.isEnabled()) {
         return text;
      } else {
         try {
            NameProtectModule var3 = ModuleManager.get(NameProtectModule.class);
            if (var3 != null && var3.isEnabled()) {
               return var3.censorScoreboardLine(text);
            }
         } catch (Throwable var4) {
         }

         return text;
      }
   }

   @ModifyArg(
      method = {"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V",
         ordinal = 2
      ),
      index = 1
   )
   private Text agartha$fakeScoreboardScore(Text text) {
      return FakeScoreboardModule.rewriteScore(text);
   }
}
