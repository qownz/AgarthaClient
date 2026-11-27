package net.agartha.client.module.impl.client;

import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.module.a_pkg.i;
import net.agartha.client.module.a_pkg.j;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class HudModule extends b {
   public static final String MODE_1 = "Mode 1";
   public static final String MODE_2 = "Mode 2";
   private final i hudMode = (i)this.addSetting(new i("HUD Mode", "Mode 1", new String[]{"Mode 1", "Mode 2"}));
   private final j transparency = (j)this.addSetting(new j("HUD Opacity", 0.5F, 0.15F, 0.85F));
   private final net.agartha.client.module.a_pkg.b fps = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("FPS", true));
   private final net.agartha.client.module.a_pkg.b ping = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Ping", true));
   private final net.agartha.client.module.a_pkg.b coordinates = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Coordinates", false)
   );
   private final net.agartha.client.module.a_pkg.b brandBox = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Agartha+", true));
   private final net.agartha.client.module.a_pkg.b positionBox = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Position", true));
   private final net.agartha.client.module.a_pkg.b netherCoords = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Nether Coordinates", false)
   );
   private final net.agartha.client.module.a_pkg.b clock = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Time", true));
   private final net.agartha.client.module.a_pkg.b server = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Server Address", true)
   );
   private final net.agartha.client.module.a_pkg.b tps = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Server TPS", false));
   private final net.agartha.client.module.a_pkg.b players = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Players Online", false)
   );
   private final net.agartha.client.module.a_pkg.b memory = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Memory", false));
   private final net.agartha.client.module.a_pkg.b speed = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Speed (BPS)", false));
   private final net.agartha.client.module.a_pkg.b direction = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Facing", false));
   private final net.agartha.client.module.a_pkg.b regionMap = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Region Map", true));
   private final net.agartha.client.module.a_pkg.b keybindsHud = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Keybinds List", true)
   );
   private final net.agartha.client.module.a_pkg.b potions = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Effects", true));
   private final net.agartha.client.module.a_pkg.b armorHud = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Armor HUD", false));
   private final i armorLayout = (i)this.addSetting(new i("Armor Layout", "Horizontal", new String[]{"Horizontal", "Vertical"}));
   private final net.agartha.client.module.a_pkg.b armorPercent = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Armor Durability", true)
   );
   private final net.agartha.client.module.a_pkg.b armorBox = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Armor Box", true));
   private final net.agartha.client.module.a_pkg.b inventoryHud = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Inventory HUD", false)
   );
   private final net.agartha.client.module.a_pkg.b staffListHud = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Staff List", false)
   );
   private static final int STAFF_ROWS = 6;
   private final net.agartha.client.module.a_pkg.b staffShowAll = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Show All Staff", false)
   );
   private final net.agartha.client.module.a_pkg.b spotifyHud = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Spotify HUD", true)
   );
   private final net.agartha.client.module.a_pkg.b iconAnimations = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Agartha Icon Animations", true)
   );
   private final j iconAnimationGap = (j)this.addSetting(new j("Icon Animation Delay", 45.0F, 4.0F, 120.0F));

   public HudModule() {
      super(
         "HUD",
         "Picks which HUD elements show and how: info bar or separate boxes, coordinates, effects, region map, armor, inventory, lists and more.",
         c.CLIENT
      );
      this.iconAnimationGap.visibleWhen(this.iconAnimations::get);
      this.staffShowAll.visibleWhen(this.staffListHud::get);
      this.brandBox.visibleWhen(this::modeTwo);
      this.positionBox.visibleWhen(this::modeTwo);
      this.coordinates.visibleWhen(() -> !this.modeTwo());
      this.tps.visibleWhen(this::modeTwo);
      this.players.visibleWhen(this::modeTwo);
      this.memory.visibleWhen(this::modeTwo);
      this.speed.visibleWhen(this::modeTwo);
      this.netherCoords.visibleWhen(() -> false);
      this.direction.visibleWhen(this::modeTwo);
      this.server.visibleWhen(this::modeTwo);
      this.clock.visibleWhen(this::modeTwo);
      this.iconAnimations.visibleWhen(() -> !this.modeTwo());
      this.iconAnimationGap.visibleWhen(() -> !this.modeTwo() && this.iconAnimations.get());
      this.armorLayout.visibleWhen(() -> false);
      this.armorPercent.visibleWhen(() -> false);
      this.armorBox.visibleWhen(() -> false);
   }

   public boolean modeTwo() {
      return this.hudMode.is("Mode 2");
   }

   public net.agartha.client.module.a_pkg.b regionMapSetting() {
      return this.regionMap;
   }

   public net.agartha.client.module.a_pkg.b armorHudSetting() {
      return this.armorHud;
   }

   public i armorLayoutSetting() {
      return this.armorLayout;
   }

   public net.agartha.client.module.a_pkg.b armorPercentSetting() {
      return this.armorPercent;
   }

   public net.agartha.client.module.a_pkg.b armorBoxSetting() {
      return this.armorBox;
   }

   public boolean brandBox() {
      return this.brandBox.get();
   }

   public boolean positionBox() {
      return this.positionBox.get();
   }

   public boolean tps() {
      return this.tps.get();
   }

   public boolean players() {
      return this.players.get();
   }

   public boolean memory() {
      return this.memory.get();
   }

   public boolean speed() {
      return this.speed.get();
   }

   public boolean netherCoords() {
      return this.netherCoords.get();
   }

   public boolean direction() {
      return this.direction.get();
   }

   public boolean server() {
      return this.server.get();
   }

   public boolean clock() {
      return this.clock.get();
   }

   public boolean armorHud() {
      return this.armorHud.get();
   }

   public boolean armorVertical() {
      return this.armorLayout.is("Vertical");
   }

   public boolean armorPercent() {
      return this.armorPercent.get();
   }

   public boolean armorBox() {
      return this.armorBox.get();
   }

   public boolean inventoryHud() {
      return this.inventoryHud.get();
   }

   public boolean coordinates() {
      return this.coordinates.get();
   }

   public boolean ping() {
      return this.ping.get();
   }

   public boolean fps() {
      return this.fps.get();
   }

   public boolean potions() {
      return this.potions.get();
   }

   public boolean regionMap() {
      return this.regionMap.get();
   }

   public boolean spotifyHud() {
      return this.spotifyHud.get();
   }

   public boolean keybindsHud() {
      return this.keybindsHud.get();
   }

   public boolean staffListHud() {
      return this.staffListHud.get();
   }

   public float hudOpacity() {
      return this.transparency.get();
   }

   public int staffListRows() {
      return this.staffShowAll.get() ? Integer.MAX_VALUE : 6;
   }

   public boolean iconAnimations() {
      return this.iconAnimations.get();
   }

   public float iconAnimationDelay() {
      return Math.max(2.0F, this.iconAnimationGap.get());
   }
}
