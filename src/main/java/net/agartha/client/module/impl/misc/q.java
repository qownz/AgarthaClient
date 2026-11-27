package net.agartha.client.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.PlayerInput;

@Environment(EnvType.CLIENT)
public final class q extends net.agartha.client.module.b {
   public static final String MODE_VANILLA = "Vanilla";
   public static final String MODE_PACKET = "Packet";
   private final net.agartha.client.module.a_pkg.i mode = this.addSetting(new net.agartha.client.module.a_pkg.i("Mode", "Vanilla", "Vanilla", "Packet"));

   public q() {
      super(
         "Sneak",
         "Sneaks for you. Vanilla mode crouches normally and slows you down; Packet mode tells the server you are sneaking without the slowdown.",
         net.agartha.client.module.c.MISC
      );
   }

   public boolean doPacket() {
      return this.a() && !b() && "Packet".equals(this.mode.get());
   }

   public boolean doVanilla() {
      return this.a() && !b() && "Vanilla".equals(this.mode.get());
   }

   public static PlayerInput forceSneak(PlayerInput playerInput) {
      return playerInput == null
         ? null
         : new PlayerInput(
            playerInput.forward(), playerInput.backward(), playerInput.left(), playerInput.right(), playerInput.jump(), true, playerInput.sprint()
         );
   }

   private boolean a() {
      if (!this.isEnabled()) {
         return false;
      } else {
         MinecraftClient var1 = MinecraftClient.getInstance();
         return var1.player != null;
      }
   }

   private static boolean b() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      return var0.player != null && var0.player.getAbilities().flying;
   }
}
