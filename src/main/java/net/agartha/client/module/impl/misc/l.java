package net.agartha.client.module.impl.misc;

import net.agartha.client.util.ab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class l extends net.agartha.client.module.b {
   private final net.agartha.client.module.a_pkg.i home = this.addSetting(new net.agartha.client.module.a_pkg.i("Home", "1", "1", "2", "3"));
   private final net.agartha.client.module.a_pkg.b donutPlus = this.addSetting(new net.agartha.client.module.a_pkg.b("Donut+ Mode", false));
   private final net.agartha.client.module.a_pkg.j homeSlot = this.addSetting(new net.agartha.client.module.a_pkg.j("Home Slot", 1.0F, 1.0F, 99.0F).whole());
   private final net.agartha.client.module.a_pkg.b chatFeedback = this.addSetting(new net.agartha.client.module.a_pkg.b("Chat Feedback", true));
   private final net.agartha.client.module.a_pkg.j commandDelay = this.addSetting(new net.agartha.client.module.a_pkg.j("Command Delay", 750.0F, 100.0F, 3000.0F));

   public l() {
      super("Home Reset", "Deletes the chosen home and sets it again where you stand.", net.agartha.client.module.c.MISC);
      this.home.visibleWhen(() -> !this.donutPlus.get());
      this.homeSlot.visibleWhen(this.donutPlus::get);
   }

   private int a() {
      if (this.donutPlus.get()) {
         return Math.max(1, Math.min(99, this.homeSlot.getInt()));
      } else {
         try {
            return Integer.parseInt(this.home.get());
         } catch (NumberFormatException var2) {
            return 1;
         }
      }
   }

   @Override
   protected void onEnable() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.player != null && var1.world != null) {
         int var2 = this.a();
         if (net.agartha.client.util.f.allowAutomated("/delhome " + var2, "Home Reset")) {
            var1.player.networkHandler.sendChatCommand("delhome " + var2);
            Thread var3 = new Thread(() -> {
               try {
                  Thread.sleep(this.commandDelay.getInt());
                  var1.execute(() -> {
                     if (var1.player != null && var1.world != null) {
                        var1.player.networkHandler.sendChatCommand("sethome " + var2);
                     }
                  });
               } catch (InterruptedException var4) {
                  Thread.currentThread().interrupt();
               }
            }, "Agartha-HomeReset");
            var3.setDaemon(true);
            var3.start();
            if (this.chatFeedback.get()) {
               ab.send("Home Reset", "Reset home " + var2);
            }

            this.setEnabled(false);
         }
      } else {
         this.setEnabled(false);
      }
   }
}
