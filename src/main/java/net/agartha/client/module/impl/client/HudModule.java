package net.agartha.client.module.impl.client;

import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class HudModule extends Module {
   public static final String MODE_1 = "Mode 1";
   public static final String MODE_2 = "Mode 2";
   private final EnumSetting hudMode = (EnumSetting)this.addSetting(new EnumSetting("HUD Mode", "Mode 1", new String[]{"Mode 1", "Mode 2"}));
   private final NumberSetting transparency = (NumberSetting)this.addSetting(new NumberSetting("HUD Opacity", 0.5F, 0.15F, 0.85F));
   private final BooleanSetting fps = (BooleanSetting)this.addSetting(new BooleanSetting("FPS", true));
   private final BooleanSetting ping = (BooleanSetting)this.addSetting(new BooleanSetting("Ping", true));
   private final BooleanSetting coordinates = (BooleanSetting)this.addSetting(
      new BooleanSetting("Coordinates", false)
   );
   private final BooleanSetting brandBox = (BooleanSetting)this.addSetting(new BooleanSetting("Agartha+", true));
   private final BooleanSetting positionBox = (BooleanSetting)this.addSetting(new BooleanSetting("Position", true));
   private final BooleanSetting netherCoords = (BooleanSetting)this.addSetting(
      new BooleanSetting("Nether Coordinates", false)
   );
   private final BooleanSetting clock = (BooleanSetting)this.addSetting(new BooleanSetting("Time", true));
   private final BooleanSetting server = (BooleanSetting)this.addSetting(
      new BooleanSetting("Server Address", true)
   );
   private final BooleanSetting tps = (BooleanSetting)this.addSetting(new BooleanSetting("Server TPS", false));
   private final BooleanSetting players = (BooleanSetting)this.addSetting(
      new BooleanSetting("Players Online", false)
   );
   private final BooleanSetting memory = (BooleanSetting)this.addSetting(new BooleanSetting("Memory", false));
   private final BooleanSetting speed = (BooleanSetting)this.addSetting(new BooleanSetting("Speed (BPS)", false));
   private final BooleanSetting direction = (BooleanSetting)this.addSetting(new BooleanSetting("Facing", false));
   private final BooleanSetting regionMap = (BooleanSetting)this.addSetting(new BooleanSetting("Region Map", true));
   private final BooleanSetting keybindsHud = (BooleanSetting)this.addSetting(
      new BooleanSetting("Keybinds List", true)
   );
   private final BooleanSetting potions = (BooleanSetting)this.addSetting(new BooleanSetting("Effects", true));
   private final BooleanSetting armorHud = (BooleanSetting)this.addSetting(new BooleanSetting("Armor HUD", false));
   private final EnumSetting armorLayout = (EnumSetting)this.addSetting(new EnumSetting("Armor Layout", "Horizontal", new String[]{"Horizontal", "Vertical"}));
   private final BooleanSetting armorPercent = (BooleanSetting)this.addSetting(
      new BooleanSetting("Armor Durability", true)
   );
   private final BooleanSetting armorBox = (BooleanSetting)this.addSetting(new BooleanSetting("Armor Box", true));
   private final BooleanSetting inventoryHud = (BooleanSetting)this.addSetting(
      new BooleanSetting("Inventory HUD", false)
   );
   private final BooleanSetting staffListHud = (BooleanSetting)this.addSetting(
      new BooleanSetting("Staff List", false)
   );
   private static final int STAFF_ROWS = 6;
   private final BooleanSetting staffShowAll = (BooleanSetting)this.addSetting(
      new BooleanSetting("Show All Staff", false)
   );
   private final BooleanSetting spotifyHud = (BooleanSetting)this.addSetting(
      new BooleanSetting("Spotify HUD", true)
   );
   private final BooleanSetting iconAnimations = (BooleanSetting)this.addSetting(
      new BooleanSetting("Agartha Icon Animations", true)
   );
   private final NumberSetting iconAnimationGap = (NumberSetting)this.addSetting(new NumberSetting("Icon Animation Delay", 45.0F, 4.0F, 120.0F));

   public HudModule() {
      super(
         "HUD",
         "Picks which HUD elements show and how: info bar or separate boxes, coordinates, effects, region map, armor, inventory, lists and more.",
         ModuleCategory.CLIENT
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

   public BooleanSetting regionMapSetting() {
      return this.regionMap;
   }

   public BooleanSetting armorHudSetting() {
      return this.armorHud;
   }

   public EnumSetting armorLayoutSetting() {
      return this.armorLayout;
   }

   public BooleanSetting armorPercentSetting() {
      return this.armorPercent;
   }

   public BooleanSetting armorBoxSetting() {
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
