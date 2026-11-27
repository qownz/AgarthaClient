package net.agartha.client.module.impl.client;

import net.agartha.client.integration.spotify.SpotifySmtcBridge;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SpotifyHudModule extends Module {
   private final NumberSetting scale = (NumberSetting)this.addSetting(new NumberSetting("Scale", 1.0F, 0.5F, 2.5F));
   private final NumberSetting opacity = (NumberSetting)this.addSetting(new NumberSetting("Opacity", 1.0F, 0.15F, 1.0F));
   private final BooleanSetting shadow = (BooleanSetting)this.addSetting(new BooleanSetting("Shadow", true));
   private final BooleanSetting showArtist = (BooleanSetting)this.addSetting(
      new BooleanSetting("Show Artist", true)
   );
   private final BooleanSetting showWhenPaused = (BooleanSetting)this.addSetting(
      new BooleanSetting("Show When Paused", true)
   );
   private final BooleanSetting debug = (BooleanSetting)this.addSetting(new BooleanSetting("Debug", false));

   public SpotifyHudModule() {
      super("Spotify HUD", "Shows the song playing in the Spotify desktop app, with album art and progress.", ModuleCategory.CLIENT);
      this.scale.visibleWhen(() -> false);
   }

   protected void onEnable() {
      SpotifySmtcBridge.getInstance().ensureStarted();
   }

   public NumberSetting scaleSetting() {
      return this.scale;
   }

   public float scale() {
      return this.scale.get();
   }

   public float opacity() {
      return this.opacity.get();
   }

   public boolean shadow() {
      return this.shadow.get();
   }

   public boolean showArtist() {
      return this.showArtist.get();
   }

   public boolean showWhenPaused() {
      return this.showWhenPaused.get();
   }

   public boolean debug() {
      return this.debug.get();
   }
}
