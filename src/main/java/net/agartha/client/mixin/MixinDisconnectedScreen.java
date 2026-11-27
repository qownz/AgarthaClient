package net.agartha.client.mixin;

import net.agartha.client.compat.AgarthaDisconnectTick;
import net.agartha.client.module.e;
import net.agartha.client.module.impl.misc.d;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ButtonWidget.Builder;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({DisconnectedScreen.class})
public abstract class MixinDisconnectedScreen implements AgarthaDisconnectTick {
   @Shadow
   @Final
   private DirectionalLayoutWidget grid;
   @Unique
   private ButtonWidget agartha$reconnectButton;
   @Unique
   private double agartha$timeTicks = initialDelayTicks();

   @Inject(
      method = {"init"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;refreshPositions()V",
         shift = Shift.BEFORE
      )}
   )
   private void agartha$addReconnectButtons(CallbackInfo callbackInfo) {
      d var2 = e.get(d.class);
      if (var2 != null && var2.lastServerConnection() != null && !var2.hideButtons()) {
         this.agartha$reconnectButton = new Builder(Text.literal(this.agartha$getReconnectText()), buttonWidget -> this.agartha$tryConnecting()).build();
         this.grid.add(this.agartha$reconnectButton);
         this.grid.add(new Builder(Text.literal("Toggle Auto Reconnect"), buttonWidget -> {
            var2.toggle();
            this.agartha$timeTicks = var2.delay() * 20.0F;
            this.agartha$reconnectButton.setMessage(Text.literal(this.agartha$getReconnectText()));
         }).build());
      }
   }

   @Override
   public void agartha$tickReconnect() {
      d var1 = e.get(d.class);
      if (var1 != null && var1.lastServerConnection() != null) {
         if (var1.hasRelogReconnect()) {
            if (this.agartha$reconnectButton != null) {
               this.agartha$reconnectButton.setMessage(Text.literal(this.agartha$getReconnectText()));
            }
         } else if (var1.isEnabled()) {
            if (this.agartha$timeTicks <= 0.0) {
               this.agartha$timeTicks = initialDelayTicks();
               this.agartha$tryConnecting();
            } else {
               this.agartha$timeTicks--;
               if (this.agartha$reconnectButton != null) {
                  this.agartha$reconnectButton.setMessage(Text.literal(this.agartha$getReconnectText()));
               }
            }
         }
      }
   }

   @Unique
   private static double initialDelayTicks() {
      d var0 = e.get(d.class);
      return var0 != null ? var0.delay() * 20.0F : 0.0;
   }

   @Unique
   private String agartha$getReconnectText() {
      d var1 = e.get(d.class);
      if (var1 == null) {
         return "Reconnect";
      } else {
         String var2 = "Reconnect";
         if (var1.hasRelogReconnect()) {
            var2 = var2 + " " + String.format("(%.1f)", var1.relogCountdownSeconds());
            var2 = var2 + " [" + var1.relogAttemptsRemaining() + "]";
         } else if (var1.isEnabled()) {
            var2 = var2 + " " + String.format("(%.1f)", this.agartha$timeTicks / 20.0);
         }

         return var2;
      }
   }

   @Unique
   private void agartha$tryConnecting() {
      d var1 = e.get(d.class);
      if (var1 != null && var1.lastServerConnection() != null) {
         d.a var2 = var1.lastServerConnection();
         ConnectScreen.connect(new TitleScreen(), MinecraftClient.getInstance(), var2.address(), var2.info(), false, null);
      }
   }
}
