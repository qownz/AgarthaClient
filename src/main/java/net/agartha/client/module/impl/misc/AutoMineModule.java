package net.agartha.client.module.impl.misc;

import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.EndClientTickEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class AutoMineModule extends Module {
   public AutoMineModule() {
      super("Auto Mine", "Continuously holds left click for mining.", ModuleCategory.MISC);
   }

   @Override
   protected void onDisable() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.options != null) {
         var1.options.attackKey.setPressed(false);
      }
   }

   @EventListener
   public void onTick(EndClientTickEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null && var2.world != null) {
         if (var2.currentScreen == null && !var2.player.isUsingItem()) {
            var2.options.attackKey.setPressed(true);
         }
      }
   }
}
