package net.agartha.client.module.impl.misc;

import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.util.net.ChatSender;
import net.agartha.client.util.net.MoneyCommandGuard;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class HomeResetModule extends Module {
   private final EnumSetting home = this.addSetting(new EnumSetting("Home", "1", "1", "2", "3"));
   private final BooleanSetting donutPlus = this.addSetting(new BooleanSetting("Donut+ Mode", false));
   private final NumberSetting homeSlot = this.addSetting(new NumberSetting("Home Slot", 1.0F, 1.0F, 99.0F).whole());
   private final BooleanSetting chatFeedback = this.addSetting(new BooleanSetting("Chat Feedback", true));
   private final NumberSetting commandDelay = this.addSetting(new NumberSetting("Command Delay", 750.0F, 100.0F, 3000.0F));

   public HomeResetModule() {
      super("Home Reset", "Deletes the chosen home and sets it again where you stand.", ModuleCategory.MISC);
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
         if (MoneyCommandGuard.allowAutomated("/delhome " + var2, "Home Reset")) {
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
               ChatSender.send("Home Reset", "Reset home " + var2);
            }

            this.setEnabled(false);
         }
      } else {
         this.setEnabled(false);
      }
   }
}
