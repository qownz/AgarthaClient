package net.agartha.client.mixin;

import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.misc.AutoReconnectModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ConnectScreen.class})
public abstract class MixinConnectScreen {
   @Inject(
      method = {"connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;Lnet/minecraft/client/network/CookieStorage;)V"},
      at = {@At("HEAD")}
   )
   private static void agartha$captureLastServer(
      MinecraftClient minecraftClient, ServerAddress serverAddress, ServerInfo serverInfo, CookieStorage cookieStorage, CallbackInfo callbackInfo
   ) {
      AutoReconnectModule var5 = ModuleManager.get(AutoReconnectModule.class);
      if (var5 != null) {
         var5.setLastServerConnection(serverAddress, serverInfo);
      }
   }
}
