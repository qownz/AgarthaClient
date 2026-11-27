package net.agartha.client.gui;

import imgui.ImDrawList;
import imgui.ImFont;
import net.agartha.client.gui.BellTexture;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.client.NotificationsModule;
import net.agartha.client.render.imgui.ImGuiManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class NotificationToast {
   private static final float EDGE_MARGIN = 14.0F;
   private static final float CARD_H = 58.0F;
   private static final float MIN_CARD_W = 250.0F;
   private static final float PAD_X = 14.0F;
   private static final float BELL_H = 34.0F;
   private static final float SLIDE_PX = 64.0F;
   private static final float FONT_SIZE = 20.0F;
   private static final float RADIUS = 6.0F;
   private static final float CUT = 9.0F;
   private static final int COL_ON = -9641876;
   private static final int COL_OFF = -10724097;

   private NotificationToast() {
   }

   public static boolean shouldRender() {
      NotificationsModule var0 = ModuleManager.get(NotificationsModule.class);
      return var0 != null && var0.isEnabled() && NotificationManager.getInstance().snapshot() != null;
   }

   public static void render(ImDrawList imDrawList, float f, float g, float h) {
      if (shouldRender()) {
         NotificationsModule var4 = ModuleManager.get(NotificationsModule.class);
         if (var4 != null) {
            NotificationManager var5 = NotificationManager.getInstance();
            var5.tick(h);
            NotificationManager.c var6 = var5.snapshot();
            if (var6 != null) {
               ImFont var7 = ImGuiManager.getInstance().hudValueFont();
               String var8 = var6.message();
               float var9 = 34.0F * a();
               float var10 = DrawUtils.textWidth(var7, 20.0F, var8);
               float var11 = Math.max(250.0F, 14.0F + var9 + 14.0F + var10 + 22.4F);
               String var12 = var4.notificationCorner();
               boolean var13 = var12.startsWith("Top");
               boolean var14 = var12.endsWith("Right");
               float var15 = var6.phase() == NotificationManager.b.HOLD
                  ? 1.0F
                  : (var6.phase() == NotificationManager.b.ENTER ? DrawUtils.easeOutCubic(var6.progress()) : 1.0F - DrawUtils.easeInCubic(var6.progress()));
               float var16 = DrawUtils.clamp01(var15);
               float var17 = (1.0F - var16) * 64.0F;
               float var18 = var14 ? f - var11 - 14.0F : 14.0F;
               float var19 = var13 ? 14.0F - var17 : g - 58.0F - 14.0F + var17;
               DrawUtils.card(imDrawList, var18, var19, var11, 58.0F, 6.0F, 9.0F, var16, true);
               DrawUtils.accentRim(imDrawList, var18, var19, var11, 6.0F, 9.0F, 2.0F, var16);
               float var20 = var19 + 26.0F + 1.0F;
               float var21 = var18 + 14.0F;
               BellTexture var22 = BellTexture.getInstance();
               if (var22.isReady()) {
                  imDrawList.addImage(var22.textureId(), var21, var20 - 17.0F, var21 + var9, var20 + 17.0F, 0.0F, 0.0F, 1.0F, 1.0F, DrawUtils.alpha(-1, var16));
               } else {
                  DrawUtils.claw(imDrawList, var21, var20 - 11.9F, 23.8F, DrawUtils.accent(var16));
               }

               float var23 = var21 + var9 + 14.0F;
               float var24 = var20 - 11.0F;
               a(imDrawList, var7, var23, var24, var8, var16);
               if (var6.phase() == NotificationManager.b.HOLD || var6.phase() == NotificationManager.b.ENTER) {
                  float var25 = DrawUtils.clamp01(var6.holdRemaining());
                  float var26 = var18 + 6.0F + 4.0F;
                  float var27 = var18 + var11 - 9.0F - 6.0F;
                  float var28 = var19 + 58.0F - 7.0F;
                  imDrawList.addRectFilled(var26, var28, var27, var28 + 2.0F, DrawUtils.alpha(620756991, var16), 1.0F);
                  float var29 = var26 + (var27 - var26) * var25;
                  if (var29 > var26 + 0.5F) {
                     imDrawList.addRectFilled(var26, var28, var29, var28 + 2.0F, DrawUtils.accent(var16), 1.0F);
                  }
               }
            }
         }
      }
   }

   private static void a(ImDrawList imDrawList, ImFont imFont, float f, float g, String string, float h) {
      String[] var6 = new String[]{" turned ON", " turned OFF"};

      for (String var10 : var6) {
         int var11 = string.indexOf(var10);
         if (var11 >= 0) {
            String var12 = string.substring(0, var11 + 8);
            String var13 = string.substring(var11 + 8);
            DrawUtils.text(imDrawList, imFont, 20.0F, f, g, DrawUtils.alpha(-526345, h), var12);
            float var14 = f + DrawUtils.textWidth(imFont, 20.0F, var12);
            int var15 = var10.endsWith("ON") ? -9641876 : -10724097;
            DrawUtils.text(imDrawList, imFont, 20.0F, var14, g, DrawUtils.alpha(var15, h), var13);
            return;
         }
      }

      DrawUtils.text(imDrawList, imFont, 20.0F, f, g, DrawUtils.alpha(-526345, h), string);
   }

   private static float a() {
      BellTexture var0 = BellTexture.getInstance();
      return var0.isReady() && var0.height() > 0 ? (float)var0.width() / var0.height() : 1.0F;
   }
}
