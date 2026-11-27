package net.agartha.client.module.impl.misc;

import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class AutoWalkModule extends Module {
   public AutoWalkModule() {
      super("Auto Walk", "Walks forward for you.", ModuleCategory.MISC);
   }

   @Override
   protected void onDisable() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.options != null) {
         var1.options.forwardKey.setPressed(false);
      }
   }

   @EventListener
   public void onTick(TickEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null && var2.world != null) {
         if (var2.currentScreen == null) {
            var2.options.forwardKey.setPressed(true);
         }
      }
   }
}
