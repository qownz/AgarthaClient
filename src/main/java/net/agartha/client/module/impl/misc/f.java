package net.agartha.client.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class f extends net.agartha.client.module.b {
   public f() {
      super("Auto Walk", "Walks forward for you.", net.agartha.client.module.c.MISC);
   }

   @Override
   protected void onDisable() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.options != null) {
         var1.options.forwardKey.setPressed(false);
      }
   }

   @net.agartha.client.d.f
   public void onTick(net.agartha.client.d.a_pkg.q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null && var2.world != null) {
         if (var2.currentScreen == null) {
            var2.options.forwardKey.setPressed(true);
         }
      }
   }
}
