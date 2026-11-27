package net.agartha.client.gui;

import imgui.ImDrawList;
import imgui.ImFont;
import imgui.ImGui;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.agartha.client.config.ModuleConfigManager;
import net.agartha.client.gui.AccentColor;
import net.agartha.client.gui.ArmorHud;
import net.agartha.client.gui.AvatarCache;
import net.agartha.client.gui.CanSpriteCache;
import net.agartha.client.gui.DrawUtils;
import net.agartha.client.gui.EffectIconCache;
import net.agartha.client.gui.HudBoxes;
import net.agartha.client.gui.HudLayout;
import net.agartha.client.gui.HudLayoutScreen;
import net.agartha.client.gui.KeybindsHud;
import net.agartha.client.gui.NotificationToast;
import net.agartha.client.gui.Palette;
import net.agartha.client.gui.PlayerAlertBanner;
import net.agartha.client.gui.RegionMapHud;
import net.agartha.client.gui.SpotifyHud;
import net.agartha.client.gui.StaffAlertBanner;
import net.agartha.client.gui.StaffListHud;
import net.agartha.client.gui.Theme;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.client.HudModule;
import net.agartha.client.module.impl.client.MonsterModeModule;
import net.agartha.client.module.impl.client.RegionMapModule;
import net.agartha.client.module.impl.client.SpotifyHudModule;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.render.imgui.ImGuiManager;
import net.agartha.client.util.net.ServerCheck;
import net.agartha.client.util.perf.Profiler;
import net.agartha.client.util.render.TabFooter;
import net.agartha.client.util.render.TabOverlayBounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public final class Hud {
   private static final Hud INSTANCE = new Hud();
   private static final float INTRO_DURATION = 0.55F;
   private static final float INTRO_STAGGER = 0.07F;
   private static final float INTRO_SLIDE = 26.0F;
   private static final float EDGE_SLICE = 0.2F;
   private static final float ROW_SPEED = 13.0F;
   private static final float CAN_FLIP_SECONDS = 0.34F;
   private static final float RADIUS = 6.0F;
   private static final float CUT = 7.0F;
   private static final float BAR_H = 40.0F;
   private static final float CAN_H = 64.0F;
   private static final float BAR_PAD = 16.0F;
   private static final float BLOCK_MIN_W = 74.0F;
   private static final float CAN_SLOT_PAD = 13.0F;
   private static final float EMPTY_SIDE_W = 18.0F;
   private static final float CAN_ASPECT_MONSTER = 0.4F;
   private static final float STAR_SIZE = 62.0F;
   private static final float STAR_SLOT_PAD = 4.0F;
   private static final float MODE_FLIP_SECONDS = 0.45F;
   private static final float MODE_SLIDE_RATE = 9.0F;
   private static final float COORD_ROW_H = 25.0F;
   private static final float COORD_PAD_X = 12.0F;
   private static final float COORD_PAD_Y = 7.0F;
   private static final float COORD_COL_GAP = 12.0F;
   private static final String COORD_LEAD = "XYZ";
   private static final float COORD_LEAD_GAP = 7.0F;
   private static final float COORD_COMMA_GAP = 4.0F;
   private static final int DIM_OVERWORLD = -10299526;
   private static final int DIM_NETHER = -11836417;
   private static final int DIM_END = -1733944;
   private static final float FX_ROW_H = 34.0F;
   private static final float FX_ICON = 23.0F;
   private static final float FX_TEXT_X = 39.0F;
   private static final float FX_METER_W = 12.0F;
   private static final float FX_GAP = 4.0F;
   private static final float FX_MIN_W = 180.0F;
   private static final float LIST_REFERENCE_W = 234.0F;
   private static final float SCALE_STEP = 0.05F;
   private static final float PRESENCE_SECONDS = 0.3F;
   private static final float HANDLE_SIZE = 8.0F;
   private static final float HANDLE_GRAB = 9.0F;
   private static final float REGION_BASE_CELL = 22.0F;
   private static final float CHAT_HIDE_SECONDS = 0.14F;
   private static final float CHAT_HIDE_OVERSHOOT = 12.0F;
   private static final float TAB_HIDE_SECONDS = 0.22F;
   private static final float TAB_MARGIN = 4.0F;
   private static final int EDGE_LEFT = 0;
   private static final int EDGE_RIGHT = 1;
   private static final int EDGE_TOP = 2;
   private static final int EDGE_BOTTOM = 3;
   private final HudLayout layout = HudLayout.getInstance();
   private final HudModule hudModule;
   private final RegionMapModule regionMapModule;
   private final SpotifyHudModule spotifyHudModule;
   private float introTime;
   private boolean hudVisibleLastFrame;
   private String draggingId;
   private float dragOffsetX;
   private float dragOffsetY;
   private String resizingId;
   private float resizeCenterX;
   private float resizeCenterY;
   private float resizeDirX;
   private float resizeDirY;
   private float resizeStartW;
   private float resizeStartH;
   private float resizeStartScale;
   private List<SnapGuides.a> activeSnapGuides = List.of();
   private static final long FPS_REFRESH_NANOS = 500000000L;
   private int shownFps = -1;
   private int shownPing;
   private long fpsSampledAt;
   private Theme canTheme;
   private Theme canPreviousTheme;
   private long canFlipStartNanos;
   private long nextCanAnimNanos;
   private long canAnimStartNanos;
   private int canAnimKind;
   private Boolean shownMonsterMode;
   private boolean previousMonsterMode;
   private long modeFlipStartNanos;
   private static float modeBlend = -1.0F;
   private static long modeBlendNanos;
   private final Map<String, Float> presence = new HashMap<>();
   private final Map<String, Float> chatHide = new HashMap<>();
   private final Map<String, Float> tabHide = new HashMap<>();
   private final Map<String, Integer> tabEdge = new HashMap<>();
   private final Map<String, Integer> potionMaxDuration = new HashMap<>();
   private final Map<String, Float> potionAnim = new HashMap<>();
   private final Map<String, Hud.d> potionRowCache = new HashMap<>();
   private float potionSmoothX = Float.NaN;
   private float potionSmoothY = Float.NaN;
   private float potionSmoothW = Float.NaN;
   private float potionSmoothH = Float.NaN;
   private boolean modeTwo;
   private Boolean shownModeTwo;
   private final boolean[] boxShown = new boolean[12];
   private final List<String> visibleIds = new ArrayList<>();
   private final List<String> obstacleIds = new ArrayList<>();
   private static List<Hud.b> cachedCoordRows;
   private static int cachedCoordX;
   private static int cachedCoordY;
   private static int cachedCoordZ;
   private static Object cachedCoordDim;

   private Hud() {
      this.hudModule = ModuleManager.get(HudModule.class);
      this.regionMapModule = ModuleManager.get(RegionMapModule.class);
      this.spotifyHudModule = ModuleManager.get(SpotifyHudModule.class);
   }

   public static Hud getInstance() {
      return INSTANCE;
   }

   public boolean shouldRenderFrame() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.world == null || foreignMenuOpen()) {
         this.a();
         return false;
      } else if (var1.currentScreen instanceof HudLayoutScreen) {
         return true;
      } else if (this.hudModule != null && this.hudModule.enabled().get()) {
         return true;
      } else if (!this.presence.isEmpty()) {
         return true;
      } else {
         this.a();
         return NotificationToast.shouldRender() || StaffAlertBanner.active() || PlayerAlertBanner.active();
      }
   }

   public static boolean foreignMenuOpen() {
      Screen var0 = MinecraftClient.getInstance().currentScreen;
      if (var0 == null) {
         return false;
      } else {
         String var1 = var0.getClass().getName();
         return !var1.startsWith("net.minecraft.") && !var1.startsWith("net.agartha.") && !var1.startsWith("com.example.client.");
      }
   }

   public void render() {
      long var1 = Profiler.begin();
      long var3 = Profiler.allocatedBytes();
      DrawUtils.beginGlass(this.hudModule.hudOpacity(), true);

      try {
         this.b();
      } finally {
         DrawUtils.endGlass();
         Profiler.record("hud.render", var1);
         Profiler.recordAllocatedBytes("hud.render", var3);
      }
   }

   private void a() {
      this.hudVisibleLastFrame = false;
      this.introTime = 0.0F;
   }

   private void b() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.world != null) {
         boolean var2 = var1.currentScreen instanceof HudLayoutScreen;
         boolean var3 = var1.currentScreen instanceof ChatScreen;
         boolean var4 = var2 || var3;
         float var5 = Math.max(ImGui.getIO().getDeltaTime(), 0.001F);
         float var6 = ImGui.getIO().getDisplaySizeX();
         float var7 = ImGui.getIO().getDisplaySizeY();
         boolean var8 = this.hudModule != null && (var2 || this.hudModule.enabled().get());
         boolean var9 = this.hudModule != null && this.hudModule.modeTwo();
         if (this.shownModeTwo == null || var2) {
            this.shownModeTwo = var9;
         } else if (var9 != this.shownModeTwo) {
            if (this.presence.isEmpty()) {
               this.shownModeTwo = var9;
               this.a();
            } else {
               var8 = false;
            }
         }

         this.layout.setModeTwo(this.shownModeTwo);
         if (!var8 && this.presence.isEmpty()) {
            this.a();
            if (NotificationToast.shouldRender() || StaffAlertBanner.active() || PlayerAlertBanner.active()) {
               a("##agartha_notifications_only", var6, var7, false);
               ImDrawList var42 = ImGui.getWindowDrawList();
               NotificationToast.render(var42, var6, var7, var5);
               StaffAlertBanner.render(var42, var6, var7);
               PlayerAlertBanner.render(var42, var6, var7);
               ImGui.end();
            }
         } else {
            boolean var10 = var8 && this.presence.isEmpty();
            this.modeTwo = this.shownModeTwo;
            boolean var11 = var2 || this.hudModule.ping();
            boolean var12 = var2 || this.hudModule.fps();
            boolean var13 = this.a("info_bar", var8 && !this.modeTwo, var5, var10) > 0.0F;
            boolean var14 = this.a("coordinates", var8 && !this.modeTwo && (var2 || this.hudModule.coordinates()), var5, var10) > 0.0F;
            boolean var15 = false;

            for (int var16 = 0; var16 < 12; var16++) {
               this.boxShown[var16] = this.a(HudBoxes.IDS[var16], var8 && this.modeTwo && HudBoxes.enabled(this.hudModule, var16), var5, var10) > 0.0F;
               var15 |= this.boxShown[var16];
            }

            boolean var43 = this.a("armor_hud", var8 && (var2 || this.hudModule.armorHud()), var5, var10) > 0.0F;
            boolean var17 = this.a("inventory_hud", var8 && (var2 || this.hudModule.inventoryHud()), var5, var10) > 0.0F;
            boolean var18 = this.a("potion_tracker", var8 && (var2 || this.hudModule.potions()), var5, var10) > 0.0F;
            boolean var19 = this.regionMapModule != null && this.a("region_map", var8 && (var2 || this.hudModule.regionMap()), var5, var10) > 0.0F;
            boolean var20 = this.spotifyHudModule != null
               && this.a("spotify_hud", var8 && (var2 || this.hudModule.spotifyHud()) && SpotifyHud.shouldDraw(this.spotifyHudModule, var2), var5, var10) > 0.0F;
            this.a(var2, var5);
            this.b(var1, var3 && !var2, var5, var6, var7);
            this.a(var1, var2, var5, var6, var7);
            Hud.c var21 = this.a(var1, var12, var11, var5);
            HudLayout.c var22 = this.layout.element("info_bar");
            a(var21, scaleOf(var22));
            this.layout.updateBounds(var22.id, var21.width, var21.height, var6, var7);
            this.visibleIds.clear();
            if (var13) {
               this.visibleIds.add("info_bar");
            }

            if (var15) {
               HudBoxes.prepare(Math.max(0, this.shownFps), this.shownPing, var5);

               for (int var23 = 0; var23 < 12; var23++) {
                  if (this.boxShown[var23]) {
                     HudLayout.c var24 = this.layout.element(HudBoxes.IDS[var23]);
                     float var25 = scaleOf(var24);
                     this.layout.updateBounds(var24.id, HudBoxes.width(var23, var25), HudBoxes.height(var25), var6, var7);
                     this.visibleIds.add(var24.id);
                  }
               }
            }

            HudLayout.c var44 = this.layout.element("armor_hud");
            if (var43) {
               ArmorHud.a var45 = ArmorHud.measure(this.hudModule, scaleOf(var44));
               this.layout.updateBounds(var44.id, var45.width(), var45.height(), var6, var7);
            }

            HudLayout.c var46 = this.layout.element("inventory_hud");
            if (var17) {
               InventoryHud.a var47 = InventoryHud.measure(scaleOf(var46));
               this.layout.updateBounds(var46.id, var47.width(), var47.height(), var6, var7);
            }

            float var48 = 0.0F;
            if (var19) {
               RegionMapHud.b var26 = RegionMapHud.measureSize(this.regionMapModule);
               var48 = var26.width();
               this.layout.updateBounds("region_map", var26.width(), var26.height(), var6, var7);
            }

            List var49 = var14 ? a(var1) : List.of();
            HudLayout.c var27 = this.layout.element("coordinates");
            Hud.a var28 = null;
            if (var14) {
               var28 = a(var49, scaleOf(var27), this.a(var27) ? var48 : 0.0F);
               this.layout.updateBounds(var27.id, var28.width(), var28.height(), var6, var7);
            }

            List var29 = var18 ? this.a(var1, var5, var2) : List.of();
            HudLayout.c var30 = this.layout.element("potion_tracker");
            if (var18) {
               float[] var31 = a(var29, scaleOf(var30));
               this.a(var30, var31[0], var31[1], var6, var7, var5, var2);
            }

            if (var20) {
               SpotifyHud.a var32 = SpotifyHud.measureSize(this.spotifyHudModule, var2);
               this.layout.updateBounds("spotify_hud", var32.width(), var32.height(), var6, var7);
            }

            float var50 = 234.0F;
            boolean var33 = this.a("keybinds", var8 && (var2 || this.hudModule.keybindsHud()) && KeybindsHud.shouldDraw(var2), var5, var10) > 0.0F;
            HudLayout.c var34 = this.layout.element("keybinds");
            if (var33) {
               KeybindsHud.a var35 = KeybindsHud.measureSize(var2, scaleOf(var34), 0.0F, true);
               this.layout.updateBounds(var34.id, var35.width(), var35.height(), var6, var7);
            }

            boolean var51 = this.a("staff_list", var8 && (var2 || this.hudModule.staffListHud() && ServerCheck.connected()), var5, var10) > 0.0F;
            HudLayout.c var36 = this.layout.element("staff_list");
            if (var51) {
               AvatarCache.getInstance().processPendingUploads();
               StaffListHud.a var37 = StaffListHud.measureSize(this.hudModule, scaleOf(var36), var50);
               this.layout.updateBounds(var36.id, var37.width(), var37.height(), var6, var7);
            }

            if (var19) {
               this.visibleIds.add("region_map");
            }

            if (var33) {
               this.visibleIds.add("keybinds");
            }

            if (var51) {
               this.visibleIds.add("staff_list");
            }

            if (var18) {
               this.visibleIds.add("potion_tracker");
            }

            if (var20) {
               this.visibleIds.add("spotify_hud");
            }

            if (var14) {
               this.visibleIds.add("coordinates");
            }

            if (var43) {
               this.visibleIds.add("armor_hud");
            }

            if (var17) {
               this.visibleIds.add("inventory_hud");
            }

            this.obstacleIds.clear();

            for (int var52 = 0; var52 < this.visibleIds.size(); var52++) {
               String var38 = this.visibleIds.get(var52);
               if (this.layout.element(var38).userMoved && !"region_map".equals(var38)) {
                  this.obstacleIds.add(var38);
               }
            }

            float var53 = this.layout.flowBoxes(HudBoxes.IDS, this.boxShown, 7, this.obstacleIds, var6, var7);
            if (this.modeTwo) {
               this.layout.keepRegionUnderBoxes(var53);
            }

            this.layout.dockRegionAt(var53, var6, var7);
            this.layout.dockToRegionMap(var19, var33, var6, var7);
            this.layout.separate(this.visibleIds, var6, var7);
            if (var4) {
               this.a(var6, var7, var2);
            } else {
               this.e();
            }

            a("##hud_overlay", var6, var7, var4);
            ImDrawList var54 = ImGui.getWindowDrawList();
            if (var13 && (var22.visible || var2)) {
               this.a(var54, var22, var21, this.a("info_bar", var2));
            }

            for (int var39 = 0; var39 < 12; var39++) {
               if (this.boxShown[var39]) {
                  String var40 = HudBoxes.IDS[var39];
                  Hud.e var41 = this.a(var40, var2);
                  HudBoxes.render(var54, var39, this.layout.element(var40), var41.dx(), var41.dy(), var41.alpha());
               }
            }

            if (var19) {
               Hud.e var55 = this.a("region_map", var2);
               RegionMapHud.render(var54, this.regionMapModule, this.layout.element("region_map"), var55.dx(), var55.dy(), var55.alpha());
            }

            if (var14 && (var27.visible || var2)) {
               a(var54, var27, var49, var28, this.a("coordinates", var2));
            }

            if (var18 && (var30.visible || var2)) {
               a(var54, this.layout.element("potion_tracker"), var29, this.a("potion_tracker", var2));
            }

            if (var20) {
               Hud.e var56 = this.a("spotify_hud", var2);
               SpotifyHud.render(var54, this.spotifyHudModule, this.layout.element("spotify_hud"), var2, var56.dx(), var56.dy(), var56.alpha());
            }

            if (var33) {
               Hud.e var57 = this.a("keybinds", var2);
               KeybindsHud.render(var54, var34, var2, var57.dx(), var57.dy(), var57.alpha(), true);
            }

            if (var51) {
               Hud.e var58 = this.a("staff_list", var2);
               StaffListHud.render(var54, this.hudModule, var36, var58.dx(), var58.dy(), var58.alpha());
            }

            if (var43) {
               Hud.e var59 = this.a("armor_hud", var2);
               ArmorHud.render(var54, this.hudModule, var44, var59.dx(), var59.dy(), var59.alpha());
            }

            if (var17) {
               Hud.e var60 = this.a("inventory_hud", var2);
               InventoryHud.render(var54, var46, var60.dx(), var60.dy(), var60.alpha());
            }

            NotificationToast.render(var54, var6, var7, var5);
            StaffAlertBanner.render(var54, var6, var7);
            PlayerAlertBanner.render(var54, var6, var7);
            if (var2) {
               this.a(var54, var6, var7);
            }

            ImGui.end();
         }
      }
   }

   private static void a(String string, float f, float g, boolean bl) {
      ImGui.setNextWindowPos(0.0F, 0.0F, 1);
      ImGui.setNextWindowSize(f, g);
      int var4 = 205215;
      if (!bl) {
         var4 |= 197120;
      }

      ImGui.pushStyleVar(4, 0.0F);
      ImGui.begin(string, var4);
      ImGui.popStyleVar();
   }

   private void a(boolean bl, float f) {
      float var3 = 0.9F;
      if (bl) {
         this.introTime = var3;
         this.hudVisibleLastFrame = true;
      } else if (!this.hudVisibleLastFrame) {
         this.introTime = 0.0F;
         this.hudVisibleLastFrame = true;
      } else {
         this.introTime = Math.min(this.introTime + Math.min(f, 0.05F), var3);
      }
   }

   private Hud.e a(String string, boolean bl) {
      return bl ? Hud.e.IDENTITY : this.a(string, this.c(string, this.b(string, this.a(string))));
   }

   private Hud.e a(String string, Hud.e arg) {
      Float var3 = this.tabHide.get(string);
      if (var3 != null && !(var3 <= 0.0F)) {
         float var4 = DrawUtils.smoothstep(var3);
         HudLayout.c var5 = this.layout.element(string);
         float var6 = ImGui.getIO().getDisplaySizeX();
         float var7 = ImGui.getIO().getDisplaySizeY();
         float var8 = 0.0F;
         float var9 = 0.0F;
         switch (this.tabEdge.getOrDefault(string, 2)) {
            case 0:
               var8 = -(var5.x + var5.width + 12.0F);
               break;
            case 1:
               var8 = var6 - var5.x + 12.0F;
               break;
            case 2:
            default:
               var9 = -(var5.y + var5.height + 12.0F);
               break;
            case 3:
               var9 = var7 - var5.y + 12.0F;
         }

         return new Hud.e(arg.dx() + var8 * var4, arg.dy() + var9 * var4, arg.alpha() * (1.0F - 0.6F * var4));
      } else {
         return arg;
      }
   }

   private void a(MinecraftClient minecraftClient, boolean bl, float f, float g, float h) {
      int[] var6 = bl ? null : TabOverlayBounds.open();
      float var7 = 0.0F;
      float var8 = 0.0F;
      float var9 = 0.0F;
      float var10 = 0.0F;
      if (var6 != null) {
         float var11 = g / Math.max(1.0F, (float)minecraftClient.getWindow().getScaledWidth());
         var7 = var6[0] * var11 - 4.0F;
         var8 = var6[1] * var11 - 4.0F;
         var9 = var6[2] * var11 + 4.0F;
         var10 = var6[3] * var11 + 4.0F;
      }

      float var16 = f / 0.22F;

      for (HudLayout.c var13 : this.layout.orderedElements()) {
         boolean var14 = var6 != null && this.d(var13.id) && var13.x < var9 && var13.x + var13.width > var7 && var13.y < var10 && var13.y + var13.height > var8;
         float var15 = this.tabHide.getOrDefault(var13.id, 0.0F);
         if (var14 && var15 <= 0.0F) {
            this.tabEdge.put(var13.id, a(var13, g, h));
         }

         var15 = var14 ? Math.min(1.0F, var15 + var16) : Math.max(0.0F, var15 - var16);
         if (var15 <= 0.0F) {
            this.tabHide.remove(var13.id);
         } else {
            this.tabHide.put(var13.id, var15);
         }
      }
   }

   private static int a(HudLayout.c arg, float f, float g) {
      float var3 = arg.x + arg.width * 0.5F;
      float var4 = arg.y + arg.height * 0.5F;
      float var6 = f - var3;
      float var8 = g - var4;
      float var9 = Math.min(Math.min(var3, var6), Math.min(var4, var8));
      if (var9 == var4) {
         return 2;
      } else if (var9 == var3) {
         return 0;
      } else {
         return var9 == var6 ? 1 : 3;
      }
   }

   private float a(String string, boolean bl, float f, boolean bl2) {
      float var5 = this.presence.getOrDefault(string, 0.0F);
      float var6 = f / 0.3F;
      var5 = bl ? (bl2 ? 1.0F : Math.min(1.0F, var5 + var6)) : Math.max(0.0F, var5 - var6);
      if (var5 <= 0.0F) {
         this.presence.remove(string);
      } else {
         this.presence.put(string, var5);
      }

      return var5;
   }

   private Hud.e b(String string, Hud.e arg) {
      float var3 = this.presence.getOrDefault(string, 0.0F);
      if (var3 >= 1.0F) {
         return arg;
      } else {
         float var4 = DrawUtils.smoothstep(var3);
         Hud.e var5 = this.a(string, 26.0F * (1.0F - var4), 1.0F);
         return new Hud.e(arg.dx() + var5.dx(), arg.dy() + var5.dy(), arg.alpha() * var4);
      }
   }

   private Hud.e c(String string, Hud.e arg) {
      Float var3 = this.chatHide.get(string);
      if (var3 != null && !(var3 <= 0.0F)) {
         float var4 = DrawUtils.smoothstep(var3);
         HudLayout.c var5 = this.layout.element(string);
         float var6 = ImGui.getIO().getDisplaySizeY() - var5.y + 12.0F;
         return new Hud.e(arg.dx(), arg.dy() + var6 * var4, arg.alpha() * (1.0F - var4));
      } else {
         return arg;
      }
   }

   private void b(MinecraftClient minecraftClient, boolean bl, float f, float g, float h) {
      float var6 = 0.0F;
      float var7 = h;
      if (bl) {
         float var8 = g / Math.max(1.0F, (float)minecraftClient.getWindow().getScaledWidth());
         double var9 = (Double)minecraftClient.options.getChatScale().getValue();
         float var11 = (float)(ChatHud.getWidth((Double)minecraftClient.options.getChatWidth().getValue()) * var9) + 12.0F;
         float var12 = (float)(ChatHud.getHeight((Double)minecraftClient.options.getChatHeightFocused().getValue()) * var9) + 48.0F;
         var6 = Math.max(g * 0.25F, var11 * var8);
         var7 = Math.max(h * 0.5F, h - var12 * var8);
      }

      float var15 = f / 0.14F;

      for (HudLayout.c var10 : this.layout.orderedElements()) {
         float var17 = var10.x + var10.width * 0.5F;
         float var18 = var10.y + var10.height * 0.5F;
         boolean var13 = bl && var17 < var6 && var18 > var7;
         float var14 = this.chatHide.getOrDefault(var10.id, 0.0F);
         var14 = var13 ? Math.min(1.0F, var14 + var15) : Math.max(0.0F, var14 - var15);
         if (var14 <= 0.0F) {
            this.chatHide.remove(var10.id);
         } else {
            this.chatHide.put(var10.id, var14);
         }
      }
   }

   private boolean a(HudLayout.c arg) {
      HudLayout.c var2 = this.layout.element("region_map");
      return arg.positioned && var2.positioned && Math.abs(arg.x - var2.x) <= 12.0F && Math.abs(arg.y - (var2.y + var2.height)) <= 12.0F;
   }

   private Hud.e a(String string) {
      float var2 = DrawUtils.clamp01((this.introTime - b(string)) / 0.55F);
      if (var2 >= 1.0F) {
         return Hud.e.IDENTITY;
      } else {
         float var3 = DrawUtils.easeOutCubic(var2);
         return this.a(string, 26.0F * (1.0F - var3), var3);
      }
   }

   private Hud.e a(String string, float f, float g) {
      float var4 = ImGui.getIO().getDisplaySizeX();
      float var5 = ImGui.getIO().getDisplaySizeY();
      HudLayout.c var6 = this.layout.element(string);
      float var7 = var6.x + var6.width * 0.5F;
      float var8 = var6.y + var6.height * 0.5F;
      if (var7 < var4 * 0.2F) {
         return new Hud.e(-f, 0.0F, g);
      } else if (var7 > var4 * 0.8F) {
         return new Hud.e(f, 0.0F, g);
      } else {
         return var8 < var5 * 0.5F ? new Hud.e(0.0F, -f, g) : new Hud.e(0.0F, f, g);
      }
   }

   private static float b(String string) {
      return switch (string) {
         case "region_map" -> 0.07F;
         case "coordinates" -> 0.14F;
         case "potion_tracker" -> 0.21000001F;
         case "spotify_hud" -> 0.28F;
         default -> 0.0F;
      };
   }

   private Hud.c a(MinecraftClient minecraftClient, boolean bl, boolean bl2, float f) {
      Hud.c var5 = new Hud.c();
      long var6 = System.nanoTime();
      if (bl || bl2) {
         if (this.shownFps < 0 || var6 - this.fpsSampledAt >= 500000000L) {
            if (bl) {
               float var8 = ImGui.getIO().getFramerate();
               this.shownFps = Math.max(0, Math.min(9999, Math.round(var8 > 0.0F ? var8 : minecraftClient.getCurrentFps())));
            }

            if (bl2) {
               this.shownPing = c(minecraftClient);
            }

            this.fpsSampledAt = var6;
         }

         if (bl) {
            var5.fps = String.valueOf(Math.max(0, this.shownFps));
         }

         if (bl2) {
            var5.ping = String.valueOf(this.shownPing);
         }
      }

      return var5;
   }

   private static void a(Hud.c arg, float f) {
      ImFont var2 = ImGuiManager.getInstance().hudValueFont();
      ImFont var3 = ImGuiManager.getInstance().hudLabelFont();
      float var4 = 26.0F * f;
      float var5 = 11.0F * f;
      float var6 = Math.max(DrawUtils.trackedWidth(var3, var5, "FPS", 1.2F * f), DrawUtils.trackedWidth(var3, var5, "MS", 1.2F * f));
      float var7 = Math.max(74.0F * f, a(var2, var4, 4) + 5.0F * f + var6 + 32.0F * f);
      boolean var8 = arg.fps != null || arg.ping != null;
      arg.leftW = var8 ? var7 : 18.0F * f;
      arg.rightW = var8 ? var7 : 18.0F * f;
      float var9 = c();
      arg.canW = a(62.0F * f, 60.16F * f * 0.4F, var9);
      arg.slotW = arg.canW + a(4.0F, 13.0F, var9) * 2.0F * f;
      arg.width = arg.leftW + arg.slotW + arg.rightW;
      arg.height = 64.0F * f;
   }

   private void a(ImDrawList imDrawList, HudLayout.c arg, Hud.c arg2, Hud.e arg3) {
      float var5 = arg3.alpha();
      if (!(var5 <= 0.002F)) {
         float var6 = scaleOf(arg);
         float var7 = arg.x + arg3.dx();
         float var8 = arg.y + arg3.dy();
         float var9 = arg.width;
         float var10 = 40.0F * var6;
         float var11 = var8 + (arg.height - var10) * 0.5F;
         float var12 = 6.0F * var6;
         float var13 = 7.0F * var6;
         DrawUtils.card(imDrawList, var7, var11, var9, var10, var12, var13, var5, true);
         DrawUtils.accentRim(imDrawList, var7, var11, var9, var12, var13, 2.0F * var6, var5);
         ImFont var14 = ImGuiManager.getInstance().hudValueFont();
         ImFont var15 = ImGuiManager.getInstance().hudLabelFont();
         float var16 = var11 + var10 * 0.5F;
         if (arg2.fps != null) {
            a(imDrawList, var14, var15, var6, var7, arg2.leftW, var16, arg2.fps, "FPS", var5);
         }

         if (arg2.ping != null) {
            a(imDrawList, var14, var15, var6, var7 + arg2.leftW + arg2.slotW, arg2.rightW, var16, arg2.ping, "MS", var5);
         }

         float var17 = var7 + arg2.leftW;
         int var18 = DrawUtils.alpha(620756991, var5);
         if (arg2.fps != null || arg2.ping != null) {
            float var19 = var17 + arg2.slotW;
            imDrawList.addLine(var17, var11 + var10 * 0.25F, var17, var11 + var10 * 0.75F, var18, 1.0F);
            imDrawList.addLine(var19, var11 + var10 * 0.25F, var19, var11 + var10 * 0.75F, var18, 1.0F);
         }

         this.a(imDrawList, var17 + arg2.slotW * 0.5F, var8, arg2.canW, arg.height, var6, var5);
      }
   }

   private static void a(ImDrawList imDrawList, ImFont imFont, ImFont imFont2, float f, float g, float h, float i, String string, String string2, float j) {
      float var10 = 26.0F * f;
      float var11 = 11.0F * f;
      float var12 = 5.0F * f;
      float var13 = DrawUtils.textWidth(imFont, var10, string);
      float var14 = DrawUtils.trackedWidth(imFont2, var11, string2, 1.2F * f);
      float var15 = g + (h - (var13 + var12 + var14)) * 0.5F;
      float var16 = i - var10 * 0.52F;
      DrawUtils.text(imDrawList, imFont, var10, var15, var16, DrawUtils.alpha(-526345, j), string);
      float var17 = i - var11 * 0.52F;
      DrawUtils.tracked(imDrawList, imFont2, var11, var15 + var13 + var12, var17, DrawUtils.accent(j * 0.9F), string2, 1.2F * f);
   }

   private static float a(ImFont imFont, float f, int i) {
      float var3 = 0.0F;

      for (char var4 = '0'; var4 <= '9'; var4++) {
         var3 = Math.max(var3, DrawUtils.textWidth(imFont, f, String.valueOf(var4)));
      }

      return var3 * i;
   }

   private static float c() {
      float var0 = MonsterModeModule.isActive() ? 1.0F : 0.0F;
      long var1 = System.nanoTime();
      if (modeBlend < 0.0F) {
         modeBlend = var0;
      } else {
         float var3 = Math.min((float)(var1 - modeBlendNanos) / 1.0E9F, 0.1F);
         modeBlend = modeBlend + (var0 - modeBlend) * (1.0F - (float)Math.exp(-9.0F * var3));
         if (Math.abs(var0 - modeBlend) < 0.001F) {
            modeBlend = var0;
         }
      }

      modeBlendNanos = var1;
      return modeBlend;
   }

   private static float a(float f, float g, float h) {
      return f + (g - f) * h;
   }

   private void a(ImDrawList imDrawList, float f, float g, float h, float i, float j, float k) {
      Theme var8 = AccentColor.current();
      long var9 = System.nanoTime();
      if (this.canTheme == null) {
         this.canTheme = var8;
         this.canPreviousTheme = var8;
      } else if (var8 != this.canTheme) {
         this.canPreviousTheme = this.canTheme;
         this.canTheme = var8;
         this.canFlipStartNanos = var9;
      }

      float var11 = this.canFlipStartNanos == 0L ? 1.0F : DrawUtils.clamp01((float)(var9 - this.canFlipStartNanos) / 3.4E8F);
      Theme var12 = var11 < 0.5F ? this.canPreviousTheme : this.canTheme;
      float var13 = var11 >= 1.0F ? 1.0F : Math.abs((float)Math.cos(var11 * Math.PI));
      boolean var14 = MonsterModeModule.isActive();
      if (this.shownMonsterMode == null) {
         this.shownMonsterMode = var14;
         this.previousMonsterMode = var14;
      } else if (var14 != this.shownMonsterMode) {
         this.previousMonsterMode = this.shownMonsterMode;
         this.shownMonsterMode = var14;
         this.modeFlipStartNanos = var9;
      }

      float var15 = this.modeFlipStartNanos == 0L ? 1.0F : DrawUtils.clamp01((float)(var9 - this.modeFlipStartNanos) / 4.5E8F);
      boolean var16 = var15 < 0.5F ? this.previousMonsterMode : var14;
      if (var15 < 1.0F) {
         var13 *= Math.abs((float)Math.cos(var15 * Math.PI));
      }

      double var17 = var9 / 1.0E9;
      float var19 = var16 ? (float)Math.sin(var17 * Math.PI * 2.0 / 3.4) * 1.3F * j : 0.0F;
      float var20 = var16 ? 64.0F * j * 0.94F : 62.0F * j;
      float var21 = var16 ? var20 * 0.4F : var20;
      float var22 = g + i * 0.5F;
      float var23 = 1.0F;
      float var24 = 1.0F;
      float var25 = 0.0F;
      float var26 = 0.0F;
      if (var16 && this.hudModule != null && this.hudModule.iconAnimations()) {
         if (this.nextCanAnimNanos == 0L) {
            this.nextCanAnimNanos = var9 + (long)(this.hudModule.iconAnimationDelay() * 1.0E9);
         } else if (this.canAnimStartNanos == 0L && var9 >= this.nextCanAnimNanos) {
            this.canAnimStartNanos = var9;
            this.canAnimKind = ThreadLocalRandom.current().nextInt(3);
            float var27 = this.hudModule.iconAnimationDelay();
            this.nextCanAnimNanos = var9 + (long)(var27 * (0.7F + ThreadLocalRandom.current().nextFloat() * 0.6F) * 1.0E9);
         }

         if (this.canAnimStartNanos != 0L) {
            float var42 = switch (this.canAnimKind) {
               case 1 -> 1.15F;
               case 2 -> 0.55F;
               default -> 0.95F;
            };
            float var28 = (float)(var9 - this.canAnimStartNanos) / (var42 * 1.0E9F);
            if (var28 >= 1.0F) {
               this.canAnimStartNanos = 0L;
            } else {
               switch (this.canAnimKind) {
                  case 0:
                     var23 = Math.abs((float)Math.cos(var28 * Math.PI * 2.0));
                     break;
                  case 1:
                     var26 = var16
                        ? (float)(Math.sin(var28 * Math.PI * 3.0) * Math.toRadians(15.0) * Math.pow(1.0 - var28, 1.2))
                        : (float)(Math.sin(var28 * Math.PI * 2.0) * Math.toRadians(20.0) * Math.sin(Math.PI * Math.min(1.0, var28 * 1.05)));
                     break;
                  default:
                     float var29 = (float)Math.sin(var28 * Math.PI);
                     var25 = -var29 * 7.0F * j;
                     var24 = 1.0F - var29 * 0.08F;
               }
            }
         }
      } else {
         this.canAnimStartNanos = 0L;
         this.nextCanAnimNanos = 0L;
      }

      var20 *= var24;
      float var43 = var22 - var20 * 0.5F + var19 + var25;
      int var44 = AccentColor.accent();
      float var45 = k * (var15 < 1.0F ? Math.abs((float)Math.cos(var15 * Math.PI)) : 1.0F);
      if (var16) {
         for (int var30 = 4; var30 >= 1; var30--) {
            float var31 = var21 * (0.45F + var30 * 0.26F);
            imDrawList.addCircleFilled(f, var22, var31, DrawUtils.withAlpha(var44, Math.round(9.0F * var45)), 32);
         }

         float var46 = var21 * 0.44F;
         float var48 = var22 + var20 * 0.5F - 1.5F * j;
         imDrawList.addRectFilled(
            f - var46, var48, f + var46, var48 + 3.0F * j, DrawUtils.withAlpha(-16777216, Math.round(80.0F * var45)), 2.0F * j
         );
      }

      CanSpriteCache var47 = var16 ? CanSpriteCache.legacy() : CanSpriteCache.getInstance();
      float var49 = var21 * 0.5F * Math.max(0.04F, var13 * var23);
      if (var47.isReady(var12)) {
         int var32 = DrawUtils.alpha(-1, k);
         if (var26 == 0.0F) {
            imDrawList.addImage(var47.textureId(var12), f - var49, var43, f + var49, var43 + var20, 0.0F, 0.0F, 1.0F, 1.0F, var32);
         } else {
            float var33 = var43 + var20 * (var16 ? 0.92F : 0.5F);
            float var34 = (float)Math.cos(var26);
            float var35 = (float)Math.sin(var26);
            float[] var36 = new float[]{-var49, var49, var49, -var49};
            float[] var37 = new float[]{var43 - var33, var43 - var33, var43 + var20 - var33, var43 + var20 - var33};
            float[] var38 = new float[4];
            float[] var39 = new float[4];

            for (int var40 = 0; var40 < 4; var40++) {
               var38[var40] = f + var36[var40] * var34 - var37[var40] * var35;
               var39[var40] = var33 + var36[var40] * var35 + var37[var40] * var34;
            }

            imDrawList.addImageQuad(
               var47.textureId(var12),
               var38[0],
               var39[0],
               var38[1],
               var39[1],
               var38[2],
               var39[2],
               var38[3],
               var39[3],
               0.0F,
               0.0F,
               1.0F,
               0.0F,
               1.0F,
               1.0F,
               0.0F,
               1.0F,
               var32
            );
         }
      } else {
         float var50 = var20 * 0.5F;
         DrawUtils.claw(imDrawList, f - var50 * 0.4F, var22 - var50 * 0.5F, var50, DrawUtils.accent(k));
      }
   }

   private static List<Hud.b> a(MinecraftClient minecraftClient) {
      if (minecraftClient.player != null && minecraftClient.world != null) {
         int var1 = (int)Math.floor(minecraftClient.player.getX());
         int var2 = (int)Math.floor(minecraftClient.player.getY());
         int var3 = (int)Math.floor(minecraftClient.player.getZ());
         RegistryKey var4 = minecraftClient.world.getRegistryKey();
         if (cachedCoordRows != null && var1 == cachedCoordX && var2 == cachedCoordY && var3 == cachedCoordZ && var4 == cachedCoordDim) {
            return cachedCoordRows;
         } else {
            cachedCoordX = var1;
            cachedCoordY = var2;
            cachedCoordZ = var3;
            cachedCoordDim = var4;
            cachedCoordRows = b(minecraftClient);
            return cachedCoordRows;
         }
      } else {
         cachedCoordRows = null;
         return b(minecraftClient);
      }
   }

   private static List<Hud.b> b(MinecraftClient minecraftClient) {
      if (minecraftClient.player != null && minecraftClient.world != null) {
         int var1 = (int)Math.floor(minecraftClient.player.getX());
         int var2 = (int)Math.floor(minecraftClient.player.getY());
         int var3 = (int)Math.floor(minecraftClient.player.getZ());
         if (minecraftClient.world.getRegistryKey() == World.END) {
            return List.of(new Hud.b("E", -1733944, a(var1), a(var2), a(var3), true));
         } else {
            return minecraftClient.world.getRegistryKey() == World.NETHER
               ? List.of(new Hud.b("O", -10299526, a(var1 * 8), a(var2), a(var3 * 8), false), new Hud.b("N", -11836417, a(var1), a(var2), a(var3), true))
               : List.of(
                  new Hud.b("O", -10299526, a(var1), a(var2), a(var3), true),
                  new Hud.b("N", -11836417, a(Math.floorDiv(var1, 8)), a(var2), a(Math.floorDiv(var3, 8)), false)
               );
         }
      } else {
         return List.of(new Hud.b("O", -10299526, "0", "0", "0", true), new Hud.b("N", -11836417, "0", "0", "0", false));
      }
   }

   private static String a(int i) {
      return String.valueOf(i);
   }

   private static Hud.a a(List<Hud.b> list, float f, float g) {
      ImFont var3 = ImGuiManager.getInstance().hudValueFont();
      ImFont var4 = ImGuiManager.getInstance().hudLabelFont();
      float[] var5 = new float[]{17.0F, 16.0F, 15.0F, 14.0F, 13.0F, 12.0F};
      Hud.a var6 = null;

      for (float var10 : var5) {
         Hud.a var11 = a(list, f, var10, var3, var4);
         var6 = var11;
         if (g <= 1.0F || var11.width() <= g) {
            break;
         }
      }

      if (var6 == null) {
         return null;
      } else {
         float var12 = Math.max(var6.width(), Math.min(g, 400.0F * f));
         return new Hud.a(var12, var6.height(), var6.leadW(), var6.commaW(), var6.numbers(), var6.valueSize(), var6.rowH());
      }
   }

   private static Hud.a a(List<Hud.b> list, float f, float g, ImFont imFont, ImFont imFont2) {
      float var5 = g * f;
      float var6 = 9.0F * f;
      float var7 = DrawUtils.textWidth(imFont, var5, "XYZ") + 7.0F * f;
      float var8 = DrawUtils.textWidth(imFont, var5, ",") + 4.0F * f;
      float var9 = 0.0F;

      for (Hud.b var11 : list) {
         var9 = Math.max(
            var9,
            DrawUtils.textWidth(imFont, var5, var11.x())
               + DrawUtils.textWidth(imFont, var5, var11.y())
               + DrawUtils.textWidth(imFont, var5, var11.z())
               + var8 * 2.0F
         );
      }

      float var16 = DrawUtils.textWidth(imFont, var5, "-");
      var9 = Math.max(var9, var16 * 2.0F + a(imFont, var5, 6) * 2.0F + a(imFont, var5, 3) + var8 * 2.0F);
      float var17 = 14.0F * f;
      float var12 = var6 * 2.0F + var17 + var7 + var9;
      float var13 = Math.max(18.0F * f, var5 + 5.0F * f);
      float var14 = 7.0F * f * 2.0F + list.size() * var13;
      return new Hud.a(var12, var14, var7, var8, var9, var5, var13);
   }

   private static void a(ImDrawList imDrawList, HudLayout.c arg, List<Hud.b> list, Hud.a arg2, Hud.e arg3) {
      float var5 = arg3.alpha();
      if (!(var5 <= 0.002F) && !list.isEmpty() && arg2 != null) {
         float var6 = scaleOf(arg);
         float var7 = arg.x + arg3.dx();
         float var8 = arg.y + arg3.dy();
         float var9 = 6.0F * var6;
         float var10 = 7.0F * var6;
         DrawUtils.card(imDrawList, var7, var8, arg.width, arg.height, var9, var10, var5, true);
         DrawUtils.accentRim(imDrawList, var7, var8, arg.width, var9, var10, 2.0F * var6, var5);
         ImFont var11 = ImGuiManager.getInstance().hudValueFont();
         float var12 = arg2.valueSize();
         float var13 = arg2.rowH();
         float var14 = 9.0F * var6;
         float var15 = arg2.commaW();
         float var16 = var8 + 7.0F * var6;

         for (int var17 = 0; var17 < list.size(); var17++) {
            Hud.b var18 = (Hud.b)list.get(var17);
            float var19 = var5 * (var18.active() ? 1.0F : 0.55F);
            float var20 = var16 + var13 * 0.5F;
            float var21 = var7 + var14;
            if (var18.active()) {
               imDrawList.addCircleFilled(var21 + 3.0F * var6, var20, 5.5F * var6, DrawUtils.withAlpha(var18.dot(), Math.round(45.0F * var5)), 20);
            }

            imDrawList.addCircleFilled(var21 + 3.0F * var6, var20, 2.9F * var6, DrawUtils.alpha(var18.dot(), var19), 16);
            var21 += 14.0F * var6;
            float var22 = DrawUtils.midTop(var11, var12, var20);
            DrawUtils.text(imDrawList, var11, var12, var21, var22, DrawUtils.accent(var19), "XYZ");
            var21 += arg2.leadW();
            int var23 = DrawUtils.alpha(-526345, var19);
            int var24 = DrawUtils.alpha(-6119783, var19);
            var21 = a(imDrawList, var11, var12, var21, var22, var23, var18.x());
            DrawUtils.text(imDrawList, var11, var12, var21, var22, var24, ",");
            var21 += var15;
            var21 = a(imDrawList, var11, var12, var21, var22, var23, var18.y());
            DrawUtils.text(imDrawList, var11, var12, var21, var22, var24, ",");
            var21 += var15;
            a(imDrawList, var11, var12, var21, var22, var23, var18.z());
            if (var17 < list.size() - 1) {
               float var25 = var16 + var13;
               imDrawList.addLine(var7 + var14, var25, var7 + arg.width - var14, var25, DrawUtils.alpha(352321535, var5), 1.0F);
            }

            var16 += var13;
         }
      }
   }

   private static float a(ImDrawList imDrawList, ImFont imFont, float f, float g, float h, int i, String string) {
      DrawUtils.text(imDrawList, imFont, f, g, h, i, string);
      return g + DrawUtils.textWidth(imFont, f, string);
   }

   private List<Hud.d> a(MinecraftClient minecraftClient, float f, boolean bl) {
      if (minecraftClient.player == null) {
         this.potionMaxDuration.clear();
         this.potionAnim.clear();
         this.potionRowCache.clear();
         return List.of();
      } else {
         LinkedHashMap var4 = new LinkedHashMap();

         for (StatusEffectInstance var6 : minecraftClient.player.getStatusEffects()) {
            String var7 = a(var6);
            Hud.d var8 = this.a(var7, var6);
            var4.put(var7, var8);
            this.potionRowCache.put(var7, var8);
         }

         this.potionMaxDuration.keySet().retainAll(var4.keySet());
         HashSet var13 = new HashSet(var4.keySet());
         var13.addAll(this.potionAnim.keySet());
         ArrayList var14 = new ArrayList();

         for (String var16 : (Iterable<String>) var13) {
            boolean var9 = var4.containsKey(var16);
            float var10 = this.potionAnim.getOrDefault(var16, 0.0F);
            float var11 = DrawUtils.approach(var10, var9 ? 1.0F : 0.0F, f, 13.0F);
            if (!var9 && var11 <= 0.004F) {
               this.potionAnim.remove(var16);
               this.potionRowCache.remove(var16);
            } else {
               this.potionAnim.put(var16, var11);
               Hud.d var12 = var9 ? (Hud.d)var4.get(var16) : this.potionRowCache.get(var16);
               if (var12 != null) {
                  var14.add(new Hud.d(var12.key(), var12.name(), var12.duration(), var12.expiring(), var12.progress(), var11, var12.effect()));
               }
            }
         }

         var14.sort(Comparator.comparing(Hud.d::key));
         if (var14.isEmpty() && bl) {
            var14.add(new Hud.d("placeholder", "Effects", "0:00", false, 0.65F, 1.0F, null));
         }

         return var14;
      }
   }

   private Hud.d a(String string, StatusEffectInstance statusEffectInstance) {
      RegistryEntry var3 = statusEffectInstance.getEffectType();
      String var4 = I18n.translate(((StatusEffect)var3.value()).getTranslationKey(), new Object[0]);
      int var5 = statusEffectInstance.getAmplifier();
      if (var5 > 0) {
         var4 = var4 + " " + c(var5 + 1);
      }

      int var6 = statusEffectInstance.getDuration();
      if (var6 >= 0 && var6 != Integer.MAX_VALUE) {
         int var7 = this.potionMaxDuration.getOrDefault(string, 0);
         if (var6 > var7) {
            this.potionMaxDuration.put(string, var6);
            var7 = var6;
         }

         int var8 = Math.max(0, var6 / 20);
         String var9 = b(var8);
         float var10 = var7 > 0 ? (float)var6 / var7 : 1.0F;
         return new Hud.d(string, var4, var9, var8 <= 10, var10, 1.0F, var3);
      } else {
         return new Hud.d(string, var4, "INF", false, 1.0F, 1.0F, var3);
      }
   }

   private static String b(int i) {
      StringBuilder var1 = new StringBuilder(8);
      if (i >= 3600) {
         var1.append(i / 3600).append(':');
         a(var1, i / 60 % 60);
      } else {
         var1.append(i / 60);
      }

      var1.append(':');
      a(var1, i % 60);
      return var1.toString();
   }

   private static void a(StringBuilder stringBuilder, int i) {
      if (i < 10) {
         stringBuilder.append('0');
      }

      stringBuilder.append(i);
   }

   private static float[] a(List<Hud.d> list, float f) {
      ImFont var2 = ImGuiManager.getInstance().hudTextFont();
      ImFont var3 = ImGuiManager.getInstance().hudValueFont();
      float var4 = 180.0F * f;
      float var5 = 0.0F;

      for (Hud.d var7 : list) {
         float var8 = 39.0F * f
            + DrawUtils.textWidth(var2, 14.0F * f, var7.name())
            + 18.0F * f
            + DrawUtils.textWidth(var3, 19.0F * f, var7.duration())
            + 13.0F * f
            + 12.0F * f;
         var4 = Math.max(var4, var8);
         if (var5 > 0.0F) {
            var5 += 4.0F * f * var7.anim();
         }

         var5 += 34.0F * f * var7.anim();
      }

      return new float[]{var4, Math.max(var5, 34.0F * f * 0.2F)};
   }

   private void a(HudLayout.c arg, float f, float g, float h, float i, float j, boolean bl) {
      float[] var8 = this.layout.targetOrigin(arg.id, f, g, h, i);
      if (!bl && !Float.isNaN(this.potionSmoothX)) {
         this.potionSmoothX = DrawUtils.approach(this.potionSmoothX, var8[0], j, 13.0F);
         this.potionSmoothY = DrawUtils.approach(this.potionSmoothY, var8[1], j, 13.0F);
         this.potionSmoothW = DrawUtils.approach(this.potionSmoothW, f, j, 13.0F);
         this.potionSmoothH = DrawUtils.approach(this.potionSmoothH, g, j, 13.0F);
      } else {
         this.potionSmoothX = var8[0];
         this.potionSmoothY = var8[1];
         this.potionSmoothW = f;
         this.potionSmoothH = g;
      }

      this.layout.setRect(arg.id, this.potionSmoothX, this.potionSmoothY, this.potionSmoothW, this.potionSmoothH, h, i);
   }

   private void d() {
      HudLayout.c var1 = this.layout.element("potion_tracker");
      this.potionSmoothX = var1.x;
      this.potionSmoothY = var1.y;
      this.potionSmoothW = var1.width;
      this.potionSmoothH = var1.height;
   }

   private static void a(ImDrawList imDrawList, HudLayout.c arg, List<Hud.d> list, Hud.e arg2) {
      if (!list.isEmpty() && !(arg2.alpha() <= 0.002F)) {
         float var4 = scaleOf(arg);
         float var5 = ImGui.getIO().getDisplaySizeX();
         boolean var6 = !Palette.hudAccentOnLeft(arg.x, arg.width, var5);
         ImFont var7 = ImGuiManager.getInstance().hudTextFont();
         ImFont var8 = ImGuiManager.getInstance().hudValueFont();
         float var9 = 34.0F * var4;
         float var10 = arg.width;
         double var11 = System.nanoTime() / 1.0E9;
         float var13 = arg.y + arg2.dy();

         for (Hud.d var15 : list) {
            float var16 = DrawUtils.clamp01(var15.anim());
            if (!(var16 <= 0.004F)) {
               float var17 = DrawUtils.easeOutCubic(var16);
               float var18 = arg2.alpha() * var17;
               float var19 = (1.0F - var17) * 22.0F * var4 * (var6 ? 1.0F : -1.0F);
               float var20 = arg.x + arg2.dx() + var19;
               DrawUtils.card(imDrawList, var20, var13, var10, var9, 6.0F * var4, 5.6F * var4, var18, true);
               float var22 = var20 + var10 - 9.0F * var4 - 3.0F * var4;
               float var23 = var13 + 8.0F * var4;
               float var24 = var13 + var9 - 8.0F * var4;
               int var25 = var15.expiring() ? DrawUtils.alpha(-11908353, var18) : DrawUtils.accent(var18);
               imDrawList.addRectFilled(
                  var22, var23, var22 + 3.0F * var4, var24, DrawUtils.alpha(620756991, Math.min(1.0F, var18 * 1.4F)), 1.5F * var4
               );
               float var26 = var24 - (var24 - var23) * DrawUtils.clamp01(var15.progress());
               if (var24 - var26 > 0.5F) {
                  imDrawList.addRectFilled(var22, var26, var22 + 3.0F * var4, var24, var25, 1.5F * var4);
               }

               float var27 = var13 + var9 * 0.5F;
               if (var15.effect() != null) {
                  float var28 = 23.0F * var4;
                  a(imDrawList, var15.effect(), var20 + 8.0F * var4, var27 - var28 * 0.5F, var28, var18);
               }

               float var33 = 14.0F * var4;
               DrawUtils.text(
                  imDrawList,
                  var7,
                  var33,
                  var20 + 39.0F * var4,
                  DrawUtils.midTop(var7, var33, var27),
                  DrawUtils.alpha(-526345, var18),
                  var15.name()
               );
               float var29 = 19.0F * var4;
               float var30 = DrawUtils.textWidth(var8, var29, var15.duration());
               float var31 = var18;
               int var32 = -526345;
               if (var15.expiring()) {
                  var32 = -11908353;
                  var31 = var18 * (0.55F + 0.45F * (float)Math.abs(Math.sin(var11 * 5.0)));
               }

               DrawUtils.text(
                  imDrawList,
                  var8,
                  var29,
                  var20 + var10 - 13.0F * var4 - 12.0F * var4 - var30,
                  DrawUtils.midTop(var8, var29, var27),
                  DrawUtils.alpha(var32, var31),
                  var15.duration()
               );
               var13 += (var9 + 4.0F * var4) * var16;
            }
         }
      }
   }

   private static void a(ImDrawList imDrawList, RegistryEntry<StatusEffect> registryEntry, float f, float g, float h, float i) {
      int var6 = EffectIconCache.textureId(registryEntry);
      if (var6 >= 0) {
         float var7 = Math.round(f);
         float var8 = Math.round(g);
         imDrawList.addImage(var6, var7, var8, var7 + Math.round(h), var8 + Math.round(h), 0.0F, 0.0F, 1.0F, 1.0F, DrawUtils.alpha(-1, i));
      }
   }

   private static String a(StatusEffectInstance statusEffectInstance) {
      return statusEffectInstance.getEffectType().getKey().map(registryKey -> registryKey.getValue().toString()).orElse("unknown")
         + ":"
         + statusEffectInstance.getAmplifier();
   }

   private static String c(int i) {
      return switch (i) {
         case 1 -> "I";
         case 2 -> "II";
         case 3 -> "III";
         case 4 -> "IV";
         case 5 -> "V";
         case 6 -> "VI";
         case 7 -> "VII";
         case 8 -> "VIII";
         case 9 -> "IX";
         case 10 -> "X";
         default -> String.valueOf(i);
      };
   }

   private void e() {
      if (this.draggingId != null || this.resizingId != null) {
         this.f();
      }

      this.draggingId = null;
      this.resizingId = null;
      this.activeSnapGuides = List.of();
   }

   private void f() {
      this.layout.save();
      ModuleConfigManager.getInstance().save();
   }

   private void a(float f, float g, boolean bl) {
      float var4 = ImGui.getMousePosX();
      float var5 = ImGui.getMousePosY();
      float var6 = ImGui.getIO().getMouseWheel();
      if (var6 != 0.0F && this.resizingId == null) {
         String var7 = this.draggingId != null ? this.draggingId : this.b(var4, var5);
         if (var7 != null) {
            this.a(var7, this.c(var7) + (var6 > 0.0F ? 0.05F : -0.05F));
            this.f();
         }
      }

      if (this.resizingId != null) {
         if (ImGui.isMouseDown(0)) {
            this.a(var4, var5, f, g);
         } else {
            this.layout.resolveOverlap(this.resizingId, this.visibleIds, f, g);
            this.resizingId = null;
            this.f();
         }
      } else if (this.draggingId != null) {
         if (ImGui.isMouseDown(0)) {
            HudLayout.c var10 = this.layout.element(this.draggingId);
            SnapGuides.b var11 = SnapGuides.snapPosition(this.draggingId, var4 - this.dragOffsetX, var5 - this.dragOffsetY, var10.width, var10.height, f, g, this.layout);
            this.activeSnapGuides = var11.activeGuides();
            this.layout.moveTo(this.draggingId, var11.x(), var11.y(), f, g);
            if ("potion_tracker".equals(this.draggingId)) {
               this.d();
            }
         } else {
            this.activeSnapGuides = List.of();
            if ("potion_tracker".equals(this.draggingId)) {
               this.layout.refreshAnchor(this.draggingId, f, g);
               this.d();
            }

            this.layout.resolveOverlap(this.draggingId, this.visibleIds, f, g);
            this.draggingId = null;
            this.layout.save();
         }
      } else if (ImGui.isMouseClicked(0)) {
         if (!bl || !this.a(var4, var5)) {
            String var9 = this.b(var4, var5);
            if (var9 != null) {
               HudLayout.c var8 = this.layout.element(var9);
               if ("region_map".equals(var9)) {
                  this.layout.undockLists(f, g);
               }

               this.draggingId = var9;
               this.dragOffsetX = var4 - var8.x;
               this.dragOffsetY = var5 - var8.y;
            }
         }
      }
   }

   private boolean a(float f, float g) {
      List var3 = this.layout.orderedElements();

      for (int var4 = var3.size() - 1; var4 >= 0; var4--) {
         HudLayout.c var5 = (HudLayout.c)var3.get(var4);
         if (this.d(var5.id)) {
            float var6 = var5.x - 3.0F;
            float var7 = var5.y - 3.0F;
            float var8 = var5.x + var5.width + 3.0F;
            float var9 = var5.y + var5.height + 3.0F;

            for (int var10 = 0; var10 < 4; var10++) {
               boolean var11 = var10 == 1 || var10 == 2;
               boolean var12 = var10 >= 2;
               float var13 = var11 ? var8 : var6;
               float var14 = var12 ? var9 : var7;
               if (!(Math.abs(f - var13) > 9.0F) && !(Math.abs(g - var14) > 9.0F)) {
                  this.resizingId = var5.id;
                  this.resizeDirX = var11 ? 1.0F : -1.0F;
                  this.resizeDirY = var12 ? 1.0F : -1.0F;
                  this.resizeCenterX = var5.x + var5.width * 0.5F;
                  this.resizeCenterY = var5.y + var5.height * 0.5F;
                  this.resizeStartW = Math.max(1.0F, var5.width);
                  this.resizeStartH = Math.max(1.0F, var5.height);
                  this.resizeStartScale = this.c(var5.id);
                  return true;
               }
            }
         }
      }

      return false;
   }

   private void a(float f, float g, float h, float i) {
      float var5 = this.resizeStartW * 0.5F * this.resizeDirX;
      float var6 = this.resizeStartH * 0.5F * this.resizeDirY;
      float var7 = f - this.resizeCenterX;
      float var8 = g - this.resizeCenterY;
      float var9 = (var7 * var5 + var8 * var6) / (var5 * var5 + var6 * var6);
      this.a(this.resizingId, this.resizeStartScale * Math.max(0.05F, var9));
      HudLayout.c var10 = this.layout.element(this.resizingId);
      this.layout.moveTo(this.resizingId, this.resizeCenterX - var10.width * 0.5F, this.resizeCenterY - var10.height * 0.5F, h, i);
      if ("potion_tracker".equals(this.resizingId)) {
         this.d();
      }
   }

   private float c(String string) {
      if ("region_map".equals(string) && this.regionMapModule != null) {
         return this.regionMapModule.cellSize() / 22.0F;
      } else {
         return "spotify_hud".equals(string) && this.spotifyHudModule != null ? this.spotifyHudModule.scale() : scaleOf(this.layout.element(string));
      }
   }

   private void a(String string, float f) {
      if ("region_map".equals(string) && this.regionMapModule != null) {
         NumberSetting var3 = this.regionMapModule.cellSizeSetting();
         float var4 = this.c(string);
         float var5 = Math.max(var3.min() / 22.0F, Math.min(var3.max() / 22.0F, f));
         if (!(var4 <= 0.0F) && !(Math.abs(var5 - var4) < 1.0E-4F)) {
            float var6 = var5 / var4;
            var3.set(22.0F * var5);
            NumberSetting var7 = this.regionMapModule.cellGapSetting();
            var7.set(var7.get() * var6);
         }
      } else if ("spotify_hud".equals(string) && this.spotifyHudModule != null) {
         this.spotifyHudModule.scaleSetting().set(f);
      } else {
         this.layout.setScale(string, f);
      }
   }

   private String b(float f, float g) {
      List var3 = this.layout.orderedElements();

      for (int var4 = var3.size() - 1; var4 >= 0; var4--) {
         HudLayout.c var5 = (HudLayout.c)var3.get(var4);
         if (this.d(var5.id) && !(this.chatHide.getOrDefault(var5.id, 0.0F) >= 0.5F) && !(this.tabHide.getOrDefault(var5.id, 0.0F) >= 0.5F) && c(var5, f, g)) {
            return var5.id;
         }
      }

      return null;
   }

   private void a(ImDrawList imDrawList, float f, float g) {
      if (!this.activeSnapGuides.isEmpty()) {
         SnapGuides.drawGuides(imDrawList, this.activeSnapGuides, f, g, DrawUtils.accent(0.5F));
      }

      float var4 = ImGui.getMousePosX();
      float var5 = ImGui.getMousePosY();
      this.b(imDrawList, var4, var5);
   }

   private void b(ImDrawList imDrawList, float f, float g) {
      ImFont var4 = ImGuiManager.getInstance().hudLabelFont();
      float var5 = 11.0F;

      for (HudLayout.c var7 : this.layout.orderedElements()) {
         if (this.d(var7.id)) {
            boolean var8 = var7.id.equals(this.draggingId) || var7.id.equals(this.resizingId);
            boolean var9 = c(var7, f, g) || b(var7, f, g);
            float var10 = var8 ? 1.0F : (var9 ? 0.75F : 0.35F);
            float var11 = var7.x - 3.0F;
            float var12 = var7.y - 3.0F;
            float var13 = var7.x + var7.width + 3.0F;
            float var14 = var7.y + var7.height + 3.0F;
            if (var8) {
               DrawUtils.glow(imDrawList, var11, var12, var13, var14, 6.0F, 7.0F, AccentColor.accent(), 0.8F);
            }

            DrawUtils.strokeSlashed(imDrawList, var11, var12, var13, var14, 6.0F, 7.0F, DrawUtils.accent(var10), var8 ? 2.0F : 1.2F);
            boolean var15 = a(var8, var9);
            String var16 = e(var7.id).toUpperCase(Locale.ROOT);
            if (var15) {
               var16 = var16 + "  " + Math.round(this.c(var7.id) * 100.0F) + "%";
            }

            float var17 = var11 + 8.0F;
            float var18 = DrawUtils.trackedWidth(var4, var5, var16, 1.0F) + 14.0F;
            float var19 = Math.max(2.0F, var12 - 20.0F);
            DrawUtils.fillSlashed(imDrawList, var17, var19, var17 + var18, var19 + 17.0F, 3.0F, 4.0F, var15 ? AccentColor.accent() : -234091766);
            DrawUtils.tracked(
               imDrawList, var4, var5, var17 + 7.0F, var19 + 2.5F, var15 ? DrawUtils.inkOnAccent() : -6119783, var16, 1.0F
            );
            if (var15) {
               b(imDrawList, var11, var12, var13, var14, f, g);
            }
         }
      }
   }

   private boolean d(String string) {
      return this.visibleIds.contains(string);
   }

   static boolean isDrawn(String string) {
      return INSTANCE.visibleIds.contains(string);
   }

   private static boolean b(HudLayout.c arg, float f, float g) {
      float var3 = arg.x - 3.0F;
      float var4 = arg.y - 3.0F;
      float var5 = arg.x + arg.width + 3.0F;
      float var6 = arg.y + arg.height + 3.0F;
      boolean var7 = Math.abs(f - var3) <= 9.0F || Math.abs(f - var5) <= 9.0F;
      boolean var8 = Math.abs(g - var4) <= 9.0F || Math.abs(g - var6) <= 9.0F;
      return var7 && var8;
   }

   private static void b(ImDrawList imDrawList, float f, float g, float h, float i, float j, float k) {
      float var7 = 4.0F;
      float[][] var8 = new float[][]{{f, g}, {h, g}, {h, i}, {f, i}};

      for (float[] var12 : var8) {
         boolean var13 = Math.abs(j - var12[0]) <= 9.0F && Math.abs(k - var12[1]) <= 9.0F;
         imDrawList.addRectFilled(var12[0] - var7, var12[1] - var7, var12[0] + var7, var12[1] + var7, -234091766, 1.5F);
         imDrawList.addRectFilled(
            var12[0] - var7 + 1.5F,
            var12[1] - var7 + 1.5F,
            var12[0] + var7 - 1.5F,
            var12[1] + var7 - 1.5F,
            DrawUtils.accent(var13 ? 1.0F : 0.75F),
            1.0F
         );
         if (var13) {
            imDrawList.addRect(
               var12[0] - var7 - 2.5F,
               var12[1] - var7 - 2.5F,
               var12[0] + var7 + 2.5F,
               var12[1] + var7 + 2.5F,
               DrawUtils.accent(0.6F),
               2.0F,
               0,
               1.2F
            );
         }
      }
   }

   private static boolean a(boolean bl, boolean bl2) {
      return bl || bl2;
   }

   private static boolean c(HudLayout.c arg, float f, float g) {
      return f >= arg.x && f <= arg.x + arg.width && g >= arg.y && g <= arg.y + arg.height;
   }

   private static String e(String string) {
      return switch (string) {
         case "info_bar" -> "Info Bar";
         case "coordinates" -> "Coordinates";
         case "potion_tracker" -> "Effects";
         case "region_map" -> "Region Map";
         case "spotify_hud" -> "Spotify";
         case "armor_hud" -> "Armor";
         case "inventory_hud" -> "Inventory";
         case "keybinds" -> "Keybinds";
         case "staff_list" -> "Staff List";
         default -> f(string);
      };
   }

   private static String f(String string) {
      for (int var1 = 0; var1 < 12; var1++) {
         if (HudBoxes.IDS[var1].equals(string)) {
            return HudBoxes.name(var1);
         }
      }

      return string;
   }

   private static int c(MinecraftClient minecraftClient) {
      int var1 = TabFooter.ping();
      if (var1 >= 0) {
         return Math.min(9999, var1);
      } else {
         ClientPlayNetworkHandler var2 = minecraftClient.getNetworkHandler();
         if (var2 != null && minecraftClient.player != null) {
            int var3 = a(minecraftClient, var2);
            if (var3 <= 0) {
               var3 = a(var2);
            }

            return Math.min(9999, Math.max(0, var3));
         } else {
            return 0;
         }
      }
   }

   private static int a(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler) {
      PlayerListEntry var2 = clientPlayNetworkHandler.getPlayerListEntry(clientPlayNetworkHandler.getProfile().id());
      if (var2 == null) {
         var2 = clientPlayNetworkHandler.getPlayerListEntry(minecraftClient.player.getUuid());
      }

      if (var2 == null) {
         var2 = clientPlayNetworkHandler.getCaseInsensitivePlayerInfo(clientPlayNetworkHandler.getProfile().name());
      }

      if (var2 == null) {
         var2 = clientPlayNetworkHandler.getCaseInsensitivePlayerInfo(minecraftClient.player.getName().getString());
      }

      return var2 == null ? 0 : Math.max(0, var2.getLatency());
   }

   private static int a(ClientPlayNetworkHandler clientPlayNetworkHandler) {
      ServerInfo var1 = clientPlayNetworkHandler.getServerInfo();
      return var1 != null && var1.ping > 0L && var1.ping <= 60000L ? (int)var1.ping : 0;
   }

   static float scaleOf(HudLayout.c arg) {
      return !Float.isNaN(arg.scale) && !Float.isInfinite(arg.scale) ? Math.max(0.5F, Math.min(3.0F, arg.scale)) : 1.0F;
   }

   @Environment(EnvType.CLIENT)
   private record a(float width, float height, float leadW, float commaW, float numbers, float valueSize, float rowH) {
   }

   @Environment(EnvType.CLIENT)
   private record b(String tag, int dot, String x, String y, String z, boolean active) {
   }

   @Environment(EnvType.CLIENT)
   private static final class c {
      String fps;
      String ping;
      float width;
      float height;
      float leftW;
      float rightW;
      float slotW;
      float canW;
   }

   @Environment(EnvType.CLIENT)
   private record d(String key, String name, String duration, boolean expiring, float progress, float anim, RegistryEntry<StatusEffect> effect) {
   }

   @Environment(EnvType.CLIENT)
   private record e(float dx, float dy, float alpha) {
      static final Hud.e IDENTITY = new Hud.e(0.0F, 0.0F, 1.0F);
   }
}
