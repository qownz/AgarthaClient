package net.agartha.client.e;

import imgui.ImDrawList;
import imgui.ImFont;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import net.agartha.client.module.impl.client.AgarthaModule;
import net.agartha.client.module.impl.client.HudModule;
import net.agartha.client.module.impl.client.MonsterModeModule;
import net.agartha.client.module.impl.client.RegionMapModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;

@Environment(EnvType.CLIENT)
final class i {
   private static final float THEME_MODAL_W = 520.0F;
   private static final float HEX_MODAL_W = 405.0F;
   private static final float HEX_MODAL_H = 424.0F;
   private static final float PALETTE_MODAL_W = 630.0F;
   private static final float PALETTE_MODAL_H = 750.0F;
   private static final float NO_RENDER_MODAL_W = 440.0F;
   private static final float NO_RENDER_MODAL_H = 520.0F;
   private static final float ROW_H = 26.0F;
   private static final float CHECK = 15.0F;
   private static final float MODAL_INTRO_SECONDS = 0.18F;
   private static final float MODAL_INTRO_RISE = 18.0F;
   private final BooleanSupplier animationsEnabled;
   private float modalIntroTime;
   private boolean modalOpenLastFrame;
   private boolean themeWindowOpen;
   private String themeSnapshot;
   private final float[] themeCardHover = new float[k.values().length];
   private final ImBoolean colorPickerOpen = new ImBoolean(false);
   private net.agartha.client.module.b activeColorModule;
   private net.agartha.client.module.a_pkg.c activeColorSetting;
   private float[] colorPickerSnapshot;
   private net.agartha.client.module.a_pkg.a_o activeBlockColorPalette;
   private boolean blockColorWindowOpen;
   private final ImString blockSearchBuf = new ImString(256);
   private net.agartha.client.module.a_pkg.a_o.a activeBlockColorEntry;
   private boolean editingBlockDefaultColor;
   private final ImBoolean blockColorPickerOpen = new ImBoolean(false);
   private float[] blockColorPickerSnapshot;
   private net.agartha.client.module.a_pkg.d activeItemColorPalette;
   private boolean itemColorWindowOpen;
   private final ImString itemSearchBuf = new ImString(256);
   private net.agartha.client.module.a_pkg.d.a activeItemColorEntry;
   private boolean editingItemDefaultColor;
   private final ImBoolean itemColorPickerOpen = new ImBoolean(false);
   private float[] itemColorPickerSnapshot;
   private net.agartha.client.module.a_pkg.h activeMobList;
   private boolean mobListOpen;
   private final ImString mobSearchBuf = new ImString(64);
   private static final float ITEM_MODAL_W = 520.0F;
   private static final float ITEM_MODAL_H = 640.0F;
   private static final float ITEM_ROW_H = 30.0F;
   private static final float ITEM_ICON = 22.0F;
   private net.agartha.client.module.a_pkg.e activeItemList;
   private boolean itemListOpen;
   private net.agartha.client.module.a_pkg.f activeItemPick;
   private boolean itemPickOpen;
   private final ImString itemPickSearch = new ImString(64);
   private List<ad.a> itemView = List.of();
   private String itemViewKey;
   private int itemSelectionVersion;
   private net.agartha.client.module.impl.render.m activeNoRenderModule;
   private net.agartha.client.module.impl.render.m.b activeNoRenderCategory;
   private boolean noRenderModalOpen;
   private boolean regionMapModalOpen;
   private boolean armorHudModalOpen;
   private static final float REGION_MODAL_W = 380.0F;
   private boolean swatchClicked;
   private static final float PALETTE_ICON = 20.0F;

   i(BooleanSupplier booleanSupplier) {
      this.animationsEnabled = booleanSupplier;
   }

   boolean anyOpen() {
      return this.themeWindowOpen
         || this.noRenderModalOpen
         || this.regionMapModalOpen
         || this.armorHudModalOpen
         || g.isModalOpen()
         || l.isModalOpen()
         || p.isModalOpen()
         || am.isModalOpen()
         || this.blockColorPickerOpen.get()
         || this.itemColorPickerOpen.get()
         || this.colorPickerOpen.get()
         || this.blockColorWindowOpen
         || this.itemColorWindowOpen
         || this.mobListOpen
         || this.itemListOpen
         || this.itemPickOpen;
   }

   boolean isThemeOpen() {
      return this.themeWindowOpen;
   }

   boolean isBlockPaletteOpen() {
      return this.blockColorWindowOpen;
   }

   boolean isItemPaletteOpen() {
      return this.itemColorWindowOpen;
   }

   boolean isNoRenderOpen(net.agartha.client.module.impl.render.m arg, net.agartha.client.module.impl.render.m.b arg2) {
      return this.noRenderModalOpen && this.activeNoRenderModule == arg && this.activeNoRenderCategory == arg2;
   }

   private void a() {
      this.themeWindowOpen = false;
      this.themeSnapshot = null;
      this.blockColorWindowOpen = false;
      this.activeBlockColorPalette = null;
      this.itemColorWindowOpen = false;
      this.activeItemColorPalette = null;
      this.colorPickerOpen.set(false);
      this.blockColorPickerOpen.set(false);
      this.itemColorPickerOpen.set(false);
      this.mobListOpen = false;
      this.activeMobList = null;
      this.itemListOpen = false;
      this.activeItemList = null;
      this.itemPickOpen = false;
      this.activeItemPick = null;
      this.g();
      this.regionMapModalOpen = false;
      this.armorHudModalOpen = false;
   }

   void openTheme(net.agartha.client.module.a_pkg.i arg) {
      if (!this.themeWindowOpen) {
         this.a();
         this.themeSnapshot = arg.get();
         this.themeWindowOpen = true;
      }
   }

   void openColor(net.agartha.client.module.b arg, net.agartha.client.module.a_pkg.c arg2) {
      this.a();
      this.activeColorModule = arg;
      this.activeColorSetting = arg2;
      this.colorPickerSnapshot = Arrays.copyOf(arg2.value(), 4);
      this.colorPickerOpen.set(true);
   }

   void openBlockPalette(net.agartha.client.module.a_pkg.a_o arg) {
      this.a();
      this.activeBlockColorPalette = arg;
      this.blockColorWindowOpen = true;
      this.activeBlockColorEntry = null;
      this.editingBlockDefaultColor = false;
   }

   void openItemPalette(net.agartha.client.module.a_pkg.d arg) {
      this.a();
      this.activeItemColorPalette = arg;
      this.itemColorWindowOpen = true;
      this.activeItemColorEntry = null;
      this.editingItemDefaultColor = false;
   }

