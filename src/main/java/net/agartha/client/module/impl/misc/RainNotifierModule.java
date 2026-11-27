package net.agartha.client.module.impl.misc;

import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.util.net.ChatSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

@Environment(EnvType.CLIENT)
public final class RainNotifierModule extends Module {
   private static final float STARTS = 0.9F;
   private static final float ENDS = 0.2F;
   private ClientWorld level;
   private boolean storming;

   public RainNotifierModule() {
      super("Rain Notifier", "Tells you in chat when a thunderstorm starts.", ModuleCategory.MISC);
   }

   @Override
   protected void onEnable() {
      this.level = null;
   }

   @EventListener
   public void onTick(TickEvent arg) {
      ClientWorld var2 = MinecraftClient.getInstance().world;
      if (var2 == null) {
         this.level = null;
      } else {
         float var3 = var2.getDimension().hasSkyLight() && !var2.getDimension().hasCeiling() ? var2.getThunderGradient(1.0F) : 0.0F;
         boolean var4 = this.storming ? var3 > 0.2F : var3 > 0.9F;
         if (var2 != this.level) {
            this.level = var2;
            this.storming = var4;
         } else {
            if (var4 && !this.storming) {
               ChatSender.send("Rain Notifier", "A thunderstorm has started.");
            }

            this.storming = var4;
         }
      }
   }
}
