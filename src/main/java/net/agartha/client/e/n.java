package net.agartha.client.e;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class n {
   public static final int MAP_SIZE = 9;
   private static final double REGION_SIZE = 50000.0;
   private static final double MAP_OFFSET = 225000.0;
   private static final String[] REGION_TYPE_NAMES = new String[]{"EU Central", "EU West", "NA East", "NA West", "Asia", "Oceania"};
   private static final int[] REGION_TYPE_COLORS = new int[]{-10236257, -10246656, -1397425, -4559313, -12401931, -16545540};
   private static final Map<Integer, n.a> REGION_MAP = a();

   private n() {
   }

   public static n.a regionInfo(int i) {
      return REGION_MAP.get(i);
   }

   public static int regionColor(int i) {
      return i >= 0 && i < REGION_TYPE_COLORS.length ? REGION_TYPE_COLORS[i] : -5592406;
   }

   public static String[] regionTypeNames() {
      return REGION_TYPE_NAMES;
   }

   public static int[] regionTypeColors() {
      return REGION_TYPE_COLORS;
   }

   public static int regionAt(double d, double e) {
      int[] var4 = worldToGrid(d, e);
      if (var4[0] >= 0 && var4[0] < 9 && var4[1] >= 0 && var4[1] < 9) {
         n.a var5 = REGION_MAP.get(var4[1] * 9 + var4[0]);
         return var5 != null ? var5.regionId : -1;
      } else {
         return -1;
      }
   }

   public static String regionTypeName(double d, double e) {
      int[] var4 = worldToGrid(d, e);
      if (var4[0] >= 0 && var4[0] < 9 && var4[1] >= 0 && var4[1] < 9) {
         n.a var5 = REGION_MAP.get(var4[1] * 9 + var4[0]);
         return var5 == null ? "Unknown" : REGION_TYPE_NAMES[var5.regionType];
      } else {
         return "Unknown";
      }
   }

   public static int[] worldToGrid(double d, double e) {
      return new int[]{(int)((d + 225000.0) / 50000.0), (int)((e + 225000.0) / 50000.0)};
   }

   public static double[] worldToCellPosition(double d, double e) {
      return new double[]{a(d + 225000.0, 50000.0) / 50000.0, a(e + 225000.0, 50000.0) / 50000.0};
   }

   private static double a(double d, double e) {
      double var4 = d % e;
      return var4 < 0.0 ? var4 + e : var4;
   }

   private static Map<Integer, n.a> a() {
      int[][] var0 = new int[][]{
         {82, 5},
         {100, 3},
         {101, 3},
         {102, 3},
         {103, 2},
         {104, 2},
         {105, 2},
         {106, 2},
         {91, 2},
         {83, 5},
         {44, 3},
         {75, 3},
         {42, 3},
         {41, 2},
         {40, 2},
         {39, 2},
         {38, 2},
         {92, 2},
         {84, 5},
         {45, 3},
         {14, 3},
         {13, 3},
         {12, 2},
         {11, 2},
         {10, 2},
         {37, 2},
         {93, 2},
         {85, 5},
         {46, 5},
         {74, 5},
         {3, 3},
         {2, 2},
         {1, 2},
         {25, 2},
         {36, 2},
         {94, 2},
         {86, 4},
         {47, 4},
         {72, 4},
         {71, 4},
         {5, 2},
         {4, 2},
         {24, 2},
         {35, 2},
         {95, 2},
         {87, 4},
         {51, 1},
         {17, 1},
         {9, 0},
         {8, 0},
         {7, 0},
         {23, 0},
         {34, 0},
         {96, 2},
         {88, 4},
         {54, 1},
         {18, 1},
         {61, 0},
         {62, 0},
         {21, 0},
         {22, 0},
         {33, 0},
         {97, 0},
         {89, 0},
         {26, 1},
         {27, 0},
         {28, 0},
         {29, 0},
         {30, 0},
         {59, 0},
         {32, 0},
         {98, 0},
         {90, 0},
         {107, 1},
         {108, 1},
         {109, 1},
         {110, 1},
         {111, 1},
         {112, 1},
         {113, 1},
         {99, 0}
      };
      HashMap var1 = new HashMap();

      for (int var2 = 0; var2 < var0.length; var2++) {
         var1.put(var2, new n.a(var0[var2][0], var0[var2][1]));
      }

      return var1;
   }

   @Environment(EnvType.CLIENT)
   public static final class a {
      public final int regionId;
      public final int regionType;

      a(int i, int j) {
         this.regionId = i;
         this.regionType = j;
      }
   }
}
