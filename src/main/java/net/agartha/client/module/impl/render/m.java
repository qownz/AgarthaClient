package net.agartha.client.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public final class m extends net.agartha.client.module.b {
   private final net.agartha.client.module.a_pkg.b portalOverlay = this.addSetting(new net.agartha.client.module.a_pkg.b("Portal Overlay", false));
   private final net.agartha.client.module.a_pkg.b spyglassOverlay = this.addSetting(new net.agartha.client.module.a_pkg.b("Spyglass Overlay", false));
   private final net.agartha.client.module.a_pkg.b nausea = this.addSetting(new net.agartha.client.module.a_pkg.b("Nausea", false));
   private final net.agartha.client.module.a_pkg.b pumpkinOverlay = this.addSetting(new net.agartha.client.module.a_pkg.b("Pumpkin Overlay", false));
   private final net.agartha.client.module.a_pkg.b powderedSnowOverlay = this.addSetting(new net.agartha.client.module.a_pkg.b("Powdered Snow Overlay", false));
   private final net.agartha.client.module.a_pkg.b fireOverlay = this.addSetting(new net.agartha.client.module.a_pkg.b("Fire Overlay", false));
   private final net.agartha.client.module.a_pkg.b liquidOverlay = this.addSetting(new net.agartha.client.module.a_pkg.b("Liquid Overlay", false));
   private final net.agartha.client.module.a_pkg.b inWallOverlay = this.addSetting(new net.agartha.client.module.a_pkg.b("In Wall Overlay", false));
   private final net.agartha.client.module.a_pkg.b vignette = this.addSetting(new net.agartha.client.module.a_pkg.b("Vignette", false));
   private final net.agartha.client.module.a_pkg.b guiBackground = this.addSetting(new net.agartha.client.module.a_pkg.b("GUI Background", false));
   private final net.agartha.client.module.a_pkg.b totemAnimation = this.addSetting(new net.agartha.client.module.a_pkg.b("Totem Animation", false));
   private final net.agartha.client.module.a_pkg.b eatingParticles = this.addSetting(new net.agartha.client.module.a_pkg.b("Eating Particles", false));
   private final net.agartha.client.module.a_pkg.b enchantmentGlint = this.addSetting(new net.agartha.client.module.a_pkg.b("Enchantment Glint", false));
   private final net.agartha.client.module.a_pkg.b bossBar = this.addSetting(new net.agartha.client.module.a_pkg.b("Boss Bar", false));
   private final net.agartha.client.module.a_pkg.b scoreboard = this.addSetting(new net.agartha.client.module.a_pkg.b("Scoreboard", false));
   private final net.agartha.client.module.a_pkg.b crosshair = this.addSetting(new net.agartha.client.module.a_pkg.b("Crosshair", false));
   private final net.agartha.client.module.a_pkg.b title = this.addSetting(new net.agartha.client.module.a_pkg.b("Title", false));
   private final net.agartha.client.module.a_pkg.b heldItemName = this.addSetting(new net.agartha.client.module.a_pkg.b("Held Item Name", false));
   private final net.agartha.client.module.a_pkg.b obfuscation = this.addSetting(new net.agartha.client.module.a_pkg.b("Obfuscation", false));
   private final net.agartha.client.module.a_pkg.b potionIcons = this.addSetting(new net.agartha.client.module.a_pkg.b("Potion Icons", false));
   private final net.agartha.client.module.a_pkg.b messageSignatureIndicator = this.addSetting(
      new net.agartha.client.module.a_pkg.b("Message Signature Indicator", false)
   );
   private final net.agartha.client.module.a_pkg.b weather = this.addSetting(new net.agartha.client.module.a_pkg.b("Weather", false));
   private final net.agartha.client.module.a_pkg.b worldBorder = this.addSetting(new net.agartha.client.module.a_pkg.b("World Border", false));
   private final net.agartha.client.module.a_pkg.b blindness = this.addSetting(new net.agartha.client.module.a_pkg.b("Blindness", false));
   private final net.agartha.client.module.a_pkg.b darkness = this.addSetting(new net.agartha.client.module.a_pkg.b("Darkness", false));
   private final net.agartha.client.module.a_pkg.b fog = this.addSetting(new net.agartha.client.module.a_pkg.b("Fog", false));
   private final net.agartha.client.module.a_pkg.b enchantmentTableBook = this.addSetting(new net.agartha.client.module.a_pkg.b("Enchantment Table Book", false));
   private final net.agartha.client.module.a_pkg.b signText = this.addSetting(new net.agartha.client.module.a_pkg.b("Sign Text", false));
   private final net.agartha.client.module.a_pkg.b blockBreakOverlay = this.addSetting(new net.agartha.client.module.a_pkg.b("Block Break Overlay", false));
   private final net.agartha.client.module.a_pkg.b beaconBeams = this.addSetting(new net.agartha.client.module.a_pkg.b("Beacon Beams", false));
   private final net.agartha.client.module.a_pkg.b fallingBlocks = this.addSetting(new net.agartha.client.module.a_pkg.b("Falling Blocks", false));
   private final net.agartha.client.module.a_pkg.b caveCulling = this.addSetting(new net.agartha.client.module.a_pkg.b("Cave Culling", false));
   private final net.agartha.client.module.a_pkg.b mapMarkers = this.addSetting(new net.agartha.client.module.a_pkg.b("Map Markers", false));
   private final net.agartha.client.module.a_pkg.b mapContents = this.addSetting(new net.agartha.client.module.a_pkg.b("Map Contents", false));
   private final net.agartha.client.module.a_pkg.i banners = this.addSetting(
      new net.agartha.client.module.a_pkg.i("Banners", m.a.Everything.name(), m.a.Everything.name(), m.a.Pillar.name(), m.a.None.name())
   );
   private final net.agartha.client.module.a_pkg.b fireworkExplosions = this.addSetting(new net.agartha.client.module.a_pkg.b("Firework Explosions", false));
   private final net.agartha.client.module.a_pkg.b barrierInvisibility = this.addSetting(new net.agartha.client.module.a_pkg.b("Barrier Invisibility", false));
   private final net.agartha.client.module.a_pkg.b textureRotations = this.addSetting(new net.agartha.client.module.a_pkg.b("Texture Rotations", false));
   private final net.agartha.client.module.a_pkg.b armor = this.addSetting(new net.agartha.client.module.a_pkg.b("Armor", false));
   private final net.agartha.client.module.a_pkg.b invisibility = this.addSetting(new net.agartha.client.module.a_pkg.b("Invisibility", false));
   private final net.agartha.client.module.a_pkg.b glowing = this.addSetting(new net.agartha.client.module.a_pkg.b("Glowing", false));
   private final net.agartha.client.module.a_pkg.b spawnerEntities = this.addSetting(new net.agartha.client.module.a_pkg.b("Spawner Entities", false));
   private final net.agartha.client.module.a_pkg.b deadEntities = this.addSetting(new net.agartha.client.module.a_pkg.b("Dead Entities", false));
   private final net.agartha.client.module.a_pkg.b nametags = this.addSetting(new net.agartha.client.module.a_pkg.b("Nametags", false));
   private boolean runtimeActive;
   private World lastJoinedLevel;

   public m() {
      super("No Render", "Disables certain animations or overlays from rendering.", net.agartha.client.module.c.RENDER);
      this.enabled().set(true);

      for (net.agartha.client.module.a_pkg.k var2 : this.settings()) {
         var2.visibleWhen(() -> false);
      }

      this.caveCulling.onChange(this::c);
      this.textureRotations.onChange(this::c);
      net.agartha.client.d.a_pkg.q.TYPE.listen(this, this::a);
   }

   @Override
   protected void onEnable() {
      if (MinecraftClient.getInstance().world != null) {
         this.a();
      }
   }

   @Override
   protected void onDisable() {
      this.b();
   }

   public boolean isRuntimeActive() {
      return this.runtimeActive;
   }

   private void a(net.agartha.client.d.a_pkg.q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientWorld var3 = var2.world;
      if (var3 == null) {
         if (this.lastJoinedLevel != null) {
            this.lastJoinedLevel = null;
            this.b();
         }
      } else if (var3 != this.lastJoinedLevel) {
         this.lastJoinedLevel = var3;
         if (this.enabled().get()) {
            this.a();
         }
      }
   }

   private void a() {
      if (!this.runtimeActive) {
         this.runtimeActive = true;
         this.d();
      }
   }

   private void b() {
      if (this.runtimeActive) {
         this.runtimeActive = false;
         this.d();
      }
   }

   private void c() {
      if (this.runtimeActive) {
         if (this.caveCulling.get() || this.textureRotations.get()) {
            MinecraftClient var1 = MinecraftClient.getInstance();
            if (var1.worldRenderer != null) {
               var1.worldRenderer.reload();
            }
         }
      }
   }

   private void d() {
      this.c();
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.worldRenderer != null) {
         var1.worldRenderer.scheduleTerrainUpdate();
      }
   }

   public net.agartha.client.module.a_pkg.b[] settingsFor(m.b arg) {
      return switch (arg) {
         case OVERLAY -> new net.agartha.client.module.a_pkg.b[]{
            this.portalOverlay,
            this.spyglassOverlay,
            this.nausea,
            this.pumpkinOverlay,
            this.powderedSnowOverlay,
            this.fireOverlay,
            this.liquidOverlay,
            this.inWallOverlay,
            this.vignette,
            this.guiBackground,
            this.totemAnimation,
            this.eatingParticles,
            this.enchantmentGlint
         };
         case HUD -> new net.agartha.client.module.a_pkg.b[]{
            this.bossBar, this.scoreboard, this.crosshair, this.title, this.heldItemName, this.obfuscation, this.potionIcons, this.messageSignatureIndicator
         };
         case WORLD -> new net.agartha.client.module.a_pkg.b[]{
            this.weather,
            this.worldBorder,
            this.blindness,
            this.darkness,
            this.fog,
            this.enchantmentTableBook,
            this.signText,
            this.blockBreakOverlay,
            this.beaconBeams,
            this.fallingBlocks,
            this.caveCulling,
            this.mapMarkers,
            this.mapContents,
            this.fireworkExplosions,
            this.barrierInvisibility,
            this.textureRotations
         };
         case ENTITY -> new net.agartha.client.module.a_pkg.b[]{
            this.armor, this.invisibility, this.glowing, this.spawnerEntities, this.deadEntities, this.nametags
         };
      };
   }

   public net.agartha.client.module.a_pkg.i bannersSetting() {
      return this.banners;
   }

   private boolean e() {
      return this.runtimeActive;
   }

   public boolean noPortalOverlay() {
      return this.e() && this.portalOverlay.get();
   }

   public boolean noSpyglassOverlay() {
      return this.e() && this.spyglassOverlay.get();
   }

   public boolean noNausea() {
      return this.e() && this.nausea.get();
   }

   public boolean noPumpkinOverlay() {
      return this.e() && this.pumpkinOverlay.get();
   }

   public boolean noPowderedSnowOverlay() {
      return this.e() && this.powderedSnowOverlay.get();
   }

   public boolean noFireOverlay() {
      return this.e() && this.fireOverlay.get();
   }

   public boolean noLiquidOverlay() {
      return this.e() && this.liquidOverlay.get();
   }

   public boolean noInWallOverlay() {
      return this.e() && this.inWallOverlay.get();
   }

   public boolean noVignette() {
      return this.e() && this.vignette.get();
   }

   public boolean noGuiBackground() {
      return this.e() && this.guiBackground.get();
   }

   public boolean noTotemAnimation() {
      return this.e() && this.totemAnimation.get();
   }

   public boolean noEatParticles() {
      return this.e() && this.eatingParticles.get();
   }

   public boolean noEnchantGlint() {
      return this.e() && this.enchantmentGlint.get();
   }

   public boolean noBossBar() {
      return this.e() && this.bossBar.get();
   }

   public boolean noScoreboard() {
      return this.e() && this.scoreboard.get();
   }

   public boolean noCrosshair() {
      return this.e() && this.crosshair.get();
   }

   public boolean noTitle() {
      return this.e() && this.title.get();
   }

   public boolean noHeldItemName() {
      return this.e() && this.heldItemName.get();
   }

   public boolean noObfuscation() {
      return this.e() && this.obfuscation.get();
   }

   public boolean noPotionIcons() {
      return this.e() && this.potionIcons.get();
   }

   public boolean noMessageSignatureIndicator() {
      return this.e() && this.messageSignatureIndicator.get();
   }

   public boolean noWeather() {
      return this.e() && this.weather.get();
   }

   public boolean noWorldBorder() {
      return this.e() && this.worldBorder.get();
   }

   public boolean noBlindness() {
      return this.e() && this.blindness.get();
   }

   public boolean noDarkness() {
      return this.e() && this.darkness.get();
   }

   public boolean noFog() {
      return this.e() && this.fog.get();
   }

   public boolean noEnchTableBook() {
      return this.e() && this.enchantmentTableBook.get();
   }

   public boolean noSignText() {
      return this.e() && this.signText.get();
   }

   public boolean noBlockBreakOverlay() {
      return this.e() && this.blockBreakOverlay.get();
   }

   public boolean noBeaconBeams() {
      return this.e() && this.beaconBeams.get();
   }

   public boolean noFallingBlocks() {
      return this.e() && this.fallingBlocks.get();
   }

   public boolean noCaveCulling() {
      return this.e() && this.caveCulling.get();
   }

   public boolean noMapMarkers() {
      return this.e() && this.mapMarkers.get();
   }

   public boolean noMapContents() {
      return this.e() && this.mapContents.get();
   }

   public boolean noFireworkExplosions() {
      return this.e() && this.fireworkExplosions.get();
   }

   public boolean noBarrierInvis() {
      return this.e() && this.barrierInvisibility.get();
   }

   public boolean noTextureRotations() {
      return this.e() && this.textureRotations.get();
   }

   public m.a bannerRenderMode() {
      return !this.e() ? m.a.Everything : m.a.valueOf(this.banners.get());
   }

   public boolean noArmor() {
      return this.e() && this.armor.get();
   }

   public boolean noInvisibility() {
      return this.e() && this.invisibility.get();
   }

   public boolean noGlowing() {
      return this.e() && this.glowing.get();
   }

   public boolean noMobInSpawner() {
      return this.e() && this.spawnerEntities.get();
   }

   public boolean noDeadEntities() {
      return this.e() && this.deadEntities.get();
   }

   public boolean noNametags() {
      return this.e() && this.nametags.get();
   }

   @Environment(EnvType.CLIENT)
   public static enum a {
      Everything,
      Pillar,
      None;
   }

   @Environment(EnvType.CLIENT)
   public static enum b {
      OVERLAY,
      HUD,
      WORLD,
      ENTITY;
   }
}
