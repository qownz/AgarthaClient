package net.agartha.client.e;

import imgui.ImDrawList;
import imgui.ImFont;
import imgui.ImFontGlyph;
import imgui.ImGui;
import java.util.IdentityHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class c {
   public static final int SURFACE = -234091766;
   public static final int SURFACE_HI = -15461614;
   public static final int SURFACE_LOW = -16250873;
   public static final int HAIRLINE = 352321535;
   public static final int HAIRLINE_HI = 620756991;
   public static final int TEXT = -526345;
   public static final int TEXT_MUTED = -6119783;
   public static final int TEXT_DIM = -9738140;
   public static final int WARN = -11908353;
   public static final int STYLE_SHARP = 0;
   public static final int STYLE_SMOOTH = 1;
   private static volatile boolean smooth;
   private static volatile boolean themeFill;
   private static int toneFrame = Integer.MIN_VALUE;
   private static int toneR;
   private static int toneG;
   private static int toneB;
   private static boolean glass;
   private static float glassOpacity = 0.35F;
   private static boolean glassTextShadow = true;
   private static final float MIN_HUD_TEXT = 11.0F;
   private static final IdentityHashMap<ImFont, Float> CAP_MID = new IdentityHashMap<>();
   private static int capGeneration = -1;
   private static final IdentityHashMap<ImFont, Float> CAP_BOTTOM = new IdentityHashMap<>();
   private static int capBottomGeneration = -1;

   private c() {
   }

   public static void setStyle(int i) {
      smooth = i == 1;
   }

   public static boolean smooth() {
      return smooth;
   }

   public static void setThemeFill(boolean bl) {
      themeFill = bl;
   }

   private static boolean a() {
      return glass || themeFill;
   }

   public static int tone(int i) {
      int var1 = i & 0xFF;
      int var2 = i >>> 8 & 0xFF;
      int var3 = i >>> 16 & 0xFF;
      int var4 = Math.max(var1, Math.max(var2, var3));
      int var5 = Math.min(var1, Math.min(var2, var3));
      if (var4 - var5 > 24) {
         return i;
      } else {
         int var6 = net.agartha.client.f.a.getInstance().frameCount();
         if (var6 != toneFrame) {
            toneFrame = var6;
            int var7 = at.accent();
            toneR = var7 & 0xFF;
            toneG = var7 >>> 8 & 0xFF;
            toneB = var7 >>> 16 & 0xFF;
         }

         int var17 = i >>> 24 & 0xFF;
         float var8 = (var1 + var2 + var3) / 765.0F;
         float var9;
         if (var8 < 0.5F) {
            var9 = 0.62F;
         } else if (var17 < 144) {
            var9 = 0.55F;
         } else {
            var9 = 0.1F + 0.6F * (1.0F - var8);
         }

         float var10 = Math.min(1.6F, var8 * 1.6F + 0.02F);
         int var11 = Math.min(255, Math.round(toneR * var10));
         int var12 = Math.min(255, Math.round(toneG * var10));
         int var13 = Math.min(255, Math.round(toneB * var10));
         if (var8 >= 0.5F) {
            var11 = var11 + 255 >> 1;
            var12 = var12 + 255 >> 1;
            var13 = var13 + 255 >> 1;
         }

         int var14 = Math.round(var1 + (var11 - var1) * var9);
         int var15 = Math.round(var2 + (var12 - var2) * var9);
         int var16 = Math.round(var3 + (var13 - var3) * var9);
         return var17 << 24 | var16 << 16 | var15 << 8 | var14;
      }
   }

   public static void pushColor(int i, float f, float g, float h, float j) {
      if (!a()) {
         ImGui.pushStyleColor(i, f, g, h, j);
      } else {
         int var5 = Math.round(clamp01(j) * 255.0F) << 24
            | Math.round(clamp01(h) * 255.0F) << 16
            | Math.round(clamp01(g) * 255.0F) << 8
            | Math.round(clamp01(f) * 255.0F);
         int var6 = tone(var5);
         ImGui.pushStyleColor(i, (var6 & 0xFF) / 255.0F, (var6 >>> 8 & 0xFF) / 255.0F, (var6 >>> 16 & 0xFF) / 255.0F, j);
      }
   }

   public static float smoothRadius(float f, float g, float h, float i) {
      float var4 = Math.max(f, g) * 1.55F + 2.0F;
      return Math.max(0.0F, Math.min(var4, Math.min(h, i) * 0.5F));
   }

   public static void beginGlass(float f, boolean bl) {
      glass = true;
      glassOpacity = clamp01(f);
      glassTextShadow = bl;
   }

   public static void endGlass() {
      glass = false;
   }

   public static boolean glassActive() {
      return glass;
   }

   public static int f(int i) {
      if (a()) {
         i = tone(i);
      }

      float var1 = ImGui.getStyle().getAlpha();
      return var1 >= 0.999F ? i : alpha(i, var1);
   }

   public static int alpha(int i, float f) {
      int var2 = i >>> 24 & 0xFF;
      int var3 = Math.round(var2 * clamp01(f));
      return i & 16777215 | var3 << 24;
   }

   public static int withAlpha(int i, int j) {
      return i & 16777215 | Math.max(0, Math.min(255, j)) << 24;
   }

   public static int lerp(int i, int j, float f) {
      return d.lerpColor(i, j, clamp01(f));
   }

   public static int accent(float f) {
      return alpha(at.accent(), f);
   }

   public static int inkOnAccent() {
      int var0 = at.accent();
      int var1 = var0 & 0xFF;
      int var2 = var0 >> 8 & 0xFF;
      int var3 = var0 >> 16 & 0xFF;
      float var4 = 0.2126F * var1 + 0.7152F * var2 + 0.0722F * var3;
      return var4 > 150.0F ? -16119286 : -1;
   }

   public static float clamp01(float f) {
      return f < 0.0F ? 0.0F : (f > 1.0F ? 1.0F : f);
   }

   public static float approach(float f, float g, float h, float i) {
      float var4 = 1.0F - (float)Math.exp(-i * Math.min(h, 0.1F));
      float var5 = f + (g - f) * var4;
      return Math.abs(var5 - g) < 5.0E-4F ? g : var5;
   }

   public static float easeOutCubic(float f) {
      float var1 = 1.0F - clamp01(f);
      return 1.0F - var1 * var1 * var1;
   }

   public static float easeInCubic(float f) {
      float var1 = clamp01(f);
      return var1 * var1 * var1;
   }

   public static float easeOutBack(float f) {
      float var1 = clamp01(f);
      float var2 = 1.55F;
      float var3 = var1 - 1.0F;
      return var3 * var3 * ((var2 + 1.0F) * var3 + var2) + 1.0F;
   }

   public static float smoothstep(float f) {
      float var1 = clamp01(f);
      return var1 * var1 * (3.0F - 2.0F * var1);
   }

   private static void a(ImDrawList imDrawList, float f, float g, float h, float i, float j, float k) {
      float var7 = h - f;
      float var8 = i - g;
      imDrawList.pathClear();
      if (smooth) {
         imDrawList.pathRect(f, g, h, i, smoothRadius(j, k, var7, var8));
      } else {
         j = Math.max(0.0F, Math.min(j, Math.min(var7, var8) * 0.5F));
         k = Math.max(0.0F, Math.min(k, Math.min(var7, var8) * 0.5F));
         if (j > 0.5F) {
            imDrawList.pathArcTo(f + j, g + j, j, (float) Math.PI, (float) (Math.PI * 3.0 / 2.0), 6);
         } else {
            imDrawList.pathLineTo(f, g);
         }

         imDrawList.pathLineTo(h - k, g);
         imDrawList.pathLineTo(h, g + k);
         if (j > 0.5F) {
            imDrawList.pathArcTo(h - j, i - j, j, 0.0F, (float) (Math.PI / 2), 6);
         } else {
            imDrawList.pathLineTo(h, i);
         }

         imDrawList.pathLineTo(f + k, i);
         imDrawList.pathLineTo(f, i - k);
      }
   }

   public static void fillSlashed(ImDrawList imDrawList, float f, float g, float h, float i, float j, float k, int l) {
      l = f(l);
      if (!(h - f < 1.0F) && !(i - g < 1.0F) && (l >>> 24 & 0xFF) != 0) {
         a(imDrawList, f, g, h, i, j, k);
         imDrawList.pathFillConvex(l);
      }
   }

   public static void strokeSlashed(ImDrawList imDrawList, float f, float g, float h, float i, float j, float k, int l, float m) {
      l = f(l);
      if (!(h - f < 1.0F) && !(i - g < 1.0F) && (l >>> 24 & 0xFF) != 0) {
         a(imDrawList, f, g, h, i, j, k);
         imDrawList.pathStroke(l, 1, m);
      }
   }

   public static void shadow(ImDrawList imDrawList, float f, float g, float h, float i, float j, float k, float l) {
   }

   public static void glow(ImDrawList imDrawList, float f, float g, float h, float i, float j, float k, int l, float m) {
      if (!(m <= 0.01F)) {
         int var9 = l >>> 24 & 0xFF;

         for (int var10 = 1; var10 <= 4; var10++) {
            float var11 = var10 * 2.2F;
            float var12 = 1.0F - (var10 - 1) / 4.0F;
            int var13 = Math.round(var9 * 0.16F * m * var12 * var12);
            strokeSlashed(imDrawList, f - var11, g - var11, h + var11, i + var11, j + var11, k + var11 * 0.6F, withAlpha(l, var13), 2.4F);
         }
      }
   }

   public static void card(ImDrawList imDrawList, float f, float g, float h, float i, float j, float k, float l, boolean bl) {
      if (!(l <= 0.002F)) {
         flatCard(imDrawList, f, g, h, i, j, k, l, glass ? l * glassOpacity : l);
      }
   }

   public static void flatCard(ImDrawList imDrawList, float f, float g, float h, float i, float j, float k, float l, float m) {
      fillSlashed(imDrawList, f, g, f + h, g + i, j, k, alpha(-234091766, m));
      strokeSlashed(imDrawList, f + 0.5F, g + 0.5F, f + h - 0.5F, g + i - 0.5F, j, k, accent(0.3F * l), 1.0F);
   }

   public static void accentRim(ImDrawList imDrawList, float f, float g, float h, float i, float j, float k, float l) {
   }

   public static void accentRim(ImDrawList imDrawList, float f, float g, float h, float i, float j, float k, float l, c.a arg) {
   }

   public static float claw(ImDrawList imDrawList, float f, float g, float h, int i) {
      i = f(i);
      float var5 = Math.max(2.0F, h * 0.2F);
      float var6 = var5 * 0.55F;

      for (int var7 = 0; var7 < 3; var7++) {
         float var8 = f + var7 * (var5 + var6);
         float var9 = var7 == 1 ? h : h * 0.86F;
         float var10 = (var7 - 1) * var5 * 0.35F;
         float var11 = g + (var7 == 1 ? 0.0F : h * 0.02F);
         imDrawList.pathClear();
         imDrawList.pathLineTo(var8, var11);
         imDrawList.pathLineTo(var8 + var5, var11);
         imDrawList.pathLineTo(var8 + var5 * 0.86F + var10, var11 + var9 * 0.62F);
         imDrawList.pathLineTo(var8 + var5 * 0.5F + var10, var11 + var9);
         imDrawList.pathLineTo(var8 + var5 * 0.12F + var10, var11 + var9 * 0.55F);
         imDrawList.pathFillConvex(i);
      }

      return 3.0F * var5 + 2.0F * var6;
   }

   private static float a(float f) {
      return glass ? Math.max(f, 11.0F) : f;
   }

   public static float textWidth(ImFont imFont, float f, String string) {
      f = a(f);
      if (string == null || string.isEmpty()) {
         return 0.0F;
      } else if (imFont == null) {
         return ImGui.calcTextSize(string).x * (f / Math.max(1.0F, (float)ImGui.getFontSize()));
      } else {
         net.agartha.client.f.a var3 = net.agartha.client.f.a.getInstance();
         float var4 = var3.snappedSize(imFont, f);
         return var3.sized(imFont, f).calcTextSizeAX(var4, Float.MAX_VALUE, 0.0F, string);
      }
   }

   public static float midTop(ImFont imFont, float f, float g) {
      f = a(f);
      if (imFont == null) {
         return g - f * 0.5F;
      } else {
         net.agartha.client.f.a var3 = net.agartha.client.f.a.getInstance();
         if (capGeneration != var3.fontGeneration()) {
            capGeneration = var3.fontGeneration();
            CAP_MID.clear();
         }

         ImFont var4 = var3.sized(imFont, f);
         Float var5 = CAP_MID.get(var4);
         if (var5 == null) {
            ImFontGlyph var6 = var4.findGlyph(56);
            float var7 = var4.getFontSize();
            var5 = var6 != null && var6.ptr != 0L ? (var6.getY0() + var6.getY1()) * 0.5F : var7 * 0.5F;
            CAP_MID.put(var4, var5);
         }

         float var9 = var3.snappedSize(imFont, f);
         return g - var5 * (var9 / Math.max(1.0F, var4.getFontSize()));
      }
   }

   private static float a(ImFont imFont, float f) {
      f = a(f);
      if (imFont == null) {
         return f * 0.8F;
      } else {
         net.agartha.client.f.a var2 = net.agartha.client.f.a.getInstance();
         if (capBottomGeneration != var2.fontGeneration()) {
            capBottomGeneration = var2.fontGeneration();
            CAP_BOTTOM.clear();
         }

         ImFont var3 = var2.sized(imFont, f);
         Float var4 = CAP_BOTTOM.get(var3);
         if (var4 == null) {
            ImFontGlyph var5 = var3.findGlyph(56);
            var4 = var5 != null && var5.ptr != 0L ? var5.getY1() : var3.getFontSize() * 0.8F;
            CAP_BOTTOM.put(var3, var4);
         }

         return var4 * (var2.snappedSize(imFont, f) / Math.max(1.0F, var3.getFontSize()));
      }
   }

   public static float baselineTop(ImFont imFont, float f, ImFont imFont2, float g, float h) {
      return midTop(imFont2, g, h) + a(imFont2, g) - a(imFont, f);
   }

   public static void text(ImDrawList imDrawList, ImFont imFont, float f, float g, float h, int i, String string) {
      i = f(i);
      if (string != null && !string.isEmpty() && (i >>> 24 & 0xFF) != 0) {
         if (glass && glassTextShadow) {
            float var7 = Math.max(1.0F, (float)Math.round(f / 14.0F));
            int var8 = i >>> 24 & 0xFF;
            a(imDrawList, imFont, f, g + var7, h + var7, withAlpha(-16777216, Math.round(var8 * 0.7F)), string);
         }

         a(imDrawList, imFont, f, g, h, i, string);
      }
   }

   public static void textShadowed(ImDrawList imDrawList, ImFont imFont, float f, float g, float h, int i, String string) {
      int var7 = f(i);
      if (string != null && !string.isEmpty() && (var7 >>> 24 & 0xFF) != 0) {
         int var8 = var7 >>> 24 & 0xFF;
         a(imDrawList, imFont, f, g + 1.0F, h + 1.0F, withAlpha(-16777216, Math.round(var8 * 0.55F)), string);
         a(imDrawList, imFont, f, g, h, var7, string);
      }
   }

   private static void a(ImDrawList imDrawList, ImFont imFont, float f, float g, float h, int i, String string) {
      f = a(f);
      if (imFont != null) {
         net.agartha.client.f.a var7 = net.agartha.client.f.a.getInstance();
         imDrawList.addText(var7.sized(imFont, f), Math.round(var7.snappedSize(imFont, f)), Math.round(g), Math.round(h), i, string);
      } else {
         imDrawList.addText(Math.round(g), Math.round(h), i, string);
      }
   }

   public static float trackedWidth(ImFont imFont, float f, String string, float g) {
      if (string != null && !string.isEmpty()) {
         float var4 = 0.0F;

         for (int var5 = 0; var5 < string.length(); var5++) {
            var4 += textWidth(imFont, f, String.valueOf(string.charAt(var5)));
         }

         return var4 + g * (string.length() - 1);
      } else {
         return 0.0F;
      }
   }

   public static void tracked(ImDrawList imDrawList, ImFont imFont, float f, float g, float h, int i, String string, float j) {
      float var8 = g;

      for (int var9 = 0; var9 < string.length(); var9++) {
         String var10 = String.valueOf(string.charAt(var9));
         text(imDrawList, imFont, f, var8, h, i, var10);
         var8 += textWidth(imFont, f, var10) + j;
      }
   }

   public static String ellipsize(ImFont imFont, float f, String string, float g) {
      if (string == null) {
         return "";
      } else if (textWidth(imFont, f, string) <= g) {
         return string;
      } else {
         String var4 = "...";
         float var5 = textWidth(imFont, f, var4);
         if (var5 >= g) {
            return "";
         } else {
            int var6 = 0;
            int var7 = string.length();

            while (var6 < var7) {
               int var8 = var6 + var7 + 1 >>> 1;
               if (textWidth(imFont, f, string.substring(0, var8)) + var5 <= g) {
                  var6 = var8;
               } else {
                  var7 = var8 - 1;
               }
            }

            return string.substring(0, var6).stripTrailing() + var4;
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum a {
      TO_RIGHT,
      TO_LEFT,
      NONE;
   }
}
