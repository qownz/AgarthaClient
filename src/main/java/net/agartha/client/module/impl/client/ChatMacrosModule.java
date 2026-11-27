package net.agartha.client.module.impl.client;

import net.agartha.client.event.EventListener;
import net.agartha.client.event.EventPriority;
import net.agartha.client.event.impl.ChatSendEvent;
import net.agartha.client.event.impl.KeyInputEvent;
import net.agartha.client.event.impl.MouseInputEvent;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.macro.ChatMacro;
import net.agartha.client.macro.MacroManager;
import net.agartha.client.macro.MacroScheduler;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class ChatMacrosModule extends Module {
   public ChatMacrosModule() {
      super("Chat Macros", "Keybind-triggered chat message macros.", ModuleCategory.CLIENT);
      this.setEnabled(true);
   }

   protected void onDisable() {
      MacroScheduler.clearAll();
   }

   @EventListener
   public void onChatSend(ChatSendEvent arg) {
      if (this.isEnabled()) {
         if (MacroScheduler.tryHandleCommand(arg.message)) {
            arg.setCancelled(true);
         }
      }
   }

   @EventListener
   public void onTick(TickEvent arg) {
      if (this.isEnabled()) {
         MacroScheduler.tick();
      }
   }

   @EventListener(
      priority = EventPriority.HIGH
   )
   public void onKey(KeyInputEvent arg) {
      if (this.isEnabled() && arg.action != 0) {
         if (canTriggerInWorld()) {
            for (ChatMacro var3 : MacroManager.getInstance().all()) {
               if (var3.onAction(true, arg.key, arg.mods)) {
                  return;
               }
            }
         }
      }
   }

   @EventListener(
      priority = EventPriority.HIGH
   )
   public void onMouse(MouseInputEvent arg) {
      if (this.isEnabled() && arg.action != 0) {
         if (canTriggerInWorld()) {
            for (ChatMacro var3 : MacroManager.getInstance().all()) {
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
