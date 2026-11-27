package net.agartha.client.j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.agartha.client.module.impl.client.ChatMacrosModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class a {
   private String name = "";
   private final List<String> messages = new ArrayList<>();
   private e keybind = new e();

   public a() {
   }

   public a(String string, List<String> list, e arg) {
      this.name = string == null ? "" : string;
      if (list != null) {
         this.messages.addAll(list);
      }

      this.keybind = arg == null ? new e() : arg.copy();
   }

   public a copy() {
      return new a(this.name, this.messages, this.keybind);
   }

   public String name() {
      return this.name;
   }

   public void setName(String string) {
      this.name = string == null ? "" : string;
   }

   public List<String> messages() {
      return this.messages;
   }

   public void setMessages(List<String> list) {
      this.messages.clear();
      if (list != null) {
         this.messages.addAll(list);
      }
   }

   public e keybind() {
      return this.keybind;
   }

   public void setKeybind(e arg) {
      this.keybind = arg == null ? new e() : arg.copy();
   }

   public boolean onAction(boolean bl, int i, int j) {
      MinecraftClient var4 = MinecraftClient.getInstance();
      return this.keybind.matches(bl, i, j) && var4.currentScreen == null ? this.onAction() : false;
   }

   public boolean onAction() {
      ChatMacrosModule var1 = net.agartha.client.module.e.get(ChatMacrosModule.class);
      if (var1 != null && var1.isEnabled()) {
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (var2.player == null) {
            return false;
         } else {
            for (String var4 : this.messages) {
               if (var4 != null && !var4.isBlank()) {
                  c.sendPlayerMsg(var4, false);
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         a var2 = (a)object;
         return this.name.equalsIgnoreCase(var2.name);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name.toLowerCase());
   }
}
