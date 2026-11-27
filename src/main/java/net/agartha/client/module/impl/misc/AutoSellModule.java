package net.agartha.client.module.impl.misc;

import java.util.Random;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.d.f;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.e.aj;
import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.module.a_pkg.e;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.util.ab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

@Environment(EnvType.CLIENT)
public final class AutoSellModule extends b {
   private static final String NAME = "Auto Sell";
   private static final int SELLABLE_SLOTS = 36;
   private static final int SELL_BUTTON = 53;
   private static final long COMMAND_GAP_MS = 1000L;
   private static final long OPEN_WAIT_MS = 10000L;
   private static final long READ_AFTER_SELL_MS = 450L;
   private static final long SETTLE_ON_OPEN_MS = 250L;
   private static final long IDLE_LOOK_MS = 250L;
   private static final long RESYNC_REOPEN_MS = 500L;
   private static final int RESYNC_AFTER = 2;
   private static final Random PACE = new Random();
   private final e sKeep = (e)this.addSetting(new e("Keep Items", new String[]{"minecraft:elytra", "minecraft:netherite_pickaxe", "minecraft:netherite_sword"}));
   private final j sMoveDelay = (j)this.addSetting(new j("Move Delay (ms)", 90.0F, 30.0F, 500.0F).whole());
   private final j sSellDelay = (j)this.addSetting(new j("Sell Delay (ms)", 400.0F, 150.0F, 2000.0F).whole());
   private final j sBatch = (j)this.addSetting(new j("Stacks Per Sell", 36.0F, 1.0F, 45.0F).whole());
   private final net.agartha.client.module.a_pkg.b sStopWhenClosed = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Stop When Closed", true)
   );
   private final j sReopenDelay = (j)this.addSetting(new j("Reopen Delay (s)", 3.0F, 1.0F, 30.0F).whole());
   private final net.agartha.client.module.a_pkg.b sDebugChat = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Debug Chat", false)
   );
   private AutoSellModule.a step = AutoSellModule.a.FILL;
   private int menuId = -1;
   private boolean menuWasOpen;
   private boolean ownClose;
   private long opening;
   private long lastCommand;
   private long reopenAt;
   private long nextAt;
   private int putIn;
   private int inMenuAtSell;
   private int leftovers;
   private int itemsSold;
   private int sells;
   private boolean alerted;

   public AutoSellModule() {
      super("Auto Sell", "Sells your inventory through /sell, keeping the items you pick.", c.DONUT);
      this.sReopenDelay.visibleWhen(() -> !this.sStopWhenClosed.get());
   }

   public boolean persistEnabled() {
      return false;
   }

   protected void onEnable() {
      this.step = AutoSellModule.a.FILL;
      this.menuId = -1;
      this.menuWasOpen = false;
      this.ownClose = false;
      this.opening = 0L;
      this.lastCommand = 0L;
      this.reopenAt = 0L;
      this.nextAt = 0L;
      this.putIn = 0;
      this.inMenuAtSell = 0;
      this.leftovers = 0;
      this.itemsSold = 0;
      this.sells = 0;
      this.alerted = false;
   }

   protected void onDisable() {
      ClientPlayerEntity var1 = MinecraftClient.getInstance().player;
      if (var1 != null && this.isOurMenu(var1)) {
         var1.closeHandledScreen();
      }

      this.menuId = -1;
      this.debug("Stopped after " + this.sells + " sell(s), " + this.itemsSold + " item(s).");
   }

   @f
   public void onTick(q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientPlayerEntity var3 = var2.player;
      if (var3 != null && var2.getNetworkHandler() != null && var2.interactionManager != null && !var3.isDead()) {
         long var4 = System.currentTimeMillis();
         if (this.menuId < 0 && this.opening > 0L) {
            this.waitForMenu(var2, var3, var4);
         } else if (!this.isOurMenu(var3)) {
            this.menuId = -1;
            if (!this.menuWasOpen || !this.handleClosed(var4)) {
               this.openMenu(var2, var3, var4);
            }
         } else if (var4 >= this.nextAt) {
            ScreenHandler var6 = var3.currentScreenHandler;
            switch (this.step) {
               case FILL:
                  this.fill(var2, var3, var6, var4);
                  break;
               case SELL:
                  this.sell(var2, var3, var6, var4);
                  break;
               case READ:
                  this.read(var3, var6, var4);
            }
         }
      } else {
         this.menuId = -1;
      }
   }

   private void waitForMenu(MinecraftClient minecraftClient, ClientPlayerEntity clientPlayerEntity, long l) {
      if (clientPlayerEntity.currentScreenHandler != clientPlayerEntity.playerScreenHandler
         && minecraftClient.currentScreen instanceof HandledScreen
         && menuSlots(clientPlayerEntity.currentScreenHandler) > 53) {
         this.menuId = clientPlayerEntity.currentScreenHandler.syncId;
         this.opening = 0L;
         this.menuWasOpen = true;
         this.alerted = false;
         this.step = AutoSellModule.a.FILL;
         this.putIn = 0;
         this.nextAt = l + 250L;
         this.debug("Sell menu open.");
      } else if (l > this.opening) {
         this.opening = 0L;
         this.reopenAt = l + this.reopenDelayMs();
         if (!this.alerted) {
            alert("/sell did not open a menu, trying again.");
         }

         this.alerted = true;
      }
   }

   private boolean handleClosed(long l) {
      this.menuWasOpen = false;
      if (this.ownClose) {
         this.ownClose = false;
         return false;
      } else if (this.sStopWhenClosed.get()) {
         this.debug("Menu closed by you, turning off.");
         this.setEnabled(false);
         return true;
      } else {
         this.reopenAt = l + this.reopenDelayMs();
         this.debug("Menu closed, reopening in " + this.sReopenDelay.getInt() + "s.");
         return false;
      }
   }

   private void openMenu(MinecraftClient minecraftClient, ClientPlayerEntity clientPlayerEntity, long l) {
      if (minecraftClient.currentScreen == null && l >= this.reopenAt && l - this.lastCommand >= 1000L) {
         if (net.agartha.client.util.f.allowAutomated("/sell", "Auto Sell")) {
            clientPlayerEntity.networkHandler.sendChatCommand("sell");
            this.lastCommand = l;
            this.opening = l + 10000L;
         }
      }
   }

   private void fill(MinecraftClient minecraftClient, ClientPlayerEntity clientPlayerEntity, ScreenHandler screenHandler, long l) {
      Slot var6 = this.putIn < this.sBatch.getInt() ? this.nextToSell(screenHandler) : null;
      if (var6 != null) {
         minecraftClient.interactionManager.clickSlot(screenHandler.syncId, var6.id, 0, SlotActionType.QUICK_MOVE, clientPlayerEntity);
         this.putIn++;
         this.nextAt = l + paced(this.sMoveDelay.getInt());
      } else {
         if (itemsInMenu(screenHandler) > 0) {
            this.debug("Put in " + this.putIn + " stack(s), selling.");
            this.step = AutoSellModule.a.SELL;
            this.nextAt = l + paced(this.sSellDelay.getInt());
         } else {
            this.putIn = 0;
            this.nextAt = l + 250L;
         }
      }
   }

   private void sell(MinecraftClient minecraftClient, ClientPlayerEntity clientPlayerEntity, ScreenHandler screenHandler, long l) {
      this.inMenuAtSell = itemsInMenu(screenHandler);
      if (this.inMenuAtSell > 0) {
         minecraftClient.interactionManager.clickSlot(screenHandler.syncId, 53, 0, SlotActionType.PICKUP, clientPlayerEntity);
      }

      this.step = AutoSellModule.a.READ;
      this.nextAt = l + 450L;
   }

   private void read(ClientPlayerEntity clientPlayerEntity, ScreenHandler screenHandler, long l) {
      int var5 = itemsInMenu(screenHandler);
      int var6 = Math.max(0, this.inMenuAtSell - var5);
      if (var6 > 0) {
         this.itemsSold += var6;
         this.sells++;
         this.debug("Sold " + var6 + " item(s).");
      }

      this.step = AutoSellModule.a.FILL;
      this.putIn = 0;
      this.nextAt = l + paced(this.sMoveDelay.getInt());
      if (var5 == 0) {
         this.leftovers = 0;
      } else {
         if (++this.leftovers >= 2) {
            this.leftovers = 0;
            this.ownClose = true;
            clientPlayerEntity.closeHandledScreen();
            this.reopenAt = l + 500L;
            this.debug(var5 + " item(s) stayed in the menu, reopening it to resync.");
         }
      }
   }

   private Slot nextToSell(ScreenHandler screenHandler) {
      for (Slot var3 : screenHandler.slots) {
         if (var3.inventory instanceof PlayerInventory && var3.hasStack() && var3.getIndex() < 36 && !this.sKeep.contains(var3.getStack().getItem())) {
            return var3;
         }
      }

      return null;
   }

   private boolean isOurMenu(ClientPlayerEntity clientPlayerEntity) {
      return this.menuId >= 0
         && clientPlayerEntity.currentScreenHandler != clientPlayerEntity.playerScreenHandler
         && clientPlayerEntity.currentScreenHandler.syncId == this.menuId;
   }

   private static int itemsInMenu(ScreenHandler screenHandler) {
      int var1 = 0;

      for (Slot var3 : screenHandler.slots) {
         if (!(var3.inventory instanceof PlayerInventory) && var3.id != 53 && var3.hasStack()) {
            var1 += var3.getStack().getCount();
         }
      }

      return var1;
   }

   private static int menuSlots(ScreenHandler screenHandler) {
      int var1 = 0;

      for (Slot var3 : screenHandler.slots) {
         if (!(var3.inventory instanceof PlayerInventory)) {
            var1++;
         }
      }

      return var1;
   }

   private long reopenDelayMs() {
      return this.sReopenDelay.getInt() * 1000L;
   }

   private static long paced(int i) {
      double var1 = i * 0.25;
      return Math.max(20L, Math.round(i - var1 + PACE.nextDouble() * var1 * 2.0));
   }

   private static void alert(String string) {
      aj.getInstance().pushAlert("Auto Sell: " + string);
      AgarthaClientMod.LOGGER.warn("[{}] {}", "Auto Sell", string);
   }

   private void debug(String string) {
      if (this.sDebugChat.get()) {
         ab.send("Auto Sell", string);
         AgarthaClientMod.LOGGER.info("[{}] {}", "Auto Sell", string);
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum a {
      FILL,
      SELL,
      READ;
   }
}
