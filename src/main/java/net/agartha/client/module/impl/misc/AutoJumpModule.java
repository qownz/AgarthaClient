package net.agartha.client.module.impl.misc;

import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class AutoJumpModule extends Module {
   public AutoJumpModule() {
      super("Auto Jump", "Jumps again every time you land.", ModuleCategory.MISC);
   }

   @EventListener
   public void onTick(TickEvent arg) {
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
