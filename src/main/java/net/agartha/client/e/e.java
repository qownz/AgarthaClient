package net.agartha.client.e;

import imgui.ImDrawList;
import imgui.ImGui;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;

@Environment(EnvType.CLIENT)
public final class e {
   private static final Map<Integer, Float> HOVER = new HashMap<>();
   private static final Map<Integer, Float> VALUE = new HashMap<>();
   private static final Map<Integer, Integer> NOTCH = new HashMap<>();
   private static final Set<Integer> HOVERED = new HashSet<>();
   private static final int SLIDER_NOTCHES = 20;
   private static final float SLIDER_HIT_H = 16.0F;
   private static final float TRACK_H = 4.0F;

   private e() {
   }

   private static void a(int i, boolean bl) {
      if (bl) {
         if (HOVERED.add(i)) {
            au.hover();
         }
      } else {
         HOVERED.remove(i);
      }
   }

   private static float a() {
      return Math.max(ImGui.getIO().getDeltaTime(), 0.001F);
   }

   private static float a(Map<Integer, Float> map, int i, float f, float g) {
      Float var4 = (Float)map.get(i);
      float var5 = var4 == null ? f : c.approach(var4, f, a(), g);
      map.put(i, var5);
      return var5;
   }

   public static boolean sliderFloat(String string, float[] fs, float f, float g, float h) {
      float var5 = ImGui.getCursorScreenPosX();
      float var6 = ImGui.getCursorScreenPosY();
      ImGui.invisibleButton(string, h, 16.0F);
      int var7 = ImGui.getItemID();
      boolean var8 = ImGui.isItemHovered();
      boolean var9 = ImGui.isItemActive();
      boolean var10 = false;
      if (var9) {
         float var11 = c.clamp01((ImGui.getIO().getMousePosX() - var5) / Math.max(1.0F, h));
         float var12 = f + var11 * (g - f);
         if (var12 != fs[0]) {
            fs[0] = var12;
            var10 = true;
         }
      }

      float var20 = a(fs[0], f, g);
      if (var9) {
         int var21 = Math.round(var20 * 20.0F);
         Integer var13 = NOTCH.put(var7, var21);
         if (var13 != null && var13 != var21) {
            au.sliderTick(var20);
         }
      } else {
         NOTCH.remove(var7);
      }

      float var22 = var9 ? var20 : a(VALUE, var7, var20, 18.0F);
      if (var9) {
         VALUE.put(var7, var20);
      }

      float var23 = a(HOVER, var7, !var8 && !var9 ? 0.0F : 1.0F, 16.0F);
      ImDrawList var14 = ImGui.getWindowDrawList();
      float var15 = var6 + 8.0F;
      float var16 = var15 - 2.0F;
      float var17 = var5 + var22 * h;
      var14.addRectFilled(var5, var16, var5 + h, var16 + 4.0F, c.f(-14540513), 2.0F);
      if (var17 > var5 + 0.5F) {
         var14.addRectFilledMultiColor(var5, var16, var17, var16 + 4.0F, c.f(c.accent(0.45F)), c.f(c.accent(1.0F)), c.f(c.accent(1.0F)), c.f(c.accent(0.45F)));
      }

      float var18 = 6.0F + var23 * 2.0F;
      float var19 = 12.0F + var23 * 2.0F;
      if (var23 > 0.01F) {
         var14.addCircleFilled(var17, var15, 9.0F * var23, c.f(c.accent(0.16F * var23)), 20);
      }

      var14.addRectFilled(
         var17 - var18 * 0.5F + 0.5F, var15 - var19 * 0.5F + 1.5F, var17 + var18 * 0.5F + 0.5F, var15 + var19 * 0.5F + 1.5F, c.f(1711276032), var18 * 0.5F
      );
      var14.addRectFilled(
         var17 - var18 * 0.5F, var15 - var19 * 0.5F, var17 + var18 * 0.5F, var15 + var19 * 0.5F, c.f(var9 ? at.accent() : -723724), var18 * 0.5F
      );
      return var10;
   }

   public static boolean chipButton(String string, String string2, float f, float g, boolean bl) {
      return chipButton(string, string2, f, g, bl, null);
   }

