package net.agartha.client.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class b extends net.agartha.client.module.b {
   public b() {
      super("Auto Jump", "Jumps again every time you land.", net.agartha.client.module.c.MISC);
   }

   @net.agartha.client.d.f
   public void onTick(net.agartha.client.d.a_pkg.q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null && var2.world != null) {
         if (var2.currentScreen == null && !var2.player.isSneaking()) {
            if (var2.player.isOnGround()) {
               var2.player.jump();
            }
         }
      }
   }
}