   void openMobList(net.agartha.client.module.a_pkg.h arg) {
      this.a();
      this.activeMobList = arg;
      this.mobListOpen = true;
      this.mobSearchBuf.set("");
   }

   void openItemList(net.agartha.client.module.a_pkg.e arg) {
      this.a();
      this.activeItemList = arg;
      this.itemListOpen = true;
      this.itemPickSearch.set("");
      this.itemViewKey = null;
   }

   void openItemPick(net.agartha.client.module.a_pkg.f arg) {
      this.a();
      this.activeItemPick = arg;
      this.itemPickOpen = true;
      this.itemPickSearch.set("");
      this.itemViewKey = null;
   }

   boolean isRegionMapOpen() {
      return this.regionMapModalOpen;
   }

   boolean isArmorHudOpen() {
      return this.armorHudModalOpen;
   }

   void openArmorHud() {
      this.a();
      this.armorHudModalOpen = true;
   }

   void openRegionMap() {
      this.a();
      this.regionMapModalOpen = true;
   }

   void openNoRender(net.agartha.client.module.impl.render.m arg, net.agartha.client.module.impl.render.m.b arg2) {
      this.a();
      this.activeNoRenderModule = arg;
      this.activeNoRenderCategory = arg2;
      this.noRenderModalOpen = true;
   }

   void updateIntro(float f) {
      boolean var2 = this.anyOpen();
      if (var2 && !this.modalOpenLastFrame) {
         this.modalIntroTime = 0.0F;
      } else if (var2) {
         this.modalIntroTime = Math.min(this.modalIntroTime + f, 0.18F);
      }

      this.modalOpenLastFrame = var2;
   }

   boolean render(float f) {
      if (this.themeWindowOpen) {
         this.d();
         this.a(f);
      } else if (this.noRenderModalOpen) {
         this.d();
         this.p();
      } else if (this.regionMapModalOpen) {
         this.d();
         this.n();
      } else if (this.armorHudModalOpen) {
         this.d();
         this.o();
      } else if (g.isModalOpen()) {
         this.d();
         g.renderModal(f);
      } else if (l.isModalOpen()) {
         this.d();
         l.renderModal();
      } else if (p.isModalOpen()) {
         this.d();
         p.renderModal();
      } else if (am.isModalOpen()) {
         this.d();
         am.renderModal();
      } else if (this.blockColorPickerOpen.get()) {
         this.d();
         this.t();
      } else if (this.itemColorPickerOpen.get()) {
         this.d();
         this.x();
      } else if (this.colorPickerOpen.get()) {
         this.d();
         this.f();
      } else if (this.blockColorWindowOpen) {
         this.d();
         this.s();
      } else if (this.itemColorWindowOpen) {
         this.d();
         this.w();
      } else if (this.mobListOpen) {
         this.d();
         this.i();
      } else if (this.itemListOpen) {
         this.d();
         this.l();
      } else {
         if (!this.itemPickOpen) {
            return false;
         }

         this.d();
         this.m();
      }

      return true;
   }

   boolean handleEscape() {
      if (g.handleEscapeKey()) {
         return true;
      } else if (l.handleEscapeKey()) {
         return true;
      } else if (p.handleEscapeKey()) {
         return true;
      } else if (am.handleEscapeKey()) {
         return true;
      } else if (this.blockColorPickerOpen.get()) {
         this.c(false);
         return true;
      } else if (this.itemColorPickerOpen.get()) {
         this.d(false);
         return true;
      } else if (this.colorPickerOpen.get()) {
         this.b(false);
         return true;
      } else if (this.blockColorWindowOpen) {
         this.r();
         return true;
      } else if (this.itemColorWindowOpen) {
         this.v();
         return true;
      } else if (this.mobListOpen) {
         this.h();
         return true;
      } else if (this.itemListOpen) {
         this.j();
         return true;
      } else if (this.itemPickOpen) {
         this.k();
         return true;
      } else if (this.noRenderModalOpen) {
         this.g();
         return true;
      } else if (this.regionMapModalOpen) {
         this.regionMapModalOpen = false;
         return true;
      } else if (this.armorHudModalOpen) {
         this.armorHudModalOpen = false;
         return true;
      } else if (this.themeWindowOpen) {
         this.a(true);
         return true;
      } else {
         return false;
      }
   }

   void dismiss() {
      g.closeAll();
      l.closeAll();
      p.closeAll();
      am.closeAll();
   }

   private float b() {
      return !this.animationsEnabled.getAsBoolean() ? 1.0F : c.easeOutCubic(this.modalIntroTime / 0.18F);
   }

   private void a(float f, float g) {
      float var3 = ImGui.getIO().getDisplaySizeX();
      float var4 = ImGui.getIO().getDisplaySizeY();
      float var5 = this.b();
      ImGui.setNextWindowSize(f, g, 1);
      ImGui.setNextWindowPos((var3 - f) * 0.5F, (var4 - g) * 0.5F + 18.0F * (1.0F - var5), 1);
      ImGui.setNextWindowBgAlpha(var5 * d.WINDOW_BG_F[3]);
   }

   private static int c() {
      return 196902;
   }

   private void d() {
      float var1 = ImGui.getIO().getDisplaySizeX();
      float var2 = ImGui.getIO().getDisplaySizeY();
      float var3 = this.b();
      ImGui.setNextWindowPos(0.0F, 0.0F, 1);
      ImGui.setNextWindowSize(var1, var2, 1);
      ImGui.pushStyleVar(4, 0.0F);
      ImGui.pushStyleVar(2, 0.0F, 0.0F);
      c.pushColor(2, 0.0F, 0.0F, 0.0F, 0.62F * Math.max(0.35F, var3));
      int var4 = 205087;
      if (ImGui.begin("##modal_backdrop", var4)) {
         ImDrawList var5 = ImGui.getWindowDrawList();
         float var6 = var2 * 0.35F;
         var5.addRectFilledMultiColor(
            0.0F,
            var2 - var6,
            var1,
            var2,
            at.accentWithAlpha(0),
            at.accentWithAlpha(0),
            at.accentWithAlpha(Math.round(26.0F * var3)),
            at.accentWithAlpha(Math.round(26.0F * var3))
         );
      }

      ImGui.end();
      ImGui.popStyleColor();
      ImGui.popStyleVar(2);
   }

   private static int e() {
      return 135593984;
   }

