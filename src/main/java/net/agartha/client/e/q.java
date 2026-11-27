package net.agartha.client.e;

import imgui.ImDrawList;
import imgui.ImFont;
import java.time.LocalTime;
import net.agartha.client.module.impl.client.HudModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public final class q {
   public static final int BRAND = 0;
   public static final int FPS = 1;
   public static final int PING = 2;
   public static final int TPS = 3;
   public static final int CLOCK = 4;
   public static final int MEMORY = 5;
   public static final int SPEED = 6;
   public static final int POSITION = 7;
   public static final int NETHER = 8;
   public static final int FACING = 9;
   public static final int SERVER = 10;
   public static final int PLAYERS = 11;
   public static final int KINDS = 12;
   public static final int ROW_BREAK = 7;
   public static final String[] IDS = new String[]{
      "box_brand",
      "box_fps",
      "box_ping",
      "box_tps",
      "box_clock",
      "box_memory",
      "box_speed",
      "box_position",
      "box_nether",
      "box_facing",
      "box_server",
      "box_players"
   };
   private static final String[] NAMES = new String[]{
      "Agartha+", "FPS", "Ping", "TPS", "Time", "Memory", "Speed", "Position", "Nether", "Facing", "Server", "Players"
   };
   private static final float BOX_H = 30.0F;
   private static final float PAD_X = 10.0F;
   private static final float ICON = 14.0F;
   private static final float ICON_GAP = 7.0F;
   private static final float VALUE_SIZE = 19.0F;
   private static final float TEXT_SIZE = 15.0F;
   private static final float LABEL_SIZE = 13.0F;
   private static final float LABEL_GAP = 5.0F;
   private static final float AXIS_GAP = 7.0F;
   private static final float RADIUS = 5.0F;
   private static final float CUT = 6.0F;
   private static final String COMMA = ",";
   private static final String[] DIRECTIONS = new String[]{"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
   private static final String[] AXES = new String[]{"+Z", "-X +Z", "-X", "-X -Z", "-Z", "+X -Z", "+X", "+X +Z"};
   private static final String[] DIGIT = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
   private static final String BRAND_NAME = "Agartha";
   private static final String BRAND_PLUS = "+";
   private static final String[] values = new String[12];
   private static final String[] labels = new String[]{null, "FPS", "MS", "TPS", null, "MB", "B/S", null, null, null, null, "ONLINE"};
   private static final q.a fpsText = new q.a();
   private static final q.a pingText = new q.a();
   private static final q.a tpsText = new q.a();
   private static final q.a memoryText = new q.a();
   private static final q.a speedText = new q.a();
   private static final q.a playersText = new q.a();
   private static final q.a[] coordText = new q.a[]{new q.a(), new q.a(), new q.a()};
   private static final q.a[] netherText = new q.a[]{new q.a(), new q.a()};
   private static int clockMinute = -1;
   private static String clockValue = "";
   private static String serverValue = "";
   private static ServerInfo serverSource;
   private static boolean serverSingle;
   private static float smoothSpeed;
   private static int octant;
   private static float yaw;
   private static int pingValue;
   private static float tpsValue = 20.0F;
   private static long memorySampledAt;
   private static boolean otherIsOverworld;
   private static boolean inEnd;
   private static ImFont digitFont;
   private static float digitSize = -1.0F;
   private static float digitWidth;

   private q() {
   }

   public static String name(int i) {
      return NAMES[i];
   }

   public static boolean enabled(HudModule hudModule, int i) {
      return switch (i) {
         case 0 -> hudModule.brandBox();
         case 1 -> hudModule.fps();
         case 2 -> hudModule.ping();
         case 3 -> hudModule.tps();
         case 4 -> hudModule.clock();
         case 5 -> hudModule.memory();
         case 6 -> hudModule.speed();
         case 7 -> hudModule.positionBox();
         case 8 -> false;
         case 9 -> hudModule.direction();
         case 10 -> hudModule.server();
         case 11 -> hudModule.players();
         default -> false;
      };
   }

   public static void prepare(int i, int j, float f) {
      MinecraftClient var3 = MinecraftClient.getInstance();
      ClientPlayerEntity var4 = var3.player;
      inEnd = var3.world != null && var3.world.getRegistryKey() == World.END;
      pingValue = j;
      tpsValue = net.agartha.client.util.v.tps();
      values[1] = fpsText.of(i);
      values[2] = pingText.of(j);
      values[3] = tpsText.tenths(Math.round(tpsValue * 10.0F));
      long var5 = System.nanoTime();
      if (var5 - memorySampledAt > 500000000L || values[5] == null) {
         memorySampledAt = var5;
         Runtime var7 = Runtime.getRuntime();
         values[5] = memoryText.of((int)(var7.totalMemory() - var7.freeMemory() >>> 20));
      }

      int var15 = 0;
      int var8 = 0;
      int var9 = 0;
      if (var4 != null) {
         double var10 = var4.getX() - var4.lastX;
         double var12 = var4.getZ() - var4.lastZ;
         float var14 = (float)Math.sqrt(var10 * var10 + var12 * var12) * 20.0F;
         smoothSpeed = c.approach(smoothSpeed, var14, f, 10.0F);
         yaw = var4.getYaw();
         octant = Math.floorMod(Math.round(yaw / 45.0F), 8);
         var15 = (int)Math.floor(var4.getX());
         var8 = (int)Math.floor(var4.getY());
         var9 = (int)Math.floor(var4.getZ());
      }

      values[6] = speedText.tenths(Math.round(smoothSpeed * 10.0F));
      values[4] = a();
      values[10] = a(var3);
      values[9] = DIRECTIONS[octant];
      labels[9] = AXES[octant];
      ClientPlayNetworkHandler var16 = var3.getNetworkHandler();
      values[11] = playersText.of(var16 == null ? 0 : var16.getPlayerList().size());
      boolean var11 = var4 != null && var3.world.getRegistryKey() == World.NETHER;
      otherIsOverworld = var11;
      coordText[0].of(var15);
      coordText[1].of(var8);
      coordText[2].of(var9);
      netherText[0].of(var11 ? var15 * 8 : Math.floorDiv(var15, 8));
      netherText[1].of(var11 ? var9 * 8 : Math.floorDiv(var9, 8));
   }

   public static float height(float f) {
      return 30.0F * f;
   }

   public static float width(int i, float f) {
      ImFont var2 = net.agartha.client.f.a.getInstance().hudValueFont();
      ImFont var3 = net.agartha.client.f.a.getInstance().hudTextFont();
      float var5 = a(var2, 19.0F * f);
      float var6 = 10.0F * f * 2.0F + 14.0F * f + 7.0F * f;

      return switch (i) {
         case 0 -> 10.0F * f * 2.0F + c.textWidth(var3, 15.0F * f, "Agartha") + c.textWidth(var3, 15.0F * f, "+") + f;
         default -> var6 + var5 * Math.max(3, values[i].length()) + a(var2, f, labels[i]);
         case 3, 6 -> var6 + var5 * Math.max(4, values[i].length()) + a(var2, f, labels[i]);
         case 4 -> var6 + var5 * 5.0F;
         case 7 -> 10.0F * f * 2.0F + a(coordText, 3, var5, f);
         case 8 -> var6 + a(netherText, 2, var5, f);
         case 9 -> var6 + var5 * 2.0F + a(var2, f, "+X +Z");
         case 10 -> var6 + c.textWidth(var3, 15.0F * f, values[10]);
      };
   }

   public static void render(ImDrawList imDrawList, int i, t.c arg, float f, float g, float h) {
      float var6 = c.clamp01(h);
      if (!(var6 <= 0.002F)) {
         float var7 = v.scaleOf(arg);
         float var8 = arg.x + f;
         float var9 = arg.y + g;
         float var10 = arg.width;
         float var11 = arg.height;
         c.card(imDrawList, var8, var9, var10, var11, 5.0F * var7, 6.0F * var7, var6, true);
         float var12 = var9 + var11 * 0.5F;
         float var13 = var8 + 10.0F * var7;
         float var14 = var13;
         if (i != 0 && i != 7) {
            a(imDrawList, i, var13, var12, 14.0F * var7, var7, var6);
            var14 = var13 + (14.0F * var7 + 7.0F * var7);
         }

         ImFont var15 = net.agartha.client.f.a.getInstance().hudValueFont();
         ImFont var16 = net.agartha.client.f.a.getInstance().hudTextFont();
         float var18 = 19.0F * var7;
         float var19 = 13.0F * var7;
         float var20 = 15.0F * var7;
         int var21 = c.alpha(-526345, var6);
         switch (i) {
            case 0:
               c.text(imDrawList, var16, var20, var14, c.midTop(var16, var20, var12), var21, "Agartha");
               float var24 = var14 + c.textWidth(var16, var20, "Agartha") + var7;
               c.text(imDrawList, var16, var20, var24, c.midTop(var16, var20, var12), c.accent(var6), "+");
               break;
            case 1:
            case 2:
            case 3:
            case 5:
            case 6:
            case 9:
            default:
               c.text(imDrawList, var15, var18, var14, c.midTop(var15, var18, var12), var21, values[i]);
               String var22 = labels[i];
               if (var22 != null) {
                  float var23 = var14 + c.textWidth(var15, var18, values[i]) + 5.0F * var7;
                  c.tracked(
                     imDrawList,
                     var15,
                     var19,
                     var23,
                     c.baselineTop(var15, var19, var15, var18, var12),
                     i == 9 ? c.alpha(-6119783, var6) : c.accent(var6 * 0.9F),
                     var22,
                     0.4F * var7
                  );
               }
               break;
            case 4:
               c.text(imDrawList, var15, var18, var14, c.midTop(var15, var18, var12), var21, values[i]);
               break;
            case 7:
               a(imDrawList, coordText, 3, var14, var12, var7, var6);
               break;
            case 8:
               a(imDrawList, netherText, 2, var14, var12, var7, var6);
               break;
            case 10:
               c.text(imDrawList, var16, var20, var14, c.midTop(var16, var20, var12), var21, values[i]);
         }
      }
   }

   private static void a(ImDrawList imDrawList, q.a[] args, int i, float f, float g, float h, float j) {
      ImFont var7 = net.agartha.client.f.a.getInstance().hudValueFont();
      float var8 = 19.0F * h;
      float var9 = a(var7, var8);
      String var10 = i == 3 ? "XYZ" : "XZ";
      float var11 = c.midTop(var7, var8, g);
      c.text(imDrawList, var7, var8, f, var11, c.accent(j), var10);
      float var12 = f + c.textWidth(var7, var8, var10) + 7.0F * h;
      int var13 = c.alpha(-526345, j);
      int var14 = c.alpha(-6119783, j);
      float var15 = c.textWidth(var7, var8, ",") + 4.0F * h;

      for (int var16 = 0; var16 < i; var16++) {
         String var17 = args[var16].text;
         c.text(imDrawList, var7, var8, var12, var11, var13, var17);
         var12 += var9 * var17.length();
         if (var16 < i - 1) {
            c.text(imDrawList, var7, var8, var12, var11, var14, ",");
            var12 += var15;
         }
      }
   }

   private static float a(q.a[] args, int i, float f, float g) {
      ImFont var4 = net.agartha.client.f.a.getInstance().hudValueFont();
      String var5 = i == 3 ? "XYZ" : "XZ";
      float var6 = c.textWidth(var4, 19.0F * g, var5) + 7.0F * g;
      float var7 = c.textWidth(var4, 19.0F * g, ",") + 4.0F * g;

      for (int var8 = 0; var8 < i; var8++) {
         var6 += f * args[var8].text.length();
      }

      return var6 + var7 * (i - 1);
   }

   private static void a(ImDrawList imDrawList, int i, float f, float g, float h, float j, float k) {
      int var7 = c.f(c.accent(k));
      int var8 = c.f(c.accent(k * 0.35F));
      int var9 = c.f(c.alpha(-9738140, k));
      float var10 = h * 0.5F;
      float var11 = f + var10;
      float var12 = Math.max(1.0F, 1.6F * j);
      switch (i) {
         case 1:
            float var35 = h * 0.34F;
            float var40 = f + var12 * 0.5F;
            float var44 = f + h - var12 * 0.5F;
            float var47 = g - var10 + var12 * 0.5F;
            float var50 = g + var10 - var12 * 0.5F;
            imDrawList.addLine(var40, var47, var40 + var35, var47, var7, var12);
            imDrawList.addLine(var40, var47, var40, var47 + var35, var7, var12);
            imDrawList.addLine(var44, var47, var44 - var35, var47, var7, var12);
            imDrawList.addLine(var44, var47, var44, var47 + var35, var7, var12);
            imDrawList.addLine(var40, var50, var40 + var35, var50, var7, var12);
            imDrawList.addLine(var40, var50, var40, var50 - var35, var7, var12);
            imDrawList.addLine(var44, var50, var44 - var35, var50, var7, var12);
            imDrawList.addLine(var44, var50, var44, var50 - var35, var7, var12);
            break;
         case 2:
            int var34 = pingValue <= 0 ? 0 : (pingValue <= 80 ? 4 : (pingValue <= 150 ? 3 : (pingValue <= 250 ? 2 : 1)));
            float var39 = h * 0.17F;
            float var43 = h * 0.27F;

            for (int var46 = 0; var46 < 4; var46++) {
               float var49 = f + var46 * var43;
               float var51 = g + var10 - h * (0.3F + 0.233F * var46);
               imDrawList.addRectFilled(var49, var51, var49 + var39, g + var10, var46 < var34 ? var7 : var9, var39 * 0.4F);
            }
            break;
         case 3:
            int var33 = tpsValue >= 15.0F ? var7 : c.f(c.alpha(-11908353, k));
            imDrawList.pathClear();
            imDrawList.pathLineTo(f, g);
            imDrawList.pathLineTo(f + h * 0.28F, g);
            imDrawList.pathLineTo(f + h * 0.42F, g - var10 * 0.9F);
            imDrawList.pathLineTo(f + h * 0.58F, g + var10 * 0.9F);
            imDrawList.pathLineTo(f + h * 0.72F, g);
            imDrawList.pathLineTo(f + h, g);
            imDrawList.pathStroke(var33, 0, var12);
            break;
         case 4:
            imDrawList.addCircle(var11, g, var10, var7, 20, var12);
            imDrawList.addLine(var11, g, var11, g - var10 * 0.6F, var7, var12);
            imDrawList.addLine(var11, g, var11 + var10 * 0.45F, g + var10 * 0.15F, var7, var12);
            break;
         case 5:
            float var32 = var10 * 0.62F;
            imDrawList.addRect(var11 - var32, g - var32, var11 + var32, g + var32, var7, 1.5F * j, 0, var12);
            imDrawList.addRectFilled(var11 - var32 * 0.4F, g - var32 * 0.4F, var11 + var32 * 0.4F, g + var32 * 0.4F, var8, j);

            for (int var38 = -1; var38 <= 1; var38++) {
               float var42 = g + var38 * var32 * 0.6F;
               imDrawList.addLine(f, var42, var11 - var32, var42, var7, var12 * 0.8F);
               imDrawList.addLine(var11 + var32, var42, f + h, var42, var7, var12 * 0.8F);
            }
            break;
         case 6:
            for (int var31 = 0; var31 < 2; var31++) {
               float var37 = f + var31 * h * 0.42F;
               imDrawList.addLine(var37, g - var10 * 0.8F, var37 + h * 0.4F, g, var7, var12);
               imDrawList.addLine(var37 + h * 0.4F, g, var37, g + var10 * 0.8F, var7, var12);
            }
            break;
         case 7:
            float var30 = var10 * 0.62F;
            float var36 = g - var10 * 0.3F;
            imDrawList.addCircleFilled(var11, var36, var30, var7, 20);
            imDrawList.addTriangleFilled(var11 - var30 * 0.86F, var36 + var30 * 0.5F, var11 + var30 * 0.86F, var36 + var30 * 0.5F, var11, g + var10, var7);
            imDrawList.addCircleFilled(var11, var36, var30 * 0.4F, c.f(c.alpha(-16250873, k)), 12);
            break;
         case 8:
            imDrawList.addQuadFilled(var11, g - var10, f + h, g, var11, g + var10, f, g, otherIsOverworld ? var8 : var7);
            if (otherIsOverworld) {
               imDrawList.addQuad(var11, g - var10, f + h, g, var11, g + var10, f, g, var7, var12);
            }

            imDrawList.addQuadFilled(
               var11, g - var10 * 0.38F, var11 + var10 * 0.38F, g, var11, g + var10 * 0.38F, var11 - var10 * 0.38F, g, c.f(c.alpha(-16250873, k * 0.8F))
            );
            break;
         case 9:
            double var29 = Math.toRadians(yaw + 180.0);
            float var41 = (float)Math.sin(var29);
            float var45 = (float)(-Math.cos(var29));
            float var48 = -var45;
            float var52 = var11 + var41 * var10;
            float var20 = g + var45 * var10;
            float var21 = var11 - var41 * var10 * 0.6F;
            float var22 = g - var45 * var10 * 0.6F;
            float var23 = var21 + var48 * var10 * 0.55F;
            float var24 = var22 + var41 * var10 * 0.55F;
            float var25 = var21 - var48 * var10 * 0.55F;
            float var26 = var22 - var41 * var10 * 0.55F;
            float var27 = var11 - var41 * var10 * 0.25F;
            float var28 = g - var45 * var10 * 0.25F;
            imDrawList.addTriangleFilled(var52, var20, var23, var24, var27, var28, var7);
            imDrawList.addTriangleFilled(var52, var20, var27, var28, var25, var26, var7);
            break;
         case 10:
            float var13 = h * 0.28F;
            float var14 = h * 0.08F;
            float var15 = g - (var13 * 3.0F + var14 * 2.0F) * 0.5F;
            int var16 = c.f(c.alpha(-16250873, k));

            for (int var17 = 0; var17 < 3; var17++) {
               float var18 = var15 + var17 * (var13 + var14);
               float var19 = var18 + var13 * 0.5F;
               imDrawList.addRectFilled(f, var18, f + h, var18 + var13, var7, var13 * 0.3F);
               imDrawList.addCircleFilled(f + h * 0.2F, var19, var13 * 0.2F, var16, 8);
               imDrawList.addLine(f + h * 0.42F, var19, f + h * 0.8F, var19, var16, Math.max(1.0F, var13 * 0.22F));
            }
            break;
         case 11:
            imDrawList.addCircleFilled(f + h * 0.68F, g - var10 * 0.38F, var10 * 0.3F, var8, 12);
            imDrawList.addCircleFilled(f + h * 0.68F, g + var10 * 0.62F, var10 * 0.48F, var8, 16);
            imDrawList.addCircleFilled(f + h * 0.36F, g - var10 * 0.3F, var10 * 0.36F, var7, 12);
            imDrawList.pathClear();
            imDrawList.pathArcTo(f + h * 0.36F, g + var10, var10 * 0.58F, (float) Math.PI, (float) (Math.PI * 2), 12);
            imDrawList.pathFillConvex(var7);
            break;
         default:
            imDrawList.addCircleFilled(var11, g, var10 * 0.4F, var7, 12);
      }
   }

   private static String a() {
      LocalTime var0 = LocalTime.now();
      int var1 = var0.getHour() * 60 + var0.getMinute();
      if (var1 != clockMinute) {
         clockMinute = var1;
         int var2 = var0.getHour();
         int var3 = var0.getMinute();
         clockValue = (var2 < 10 ? "0" : "") + var2 + (var3 < 10 ? ":0" : ":") + var3;
      }

      return clockValue;
   }

   private static String a(MinecraftClient minecraftClient) {
      ServerInfo var1 = minecraftClient.getCurrentServerEntry();
      boolean var2 = minecraftClient.isIntegratedServerRunning();
      if (var1 != serverSource || var2 != serverSingle || serverValue.isEmpty()) {
         serverSource = var1;
         serverSingle = var2;
         serverValue = var2 ? "Singleplayer" : (var1 != null ? var1.address : "Offline");
      }

      return serverValue;
   }

   private static float a(ImFont imFont, float f, String string) {
      return string == null ? 0.0F : 5.0F * f + c.trackedWidth(imFont, 13.0F * f, string, 0.4F * f);
   }

   private static float a(ImFont imFont, float f) {
      if (f == digitSize && imFont == digitFont) {
         return digitWidth;
      } else {
         float var2 = 0.0F;

         for (int var3 = 0; var3 < 10; var3++) {
            var2 = Math.max(var2, c.textWidth(imFont, f, DIGIT[var3]));
         }

         digitFont = imFont;
         digitSize = f;
         digitWidth = var2;
         return var2;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class a {
      private int value = Integer.MIN_VALUE;
      private boolean tenths;
      String text = "0";

      String of(int i) {
         if (i != this.value || this.tenths) {
            this.value = i;
            this.tenths = false;
            this.text = String.valueOf(i);
         }

         return this.text;
      }

      String tenths(int i) {
         if (i != this.value || !this.tenths) {
            this.value = i;
            this.tenths = true;
            this.text = i / 10 + "." + i % 10;
         }

         return this.text;
      }
   }
}
