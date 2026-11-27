package net.agartha.client.module.impl.misc;

import net.agartha.client.util.ab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

@Environment(EnvType.CLIENT)
public final class n extends net.agartha.client.module.b {
   private static final float STARTS = 0.9F;
   private static final float ENDS = 0.2F;
   private ClientWorld level;
   private boolean storming;

   public n() {
      super("Rain Notifier", "Tells you in chat when a thunderstorm starts.", net.agartha.client.module.c.MISC);
   }

   @Override
   protected void onEnable() {
      this.level = null;
   }

   @net.agartha.client.d.f
   public void onTick(net.agartha.client.d.a_pkg.q arg) {
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
               ab.send("Rain Notifier", "A thunderstorm has started.");
            }

            this.storming = var4;
         }
      }
   }
}
