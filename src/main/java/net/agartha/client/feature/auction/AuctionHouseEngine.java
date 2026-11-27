package net.agartha.client.feature.auction;

import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.feature.auction.MoneyParser;
import net.agartha.client.gui.NotificationManager;
import net.agartha.client.util.net.ChatSender;
import net.agartha.client.util.net.MoneyCommandGuard;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType.Default;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public final class AuctionHouseEngine {
   private static final int AUTO_SLOT = -1;
   private static final int FIRST_MAIN_INVENTORY_SLOT_ID = 9;
   private static final int LAST_INVENTORY_SLOT_ID = 44;
   private static final int FIRST_HOTBAR_SLOT_ID = 36;
   private static final int PLAYER_INVENTORY_SLOTS = 36;
   private static final String PLAIN_CHEST_ID = "minecraft:chest";
   private static final String ENDER_CHEST_ID = "minecraft:ender_chest";
   private static final int LISTING_CAP = 18;
   private static final long SIGN_RETRY_MS = 30000L;
   private static final long STEP_BACK_MS = 8000L;
   private static final long ALREADY_REOPEN_GAP_MS = 800L;
   private static final int ALREADY_REOPEN_LIMIT = 3;
   private static final Random DELAY_JITTER = new Random();
   private final AuctionHouseSettings settings;
   private final Runnable stopCallback;
   private ClientPlayerEntity player;
   private static final int INVENTORY_OPEN_DELAY = 2;
   private AuctionHouseEngine.b state = AuctionHouseEngine.b.IDLE;
   private AuctionHouseEngine.b lastState = AuctionHouseEngine.b.IDLE;
   private long lastStateTime;
   private int delayTimer;
   private int listedSlots;
   private int noStockTicks;
   private boolean initialListingCheck;
   private boolean limitReached;
   private long lastListingScanTime;
   private long currentListPrice;
   private boolean relisting;
   private int collectChestSlot = -1;
   private int collectAlreadyCount;
   private long lastAlreadyMs;
   private long alreadyBurstStart;
   private int alreadyReopens;
   private boolean collectRecoverPending;
   private int orderChestSlot = -1;
   private int orderSyncId = -1;
   private long signWaitStartMs;
   private boolean deathHeld;
   private boolean blockedHold;
   private static final String SMALL_CAPS = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";

   public AuctionHouseEngine(AuctionHouseSettings arg, Runnable runnable) {
      this.settings = arg;
      this.stopCallback = runnable;
   }

   private MinecraftClient a() {
      return MinecraftClient.getInstance();
   }

   private ClientPlayerEntity b() {
      return this.a().player;
   }

   private static int a(int i) {
      int var1 = Math.max(0, i - 1);
      int var2 = i + 2;
      return var1 + DELAY_JITTER.nextInt(var2 - var1 + 1);
   }

   public void onEnable() {
      this.state = AuctionHouseEngine.b.PRECHECK_AH;
      this.lastState = AuctionHouseEngine.b.IDLE;
      this.lastStateTime = System.currentTimeMillis();
      this.delayTimer = 10;
      this.listedSlots = 0;
      this.noStockTicks = 0;
      this.initialListingCheck = true;
      this.limitReached = false;
      this.lastListingScanTime = 0L;
      this.currentListPrice = MoneyParser.parse(this.settings.maxPrice());
      this.relisting = false;
      this.collectChestSlot = -1;
      this.collectAlreadyCount = 0;
      this.lastAlreadyMs = 0L;
      this.alreadyBurstStart = 0L;
      this.alreadyReopens = 0;
      this.collectRecoverPending = false;
      this.orderChestSlot = -1;
      this.orderSyncId = -1;
      this.signWaitStartMs = 0L;
      this.deathHeld = false;
      this.blockedHold = false;
      this.m("Activated.");
   }

   public void onDisable() {
      this.state = AuctionHouseEngine.b.IDLE;
      this.delayTimer = 0;
      ClientPlayerEntity var1 = this.b();
      MinecraftClient var2 = this.a();
      if (var1 != null && var2 != null && a(var2, var1)) {
         var1.closeHandledScreen();
      }
   }

   private static boolean a(MinecraftClient minecraftClient, ClientPlayerEntity clientPlayerEntity) {
      return minecraftClient.currentScreen != null
         || clientPlayerEntity.currentScreenHandler != null && clientPlayerEntity.currentScreenHandler != clientPlayerEntity.playerScreenHandler;
   }

   private void c() {
      if (this.player != null && a(this.a(), this.player)) {
         this.player.closeHandledScreen();
      }
   }

   private boolean d() {
      return this.a().currentScreen instanceof InventoryScreen;
   }

   private boolean e() {
      if (this.d()) {
         return true;
      } else if (this.a().currentScreen != null) {
         this.player.closeHandledScreen();
         this.delayTimer = a(this.settings.closeDelay());
         return false;
      } else {
         this.a().setScreen(new InventoryScreen(this.player));
         this.m("Opened inventory for item moves.");
         this.delayTimer = a(2);
         return false;
      }
   }

   public void onChat(String string) {
      this.c(string);
   }

   public String statusLine() {
      ClientPlayerEntity var1 = this.b();
      if (var1 == null) {
         return null;
      } else if (this.settings.singleSell()) {
         return this.listedSlots + "l+" + this.h() + "w/" + this.f();
      } else {
         AuctionHouseEngine.a var2 = this.a(var1);
         return this.listedSlots + "l+" + var2.matchingSlots + "s/" + this.f();
      }
   }

   public void tick() {
      MinecraftClient var1 = this.a();
      ClientPlayerEntity var2 = this.b();
      if (var2 != null && var1 != null && var1.interactionManager != null && var2.networkHandler != null) {
         if (!var2.isAlive()) {
            if (!this.deathHeld) {
               this.deathHeld = true;
               this.player = var2;
               this.c();
               this.a("Paused because the player died. Staying on.");
               this.state = AuctionHouseEngine.b.RESTART;
               this.delayTimer = 20;
            }
         } else {
            this.deathHeld = false;
            this.player = var2;
            this.j();
         }
      }
   }

   private int f() {
      return Math.min(18, Math.max(1, this.settings.targetListings()));
   }

   private int g() {
      return this.settings.singleSell() ? this.settings.singleInventoryStock() : this.settings.multiInventoryStock();
   }

   private int h() {
      ClientPlayerEntity var1 = this.b();
      if (var1 == null) {
         return 0;
      } else {
         int var2 = 0;

         for (int var3 = 0; var3 < 9; var3++) {
            ItemStack var4 = var1.getInventory().getStack(var3);
            if (this.a(var4) && var4.getCount() > 1) {
               var2++;
            }
         }

         return var2;
      }
   }

   private void i() {
      this.stopCallback.run();
   }

   private void a(String string) {
      NotificationManager.getInstance().pushAlert("AH Trader: " + string);
      AgarthaClientMod.LOGGER.warn("[AH Trader] {}", string);
   }

   private void b(String string) {
      if (this.settings.debugChat()) {
         ChatSender.send("AH Trader", string);
         AgarthaClientMod.LOGGER.info("[AH Trader] {}", string);
      }
   }

   private void j() {
      if (this.player != null) {
         float var1 = 1.0F;
         if (this.state != this.lastState) {
            this.lastState = this.state;
            this.lastStateTime = System.currentTimeMillis();
            this.m("State -> " + this.state.name());
         }

         boolean var2 = this.state == AuctionHouseEngine.b.SELECT_REQUESTED_ITEM && this.signWaitStartMs != 0L;
         if (this.state != AuctionHouseEngine.b.IDLE && !var2) {
            long var3 = (System.currentTimeMillis() - this.lastStateTime) / 1000L;
            if (var3 >= this.settings.timeoutSeconds()) {
               this.l("Timed out in " + this.state.name());
               return;
            }
         }

         if (this.delayTimer > 0) {
            this.delayTimer--;
         } else if (this.state == AuctionHouseEngine.b.IDLE && this.I()) {
            this.g("Periodic recount");
         } else {
            switch (this.state) {
               case IDLE:
               default:
                  break;
               case PRECHECK_AH:
                  this.k(this.J());
                  this.state = AuctionHouseEngine.b.READ_OWN_LISTINGS;
                  this.delayTimer = a(this.settings.menuDelay());
                  break;
               case READ_OWN_LISTINGS:
                  this.k();
                  break;
               case OPEN_ORDER:
                  this.k(this.settings.orderCommand());
                  this.state = AuctionHouseEngine.b.SELECT_ORDER_MENU;
                  this.delayTimer = a(this.settings.menuDelay());
                  break;
               case SELECT_ORDER_MENU:
                  this.m();
                  break;
               case SELECT_REQUESTED_ITEM:
                  this.n();
                  break;
               case OPEN_REQUESTED_CHEST:
                  this.o();
                  break;
               case WAIT_REQUESTED_CHEST_OPEN:
                  this.p();
                  break;
               case TAKE_STOCK:
                  this.q();
                  break;
               case CLOSE_ORDER_SCREEN:
                  this.player.closeHandledScreen();
                  if (this.settings.competitionPricing()) {
                     this.state = AuctionHouseEngine.b.PRECHECK_AH;
                     this.delayTimer = a(this.settings.closeDelay());
                  } else {
                     this.d("Closed order screen");
                  }
                  break;
               case PREPARE_HELD_ITEM:
                  this.r();
                  break;
               case SPLIT_SINGLE:
                  this.u();
                  break;
               case SEND_SELL_COMMAND:
                  AuctionHouseEngine.a var5 = this.a(this.player);
                  if (var5.matchingSlots <= 0) {
                     this.d("Nothing to sell");
                     return;
                  }

                  if (this.H()) {
                     this.i("Inventory contains an item other than the selected target");
                     return;
                  }

                  this.k(this.settings.ahAlias() + " sell " + this.s());
                  this.c();
                  this.listedSlots = Math.min(this.f(), this.listedSlots + 1);
                  this.state = AuctionHouseEngine.b.SELL_COOLDOWN;
                  this.delayTimer = a(this.settings.sellCommandDelay());
                  break;
               case SELL_COOLDOWN:
                  this.d(this.settings.singleSell() ? "Single listing sent" : "Listing sent");
                  break;
               case SHOP_OPEN:
                  this.k(this.settings.shopCommand());
                  this.state = AuctionHouseEngine.b.SHOP_READ;
                  this.delayTimer = a(this.settings.menuDelay());
                  break;
               case SHOP_READ:
                  this.y();
                  break;
               case COLLECT_OPEN_AH:
                  this.collectAlreadyCount = 0;
                  this.collectRecoverPending = false;
                  this.k(this.settings.ahAlias());
                  this.state = AuctionHouseEngine.b.COLLECT_OPEN_CHEST;
                  this.delayTimer = a(this.settings.menuDelay());
                  break;
               case COLLECT_OPEN_CHEST:
                  this.z();
                  break;
               case COLLECT_WAIT_CHEST:
                  this.A();
                  break;
               case COLLECT_PULL:
                  this.B();
                  break;
               case COLLECT_RECOVER_ANVIL:
                  this.C();
                  break;
               case RESTART:
                  this.state = AuctionHouseEngine.b.PRECHECK_AH;
                  this.delayTimer = 10;
            }
         }
      }
   }

   private void c(String string) {
      String var2 = string.toLowerCase(Locale.ROOT);
      if (!var2.contains("[ahtrader]") && !var2.contains("ah trader -") && !var2.contains("[meteor]")) {
         if (var2.contains("already") && this.state != AuctionHouseEngine.b.IDLE && this.state != AuctionHouseEngine.b.RESTART) {
            long var3 = System.currentTimeMillis();
            if (var3 - this.lastAlreadyMs >= 800L) {
               this.lastAlreadyMs = var3;
               if (var3 - this.alreadyBurstStart > 20000L) {
                  this.alreadyBurstStart = var3;
                  this.alreadyReopens = 0;
               }

               this.alreadyReopens++;
               this.c();
               if (this.alreadyReopens >= 3) {
                  this.l("Chat said already, starting over");
               } else {
                  if (this.t()) {
                     this.state = AuctionHouseEngine.b.COLLECT_OPEN_AH;
                     this.m("Already: closed and reopening AH.");
                  } else {
                     this.state = AuctionHouseEngine.b.PRECHECK_AH;
                     this.m("Already: closed and recounting listings.");
                  }

                  this.delayTimer = a(Math.max(4, this.settings.closeDelay()));
               }
            }
         } else {
            if (var2.contains("many")) {
               this.h("Chat trigger: many (listing limit full)");
            } else if (var2.contains("sold") || var2.contains("bought")) {
               this.limitReached = false;
               if (this.settings.singleSell()) {
                  this.listedSlots = Math.max(0, this.listedSlots - 1);
               }

               if (this.state == AuctionHouseEngine.b.IDLE) {
                  if (!this.settings.singleSell() || this.h() <= 0 && this.a(this.b()).totalItems <= 0) {
                     this.g("Chat trigger: sold/bought");
                  } else {
                     this.state = AuctionHouseEngine.b.SPLIT_SINGLE;
                     this.delayTimer = a(this.settings.splitDelay());
                  }
               }
            }
         }
      }
   }

   private void k() {
      ScreenHandler var2 = this.player.currentScreenHandler;
      if (var2 instanceof ScreenHandler) {
         int var6 = this.k(var2);
         ItemStack var3 = this.l(var2);
         if (var3 != null) {
            this.initialListingCheck = false;
            String var7 = Registries.ITEM.getId(var3.getItem()).toString();
            this.i("AH already has a listing other than the selected item (Found: " + var7 + ")");
         } else {
            this.blockedHold = false;
            this.initialListingCheck = false;
            this.listedSlots = Math.min(18, var6);
            this.lastListingScanTime = System.currentTimeMillis();
            if (this.listedSlots >= 18) {
               this.limitReached = true;
               this.m("Server cap is full (" + this.listedSlots + "/18), waiting for a free slot.");
            } else if (this.listedSlots >= this.f()) {
               this.limitReached = true;
            } else {
               this.limitReached = false;
            }

            this.m("Own listings counted: " + this.listedSlots);
            if (this.settings.competitionPricing() && this.listedSlots > 0) {
               long var4 = this.b(var2, this.player);
               if (var4 > 0L) {
                  this.currentListPrice = var4;
                  this.m("Own listing price read: " + MoneyParser.format(var4));
               }
            }

            this.c();
            this.l();
         }
      }
   }

   private void l() {
      if (!this.settings.competitionPricing()) {
         this.d("Own listings recount finished");
      } else if (this.H()) {
         this.i("Inventory contains an item other than the selected target");
      } else {
         this.relisting = this.listedSlots == 0;
         this.state = AuctionHouseEngine.b.SHOP_OPEN;
         this.delayTimer = a(this.settings.closeDelay());
      }
   }

   private void m() {
      ScreenHandler var2 = this.player.currentScreenHandler;
      if (var2 instanceof ScreenHandler) {
         int var3 = this.b(var2);
         if (var3 == -1) {
            this.m("Order chest not found yet.");
            this.delayTimer = a(this.settings.orderRetryDelay());
         } else {
            this.noStockTicks = 0;
            this.orderChestSlot = var3;
            this.orderSyncId = this.G();
            this.a(var3, 0, SlotActionType.PICKUP);
            this.m("Clicked order chest slot " + var3 + " (waiting for sign at " + (var3 - 1) + " to clear).");
            this.state = AuctionHouseEngine.b.SELECT_REQUESTED_ITEM;
            this.delayTimer = a(this.settings.menuDelay());
         }
      } else {
         this.delayTimer = a(this.settings.orderRetryDelay());
      }
   }

   private void n() {
      ScreenHandler var2 = this.player.currentScreenHandler;
      if (!(var2 instanceof ScreenHandler)) {
         this.delayTimer = a(this.settings.orderRetryDelay());
      } else {
         int var6 = this.orderChestSlot - 1;
         boolean var3 = var6 < 0 || var6 >= this.a(var2) || !this.h(var2.getSlot(var6).getStack());
         if (!var3) {
            if (this.signWaitStartMs == 0L) {
               this.signWaitStartMs = System.currentTimeMillis();
            }

            if (System.currentTimeMillis() - this.signWaitStartMs >= 30000L) {
               this.signWaitStartMs = 0L;
               this.j("Supply sign still loading after 30s");
            } else {
               this.m("Waiting for supply sign at slot " + var6 + " to clear.");
               this.delayTimer = a(this.settings.orderRetryDelay());
            }
         } else {
            boolean var4 = this.signWaitStartMs != 0L;
            this.signWaitStartMs = 0L;
            if (var4) {
               this.lastStateTime = System.currentTimeMillis();
            }

            int var5 = this.h(var2);
            if (var5 == -1) {
               if (this.b(8000L)) {
                  this.a(AuctionHouseEngine.b.SELECT_ORDER_MENU, "Supply item not visible");
               } else {
                  this.m("Supply item not visible yet.");
                  this.delayTimer = a(this.settings.orderRetryDelay());
               }
            } else {
               this.noStockTicks = 0;
               this.orderSyncId = this.G();
               this.a(var5, 0, SlotActionType.PICKUP);
               this.m("Clicked supply item slot " + var5);
               this.state = AuctionHouseEngine.b.OPEN_REQUESTED_CHEST;
               this.delayTimer = a(this.settings.menuDelay());
            }
         }
      }
   }

   private void o() {
      ScreenHandler var2 = this.player.currentScreenHandler;
      if (!(var2 instanceof ScreenHandler)) {
         this.delayTimer = a(this.settings.orderRetryDelay());
      } else {
         boolean var4 = this.G() != this.orderSyncId && this.d(var2.getSlot(0).getStack());
         if (!var4) {
            if (this.b(8000L)) {
               this.a(AuctionHouseEngine.b.SELECT_REQUESTED_ITEM, "Order menu did not open");
            } else {
               this.m("Waiting for order menu (gray pane in slot 0).");
               this.delayTimer = a(this.settings.orderRetryDelay());
            }
         } else {
            int var3 = this.d(var2);
            if (var3 == -1) {
               if (this.b(8000L)) {
                  this.a(AuctionHouseEngine.b.SELECT_REQUESTED_ITEM, "Order menu chest not found");
               } else {
                  this.m("Order menu plain chest not found yet.");
                  this.delayTimer = a(this.settings.orderRetryDelay());
               }
            } else {
               this.noStockTicks = 0;
               this.orderSyncId = this.G();
               this.a(var3, 0, SlotActionType.PICKUP);
               this.m("Clicked order menu chest slot " + var3);
               this.state = AuctionHouseEngine.b.WAIT_REQUESTED_CHEST_OPEN;
               this.delayTimer = a(this.settings.menuDelay());
            }
         }
      }
   }

   private void p() {
      ScreenHandler var2 = this.player.currentScreenHandler;
      if (!(var2 instanceof ScreenHandler)) {
         this.delayTimer = a(this.settings.orderRetryDelay());
      } else {
         ItemStack var4 = var2.getSlot(0).getStack();
         boolean var3 = this.G() != this.orderSyncId && !this.d(var4) && !var4.isEmpty() && !this.b(var4);
         if (var3) {
            this.m("Stock window opened (slot 0 = stock item).");
            this.noStockTicks = 0;
            this.state = AuctionHouseEngine.b.TAKE_STOCK;
            this.delayTimer = a(this.settings.collectionDelay());
         } else if (this.b(8000L)) {
            this.a(AuctionHouseEngine.b.OPEN_REQUESTED_CHEST, "Stock window did not open");
         } else {
            this.m("Waiting for stock window (slot 0 -> stock item).");
            this.delayTimer = a(this.settings.orderRetryDelay());
         }
      }
   }

   private void q() {
      ScreenHandler var2 = this.player.currentScreenHandler;
      if (var2 instanceof ScreenHandler) {
         AuctionHouseEngine.a var5 = this.a(this.player);
         int var3 = Math.max(0, this.g() - var5.matchingSlots);
         if (var3 <= 0) {
            this.state = AuctionHouseEngine.b.CLOSE_ORDER_SCREEN;
            this.delayTimer = a(this.settings.closeDelay());
         } else {
            int var4 = this.f(var2);
            if (var4 == -1) {
               if (!this.g(var2)) {
                  this.j("Stock menu did not show the target item");
               } else {
                  this.noStockTicks = this.noStockTicks + a(this.settings.orderRetryDelay());
                  if (this.noStockTicks >= this.settings.emptyStableSeconds() * 20) {
                     this.j("Order stock is empty");
                  } else {
                     this.delayTimer = a(this.settings.orderRetryDelay());
                  }
               }
            } else {
               this.noStockTicks = 0;
               this.a(var4, 0, SlotActionType.QUICK_MOVE);
               this.m("Quick-moved stock from slot " + var4);
               this.delayTimer = a(this.settings.collectionDelay());
            }
         }
      }
   }

   private void r() {
      if (this.a().currentScreen != null && !this.d()) {
         this.player.closeHandledScreen();
         this.delayTimer = a(this.settings.closeDelay());
      } else if (!this.player.currentScreenHandler.getCursorStack().isEmpty()) {
         if (this.e()) {
            int var5 = this.F();
            if (var5 != -1) {
               this.a(var5, 0, SlotActionType.PICKUP);
               this.m("Cleared cursor stack into slot " + var5);
               this.delayTimer = a(this.settings.closeDelay());
            }
         }
      } else {
         int var1 = this.player.getInventory().getSelectedSlot();
         if (this.a(this.player.getInventory().getStack(var1))) {
            this.state = AuctionHouseEngine.b.SEND_SELL_COMMAND;
            this.delayTimer = a(this.settings.splitDelay());
         } else {
            int var2 = this.D();
            if (var2 != -1) {
               this.player.getInventory().setSelectedSlot(var2);
               this.m("Using existing hotbar stack at index " + var2);
               this.state = AuctionHouseEngine.b.SEND_SELL_COMMAND;
               this.delayTimer = a(this.settings.splitDelay());
            } else {
               int var3 = this.E();
               if (var3 == -1) {
                  this.d("No movable target stack found");
               } else if (this.e()) {
                  int var4 = 36 + var1;
                  this.a(var3, 0, SlotActionType.PICKUP);
                  this.a(var4, 0, SlotActionType.PICKUP);
                  this.a(var3, 0, SlotActionType.PICKUP);
                  this.m("Moved stack from slot " + var3 + " to hotbar slot " + var4);
                  this.state = AuctionHouseEngine.b.SEND_SELL_COMMAND;
                  this.delayTimer = a(this.settings.splitDelay());
               }
            }
         }
      }
   }

   private void d(String string) {
      if (this.H()) {
         this.i("Inventory contains an item other than the selected target");
      } else {
         boolean var2 = this.limitReached || this.listedSlots >= this.f();
         AuctionHouseEngine.a var3 = this.a(this.player);
         if (this.settings.singleSell()) {
            int var4 = this.h();
            this.m(string + " | listed=" + this.listedSlots + " workStack=" + var4 + " items=" + var3.totalItems + " target=" + this.f() + " full=" + var2);
            if (var2) {
               if (this.d()) {
                  this.player.closeHandledScreen();
               }

               this.state = AuctionHouseEngine.b.IDLE;
               return;
            }

            if (var4 > 0 || var3.totalItems > 0) {
               this.state = AuctionHouseEngine.b.SPLIT_SINGLE;
               this.delayTimer = a(this.settings.splitDelay());
               return;
            }
         } else {
            this.m(string + " | listed=" + this.listedSlots + " invSlots=" + var3.matchingSlots + " target=" + this.f() + " full=" + var2);
            if (var2) {
               if (this.d()) {
                  this.player.closeHandledScreen();
               }

               this.state = AuctionHouseEngine.b.IDLE;
               return;
            }

            if (var3.matchingSlots > 0) {
               this.state = AuctionHouseEngine.b.PREPARE_HELD_ITEM;
               this.delayTimer = a(this.settings.closeDelay());
               return;
            }
         }

         this.state = AuctionHouseEngine.b.OPEN_ORDER;
         this.delayTimer = a(this.settings.closeDelay());
      }
   }

   private String s() {
      return this.settings.competitionPricing() ? MoneyParser.format(this.currentListPrice) : this.settings.sellPrice();
   }

   private boolean t() {
      return this.state == AuctionHouseEngine.b.COLLECT_OPEN_AH
         || this.state == AuctionHouseEngine.b.COLLECT_OPEN_CHEST
         || this.state == AuctionHouseEngine.b.COLLECT_WAIT_CHEST
         || this.state == AuctionHouseEngine.b.COLLECT_PULL
         || this.state == AuctionHouseEngine.b.COLLECT_RECOVER_ANVIL;
   }

   private void u() {
      if (this.a().currentScreen != null && !this.d()) {
         this.player.closeHandledScreen();
         this.delayTimer = a(this.settings.closeDelay());
      } else if (!this.player.currentScreenHandler.getCursorStack().isEmpty()) {
         if (this.e()) {
            int var4 = this.F();
            if (var4 != -1) {
               this.a(var4, 0, SlotActionType.PICKUP);
               this.delayTimer = a(this.settings.splitDelay());
            }
         }
      } else {
         int var1 = this.v();
         if (var1 != -1) {
            this.player.getInventory().setSelectedSlot(var1);
            this.m("Selling single from hotbar " + var1);
            this.state = AuctionHouseEngine.b.SEND_SELL_COMMAND;
            this.delayTimer = a(this.settings.splitDelay());
         } else {
            int var2 = this.w();
            if (var2 == -1) {
               this.d("No stack to split");
            } else {
               int var3 = this.b(var2);
               if (var3 == -1) {
                  if (this.e()) {
                     if (!this.c(1)) {
                        this.i("Hotbar full, cannot free a slot");
                     } else {
                        this.delayTimer = a(this.settings.splitDelay());
                     }
                  }
               } else if (this.e()) {
                  this.a(this.d(var2), 0, SlotActionType.PICKUP);
                  this.a(this.d(var3), 1, SlotActionType.PICKUP);
                  this.a(this.d(var2), 0, SlotActionType.PICKUP);
                  this.player.getInventory().setSelectedSlot(var3);
                  this.m("Split hotbar " + var2 + " -> hotbar " + var3);
                  this.state = AuctionHouseEngine.b.SEND_SELL_COMMAND;
                  this.delayTimer = a(this.settings.splitDelay());
               }
            }
         }
      }
   }

   private int v() {
      for (int var1 = 0; var1 < 9; var1++) {
         ItemStack var2 = this.player.getInventory().getStack(var1);
         if (this.a(var2) && var2.getCount() == 1) {
            return var1;
         }
      }

      return -1;
   }

   private int w() {
      for (int var1 = 8; var1 >= 0; var1--) {
         ItemStack var2 = this.player.getInventory().getStack(var1);
         if (this.a(var2) && var2.getCount() > 1) {
            return var1;
         }
      }

      return -1;
   }

   private int b(int i) {
      for (int var2 = i + 1; var2 < 9; var2++) {
         if (this.player.getInventory().getStack(var2).isEmpty()) {
            return var2;
         }
      }

      for (int var3 = 0; var3 < 9; var3++) {
         if (var3 != i && this.player.getInventory().getStack(var3).isEmpty()) {
            return var3;
         }
      }

      return -1;
   }

   private boolean c(int i) {
      if (i < 0 || i > 8) {
         i = 1;
      }

      ItemStack var2 = this.player.getInventory().getStack(i);
      if (var2.isEmpty()) {
         return true;
      } else {
         int var3 = this.x();
         if (var3 == -1) {
            return false;
         } else {
            this.a(this.d(i), 0, SlotActionType.PICKUP);
            this.a(var3, 0, SlotActionType.PICKUP);
            return true;
         }
      }
   }

   private int x() {
      for (int var1 = 9; var1 < 36; var1++) {
         if (this.player.getInventory().getStack(var1).isEmpty()) {
            return var1;
         }
      }

      return -1;
   }

   private int d(int i) {
      return 36 + i;
   }

   private void y() {
      ScreenHandler var2 = this.player.currentScreenHandler;
      if (var2 instanceof ScreenHandler) {
         long var6 = this.a(var2, this.player);
         this.c();
         if (var6 <= 0L) {
            this.m("Shop price not found.");
            if (this.relisting) {
               this.relisting = false;
               this.d("Shop unreadable, listing stock at current price");
            } else {
               this.state = AuctionHouseEngine.b.IDLE;
               this.delayTimer = a(this.settings.closeDelay());
               this.lastListingScanTime = System.currentTimeMillis();
            }
         } else if (this.relisting) {
            this.relisting = false;
            this.currentListPrice = this.a(var6 - MoneyParser.parse(this.settings.undercutAmount()));
            this.m("Relist price set to " + MoneyParser.format(this.currentListPrice) + " (market " + MoneyParser.format(var6) + ").");
            this.d("Competition relist price set");
         } else if (this.listedSlots > 0 && var6 < this.currentListPrice) {
            this.m(
               "Undercut detected: market "
                  + MoneyParser.format(var6)
                  + " < ours "
                  + MoneyParser.format(this.currentListPrice)
                  + ". Collecting active listings."
            );
            this.state = AuctionHouseEngine.b.COLLECT_OPEN_AH;
            this.delayTimer = a(this.settings.closeDelay());
         } else {
            AuctionHouseEngine.a var4 = this.a(this.player);
            boolean var5 = this.limitReached || this.listedSlots >= this.f();
            if (var4.matchingSlots > 0 && !var5) {
               this.currentListPrice = this.a(var6 - MoneyParser.parse(this.settings.undercutAmount()));
               this.m(
                  "Listing remaining stock at "
                     + MoneyParser.format(this.currentListPrice)
                     + " (market "
                     + MoneyParser.format(var6)
                     + ")."
               );
               this.d("Competition: list remaining stock");
            } else if (var4.matchingSlots <= 0 && !var5) {
               this.state = AuctionHouseEngine.b.OPEN_ORDER;
               this.delayTimer = a(this.settings.closeDelay());
            } else {
               this.m(
                  "Still cheapest (market "
                     + MoneyParser.format(var6)
                     + " >= ours "
                     + MoneyParser.format(this.currentListPrice)
                     + "). Idling."
               );
               this.state = AuctionHouseEngine.b.IDLE;
               this.delayTimer = a(this.settings.closeDelay());
               this.lastListingScanTime = System.currentTimeMillis();
            }
         }
      } else {
         this.delayTimer = a(this.settings.orderRetryDelay());
      }
   }

   private void z() {
      ScreenHandler var2 = this.player.currentScreenHandler;
      if (var2 instanceof ScreenHandler) {
         int var3 = this.c(var2);
         if (var3 == -1) {
            this.m("Collect: bottom-row plain chest not found yet.");
            this.delayTimer = a(this.settings.orderRetryDelay());
         } else {
            this.collectChestSlot = var3;
            this.a(var3, 0, SlotActionType.PICKUP);
            this.m("Collect: clicked bottom-row chest slot " + var3);
            this.state = AuctionHouseEngine.b.COLLECT_WAIT_CHEST;
            this.delayTimer = a(this.settings.menuDelay());
         }
      } else {
         this.delayTimer = a(this.settings.orderRetryDelay());
      }
   }

   private void A() {
      ScreenHandler var2 = this.player.currentScreenHandler;
      if (var2 instanceof ScreenHandler) {
         if (this.collectChestSlot >= 0 && this.collectChestSlot < this.a(var2) && this.c(var2.getSlot(this.collectChestSlot).getStack())) {
            this.m("Collect: chest opened (marker found).");
            this.state = AuctionHouseEngine.b.COLLECT_PULL;
            this.delayTimer = a(this.settings.collectionDelay());
         } else {
            this.m("Collect: waiting for chest marker.");
            this.delayTimer = a(this.settings.orderRetryDelay());
         }
      } else {
         this.delayTimer = a(this.settings.orderRetryDelay());
      }
   }

   private void B() {
      ScreenHandler var2 = this.player.currentScreenHandler;
      if (var2 instanceof ScreenHandler) {
         if (this.collectRecoverPending) {
            this.state = AuctionHouseEngine.b.COLLECT_RECOVER_ANVIL;
            this.delayTimer = 0;
         } else {
            ItemStack var3 = var2.getSlot(0).getStack();
            if (this.d(var3) || this.e(var3)) {
               this.e(this.e(var3) ? "list pane" : "gray pane");
            } else if (!var3.isEmpty() && !this.b(var3) && !this.c(var3)) {
               this.a(0, 0, SlotActionType.PICKUP);
               this.m("Collect: pulled listing from slot 0.");
               this.delayTimer = a(this.settings.collectionDelay());
            } else {
               this.delayTimer = a(this.settings.collectionDelay());
            }
         }
      } else {
         this.delayTimer = a(this.settings.orderRetryDelay());
      }
   }

   private void C() {
      ScreenHandler var2 = this.player.currentScreenHandler;
      if (var2 instanceof ScreenHandler) {
         if (this.j(var2) != -1) {
            this.m("Collect: ender chest visible, /ah reopened -> plain chest flow.");
            this.collectRecoverPending = false;
            this.collectAlreadyCount = 0;
            this.collectChestSlot = -1;
            this.state = AuctionHouseEngine.b.COLLECT_OPEN_CHEST;
            this.delayTimer = a(this.settings.menuDelay());
         } else {
            int var3 = this.i(var2);
            if (var3 != -1) {
               this.a(var3, 0, SlotActionType.PICKUP);
               this.m("Collect: clicking anvil slot " + var3 + " (waiting for ender chest).");
            } else {
               this.m("Collect: recovery active, waiting for anvil/ender chest.");
            }

            this.delayTimer = a(this.settings.menuDelay());
         }
      } else {
         this.delayTimer = a(this.settings.orderRetryDelay());
      }
   }

   private void e(String string) {
      this.m("Collect: collection done (" + string + ").");
      this.c();
      this.listedSlots = 0;
      this.limitReached = false;
      this.collectChestSlot = -1;
      this.collectRecoverPending = false;
      this.collectAlreadyCount = 0;
      this.relisting = true;
      this.state = AuctionHouseEngine.b.SHOP_OPEN;
      this.delayTimer = a(this.settings.closeDelay());
   }

   private int a(ScreenHandler screenHandler) {
      return Math.max(0, screenHandler.slots.size() - 36);
   }

   private boolean a(ScreenHandler screenHandler, int i) {
      return i >= 0 && i < this.a(screenHandler);
   }

   private boolean b(ScreenHandler screenHandler, int i) {
      return i >= 0 && i < screenHandler.slots.size();
   }

   private int b(ScreenHandler screenHandler) {
      int var2 = this.settings.orderCategorySlot();
      if (this.a(screenHandler, var2)) {
         return var2;
      } else {
         int var3 = this.c(screenHandler);
         if (var3 != -1) {
            return var3;
         } else {
            int var4 = this.a(screenHandler) - 3;
            return this.a(screenHandler, var4) ? var4 : this.e(screenHandler);
         }
      }
   }

   private int c(ScreenHandler screenHandler) {
      int var2 = this.a(screenHandler);
      int var3 = Math.max(0, var2 - 9);

      for (int var4 = var3; var4 < var2; var4++) {
         ItemStack var5 = screenHandler.getSlot(var4).getStack();
         if (!var5.isEmpty() && this.f(var5)) {
            return var4;
         }
      }

      return -1;
   }

   private int d(ScreenHandler screenHandler) {
      int var2 = this.a(screenHandler);
      int var3 = -1;
      int var4 = -1;

      for (int var5 = 0; var5 < var2; var5++) {
         ItemStack var6 = screenHandler.getSlot(var5).getStack();
         if (this.f(var6)) {
            var4 = var5;
            if (this.a(this.player, var6, "_collect_")) {
               var3 = var5;
            }
         }
      }

      return var3 != -1 ? var3 : var4;
   }

   private int e(ScreenHandler screenHandler) {
      int var2 = this.a(screenHandler);

      for (int var3 = 0; var3 < var2; var3++) {
         ItemStack var4 = screenHandler.getSlot(var3).getStack();
         if (!var4.isEmpty() && this.f(var4)) {
            return var3;
         }
      }

      return -1;
   }

   private int a(ClientPlayerEntity clientPlayerEntity, ScreenHandler screenHandler) {
      int var3 = this.a(screenHandler);
      String var4 = this.settings.requestedMatch().trim();
      if (!var4.isEmpty()) {
         for (int var5 = 0; var5 < var3; var5++) {
            ItemStack var6 = screenHandler.getSlot(var5).getStack();
            if (this.a(clientPlayerEntity, var6, var4) && this.a(clientPlayerEntity, var6, "delivered")) {
               return var5;
            }
         }
      }

      for (int var7 = 0; var7 < var3; var7++) {
         ItemStack var8 = screenHandler.getSlot(var7).getStack();
         if (!var8.isEmpty() && !this.b(var8) && !this.f(var8)) {
            return var7;
         }
      }

      return -1;
   }

   private int f(ScreenHandler screenHandler) {
      int var2 = this.a(screenHandler);

      for (int var3 = 0; var3 < var2; var3++) {
         if (this.a(screenHandler.getSlot(var3).getStack())) {
            return var3;
         }
      }

      return -1;
   }

   private boolean g(ScreenHandler screenHandler) {
      int var2 = this.a(screenHandler);

      for (int var3 = 0; var3 < var2; var3++) {
         ItemStack var4 = screenHandler.getSlot(var3).getStack();
         if (!var4.isEmpty() && !this.b(var4)) {
            return false;
         }
      }

      return true;
   }

   private int D() {
      for (int var1 = 0; var1 < 9; var1++) {
         if (this.a(this.player.getInventory().getStack(var1))) {
            return var1;
         }
      }

      return -1;
   }

   private int E() {
      for (int var1 = 9; var1 <= 44; var1++) {
         int var2 = var1 >= 36 ? var1 - 36 : var1;
         if (this.a(this.player.getInventory().getStack(var2))) {
            return var1;
         }
      }

      return -1;
   }

   private int F() {
      for (int var1 = 9; var1 <= 44; var1++) {
         int var2 = var1 >= 36 ? var1 - 36 : var1;
         if (this.player.getInventory().getStack(var2).isEmpty()) {
            return var1;
         }
      }

      return -1;
   }

   private AuctionHouseEngine.a a(ClientPlayerEntity clientPlayerEntity) {
      int var2 = 0;
      int var3 = 0;

      for (int var4 = 0; var4 < 36; var4++) {
         ItemStack var5 = clientPlayerEntity.getInventory().getStack(var4);
         if (this.a(var5)) {
            var2++;
            var3 += var5.getCount();
         }
      }

      return new AuctionHouseEngine.a(var2, var3);
   }

   private boolean a(ItemStack itemStack) {
      return itemStack != null && !itemStack.isEmpty()
         ? Registries.ITEM.getId(itemStack.getItem()).toString().equalsIgnoreCase(this.settings.targetItemId().trim())
         : false;
   }

   private boolean a(ClientPlayerEntity clientPlayerEntity, ItemStack itemStack, String string) {
      if (itemStack != null && !itemStack.isEmpty()) {
         String var4 = string.toLowerCase(Locale.ROOT);
         if (itemStack.getName().getString().toLowerCase(Locale.ROOT).contains(var4)) {
            return true;
         } else {
            for (Text var6 : itemStack.getTooltip(TooltipContext.DEFAULT, clientPlayerEntity, Default.BASIC)) {
               if (var6.getString().toLowerCase(Locale.ROOT).contains(var4)) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private boolean b(ItemStack itemStack) {
      if (itemStack != null && !itemStack.isEmpty()) {
         String var2 = Registries.ITEM.getId(itemStack.getItem()).toString().toLowerCase(Locale.ROOT);
         return var2.contains("pane") || var2.contains("paper");
      } else {
         return true;
      }
   }

   private long a(ScreenHandler screenHandler, ClientPlayerEntity clientPlayerEntity) {
      int var3 = this.a(screenHandler);
      long var4 = -1L;

      for (int var6 = 0; var6 < var3; var6++) {
         ItemStack var7 = screenHandler.getSlot(var6).getStack();
         if (this.a(var7)) {
            long var8 = this.a(var7, clientPlayerEntity);
            if (var8 > 0L) {
               if (!this.a(var7, clientPlayerEntity, var8)) {
                  return var8;
               }

               if (var4 < 0L) {
                  var4 = var8;
               }
            }
         }
      }

      return var4;
   }

   private boolean a(ItemStack itemStack, ClientPlayerEntity clientPlayerEntity, long l) {
      Set var5 = b(clientPlayerEntity);

      for (Text var7 : itemStack.getTooltip(TooltipContext.DEFAULT, clientPlayerEntity, Default.BASIC)) {
         String var8 = f(var7.getString());
         if (!var8.isEmpty()) {
            boolean var9 = var8.contains("seller") || var8.contains("owner") || var8.contains("listed by") || var8.startsWith("by ") || var8.contains(" by ");
            if (!var9 || !var8.matches(".*\\byou\\b.*") && !var8.contains("(you)")) {
               if (!var8.equals("you") && !var8.equals("(you)")) {
                  for (String var13 : var8.split("[^a-z0-9_]+")) {
                     if (var5.contains(var13)) {
                        return true;
                     }
                  }
                  continue;
               }

               return true;
            }

            return true;
         }
      }

      return this.listedSlots > 0 && this.currentListPrice > 0L && l == this.currentListPrice;
   }

   private static Set<String> b(ClientPlayerEntity clientPlayerEntity) {
      HashSet var1 = new HashSet();
      if (clientPlayerEntity == null) {
         return var1;
      } else {
         a(var1, accountName(clientPlayerEntity));
         a(var1, clientPlayerEntity.getName().getString());
         a(var1, clientPlayerEntity.getGameProfile().name());
         a(var1, clientPlayerEntity.getNameForScoreboard());
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (var2.getNetworkHandler() != null) {
            PlayerListEntry var3 = var2.getNetworkHandler().getPlayerListEntry(clientPlayerEntity.getUuid());
            if (var3 != null && var3.getDisplayName() != null) {
               String[] var4 = f(var3.getDisplayName().getString()).split("[^a-z0-9_]+");
               if (var4.length > 0) {
                  a(var1, var4[var4.length - 1]);
               }
            }
         }

         return var1;
      }
   }

   private static void a(Set<String> set, String string) {
      if (string != null) {
         String var2 = f(string).replaceAll("[^a-z0-9_]", "");
         if (var2.length() >= 3) {
            set.add(var2);
         }
      }
   }

   private static String f(String string) {
      if (string == null) {
         return "";
      } else {
         String var1 = string.replaceAll("§.", "");
         StringBuilder var2 = new StringBuilder(var1.length());

         for (int var3 = 0; var3 < var1.length(); var3++) {
            char var4 = var1.charAt(var3);
            int var5 = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ".indexOf(var4);
            var2.append(var5 >= 0 ? (char)(97 + var5) : var4);
         }

         return var2.toString().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
      }
   }

   private long b(ScreenHandler screenHandler, ClientPlayerEntity clientPlayerEntity) {
      int var3 = Math.min(18, this.a(screenHandler));

      for (int var4 = 0; var4 < var3; var4++) {
         ItemStack var5 = screenHandler.getSlot(var4).getStack();
         if (this.a(var5)) {
            long var6 = this.a(var5, clientPlayerEntity);
            if (var6 > 0L) {
               return var6;
            }
         }
      }

      return -1L;
   }

   private long a(ItemStack itemStack, ClientPlayerEntity clientPlayerEntity) {
      long var3 = MoneyParser.parseSuffixedFromText(itemStack.getName().getString());
      if (var3 > 0L) {
         return var3;
      } else {
         for (Text var6 : itemStack.getTooltip(TooltipContext.DEFAULT, clientPlayerEntity, Default.BASIC)) {
            long var7 = MoneyParser.parseSuffixedFromText(var6.getString());
            if (var7 > 0L) {
               return var7;
            }
         }

         return -1L;
      }
   }

   private long a(long l) {
      long var3 = MoneyParser.parse(this.settings.minPrice());
      long var5 = MoneyParser.parse(this.settings.maxPrice());
      if (var5 < var3) {
         long var7 = var3;
         var3 = var5;
         var5 = var7;
      }

      return Math.max(var3, Math.min(var5, l));
   }

   private boolean c(ItemStack itemStack) {
      if (itemStack != null && !itemStack.isEmpty()) {
         String var2 = Registries.ITEM.getId(itemStack.getItem()).toString().toLowerCase(Locale.ROOT);
         return var2.contains("feather") || var2.contains("book");
      } else {
         return false;
      }
   }

   private boolean d(ItemStack itemStack) {
      if (itemStack != null && !itemStack.isEmpty()) {
         String var2 = Registries.ITEM.getId(itemStack.getItem()).toString().toLowerCase(Locale.ROOT);
         return var2.contains("gray_stained_glass_pane");
      } else {
         return false;
      }
   }

   private boolean e(ItemStack itemStack) {
      if (itemStack != null && !itemStack.isEmpty()) {
         String var2 = Registries.ITEM.getId(itemStack.getItem()).toString().toLowerCase(Locale.ROOT);
         return !var2.contains("pane") ? false : itemStack.getName().getString().toLowerCase(Locale.ROOT).contains("list");
      } else {
         return false;
      }
   }

   private boolean f(ItemStack itemStack) {
      return itemStack != null && !itemStack.isEmpty() ? "minecraft:chest".equalsIgnoreCase(Registries.ITEM.getId(itemStack.getItem()).toString()) : false;
   }

   private boolean g(ItemStack itemStack) {
      return itemStack != null && !itemStack.isEmpty()
         ? "minecraft:ender_chest".equalsIgnoreCase(Registries.ITEM.getId(itemStack.getItem()).toString())
         : false;
   }

   private int G() {
      return this.player.currentScreenHandler != null ? this.player.currentScreenHandler.syncId : -1;
   }

   private boolean h(ItemStack itemStack) {
      if (itemStack != null && !itemStack.isEmpty()) {
         String var2 = Registries.ITEM.getId(itemStack.getItem()).toString().toLowerCase(Locale.ROOT);
         return var2.contains("sign");
      } else {
         return false;
      }
   }

   private int h(ScreenHandler screenHandler) {
      int var2 = this.settings.orderItemSlot();
      if (var2 >= 0) {
         return this.a(screenHandler, var2) ? var2 : -1;
      } else {
         int var3 = this.a(screenHandler);

         for (int var4 = 0; var4 < var3; var4++) {
            if (this.a(screenHandler.getSlot(var4).getStack())) {
               return var4;
            }
         }

         return -1;
      }
   }

   private int i(ScreenHandler screenHandler) {
      int var2 = this.a(screenHandler);

      for (int var3 = 0; var3 < var2; var3++) {
         ItemStack var4 = screenHandler.getSlot(var3).getStack();
         if (!var4.isEmpty()) {
            String var5 = Registries.ITEM.getId(var4.getItem()).toString().toLowerCase(Locale.ROOT);
            if (var5.contains("anvil")) {
               return var3;
            }
         }
      }

      return -1;
   }

   private int j(ScreenHandler screenHandler) {
      int var2 = this.a(screenHandler);

      for (int var3 = 0; var3 < var2; var3++) {
         ItemStack var4 = screenHandler.getSlot(var3).getStack();
         if (this.g(var4)) {
            return var3;
         }
      }

      return -1;
   }

   private int k(ScreenHandler screenHandler) {
      int var2 = Math.min(18, this.a(screenHandler));
      int var3 = 0;

      for (int var4 = 0; var4 < var2; var4++) {
         ItemStack var5 = screenHandler.getSlot(var4).getStack();
         if (!var5.isEmpty() && !this.b(var5)) {
            var3++;
         }
      }

      return var3;
   }

   private ItemStack l(ScreenHandler screenHandler) {
      int var2 = Math.min(18, this.a(screenHandler));

      for (int var3 = 0; var3 < var2; var3++) {
         ItemStack var4 = screenHandler.getSlot(var3).getStack();
         if (!var4.isEmpty() && !this.b(var4) && !this.a(var4)) {
            return var4;
         }
      }

      return null;
   }

   private boolean H() {
      if (this.player == null) {
         return false;
      } else {
         for (int var1 = 0; var1 < 36; var1++) {
            ItemStack var2 = this.player.getInventory().getStack(var1);
            if (!var2.isEmpty() && !this.a(var2)) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean I() {
      return this.settings.idleRescanSeconds() > 0
         && !this.initialListingCheck
         && System.currentTimeMillis() - this.lastListingScanTime >= this.settings.idleRescanSeconds() * 1000L;
   }

   private void g(String string) {
      this.m(string + ", opening own listings for recount.");
      this.c();
      this.initialListingCheck = false;
      this.state = AuctionHouseEngine.b.PRECHECK_AH;
      this.delayTimer = 2;
   }

   private void h(String string) {
      if (!this.limitReached || this.state != AuctionHouseEngine.b.IDLE) {
         this.m(string + ", going idle until a slot frees.");
         this.limitReached = true;
         this.listedSlots = Math.min(18, this.f());
         this.c();
         this.state = AuctionHouseEngine.b.IDLE;
         this.delayTimer = a(this.settings.closeDelay());
         this.lastListingScanTime = System.currentTimeMillis();
      }
   }

   private String J() {
      return this.settings.ahAlias() + " " + accountName(this.player);
   }

   static String accountName(ClientPlayerEntity clientPlayerEntity) {
      String var1 = MinecraftClient.getInstance().getSession().getUsername();
      if (var1 != null && !var1.isBlank()) {
         return var1;
      } else {
         return clientPlayerEntity == null ? "" : clientPlayerEntity.getGameProfile().name();
      }
   }

   private void i(String string) {
      this.c();
      if (!this.blockedHold) {
         this.blockedHold = true;
         this.a(string + ". Staying on.");
      }

      this.state = AuctionHouseEngine.b.IDLE;
      this.delayTimer = a(this.settings.closeDelay());
      this.lastListingScanTime = System.currentTimeMillis();
   }

   private void a(AuctionHouseEngine.b arg, String string) {
      this.m(string + ", stepping back to " + arg.name());
      this.noStockTicks = 0;
      this.signWaitStartMs = 0L;
      this.state = arg;
      this.delayTimer = a(this.settings.menuDelay());
   }

   private void j(String string) {
      this.m(string + ", reopening /order");
      this.noStockTicks = 0;
      this.signWaitStartMs = 0L;
      this.state = AuctionHouseEngine.b.OPEN_ORDER;
      this.delayTimer = a(Math.max(4, this.settings.closeDelay()));
   }

   private boolean b(long l) {
      return System.currentTimeMillis() - this.lastStateTime >= l;
   }

   private void a(int i, int j, SlotActionType slotActionType) {
      if (this.player != null && this.player.currentScreenHandler != null) {
         this.a(this.player.currentScreenHandler, i, j, slotActionType);
      }
   }

   private void a(ScreenHandler screenHandler, int i, int j, SlotActionType slotActionType) {
      this.a().interactionManager.clickSlot(screenHandler.syncId, i, j, slotActionType, this.player);
   }

   private void k(String string) {
      this.c();
      String var2 = string.trim();
      if (var2.startsWith("/")) {
         var2 = var2.substring(1);
      }

      if (MoneyCommandGuard.allowAutomated("/" + var2, "AH Trader")) {
         this.player.networkHandler.sendChatCommand(var2);
         this.m("Sent command: /" + var2);
      }
   }

   private void l(String string) {
      this.c();
      this.a(string + ". Restarting.");
      this.limitReached = false;
      this.alreadyReopens = 0;
      this.noStockTicks = 0;
      this.state = AuctionHouseEngine.b.RESTART;
      this.delayTimer = 20;
   }

   private void m(String string) {
      if (this.settings.debugChat()) {
         this.b(string);
      }
   }

   @Environment(EnvType.CLIENT)
   private record a(int matchingSlots, int totalItems) {
   }

   @Environment(EnvType.CLIENT)
   private static enum b {
      IDLE,
      PRECHECK_AH,
      READ_OWN_LISTINGS,
      OPEN_ORDER,
      SELECT_ORDER_MENU,
      SELECT_REQUESTED_ITEM,
      OPEN_REQUESTED_CHEST,
      WAIT_REQUESTED_CHEST_OPEN,
      TAKE_STOCK,
      CLOSE_ORDER_SCREEN,
      PREPARE_HELD_ITEM,
      SPLIT_SINGLE,
      SEND_SELL_COMMAND,
      SELL_COOLDOWN,
      SHOP_OPEN,
      SHOP_READ,
      COLLECT_OPEN_AH,
      COLLECT_OPEN_CHEST,
      COLLECT_WAIT_CHEST,
      COLLECT_PULL,
      COLLECT_RECOVER_ANVIL,
      RESTART;
   }
}
