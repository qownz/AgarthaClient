package net.agartha.client.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

@Environment(EnvType.CLIENT)
public final class d extends net.agartha.client.module.b {
   private final net.agartha.client.module.a_pkg.j delay = this.addSetting(new net.agartha.client.module.a_pkg.j("Delay", 3.5F, 0.0F, 30.0F));
   private final net.agartha.client.module.a_pkg.b hideButtons = this.addSetting(new net.agartha.client.module.a_pkg.b("Hide Buttons", false));
   private d.a lastServerConnection;
   private boolean relogReconnectActive;
   private int relogAttemptsRemaining;
   private long relogNextAttemptAtMs;
   private float relogRetryDelaySeconds;

   public d() {
      super("Auto Reconnect", "Automatically reconnects when disconnected from a server.", net.agartha.client.module.c.MISC);
      net.agartha.client.d.a_pkg.q.TYPE.listen(this, arg -> this.tickReconnect(MinecraftClient.getInstance()));
   }

   public float delay() {
      return this.delay.get();
   }

   public boolean hideButtons() {
      return this.hideButtons.get();
   }

   public d.a lastServerConnection() {
      return this.lastServerConnection;
   }

   public boolean hasRelogReconnect() {
      return this.relogReconnectActive && this.lastServerConnection != null;
   }

   public float relogCountdownSeconds() {
      if (!this.hasRelogReconnect()) {
         return 0.0F;
      } else {
         long var1 = Math.max(0L, this.relogNextAttemptAtMs - System.currentTimeMillis());
         return (float)var1 / 1000.0F;
      }
   }

   public int relogAttemptsRemaining() {
      return this.relogAttemptsRemaining;
   }

   public void beginRelogReconnect(float f, int i) {
      if (this.lastServerConnection != null) {
         this.relogReconnectActive = true;
         this.relogRetryDelaySeconds = Math.max(0.5F, f);
         this.relogAttemptsRemaining = Math.max(1, i);
         this.relogNextAttemptAtMs = System.currentTimeMillis() + (long)(this.relogRetryDelaySeconds * 1000.0F);
      }
   }

   public void cancelRelogReconnect() {
      this.relogReconnectActive = false;
      this.relogAttemptsRemaining = 0;
      this.relogNextAttemptAtMs = 0L;
      this.relogRetryDelaySeconds = 0.0F;
   }

   public void setLastServerConnection(ServerAddress serverAddress, ServerInfo serverInfo) {
      if (serverAddress == null) {
         this.lastServerConnection = null;
         this.cancelRelogReconnect();
      } else {
         this.lastServerConnection = new d.a(serverAddress, serverInfo);
      }
   }

   public void tickReconnect(MinecraftClient minecraftClient) {
      if (this.hasRelogReconnect()) {
         if (minecraftClient != null) {
            if (minecraftClient.world != null) {
               this.cancelRelogReconnect();
            } else if (!(minecraftClient.currentScreen instanceof ConnectScreen)) {
               long var2 = System.currentTimeMillis();
               if (this.relogAttemptsRemaining <= 0) {
                  this.cancelRelogReconnect();
               } else if (var2 >= this.relogNextAttemptAtMs) {
                  this.relogAttemptsRemaining--;
                  this.relogNextAttemptAtMs = var2 + (long)(this.relogRetryDelaySeconds * 1000.0F);
                  ConnectScreen.connect(a(), minecraftClient, this.lastServerConnection.address(), this.lastServerConnection.info(), false, null);
               }
            }
         }
      }
   }

   private static Screen a() {
      return new MultiplayerScreen(new TitleScreen());
   }

   @Environment(EnvType.CLIENT)
   public record a(ServerAddress address, ServerInfo info) {
   }
}