   private static void a(String string, Runnable runnable, Runnable runnable2, Runnable runnable3) {
      float var4 = 8.0F;
      float var5 = 26.0F;
      float var6 = ImGui.getCursorPosX();
      float var7 = ImGui.getCursorPosY();
      float var8 = Math.max(1.0F, ImGui.getContentRegionAvailX());
      float var9 = Math.max(1.0F, (var8 - var4) * 0.5F);
      ImGui.setCursorPos(var6, var7);
      if (e.chipButton(string + "_reset", "Reset", var9, var5, false)) {
         runnable.run();
      }

      ImGui.setCursorPos(var6 + var9 + var4, var7);
      if (e.chipButton(string + "_undo", "Undo", var9, var5, false)) {
         runnable2.run();
      }

      float var10 = var7 + var5 + 4.0F + 2.0F;
      ImGui.setCursorPos(var6, var10);
      if (e.chipButton(string + "_saveexit", "Save & Exit", var8, var5, true)) {
         runnable3.run();
      }

      ImGui.setCursorPos(var6, var10 + var5 + 4.0F);
   }

   private static void a(String string, Runnable runnable) {
      if (e.chipButton(string, "Close", 120.0F, 24.0F, false)) {
         runnable.run();
      }
   }

   private static String a(String string, float f) {
      return c.ellipsize(ImGui.getFont(), ImGui.getFontSize(), string, f);
   }

   private void a(boolean bl) {
      if (bl && this.themeSnapshot != null) {
         AgarthaModule var2 = net.agartha.client.module.e.get(AgarthaModule.class);
         if (var2 != null) {
            var2.theme().set(this.themeSnapshot);
            net.agartha.client.c.e.getInstance().save();
         }
      }

      this.themeWindowOpen = false;
      this.themeSnapshot = null;
   }

   private void a(float f) {
      AgarthaModule var2 = net.agartha.client.module.e.get(AgarthaModule.class);
      if (var2 == null) {
         this.a(false);
      } else if (ImGui.isKeyPressed(526)) {
         this.a(true);
      } else {
         k[] var3 = k.values();
         byte var4 = 3;
         float var5 = 10.0F;
         boolean var6 = MonsterModeModule.isActive();
         float var7 = ImGui.getStyle().getWindowPaddingX();
         float var8 = ImGui.getStyle().getWindowPaddingY();
         float var9 = (520.0F - var7 * 2.0F - var5 * (var4 - 1)) / var4;
         float var10 = var6 ? 150.0F : var9 * 0.82F;
         int var11 = (var3.length + var4 - 1) / var4;
         float var12 = ImGui.getFrameHeight()
            + var8 * 2.0F
            + ImGui.getTextLineHeightWithSpacing()
            + ImGui.getStyle().getItemSpacingY() * 2.0F
            + var11 * (var10 + var5)
            + 2.0F
            + 24.0F
            + 6.0F;
         this.a(520.0F, var12);
         String var13 = MonsterModeModule.isActive() ? "Pick your flavor" : "Pick your theme";
         if (!ImGui.begin(var13 + "##agartha_theme", c() | 8 | 16)) {
            ImGui.end();
         } else {
            ImGui.textDisabled("Hover to preview, click to keep. ESC reverts.");
            ImGui.spacing();
            float var14 = ImGui.getContentRegionAvailX();
            float var15 = (var14 - var5 * (var4 - 1)) / var4;
            float var16 = var6 ? 150.0F : var15 * 0.82F;
            float var17 = ImGui.getCursorPosX();
            float var18 = ImGui.getCursorPosY();
            k var19 = null;
            k var20 = k.byName(this.themeSnapshot != null ? this.themeSnapshot : var2.theme().get());
            ImDrawList var21 = ImGui.getWindowDrawList();
            ag var22 = ag.current();

            for (int var23 = 0; var23 < var3.length; var23++) {
               k var24 = var3[var23];
               int var25 = var23 % var4;
               int var26 = var23 / var4;
               ImGui.setCursorPos(var17 + var25 * (var15 + var5), var18 + var26 * (var16 + var5));
               float var27 = ImGui.getCursorScreenPosX();
               float var28 = ImGui.getCursorScreenPosY();
               ImGui.pushID(var24.name());
               boolean var29 = ImGui.invisibleButton("##flavor", var15, var16);
               boolean var30 = ImGui.isItemHovered();
               ImGui.popID();
               if (var30) {
                  var19 = var24;
               }

               float var31 = this.themeCardHover[var23] = c.approach(this.themeCardHover[var23], var30 ? 1.0F : 0.0F, f, 14.0F);
               boolean var32 = var24 == var20;
               int var33 = var24.abgr();
               float var34 = var31 * 4.0F;
               if (var32 || var31 > 0.01F) {
                  c.glow(var21, var27, var28 - var34, var27 + var15, var28 + var16 - var34, 6.0F, 9.0F, var33, var32 ? 1.0F : var31 * 0.7F);
               }

               c.fillSlashed(var21, var27, var28 - var34, var27 + var15, var28 + var16 - var34, 6.0F, 9.0F, c.lerp(-15593200, -15066857, var31));
               a(var21, var27, var28 - var34, var15, var16, var33, Math.round(40.0F + 30.0F * var31));
               c.strokeSlashed(
                  var21,
                  var27 + 0.5F,
                  var28 + 0.5F - var34,
                  var27 + var15 - 0.5F,
                  var28 + var16 - 0.5F - var34,
                  6.0F,
                  9.0F,
                  var32 ? var33 : c.lerp(536870911, 1157627903, var31),
                  var32 ? 1.6F : 1.0F
               );
               float var35 = var6 ? 104.0F + var31 * 4.0F : Math.min(var15 * 0.88F, (var16 - 20.0F) * 1.25F) + var31 * 4.0F;
               float var36 = var35 * var22.aspect(var24);
               float var37 = var27 + var15 * 0.5F;
               float var38 = var6 ? var28 + 10.0F - var34 - var31 * 2.0F : var28 + (var16 - 20.0F - var35) * 0.5F + 4.0F - var34 - var31 * 2.0F;
               if (var22.isReady(var24)) {
                  var21.addImage(var22.textureId(var24), var37 - var36 * 0.5F, var38, var37 + var36 * 0.5F, var38 + var35);
               } else {
                  c.claw(var21, var37 - var35 * 0.5F, var38, var35, var33);
               }

               String var39 = var24.displayName().toUpperCase(Locale.ROOT);
               ImFont var40 = net.agartha.client.f.a.getInstance().hudLabelFont();
               float var41 = 11.0F;
               float var42 = var15 - 14.0F;
               float var43 = c.textWidth(var40, var41, var39);
               if (var43 > var42 && var43 > 0.0F) {
                  var41 = Math.max(8.0F, var41 * var42 / var43);
                  var43 = c.textWidth(var40, var41, var39);
               }

               c.text(var21, var40, var41, var37 - var43 * 0.5F, var28 + var16 - 20.0F - var34, var32 ? var33 : -1645601, var39);
               if (var29) {
                  var2.theme().set(var24.displayName());
                  net.agartha.client.c.e.getInstance().save();
                  this.a(false);
                  ImGui.end();
                  return;
               }
            }

            String var44 = var19 != null ? var19.displayName() : this.themeSnapshot;
            if (var44 != null && !var44.equals(var2.theme().get())) {
               var2.theme().set(var44);
            }

            int var45 = (var3.length + var4 - 1) / var4;
            ImGui.setCursorPos(var17, var18 + var45 * (var16 + var5) + 2.0F);
            a("##themeclose", () -> this.a(true));
            ImGui.end();
         }
      }
   }