   public static boolean chipButton(String string, String string2, float f, float g, boolean bl, String string3) {
      float var6 = ImGui.getCursorPosX();
      float var7 = ImGui.getCursorPosY();
      float var8 = ImGui.getCursorScreenPosX();
      float var9 = ImGui.getCursorScreenPosY();
      ImGui.invisibleButton(string, f, g);
      boolean var10 = ImGui.isItemClicked(0);
      int var11 = ImGui.getItemID();
      boolean var12 = ImGui.isItemHovered();
      boolean var13 = ImGui.isItemActive();
      float var14 = a(HOVER, var11, var12 ? 1.0F : 0.0F, 18.0F);
      a(var11, var12);
      if (var10) {
         au.click();
      }

      if (var12 && string3 != null && !string3.isEmpty()) {
         ImGui.pushStyleVar(2, 14.0F, 10.0F);
         ImGui.beginTooltip();
         ImGui.pushTextWrapPos(ImGui.getFontSize() * 28.0F);
         ImGui.textUnformatted(string3);
         ImGui.popTextWrapPos();
         ImGui.endTooltip();
         ImGui.popStyleVar();
      }

      ImDrawList var15 = ImGui.getWindowDrawList();
      float var16 = Math.min(5.0F, g * 0.3F);
      int var17;
      int var18;
      if (!bl && !var13) {
         var17 = c.lerp(-15330028, -14672099, var14);
         var18 = c.lerp(536870911, 1040187391, var14);
      } else {
         var17 = at.accentWithAlpha(var13 ? 85 : Math.round(48.0F + 24.0F * var14));
         var18 = at.accentWithAlpha(Math.round(144.0F + 64.0F * var14));
      }

      c.fillSlashed(var15, var8, var9, var8 + f, var9 + g, 3.0F, var16, var17);
      c.strokeSlashed(var15, var8 + 0.5F, var9 + 0.5F, var8 + f - 0.5F, var9 + g - 0.5F, 3.0F, var16, var18, 1.0F);
      float var19 = ImGui.calcTextSize(string2).x;
      float var20 = ImGui.getTextLineHeight();
      String var21 = var19 > f - 8.0F ? c.ellipsize(ImGui.getFont(), ImGui.getFontSize(), string2, f - 8.0F) : string2;
      var19 = ImGui.calcTextSize(var21).x;
      int var22 = !bl && !var13 ? c.lerp(-3159098, -1, var14) : at.accent();
      var15.addText(Math.round(var8 + (f - var19) * 0.5F), Math.round(var9 + (g - var20) * 0.5F), c.f(var22), var21);
      ImGui.setCursorPos(var6, var7 + g + 4.0F);
      return var10;
   }

   public static boolean valueChip(String string, String string2, String string3, float f, float g) {
      float var5 = ImGui.getCursorPosX();
      float var6 = ImGui.getCursorPosY();
      float var7 = ImGui.getCursorScreenPosX();
      float var8 = ImGui.getCursorScreenPosY();
      ImGui.invisibleButton(string, f, g);
      boolean var9 = ImGui.isItemClicked(0);
      int var10 = ImGui.getItemID();
      float var11 = a(HOVER, var10, ImGui.isItemHovered() ? 1.0F : 0.0F, 18.0F);
      a(var10, ImGui.isItemHovered());
      if (var9) {
         au.click();
      }

      ImDrawList var12 = ImGui.getWindowDrawList();
      float var13 = Math.min(5.0F, g * 0.3F);
      c.fillSlashed(var12, var7, var8, var7 + f, var8 + g, 3.0F, var13, c.lerp(-15330028, -14672099, var11));
      c.strokeSlashed(var12, var7 + 0.5F, var8 + 0.5F, var7 + f - 0.5F, var8 + g - 0.5F, 3.0F, var13, c.lerp(536870911, 1040187391, var11), 1.0F);
      float var14 = ImGui.getTextLineHeight();
      float var15 = Math.round(var8 + (g - var14) * 0.5F);
      float var16 = 10.0F;
      float var17 = ImGui.calcTextSize(string3).x;
      float var18 = var7 + f - 9.0F - var16 - var17;
      String var19 = c.ellipsize(ImGui.getFont(), ImGui.getFontSize(), string2, Math.max(0.0F, var18 - var7 - 16.0F));
      var12.addText(Math.round(var7 + 8.0F), var15, c.f(c.lerp(-6119783, -2040359, var11)), var19);
      var12.addText(Math.round(var18), var15, c.f(at.accent()), string3);
      float var20 = var7 + f - 8.0F - var16 * 0.5F + var11 * 1.5F;
      float var21 = var8 + g * 0.5F;
      var12.addLine(var20 - 2.0F, var21 - 3.5F, var20 + 1.5F, var21, c.f(c.accent(0.8F)), 1.5F);
      var12.addLine(var20 + 1.5F, var21, var20 - 2.0F, var21 + 3.5F, c.f(c.accent(0.8F)), 1.5F);
      ImGui.setCursorPos(var5, var6 + g + 4.0F);
      return var9;
   }

