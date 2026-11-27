package net.agartha.client.e;

import imgui.ImDrawList;
import imgui.ImFont;
import net.agartha.client.module.impl.client.RegionMapModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public final class ao {
   private static final float BASE_CELL = 22.0F;
   private static final float PAD = 10.0F;
   private static final float COMPACT_PAD = 6.0F;
   private static final float INFO_GAP = 8.0F;
   private static final float INFO_TEXT = 14.0F;
   private static final float INFO_LINE = 17.0F;
   private static final float MIN_TEXT = 7.0F;
   private static final float CELL_TEXT_SHARE = 0.5F;
   private static final float MIN_CELL_TEXT = 9.0F;
   private static final String END_WARNING = "You are in the End";

   private ao() {
   }

   public static double[] overworldXZ(ClientPlayerEntity clientPlayerEntity, World world) {
      double var2 = clientPlayerEntity.getX();
      double var4 = clientPlayerEntity.getZ();
      return world != null && world.getRegistryKey() == World.NETHER ? new double[]{var2 * 8.0, var4 * 8.0} : new double[]{var2, var4};
   }

   public static boolean isInTheEnd(World world) {
      return world != null && world.getRegistryKey() == World.END;
   }

   private static float a(RegionMapModule regionMapModule) {
      byte var1 = 9;
      return var1 * (regionMapModule.cellSize() + regionMapModule.cellGap()) - regionMapModule.cellGap();
   }

   public static ao.b measureSize(RegionMapModule regionMapModule) {
      ao.a var1 = ao.a.of(regionMapModule);
      float var2 = a(regionMapModule);
      return regionMapModule.compact()
         ? new ao.b(var2 + var1.pad() * 2.0F, var2 + var1.pad() * 2.0F)
         : new ao.b(var2 + var1.pad() * 2.0F, var2 + var1.pad() * 2.0F + var1.infoGap() + a(regionMapModule, var1));
   }

   public static void render(ImDrawList imDrawList, RegionMapModule regionMapModule, t.c arg) {
      render(imDrawList, regionMapModule, arg, 0.0F, 0.0F, 1.0F);
   }

   public static void render(ImDrawList imDrawList, RegionMapModule regionMapModule, t.c arg, float f, float g, float h) {
      MinecraftClient var6 = MinecraftClient.getInstance();
      ClientPlayerEntity var7 = var6.player;
      if (var7 != null && imDrawList != null && regionMapModule != null && arg != null) {
         ao.a var8 = ao.a.of(regionMapModule);
         float var9 = arg.x + f;
         float var10 = arg.y + g;
         float var11 = regionMapModule.opacity() * h;
         float var12 = 9.0F * var8.k();
         c.card(imDrawList, var9, var10, arg.width, arg.height, 6.0F * var8.k(), var12, var11, false);
         c.accentRim(imDrawList, var9, var10, arg.width, 6.0F * var8.k(), var12, Math.max(1.0F, 2.0F * var8.k()), var11);
         float var13 = var9 + var8.pad();
         float var14 = var10 + var8.pad();
         float var15 = regionMapModule.cellSize();
         float var16 = regionMapModule.cellGap();
         byte var17 = 9;
         float var18 = a(regionMapModule);
         float var19 = Math.min(4.0F * var8.k(), var15 * 0.18F);
         boolean var20 = isInTheEnd(var6.world);
         double[] var21 = overworldXZ(var7, var6.world);
         double var22 = var20 ? var7.getX() : var21[0];
         double var24 = var20 ? var7.getZ() : var21[1];

         for (int var26 = 0; var26 < var17; var26++) {
            for (int var27 = 0; var27 < var17; var27++) {
               n.a var28 = n.regionInfo(var26 * var17 + var27);
               if (var28 != null) {
                  float var29 = var13 + var27 * (var15 + var16);
                  float var30 = var14 + var26 * (var15 + var16);
                  int var31 = a(n.regionColor(var28.regionType), var11);
                  imDrawList.addRectFilled(var29, var30, var29 + var15, var30 + var15, var31, var19);
                  if (regionMapModule.showRegionIds()) {
                     a(imDrawList, var28.regionId, var29, var30, var15, regionMapModule.textScale(), var11);
                  }
               }
            }
         }

         if (!var20 && regionMapModule.showPlayer()) {
            a(imDrawList, regionMapModule, var7, var21[0], var21[1], var13, var14, var15, var16, var8.k(), var11);
         }

         if (!regionMapModule.compact()) {
            a(imDrawList, regionMapModule, var8, var6.world, var22, var24, var7.getY(), var20, var13, var14 + var18 + var8.infoGap(), var18, var11);
         }
      }
   }

   private static void a(ImDrawList imDrawList, int i, float f, float g, float h, float j, float k) {
      float var7 = Math.min(h * 0.8F, h * 0.5F * j);
      if (!(var7 < 9.0F)) {
         ImFont var8 = net.agartha.client.f.a.getInstance().hudLabelFont();
         String var9 = String.valueOf(i);
         float var10 = c.textWidth(var8, var7, var9);
         c.text(imDrawList, var8, var7, f + (h - var10) * 0.5F, c.midTop(var8, var7, g + h * 0.5F), a(-1, k), var9);
      }
   }

   private static void a(
      ImDrawList imDrawList,
      RegionMapModule regionMapModule,
      ClientPlayerEntity clientPlayerEntity,
      double d,
      double e,
      float f,
      float g,
      float h,
      float i,
      float j,
      float k
   ) {
      int[] var13 = n.worldToGrid(d, e);
      if (var13[0] >= 0 && var13[0] < 9 && var13[1] >= 0 && var13[1] < 9) {
         double[] var14 = n.worldToCellPosition(d, e);
         float var15 = f + var13[0] * (h + i) + (float)(var14[0] * h);
         float var16 = g + var13[1] * (h + i) + (float)(var14[1] * h);
         double var17 = Math.toRadians(-clientPlayerEntity.getYaw() - 90.0);
         float var19 = 6.5F * j * regionMapModule.playerScale();
         int var20 = regionMapModule.customPlayerColor() ? a(a(regionMapModule.playerIndicatorColor().argb()), k) : a(at.accent(), k);
         int var21 = a(-1184792, k);
         float var22 = var15 + (float)(Math.cos(var17) * var19);
         float var23 = var16 - (float)(Math.sin(var17) * var19);
         float var24 = var15 + (float)(Math.cos(var17 + Math.toRadians(140.0)) * var19);
         float var25 = var16 - (float)(Math.sin(var17 + Math.toRadians(140.0)) * var19);
         float var26 = var15 + (float)(Math.cos(var17 - Math.toRadians(140.0)) * var19);
         float var27 = var16 - (float)(Math.sin(var17 - Math.toRadians(140.0)) * var19);
         float var28 = var15 - (float)(Math.cos(var17) * var19 * 0.35F);
         float var29 = var16 + (float)(Math.sin(var17) * var19 * 0.35F);
         imDrawList.addCircleFilled(var15, var16, var19 * 1.05F, a(1140850688, k), 20);
         imDrawList.addTriangleFilled(var22, var23, var24, var25, var28, var29, var20);
         imDrawList.addTriangleFilled(var22, var23, var28, var29, var26, var27, var20);
         imDrawList.pathClear();
         imDrawList.pathLineTo(var22, var23);
         imDrawList.pathLineTo(var26, var27);
         imDrawList.pathLineTo(var28, var29);
         imDrawList.pathLineTo(var24, var25);
         imDrawList.pathStroke(var21, 1, Math.max(1.0F, 1.2F * j * regionMapModule.playerScale()));
      }
   }

   private static void a(
      ImDrawList imDrawList,
      RegionMapModule regionMapModule,
      ao.a arg,
      World world,
      double d,
      double e,
      double f,
      boolean bl,
      float g,
      float h,
      float i,
      float j
   ) {
      ImFont var15 = net.agartha.client.f.a.getInstance().hudTextFont();
      int var16 = a(-526345, j);
      float var17 = h;
      if (regionMapModule.showCoords()) {
         a(imDrawList, var15, arg, g, h, i, var16, "Pos: " + (int)d + ", " + (int)Math.floor(f) + ", " + (int)e);
         var17 = h + arg.infoLine();
      }

      if (regionMapModule.showNether()) {
         a(imDrawList, var15, arg, g, var17, i, var16, "Nether: " + (int)Math.floor(d / 8.0) + ", " + (int)Math.floor(e / 8.0));
         var17 += arg.infoLine();
      }

      if (regionMapModule.showRegionInfo()) {
         a(imDrawList, var15, arg, g, var17, i, var16, bl ? "Region: You are in the End" : "Region: " + n.regionAt(d, e) + " (" + n.regionTypeName(d, e) + ")");
         var17 += arg.infoLine();
      }
   }

   private static void a(ImDrawList imDrawList, ImFont imFont, ao.a arg, float f, float g, float h, int i, String string) {
      c.text(
         imDrawList, imFont, arg.infoText(), f, c.midTop(imFont, arg.infoText(), g + arg.infoLine() * 0.5F), i, c.ellipsize(imFont, arg.infoText(), string, h)
      );
   }

   private static float a(RegionMapModule regionMapModule, ao.a arg) {
      int var2 = (regionMapModule.showCoords() ? 1 : 0) + (regionMapModule.showNether() ? 1 : 0) + (regionMapModule.showRegionInfo() ? 1 : 0);
      return var2 * arg.infoLine();
   }

   private static int a(int i, float f) {
      int var2 = i >>> 24 & 0xFF;
      int var3 = Math.max(0, Math.min(255, Math.round(var2 * f)));
      return i & 16777215 | var3 << 24;
   }

   private static int a(int i) {
      int var1 = i >>> 24 & 0xFF;
      int var2 = i >>> 16 & 0xFF;
      int var3 = i >>> 8 & 0xFF;
      int var4 = i & 0xFF;
      return var1 << 24 | var4 << 16 | var3 << 8 | var2;
   }

   @Environment(EnvType.CLIENT)
   private record a(float k, float pad, float infoGap, float infoText, float infoLine) {
      static ao.a of(RegionMapModule regionMapModule) {
         float var1 = Math.max(0.1F, regionMapModule.cellSize() / 22.0F);
         float var2 = Math.max(7.0F, 14.0F * var1);
         return new ao.a(var1, (regionMapModule.compact() ? 6.0F : 10.0F) * var1, 8.0F * var1, var2, Math.max(var2 + 2.0F, 17.0F * var1));
      }
   }

   @Environment(EnvType.CLIENT)
   public record b(float width, float height) {
   }
}
