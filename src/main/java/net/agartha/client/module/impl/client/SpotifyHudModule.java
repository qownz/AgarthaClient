package net.agartha.client.module.impl.client;

import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.module.a_pkg.j;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SpotifyHudModule extends b {
   private final j scale = (j)this.addSetting(new j("Scale", 1.0F, 0.5F, 2.5F));
   private final j opacity = (j)this.addSetting(new j("Opacity", 1.0F, 0.15F, 1.0F));
   private final net.agartha.client.module.a_pkg.b shadow = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Shadow", true));
   private final net.agartha.client.module.a_pkg.b showArtist = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Show Artist", true)
   );
   private final net.agartha.client.module.a_pkg.b showWhenPaused = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Show When Paused", true)
   );
   private final net.agartha.client.module.a_pkg.b debug = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Debug", false));

   public SpotifyHudModule() {
      super("Spotify HUD", "Shows the song playing in the Spotify desktop app, with album art and progress.", c.CLIENT);
      this.scale.visibleWhen(() -> false);
   }

   protected void onEnable() {
      net.agartha.client.n.b.getInstance().ensureStarted();
   }

   public j scaleSetting() {
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
