package net.agartha.client.gui;

import net.agartha.client.gui.AccentColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class Palette {
   public static final int TOGGLE_OFF = -600032710;
   public static final int TOGGLE_THUMB = -526859;
   public static final int INNER_BOX_BG = -132770796;
   public static final int INNER_BOX_BORDER = -2144457684;
   public static final int BORDER_LIGHT = -2143536582;
   public static final int VERT_LINE_COLOR = -1875101126;
   public static final float[] TEXT_F = new float[]{0.961F, 0.961F, 0.969F, 1.0F};
   public static final float[] TEXT_DISABLED_F = new float[]{0.557F, 0.557F, 0.576F, 1.0F};
   public static final float[] WINDOW_BG_F = new float[]{0.016F, 0.016F, 0.016F, 0.94F};
   public static final float[] CHILD_BG_F = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
   public static final float[] TITLE_BG_F = new float[]{0.043F, 0.043F, 0.051F, 1.0F};
   public static final float[] TITLE_ACTIVE_F = new float[]{0.11F, 0.11F, 0.118F, 1.0F};
   public static final float[] FRAME_BG_F = new float[]{0.11F, 0.11F, 0.118F, 1.0F};
   public static final float[] FRAME_HOV_F = new float[]{0.173F, 0.173F, 0.18F, 1.0F};
   public static final float WINDOW_ROUNDING = 8.0F;
   public static final float FRAME_ROUNDING = 4.0F;
   public static final float GRAB_ROUNDING = 4.0F;
   public static final float WIN_PAD_X = 12.0F;
   public static final float WIN_PAD_Y = 10.0F;
   public static final float ITEM_SPACING_X = 8.0F;
   public static final float ITEM_SPACING_Y = 4.0F;
   public static final float CATEGORY_FONT_SIZE = 16.0F;
   public static final float MODULE_FONT_SIZE = 14.0F;
   public static final float WATERMARK_FONT_SIZE = 32.0F;
   public static final float HEADER_H = 38.0F;
   public static final float SETTING_INDENT = 18.0F;
   public static final int HUD_BG = -536541947;
   public static final int HUD_SHEEN_TOP = 352321535;
   public static final int HUD_SHADOW = 1711276032;
   public static final int HUD_TEXT = -1;
   public static final int HUD_LABEL = -5592406;
   public static final float HUD_RADIUS = 4.0F;
   public static final float HUD_ACCENT_BAR_W = 5.0F;

   private Palette() {
   }

   public static int accent() {
      return AccentColor.accent();
   }

   public static int hoverBg() {
      return AccentColor.hoverBg();
   }

   public static int borderAccent() {
      return AccentColor.borderAccent();
   }

   public static float[] accentF() {
      return AccentColor.accentF();
   }

   public static float[] borderF() {
      return AccentColor.borderF();
   }

   public static boolean hudAccentOnLeft(float f, float g, float h) {
      return h > 0.0F && g > 0.0F ? f + g * 0.5F < h * 0.5F : true;
   }

   public static int rgbAbgr(int i, int j, int k, int l) {
      return (l & 0xFF) << 24 | (k & 0xFF) << 16 | (j & 0xFF) << 8 | i & 0xFF;
   }

   public static int lerpColor(int i, int j, float f) {
      int var3 = i & 0xFF;
      int var4 = j & 0xFF;
      int var5 = i >> 8 & 0xFF;
      int var6 = j >> 8 & 0xFF;
      int var7 = i >> 16 & 0xFF;
      int var8 = j >> 16 & 0xFF;
      int var9 = i >>> 24;
      int var10 = j >>> 24;
      return (int)(var9 + (var10 - var9) * f) << 24
         | (int)(var7 + (var8 - var7) * f) << 16
         | (int)(var5 + (var6 - var5) * f) << 8
         | (int)(var3 + (var4 - var3) * f);
   }
}
