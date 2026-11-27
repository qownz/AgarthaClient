package net.agartha.client.e;

import imgui.ImDrawList;
import imgui.ImFont;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class af {
   private static final float PAD_X = 9.0F;
   private static final float PAD_Y = 7.0F;
   private static final float HEAD_GAP = 6.0F;
   private static final float COL_GAP = 16.0F;
   private static final float NAME_SIZE = 16.0F;
   private static final float KEY_SIZE = 14.0F;
   private static final float TITLE_SIZE = 10.0F;
   private static final float TITLE_TRACK = 1.0F;
   private static final float RADIUS = 6.0F;
   private static final float CUT = 7.0F;
   private static final float DOT = 3.0F;
   private static final float DOT_GAP = 9.0F;
   private static final float MIN_WIDTH_SHARE = 0.6F;
   private static final String TITLE = "KEYBINDS";
   private static final String EMPTY = "No keybinds";
   private static final float MIN_NAME_SIZE = 14.0F;
   private static final float MIN_KEY_SIZE = 13.0F;
   private static final float MIN_PAD_X = 9.0F;
   private static final float MIN_PAD_Y = 6.0F;
   private static final float MIN_COL_GAP = 12.0F;
   private static final float MIN_DOT = 2.2F;
   private static final float MIN_DOT_GAP = 7.0F;

   private af() {
   }

   private static List<net.agartha.client.module.b> a() {
      return net.agartha.client.module.e.keyboundModules();
   }

   public static boolean shouldDraw(boolean bl) {
      return bl || !a().isEmpty();
   }

   private static float a(float f) {
      return Math.max(20.0F * f, 16.0F * f + 7.0F * f);
   }

   private static float b(float f) {
      return 14.0F * f + 7.0F * f;
   }

   public static af.a measureSize(boolean bl, float f, float g, boolean bl2) {
      if (bl2) {
         af.a var4 = a(bl, f);
         return g > 0.0F ? new af.a(g, var4.height()) : var4;
      } else {
         return measureSize(bl, f, g);
      }
   }

   private static af.a a(boolean bl, float f) {
      ImFont var2 = net.agartha.client.f.a.getInstance().hudValueFont();
      float var3 = 4.4F * f + 7.0F * f;
      List var4 = a();
      float var5 = 0.0F;

      for (net.agartha.client.module.b var7 : (Iterable<net.agartha.client.module.b>) var4) {
         var5 = Math.max(
            var5, var3 + c.textWidth(var2, 14.0F * f, var7.name()) + 12.0F * f + c.textWidth(var2, 13.0F * f, net.agartha.client.j.e.keyName(var7.keybind()))
         );
      }

      int var8 = var4.size();
      if (var8 == 0) {
         var5 = var3 + c.textWidth(var2, 14.0F * f, "No keybinds");
         var8 = 1;
      }

      return new af.a(var5 + 18.0F * f, 12.0F * f + var8 * b(f));
   }

   public static af.a measureSize(boolean bl, float f, float g) {
      ImFont var3 = net.agartha.client.f.a.getInstance().hudValueFont();
      ImFont var4 = net.agartha.client.f.a.getInstance().hudLabelFont();
      float var5 = 16.0F * f;
      float var6 = 14.0F * f;
      float var7 = 10.0F * f;
      float var8 = 6.0F * f + 9.0F * f;
      List var9 = a();
      float var10 = c.trackedWidth(var4, var7, "KEYBINDS", 1.0F * f);

      for (net.agartha.client.module.b var12 : (Iterable<net.agartha.client.module.b>) var9) {
         var10 = Math.max(
            var10, var8 + c.textWidth(var3, var5, var12.name()) + 16.0F * f + c.textWidth(var3, var6, net.agartha.client.j.e.keyName(var12.keybind()))
         );
      }

      int var16 = var9.size();
      if (var16 == 0) {
         var10 = Math.max(var10, var8 + c.textWidth(var3, var5, "No keybinds"));
         var16 = bl ? 1 : 0;
      }

      float var17 = var10 + 18.0F * f;
      float var13 = Math.max(var17, 0.6F * Math.min(g * f, 400.0F * f));
      float var14 = Math.max(var7, var5) + 3.0F * f;
      float var15 = 14.0F * f + var14 + (var16 > 0 ? 6.0F * f + var16 * a(f) : 0.0F);
      return new af.a(var13, var15);
   }

   public static void render(ImDrawList imDrawList, t.c arg, boolean bl, float f, float g, float h, boolean bl2) {
      if (bl2) {
         a(imDrawList, arg, bl, f, g, h);
      } else {
         render(imDrawList, arg, bl, f, g, h);
      }
   }

   private static void a(ImDrawList imDrawList, t.c arg, boolean bl, float f, float g, float h) {
      if (imDrawList != null && arg != null) {
         float var6 = c.clamp01(h);
         if (!(var6 <= 0.002F) && shouldDraw(bl)) {
            float var7 = v.scaleOf(arg);
            float var8 = arg.x + f;
            float var9 = arg.y + g;
            c.card(imDrawList, var8, var9, arg.width, arg.height, 4.0F * var7, 5.0F * var7, var6, true);
            ImFont var10 = net.agartha.client.f.a.getInstance().hudValueFont();
            float var11 = 14.0F * var7;
            float var12 = 13.0F * var7;
            float var13 = var8 + 9.0F * var7;
            float var14 = var8 + arg.width - 9.0F * var7;
            float var15 = b(var7);
            float var16 = var9 + 6.0F * var7;
            float var17 = var13 + 4.4F * var7 + 7.0F * var7;
            List var18 = a();
            if (var18.isEmpty()) {
               c.text(imDrawList, var10, var11, var17, c.midTop(var10, var11, var16 + var15 * 0.5F), c.alpha(-9738140, var6), "No keybinds");
            } else {
               for (net.agartha.client.module.b var20 : (Iterable<net.agartha.client.module.b>) var18) {
                  boolean var21 = var20.isEnabled();
                  float var22 = var16 + var15 * 0.5F;
                  imDrawList.addCircleFilled(var13 + 2.2F * var7, var22, 2.2F * var7, var21 ? c.f(c.accent(var6)) : c.f(c.alpha(-9738140, var6)), 12);
                  String var23 = net.agartha.client.j.e.keyName(var20.keybind());
                  float var24 = c.textWidth(var10, var12, var23);
                  float var25 = Math.max(8.0F, var14 - var24 - 12.0F * var7 - var17);
                  c.text(
                     imDrawList,
                     var10,
                     var11,
                     var17,
                     c.midTop(var10, var11, var22),
                     c.alpha(var21 ? -526345 : -6119783, var6),
                     c.ellipsize(var10, var11, var20.name(), var25)
                  );
                  c.text(imDrawList, var10, var12, var14 - var24, c.midTop(var10, var12, var22), var21 ? c.accent(var6) : c.alpha(-9738140, var6), var23);
                  var16 += var15;
               }
            }
         }
      }
   }

   public static void render(ImDrawList imDrawList, t.c arg, boolean bl, float f, float g, float h) {
      if (imDrawList != null && arg != null) {
         float var6 = c.clamp01(h);
         if (!(var6 <= 0.002F) && shouldDraw(bl)) {
            float var7 = v.scaleOf(arg);
            float var8 = arg.x + f;
            float var9 = arg.y + g;
            float var10 = arg.width;
            float var11 = arg.height;
            c.card(imDrawList, var8, var9, var10, var11, 6.0F * var7, 7.0F * var7, var6, true);
            c.accentRim(imDrawList, var8, var9, var10, 6.0F * var7, 7.0F * var7, 2.0F * var7, var6);
            ImFont var12 = net.agartha.client.f.a.getInstance().hudValueFont();
            ImFont var13 = net.agartha.client.f.a.getInstance().hudLabelFont();
            float var14 = 16.0F * var7;
            float var15 = 14.0F * var7;
            float var16 = 10.0F * var7;
            float var17 = var8 + 9.0F * var7;
            float var18 = var8 + var10 - 9.0F * var7;
            float var19 = var9 + 7.0F * var7;
            float var20 = Math.max(var16, var14) + 3.0F * var7;
            c.tracked(imDrawList, var13, var16, var17, var19 + (var20 - var16) * 0.5F, c.alpha(-6119783, var6), "KEYBINDS", 1.0F * var7);
            List var21 = a();
            var19 += var20 + 6.0F * var7;
            float var22 = a(var7);
            if (var21.isEmpty()) {
               if (bl) {
                  c.text(
                     imDrawList, var12, var14, var17 + 6.0F * var7 + 9.0F * var7, var19 + var22 * 0.5F - var14 * 0.55F, c.alpha(-9738140, var6), "No keybinds"
                  );
               }
            } else {
               for (net.agartha.client.module.b var24 : (Iterable<net.agartha.client.module.b>) var21) {
                  boolean var25 = var24.isEnabled();
                  float var26 = var19 + var22 * 0.5F;
                  int var27 = var25 ? c.accent(var6) : c.alpha(-9738140, var6);
                  if (var25) {
                     imDrawList.addCircleFilled(var17 + 3.0F * var7, var26, 6.0F * var7, c.accent(var6 * 0.22F), 16);
                  }

                  imDrawList.addCircleFilled(var17 + 3.0F * var7, var26, 3.0F * var7, var27, 14);
                  String var28 = net.agartha.client.j.e.keyName(var24.keybind());
                  float var29 = c.textWidth(var12, var15, var28);
                  float var30 = var17 + 6.0F * var7 + 9.0F * var7;
                  float var31 = Math.max(8.0F, var18 - var29 - 16.0F * var7 - var30);
                  c.text(
                     imDrawList,
                     var12,
                     var14,
                     var30,
                     var26 - var14 * 0.55F,
                     c.alpha(var25 ? -526345 : -6119783, var6),
                     c.ellipsize(var12, var14, var24.name(), var31)
                  );
                  c.text(imDrawList, var12, var15, var18 - var29, var26 - var15 * 0.55F, var25 ? c.accent(var6) : c.alpha(-9738140, var6), var28);
                  var19 += var22;
               }
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public record a(float width, float height) {
   }
}
