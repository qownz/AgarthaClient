package net.agartha.client.module.impl.misc;

import net.agartha.client.d.f;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.util.p;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public final class SpawnerProtectModule extends b {
   private final j triggerBreakCount = (j)this.addSetting(new j("Trigger Count", 5.0F, 1.0F, 64.0F));
   private final j monitorChunkRadius = (j)this.addSetting(new j("Monitor Chunks", 3.0F, 1.0F, 8.0F));
   private final j spawnerRange = (j)this.addSetting(new j("Spawner Range", 32.0F, 2.0F, 32.0F));
   private final j chestSearchRange = (j)this.addSetting(new j("Chest Range", 32.0F, 2.0F, 32.0F));
   private final j mineTimeoutTicks = (j)this.addSetting(new j("Mine Timeout", 80.0F, 20.0F, 200.0F));
   private final j noSpawnerConfirmTicks = (j)this.addSetting(new j("Empty Confirm", 15.0F, 5.0F, 80.0F));
   private final j brokenBlockConfirmTicks = (j)this.addSetting(new j("Break Confirm", 2.0F, 1.0F, 8.0F));
   private final j brokenSpawnerConfirmTicks = (j)this.addSetting(new j("Spawner Confirm", 6.0F, 2.0F, 20.0F));
   private final j ghostRetryDelayTicks = (j)this.addSetting(new j("Retry Delay", 8.0F, 1.0F, 60.0F).whole());
   private final net.agartha.client.module.a_pkg.b chatNotify = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Chat Notify", true)
   );
   private final net.agartha.client.module.a_pkg.b autoQuitAfterBreak = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Auto Quit After Break", false)
   );
   private final r controller = new r(this::disableFromController);
   private final net.agartha.client.module.impl.misc.r.b settingsView = new net.agartha.client.module.impl.misc.r.b() {
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
      super("Spawner Protect", "Watches nearby breaks, then mines spawners and deposits them into an ender chest.", c.DONUT);
   }

   public r controller() {
      return this.controller;
   }

   protected void onEnable() {
      this.controller.setSettings(this.settingsView);
      this.controller.softReset();
      if (this.chatNotify.get()) {
         p.send("Protection enabled.");
      }
   }

   protected void onDisable() {
      this.controller.onDisable(MinecraftClient.getInstance());
      if (!this.suppressDisableChat && this.chatNotify.get()) {
         p.send("Protection disabled.");
      }
   }

   @f
   public void onTick(q arg) {
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
