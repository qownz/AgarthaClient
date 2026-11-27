package net.agartha.client.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

@Environment(EnvType.CLIENT)
public final class a extends net.agartha.client.module.b {
   private static final int OFFHAND_SLOT = -2;
   private final net.agartha.client.module.a_pkg.j healthThreshold = this.addSetting(new net.agartha.client.module.a_pkg.j("Health Threshold", 10.0F, 1.0F, 19.0F));
   private final net.agartha.client.module.a_pkg.j hungerThreshold = this.addSetting(new net.agartha.client.module.a_pkg.j("Hunger Threshold", 16.0F, 1.0F, 19.0F));
   private boolean eating;
   private int previousSlot = -1;
   private int activeSlot = -1;

   public a() {
      super("Auto Eat", "Automatically eats food from offhand or hotbar.", net.agartha.client.module.c.MISC);
   }

   @Override
   protected void onDisable() {
      this.e(MinecraftClient.getInstance());
   }

   @net.agartha.client.d.f
   public void onTick(net.agartha.client.d.a_pkg.q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player == null || var2.world == null || var2.interactionManager == null) {
         this.e(var2);
      } else if (var2.currentScreen == null) {
         if (this.eating) {
            if (!this.a(var2)) {
               this.e(var2);
            } else {
               if (!this.b(var2, this.activeSlot)) {
                  int var4 = this.c(var2);
                  if (var4 == -1) {
                     this.e(var2);
                     return;
                  }

                  this.a(var2, var4);
               }

               this.d(var2);
            }
         } else if (this.a(var2)) {
            int var3 = this.c(var2);
            if (var3 != -1) {
               this.previousSlot = var2.player.getInventory().getSelectedSlot();
               if (!this.a(var2, var3)) {
                  this.previousSlot = -1;
               } else {
                  this.d(var2);
                  this.eating = true;
               }
            }
         }
      }
   }

   private boolean a(MinecraftClient minecraftClient) {
      boolean var2 = minecraftClient.player.getHealth() <= this.healthThreshold.get();
      boolean var3 = minecraftClient.player.getHungerManager().getFoodLevel() <= this.hungerThreshold.getInt();
      if (!var2 && !var3) {
         return false;
      } else {
         ItemStack var4 = this.b(minecraftClient);
         if (var4.isEmpty()) {
            return false;
         } else {
            FoodComponent var5 = (FoodComponent)var4.get(DataComponentTypes.FOOD);
            return var5 != null && (minecraftClient.player.getHungerManager().isNotFull() || var5.canAlwaysEat());
         }
      }
   }

   private ItemStack b(MinecraftClient minecraftClient) {
      int var2 = this.c(minecraftClient);
      if (var2 == -2) {
         return minecraftClient.player.getOffHandStack();
      } else {
         return var2 >= 0 ? minecraftClient.player.getInventory().getStack(var2) : ItemStack.EMPTY;
      }
   }

   private int c(MinecraftClient minecraftClient) {
      int var2 = -1;
      float var3 = -1.0F;
      ItemStack var4 = minecraftClient.player.getOffHandStack();
      FoodComponent var5 = (FoodComponent)var4.get(DataComponentTypes.FOOD);
      if (a(var4, var5)) {
         var2 = -2;
         var3 = a(var5);
      }

      for (int var6 = 0; var6 < 9; var6++) {
         ItemStack var7 = minecraftClient.player.getInventory().getStack(var6);
         FoodComponent var8 = (FoodComponent)var7.get(DataComponentTypes.FOOD);
         if (a(var7, var8)) {
            float var9 = a(var8);
            if (var9 > var3) {
               var3 = var9;
               var2 = var6;
            }
         }
      }

      return var2;
   }

   private static boolean a(ItemStack itemStack, FoodComponent foodComponent) {
      return !itemStack.isEmpty() && foodComponent != null;
   }

   private static float a(FoodComponent foodComponent) {
      return foodComponent.saturation() + foodComponent.nutrition();
   }

   private boolean a(MinecraftClient minecraftClient, int i) {
      this.activeSlot = i;
      if (i == -2) {
         return true;
      } else if (i >= 0 && i <= 8) {
         minecraftClient.player.getInventory().setSelectedSlot(i);
         return true;
      } else {
         return false;
      }
   }

   private boolean b(MinecraftClient minecraftClient, int i) {
      if (i == -2) {
         return a(minecraftClient.player.getOffHandStack(), (FoodComponent)minecraftClient.player.getOffHandStack().get(DataComponentTypes.FOOD));
      } else if (i >= 0 && i <= 8) {
         ItemStack var3 = minecraftClient.player.getInventory().getStack(i);
         return a(var3, (FoodComponent)var3.get(DataComponentTypes.FOOD));
      } else {
         return false;
      }
   }

   private void d(MinecraftClient minecraftClient) {
      minecraftClient.options.useKey.setPressed(true);
      Hand var2 = this.activeSlot == -2 ? Hand.OFF_HAND : Hand.MAIN_HAND;
      if (!minecraftClient.player.isUsingItem()) {
         minecraftClient.interactionManager.interactItem(minecraftClient.player, var2);
      }
   }

   private void e(MinecraftClient minecraftClient) {
      if (minecraftClient != null && minecraftClient.options != null) {
         minecraftClient.options.useKey.setPressed(false);
         if (minecraftClient.player != null && this.previousSlot >= 0 && this.previousSlot <= 8) {
            minecraftClient.player.getInventory().setSelectedSlot(this.previousSlot);
         }

         this.eating = false;
         this.previousSlot = -1;
         this.activeSlot = -1;
      }
   }
}
