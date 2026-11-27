package net.agartha.client.e;

import imgui.ImDrawList;
import imgui.ImFont;
import java.util.List;
import net.agartha.client.module.impl.client.HudModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class as {
   private static final float PAD_X = 9.0F;
   private static final float PAD_Y = 7.0F;
   private static final float HEAD_GAP = 6.0F;
   private static final float FACE = 19.0F;
   private static final float FACE_GAP = 7.0F;
   private static final float COUNT_GAP = 8.0F;
   private static final float NAME_SIZE = 19.0F;
   private static final float TITLE_SIZE = 10.0F;
   private static final float TITLE_TRACK = 1.0F;
   private static final float RADIUS = 6.0F;
   private static final float CUT = 7.0F;
   private static final float MIN_WIDTH_SHARE = 0.65F;
   private static final String TITLE = "STAFF";
   private static final String CLEAR = "- Clear";

   private as() {
   }

   private static int a(HudModule hudModule) {
      return hudModule == null ? 6 : hudModule.staffListRows();
   }

   private static float a(float f) {
      return Math.max(19.0F * f + 4.0F * f, 19.0F * f + 7.0F * f);
   }

   public static as.a measureSize(HudModule hudModule, float f, float g) {
      ImFont var3 = net.agartha.client.f.a.getInstance().hudValueFont();
      ImFont var4 = net.agartha.client.f.a.getInstance().hudLabelFont();
      List var5 = net.agartha.client.util.x.online();
      int var6 = Math.min(var5.size(), a(hudModule));
      boolean var7 = var5.size() > var6;
      float var8 = 19.0F * f;
      float var9 = 10.0F * f;
      float var10 = 19.0F * f + 7.0F * f;
      float var11 = c.trackedWidth(var4, var9, "STAFF", 1.0F * f) + 8.0F * f + c.textWidth(var3, var8, String.valueOf(var5.size()));
      if (var5.isEmpty()) {
         var11 += 8.0F * f + c.textWidth(var3, var8, "- Clear");
      }

      float var12 = var11;

      for (int var13 = 0; var13 < var6; var13++) {
         var12 = Math.max(var12, var10 + c.textWidth(var3, var8, a((net.agartha.client.util.x.a)var5.get(var13))));
      }

      if (var7) {
         var12 = Math.max(var12, var10 + c.textWidth(var3, var8, a(var5.size() - var6)));
      }

      int var18 = var6 + (var7 ? 1 : 0);
      float var14 = var12 + 18.0F * f;
      float var15 = Math.max(var14, 0.65F * Math.min(g * f, 400.0F * f));
      float var16 = Math.max(var9, var8) + 3.0F * f;
      float var17 = 14.0F * f + var16 + (var18 > 0 ? 6.0F * f + var18 * a(f) : 0.0F);
      return new as.a(var15, var17);
   }

   private static String a(int i) {
      return "+" + i + " more";
   }

   private static String a(net.agartha.client.util.x.a arg) {
      String var1 = arg.name();
      return net.agartha.client.util.x.normalise(var1).contains(net.agartha.client.util.x.normalise(arg.matched())) ? var1 : var1 + " (" + arg.matched() + ")";
   }

   public static void render(ImDrawList imDrawList, HudModule hudModule, t.c arg, float f, float g, float h) {
      if (imDrawList != null && arg != null) {
         float var6 = c.clamp01(h);
         if (!(var6 <= 0.002F)) {
            float var7 = v.scaleOf(arg);
            float var8 = arg.x + f;
            float var9 = arg.y + g;
            float var10 = arg.width;
            float var11 = arg.height;
            c.card(imDrawList, var8, var9, var10, var11, 6.0F * var7, 7.0F * var7, var6, true);
            c.accentRim(imDrawList, var8, var9, var10, 6.0F * var7, 7.0F * var7, 2.0F * var7, var6);
            ImFont var12 = net.agartha.client.f.a.getInstance().hudValueFont();
            ImFont var13 = net.agartha.client.f.a.getInstance().hudLabelFont();
            float var14 = 19.0F * var7;
            float var15 = 10.0F * var7;
            List var16 = net.agartha.client.util.x.online();
            float var17 = var8 + 9.0F * var7;
            float var18 = var8 + var10 - 9.0F * var7;
            float var19 = var9 + 7.0F * var7;
            float var20 = Math.max(var15, var14) + 3.0F * var7;
            c.tracked(imDrawList, var13, var15, var17, var19 + (var20 - var15) * 0.5F, c.alpha(-6119783, var6), "STAFF", 1.0F * var7);
            String var21 = String.valueOf(var16.size());
            float var22 = var17 + c.trackedWidth(var13, var15, "STAFF", 1.0F * var7) + 8.0F * var7;
            c.text(imDrawList, var12, var14, var22, var19 + (var20 - var14) * 0.5F, var16.isEmpty() ? c.alpha(-9738140, var6) : c.accent(var6), var21);
            if (var16.isEmpty()) {
               c.text(
                  imDrawList,
                  var12,
                  var14,
                  var22 + c.textWidth(var12, var14, var21) + 8.0F * var7,
                  var19 + (var20 - var14) * 0.5F,
                  c.alpha(-9738140, var6),
                  "- Clear"
               );
            } else {
               var19 += var20 + 6.0F * var7;
               float var23 = a(var7);
               float var24 = 19.0F * var7;
               int var25 = Math.min(var16.size(), a(hudModule));

               for (int var26 = 0; var26 < var25; var26++) {
                  net.agartha.client.util.x.a var27 = (net.agartha.client.util.x.a)var16.get(var26);
                  float var28 = var19 + var23 * 0.5F;
                  float var29 = var28 - var24 * 0.5F;
                  int var30 = net.agartha.client.e.a_o.getInstance().textureIdFor(var27.id());
                  if (var30 >= 0) {
                     imDrawList.addImage(var30, var17, var29, var17 + var24, var29 + var24, 0.0F, 0.0F, 1.0F, 1.0F, c.alpha(-1, var6));
                  } else {
                     imDrawList.addRectFilled(var17, var29, var17 + var24, var29 + var24, c.alpha(-15461614, var6), 2.0F * var7);
                  }

                  imDrawList.addRect(var17, var29, var17 + var24, var29 + var24, c.alpha(620756991, var6), 2.0F * var7, 0, 1.0F);
                  float var31 = var17 + var24 + 7.0F * var7;
                  c.text(imDrawList, var12, var14, var31, var28 - var14 * 0.55F, c.alpha(-526345, var6), c.ellipsize(var12, var14, a(var27), var18 - var31));
                  var19 += var23;
               }

               if (var16.size() > var25) {
                  c.text(
                     imDrawList,
                     var12,
                     var14,
                     var17 + var24 + 7.0F * var7,
                     var19 + var23 * 0.5F - var14 * 0.55F,
                     c.alpha(-9738140, var6),
                     a(var16.size() - var25)
                  );
               }
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public record a(float width, float height) {
   }
}
