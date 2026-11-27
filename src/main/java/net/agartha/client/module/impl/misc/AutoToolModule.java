package net.agartha.client.module.impl.misc;

import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public final class AutoToolModule extends Module {
   private final BooleanSetting requireCorrectTool = this.addSetting(new BooleanSetting("Require Correct Tool", true));
   private final BooleanSetting preserveTools = this.addSetting(new BooleanSetting("Preserve Tools", true));

   public AutoToolModule() {
      super("Auto Tool", "Select the fastest hotbar tool for the targeted block.", ModuleCategory.MISC);
   }

   @EventListener
   public void onTick(TickEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null && var2.world != null && var2.currentScreen == null) {
         if (var2.options.attackKey.isPressed() && !var2.player.isUsingItem()) {
            if (var2.crosshairTarget != null && var2.crosshairTarget.getType() == Type.BLOCK) {
               BlockHitResult var3 = (BlockHitResult)var2.crosshairTarget;
               BlockPos var4 = var3.getBlockPos();
               BlockState var5 = var2.world.getBlockState(var4);
               if (!var5.isAir()) {
                  int var6 = this.a(var2.player, var5);
                  if (var6 >= 0 && var6 != var2.player.getInventory().getSelectedSlot()) {
                     var2.player.getInventory().setSelectedSlot(var6);
                  }
               }
            }
         }
      }
   }

   private int a(ClientPlayerEntity clientPlayerEntity, BlockState blockState) {
      PlayerInventory var3 = clientPlayerEntity.getInventory();
      int var4 = var3.getSelectedSlot();
      ItemStack var5 = var3.getStack(var4);
      float var6 = b(var5, blockState);
      int var7 = var4;

      for (int var8 = 0; var8 < 9; var8++) {
         ItemStack var9 = var3.getStack(var8);
         if (!var9.isEmpty() && !this.a(var9, blockState)) {
            float var10 = b(var9, blockState);
            if (var10 > var6 + 0.001F) {
               var6 = var10;
               var7 = var8;
            }
         }
      }

      return var7;
   }

   private boolean a(ItemStack itemStack, BlockState blockState) {
      return this.requireCorrectTool.get() && blockState.isToolRequired() && !itemStack.isSuitableFor(blockState)
         ? true
         : this.preserveTools.get() && itemStack.isDamageable() && itemStack.willBreakNextUse();
   }

   private static float b(ItemStack itemStack, BlockState blockState) {
      return itemStack.isEmpty() ? 1.0F : itemStack.getMiningSpeedMultiplier(blockState);
   }
}
