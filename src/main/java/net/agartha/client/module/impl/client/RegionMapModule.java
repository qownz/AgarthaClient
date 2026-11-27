package net.agartha.client.module.impl.client;

import net.agartha.client.module.b;
import net.agartha.client.module.a_pkg.c;
import net.agartha.client.module.a_pkg.j;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class RegionMapModule extends b {
   private final j cellSize = (j)this.addSetting(new j("Cell Size", 22.0F, 10.0F, 50.0F));
   private final j cellGap = (j)this.addSetting(new j("Cell Gap", 2.0F, 0.0F, 10.0F));
   private final j textScale = (j)this.addSetting(new j("Text Scale", 1.0F, 0.1F, 2.0F));
   private final j opacity = (j)this.addSetting(new j("Opacity", 1.0F, 0.1F, 1.0F));
   private final net.agartha.client.module.a_pkg.b showRegionIds = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Show Region IDs", true)
   );
   private final net.agartha.client.module.a_pkg.b showPlayer = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Show Player Icon", true)
   );
   private final net.agartha.client.module.a_pkg.b showCoords = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Show Coordinates", true)
   );
   private final net.agartha.client.module.a_pkg.b showNether = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Show Nether Coordinates", true)
   );
   private final net.agartha.client.module.a_pkg.b showRegionInfo = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Show Region Info", true)
   );
   private final j playerScale = (j)this.addSetting(new j("Player Scale", 1.0F, 0.5F, 3.0F));
   private final net.agartha.client.module.a_pkg.b customPlayerColor = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Custom Player Color", false)
   );
   private final c playerIndicatorColor = (c)this.addSetting(new c("Player Indicator", -52686));

   public RegionMapModule() {
      super("Region Map", "DonutSMP region map with directional indicator.", net.agartha.client.module.c.CLIENT);
      this.showRegionIds.visibleWhen(() -> false);
      this.showPlayer.visibleWhen(() -> false);
      this.showCoords.visibleWhen(() -> false);
      this.showNether.visibleWhen(() -> false);
      this.showRegionInfo.visibleWhen(() -> false);
      this.playerScale.visibleWhen(this.showPlayer::get);
      this.customPlayerColor.visibleWhen(this.showPlayer::get);
      this.playerIndicatorColor.visibleWhen(() -> this.showPlayer.get() && this.customPlayerColor.get());
   }

   public boolean compact() {
      return !this.showCoords.get() && !this.showNether.get() && !this.showRegionInfo.get();
   }

   public net.agartha.client.module.a_pkg.b showRegionIdsSetting() {
      return this.showRegionIds;
   }

   public net.agartha.client.module.a_pkg.b showPlayerSetting() {
      return this.showPlayer;
   }

   public net.agartha.client.module.a_pkg.b showCoordsSetting() {
      return this.showCoords;
   }

   public net.agartha.client.module.a_pkg.b showNetherSetting() {
      return this.showNether;
   }

   public net.agartha.client.module.a_pkg.b showRegionInfoSetting() {
      return this.showRegionInfo;
   }

   public boolean showPlayer() {
      return this.showPlayer.get();
   }

   public boolean showCoords() {
      return this.showCoords.get();
   }

   public boolean showNether() {
      return this.showNether.get();
   }

   public boolean showRegionInfo() {
      return this.showRegionInfo.get();
   }

   public j cellSizeSetting() {
      return this.cellSize;
   }

   public j cellGapSetting() {
      return this.cellGap;
   }

   public j textScaleSetting() {
      return this.textScale;
   }

   public float cellSize() {
      return this.cellSize.get();
   }

   public float cellGap() {
      return this.cellGap.get();
   }

   public float playerScale() {
      return this.playerScale.get();
   }

   public float textScale() {
      return this.textScale.get();
   }

   public float opacity() {
      return this.opacity.get();
   }

   public boolean showRegionIds() {
      return this.showRegionIds.get();
   }

   public c playerIndicatorColor() {
      return this.playerIndicatorColor;
   }

   public boolean customPlayerColor() {
      return this.customPlayerColor.get();
   }
}
