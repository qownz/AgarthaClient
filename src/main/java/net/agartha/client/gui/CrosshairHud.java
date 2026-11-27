package net.agartha.client.gui;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.client.CrosshairModule;
import net.agartha.client.module.impl.render.FreelookModule;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.option.Perspective;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttackRangeComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;

@Environment(EnvType.CLIENT)
public final class CrosshairHud {
   private static final Identifier CROSSHAIR_SPRITE = Identifier.of("minecraft", "hud/crosshair");
   private static final Identifier ATTACK_INDICATOR_FULL_SPRITE = Identifier.of("minecraft", "hud/crosshair_attack_indicator_full");
   private static final Identifier ATTACK_INDICATOR_BACKGROUND_SPRITE = Identifier.of("minecraft", "hud/crosshair_attack_indicator_background");
   private static final Identifier ATTACK_INDICATOR_PROGRESS_SPRITE = Identifier.of("minecraft", "hud/crosshair_attack_indicator_progress");
   private static final float MIN_CROSSHAIR_ATTACK_SPEED = 5.0F;

   private CrosshairHud() {
   }

   public static void render(DrawContext drawContext, CrosshairModule crosshairModule) {
      if (crosshairModule != null && crosshairModule.isEnabled()) {
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (shouldDraw(var2)) {
            int var3 = var2.getWindow().getScaledWidth();
            int var4 = var2.getWindow().getScaledHeight();
            int var5 = crosshairModule.drawSizePx();
            int var6 = (var3 - var5) / 2;
            int var7 = (var4 - var5) / 2;
            drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, CROSSHAIR_SPRITE, var6, var7, var5, var5, crosshairModule.tintArgb());
            a(drawContext, var2);
         }
      }
   }

   public static void renderFromScreen(DrawContext drawContext) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.world != null && !var1.options.hudHidden && !FreelookModule.hidesCrosshair()) {
         NoRenderModule var2 = ModuleManager.get(NoRenderModule.class);
         if (var2 == null || !var2.noCrosshair()) {
            CrosshairModule var3 = ModuleManager.get(CrosshairModule.class);
            if (var3 != null && var3.isEnabled()) {
               render(drawContext, var3);
            } else if (shouldDraw(var1)) {
               byte var4 = 15;
               int var5 = (var1.getWindow().getScaledWidth() - var4) / 2;
               int var6 = (var1.getWindow().getScaledHeight() - var4) / 2;
               drawContext.drawGuiTexture(RenderPipelines.CROSSHAIR, CROSSHAIR_SPRITE, var5, var6, var4, var4);
            }
         }
      }
   }

   public static float[] overlayBox() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      if (var0.world != null && !var0.options.hudHidden && shouldDraw(var0)) {
         NoRenderModule var1 = ModuleManager.get(NoRenderModule.class);
         if (var1 != null && var1.noCrosshair()) {
            return null;
         } else {
            CrosshairModule var2 = ModuleManager.get(CrosshairModule.class);
            int var3 = var2 != null && var2.isEnabled() ? var2.drawSizePx() : 15;
            float var4 = var0.getWindow().getScaleFactor();
            float var5 = var3 * var4 * 0.5F;
            float var6 = var0.getWindow().getFramebufferWidth() * 0.5F;
            float var7 = var0.getWindow().getFramebufferHeight() * 0.5F;
            return new float[]{var6 - var5, var7 - var5, var6 + var5, var7 + var5};
         }
      } else {
         return null;
      }
   }

   private static void a(DrawContext drawContext, MinecraftClient minecraftClient) {
      if (minecraftClient.options.getAttackIndicator().getValue() == AttackIndicator.CROSSHAIR) {
         ClientPlayerEntity var2 = minecraftClient.player;
         if (var2 != null) {
            float var3 = var2.getAttackCooldownProgress(0.0F);
            boolean var4 = false;
            Entity var5 = minecraftClient.targetedEntity;
            if (var5 instanceof LivingEntity && var3 >= 1.0F) {
               var4 = var2.getAttackCooldownProgressPerTick() > 5.0F;
               var4 &= var5.isAlive();
               AttackRangeComponent var6 = (AttackRangeComponent)var2.getActiveOrMainHandStack().get(DataComponentTypes.ATTACK_RANGE);
               if (var6 != null) {
                  HitResult var7 = minecraftClient.crosshairTarget;
                  var4 &= var7 != null && var6.isWithinRange(var2, var7.getPos());
               }
            }

            int var10 = drawContext.getScaledWindowHeight() / 2 - 7 + 16;
            int var11 = drawContext.getScaledWindowWidth() / 2 - 8;
            if (var4) {
               drawContext.drawGuiTexture(RenderPipelines.CROSSHAIR, ATTACK_INDICATOR_FULL_SPRITE, var11, var10, 16, 16);
            } else if (var3 < 1.0F) {
               int var8 = (int)(var3 * 17.0F);
               drawContext.drawGuiTexture(RenderPipelines.CROSSHAIR, ATTACK_INDICATOR_BACKGROUND_SPRITE, var11, var10, 16, 4);
               drawContext.drawGuiTexture(RenderPipelines.CROSSHAIR, ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, var11, var10, var8, 4);
            }
         }
      }
   }

   public static boolean shouldDraw(MinecraftClient minecraftClient) {
      if (minecraftClient.player == null) {
         return false;
      } else if (minecraftClient.options.hudHidden) {
         return false;
      } else if (minecraftClient.options.getPerspective() != Perspective.FIRST_PERSON) {
         return false;
      } else {
         ClientPlayerEntity var1 = minecraftClient.player;
         return var1.isSpectator() ? minecraftClient.options.spectatorHotbarKey.isPressed() : true;
      }
   }
}
