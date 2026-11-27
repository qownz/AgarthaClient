package net.agartha.client.module.impl.render;

import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.Setting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public final class NoRenderModule extends Module {
   private final BooleanSetting portalOverlay = this.addSetting(new BooleanSetting("Portal Overlay", false));
   private final BooleanSetting spyglassOverlay = this.addSetting(new BooleanSetting("Spyglass Overlay", false));
   private final BooleanSetting nausea = this.addSetting(new BooleanSetting("Nausea", false));
   private final BooleanSetting pumpkinOverlay = this.addSetting(new BooleanSetting("Pumpkin Overlay", false));
   private final BooleanSetting powderedSnowOverlay = this.addSetting(new BooleanSetting("Powdered Snow Overlay", false));
   private final BooleanSetting fireOverlay = this.addSetting(new BooleanSetting("Fire Overlay", false));
   private final BooleanSetting liquidOverlay = this.addSetting(new BooleanSetting("Liquid Overlay", false));
   private final BooleanSetting inWallOverlay = this.addSetting(new BooleanSetting("In Wall Overlay", false));
   private final BooleanSetting vignette = this.addSetting(new BooleanSetting("Vignette", false));
   private final BooleanSetting guiBackground = this.addSetting(new BooleanSetting("GUI Background", false));
   private final BooleanSetting totemAnimation = this.addSetting(new BooleanSetting("Totem Animation", false));
   private final BooleanSetting eatingParticles = this.addSetting(new BooleanSetting("Eating Particles", false));
   private final BooleanSetting enchantmentGlint = this.addSetting(new BooleanSetting("Enchantment Glint", false));
   private final BooleanSetting bossBar = this.addSetting(new BooleanSetting("Boss Bar", false));
   private final BooleanSetting scoreboard = this.addSetting(new BooleanSetting("Scoreboard", false));
   private final BooleanSetting crosshair = this.addSetting(new BooleanSetting("Crosshair", false));
   private final BooleanSetting title = this.addSetting(new BooleanSetting("Title", false));
   private final BooleanSetting heldItemName = this.addSetting(new BooleanSetting("Held Item Name", false));
   private final BooleanSetting obfuscation = this.addSetting(new BooleanSetting("Obfuscation", false));
   private final BooleanSetting potionIcons = this.addSetting(new BooleanSetting("Potion Icons", false));
   private final BooleanSetting messageSignatureIndicator = this.addSetting(
      new BooleanSetting("Message Signature Indicator", false)
   );
   private final BooleanSetting weather = this.addSetting(new BooleanSetting("Weather", false));
   private final BooleanSetting worldBorder = this.addSetting(new BooleanSetting("World Border", false));
   private final BooleanSetting blindness = this.addSetting(new BooleanSetting("Blindness", false));
   private final BooleanSetting darkness = this.addSetting(new BooleanSetting("Darkness", false));
   private final BooleanSetting fog = this.addSetting(new BooleanSetting("Fog", false));
   private final BooleanSetting enchantmentTableBook = this.addSetting(new BooleanSetting("Enchantment Table Book", false));
   private final BooleanSetting signText = this.addSetting(new BooleanSetting("Sign Text", false));
   private final BooleanSetting blockBreakOverlay = this.addSetting(new BooleanSetting("Block Break Overlay", false));
   private final BooleanSetting beaconBeams = this.addSetting(new BooleanSetting("Beacon Beams", false));
   private final BooleanSetting fallingBlocks = this.addSetting(new BooleanSetting("Falling Blocks", false));
   private final BooleanSetting caveCulling = this.addSetting(new BooleanSetting("Cave Culling", false));
   private final BooleanSetting mapMarkers = this.addSetting(new BooleanSetting("Map Markers", false));
   private final BooleanSetting mapContents = this.addSetting(new BooleanSetting("Map Contents", false));
   private final EnumSetting banners = this.addSetting(
      new EnumSetting("Banners", NoRenderModule.a.Everything.name(), NoRenderModule.a.Everything.name(), NoRenderModule.a.Pillar.name(), NoRenderModule.a.None.name())
   );
   private final BooleanSetting fireworkExplosions = this.addSetting(new BooleanSetting("Firework Explosions", false));
   private final BooleanSetting barrierInvisibility = this.addSetting(new BooleanSetting("Barrier Invisibility", false));
   private final BooleanSetting textureRotations = this.addSetting(new BooleanSetting("Texture Rotations", false));
   private final BooleanSetting armor = this.addSetting(new BooleanSetting("Armor", false));
   private final BooleanSetting invisibility = this.addSetting(new BooleanSetting("Invisibility", false));
   private final BooleanSetting glowing = this.addSetting(new BooleanSetting("Glowing", false));
   private final BooleanSetting spawnerEntities = this.addSetting(new BooleanSetting("Spawner Entities", false));
   private final BooleanSetting deadEntities = this.addSetting(new BooleanSetting("Dead Entities", false));
   private final BooleanSetting nametags = this.addSetting(new BooleanSetting("Nametags", false));
   private boolean runtimeActive;
   private World lastJoinedLevel;

   public NoRenderModule() {
      super("No Render", "Disables certain animations or overlays from rendering.", ModuleCategory.RENDER);
      this.enabled().set(true);

      for (Setting var2 : this.settings()) {
         var2.visibleWhen(() -> false);
      }

      this.caveCulling.onChange(this::c);
      this.textureRotations.onChange(this::c);
      TickEvent.TYPE.listen(this, this::a);
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

   private void a(TickEvent arg) {
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

   public BooleanSetting[] settingsFor(NoRenderModule.b arg) {
      return switch (arg) {
         case OVERLAY -> new BooleanSetting[]{
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
         case HUD -> new BooleanSetting[]{
            this.bossBar, this.scoreboard, this.crosshair, this.title, this.heldItemName, this.obfuscation, this.potionIcons, this.messageSignatureIndicator
         };
         case WORLD -> new BooleanSetting[]{
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
         case ENTITY -> new BooleanSetting[]{
            this.armor, this.invisibility, this.glowing, this.spawnerEntities, this.deadEntities, this.nametags
         };
      };
   }

   public EnumSetting bannersSetting() {
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

   public NoRenderModule.a bannerRenderMode() {
      return !this.e() ? NoRenderModule.a.Everything : NoRenderModule.a.valueOf(this.banners.get());
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