   private void b(boolean bl) {
      if (bl && this.colorPickerSnapshot != null && this.activeColorSetting != null) {
         System.arraycopy(this.colorPickerSnapshot, 0, this.activeColorSetting.value(), 0, 4);
      } else if (this.activeColorSetting != null) {
         this.activeColorSetting.notifyChanged();
      }

      this.colorPickerOpen.set(false);
      this.colorPickerSnapshot = null;
      this.activeColorSetting = null;
      this.activeColorModule = null;
   }

   private void f() {
      if (this.activeColorSetting == null || this.activeColorModule == null) {
         this.b(false);
      } else if (ImGui.isKeyPressed(526)) {
         this.b(false);
      } else {
         this.a(405.0F, 424.0F);
         String var1 = "Color - " + this.activeColorModule.name() + " / " + this.activeColorSetting.name() + "##colormodal";
         if (!ImGui.begin(var1, c())) {
            ImGui.end();
         } else {
            ImGui.textDisabled("Pick a colour. ESC saves and exits.");
            ImGui.separator();
            ImGui.colorPicker4("##picker", this.activeColorSetting.value(), e());
            ImGui.setNextItemWidth(360.0F);
            if (ImGui.colorEdit4("RGBA", this.activeColorSetting.value(), e())) {
               this.activeColorSetting.notifyChanged();
            }

            ImGui.separator();
            a("##color", () -> {
               this.activeColorSetting.resetToDefault();
               this.activeColorSetting.notifyChanged();
            }, () -> {
               if (this.colorPickerSnapshot != null) {
                  System.arraycopy(this.colorPickerSnapshot, 0, this.activeColorSetting.value(), 0, 4);
                  this.activeColorSetting.notifyChanged();
               }
            }, () -> this.b(false));
            ImGui.end();
         }
      }
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   static String noRenderCategoryLabel(net.agartha.client.module.impl.render.m.b arg) {
      return switch (arg) {
         case OVERLAY -> "Overlay";
         case HUD -> "HUD";
         case WORLD -> "World";
         case ENTITY -> "Entity";
      };
   }

   private void g() {
      this.noRenderModalOpen = false;
      this.activeNoRenderModule = null;
      this.activeNoRenderCategory = null;
   }

   private void h() {
      if (this.activeMobList != null) {
         this.activeMobList.notifyChanged();
      }

      this.mobListOpen = false;
      this.activeMobList = null;
      this.mobSearchBuf.set("");
   }

   private void i() {
      if (this.activeMobList == null) {
         this.h();
      } else if (ImGui.isKeyPressed(526)) {
         this.h();
      } else {
         this.a(440.0F, 520.0F);
         if (!ImGui.begin("Mobs##moblist", c())) {
            ImGui.end();
         } else {
            ImGui.textDisabled("Turn off a mob to stop counting it.");
            ImGui.setNextItemWidth(-1.0F);
            ImGui.inputTextWithHint("##mobsearch", "Search", this.mobSearchBuf);
            net.agartha.client.f.a.getInstance().armTextInput();
            String var1 = this.mobSearchBuf.get() == null ? "" : this.mobSearchBuf.get().trim().toLowerCase(Locale.ROOT);
            ImGui.separator();
            ImGui.beginChild("##mobrows", 0.0F, 372.0F, false);
            float var2 = ImGui.getContentRegionAvailX();
            float var3 = ImGui.getTextLineHeight();

            for (net.agartha.client.module.a_pkg.h.a var5 : this.activeMobList.entries()) {
               if (var1.isEmpty() || var5.name().toLowerCase(Locale.ROOT).contains(var1)) {
                  float var6 = ImGui.getCursorPosY();
                  a(ad.mobIcon(var5.id()), 8.0F, var6, 26.0F);
                  float var7 = 36.0F;
                  ImGui.setCursorPos(var7, var6 + (26.0F - var3) * 0.5F);
                  ImGui.textUnformatted(a(var5.name(), var2 - 15.0F - 20.0F - var7));
                  ImGui.setCursorPos(var2 - 15.0F - 2.0F, var6 + 5.5F);
                  ImGui.pushID(var5.id());
                  if (e.checkbox("##mobchk", var5.enabled().get(), 15.0F)) {
                     var5.enabled().set(!var5.enabled().get());
                     this.activeMobList.notifyChanged();
                  }

                  ImGui.popID();
                  ImGui.setCursorPosY(var6 + 26.0F);
               }
            }

            ImGui.endChild();
            ImGui.separator();
            a("##mobclose", this::h);
            ImGui.end();
         }
      }
   }

   private void j() {
      if (this.activeItemList != null) {
         net.agartha.client.c.e.getInstance().save();
      }

      this.itemListOpen = false;
      this.activeItemList = null;
      this.itemPickSearch.set("");
   }

   private void k() {
      if (this.activeItemPick != null) {
         net.agartha.client.c.e.getInstance().save();
      }

      this.itemPickOpen = false;
      this.activeItemPick = null;
      this.itemPickSearch.set("");
   }

   private List<ad.a> a(String string, String string2, Predicate<String> predicate) {
      String var4 = string + "|" + string2 + "|" + this.itemSelectionVersion;
      if (var4.equals(this.itemViewKey)) {
         return this.itemView;
      } else {
         ArrayList var5 = new ArrayList();
         ArrayList var6 = new ArrayList();

         for (ad.a var8 : ad.rows()) {
            if (string.isEmpty() || var8.search().contains(string)) {
               (predicate.test(var8.id()) ? var5 : var6).add(var8);
            }
         }

         var5.addAll(var6);
         this.itemView = var5;
         this.itemViewKey = var4;
         return this.itemView;
      }
   }

   private String a(String string) {
      ImGui.textDisabled(string);
      ImGui.setNextItemWidth(-1.0F);
      ImGui.inputTextWithHint("##itemsearch", "Search items", this.itemPickSearch);
      net.agartha.client.f.a.getInstance().armTextInput();
      String var2 = this.itemPickSearch.get() == null ? "" : this.itemPickSearch.get().trim().toLowerCase(Locale.ROOT);
      ImGui.separator();
      return var2;
   }

   private ad.a a(List<ad.a> list, Predicate<String> predicate, boolean bl, float f) {
      ad.a var5 = null;
      ImGui.beginChild("##itemrows", 0.0F, f, false);
      float var6 = ImGui.getContentRegionAvailX();
      float var7 = ImGui.getScrollY();
      float var8 = ImGui.getWindowHeight();
      int var9 = Math.max(0, (int)(var7 / 30.0F));
      int var10 = Math.min(list.size(), (int)Math.ceil((var7 + var8) / 30.0F) + 1);
      ImDrawList var11 = ImGui.getWindowDrawList();
      float var12 = ImGui.getTextLineHeight();
      float[] var13 = d.accentF();
      int var14 = d.rgbAbgr(Math.round(var13[0] * 255.0F), Math.round(var13[1] * 255.0F), Math.round(var13[2] * 255.0F), 30);

      for (int var15 = var9; var15 < var10; var15++) {
         ad.a var16 = (ad.a)list.get(var15);
         boolean var17 = predicate.test(var16.id());
         float var18 = var15 * 30.0F;
         ImGui.pushID(var16.id());
         ImGui.setCursorPos(0.0F, var18);
         float var19 = ImGui.getCursorScreenPosX();
         float var20 = ImGui.getCursorScreenPosY();
         ImGui.invisibleButton("##row", var6, 28.0F);
         boolean var21 = ImGui.isItemClicked(0);
         boolean var22 = ImGui.isItemHovered();
         if (var17) {
            var11.addRectFilled(var19, var20, var19 + var6, var20 + 30.0F - 2.0F, var14, 5.0F);
         }

         if (var22) {
            var11.addRectFilled(var19, var20, var19 + var6, var20 + 30.0F - 2.0F, c.f(352321535), 5.0F);
         }

         float var23 = 8.0F;
         if (bl) {
            ImGui.setCursorPos(var23, var18 + 6.5F);
            if (e.checkbox("##chk", var17, 15.0F)) {
               var21 = true;
            }

            var23 += 25.0F;
         }

         ab.draw(var11, var16.item(), var19 + var23, var20 + 3.0F, 22.0F, 1.0F);
         var23 += 32.0F;
         String var24 = var16.id().startsWith("minecraft:") ? var16.id().substring(10) : var16.id();
         float var25 = ImGui.calcTextSize(var24).x;
         float var26 = var6 - var23 - var25 - 20.0F;
         float var27 = Math.round(var20 + (28.0F - var12) * 0.5F);
         var11.addText(Math.round(var19 + var23), var27, c.f(var17 ? -526345 : -2435375), a(var16.name(), Math.max(40.0F, var26)));
         if (var26 > 60.0F) {
            var11.addText(Math.round(var19 + var6 - var25 - 8.0F), var27, c.f(-6119783), var24);
         }

         ImGui.popID();
         if (var21) {
            au.click();
            var5 = var16;
         }
      }

      ImGui.setCursorPos(0.0F, Math.max(0.0F, list.size() * 30.0F - 1.0F));
      ImGui.dummy(1.0F, 1.0F);
      ImGui.endChild();
      return var5;
   }

   private void l() {
      net.agartha.client.module.a_pkg.e var1 = this.activeItemList;
      if (var1 == null) {
         this.j();
      } else if (ImGui.isKeyPressed(526)) {
         this.j();
      } else {
         this.a(520.0F, 640.0F);
         if (!ImGui.begin(var1.name() + "##itemlistmodal", c())) {
            ImGui.end();
         } else {
            String var2 = this.a("Tick items to add them. Ticked items are listed first. ESC closes.");
            List var3 = this.a(var2, var1.name(), var1::isSelected);
            ad.a var4 = this.a(var3, var1::isSelected, true, 490.0F);
            if (var4 != null) {
               var1.toggle(var4.id());
            }

            ImGui.separator();
            a("##itemlistclose", this::j);
            ImGui.sameLine();
            if (e.chipButton("##itemlistclear", "Clear All", 120.0F, 24.0F, false)) {
               var1.clear();
               this.itemSelectionVersion++;
            }

            ImGui.sameLine();
            ImGui.textDisabled(var1.size() + " selected");
            ImGui.end();
         }
      }
   }

   private void m() {
      net.agartha.client.module.a_pkg.f var1 = this.activeItemPick;
      if (var1 == null) {
         this.k();
      } else if (ImGui.isKeyPressed(526)) {
         this.k();
      } else {
         this.a(520.0F, 640.0F);
         if (!ImGui.begin(var1.name() + "##itempickmodal", c())) {
            ImGui.end();
         } else {
            String var2 = this.a("Click an item to use it. ESC closes.");
            String var3 = var1.id();
            Predicate var4 = var3::equals;
            List var5 = this.a(var2, var3, var4);
            ad.a var6 = this.a(var5, var4, false, 490.0F);
            ImGui.separator();
            a("##itempickclose", this::k);
            ImGui.end();
            if (var6 != null) {
               var1.set(var6.id());
               this.k();
            }
         }
      }
   }

   private void n() {
      RegionMapModule var1 = net.agartha.client.module.e.get(RegionMapModule.class);
      if (var1 != null && !ImGui.isKeyPressed(526)) {
         net.agartha.client.module.a_pkg.b[] var2 = new net.agartha.client.module.a_pkg.b[]{
            var1.showRegionIdsSetting(), var1.showPlayerSetting(), var1.showCoordsSetting(), var1.showNetherSetting(), var1.showRegionInfoSetting()
         };
         String[] var3 = new String[]{"Region Numbers", "Player Icon", "Coordinates", "Nether Coordinates", "Region Info"};
         float var4 = var2.length * 26.0F + 8.0F;
         this.a(380.0F, var4 + 112.0F);
         if (!ImGui.begin("Region Map Settings##regionmodal", c())) {
            ImGui.end();
         } else {
            ImGui.textDisabled("Pick what the region map shows.");
            ImGui.separator();
            ImGui.beginChild("##regionparts", 0.0F, var4, false);
            float var5 = 8.0F;
            float var6 = ImGui.getContentRegionAvailX();
            float var7 = ImGui.getTextLineHeight();

            for (int var8 = 0; var8 < var2.length; var8++) {
               net.agartha.client.module.a_pkg.b var9 = var2[var8];
               float var10 = ImGui.getCursorPosY();
               ImGui.setCursorPos(8.0F, var10 + (26.0F - var7) * 0.5F);
               ImGui.textUnformatted(var3[var8]);
               ImGui.setCursorPos(var6 - 15.0F - 2.0F, var10 + 5.5F);
               ImGui.pushID(var3[var8]);
               if (e.checkbox("##regionpart", var9.get(), 15.0F)) {
                  var9.set(!var9.get());
                  net.agartha.client.c.e.getInstance().save();
               }

               ImGui.popID();
               ImGui.setCursorPosY(var10 + 26.0F);
            }

            ImGui.endChild();
            ImGui.separator();
            a("##regionclose", () -> this.regionMapModalOpen = false);
            ImGui.end();
         }
      } else {
         this.regionMapModalOpen = false;
      }
   }

   private void o() {
      HudModule var1 = net.agartha.client.module.e.get(HudModule.class);
      if (var1 != null && !ImGui.isKeyPressed(526)) {
         net.agartha.client.module.a_pkg.b[] var2 = new net.agartha.client.module.a_pkg.b[]{var1.armorPercentSetting(), var1.armorBoxSetting()};
         String[] var3 = new String[]{"Durability", "Box"};
         float var4 = (var2.length + 1) * 26.0F + 8.0F;
         this.a(380.0F, var4 + 112.0F);
         if (!ImGui.begin("Armor HUD Settings##armormodal", c())) {
            ImGui.end();
         } else {
            ImGui.textDisabled("How the armor card looks.");
            ImGui.separator();
            ImGui.beginChild("##armorparts", 0.0F, var4, false);
            float var5 = 8.0F;
            float var6 = ImGui.getContentRegionAvailX();
            float var7 = ImGui.getTextLineHeight();
            float var8 = ImGui.getCursorPosY();
            ImGui.setCursorPos(8.0F, var8 + (26.0F - var7) * 0.5F);
            ImGui.textUnformatted("Layout");
            float var9 = 96.0F;
            float var10 = 22.0F;
            ImGui.setCursorPos(var6 - var9 - 2.0F, var8 + (26.0F - var10) * 0.5F);
            if (e.chipButton("##armorlayout", var1.armorLayoutSetting().value(), var9, var10, false)) {
               var1.armorLayoutSetting().next();
               net.agartha.client.c.e.getInstance().save();
            }

            ImGui.setCursorPosY(var8 + 26.0F);

            for (int var11 = 0; var11 < var2.length; var11++) {
               net.agartha.client.module.a_pkg.b var12 = var2[var11];
               var8 = ImGui.getCursorPosY();
               ImGui.setCursorPos(8.0F, var8 + (26.0F - var7) * 0.5F);
               ImGui.textUnformatted(var3[var11]);
               ImGui.setCursorPos(var6 - 15.0F - 2.0F, var8 + 5.5F);
               ImGui.pushID(var3[var11]);
               if (e.checkbox("##armorpart", var12.get(), 15.0F)) {
                  var12.set(!var12.get());
                  net.agartha.client.c.e.getInstance().save();
               }

               ImGui.popID();
               ImGui.setCursorPosY(var8 + 26.0F);
            }

            ImGui.endChild();
            ImGui.separator();
            a("##armorclose", () -> this.armorHudModalOpen = false);
            ImGui.end();
         }
      } else {
         this.armorHudModalOpen = false;
      }
   }

   private void p() {
      if (this.activeNoRenderModule != null && this.activeNoRenderCategory != null) {
         if (ImGui.isKeyPressed(526)) {
            this.g();
         } else {
            this.a(440.0F, 520.0F);
            String var1 = "No Render - " + noRenderCategoryLabel(this.activeNoRenderCategory) + "##nrmodal";
            if (!ImGui.begin(var1, c())) {
               ImGui.end();
            } else {
               ImGui.textDisabled("Pick what to hide while No Render is on.");
               ImGui.separator();
               ImGui.beginChild("##nrsettings", 0.0F, 408.0F, false);
               float var2 = 8.0F;
               float var3 = ImGui.getContentRegionAvailX();
               float var4 = ImGui.getTextLineHeight();
               if (this.activeNoRenderCategory == net.agartha.client.module.impl.render.m.b.WORLD) {
                  net.agartha.client.module.a_pkg.i var5 = this.activeNoRenderModule.bannersSetting();
                  float var6 = ImGui.getCursorPosY();
                  ImGui.setCursorPos(8.0F, var6 + 3.0F);
                  if (e.valueChip("##nrbanners", "Banners", var5.get(), var3 - 8.0F, 20.0F)) {
                     var5.next();
                     var5.notifyChanged();
                  }

                  ImGui.setCursorPosY(var6 + 26.0F + 4.0F);
               }

               for (net.agartha.client.module.a_pkg.b var8 : this.activeNoRenderModule.settingsFor(this.activeNoRenderCategory)) {
                  float var9 = ImGui.getCursorPosY();
                  ImGui.setCursorPos(8.0F, var9 + (26.0F - var4) * 0.5F);
                  ImGui.textUnformatted(a(var8.name(), var3 - 8.0F - 15.0F - 12.0F));
                  ImGui.setCursorPos(var3 - 15.0F - 2.0F, var9 + 5.5F);
                  ImGui.pushID(var8.name());
                  if (e.checkbox("##nrchk", var8.value().get(), 15.0F)) {
                     var8.value().set(!var8.value().get());
                     var8.notifyChanged();
                  }

                  ImGui.popID();
                  ImGui.setCursorPosY(var9 + 26.0F);
               }

               ImGui.endChild();
               ImGui.separator();
               a("##nrclose", this::g);
               ImGui.end();
            }
         }
      } else {
         this.g();
      }
   }

   private float[] q() {
      if (this.editingBlockDefaultColor && this.activeBlockColorPalette != null) {
         return this.activeBlockColorPalette.defaultColor();
      } else {
         return this.activeBlockColorEntry == null ? null : this.activeBlockColorEntry.color();
      }
   }

   private void c(boolean bl) {
      float[] var2 = this.q();
      if (bl && this.blockColorPickerSnapshot != null && var2 != null) {
         System.arraycopy(this.blockColorPickerSnapshot, 0, var2, 0, 4);
      } else if (this.activeBlockColorPalette != null) {
         this.activeBlockColorPalette.notifyChanged();
      }

      this.blockColorPickerOpen.set(false);
      this.blockColorPickerSnapshot = null;
      this.activeBlockColorEntry = null;
      this.editingBlockDefaultColor = false;
   }

   private void r() {
      if (this.activeBlockColorPalette != null) {
         this.activeBlockColorPalette.notifyChanged();
      }

      this.blockColorWindowOpen = false;
      this.activeBlockColorPalette = null;
      this.blockSearchBuf.set("");
   }

   private void s() {
      if (this.activeBlockColorPalette != null) {
         if (ImGui.isKeyPressed(526)) {
            this.r();
         } else {
            this.a(630.0F, 750.0F);
            if (!ImGui.begin("Block Colors##bcpalette", c())) {
               ImGui.end();
            } else {
               ImGui.textDisabled("Toggle blocks and colours. ESC closes.");
               ImGui.separator();
               ImGui.text("Default:");
               ImGui.sameLine();
               int var1 = 131648;
               if (ImGui.colorButton("##defcol", this.activeBlockColorPalette.defaultColor(), var1, 42.0F, 16.0F)) {
                  this.editingBlockDefaultColor = true;
                  this.activeBlockColorEntry = null;
                  this.blockColorPickerSnapshot = Arrays.copyOf(this.activeBlockColorPalette.defaultColor(), 4);
                  this.blockColorPickerOpen.set(true);
               }

               ImGui.separator();
               ImGui.text("Search:");
               ImGui.sameLine();
               ImGui.setNextItemWidth(300.0F);
               ImGui.inputText("##bcsearch", this.blockSearchBuf);
               net.agartha.client.f.a.getInstance().armTextInput();
               String var2 = this.blockSearchBuf.get().toLowerCase(Locale.ROOT).trim();
               ImGui.separator();
               ImGui.beginChild("##bclist", 0.0F, 530.0F, false, 0);
               String var3 = "";
               ArrayList var4 = new ArrayList<>(this.activeBlockColorPalette.entries());
               var4.sort(
                  Comparator.<net.agartha.client.module.a_pkg.a_o.a, Boolean>comparing(arg -> !arg.enabled().get())
                     .thenComparing(net.agartha.client.module.a_pkg.a_o.a::displayName, String.CASE_INSENSITIVE_ORDER)
               );

               for (net.agartha.client.module.a_pkg.a_o.a var6 : (Iterable<net.agartha.client.module.a_pkg.a_o.a>) var4) {
                  boolean var7 = var2.isEmpty()
                     || var6.displayName().toLowerCase(Locale.ROOT).contains(var2)
                     || var6.category().toLowerCase(Locale.ROOT).contains(var2);
                  if (var7) {
                     if (!var6.category().equals(var3)) {
                        var3 = var6.category();
                        float[] var8 = d.accentF();
                        c.pushColor(0, var8[0], var8[1], var8[2], 1.0F);
                        ImGui.textUnformatted(var3.toUpperCase(Locale.ROOT));
                        ImGui.popStyleColor();
                     }

                     if (this.a(var6.id(), var6.displayName(), var6.enabled().get(), var6.color(), ad.blockIcon(var6.id()))) {
                        var6.enabled().set(!var6.enabled().get());
                        this.activeBlockColorPalette.notifyChanged();
                     }

                     if (this.swatchClicked) {
                        this.activeBlockColorEntry = var6;
                        this.editingBlockDefaultColor = false;
                        this.blockColorPickerSnapshot = Arrays.copyOf(var6.color(), 4);
                        this.blockColorPickerOpen.set(true);
                     }
                  }
               }

               ImGui.endChild();
               ImGui.separator();
               a("##bcclose", this::r);
               ImGui.end();
            }
         }
      }
   }

   private static void a(Item item, float f, float g, float h) {
      if (item != null) {
         float var4 = ImGui.getWindowPosX() + f;
         float var5 = ImGui.getWindowPosY() + g - ImGui.getScrollY() + (h - 20.0F) * 0.5F;
         if (ImGui.isRectVisible(var4, var5, var4 + 20.0F, var5 + 20.0F)) {
            ab.draw(ImGui.getWindowDrawList(), item, var4, var5, 20.0F, 1.0F);
         }
      }
   }

   private boolean a(String string, String string2, boolean bl, float[] fs, Item item) {
      float var6 = ImGui.getCursorPosY();
      float var7 = 24.0F;
      float var8 = ImGui.getContentRegionAvailX();
      ImGui.pushID(string);
      ImGui.setCursorPos(4.0F, var6 + (var7 - 15.0F) * 0.5F);
      boolean var9 = e.checkbox("##chk", bl, 15.0F);
      float var10 = 27.0F;
      a(item, var10, var6, var7);
      var10 += 28.0F;
      ImGui.setCursorPos(var10, var6 + (var7 - ImGui.getTextLineHeight()) * 0.5F);
      ImGui.textUnformatted(string2);
      float var11 = Math.max(105.0F, var8 - 46.0F);
      ImGui.setCursorPos(var11, var6 + (var7 - 16.0F) * 0.5F);
      int var12 = 131648;
      this.swatchClicked = ImGui.colorButton("##col", fs, var12, 42.0F, 16.0F);
      ImGui.popID();
      ImGui.setCursorPosY(var6 + var7 + 2.0F);
      return var9;
   }

   private void t() {
      float[] var1 = this.q();
      if (var1 == null) {
         this.c(false);
      } else if (ImGui.isKeyPressed(526)) {
         this.c(false);
      } else {
         String var2 = this.editingBlockDefaultColor ? "Default Color" : this.activeBlockColorEntry.displayName();
         this.a(405.0F, 424.0F);
         if (!ImGui.begin("Block Color - " + var2 + "##bchexmodal", c())) {
            ImGui.end();
         } else {
            ImGui.textDisabled("Pick a colour. ESC saves and exits.");
            ImGui.separator();
            ImGui.colorPicker4("##bcpicker", var1, e());
            ImGui.setNextItemWidth(360.0F);
            if (ImGui.colorEdit4("RGBA##bcce", var1, e()) && this.activeBlockColorPalette != null) {
               this.activeBlockColorPalette.notifyChanged();
            }

            ImGui.separator();
            a("##block_color", () -> {
               if (this.activeBlockColorPalette != null) {
                  this.activeBlockColorPalette.resetColorToDefault(this.editingBlockDefaultColor ? null : this.activeBlockColorEntry);
               }
            }, () -> {
               if (this.blockColorPickerSnapshot != null) {
                  System.arraycopy(this.blockColorPickerSnapshot, 0, var1, 0, 4);
                  if (this.activeBlockColorPalette != null) {
                     this.activeBlockColorPalette.notifyChanged();
                  }
               }
            }, () -> this.c(false));
            ImGui.end();
         }
      }
   }

   private float[] u() {
      if (this.editingItemDefaultColor && this.activeItemColorPalette != null) {
         return this.activeItemColorPalette.defaultColor();
      } else {
         return this.activeItemColorEntry == null ? null : this.activeItemColorEntry.color();
      }
   }

   private void d(boolean bl) {
      float[] var2 = this.u();
      if (bl && this.itemColorPickerSnapshot != null && var2 != null) {
         System.arraycopy(this.itemColorPickerSnapshot, 0, var2, 0, 4);
      } else if (this.activeItemColorPalette != null) {
         this.activeItemColorPalette.notifyChanged();
      }

      this.itemColorPickerOpen.set(false);
      this.itemColorPickerSnapshot = null;
      this.activeItemColorEntry = null;
      this.editingItemDefaultColor = false;
   }

   private void v() {
      if (this.activeItemColorPalette != null) {
         this.activeItemColorPalette.notifyChanged();
      }

      this.itemColorWindowOpen = false;
      this.activeItemColorPalette = null;
      this.itemSearchBuf.set("");
   }

   private void w() {
      if (this.activeItemColorPalette != null) {
         if (ImGui.isKeyPressed(526)) {
            this.v();
         } else {
            this.a(630.0F, 750.0F);
            if (!ImGui.begin("Item Colors##icpalette", c())) {
               ImGui.end();
            } else {
               ImGui.textDisabled("Toggle items and colours. ESC closes.");
               ImGui.separator();
               ImGui.text("Default:");
               ImGui.sameLine();
               int var1 = 131648;
               if (ImGui.colorButton("##icdefcol", this.activeItemColorPalette.defaultColor(), var1, 42.0F, 16.0F)) {
                  this.editingItemDefaultColor = true;
                  this.activeItemColorEntry = null;
                  this.itemColorPickerSnapshot = Arrays.copyOf(this.activeItemColorPalette.defaultColor(), 4);
                  this.itemColorPickerOpen.set(true);
               }

               ImGui.separator();
               ImGui.text("Search:");
               ImGui.sameLine();
               ImGui.setNextItemWidth(300.0F);
               ImGui.inputText("##icsearch", this.itemSearchBuf);
               net.agartha.client.f.a.getInstance().armTextInput();
               String var2 = this.itemSearchBuf.get().toLowerCase(Locale.ROOT).trim();
               ImGui.separator();
               ImGui.beginChild("##iclist", 0.0F, 560.0F, false, 0);

               for (net.agartha.client.module.a_pkg.d.a var4 : this.activeItemColorPalette.entries()) {
                  boolean var5 = var2.isEmpty()
                     || var4.displayName().toLowerCase(Locale.ROOT).contains(var2)
                     || var4.id().toLowerCase(Locale.ROOT).contains(var2);
                  if (var5) {
                     if (this.a(var4.id(), var4.displayName(), var4.enabled().get(), var4.color(), ad.item(var4.id()))) {
                        var4.enabled().set(!var4.enabled().get());
                        this.activeItemColorPalette.notifyChanged();
                     }

                     if (this.swatchClicked) {
                        this.activeItemColorEntry = var4;
                        this.editingItemDefaultColor = false;
                        this.itemColorPickerSnapshot = Arrays.copyOf(var4.color(), 4);
                        this.itemColorPickerOpen.set(true);
                     }
                  }
               }

               ImGui.endChild();
               ImGui.separator();
               a("##icclose", this::v);
               ImGui.end();
            }
         }
      }
   }

   private void x() {
      float[] var1 = this.u();
      if (var1 == null) {
         this.d(false);
      } else if (ImGui.isKeyPressed(526)) {
         this.d(false);
      } else {
         String var2 = this.editingItemDefaultColor ? "Default Color" : this.activeItemColorEntry.displayName();
         this.a(405.0F, 424.0F);
         if (!ImGui.begin("Item Color - " + var2 + "##ichexmodal", c())) {
            ImGui.end();
         } else {
            ImGui.textDisabled("Pick a colour. ESC saves and exits.");
            ImGui.separator();
            ImGui.colorPicker4("##icpicker", var1, e());
            ImGui.setNextItemWidth(360.0F);
            if (ImGui.colorEdit4("RGBA##icce", var1, e()) && this.activeItemColorPalette != null) {
               this.activeItemColorPalette.notifyChanged();
            }

            ImGui.separator();
            a("##item_color", () -> {
               if (this.activeItemColorPalette != null) {
                  this.activeItemColorPalette.resetColorToDefault(this.editingItemDefaultColor ? null : this.activeItemColorEntry);
               }
            }, () -> {
               if (this.itemColorPickerSnapshot != null) {
                  System.arraycopy(this.itemColorPickerSnapshot, 0, var1, 0, 4);
                  if (this.activeItemColorPalette != null) {
                     this.activeItemColorPalette.notifyChanged();
                  }
               }
            }, () -> this.d(false));
            ImGui.end();
         }
      }
   }

   private static void a(ImDrawList imDrawList, float f, float g, float h, float i, int j, int k) {
      float var7 = g + i * 0.45F;
      float var8 = g + i - 2.0F;
      int var9 = c.withAlpha(j, 0);
      int var10 = c.withAlpha(j, k);
      if (!c.smooth()) {
         imDrawList.addRectFilledMultiColor(f + 2.0F, var7, f + h - 2.0F, var8, var9, var9, var10, var10);
      } else {
         float var11 = Math.max(0.0F, c.smoothRadius(6.0F, 9.0F, h, i) - 2.0F);
         float var12 = Math.max(var7, var8 - var11);
         int var13 = c.withAlpha(j, Math.round(k * (var12 - var7) / Math.max(1.0F, var8 - var7)));
         imDrawList.addRectFilledMultiColor(f + 2.0F, var7, f + h - 2.0F, var12, var9, var9, var13, var13);
         int var14 = c.withAlpha(j, Math.round(k * 0.5F + (var13 >>> 24 & 0xFF) * 0.5F));
         imDrawList.addRectFilled(f + 2.0F, var12, f + h - 2.0F, var8, var14, var11, 192);
      }
   }
}
