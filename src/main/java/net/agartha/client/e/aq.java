package net.agartha.client.e;

import imgui.ImDrawList;
import imgui.ImFont;
import net.agartha.client.module.impl.client.SpotifyHudModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class aq {
   private static final float PAD = 10.0F;
   private static final float ART = 56.0F;
   private static final float GAP = 12.0F;
   private static final float TEXT_W = 176.0F;
   private static final float RADIUS = 6.0F;
   private static final float CUT = 8.0F;
   private static final float MARQUEE_SPEED = 18.0F;
   private static final float MARQUEE_PAUSE = 1.6F;
   private static String artistIn;
   private static float artistSizeIn;
   private static float artistWidthIn;
   private static String artistOut = "";
   private static int elapsedIn = Integer.MIN_VALUE;
   private static String elapsedOut = "0:00";
   private static int totalIn = Integer.MIN_VALUE;
   private static String totalOut = "0:00";

   private aq() {
   }

   public static boolean shouldDraw(SpotifyHudModule spotifyHudModule, boolean bl) {
      if (bl) {
         return true;
      } else if (spotifyHudModule == null) {
         return false;
      } else {
         net.agartha.client.n.b.getInstance().ensureStarted();
         net.agartha.client.n.c var2 = net.agartha.client.n.b.getInstance().snapshot();
         return !var2.present() ? false : var2.playing() || spotifyHudModule.showWhenPaused();
      }
   }

   public static aq.a measureSize(SpotifyHudModule spotifyHudModule, boolean bl) {
      float var2 = spotifyHudModule == null ? 1.0F : Math.max(0.5F, spotifyHudModule.scale());
      return new aq.a(264.0F * var2, 76.0F * var2);
   }

   public static void render(ImDrawList imDrawList, SpotifyHudModule spotifyHudModule, t.c arg, boolean bl, float f, float g, float h) {
      if (imDrawList != null && spotifyHudModule != null && arg != null) {
         if (shouldDraw(spotifyHudModule, bl)) {
            float var7 = Math.max(0.5F, spotifyHudModule.scale());
            float var8 = c.clamp01(spotifyHudModule.opacity() * h);
            if (!(var8 <= 0.002F)) {
               float var9 = arg.x + f;
               float var10 = arg.y + g;
               float var11 = arg.width;
               float var12 = arg.height;
               c.card(imDrawList, var9, var10, var11, var12, 6.0F * var7, 8.0F * var7, var8, spotifyHudModule.shadow());
               c.accentRim(imDrawList, var9, var10, var11, 6.0F * var7, 8.0F * var7, 2.0F * var7, var8);
               net.agartha.client.n.c var13 = net.agartha.client.n.b.getInstance().snapshot();
               boolean var14 = !var13.present() || !var13.playing() && !spotifyHudModule.showWhenPaused();
               float var15 = 56.0F * var7;
               float var16 = var9 + 10.0F * var7;
               float var17 = var10 + (var12 - var15) * 0.5F;
               float var18 = 5.0F * var7;
               imDrawList.addRectFilled(var16, var17, var16 + var15, var17 + var15, c.alpha(-15461614, var8), var18);
               if (!var14) {
                  net.agartha.client.n.a_o var19 = net.agartha.client.n.a_o.getInstance();
                  var19.syncFrom(var13, spotifyHudModule.debug());
                  if (var19.isReady()) {
                     imDrawList.addImageRounded(var19.textureId(), var16, var17, var16 + var15, var17 + var15, 0.0F, 0.0F, 1.0F, 1.0F, c.alpha(-1, var8), var18);
                  } else {
                     a(imDrawList, var16, var17, var15, var8);
                  }

                  if (spotifyHudModule.debug()) {
                     net.agartha.client.n.b.getInstance().debugTick(var13, var19);
                  }
               } else {
                  a(imDrawList, var16, var17, var15, var8);
               }

               imDrawList.addRect(var16, var17, var16 + var15, var17 + var15, c.alpha(620756991, var8), var18, 0, 1.0F);
               float var47 = var16 + var15 + 12.0F * var7;
               float var20 = var9 + var11 - 10.0F * var7 - 2.0F * var7;
               float var21 = Math.max(1.0F, var20 - var47);
               ImFont var22 = net.agartha.client.f.a.getInstance().hudTextFont();
               ImFont var23 = net.agartha.client.f.a.getInstance().hudLabelFont();
               ImFont var24 = net.agartha.client.f.a.getInstance().hudValueFont();
               float var25 = 15.0F * var7;
               float var26 = 12.0F * var7;
               float var27 = 13.0F * var7;
               boolean var28 = !var14 && var13.playing();
               float var29 = 13.0F * var7;
               float var30 = var21 - var29 - 6.0F * var7;
               float var31 = var17 + 1.0F * var7;
               String var32 = var14 ? "Spotify" : var13.title();
               a(imDrawList, var22, var25, var47, var31, var30, var32, c.alpha(-526345, var8));
               a(imDrawList, var20 - var29, var31 + 2.0F * var7, var29, var25 - 3.0F * var7, var28, var8);
               float var33 = var31 + var25 + 3.0F * var7;
               String var34 = var14 ? "Nothing playing" : (spotifyHudModule.showArtist() ? var13.artist() : "");
               if (var34 != null && !var34.isBlank()) {
                  c.text(imDrawList, var23, var26, var47, var33, c.alpha(-6119783, var8), a(var23, var26, var34, var21));
               }

               double var35 = var14 ? 0.0 : var13.positionSeconds();
               double var37 = var14 ? 0.0 : var13.durationSeconds();
               float var39 = var37 > 0.001 ? (float)Math.max(0.0, Math.min(1.0, var35 / var37)) : 0.0F;
               float var40 = 3.0F * var7;
               float var41 = var17 + var15 - var40 - 1.0F * var7;
               imDrawList.addRectFilled(var47, var41, var20, var41 + var40, c.alpha(620756991, var8 * 1.3F), var40);
               float var42 = var47 + var21 * var39;
               if (var42 > var47 + 0.5F) {
                  int var43 = c.accent(var8);
                  imDrawList.addRectFilledMultiColor(var47, var41, var42, var41 + var40, c.accent(var8 * 0.55F), var43, var43, c.accent(var8 * 0.55F));
                  imDrawList.addCircleFilled(var42, var41 + var40 * 0.5F, 5.0F * var7, c.accent(var8 * 0.18F), 16);
                  imDrawList.addCircleFilled(var42, var41 + var40 * 0.5F, 2.6F * var7, c.alpha(-1, var8), 12);
               }

               String var48 = a(var35);
               String var44 = b(var37);
               float var45 = var41 - var27 - 3.0F * var7;
               c.text(imDrawList, var24, var27, var47, var45, c.alpha(-6119783, var8), var48);
               float var46 = c.textWidth(var24, var27, var44);
               c.text(imDrawList, var24, var27, var20 - var46, var45, c.alpha(-9738140, var8), var44);
            }
         }
      }
   }

   private static void a(ImDrawList imDrawList, ImFont imFont, float f, float g, float h, float i, String string, int j) {
      if (string != null && !string.isEmpty()) {
         float var8 = c.textWidth(imFont, f, string);
         if (var8 <= i) {
            c.text(imDrawList, imFont, f, g, h, j, string);
         } else {
            float var9 = 34.0F;
            float var10 = var8 + var9;
            double var11 = System.nanoTime() / 1.0E9;
            double var13 = var10 / 18.0F + 1.6F;
            double var15 = var11 % var13;
            float var17 = var15 < 1.6F ? 0.0F : (float)((var15 - 1.6F) * 18.0);
            imDrawList.pushClipRect(g, h - 2.0F, g + i, h + f + 4.0F, true);
            c.text(imDrawList, imFont, f, g - var17, h, j, string);
            c.text(imDrawList, imFont, f, g - var17 + var10, h, j, string);
            imDrawList.popClipRect();
            if (!c.glassActive()) {
               float var18 = Math.min(14.0F, i * 0.15F);
               int var19 = j >>> 24 & 0xFF;
               int var20 = c.withAlpha(-234091766, Math.round(var19 * 0.95F));
               int var21 = c.withAlpha(-234091766, 0);
               imDrawList.addRectFilledMultiColor(g + i - var18, h - 2.0F, g + i, h + f + 4.0F, var21, var20, var20, var21);
               if (var17 > 0.5F) {
                  imDrawList.addRectFilledMultiColor(g, h - 2.0F, g + var18, h + f + 4.0F, var20, var21, var21, var20);
               }
            }
         }
      }
   }

   private static void a(ImDrawList imDrawList, float f, float g, float h, float i, boolean bl, float j) {
      double var7 = System.nanoTime() / 1.0E9;
      float var9 = h / 4.2F;

      for (int var10 = 0; var10 < 3; var10++) {
         float var11 = bl ? 0.3F + 0.7F * (float)Math.abs(Math.sin(var7 * (4.1 + var10 * 1.7) + var10 * 1.3)) : 0.22F;
         float var12 = Math.max(2.0F, i * var11);
         float var13 = f + var10 * var9 * 1.6F;
         imDrawList.addRectFilled(var13, g + i - var12, var13 + var9, g + i, c.accent(j * (bl ? 1.0F : 0.45F)), 1.0F);
      }
   }

   private static void a(ImDrawList imDrawList, float f, float g, float h, float i) {
      float var5 = h * 0.42F;
      c.claw(imDrawList, f + h * 0.5F - var5 * 0.4F, g + (h - var5) * 0.5F, var5, c.accent(i * 0.6F));
   }

   private static String a(ImFont imFont, float f, String string, float g) {
      if (!string.equals(artistIn) || f != artistSizeIn || g != artistWidthIn) {
         artistIn = string;
         artistSizeIn = f;
         artistWidthIn = g;
         artistOut = c.ellipsize(imFont, f, string, g);
      }

      return artistOut;
   }

   private static String a(double d) {
      int var2 = c(d);
      if (var2 != elapsedIn) {
         elapsedIn = var2;
         elapsedOut = d(d);
      }

      return elapsedOut;
   }

   private static String b(double d) {
      int var2 = c(d);
      if (var2 != totalIn) {
         totalIn = var2;
         totalOut = d(d);
      }

      return totalOut;
   }

   private static int c(double d) {
      return !(d < 0.0) && !Double.isNaN(d) && !Double.isInfinite(d) ? (int)Math.floor(d + 1.0E-4) : -1;
   }

   private static String d(double d) {
      if (!(d < 0.0) && !Double.isNaN(d) && !Double.isInfinite(d)) {
         int var2 = (int)Math.floor(d + 1.0E-4);
         return var2 / 60 + ":" + (var2 % 60 < 10 ? "0" : "") + var2 % 60;
      } else {
         return "0:00";
      }
   }

   @Environment(EnvType.CLIENT)
   public record a(float width, float height) {
   }
}
