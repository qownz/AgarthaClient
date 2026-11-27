package net.agartha.client.m.a;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface c {
   boolean singleSell();

   boolean competitionPricing();

   boolean debugChat();

   String shopCommand();

   String minPrice();

   String maxPrice();

   String undercutAmount();

   String ownListingsCommand();

   String orderCommand();

   String ahAlias();

   String sellPrice();

   String targetItemId();

   String requestedMatch();

   int orderCategorySlot();

   int orderItemSlot();

   int targetListings();

   int multiInventoryStock();

   int singleInventoryStock();

   int idleRescanSeconds();

   int menuDelay();

   int orderRetryDelay();

   int collectionDelay();

   int splitDelay();

   int sellCommandDelay();

   int closeDelay();

   int emptyStableSeconds();

   int missingOrderSeconds();

   int timeoutSeconds();
}