   public static boolean itemChip(String string, String string2, String string3, Item[] items, int i, float f, float g) {
      float var7 = ImGui.getCursorPosX();
      float var8 = ImGui.getCursorPosY();
      float var9 = ImGui.getCursorScreenPosX();
      float var10 = ImGui.getCursorScreenPosY();
      ImGui.invisibleButton(string, f, g);
      boolean var11 = ImGui.isItemClicked(0);
      int var12 = ImGui.getItemID();
      float var13 = a(HOVER, var12, ImGui.isItemHovered() ? 1.0F : 0.0F, 18.0F);
      a(var12, ImGui.isItemHovered());
      if (var11) {
         au.click();
      }

      ImDrawList var14 = ImGui.getWindowDrawList();
      float var15 = Math.min(5.0F, g * 0.3F);
      c.fillSlashed(var14, var9, var10, var9 + f, var10 + g, 3.0F, var15, c.lerp(-15330028, -14672099, var13));
      c.strokeSlashed(var14, var9 + 0.5F, var10 + 0.5F, var9 + f - 0.5F, var10 + g - 0.5F, 3.0F, var15, c.lerp(536870911, 1040187391, var13), 1.0F);
      float var16 = ImGui.getTextLineHeight();
      float var17 = Math.round(var10 + (g - var16) * 0.5F);
      float var18 = 10.0F;
      float var19 = ImGui.calcTextSize(string3).x;
      float var20 = var9 + f - 9.0F - var18 - var19;
      float var21 = Math.max(10.0F, g - 6.0F);
      float var22 = var21 * 0.78F;
      int var23 = Math.min(i, items == null ? 0 : items.length);
      float var24 = var23 == 0 ? 0.0F : var21 + var22 * (var23 - 1) + 6.0F;
      float var25 = var20 - var24;

      for (int var26 = var23 - 1; var26 >= 0; var26--) {
         ab.draw(var14, items[var26], var25 + var22 * var26, var10 + (g - var21) * 0.5F, var21, 1.0F);
      }

      String var29 = c.ellipsize(ImGui.getFont(), ImGui.getFontSize(), string2, Math.max(0.0F, var25 - var9 - 16.0F));
      var14.addText(Math.round(var9 + 8.0F), var17, c.f(c.lerp(-6119783, -2040359, var13)), var29);
      var14.addText(Math.round(var20), var17, c.f(at.accent()), string3);
      float var27 = var9 + f - 8.0F - var18 * 0.5F + var13 * 1.5F;
      float var28 = var10 + g * 0.5F;
      var14.addLine(var27 - 2.0F, var28 - 3.5F, var27 + 1.5F, var28, c.f(c.accent(0.8F)), 1.5F);
      var14.addLine(var27 + 1.5F, var28, var27 - 2.0F, var28 + 3.5F, c.f(c.accent(0.8F)), 1.5F);
      ImGui.setCursorPos(var7, var8 + g + 4.0F);
      return var11;
   }

   public static boolean checkbox(String string, boolean bl, float f) {
      float var3 = ImGui.getCursorScreenPosX();
      float var4 = ImGui.getCursorScreenPosY();
      ImGui.invisibleButton(string, f, f);
      boolean var5 = ImGui.isItemClicked(0);
      int var6 = ImGui.getItemID();
      boolean var7 = ImGui.isItemHovered();
      if (var5) {
         au.toggle(!bl);
      }

      float var8 = a(VALUE, var6, bl != var5 ? 1.0F : 0.0F, 16.0F);
      float var9 = a(HOVER, var6, var7 ? 1.0F : 0.0F, 18.0F);
      drawCheck(ImGui.getWindowDrawList(), var3, var4, f, var8, var9);
      return var5;
   }

   public static void drawCheck(ImDrawList imDrawList, float f, float g, float h, float i, float j) {
      float var6 = 3.5F;
      imDrawList.addRectFilled(f, g, f + h, g + h, c.f(c.lerp(-15001063, -14408929, j)), var6);
      imDrawList.addRect(f + 0.5F, g + 0.5F, f + h - 0.5F, g + h - 0.5F, c.f(c.lerp(c.lerp(788529151, 1442840575, j), at.accent(), i)), var6, 0, 1.2F);
      if (i > 0.01F) {
         float var7 = (1.0F - i) * h * 0.5F;
         imDrawList.addRectFilled(f + var7, g + var7, f + h - var7, g + h - var7, c.f(at.accentWithAlpha(Math.round(255.0F * i))), var6);
         int var8 = c.alpha(c.inkOnAccent(), c.clamp01((i - 0.35F) / 0.65F));
         float var9 = f + h * 0.5F;
         float var10 = g + h * 0.5F;
         float var11 = h * 0.22F;
         imDrawList.pathClear();
         imDrawList.pathLineTo(var9 - var11 * 1.25F, var10 + var11 * 0.05F);
         imDrawList.pathLineTo(var9 - var11 * 0.35F, var10 + var11 * 0.95F);
         imDrawList.pathLineTo(var9 + var11 * 1.35F, var10 - var11 * 0.85F);
         imDrawList.pathStroke(c.f(var8), 0, 1.8F);
      }
   }

   private static float a(float f, float g, float h) {
      float var3 = h - g;
      return var3 < 1.0E-6F ? 0.0F : c.clamp01((f - g) / var3);
   }
}
