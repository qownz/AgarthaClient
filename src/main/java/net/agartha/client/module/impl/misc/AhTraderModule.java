package net.agartha.client.module.impl.misc;

import java.util.function.BooleanSupplier;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.m.a.c;
import net.agartha.client.module.b;
import net.agartha.client.module.a_pkg.f;
import net.agartha.client.module.a_pkg.i;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.module.a_pkg.l;
import net.agartha.client.util.m;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class AhTraderModule extends b implements c {
   private static final int AUTO_SLOT = -1;
   private final i sellMode = (i)this.addSetting(new i("Sell Mode", "Multi", new String[]{"Multi", "Single"}));
   private final i priceMode = (i)this.addSetting(new i("Price Mode", "Flat", new String[]{"Flat", "Competition"}));
   private final net.agartha.client.module.a_pkg.b sDebugChat = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Debug Chat", true)
   );
   private final l sMinPrice = (l)this.addSetting(new l("Min Price", "70k"));
   private final l sMaxPrice = (l)this.addSetting(new l("Max Price", "120k"));
   private final l sUndercutAmount = (l)this.addSetting(new l("Undercut Amount", "1k"));
   private final l sSellPrice = (l)this.addSetting(new l("Sell Price", "500k"));
   private final f sTargetItem = (f)this.addSetting(new f("Target Item", "minecraft:obsidian"));
   private final j sTargetListings = (j)this.addSetting(new j("Target Listings", 18.0F, 1.0F, 18.0F).whole());
   private final j sMultiInventoryStock = (j)this.addSetting(new j("Multi Inventory Stock", 18.0F, 1.0F, 36.0F).whole());
   private final j sSingleInventoryStock = (j)this.addSetting(new j("Single Inventory Stock", 1.0F, 1.0F, 18.0F).whole());
   private final j sIdleRescanSeconds = (j)this.addSetting(new j("Idle Rescan Seconds", 7.0F, 3.0F, 60.0F).whole());
   private final j sListSpeed = (j)this.addSetting(new j("List Speed", 8.0F, 1.0F, 10.0F).whole());
   private final j sSwapSpeed = (j)this.addSetting(new j("Swap Speed", 8.0F, 1.0F, 10.0F).whole());
   private final j sMenuDelay = (j)this.addSetting(new j("Menu Delay", 8.0F, 1.0F, 60.0F).whole());
   private final j sOrderRetryDelay = (j)this.addSetting(new j("Order Retry Delay", 5.0F, 1.0F, 60.0F).whole());
   private final j sCollectionDelay = (j)this.addSetting(new j("Collection Delay", 2.0F, 0.0F, 20.0F).whole());
   private final j sCloseDelay = (j)this.addSetting(new j("Close Delay", 4.0F, 1.0F, 40.0F).whole());
   private final j sEmptyStableSeconds = (j)this.addSetting(new j("Empty Stable Seconds", 2.0F, 1.0F, 10.0F).whole());
   private final j sMissingOrderSeconds = (j)this.addSetting(new j("Missing Order Seconds", 5.0F, 1.0F, 30.0F).whole());
   private final j sTimeoutSeconds = (j)this.addSetting(new j("Timeout Seconds", 30.0F, 5.0F, 120.0F).whole());
   private final net.agartha.client.m.a.b_o engine = new net.agartha.client.m.a.b_o(this, () -> this.setEnabled(false));

   public AhTraderModule() {
      super(
         "AH Trader",
         "Orders, lists and maintains Donut AH stock (multi or single sell). /ah sell lists instantly. Own listings use your logged-in account name. List and swap speeds are jittered and cannot hit every tick.",
         net.agartha.client.module.c.DONUT
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
      m.setListener(this.engine::onChat);
      this.engine.onEnable();
   }

   protected void onDisable() {
      m.clearListener();
      this.engine.onDisable();
   }

   @net.agartha.client.d.f
   public void onTick(q arg) {
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
