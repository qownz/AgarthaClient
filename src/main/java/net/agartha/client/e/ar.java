package net.agartha.client.e;

import imgui.ImDrawList;
import imgui.ImFont;
import net.agartha.client.module.impl.misc.StaffAlertModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ar {
   private static final float CARD_H = 86.0F;
   private static final float PAD_X = 22.0F;
   private static final float RADIUS = 6.0F;
   private static final float CUT = 10.0F;
   private static final float LABEL_SIZE = 13.0F;
   private static final float NAME_SIZE = 24.0F;
   private static final float DETAIL_SIZE = 15.0F;

   private ar() {
   }

   public static boolean active() {
      StaffAlertModule var0 = net.agartha.client.module.e.get(StaffAlertModule.class);
      return var0 != null && var0.bannerActive();
   }

   public static void render(ImDrawList imDrawList, float f, float g) {
      StaffAlertModule var3 = net.agartha.client.module.e.get(StaffAlertModule.class);
      if (imDrawList != null && var3 != null && var3.bannerActive()) {
         float var4 = var3.bannerAlpha();
         if (!(var4 <= 0.01F)) {
            ImFont var5 = net.agartha.client.f.a.getInstance().hudValueFont();
            String var6 = var3.bannerName();
            String var7 = var3.bannerDetail();
            float var8 = c.textWidth(var5, 24.0F, var6);
            float var9 = c.textWidth(var5, 15.0F, var7);
            float var10 = Math.max(280.0F, Math.max(var8, var9) + 44.0F);
            float var11 = (f - var10) * 0.5F;
            float var12 = g * 0.5F - 124.0F;
            c.card(imDrawList, var11, var12, var10, 86.0F, 6.0F, 10.0F, var4, true);
            c.accentRim(imDrawList, var11, var12, var10, 6.0F, 10.0F, 2.0F, var4);
            float var13 = c.textWidth(var5, 13.0F, "STAFF");
            c.text(imDrawList, var5, 13.0F, var11 + (var10 - var13) * 0.5F, var12 + 10.0F, c.alpha(c.accent(1.0F), var4), "STAFF");
            c.text(imDrawList, var5, 24.0F, var11 + (var10 - var8) * 0.5F, var12 + 28.0F, c.alpha(-526345, var4), var6);
            c.text(imDrawList, var5, 15.0F, var11 + (var10 - var9) * 0.5F, var12 + 56.0F, c.alpha(-6119783, var4), var7);
         }
      }
   }
}
