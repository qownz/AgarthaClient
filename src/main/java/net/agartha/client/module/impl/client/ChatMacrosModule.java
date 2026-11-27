package net.agartha.client.module.impl.client;

import net.agartha.client.d.f;
import net.agartha.client.d.i;
import net.agartha.client.d.a_pkg.g;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.j.a;
import net.agartha.client.j.d;
import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class ChatMacrosModule extends b {
   public ChatMacrosModule() {
      super("Chat Macros", "Keybind-triggered chat message macros.", c.CLIENT);
      this.setEnabled(true);
   }

   protected void onDisable() {
      net.agartha.client.j.b.clearAll();
   }

   @f
   public void onChatSend(net.agartha.client.d.a_pkg.b arg) {
      if (this.isEnabled()) {
         if (net.agartha.client.j.b.tryHandleCommand(arg.message)) {
            arg.setCancelled(true);
         }
      }
   }

   @f
   public void onTick(q arg) {
      if (this.isEnabled()) {
         net.agartha.client.j.b.tick();
      }
   }

   @f(
      priority = i.HIGH
   )
   public void onKey(net.agartha.client.d.a_pkg.f arg) {
      if (this.isEnabled() && arg.action != 0) {
         if (canTriggerInWorld()) {
            for (a var3 : d.getInstance().all()) {
               if (var3.onAction(true, arg.key, arg.mods)) {
                  return;
               }
            }
         }
      }
   }

   @f(
      priority = i.HIGH
   )
   public void onMouse(g arg) {
      if (this.isEnabled() && arg.action != 0) {
         if (canTriggerInWorld()) {
            for (a var3 : d.getInstance().all()) {
               if (var3.onAction(false, arg.button, arg.mods)) {
                  return;
               }
            }
         }
      }
   }

   private static boolean canTriggerInWorld() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      return var0.player != null && var0.currentScreen == null;
   }
}
