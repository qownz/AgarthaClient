package net.agartha.client.gui;

import imgui.ImDrawList;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.type.ImString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.agartha.client.config.ModuleConfigManager;
import net.agartha.client.gui.AccentColor;
import net.agartha.client.gui.ClawTexture;
import net.agartha.client.gui.ClickGuiLayout;
import net.agartha.client.gui.DrawUtils;
import net.agartha.client.gui.ItemLookup;
import net.agartha.client.gui.MacroModal;
import net.agartha.client.gui.ModalManager;
import net.agartha.client.gui.OpenAnimation;
import net.agartha.client.gui.Palette;
import net.agartha.client.gui.UiSounds;
import net.agartha.client.gui.WhitelistModal;
import net.agartha.client.gui.Widgets;
import net.agartha.client.input.Keybind;
import net.agartha.client.macro.MacroManager;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.client.AgarthaModule;
import net.agartha.client.module.impl.client.ChatMacrosModule;
import net.agartha.client.module.impl.client.ColorSettingsModule;
import net.agartha.client.module.impl.client.ConfigModule;
import net.agartha.client.module.impl.client.HudModule;
import net.agartha.client.module.impl.client.MonsterModeModule;
import net.agartha.client.module.impl.client.NotificationsModule;
import net.agartha.client.module.impl.client.RegionMapModule;
import net.agartha.client.module.impl.client.SpotifyHudModule;
import net.agartha.client.module.impl.misc.PlayerAlertModule;
import net.agartha.client.module.impl.misc.SkinChangeModule;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.agartha.client.module.impl.render.PlayerEspModule;
import net.agartha.client.module.impl.render.StorageEspModule;
import net.agartha.client.module.setting.BlockColorSetting;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ColorSetting;
import net.agartha.client.module.setting.EntityTypeListSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.ItemColorSetting;
import net.agartha.client.module.setting.ItemIdListSetting;
import net.agartha.client.module.setting.ItemIdSetting;
import net.agartha.client.module.setting.KeybindSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.module.setting.Setting;
import net.agartha.client.module.setting.StringSetting;
import net.agartha.client.render.imgui.ImGuiManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class ClickGui {
   private static final float PANEL_W = 248.0F;
   private static final float PANEL_GAP = 20.0F;
   private static final float PANEL_START_X = 20.0F;
   private static final float PANEL_START_Y = 92.0F;
   private static final float PANEL_R = 8.0F;
   private static final float PANEL_CUT = 12.0F;
   private static final float HEADER_H = 44.0F;
   private static final float BODY_PAD = 6.0F;
   private static final float BODY_TOP = 2.0F;
   private static final float MUTED_LINE_H = 22.0F;
   private static final float PANEL_BOTTOM_MARGIN = 28.0F;
   private static final float DEFAULT_PANEL_ROW_H = 320.0F;
   private static final float ROW_H = 30.0F;
   private static final float ROW_GAP = 2.0F;
   private static final float SWITCH_W = 28.0F;
   private static final float SWITCH_H = 14.0F;
   private static final float GEAR_W = 20.0F;
   private static final float SET_ROW_H = 26.0F;
   private static final float NUM_ROW_H = 42.0F;
   private static final float STR_ROW_H = 50.0F;
   private static final float SECTION_H = 22.0F;
   private static final float ACTION_H = 26.0F;
   private static final float CHIP_H = 20.0F;
   private static final float NR_CHIP_H = 22.0F;
   private static final float NR_CHIP_GAP = 4.0F;
   private static final float INDENT = 16.0F;
   private static final float DRAWER_TOP = 4.0F;
   private static final float DRAWER_BOTTOM = 6.0F;
   private static final float DRAWER_RIGHT = 8.0F;
   private static final float CHECK = 15.0F;
   private static final float NUMBER_VALUE_W = 52.0F;
   private static final float SETTINGS_MAX_VISIBLE_H = 420.0F;
   private static final float SEARCH_INPUT_H = 28.0F;
   private static final int SEARCH_MAX_RESULTS = 10;
   private static final float INTRO_DURATION = 0.48F;
   private static final float INTRO_STAGGER = 0.05F;
   private static final float INTRO_SLIDE = 34.0F;
   private static final float INTRO_MAX_DT = 0.05F;
   private static final float OUTRO_DURATION = 0.2F;
   private static final float OUTRO_DROP = 22.0F;
   private static final float SHOCKWAVE_SECONDS = 0.6F;
   private static final float ROW_SPEED = 14.0F;
   private static final float HOVER_SPEED = 18.0F;
   private static final int STYLE_VAR_COUNT = 6;
   private static final int STYLE_COLOR_COUNT = 20;
   private static final ModuleCategory[] GUI_CATEGORY_ORDER = new ModuleCategory[]{
      ModuleCategory.RENDER, ModuleCategory.DONUT, ModuleCategory.MISC, ModuleCategory.CLIENT
   };
   private final ModalManager modals = new ModalManager(ClickGui::d);
   private final Item[] chipIcons = new Item[4];
   private final OpenAnimation<Module> panelExpansion = new OpenAnimation<>();
   private final OpenAnimation<Module> searchExpansion = new OpenAnimation<>();
   private final OpenAnimation<ModuleCategory> categoryExpansion = new OpenAnimation<>();
   private final OpenAnimation<String> searchExpanded = new OpenAnimation<>();
   private final Map<String, Float> anim = new HashMap<>();
   private Module listeningModule;
   private boolean listeningFirstFrame;
   private KeybindSetting listeningKeySetting;
   private boolean listeningKeyFirstFrame;
   private static final int LISTEN_CANCEL = -2;
   private final boolean[] heldWhenListening = new boolean[1050];
   private NumberSetting editingNumberSetting;
   private final ImString editingNumberText = new ImString(16);
   private ClickGui.b pendingConfirm = ClickGui.b.NONE;
   private Module pendingResetModule;
   private final ImString globalSearch = new ImString(128);
   private final Map<ModuleCategory, List<Module>> categoryModules = new HashMap<>();
   private int cachedRegistryRevision = -1;
   private int cachedVisibleRevision = -1;
   private ModuleCategory[] cachedVisibleCategories = GUI_CATEGORY_ORDER;
   private String cachedGlobalSearch = "";
   private List<Module> cachedGlobalResults = List.of();
   private int appliedLayoutEpoch = -1;
   private boolean forceLayoutThisFrame;
   private boolean screenLayoutRefreshPending = true;
   private float lastLayoutScreenW = -1.0F;
   private float lastLayoutScreenH = -1.0F;
   private float guiIntroTime;
   private float openFxTime;
   private boolean introSkipFrame;
   private boolean introDelaysBuilt;
   private String introBuiltDirection = "";
   private float introMaxDelay;
   private final Map<String, Float> introDelayByPanel = new HashMap<>();
   private final Map<String, Float> introProgressSnapshot = new HashMap<>();
   private final Set<String> introAlphaPushed = new HashSet<>();
   private final Set<String> hoveredRows = new HashSet<>();
   private long frameNanos = System.nanoTime();
   private boolean closing;
   private float outroTime;
   private Runnable onClosed;

   public void resetGuiIntro() {
      this.guiIntroTime = 0.0F;
      this.openFxTime = 0.0F;
      this.introSkipFrame = true;
      this.introDelaysBuilt = false;
      this.introBuiltDirection = "";
      this.introDelayByPanel.clear();
      this.introProgressSnapshot.clear();
      this.introAlphaPushed.clear();
      this.introMaxDelay = 0.0F;
      this.closing = false;
      this.outroTime = 0.0F;
      this.onClosed = null;
      this.hoveredRows.clear();
      UiSounds.guiOpen();
      ClickGuiLayout var1 = ClickGuiLayout.getInstance();
      int var2 = var1.layoutEpoch();
      if (var1.isReapplyPending()) {
         this.appliedLayoutEpoch = var2 - 1;
         this.forceLayoutThisFrame = true;
      } else {
         this.appliedLayoutEpoch = var2;
         this.forceLayoutThisFrame = false;
      }
   }

   public void requestClose(Runnable runnable) {
      if (!this.closing) {
         UiSounds.guiClose();
      }

      if (!this.closing && d() && !this.modals.anyOpen()) {
         this.closing = true;
         this.outroTime = 0.0F;
         this.onClosed = null;
         runnable.run();
         ImGuiManager.getInstance().playFarewell(this::render, 0.2F);
      } else {
         this.closing = false;
         this.onClosed = null;
         runnable.run();
      }
   }

   public void render() {
      this.frameNanos = System.nanoTime();
      float var1 = Math.max(ImGui.getIO().getDeltaTime(), 0.001F);
      ClickGuiLayout var2 = ClickGuiLayout.getInstance();
      int var3 = var2.layoutEpoch();
      float var4 = ImGui.getIO().getDisplaySizeX();
      float var5 = ImGui.getIO().getDisplaySizeY();
      if (Math.abs(var4 - this.lastLayoutScreenW) > 0.5F || Math.abs(var5 - this.lastLayoutScreenH) > 0.5F) {
         this.lastLayoutScreenW = var4;
         this.lastLayoutScreenH = var5;
         this.screenLayoutRefreshPending = true;
      }

      this.forceLayoutThisFrame = this.appliedLayoutEpoch != var3 || var2.isReapplyPending() || this.screenLayoutRefreshPending;
      this.introAlphaPushed.clear();
      this.modals.updateIntro(var1);
      if (!this.modals.anyOpen()) {
         this.a(var1);
      }

      this.openFxTime = this.openFxTime + Math.min(var1, 0.05F);
      if (this.closing) {
         this.outroTime = this.outroTime + Math.min(var1, 0.05F);
      }

      p();
      boolean var6 = a(ImGuiManager.getInstance().moduleFont());

      try {
         this.a(var4, var5);
         if (this.closing || !this.modals.render(var1)) {
            for (ModuleCategory var10 : this.k()) {
               this.a(var10, this.a(var10));
            }

            this.l();
            if (this.forceLayoutThisFrame) {
               this.appliedLayoutEpoch = var3;
               var2.markReapplyConsumed();
               this.screenLayoutRefreshPending = false;
               this.forceLayoutThisFrame = false;
            }
         }
      } finally {
         if (var6) {
            ImGui.popFont();
         }

         q();
      }

      if (this.closing && this.outroTime >= 0.2F && this.onClosed != null) {
         Runnable var14 = this.onClosed;
         this.onClosed = null;
         MinecraftClient.getInstance().execute(var14);
      }
   }

   private float a() {
      return this.closing ? DrawUtils.easeInCubic(this.outroTime / 0.2F) : 0.0F;
   }

   private void a(float f, float g) {
      if (b()) {
         ImDrawList var3 = ImGui.getBackgroundDrawList();
         float var4 = d() ? DrawUtils.easeOutCubic(this.openFxTime / 0.28F) : 1.0F;
         float var5 = var4 * (1.0F - this.a());
         if (!(var5 <= 0.01F)) {
            int var6 = DrawUtils.withAlpha(-16777216, Math.round(95.0F * var5));
            int var7 = DrawUtils.withAlpha(-16777216, Math.round(165.0F * var5));
            var3.addRectFilledMultiColor(0.0F, 0.0F, f, g, var6, var6, var7, var7);
            float var8 = g * 0.4F;
            int var9 = AccentColor.accentWithAlpha(Math.round(22.0F * var5));
            int var10 = AccentColor.accentWithAlpha(0);
            var3.addRectFilledMultiColor(0.0F, g - var8, f, g, var10, var10, var9, var9);
         }
      }
   }

   private void a(float f) {
      if (d()) {
         if (this.introSkipFrame) {
            this.introSkipFrame = false;
         } else {
            this.i();
            this.guiIntroTime = Math.min(this.guiIntroTime + Math.min(f, 0.05F), 0.48F + this.introMaxDelay);
         }
      }
   }

   private static boolean b() {
      AgarthaModule var0 = e();
      return var0 == null || var0.menuBackground().get();
   }

   private static boolean c() {
      AgarthaModule var0 = e();
      return var0 == null || var0.menuParticles().get();
   }

   private static boolean d() {
      AgarthaModule var0 = e();
      return var0 == null || var0.menuAnimation().get();
   }

   private static AgarthaModule e() {
      return ModuleManager.get(AgarthaModule.class);
   }

   private static float f() {
      AgarthaModule var0 = e();
      return var0 == null ? 1.0F : var0.guiOpacity();
   }

   private static int a(int i) {
      if (f() >= 0.999F) {
         return i & 16777215 | 0xFF000000;
      } else {
         int var1 = i >>> 24 & 0xFF;
         return DrawUtils.withAlpha(i, Math.round(var1 * f()));
      }
   }

   private static boolean g() {
      AgarthaModule var0 = e();
      return var0 != null && var0.lockClickGui().get();
   }

   private static String h() {
      AgarthaModule var0 = e();
      return var0 == null ? "Top to Bottom" : var0.animation().get();
   }

   private void i() {
      String var1 = h();
      if (!this.introDelaysBuilt || !var1.equals(this.introBuiltDirection)) {
         this.a(var1);
         this.introDelaysBuilt = true;
         this.introBuiltDirection = var1;
      }
   }

   private void a(String string) {
      this.introDelayByPanel.clear();
      ArrayList<ClickGui.a> var2 = new ArrayList<>();

      for (ModuleCategory var6 : this.k()) {
         float[] var7 = this.a(var6);
         var2.add(new ClickGui.a(var6.name(), var7[0], var7[1]));
      }

      float[] var9 = this.j();
      var2.add(new ClickGui.a("search", var9[0], var9[1]));
      boolean var10 = string.equals("Left to Right") || string.equals("Right to Left");
      boolean var11 = string.equals("Right to Left") || string.equals("Bottom to Top");
      var2.sort((arg, arg2) -> {
         float var4 = var10 ? arg.x() : arg.y();
         float var5 = var10 ? arg2.x() : arg2.y();
         if (Math.abs(var4 - var5) > 1.0F) {
            return var11 ? Float.compare(var5, var4) : Float.compare(var4, var5);
         } else {
            return Float.compare(var10 ? arg.y() : arg.x(), var10 ? arg2.y() : arg2.x());
         }
      });
      float var12 = 0.0F;

      for (int var13 = 0; var13 < var2.size(); var13++) {
         float var8 = var13 * 0.05F;
         this.introDelayByPanel.put(((ClickGui.a)var2.get(var13)).id(), var8);
         var12 = Math.max(var12, var8);
      }

      this.introMaxDelay = var12;
   }

   private float b(String string) {
      if (!d()) {
         return 1.0F;
      } else {
         float var2 = this.introDelayByPanel.getOrDefault(string, 0.0F);
         return DrawUtils.clamp01((this.guiIntroTime - var2) / 0.48F);
      }
   }

   private boolean a(String string, float[] fs) {
      if (!d()) {
         ImGui.setNextWindowPos(fs[0], fs[1], this.forceLayoutThisFrame ? 1 : 2);
         this.introProgressSnapshot.put(string, 1.0F);
         return true;
      } else {
         this.i();
         float var3 = this.b(string);
         if (var3 <= 0.0F) {
            return false;
         } else {
            this.introProgressSnapshot.put(string, var3);
            float var4 = this.a();
            if (var3 < 1.0F || var4 > 0.0F) {
               float var5 = 34.0F * (1.0F - DrawUtils.easeOutBack(var3));
               float var6 = 0.0F;
               float var7 = 0.0F;
               String var8 = h();
               switch (var8) {
                  case "Left to Right":
                     var6 = -var5;
                     break;
                  case "Right to Left":
                     var6 = var5;
                     break;
                  case "Bottom to Top":
                     var7 = var5;
                     break;
                  default:
                     var7 = -var5;
               }

               var7 += 22.0F * var4;
               ImGui.setNextWindowPos(fs[0] + var6, fs[1] + var7, 1);
               float var11 = DrawUtils.easeOutCubic(var3) * (1.0F - var4);
               ImGui.pushStyleVar(0, Math.max(0.001F, var11));
               this.introAlphaPushed.add(string);
            } else if (this.forceLayoutThisFrame) {
               ImGui.setNextWindowPos(fs[0], fs[1], 1);
            }

            return true;
         }
      }
   }

   private void c(String string) {
      this.introProgressSnapshot.remove(string);
      if (this.introAlphaPushed.remove(string)) {
         ImGui.popStyleVar();
      }
   }

   private float d(String string) {
      return this.introProgressSnapshot.getOrDefault(string, 1.0F);
   }

   private int a(boolean bl) {
      int var2 = 197049;
      var2 |= bl ? 64 : 2;
      if (g()) {
         var2 |= 4;
      }

      if (this.closing) {
         var2 |= 197120;
      }

      return var2;
   }

   private void e(String string) {
      ClickGuiLayout var2 = ClickGuiLayout.getInstance();
      if (!this.closing
         && !this.forceLayoutThisFrame
         && !var2.isReapplyPending()
         && this.appliedLayoutEpoch == var2.layoutEpoch()
         && !(this.d(string) < 0.999F)
         && !g()) {
         var2.updatePosition(string, ImGui.getWindowPosX(), ImGui.getWindowPosY(), 248.0F, ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY());
      }
   }

   private void b(String string, float[] fs) {
      if (g() && this.d(string) >= 0.999F && !this.closing) {
         ImGui.setWindowPos(fs[0], fs[1]);
      }
   }

   private static int a(float f, int i) {
      float var2 = 268.0F;
      int var3 = Math.max(1, (int)((f - 40.0F + 20.0F) / var2));
      return Math.min(var3, i);
   }

   private static float b(float f, int i) {
      float var2 = i * 248.0F + (i - 1) * 20.0F;
      return Math.max(20.0F, (f - var2) * 0.5F);
   }

   private static float[] a(int i, float f) {
      int var2 = GUI_CATEGORY_ORDER.length + 1;
      int var3 = a(f, var2);
      int var4 = i / var3;
      int var5 = i % var3;
      int var6 = Math.min(var3, var2 - var4 * var3);
      return new float[]{b(f, var6) + var5 * 268.0F, 92.0F + var4 * 340.0F};
   }

   private static float[] b(float f) {
      return a(GUI_CATEGORY_ORDER.length, f);
   }

   private float[] a(ModuleCategory arg) {
      float var2 = ImGui.getIO().getDisplaySizeX();
      float var3 = ImGui.getIO().getDisplaySizeY();
      int var4 = 0;

      for (int var5 = 0; var5 < GUI_CATEGORY_ORDER.length; var5++) {
         if (GUI_CATEGORY_ORDER[var5] == arg) {
            var4 = var5;
         }
      }

      float[] var6 = a(var4, var2);
      return ClickGuiLayout.getInstance().resolvePosition(arg.name(), var6[0], var6[1], 248.0F, var2, var3);
   }

   private float[] j() {
      float var1 = ImGui.getIO().getDisplaySizeX();
      float var2 = ImGui.getIO().getDisplaySizeY();
      float[] var3 = b(var1);
      return ClickGuiLayout.getInstance().resolvePosition("search", var3[0], var3[1], 248.0F, var1, var2);
   }

   private ModuleCategory[] k() {
      int var1 = ModuleManager.revision();
      if (var1 == this.cachedVisibleRevision) {
         return this.cachedVisibleCategories;
      } else {
         ArrayList<ModuleCategory> var2 = new ArrayList<>();

         for (ModuleCategory var6 : GUI_CATEGORY_ORDER) {
            if (!this.b(var6).isEmpty()) {
               var2.add(var6);
            }
         }

         this.cachedVisibleCategories = var2.toArray(ModuleCategory[]::new);
         this.cachedVisibleRevision = var1;
         return this.cachedVisibleCategories;
      }
   }

   private List<Module> b(ModuleCategory arg) {
      int var2 = ModuleManager.revision();
      if (var2 != this.cachedRegistryRevision) {
         this.categoryModules.clear();
         this.cachedRegistryRevision = var2;
         this.cachedVisibleRevision = -1;
      }

      return this.categoryModules.computeIfAbsent(arg, argx -> {
         ArrayList<Module> var1 = new ArrayList<>();

         for (Module var3 : ModuleManager.all()) {
            if (var3.category() == argx && !f(var3)) {
               var1.add(var3);
            }
         }

         if (argx == ModuleCategory.CLIENT) {
            var1.sort((argxx, arg2) -> {
               int var2x = a(argxx);
               int var3x = a(arg2);
               return var2x != var3x ? Integer.compare(var2x, var3x) : argxx.name().compareToIgnoreCase(arg2.name());
            });
         }

         return List.copyOf(var1);
      });
   }

   private static int a(Module arg) {
      if (arg instanceof AgarthaModule) {
         return 0;
      } else if (arg instanceof HudModule) {
         return 1;
      } else if (arg instanceof ConfigModule) {
         return 2;
      } else {
         return arg instanceof ColorSettingsModule ? 4 : 3;
      }
   }

   private List<Module> f(String string) {
      if (string.equals(this.cachedGlobalSearch)) {
         return this.cachedGlobalResults;
      } else {
         ArrayList var2 = new ArrayList();

         for (Module var4 : ModuleManager.all()) {
            if (!f(var4) && var4.name().toLowerCase(Locale.ROOT).contains(string)) {
               var2.add(var4);
            }
         }

         this.cachedGlobalSearch = string;
         this.cachedGlobalResults = List.copyOf(var2);
         return this.cachedGlobalResults;
      }
   }

   private void a(ModuleCategory arg, float[] fs) {
      List var3 = this.b(arg);
      this.categoryExpansion.prime(arg, true);
      float var4 = ImGui.getIO().getDisplaySizeY();
      float var5 = var4 > 0.0F ? Math.max(120.0F, var4 - fs[1] - 28.0F) : 9999.0F;
      float var6 = 46.0F;
      float var7 = this.categoryExpansion.factor(arg, this.frameNanos);
      float var8 = this.a(var3, this.panelExpansion);
      float var9 = (float)Math.floor(Math.min(var8, Math.max(50.0F, var5 - var6 - 10.0F)) * var7);
      boolean var10 = var9 >= 1.0F;
      float var11 = (float)Math.floor(Math.min(var5, var6 + (var10 ? var9 + 8.0F : 0.0F)));
      String var12 = arg.name();
      if (this.a(var12, fs)) {
         ImGui.setNextWindowSizeConstraints(248.0F, 40.0F, 248.0F, var5);
         ImGui.setNextWindowSize(248.0F, var11, 1);
         ImGui.pushStyleVar(2, 0.0F, 0.0F);
         boolean var13 = ImGui.begin("##panel_" + var12, this.a(false));
         ImGui.popStyleVar();
         if (var13) {
            this.b(var12, fs);
            this.g(var12);
            int var14 = 0;

            for (Module var16 : (Iterable<Module>) var3) {
               if (var16.isEnabled()) {
                  var14++;
               }
            }

            if (this.a(var12, arg.displayName(), var14, var3.size(), var7, false)) {
               UiSounds.drawer(this.categoryExpansion.toggle(arg, this.frameNanos));
            }

            if (var10) {
               float var21 = 236.0F;
               ImGui.setCursorPos(6.0F, var6);
               ImGui.pushStyleVar(2, 0.0F, 0.0F);
               int var22 = 196616;
               if (!this.categoryExpansion.isOpen(arg) || this.closing) {
                  var22 |= 197120;
               }

               boolean var17 = ImGui.beginChild("##rows_" + var12, var21, var9, false, var22);
               ImGui.popStyleVar();
               if (var17) {
                  float var18 = 2.0F;

                  for (Module var20 : (Iterable<Module>) var3) {
                     var18 = this.a(var20, var18, 0.0F, var21, this.panelExpansion, "p:", false);
                  }

                  ImGui.setCursorPos(0.0F, var18);
                  if (var8 <= var5 - var6 - 10.0F) {
                     ImGui.setScrollY(0.0F);
                  } else {
                     m();
                  }
               }

               ImGui.endChild();
            }

            this.e(var12);
         }

         ImGui.end();
         this.c(var12);
      }
   }

   private void l() {
      String var1 = "search";
      float[] var2 = this.j();
      if (this.a(var1, var2)) {
         this.searchExpanded.prime(var1, true);
         float var3 = this.searchExpanded.factor(var1, this.frameNanos);
         String var4 = this.globalSearch.get();
         String var5 = var4 == null ? "" : var4.toLowerCase(Locale.ROOT).trim();
         List var6 = var5.isEmpty() ? List.of() : this.f(var5);
         int var7 = Math.min(10, var6.size());
         float var8 = 86.0F;
         float var9;
         if (var7 == 0) {
            var9 = 22.0F;
         } else {
            var9 = 0.0F;

            for (int var10 = 0; var10 < var7; var10++) {
               var9 += 32.0F + this.a((Module)var6.get(var10), this.searchExpansion);
            }

            if (var6.size() > var7) {
               var9 += 22.0F;
            }
         }

         float var20 = ImGui.getIO().getDisplaySizeY();
         float var11 = var20 > 0.0F ? Math.max(120.0F, var20 - var2[1] - 28.0F) : 9999.0F;
         float var12 = (float)Math.floor((var8 - 44.0F + var9 + 8.0F) * var3);
         boolean var13 = var12 >= 1.0F;
         float var14 = (float)Math.floor(Math.min(var11, 44.0F + (var13 ? var12 : 0.0F)));
         ImGui.setNextWindowSizeConstraints(248.0F, 40.0F, 248.0F, var11);
         ImGui.setNextWindowSize(248.0F, var14, 1);
         ImGui.pushStyleVar(2, 0.0F, 0.0F);
         boolean var15 = ImGui.begin("##panel_search", this.a(false));
         ImGui.popStyleVar();
         if (var15) {
            this.b(var1, var2);
            this.g(var1);
            if (this.a(var1, "Search", -1, 0, var3, true)) {
               UiSounds.drawer(this.searchExpanded.toggle(var1, this.frameNanos));
            }

            if (!var13) {
               this.e(var1);
               ImGui.end();
               this.c(var1);
               return;
            }

            float var16 = 10.0F;
            float var17 = 248.0F - var16 * 2.0F;
            ImGui.setCursorPos(var16, 50.0F);
            this.c(var17);
            float var18 = var8;
            if (var5.isEmpty()) {
               a(var16 + 2.0F, var8, "Type to search every module");
            } else if (var7 == 0) {
               a(var16 + 2.0F, var8, "No flavor found");
            } else {
               for (int var19 = 0; var19 < var7; var19++) {
                  var18 = this.a((Module)var6.get(var19), var18, 6.0F, 242.0F, this.searchExpansion, "s:", true);
               }

               if (var6.size() > var7) {
                  a(var16 + 2.0F, var18, "...and " + (var6.size() - var7) + " more");
               }
            }

            this.e(var1);
         }

         ImGui.end();
         this.c(var1);
      }
   }

   private static void a(float f, float g, String string) {
      a(string, f, g + (22.0F - ImGui.getTextLineHeight()) * 0.5F - 2.0F, 248.0F - f * 2.0F, -9738140);
   }

   private void c(float f) {
      float var2 = ImGui.getCursorScreenPosX();
      float var3 = ImGui.getCursorScreenPosY();
      ImDrawList var4 = ImGui.getWindowDrawList();
      DrawUtils.fillSlashed(var4, var2, var3, var2 + f, var3 + 28.0F, 4.0F, 7.0F, a(-15461614));
      ImGui.pushStyleVar(11, 28.0F, (28.0F - ImGui.getTextLineHeight()) * 0.5F);
      ImGui.pushStyleVar(13, 0.0F);
      DrawUtils.pushColor(7, 0.0F, 0.0F, 0.0F, 0.0F);
      DrawUtils.pushColor(8, 0.0F, 0.0F, 0.0F, 0.0F);
      DrawUtils.pushColor(9, 0.0F, 0.0F, 0.0F, 0.0F);
      ImGui.setNextItemWidth(f);
      ImGui.inputTextWithHint("##globalsearch", "Search modules...", this.globalSearch);
      ImGuiManager.getInstance().armTextInput();
      boolean var5 = ImGui.isItemActive();
      boolean var6 = ImGui.isItemHovered();
      ImGui.popStyleColor(3);
      ImGui.popStyleVar(2);
      float var7 = this.a("search:focus", var5 ? 1.0F : 0.0F, 16.0F);
      float var8 = this.a("search:hover", var6 ? 1.0F : 0.0F, 18.0F);
      int var9 = DrawUtils.lerp(DrawUtils.lerp(536870911, 1040187391, var8), AccentColor.accent(), var7);
      var4.pushClipRectFullScreen();
      if (var7 > 0.01F) {
         DrawUtils.glow(var4, var2, var3, var2 + f, var3 + 28.0F, 4.0F, 7.0F, AccentColor.accent(), var7 * 0.6F);
      }

      var4.popClipRect();
      DrawUtils.strokeSlashed(var4, var2 + 0.5F, var3 + 0.5F, var2 + f - 0.5F, var3 + 28.0F - 0.5F, 4.0F, 7.0F, var9, 1.0F);
      float var10 = var2 + 14.0F;
      float var11 = var3 + 14.0F - 1.0F;
      int var12 = DrawUtils.lerp(-9738140, AccentColor.accent(), var7);
      var4.addCircle(var10, var11, 4.5F, DrawUtils.f(var12), 16, 1.6F);
      var4.addLine(var10 + 3.4F, var11 + 3.4F, var10 + 7.0F, var11 + 7.0F, DrawUtils.f(var12), 1.8F);
   }

   private void g(String string) {
      float var2 = ImGui.getWindowPosX();
      float var3 = ImGui.getWindowPosY();
      float var4 = ImGui.getWindowWidth();
      float var5 = ImGui.getWindowHeight();
      ImDrawList var6 = ImGui.getWindowDrawList();
      var6.pushClipRectFullScreen();
      float var7 = f();
      DrawUtils.flatCard(var6, var2, var3, var4, var5, 8.0F, 12.0F, 1.0F, Math.max(var7, 0.15F));
      var6.popClipRect();
      float var8 = this.d(string);
      if (c() && !this.closing) {
         float var9 = d() ? var8 : DrawUtils.clamp01(this.openFxTime / 0.48F);
         if (var9 < 1.0F) {
            a(var6, string, var2, var3, var4, var5, var9);
         }
      }
   }

   private static void a(ImDrawList imDrawList, String string, float f, float g, float h, float i, float j) {
      int var7 = string.hashCode();
      float var8 = 1.0F - DrawUtils.smoothstep(j);
      imDrawList.pushClipRect(f + 2.0F, g + 2.0F, f + h - 2.0F, g + i - 2.0F, true);

      for (int var9 = 0; var9 < 16; var9++) {
         int var10 = var7 * 31 + var9 * 7919;
         float var11 = (var10 >>> 3 & 1023) / 1023.0F;
         float var12 = 0.7F + (var10 >>> 13 & 0xFF) / 255.0F * 0.8F;
         float var13 = (var10 >>> 21 & 0xFF) / 255.0F;
         float var14 = (j * var12 + var13) % 1.0F;
         float var15 = f + 10.0F + var11 * (h - 20.0F) + (float)Math.sin((j * 6.0F + var9) * 2.1F) * 3.0F;
         float var16 = g + i - var14 * (i + 10.0F);
         float var17 = 1.2F + (var10 >>> 5 & 7) / 7.0F * 2.4F;
         int var18 = Math.round(170.0F * var8 * (1.0F - var14 * 0.6F));
         imDrawList.addCircle(var15, var16, var17, DrawUtils.f(AccentColor.accentWithAlpha(var18)), 12, 1.0F);
         imDrawList.addCircleFilled(var15 - var17 * 0.3F, var16 - var17 * 0.3F, var17 * 0.35F, DrawUtils.f(DrawUtils.withAlpha(-1, Math.round(var18 * 0.7F))), 8);
      }

      imDrawList.popClipRect();
   }

   private boolean a(String string, String string2, int i, int j, float f, boolean bl) {
      float var7 = ImGui.getWindowPosX();
      float var8 = ImGui.getWindowPosY();
      float var9 = ImGui.getWindowWidth();
      ImDrawList var10 = ImGui.getWindowDrawList();
      boolean var11 = false;
      float var12 = 28.0F;
      ImGui.setCursorPos(0.0F, 0.0F);
      ImGui.invisibleButton("##drag_" + string, Math.max(1.0F, var9 - var12), 44.0F);
      boolean var13 = ImGui.isItemHovered();
      if (!g() && ImGui.isItemActive() && ImGui.isMouseDragging(0)) {
         ImVec2 var14 = ImGui.getIO().getMouseDelta();
         ImGui.setWindowPos(ImGui.getWindowPosX() + var14.x, ImGui.getWindowPosY() + var14.y);
      }

      if (var13 && ImGui.isMouseClicked(1)) {
         var11 = true;
      }

      float var31 = this.a("hdr:" + string, var13 ? 1.0F : 0.0F, 18.0F);
      float var15 = var8 + 22.0F;
      float var16 = var7 + 13.0F;
      float var17;
      if (bl) {
         int var18 = AccentColor.accent();
         var10.addCircle(var16 + 5.0F, var15 - 1.5F, 5.0F, DrawUtils.f(var18), 16, 2.0F);
         var10.addLine(var16 + 8.8F, var15 + 2.3F, var16 + 12.5F, var15 + 6.0F, DrawUtils.f(var18), 2.2F);
         var16 += 14.0F;
         var17 = 9.0F;
      } else {
         float var33 = 24.0F;
         float var19 = var15 - 3.3F;
         var16 += ClawTexture.draw(var10, var16, var19 - var33 * 0.5F - var31 * 0.5F + var33 * 0.09F, var33, AccentColor.accent());
         var17 = 6.0F;
      }

      ImFont var34 = ImGuiManager.getInstance().categoryFont();
      float var35 = 15.0F;
      DrawUtils.tracked(var10, var34, var35, var16 + var17, var15 - var35 * 0.58F, -526345, string2.toUpperCase(Locale.ROOT), 1.6F);
      ImGui.setCursorPos(var9 - var12, 0.0F);
      ImGui.invisibleButton("##chev_" + string, var12, 44.0F);
      if (ImGui.isItemClicked(0)) {
         var11 = true;
      }

      boolean var20 = ImGui.isItemHovered();
      if (var20 && ImGui.isMouseClicked(1)) {
         var11 = true;
      }

      float var21 = var7 + var9 - 16.0F;
      float var22 = (float)((-Math.PI / 2) * (1.0F - f));
      a(var10, var21, var15, 4.5F, var22, var20 ? AccentColor.accent() : -6119783);
      float var23 = var21 - 12.0F;
      if (i >= 0) {
         ImFont var24 = ImGuiManager.getInstance().hudLabelFont();
         float var25 = 11.0F;
         String var26 = String.valueOf(i);
         String var27 = "/" + j;
         float var28 = DrawUtils.textWidth(var24, var25, var27);
         float var29 = DrawUtils.textWidth(var24, var25, var26);
         float var30 = var23 - var28 - var29;
         DrawUtils.text(var10, var24, var25, var30, var15 - var25 * 0.62F, i > 0 ? AccentColor.accent() : -9738140, var26);
         DrawUtils.text(var10, var24, var25, var30 + var29, var15 - var25 * 0.62F, -9738140, var27);
      }

      if (f > 0.01F) {
         float var36 = var8 + 44.0F - 0.5F;
         var10.addLine(var7 + 12.0F, var36, var7 + var9 - 12.0F, var36, DrawUtils.f(DrawUtils.alpha(452984831, f)), 1.0F);
      }

      ImGui.setCursorPos(0.0F, 46.0F);
      return var11;
   }

   private static void a(ImDrawList imDrawList, float f, float g, float h, float i, int j) {
      float var6 = (float)Math.cos(i);
      float var7 = (float)Math.sin(i);
      float var8 = -h;
      float var9 = -h * 0.5F;
      float var10 = 0.0F;
      float var11 = h * 0.5F;
      float var13 = -h * 0.5F;
      float var14 = f + var8 * var6 - var9 * var7;
      float var15 = g + var8 * var7 + var9 * var6;
      float var16 = f + var10 * var6 - var11 * var7;
      float var17 = g + var10 * var7 + var11 * var6;
      float var18 = f + h * var6 - var13 * var7;
      float var19 = g + h * var7 + var13 * var6;
      imDrawList.addLine(var14, var15, var16, var17, DrawUtils.f(j), 1.8F);
      imDrawList.addLine(var16, var17, var18, var19, DrawUtils.f(j), 1.8F);
   }

   private static void m() {
      if (ImGui.isWindowHovered() && ImGui.isMouseDragging(0) && !ImGui.isAnyItemActive()) {
         ImGui.setScrollY(Math.max(0.0F, ImGui.getScrollY() - ImGui.getIO().getMouseDeltaY()));
      }
   }

   private float a(List<Module> list, OpenAnimation<Module> arg) {
      float var3 = 2.0F;

      for (Module var5 : list) {
         var3 += 32.0F + this.a(var5, arg);
      }

      return Math.max(40.0F, var3 + 2.0F);
   }

   private static void b(Module arg) {
      arg.toggle();
      UiSounds.toggle(arg.isEnabled());
   }

   private float a(Module arg, OpenAnimation<Module> arg2) {
      float var3 = arg2.factor(arg, this.frameNanos);
      return var3 <= 0.0F ? 0.0F : (float)Math.floor(d(arg) * var3);
   }

   private float a(Module arg, float f, float g, float h, OpenAnimation<Module> arg2, String string, boolean bl) {
      ImGui.pushID(string + arg.category().name() + ":" + arg.name());
      String var8 = string + arg.name();
      boolean var9 = g(arg);
      boolean var10 = arg instanceof ConfigModule;
      boolean var11 = var9 || var10;
      float var12 = h - g;
      float var13 = h - 8.0F - 28.0F;
      float var14 = var13 - 6.0F - 20.0F;
      ImGui.setCursorPos(g, f);
      float var15 = ImGui.getCursorScreenPosX();
      float var16 = ImGui.getCursorScreenPosY();
      ImGui.invisibleButton("##row", Math.max(1.0F, var14 - g), 30.0F);
      boolean var17 = ImGui.isItemHovered();
      if (var17 && ImGui.isMouseClicked(0) && !var11) {
         b(arg);
      }

      if (var17 && ImGui.isMouseClicked(1)) {
         UiSounds.drawer(arg2.toggle(arg, this.frameNanos));
      }

      ImGui.setCursorPos(var14, f);
      ImGui.invisibleButton("##gear", 20.0F, 30.0F);
      boolean var18 = ImGui.isItemClicked(0);
      boolean var19 = ImGui.isItemHovered();
      if (var18) {
         UiSounds.drawer(arg2.toggle(arg, this.frameNanos));
      }

      if (var19 && ImGui.isMouseClicked(1)) {
         UiSounds.drawer(arg2.toggle(arg, this.frameNanos));
      }

      ImGui.setCursorPos(var13 - 4.0F, f);
      ImGui.invisibleButton("##switch", 40.0F, 30.0F);
      boolean var20 = ImGui.isItemClicked(0);
      boolean var21 = ImGui.isItemHovered();
      if (var20 && !var11) {
         b(arg);
      }

      boolean var22 = var17 || var19 || var21;
      if (!var22 || this.closing) {
         this.hoveredRows.remove(var8);
      } else if (this.hoveredRows.add(var8)) {
         UiSounds.hover();
      }

      float var23 = this.a("h:" + var8, var22 ? 1.0F : 0.0F, 18.0F);
      float var24 = this.a("e:" + var8, var10 || !var9 && !arg.isEnabled() ? 0.0F : 1.0F, 14.0F);
      float var25 = arg2.factor(arg, this.frameNanos);
      ImDrawList var26 = ImGui.getWindowDrawList();
      float var28 = var16 + 30.0F;
      float var29 = var16 + 15.0F;
      boolean var30 = DrawUtils.smooth();
      if (var23 > 0.01F) {
         var26.addRectFilled(var15, var16, var15 + var12, var28, DrawUtils.f(DrawUtils.withAlpha(-1, Math.round(12.0F * var23))), var30 ? 9.0F : 5.0F);
      }

      if (var24 > 0.01F) {
         var26.addRectFilled(var15, var16 + 1.0F, var15 + var12, var28 - 1.0F, DrawUtils.f(AccentColor.accentWithAlpha(Math.round(30.0F * var24))), var30 ? 9.0F : 4.0F);
         float var31 = 16.0F * DrawUtils.easeOutCubic(var24);
         var26.addRectFilled(var15 + 4.0F, var29 - var31 * 0.5F, var15 + 7.0F, var29 + var31 * 0.5F, DrawUtils.f(AccentColor.accent()), 1.5F);
      }

      float var46 = ImGui.getTextLineHeight();
      float var32 = var15 + 12.0F + 3.0F * var24;
      int var33 = DrawUtils.lerp(DrawUtils.lerp(-6119783, -2435375, var23), -526345, var24);
      var26.addText(Math.round(var32), Math.round(var29 - var46 * 0.5F), DrawUtils.f(var33), arg.name());
      float var34 = var32 + ImGui.calcTextSize(arg.name()).x;
      ImFont var35 = ImGuiManager.getInstance().hudLabelFont();
      float var36 = var15 + (var14 - g) - 2.0F;
      if (bl) {
         String var37 = arg.category().displayName().toUpperCase(Locale.ROOT);
         float var38 = DrawUtils.trackedWidth(var35, 9.5F, var37, 0.8F);
         if (var34 + 8.0F + var38 < var36) {
            DrawUtils.tracked(var26, var35, 9.5F, var36 - var38, var29 - 5.5F, -9738140, var37, 0.8F);
            var36 -= var38 + 6.0F;
         }
      }

      if (arg.keybind() != -1) {
         String var47 = b(arg.keybind());
         float var49 = DrawUtils.textWidth(var35, 10.0F, var47) + 8.0F;
         if (var34 + 8.0F + var49 < var36) {
            float var39 = var36 - var49;
            var26.addRectFilled(var39, var29 - 7.5F, var39 + var49, var29 + 7.5F, DrawUtils.f(385875967), var30 ? 7.5F : 3.0F);
            DrawUtils.text(var26, var35, 10.0F, var39 + 4.0F, var29 - 6.0F, AccentColor.accentWithAlpha(210), var47);
         }
      }

      float var48 = this.a("g:" + var8, var19 ? 1.0F : 0.0F, 18.0F);
      float var50 = var15 + (var14 - g) + 10.0F;
      float var51 = (float)((Math.PI / 4) * var25);
      int var40 = DrawUtils.lerp(DrawUtils.lerp(-9738140, -526345, var48), AccentColor.accent(), var25);
      float var41 = 4.2F + var48 * 0.6F;
      float var42 = (float)Math.cos(var51) * var41;
      float var43 = (float)Math.sin(var51) * var41;
      var26.addLine(var50 - var42, var29 - var43, var50 + var42, var29 + var43, DrawUtils.f(var40), 1.7F);
      var26.addLine(var50 + var43, var29 - var42, var50 - var43, var29 + var42, DrawUtils.f(var40), 1.7F);
      a(var26, var15 + (var13 - g), var29, var24, var21 ? 1.0F : var23 * 0.5F);
      float var44 = f + 30.0F + 2.0F;
      float var45 = this.a(arg, arg2);
      if (var45 >= 1.0F) {
         this.a(arg, var44, var45, var25, g, h, arg2, "##drawer_" + string);
      }

      ImGui.popID();
      return var44 + var45;
   }

   private static void a(ImDrawList imDrawList, float f, float g, float h, float i) {
      float var5 = g - 7.0F;
      float var6 = 7.0F;
      int var7 = DrawUtils.lerp(DrawUtils.lerp(-14014169, -13356241, i), AccentColor.accent(), h);
      if (h > 0.01F) {
         imDrawList.addRectFilled(f - 2.5F, var5 - 2.5F, f + 28.0F + 2.5F, var5 + 14.0F + 2.5F, DrawUtils.f(AccentColor.accentWithAlpha(Math.round(40.0F * h))), var6 + 2.5F);
      }

      imDrawList.addRectFilled(f, var5, f + 28.0F, var5 + 14.0F, DrawUtils.f(var7), var6);
      float var8 = 14.0F;
      float var9 = f + var6 + var8 * h;
      float var10 = (float)Math.sin(Math.PI * DrawUtils.clamp01(h)) * 3.5F;
      float var11 = var6 - 2.5F;
      int var12 = DrawUtils.inkOnAccent() == -1 ? -1 : -16053493;
      int var13 = DrawUtils.lerp(-2500910, var12, h);
      imDrawList.addRectFilled(var9 - var11 - var10 * 0.5F, g - var11, var9 + var11 + var10 * 0.5F, g + var11, DrawUtils.f(var13), var11);
   }

   private static float c(Module arg) {
      float var1 = 4.0F;

      for (Setting var3 : arg.settings()) {
         if (var3.isVisible()) {
            if (var3 instanceof StringSetting) {
               var1 += 50.0F;
            } else if (var3 instanceof NumberSetting) {
               var1 += 42.0F;
            } else {
               var1 += 26.0F;
            }
         }
      }

      if (arg instanceof StorageEspModule var5) {
         var1 += 22.0F;
         if (var5.hasVisibleStorageColors()) {
            var1 += 22.0F;
         }
      }

      if (arg instanceof NotificationsModule) {
         var1 += 26.0F;
      }

      if (!g(arg)) {
         var1 += 26.0F;
      }

      if (arg instanceof NoRenderModule) {
         var1 += 4.0F + NoRenderModule.b.values().length * 26.0F;
      }

      if (e(arg)) {
         var1 += 56.0F;
      }

      if (arg instanceof HudModule var6 && var6.regionMap()) {
         var1 += 26.0F;
      }

      if (g(arg)) {
         var1 += 52.0F;
      }

      if (arg instanceof ChatMacrosModule) {
         var1 += 26.0F;
      }

      if (arg instanceof ConfigModule) {
         var1 += 26.0F;
      }

      if (arg instanceof PlayerEspModule) {
         var1 += 26.0F;
      }

      var1 += 26.0F;
      return var1 + 6.0F;
   }

   private static float d(Module arg) {
      return Math.min(c(arg), n());
   }

   private static float n() {
      float var0 = ImGui.getIO().getDisplaySizeY();
      return var0 <= 0.0F ? 420.0F : Math.max(180.0F, Math.min(420.0F, var0 - 180.0F));
   }

   private void a(Module arg, float f, float g, float h, float i, float j, OpenAnimation<Module> arg2, String string) {
      float var9 = j - i;
      ImGui.setCursorPos(i, f);
      float var10 = ImGui.getCursorScreenPosX();
      float var11 = ImGui.getCursorScreenPosY();
      ImDrawList var12 = ImGui.getWindowDrawList();
      var12.addRectFilled(var10, var11, var10 + var9, var11 + g, DrawUtils.f(-1979382523), DrawUtils.smooth() ? 10.0F : 5.0F);
      var12.addLine(var10 + 6.5F, var11 + 5.0F, var10 + 6.5F, var11 + Math.max(5.0F, g - 5.0F), DrawUtils.f(AccentColor.accentWithAlpha(Math.round(110.0F * h))), 1.2F);
      int var13 = 196616;
      if (!arg2.isOpen(arg) || this.closing) {
         var13 |= 197120;
      }

      ImGui.pushStyleVar(2, 0.0F, 0.0F);
      if (ImGui.beginChild(string + arg.name(), var9, g, false, var13)) {
         float var14 = var9 - 8.0F;
         ImGui.setCursorPosY(4.0F);
         boolean var15 = false;
         boolean var16 = false;

         for (Setting var18 : arg.settings()) {
            if (var18.isVisible()) {
               if (arg instanceof StorageEspModule var19) {
                  if (!var15 && var19.isTypeSetting(var18)) {
                     a("Storage Types", var14);
                     var15 = true;
                  }

                  if (!var16 && var19.isStorageColorSetting(var18)) {
                     a("Storage Colors", var14);
                     var16 = true;
                  }
               }

               ImGui.pushID(var18.name());
               this.a(arg, var18, var14);
               ImGui.popID();
               if (arg instanceof HudModule var37 && var18 == var37.armorHudSetting() && var37.armorHud()) {
                  this.d(var14);
               }

               if (arg instanceof HudModule var38 && var18 == var38.regionMapSetting() && var38.regionMap()) {
                  this.e(var14);
               }
            }
         }

         if (arg instanceof NotificationsModule var23) {
            float var31 = ImGui.getCursorPosY();
            ImGui.setCursorPos(16.0F, var31 + 3.0F);
            if (Widgets.valueChip("##notifpos", "Position", NotificationsModule.formatCornerLabel(var23.notificationCorner()), var14 - 16.0F, 20.0F)) {
               var23.notificationCornerSetting().next();
               ModuleConfigManager.getInstance().save();
            }

            ImGui.setCursorPosY(var31 + 26.0F);
         }

         if (!g(arg)) {
            this.a(arg, var14);
         }

         if (arg instanceof NoRenderModule var24) {
            ImGui.setCursorPosY(ImGui.getCursorPosY() + 4.0F);

            for (NoRenderModule.b var21 : NoRenderModule.b.values()) {
               float var22 = ImGui.getCursorPosY();
               ImGui.setCursorPos(16.0F, var22);
               if (Widgets.chipButton("##nr_" + var21.name(), ModalManager.noRenderCategoryLabel(var21), var14 - 16.0F, 22.0F, this.modals.isNoRenderOpen(var24, var21))) {
                  this.modals.openNoRender(var24, var21);
               }

               ImGui.setCursorPosY(var22 + 22.0F + 4.0F);
            }
         }

         if (arg instanceof PlayerEspModule var25) {
            float var33 = ImGui.getCursorPosY();
            int var40 = var25.friendNames().size();
            ImGui.setCursorPos(16.0F, var33 + 3.0F);
            if (Widgets.chipButton("##managefriends", var40 == 0 ? "Manage Friends" : "Manage Friends (" + var40 + ")", var14 - 16.0F, 20.0F, false)) {
               FriendsModal.open();
            }

            ImGui.setCursorPosY(var33 + 26.0F);
         }

         if (arg instanceof PlayerAlertModule var26) {
            float var34 = ImGui.getCursorPosY();
            int var41 = var26.whitelistNames().size();
            ImGui.setCursorPos(16.0F, var34 + 3.0F);
            if (Widgets.chipButton("##managewhitelist", var41 == 0 ? "Manage Whitelist" : "Manage Whitelist (" + var41 + ")", var14 - 16.0F, 20.0F, false)) {
               WhitelistModal.open();
            }

            ImGui.setCursorPosY(var34 + 26.0F);
         }

         if (arg instanceof ConfigModule) {
            float var27 = ImGui.getCursorPosY();
            ImGui.setCursorPos(16.0F, var27 + 3.0F);
            if (Widgets.chipButton("##manageconfigs", "Manage Configs", var14 - 16.0F, 20.0F, false)) {
               ConfigModal.open();
            }

            ImGui.setCursorPosY(var27 + 26.0F);
         }

         if (e(arg)) {
            this.f(var14);
         }

         if (arg instanceof AgarthaModule var28) {
            this.a(var28, var14);
         }

         if (arg instanceof ChatMacrosModule) {
            float var29 = ImGui.getCursorPosY();
            int var35 = MacroManager.getInstance().all().size();
            ImGui.setCursorPos(16.0F, var29 + 3.0F);
            if (Widgets.chipButton("##chatmacros", var35 == 0 ? "Manage Macros" : "Manage Macros (" + var35 + ")", var14 - 16.0F, 20.0F, false)) {
               MacroModal.openListModal();
            }

            ImGui.setCursorPosY(var29 + 26.0F);
         }

         if (arg instanceof SkinChangeModule var30) {
            float var36 = ImGui.getCursorPosY();
            ImGui.setCursorPos(16.0F, var36 + 3.0F);
            if (Widgets.chipButton("##skinapply", "Apply", var14 - 16.0F, 20.0F, false)) {
               var30.applyNow();
            }

            ImGui.setCursorPosY(var36 + 26.0F);
         }

         this.b(arg, var14);
         m();
      }

      ImGui.endChild();
      ImGui.popStyleVar();
   }

   private static void a(String string, float f) {
      float var2 = ImGui.getCursorPosY();
      ImDrawList var3 = ImGui.getWindowDrawList();
      float var4 = ImGui.getWindowPosX();
      float var5 = ImGui.getWindowPosY() - ImGui.getScrollY();
      ImFont var6 = ImGuiManager.getInstance().hudLabelFont();
      String var7 = string.toUpperCase(Locale.ROOT);
      float var8 = var5 + var2 + 6.0F;
      DrawUtils.tracked(var3, var6, 10.0F, var4 + 16.0F, var8, AccentColor.accentWithAlpha(200), var7, 1.2F);
      float var9 = var4 + 16.0F + DrawUtils.trackedWidth(var6, 10.0F, var7, 1.2F) + 8.0F;
      var3.addLine(var9, var8 + 5.5F, var4 + f, var8 + 5.5F, DrawUtils.f(452984831), 1.0F);
      ImGui.setCursorPosY(var2 + 22.0F);
   }

   private void a(Module arg, Setting arg2, float f) {
      float var4 = ImGui.getCursorPosY();
      float var5 = ImGui.getTextLineHeight();
      if (arg2 instanceof ColorSetting var27) {
         a(arg2.name(), 16.0F, var4 + (26.0F - var5) * 0.5F, f - 16.0F - 30.0F, -2435375);
         float var36 = 16.0F;
         ImGui.setCursorPos(f - var36 - 1.0F, var4 + (26.0F - var36) * 0.5F);
         float var42 = ImGui.getCursorScreenPosX();
         float var45 = ImGui.getCursorScreenPosY();
         ImGui.invisibleButton("##color", var36, var36);
         if (ImGui.isItemClicked(0)) {
            this.modals.openColor(arg, var27);
         }

         boolean var48 = ImGui.isItemHovered();
         float[] var49 = var27.value();
         int var12 = Math.round(var49[0] * 255.0F);
         int var13 = Math.round(var49[1] * 255.0F);
         int var14 = Math.round(var49[2] * 255.0F);
         int var15 = Palette.rgbAbgr(var12, var13, var14, var49.length > 3 ? Math.round(var49[3] * 255.0F) : 255);
         ImDrawList var16 = ImGui.getWindowDrawList();
         if (var48) {
            var16.addCircleFilled(var42 + var36 * 0.5F, var45 + var36 * 0.5F, var36 * 0.5F + 3.0F, DrawUtils.f(587202559), 24);
         }

         var16.addCircleFilled(var42 + var36 * 0.5F, var45 + var36 * 0.5F, var36 * 0.5F, DrawUtils.f(var15), 24);
         var16.addCircle(var42 + var36 * 0.5F, var45 + var36 * 0.5F, var36 * 0.5F, DrawUtils.f(1728053247), 24, 1.2F);
         if (var48) {
            ImGui.beginTooltip();
            ImGui.textUnformatted(String.format(Locale.ROOT, "#%02X%02X%02X", var12, var13, var14));
            ImGui.endTooltip();
         }

         ImGui.setCursorPosY(var4 + 26.0F);
      } else if (arg2 instanceof BooleanSetting var26) {
         ImGui.setCursorPos(16.0F, var4);
         ImGui.invisibleButton("##blabel", Math.max(1.0F, f - 16.0F - 15.0F - 6.0F), 26.0F);
         boolean var35 = ImGui.isItemClicked(0);
         boolean var41 = ImGui.isItemHovered();
         a(arg2.name(), 16.0F, var4 + (26.0F - var5) * 0.5F, f - 16.0F - 15.0F - 10.0F, var41 ? -526345 : -2435375);
         ImGui.setCursorPos(f - 15.0F, var4 + 5.5F);
         boolean var44 = Widgets.checkbox("##bcheck", var26.value().get(), 15.0F);
         if (var35) {
            UiSounds.toggle(!var26.value().get());
         }

         if (var35 || var44) {
            var26.value().set(!var26.value().get());
            var26.notifyChanged();
         }

         ImGui.setCursorPosY(var4 + 26.0F);
      } else if (arg2 instanceof NumberSetting var25) {
         float var34 = Math.max(52.0F, ImGui.calcTextSize(var25.display()).x + 6.0F);
         a(arg2.name(), 16.0F, var4 + 3.0F, f - 16.0F - var34 - 6.0F, -6119783);
         this.a(var25, f, var4 + 3.0F, var34);
         ImGui.setCursorPos(16.0F, var4 + 22.0F);
         if (Widgets.sliderFloat("##slider", var25.value(), var25.min(), var25.max(), f - 16.0F)) {
            this.editingNumberSetting = null;
            var25.settle();
            var25.notifyChanged();
         }

         ImGui.setCursorPosY(var4 + 42.0F);
      } else if (arg2 instanceof EnumSetting var6 && arg instanceof AgarthaModule var7 && var6 == var7.theme()) {
         ImGui.setCursorPos(16.0F, var4 + 3.0F);
         if (Widgets.valueChip("##theme", MonsterModeModule.isActive() ? "Flavor" : "Theme", var6.get(), f - 16.0F, 20.0F)) {
            this.modals.openTheme(var6);
         }

         ImGui.setCursorPosY(var4 + 26.0F);
      } else if (arg2 instanceof BlockColorSetting var24) {
         long var33 = var24.entries().stream().filter(argx -> argx.enabled().get()).count();
         ImGui.setCursorPos(16.0F, var4 + 3.0F);
         if (Widgets.valueChip("##bcedit", "Block Colors", var33 + " active", f - 16.0F, 20.0F)) {
            this.modals.openBlockPalette(var24);
         }

         ImGui.setCursorPosY(var4 + 26.0F);
      } else if (arg2 instanceof EntityTypeListSetting var23) {
         ImGui.setCursorPos(16.0F, var4 + 3.0F);
         if (Widgets.valueChip("##moblist", "Mobs", var23.summary(), f - 16.0F, 20.0F)) {
            this.modals.openMobList(var23);
         }

         ImGui.setCursorPosY(var4 + 26.0F);
      } else if (arg2 instanceof ItemColorSetting var22) {
         long var32 = var22.entries().stream().filter(argx -> argx.enabled().get()).count();
         ImGui.setCursorPos(16.0F, var4 + 3.0F);
         if (Widgets.valueChip("##icedit", "Item Colors", var32 + " active", f - 16.0F, 20.0F)) {
            this.modals.openItemPalette(var22);
         }

         ImGui.setCursorPosY(var4 + 26.0F);
      } else if (arg2 instanceof ItemIdListSetting var21) {
         int var31 = 0;

         for (String var43 : var21.selectedIds()) {
            if (var31 >= this.chipIcons.length) {
               break;
            }

            Item var47 = ItemLookup.item(var43);
            if (var47 != null) {
               this.chipIcons[var31++] = var47;
            }
         }

         int var40 = var21.size();
         ImGui.setCursorPos(16.0F, var4 + 3.0F);
         if (Widgets.itemChip("##itemlist", arg2.name(), var40 == 0 ? "None" : String.valueOf(var40), this.chipIcons, var31, f - 16.0F, 20.0F)) {
            this.modals.openItemList(var21);
         }

         ImGui.setCursorPosY(var4 + 26.0F);
      } else if (arg2 instanceof ItemIdSetting var20) {
         Item var30 = ItemLookup.item(var20.id());
         this.chipIcons[0] = var30;
         String var38 = var30 == null ? var20.id() : ItemLookup.name(var30);
         ImGui.setCursorPos(16.0F, var4 + 3.0F);
         if (Widgets.itemChip("##itempick", arg2.name(), var38, this.chipIcons, var30 == null ? 0 : 1, f - 16.0F, 20.0F)) {
            this.modals.openItemPick(var20);
         }

         ImGui.setCursorPosY(var4 + 26.0F);
      } else if (arg2 instanceof EnumSetting var19) {
         ImGui.setCursorPos(16.0F, var4 + 3.0F);
         if (Widgets.valueChip("##mode", arg2.name(), var19.value(), f - 16.0F, 20.0F)) {
            var19.next();
         }

         ImGui.setCursorPosY(var4 + 26.0F);
      } else if (!(arg2 instanceof KeybindSetting var17)) {
         if (arg2 instanceof StringSetting var18) {
            a(arg2.name(), 16.0F, var4 + 4.0F, f - 16.0F, -6119783);
            ImGui.setCursorPos(16.0F, var4 + 22.0F);
            ImGui.pushStyleVar(12, DrawUtils.smooth() ? 8.0F : 4.0F);
            ImGui.pushStyleVar(11, 7.0F, 4.0F);
            ImGui.pushStyleVar(13, 1.0F);
            DrawUtils.pushColor(7, 0.085F, 0.082F, 0.078F, 1.0F);
            DrawUtils.pushColor(8, 0.12F, 0.115F, 0.11F, 1.0F);
            DrawUtils.pushColor(9, 0.1F, 0.095F, 0.09F, 1.0F);
            float[] var29 = Palette.accentF();
            DrawUtils.pushColor(5, var29[0], var29[1], var29[2], 0.35F);
            ImGui.setNextItemWidth(f - 16.0F);
            if (ImGui.inputText("##str", var18.getImString())) {
               var18.notifyChanged();
            }

            ImGuiManager.getInstance().armTextInput();
            ImGui.popStyleColor(4);
            ImGui.popStyleVar(3);
            ImGui.setCursorPosY(var4 + 50.0F);
         }
      } else {
         boolean var28 = arg instanceof AgarthaModule var8 && var17 == var8.clickGuiKeySetting();
         boolean var37 = this.listeningKeySetting == var17;
         boolean var9 = false;
         if (var37) {
            if (this.listeningKeyFirstFrame) {
               this.listeningKeyFirstFrame = false;
               this.b(var28);
            } else {
               int var10 = this.c(var28);
               if (var10 == -2) {
                  this.listeningKeySetting = null;
                  var37 = false;
                  var9 = true;
               } else if (var10 == 256) {
                  var17.setValue(-1);
                  this.listeningKeySetting = null;
                  var37 = false;
               } else if (var10 != -1) {
                  var17.setValue(var10);
                  this.listeningKeySetting = null;
                  var37 = false;
               }
            }
         }

         float var46 = 72.0F;
         a(arg2.name(), 16.0F, var4 + (26.0F - var5) * 0.5F, f - 16.0F - var46 - 8.0F, -2435375);
         ImGui.setCursorPos(f - var46, var4 + 3.0F);
         String var11 = var37 ? "..." : (var17.value() == -1 ? "None" : b(var17.value()));
         if (Widgets.chipButton("##keyval", var11, var46, 20.0F, var37) && !var9) {
            if (var37) {
               this.listeningKeySetting = null;
            } else {
               this.listeningKeySetting = var17;
               this.listeningKeyFirstFrame = true;
               this.listeningModule = null;
               this.listeningFirstFrame = false;
            }
         }

         ImGui.setCursorPosY(var4 + 26.0F);
      }
   }

   private static void a(String string, float f, float g, float h, int i) {
      if (!(h <= 1.0F)) {
         String var5 = DrawUtils.ellipsize(ImGui.getFont(), ImGui.getFontSize(), string, h);
         float var6 = ImGui.getWindowPosX() + f;
         float var7 = ImGui.getWindowPosY() + g - ImGui.getScrollY();
         ImGui.getWindowDrawList().addText(Math.round(var6), Math.round(var7), DrawUtils.f(i), var5);
      }
   }

   private void a(NumberSetting arg, float f, float g, float h) {
      float var5 = f - h;
      if (this.editingNumberSetting != arg) {
         String var13 = arg.display();
         float var14 = ImGui.calcTextSize(var13).x;
         ImGui.setCursorPos(var5, g - 2.0F);
         ImGui.invisibleButton("##numval", h, ImGui.getTextLineHeight() + 4.0F);
         if (ImGui.isItemClicked(0)) {
            this.editingNumberSetting = arg;
            this.editingNumberText.set(arg.editText());
         }

         boolean var15 = ImGui.isItemHovered();
         ImDrawList var9 = ImGui.getWindowDrawList();
         float var10 = ImGui.getWindowPosX();
         float var11 = ImGui.getWindowPosY() - ImGui.getScrollY();
         if (var15) {
            var9.addRectFilled(var10 + var5, var11 + g - 2.0F, var10 + f, var11 + g + ImGui.getTextLineHeight() + 2.0F, DrawUtils.f(AccentColor.accentWithAlpha(36)), 3.0F);
         }

         var9.addText(Math.round(var10 + f - var14 - 3.0F), Math.round(var11 + g), DrawUtils.f(AccentColor.accent()), var13);
      } else {
         ImGui.setCursorPos(var5, g - 2.0F);
         ImGui.pushItemWidth(h);
         ImGui.pushStyleVar(11, 4.0F, 1.0F);
         ImGui.setKeyboardFocusHere();
         short var6 = 4161;
         boolean var7 = ImGui.inputText("##numedit", this.editingNumberText, var6);
         ImGuiManager.getInstance().armTextInput();
         boolean var8 = ImGui.isItemDeactivatedAfterEdit();
         ImGui.popStyleVar();
         ImGui.popItemWidth();
         if (var7 || var8) {
            try {
               arg.set(Float.parseFloat(this.editingNumberText.get().trim().replace(',', '.')));
               arg.notifyChanged();
            } catch (NumberFormatException var12) {
            }

            this.editingNumberSetting = null;
         }
      }
   }

   private void a(Module arg, float f) {
      float var3 = ImGui.getCursorPosY();
      boolean var4 = this.listeningModule == arg;
      boolean var5 = false;
      if (var4) {
         if (this.listeningFirstFrame) {
            this.listeningFirstFrame = false;
            this.b(false);
         } else {
            int var6 = this.c(false);
            if (var6 == -2) {
               this.listeningModule = null;
               var4 = false;
               var5 = true;
            } else if (var6 == 256) {
               arg.setKeybind(-1);
               this.listeningModule = null;
               var4 = false;
            } else if (var6 != -1) {
               arg.setKeybind(var6);
               this.listeningModule = null;
               var4 = false;
            }
         }
      }

      float var11 = 66.0F;
      float var7 = 58.0F;
      float var8 = 6.0F;
      a("Bind", 16.0F, var3 + (26.0F - ImGui.getTextLineHeight()) * 0.5F, f - 16.0F - var11 - var7 - var8 * 2.0F, -6119783);
      float var9 = var3 + 3.0F;
      ImGui.setCursorPos(f - var11 - var8 - var7, var9);
      if (Widgets.chipButton("##bindmode", arg.isHoldMode() ? "Hold" : "Toggle", var7, 20.0F, arg.isHoldMode())) {
         arg.setHoldMode(!arg.isHoldMode());
      }

      ImGui.setCursorPos(f - var11, var9);
      String var10 = var4 ? "..." : (arg.keybind() == -1 ? "None" : b(arg.keybind()));
      if (Widgets.chipButton("##bind", var10, var11, 20.0F, var4) && !var5) {
         if (var4) {
            this.listeningModule = null;
         } else {
            this.listeningModule = arg;
            this.listeningFirstFrame = true;
            this.listeningKeySetting = null;
            this.listeningKeyFirstFrame = false;
         }
      }

      ImGui.setCursorPosY(var3 + 26.0F);
   }

   private void b(Module arg, float f) {
      float var3 = ImGui.getCursorPosY();
      float var4 = var3 + 3.0F;
      float var5 = f - 16.0F;
      if (this.pendingResetModule == arg) {
         a("##modreset", 16.0F, var4, var5, 20.0F, () -> {
            this.pendingResetModule = null;
            arg.resetToDefaults();
            ModuleConfigManager.getInstance().save();
         }, () -> this.pendingResetModule = null);
      } else {
         float var6 = (var5 - 6.0F) * 0.5F;
         String var7 = arg.description() != null && !arg.description().isBlank() ? arg.description() : arg.name();
         ImGui.setCursorPos(16.0F, var4);
         Widgets.chipButton("##modinfo", "Info", var6, 20.0F, false, var7);
         ImGui.setCursorPos(16.0F + var6 + 6.0F, var4);
         if (Widgets.chipButton("##modreset", "Reset", var6, 20.0F, false)) {
            this.pendingResetModule = arg;
            this.pendingConfirm = ClickGui.b.NONE;
         }
      }

      ImGui.setCursorPosY(var3 + 26.0F);
   }

   private void d(float f) {
      float var2 = ImGui.getCursorPosY();
      ImGui.setCursorPos(16.0F, var2 + 3.0F);
      if (Widgets.chipButton("##armorhudsettings", "Armor HUD Settings", f - 16.0F, 20.0F, this.modals.isArmorHudOpen())) {
         this.modals.openArmorHud();
      }

      ImGui.setCursorPosY(var2 + 26.0F);
   }

   private void e(float f) {
      float var2 = ImGui.getCursorPosY();
      ImGui.setCursorPos(16.0F, var2 + 3.0F);
      if (Widgets.chipButton("##regionmapsettings", "Region Map Settings", f - 16.0F, 20.0F, this.modals.isRegionMapOpen())) {
         this.modals.openRegionMap();
      }

      ImGui.setCursorPosY(var2 + 26.0F);
   }

   private void f(float f) {
      ImGui.setCursorPosY(ImGui.getCursorPosY() + 4.0F);
      float var2 = ImGui.getCursorPosY();
      float var3 = f - 16.0F;
      ImGui.setCursorPos(16.0F, var2 + 3.0F);
      if (Widgets.chipButton("##editlayout", "Edit Layout", var3, 20.0F, false)) {
         this.pendingConfirm = ClickGui.b.NONE;
         MinecraftClient.getInstance().setScreen(new HudLayoutScreen(MinecraftClient.getInstance().currentScreen));
      }

      float var4 = var2 + 26.0F;
      float var5 = var4 + 3.0F;
      if (this.pendingConfirm == ClickGui.b.RESET_LAYOUT) {
         a("##resetlayout", 16.0F, var5, var3, 20.0F, () -> {
            this.pendingConfirm = ClickGui.b.NONE;
            o();
         }, () -> this.pendingConfirm = ClickGui.b.NONE);
      } else {
         ImGui.setCursorPos(16.0F, var5);
         if (Widgets.chipButton("##resetlayout", "Reset Layout", var3, 20.0F, false)) {
            this.pendingConfirm = ClickGui.b.RESET_LAYOUT;
            this.pendingResetModule = null;
         }
      }

      ImGui.setCursorPosY(var4 + 26.0F);
   }

   private void a(AgarthaModule agarthaModule, float f) {
      float var3 = ImGui.getCursorPosY();
      float var4 = f - 16.0F;
      float var5 = var3 + 3.0F;
      if (this.pendingConfirm == ClickGui.b.RESET_CLIENT) {
         a("##clientreset", 16.0F, var5, var4, 20.0F, () -> {
            this.pendingConfirm = ClickGui.b.NONE;
            agarthaModule.resetClientToFactoryDefaults();
         }, () -> this.pendingConfirm = ClickGui.b.NONE);
      } else {
         ImGui.setCursorPos(16.0F, var5);
         if (Widgets.chipButton("##clientreset", "Reset Client", var4, 20.0F, false)) {
            this.pendingConfirm = ClickGui.b.RESET_CLIENT;
            this.pendingResetModule = null;
         }
      }

      float var6 = var3 + 26.0F;
      float var7 = var6 + 3.0F;
      if (this.pendingConfirm == ClickGui.b.RESET_CLICK_GUI) {
         a("##clickguireset", 16.0F, var7, var4, 20.0F, () -> {
            this.pendingConfirm = ClickGui.b.NONE;
            ClickGuiLayout var1 = ClickGuiLayout.getInstance();
            var1.resetToDefaults();
            this.appliedLayoutEpoch = var1.layoutEpoch() - 1;
            this.forceLayoutThisFrame = true;
         }, () -> this.pendingConfirm = ClickGui.b.NONE);
      } else {
         ImGui.setCursorPos(16.0F, var7);
         if (Widgets.chipButton("##clickguireset", "Reset Click GUI Layout", var4, 20.0F, false)) {
            this.pendingConfirm = ClickGui.b.RESET_CLICK_GUI;
            this.pendingResetModule = null;
         }
      }

      ImGui.setCursorPosY(var6 + 26.0F);
   }

   private static void a(String string, float f, float g, float h, float i, Runnable runnable, Runnable runnable2) {
      float var7 = (h - 6.0F) * 0.5F;
      ImGui.setCursorPos(f, g);
      if (Widgets.chipButton(string + "_yes", "Confirm", var7, i, true)) {
         runnable.run();
      }

      ImGui.setCursorPos(f + var7 + 6.0F, g);
      if (Widgets.chipButton(string + "_no", "Cancel", var7, i, false)) {
         runnable2.run();
      }
   }

   private static void o() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      HudLayout.getInstance().resetToDefaults(UiScale.width(var0), UiScale.height(var0));
      RegionMapModule var1 = ModuleManager.get(RegionMapModule.class);
      if (var1 != null) {
         var1.cellSizeSetting().resetToDefault();
         var1.cellGapSetting().resetToDefault();
         var1.textScaleSetting().resetToDefault();
      }

      SpotifyHudModule var2 = ModuleManager.get(SpotifyHudModule.class);
      if (var2 != null) {
         var2.scaleSetting().resetToDefault();
      }

      ModuleConfigManager.getInstance().save();
   }

   public boolean handleEscapeKey() {
      if (this.closing) {
         return true;
      } else if (this.modals.handleEscape()) {
         return true;
      } else if (this.editingNumberSetting != null) {
         this.editingNumberSetting = null;
         return true;
      } else if (this.listeningKeySetting != null) {
         this.listeningKeySetting.setValue(-1);
         this.listeningKeySetting = null;
         this.listeningKeyFirstFrame = false;
         return true;
      } else if (this.listeningModule != null) {
         this.listeningModule.setKeybind(-1);
         this.listeningModule = null;
         this.listeningFirstFrame = false;
         return true;
      } else if (this.pendingConfirm != ClickGui.b.NONE) {
         this.pendingConfirm = ClickGui.b.NONE;
         return true;
      } else if (this.pendingResetModule != null) {
         this.pendingResetModule = null;
         return true;
      } else {
         return false;
      }
   }

   public void dismissTransientState() {
      this.modals.dismiss();
      this.editingNumberSetting = null;
      this.listeningKeySetting = null;
      this.listeningKeyFirstFrame = false;
      this.listeningModule = null;
      this.listeningFirstFrame = false;
      this.pendingConfirm = ClickGui.b.NONE;
      this.pendingResetModule = null;
   }

   public boolean isListeningForKeybind() {
      return this.listeningModule != null || this.listeningKeySetting != null;
   }

   private float a(String string, float f, float g) {
      float var4 = Math.max(ImGui.getIO().getDeltaTime(), 0.001F);
      Float var5 = this.anim.get(string);
      float var6 = var5 == null ? f : DrawUtils.approach(var5, f, var4, g);
      this.anim.put(string, var6);
      return var6;
   }

   private void b(boolean bl) {
      Arrays.fill(this.heldWhenListening, false);
      long var2 = MinecraftClient.getInstance().getWindow().getHandle();

      for (int var4 = 32; var4 <= 348 && var4 < this.heldWhenListening.length; var4++) {
         if (bl || !Keybind.isShiftKey(var4)) {
            this.heldWhenListening[var4] = GLFW.glfwGetKey(var2, var4) == 1;
         }
      }

      for (int var6 = 0; var6 <= 7; var6++) {
         int var5 = 1000 + var6;
         if (var5 < this.heldWhenListening.length) {
            this.heldWhenListening[var5] = GLFW.glfwGetMouseButton(var2, var6) == 1;
         }
      }
   }

   private int c(boolean bl) {
      long var2 = MinecraftClient.getInstance().getWindow().getHandle();

      for (int var4 = 32; var4 <= 348; var4++) {
         if (bl || !Keybind.isShiftKey(var4)) {
            boolean var5 = GLFW.glfwGetKey(var2, var4) == 1;
            if (var4 < this.heldWhenListening.length && !var5) {
               this.heldWhenListening[var4] = false;
            }

            if (var5 && (var4 >= this.heldWhenListening.length || !this.heldWhenListening[var4])) {
               return var4;
            }
         }
      }

      for (int var7 = 0; var7 <= 7; var7++) {
         int var8 = 1000 + var7;
         boolean var6 = GLFW.glfwGetMouseButton(var2, var7) == 1;
         if (var8 < this.heldWhenListening.length && !var6) {
            this.heldWhenListening[var8] = false;
         }

         if (var6 && (var8 >= this.heldWhenListening.length || !this.heldWhenListening[var8])) {
            return Keybind.isClickButton(var8) ? -2 : var8;
         }
      }

      return -1;
   }

   private static String b(int i) {
      if (i >= 1000) {
         return switch (i - 1000) {
            case 0 -> "LMB";
            case 1 -> "RMB";
            case 2 -> "MMB";
            default -> "MB" + (i - 1000 + 1);
         };
      } else {
         if (i >= 39 && i <= 96 || i >= 161 && i <= 162) {
            String var1 = GLFW.glfwGetKeyName(i, -1);
            if (var1 != null && !var1.isBlank()) {
               return var1.toUpperCase(Locale.ROOT);
            }
         }

         if (i >= 290 && i <= 314) {
            return "F" + (i - 290 + 1);
         } else {
            return switch (i) {
               case 32 -> "SPACE";
               case 256 -> "ESC";
               case 257 -> "ENTER";
               case 258 -> "TAB";
               case 259 -> "BKSP";
               case 260 -> "INS";
               case 261 -> "DEL";
               case 262 -> "RIGHT";
               case 263 -> "LEFT";
               case 264 -> "DOWN";
               case 265 -> "UP";
               case 266 -> "PGUP";
               case 267 -> "PGDN";
               case 268 -> "HOME";
               case 269 -> "END";
               case 335 -> "NUM ENTER";
               case 340 -> "LSHIFT";
               case 341, 345 -> "CTRL";
               case 342, 346 -> "ALT";
               case 344 -> "RSHIFT";
               default -> "K" + i;
            };
         }
      }
   }

   private static boolean e(Module arg) {
      return arg instanceof HudModule;
   }

   private static boolean f(Module arg) {
      return arg instanceof RegionMapModule || arg instanceof SpotifyHudModule;
   }

   private static boolean g(Module arg) {
      return arg instanceof AgarthaModule;
   }

   private static void p() {
      boolean var0 = DrawUtils.smooth();
      ImGui.pushStyleVar(3, var0 ? 14.0F : 8.0F);
      ImGui.pushStyleVar(12, var0 ? 8.0F : 4.0F);
      ImGui.pushStyleVar(21, var0 ? 8.0F : 4.0F);
      ImGui.pushStyleVar(2, 12.0F, 10.0F);
      ImGui.pushStyleVar(14, 8.0F, 4.0F);
      ImGui.pushStyleVar(4, 0.0F);
      float[] var1 = Palette.TEXT_F;
      float[] var2 = Palette.TEXT_DISABLED_F;
      float[] var3 = Palette.WINDOW_BG_F;
      float[] var4 = Palette.CHILD_BG_F;
      float[] var5 = Palette.borderF();
      float[] var6 = Palette.accentF();
      float[] var7 = Palette.FRAME_BG_F;
      float[] var8 = Palette.FRAME_HOV_F;
      float[] var9 = Palette.TITLE_BG_F;
      float[] var10 = Palette.TITLE_ACTIVE_F;
      DrawUtils.pushColor(0, var1[0], var1[1], var1[2], var1[3]);
      DrawUtils.pushColor(1, var2[0], var2[1], var2[2], var2[3]);
      DrawUtils.pushColor(2, var3[0], var3[1], var3[2], var3[3]);
      DrawUtils.pushColor(3, var4[0], var4[1], var4[2], var4[3]);
      DrawUtils.pushColor(5, var5[0], var5[1], var5[2], var5[3]);
      DrawUtils.pushColor(10, var9[0], var9[1], var9[2], var9[3]);
      DrawUtils.pushColor(11, var10[0], var10[1], var10[2], var10[3]);
      DrawUtils.pushColor(12, var9[0], var9[1], var9[2], var9[3]);
      DrawUtils.pushColor(7, var7[0], var7[1], var7[2], var7[3]);
      DrawUtils.pushColor(8, var8[0], var8[1], var8[2], var8[3]);
      DrawUtils.pushColor(9, var8[0], var8[1], var8[2], var8[3]);
      DrawUtils.pushColor(19, var6[0], var6[1], var6[2], var6[3]);
      DrawUtils.pushColor(20, var6[0], var6[1], var6[2], 1.0F);
      DrawUtils.pushColor(18, var6[0], var6[1], var6[2], 1.0F);
      DrawUtils.pushColor(21, var7[0], var7[1], var7[2], var7[3]);
      DrawUtils.pushColor(22, var8[0], var8[1], var8[2], var8[3]);
      DrawUtils.pushColor(23, var6[0], var6[1], var6[2], 0.6F);
      DrawUtils.pushColor(24, var7[0], var7[1], var7[2], var7[3]);
      DrawUtils.pushColor(25, var8[0], var8[1], var8[2], var8[3]);
      DrawUtils.pushColor(27, var5[0], var5[1], var5[2], var5[3]);
   }

   private static void q() {
      ImGui.popStyleColor(20);
      ImGui.popStyleVar(6);
   }

   private static boolean a(ImFont imFont) {
      if (imFont == null) {
         return false;
      } else {
         ImGui.pushFont(imFont);
         return true;
      }
   }

   @Environment(EnvType.CLIENT)
   private record a(String id, float x, float y) {
   }

   @Environment(EnvType.CLIENT)
   private static enum b {
      NONE,
      RESET_LAYOUT,
      RESET_CLIENT,
      RESET_CLICK_GUI;
   }
}
