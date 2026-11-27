package net.agartha.client.module.impl.misc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

@Environment(EnvType.CLIENT)
public final class s extends net.agartha.client.module.b {
   private static final String MODE_STRICT = "Strict";
   private static final String MODE_RAGE = "Rage";
   private static volatile Field cachedActionField;
   private static volatile Object cachedAttackAction;
   private static volatile boolean actionLookupAttempted;
   private final net.agartha.client.module.a_pkg.i mode = this.addSetting(new net.agartha.client.module.a_pkg.i("Sprint Mode", "Strict", "Strict", "Rage"));
   private final net.agartha.client.module.a_pkg.b keepSprint = this.addSetting(new net.agartha.client.module.a_pkg.b("Keep Sprint", false));
   private final net.agartha.client.module.a_pkg.b unsprintOnHit = this.addSetting(new net.agartha.client.module.a_pkg.b("Unsprint On Hit", false));
   private final net.agartha.client.module.a_pkg.b unsprintInWater = this.addSetting(new net.agartha.client.module.a_pkg.b("Unsprint In Water", true));
   private final net.agartha.client.module.a_pkg.b sprintWhileStationary = this.addSetting(new net.agartha.client.module.a_pkg.b("Sprint While Stationary", false));
   private volatile boolean pendingResprint;

   public s() {
      super("Sprint", "Automatically sprints.", net.agartha.client.module.c.MISC);
      this.unsprintInWater.visibleWhen(() -> "Rage".equals(this.mode.get()));
      this.sprintWhileStationary.visibleWhen(() -> "Rage".equals(this.mode.get()));
   }

   @Override
   protected void onDisable() {
      this.pendingResprint = false;
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.player != null && !var1.options.sprintKey.isPressed()) {
         var1.player.setSprinting(false);
      }
   }

   @net.agartha.client.d.f
   public void onTick(net.agartha.client.d.a_pkg.q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null && var2.world != null) {
         if (this.pendingResprint && this.a(var2) && !var2.player.isSprinting()) {
            var2.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(var2.player, Mode.START_SPRINTING));
            var2.player.setSprinting(true);
         }

         this.pendingResprint = false;
         if (!"Rage".equals(this.mode.get()) || !this.unsprintInWater.get() || !var2.player.isTouchingWater()) {
            var2.player.setSprinting(this.a(var2));
         }
      }
   }

   @net.agartha.client.d.f
   public void onPacketSend(net.agartha.client.d.a_pkg.k arg) {
      if (this.unsprintOnHit.get()) {
         if (arg.packet instanceof PlayerInteractEntityC2SPacket var2) {
            if (a(var2)) {
               MinecraftClient var4 = MinecraftClient.getInstance();
               if (var4.player != null && var4.getNetworkHandler() != null) {
                  this.pendingResprint = this.keepSprint.get() && this.a(var4);
                  var4.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(var4.player, Mode.STOP_SPRINTING));
                  var4.player.setSprinting(false);
               }
            }
         }
      }
   }

   private boolean a(MinecraftClient minecraftClient) {
      if (!this.isEnabled() || minecraftClient.player == null) {
         return false;
      } else if (minecraftClient.currentScreen != null) {
         return false;
      } else {
         float var2 = "Rage".equals(this.mode.get())
            ? Math.abs(minecraftClient.player.forwardSpeed) + Math.abs(minecraftClient.player.sidewaysSpeed)
            : minecraftClient.player.forwardSpeed;
         if (!(var2 <= (minecraftClient.player.isSubmergedInWater() ? 1.0E-5F : 0.8F)) || !"Strict".equals(this.mode.get()) && this.sprintWhileStationary.get()
            )
          {
            boolean var3 = !minecraftClient.player.isPartlyTouchingWater()
                  && !minecraftClient.player.hasBlindnessEffect()
                  && minecraftClient.player.hasVehicle()
               ? minecraftClient.player.getVehicle() != null
                  && minecraftClient.player.getVehicle().canSprintAsVehicle()
                  && minecraftClient.player.getVehicle().isLogicalSideForUpdatingMovement()
               : minecraftClient.player.getHungerManager().canSprint()
                  && (!minecraftClient.player.horizontalCollision || minecraftClient.player.collidedSoftly);
            return "Rage".equals(this.mode.get()) || var3;
         } else {
            return false;
         }
      }
   }

   private static boolean a(PlayerInteractEntityC2SPacket playerInteractEntityC2SPacket) {
      a();
      if (cachedActionField != null && cachedAttackAction != null) {
         try {
            return cachedActionField.get(playerInteractEntityC2SPacket) == cachedAttackAction;
         } catch (IllegalAccessException var2) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static void a() {
      if (!actionLookupAttempted) {
         synchronized (s.class) {
            if (!actionLookupAttempted) {
               try {
                  Field[] var1 = PlayerInteractEntityC2SPacket.class.getDeclaredFields();
                  Field var2 = null;
                  Field var3 = null;

                  for (Field var7 : var1) {
                     if (var7.getType().getSimpleName().equals("Action")) {
                        var7.setAccessible(true);
                        if (Modifier.isStatic(var7.getModifiers())) {
                           var3 = var7;
                        } else {
                           var2 = var7;
                        }
                     }
                  }

                  cachedActionField = var2;
                  cachedAttackAction = var3 != null ? var3.get(null) : null;
               } catch (IllegalAccessException var13) {
                  cachedActionField = null;
                  cachedAttackAction = null;
               } finally {
                  actionLookupAttempted = true;
               }
            }
         }
      }
   }
}
