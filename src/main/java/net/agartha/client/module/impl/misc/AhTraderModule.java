package net.agartha.client.module.impl.misc;

import java.util.function.BooleanSupplier;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.feature.auction.AuctionHouseEngine;
import net.agartha.client.feature.auction.AuctionHouseSettings;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.ItemIdSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.module.setting.StringSetting;
import net.agartha.client.util.net.ChatBus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class AhTraderModule extends Module implements AuctionHouseSettings {
   private static final int AUTO_SLOT = -1;
   private final EnumSetting sellMode = (EnumSetting)this.addSetting(new EnumSetting("Sell Mode", "Multi", new String[]{"Multi", "Single"}));
   private final EnumSetting priceMode = (EnumSetting)this.addSetting(new EnumSetting("Price Mode", "Flat", new String[]{"Flat", "Competition"}));
   private final BooleanSetting sDebugChat = (BooleanSetting)this.addSetting(
      new BooleanSetting("Debug Chat", true)
   );
   private final StringSetting sMinPrice = (StringSetting)this.addSetting(new StringSetting("Min Price", "70k"));
   private final StringSetting sMaxPrice = (StringSetting)this.addSetting(new StringSetting("Max Price", "120k"));
   private final StringSetting sUndercutAmount = (StringSetting)this.addSetting(new StringSetting("Undercut Amount", "1k"));
   private final StringSetting sSellPrice = (StringSetting)this.addSetting(new StringSetting("Sell Price", "500k"));
   private final ItemIdSetting sTargetItem = (ItemIdSetting)this.addSetting(new ItemIdSetting("Target Item", "minecraft:obsidian"));
   private final NumberSetting sTargetListings = (NumberSetting)this.addSetting(new NumberSetting("Target Listings", 18.0F, 1.0F, 18.0F).whole());
   private final NumberSetting sMultiInventoryStock = (NumberSetting)this.addSetting(new NumberSetting("Multi Inventory Stock", 18.0F, 1.0F, 36.0F).whole());
   private final NumberSetting sSingleInventoryStock = (NumberSetting)this.addSetting(new NumberSetting("Single Inventory Stock", 1.0F, 1.0F, 18.0F).whole());
   private final NumberSetting sIdleRescanSeconds = (NumberSetting)this.addSetting(new NumberSetting("Idle Rescan Seconds", 7.0F, 3.0F, 60.0F).whole());
   private final NumberSetting sListSpeed = (NumberSetting)this.addSetting(new NumberSetting("List Speed", 8.0F, 1.0F, 10.0F).whole());
   private final NumberSetting sSwapSpeed = (NumberSetting)this.addSetting(new NumberSetting("Swap Speed", 8.0F, 1.0F, 10.0F).whole());
   private final NumberSetting sMenuDelay = (NumberSetting)this.addSetting(new NumberSetting("Menu Delay", 8.0F, 1.0F, 60.0F).whole());
   private final NumberSetting sOrderRetryDelay = (NumberSetting)this.addSetting(new NumberSetting("Order Retry Delay", 5.0F, 1.0F, 60.0F).whole());
   private final NumberSetting sCollectionDelay = (NumberSetting)this.addSetting(new NumberSetting("Collection Delay", 2.0F, 0.0F, 20.0F).whole());
   private final NumberSetting sCloseDelay = (NumberSetting)this.addSetting(new NumberSetting("Close Delay", 4.0F, 1.0F, 40.0F).whole());
   private final NumberSetting sEmptyStableSeconds = (NumberSetting)this.addSetting(new NumberSetting("Empty Stable Seconds", 2.0F, 1.0F, 10.0F).whole());
   private final NumberSetting sMissingOrderSeconds = (NumberSetting)this.addSetting(new NumberSetting("Missing Order Seconds", 5.0F, 1.0F, 30.0F).whole());
   private final NumberSetting sTimeoutSeconds = (NumberSetting)this.addSetting(new NumberSetting("Timeout Seconds", 30.0F, 5.0F, 120.0F).whole());
   private final AuctionHouseEngine engine = new AuctionHouseEngine(this, () -> this.setEnabled(false));

   public AhTraderModule() {
      super(
         "AH Trader",
         "Orders, lists and maintains Donut AH stock (multi or single sell). /ah sell lists instantly. Own listings use your logged-in account name. List and swap speeds are jittered and cannot hit every tick.",
         ModuleCategory.DONUT
      );
      this.sTargetItem.aliases(new String[]{"Target Item ID"});
      this.sMinPrice.visibleWhen(this::competitionPricing);
      this.sMaxPrice.visibleWhen(this::competitionPricing);
      this.sUndercutAmount.visibleWhen(this::competitionPricing);
      this.sSellPrice.visibleWhen(() -> !this.competitionPricing());
      this.sMultiInventoryStock.visibleWhen(() -> !this.singleSell());
      this.sSingleInventoryStock.visibleWhen(this::singleSell);
      this.lockDelaySettings();
   }

   private void lockDelaySettings() {
      BooleanSupplier var1 = () -> false;
      this.sMenuDelay.visibleWhen(var1);
      this.sOrderRetryDelay.visibleWhen(var1);
      this.sCollectionDelay.visibleWhen(var1);
      this.sCloseDelay.visibleWhen(var1);
   }

   protected void onEnable() {
      ChatBus.setListener(this.engine::onChat);
      this.engine.onEnable();
   }

   protected void onDisable() {
      ChatBus.clearListener();
      this.engine.onDisable();
   }

   @EventListener
   public void onTick(TickEvent arg) {
      if (this.isEnabled()) {
         this.engine.tick();
      }
   }

   public String statusLine() {
      return this.engine.statusLine();
   }

   public boolean singleSell() {
      return this.sellMode.is("Single");
   }

   public boolean competitionPricing() {
      return this.priceMode.is("Competition");
   }

   public boolean debugChat() {
      return this.sDebugChat.get();
   }

   public String shopCommand() {
      return "shop";
   }

   public String minPrice() {
      return this.sMinPrice.get();
   }

   public String maxPrice() {
      return this.sMaxPrice.get();
   }

   public String undercutAmount() {
      return this.sUndercutAmount.get();
   }

   public String ownListingsCommand() {
      return "ah {player}";
   }

   public String orderCommand() {
      return "order";
   }

   public String ahAlias() {
      return "ah";
   }

   public String sellPrice() {
      return this.sSellPrice.get();
   }

   public String targetItemId() {
      return this.sTargetItem.id();
   }

   public String requestedMatch() {
      return "requested";
   }

   public int orderCategorySlot() {
      return -1;
   }

   public int orderItemSlot() {
      return -1;
   }

   public int targetListings() {
      return this.sTargetListings.getInt();
   }

   public int multiInventoryStock() {
      return this.sMultiInventoryStock.getInt();
   }

   public int singleInventoryStock() {
      return this.sSingleInventoryStock.getInt();
   }

   public int idleRescanSeconds() {
      return this.sIdleRescanSeconds.getInt();
   }

   public int menuDelay() {
      return this.sMenuDelay.getInt();
   }

   public int orderRetryDelay() {
      return this.sOrderRetryDelay.getInt();
   }

   public int collectionDelay() {
      return this.sCollectionDelay.getInt();
   }

   public int splitDelay() {
      return Math.max(2, 12 - this.sSwapSpeed.getInt());
   }

   public int sellCommandDelay() {
      return Math.max(4, 24 - this.sListSpeed.getInt() * 2);
   }

   public int closeDelay() {
      return this.sCloseDelay.getInt();
   }

   public int emptyStableSeconds() {
      return this.sEmptyStableSeconds.getInt();
   }

   public int missingOrderSeconds() {
      return this.sMissingOrderSeconds.getInt();
   }

   public int timeoutSeconds() {
      return this.sTimeoutSeconds.getInt();
   }
}
