package net.agartha.client.module.impl.client;

import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ColorSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class RegionMapModule extends Module {
   private final NumberSetting cellSize = (NumberSetting)this.addSetting(new NumberSetting("Cell Size", 22.0F, 10.0F, 50.0F));
   private final NumberSetting cellGap = (NumberSetting)this.addSetting(new NumberSetting("Cell Gap", 2.0F, 0.0F, 10.0F));
   private final NumberSetting textScale = (NumberSetting)this.addSetting(new NumberSetting("Text Scale", 1.0F, 0.1F, 2.0F));
   private final NumberSetting opacity = (NumberSetting)this.addSetting(new NumberSetting("Opacity", 1.0F, 0.1F, 1.0F));
   private final BooleanSetting showRegionIds = (BooleanSetting)this.addSetting(
      new BooleanSetting("Show Region IDs", true)
   );
   private final BooleanSetting showPlayer = (BooleanSetting)this.addSetting(
      new BooleanSetting("Show Player Icon", true)
   );
   private final BooleanSetting showCoords = (BooleanSetting)this.addSetting(
      new BooleanSetting("Show Coordinates", true)
   );
   private final BooleanSetting showNether = (BooleanSetting)this.addSetting(
      new BooleanSetting("Show Nether Coordinates", true)
   );
   private final BooleanSetting showRegionInfo = (BooleanSetting)this.addSetting(
      new BooleanSetting("Show Region Info", true)
   );
   private final NumberSetting playerScale = (NumberSetting)this.addSetting(new NumberSetting("Player Scale", 1.0F, 0.5F, 3.0F));
   private final BooleanSetting customPlayerColor = (BooleanSetting)this.addSetting(
      new BooleanSetting("Custom Player Color", false)
   );
   private final ColorSetting playerIndicatorColor = (ColorSetting)this.addSetting(new ColorSetting("Player Indicator", -52686));

   public RegionMapModule() {
      super("Region Map", "DonutSMP region map with directional indicator.", ModuleCategory.CLIENT);
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

   public BooleanSetting showRegionIdsSetting() {
      return this.showRegionIds;
   }

   public BooleanSetting showPlayerSetting() {
      return this.showPlayer;
   }

   public BooleanSetting showCoordsSetting() {
      return this.showCoords;
   }

   public BooleanSetting showNetherSetting() {
      return this.showNether;
   }

   public BooleanSetting showRegionInfoSetting() {
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

   public NumberSetting cellSizeSetting() {
      return this.cellSize;
   }

   public NumberSetting cellGapSetting() {
      return this.cellGap;
   }

   public NumberSetting textScaleSetting() {
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

   public ColorSetting playerIndicatorColor() {
      return this.playerIndicatorColor;
   }

   public boolean customPlayerColor() {
      return this.customPlayerColor.get();
   }
}
