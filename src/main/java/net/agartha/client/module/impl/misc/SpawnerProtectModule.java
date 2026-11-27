package net.agartha.client.module.impl.misc;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.util.net.ProtectMessages;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public final class SpawnerProtectModule extends Module {
   private final NumberSetting triggerBreakCount = (NumberSetting)this.addSetting(new NumberSetting("Trigger Count", 5.0F, 1.0F, 64.0F));
   private final NumberSetting monitorChunkRadius = (NumberSetting)this.addSetting(new NumberSetting("Monitor Chunks", 3.0F, 1.0F, 8.0F));
   private final NumberSetting spawnerRange = (NumberSetting)this.addSetting(new NumberSetting("Spawner Range", 32.0F, 2.0F, 32.0F));
   private final NumberSetting chestSearchRange = (NumberSetting)this.addSetting(new NumberSetting("Chest Range", 32.0F, 2.0F, 32.0F));
   private final NumberSetting mineTimeoutTicks = (NumberSetting)this.addSetting(new NumberSetting("Mine Timeout", 80.0F, 20.0F, 200.0F));
   private final NumberSetting noSpawnerConfirmTicks = (NumberSetting)this.addSetting(new NumberSetting("Empty Confirm", 15.0F, 5.0F, 80.0F));
   private final NumberSetting brokenBlockConfirmTicks = (NumberSetting)this.addSetting(new NumberSetting("Break Confirm", 2.0F, 1.0F, 8.0F));
   private final NumberSetting brokenSpawnerConfirmTicks = (NumberSetting)this.addSetting(new NumberSetting("Spawner Confirm", 6.0F, 2.0F, 20.0F));
   private final NumberSetting ghostRetryDelayTicks = (NumberSetting)this.addSetting(new NumberSetting("Retry Delay", 8.0F, 1.0F, 60.0F).whole());
   private final BooleanSetting chatNotify = (BooleanSetting)this.addSetting(
      new BooleanSetting("Chat Notify", true)
   );
   private final BooleanSetting autoQuitAfterBreak = (BooleanSetting)this.addSetting(
      new BooleanSetting("Auto Quit After Break", false)
   );
   private final SpawnerProtectionLogic controller = new SpawnerProtectionLogic(this::disableFromController);
   private final SpawnerProtectionLogic.b settingsView = new SpawnerProtectionLogic.b() {
      public int triggerBreakCount() {
         return SpawnerProtectModule.this.triggerBreakCount.getInt();
      }

      public int monitorChunkRadius() {
         return SpawnerProtectModule.this.monitorChunkRadius.getInt();
      }

      public int spawnerRange() {
         return SpawnerProtectModule.this.spawnerRange.getInt();
      }

      public int chestSearchRange() {
         return SpawnerProtectModule.this.chestSearchRange.getInt();
      }

      public int mineTimeoutTicks() {
         return SpawnerProtectModule.this.mineTimeoutTicks.getInt();
      }

      public int noSpawnerConfirmTicks() {
         return SpawnerProtectModule.this.noSpawnerConfirmTicks.getInt();
      }

      public int brokenBlockConfirmTicks() {
         return SpawnerProtectModule.this.brokenBlockConfirmTicks.getInt();
      }

      public int brokenSpawnerConfirmTicks() {
         return SpawnerProtectModule.this.brokenSpawnerConfirmTicks.getInt();
      }

      public int ghostRetryDelayTicks() {
         return SpawnerProtectModule.this.ghostRetryDelayTicks.getInt();
      }

      public boolean chatNotify() {
         return SpawnerProtectModule.this.chatNotify.get();
      }

      public boolean autoQuitAfterBreak() {
         return SpawnerProtectModule.this.autoQuitAfterBreak.get();
      }
   };
   private boolean suppressDisableChat;

   public SpawnerProtectModule() {
      super("Spawner Protect", "Watches nearby breaks, then mines spawners and deposits them into an ender chest.", ModuleCategory.DONUT);
   }

   public SpawnerProtectionLogic controller() {
      return this.controller;
   }

   protected void onEnable() {
      this.controller.setSettings(this.settingsView);
      this.controller.softReset();
      if (this.chatNotify.get()) {
         ProtectMessages.send("Protection enabled.");
      }
   }

   protected void onDisable() {
      this.controller.onDisable(MinecraftClient.getInstance());
      if (!this.suppressDisableChat && this.chatNotify.get()) {
         ProtectMessages.send("Protection disabled.");
      }
   }

   @EventListener
   public void onTick(TickEvent arg) {
      this.controller.tick(MinecraftClient.getInstance());
   }

   public void handleServerVerifiedBlockUpdate(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
      if (this.isEnabled()) {
         this.controller.onBlockUpdate(blockPos, blockState, blockState2);
      }
   }

   private void disableFromController() {
      if (this.isEnabled()) {
         this.suppressDisableChat = true;

         try {
            this.setEnabled(false);
         } finally {
            this.suppressDisableChat = false;
         }
      }
   }
}
