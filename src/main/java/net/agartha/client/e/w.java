package net.agartha.client.e;

import imgui.ImDrawList;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
final class w {
   static final float EDGE_MARGIN = 5.0F;
   static final float SNAP_THRESHOLD = 8.0F;

   private w() {
   }

   static w.b snapPosition(String string, float f, float g, float h, float i, float j, float k, t arg) {
      List var8 = a(string, h, i, j, k, arg);
      float var9 = f;
      float var10 = g;
      float var11 = f + h;
      float var12 = g + i;
      float var13 = f + h * 0.5F;
      float var14 = g + i * 0.5F;
      float var15 = 0.0F;
      float var16 = 0.0F;
      float var17 = 9.0F;
      float var18 = 9.0F;
      w.a var19 = null;
      w.a var20 = null;

      for (w.a var22 : (Iterable<w.a>) var8) {
         if (var22.vertical()) {
            float[] var31 = new float[]{var9, var11, var13};

            for (float var35 : var31) {
               float var36 = var22.pos() - var35;
               float var37 = Math.abs(var36);
               if (var37 <= 8.0F && var37 < var17) {
                  var17 = var37;
                  var15 = var36;
                  var19 = var22;
               }
            }
         } else {
            float[] var23 = new float[]{var10, var12, var14};

            for (float var27 : var23) {
               float var28 = var22.pos() - var27;
               float var29 = Math.abs(var28);
               if (var29 <= 8.0F && var29 < var18) {
                  var18 = var29;
                  var16 = var28;
                  var20 = var22;
               }
            }
         }
      }

      ArrayList var30 = new ArrayList(2);
      if (var19 != null) {
         var30.add(var19);
      }

      if (var20 != null) {
         var30.add(var20);
      }

      return new w.b(f + var15, g + var16, var30);
   }

   private static List<w.a> a(String string, float f, float g, float h, float i, t arg) {
      ArrayList var6 = new ArrayList();
      var6.add(new w.a(5.0F, true));
      var6.add(new w.a(h - 5.0F, true));
      var6.add(new w.a(5.0F, false));
      var6.add(new w.a(i - 5.0F, false));
      var6.add(new w.a(h * 0.5F, true));

      for (t.c var8 : arg.orderedElements()) {
         if (!var8.id.equals(string) && v.isDrawn(var8.id) && !(var8.width <= 0.0F) && !(var8.height <= 0.0F)) {
            float var9 = var8.x;
            float var10 = var8.y;
            float var11 = var8.x + var8.width;
            float var12 = var8.y + var8.height;
            float var13 = var8.x + var8.width * 0.5F;
            float var14 = var8.y + var8.height * 0.5F;
            var6.add(new w.a(var9, true));
            var6.add(new w.a(var11, true));
            var6.add(new w.a(var13, true));
            var6.add(new w.a(var10, false));
            var6.add(new w.a(var12, false));
            var6.add(new w.a(var14, false));
         }
      }

      return var6;
   }

   static void drawGuides(ImDrawList imDrawList, List<w.a> list, float f, float g, int i) {
      if (list != null && !list.isEmpty()) {
         for (w.a var6 : list) {
            if (var6.vertical()) {
               imDrawList.addLine(var6.pos(), 0.0F, var6.pos(), g, i, 1.0F);
            } else {
               imDrawList.addLine(0.0F, var6.pos(), f, var6.pos(), i, 1.0F);
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   record a(float pos, boolean vertical) {
   }

   @Environment(EnvType.CLIENT)
   record b(float x, float y, List<w.a> activeGuides) {
   }
}
