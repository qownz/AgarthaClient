package net.agartha.client.util.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.agartha.client.util.world.LightStore;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.LightType;

@Environment(EnvType.CLIENT)
public final class GeodeAnalyzer {
   public static final int GEODE_LINK_DISTANCE = 5;
   public static final int SCAN_Y_MIN = -64;
   public static final int SCAN_Y_MAX = 128;
   public static final int HARVEST_YOUNG_BUDS = 12;
   public static final int HARVEST_MIN_MEDIUM = 3;
   public static final int HARVEST_MAX_CLUSTERS = 2;
   public static final int SMALL = 1;
   public static final int MEDIUM = 2;
   public static final int LARGE = 3;
   public static final int CLUSTER = 4;
   private static final double MAX_FIT_DRIFT = 24.0;
   private static final double MIN_FIT_RADIUS = 3.0;
   private static final double MAX_FIT_RADIUS = 16.0;

   private GeodeAnalyzer() {
   }

   public static int stageForLight(int i) {
      return switch (i) {
         case 1 -> 1;
         case 2 -> 2;
         default -> 0;
         case 4 -> 3;
         case 5 -> 4;
      };
   }

   public static int stageForBlock(Block block) {
      if (block == Blocks.SMALL_AMETHYST_BUD) {
         return 1;
      } else if (block == Blocks.MEDIUM_AMETHYST_BUD) {
         return 2;
      } else if (block == Blocks.LARGE_AMETHYST_BUD) {
         return 3;
      } else {
         return block == Blocks.AMETHYST_CLUSTER ? 4 : 0;
      }
   }

   public static GeodeAnalyzer.c classify(int i, int j) {
      int var2 = stageForLight(i);
      int var3 = stageForLight(j);
      if (var3 == var2 + 1) {
         return GeodeAnalyzer.c.GROWTH;
      } else {
         return var2 > 0 && j == 0 ? GeodeAnalyzer.c.REMOVED : GeodeAnalyzer.c.NONE;
      }
   }

   public static List<GeodeAnalyzer.b> buds(ClientWorld clientWorld, List<LightStore.b> list) {
      ArrayList var2 = new ArrayList(list.size());
      Mutable var3 = new Mutable();

      for (LightStore.b var5 : list) {
         BlockPos var6 = var5.pos();
         if (!a(clientWorld, var6, var5.light(), var3)) {
            BlockState var7 = clientWorld.getBlockState(var6);
            int var8 = stageForBlock(var7.getBlock());
            if (var8 == 0) {
               if (var7.getLuminance() > 0) {
                  continue;
               }

               var8 = stageForLight(var5.light());
            }

            var2.add(new GeodeAnalyzer.b(var6.getX(), var6.getY(), var6.getZ(), var8));
         }
      }

      return var2;
   }

   private static boolean a(ClientWorld clientWorld, BlockPos blockPos, int i, Mutable mutable) {
      int var4 = blockPos.getX();
      int var5 = blockPos.getY();
      int var6 = blockPos.getZ();
      return (var4 & 15) == 0 && a(clientWorld, mutable.set(var4 - 1, var5, var6), i)
         || (var4 & 15) == 15 && a(clientWorld, mutable.set(var4 + 1, var5, var6), i)
         || (var6 & 15) == 0 && a(clientWorld, mutable.set(var4, var5, var6 - 1), i)
         || (var6 & 15) == 15 && a(clientWorld, mutable.set(var4, var5, var6 + 1), i)
         || (var5 & 15) == 0 && a(clientWorld, mutable.set(var4, var5 - 1, var6), i)
         || (var5 & 15) == 15 && a(clientWorld, mutable.set(var4, var5 + 1, var6), i);
   }

   private static boolean a(ClientWorld clientWorld, BlockPos blockPos, int i) {
      return clientWorld.getLightLevel(LightType.BLOCK, blockPos) > i;
   }

   static double[] fitCenter(List<GeodeAnalyzer.b> list) {
      double var1 = list.size();
      double var3 = 0.0;
      double var5 = 0.0;
      double var7 = 0.0;

      for (GeodeAnalyzer.b var10 : list) {
         var3 += var10.x() + 0.5;
         var5 += var10.y() + 0.5;
         var7 += var10.z() + 0.5;
      }

      var3 /= var1;
      var5 /= var1;
      var7 /= var1;
      double[] var28 = new double[]{var3, var5, var7};
      if (list.size() < 8) {
         return var28;
      } else {
         double[][] var29 = new double[4][4];
         double[] var11 = new double[4];

         for (GeodeAnalyzer.b var13 : list) {
            double var14 = var13.x() + 0.5 - var3;
            double var16 = var13.y() + 0.5 - var5;
            double var18 = var13.z() + 0.5 - var7;
            double[] var20 = new double[]{2.0 * var14, 2.0 * var16, 2.0 * var18, 1.0};
            double var21 = var14 * var14 + var16 * var16 + var18 * var18;

            for (int var23 = 0; var23 < 4; var23++) {
               for (int var24 = 0; var24 < 4; var24++) {
                  var29[var23][var24] = var29[var23][var24] + var20[var23] * var20[var24];
               }

               var11[var23] += var20[var23] * var21;
            }
         }

         double[] var30 = a(var29, var11);
         if (var30 == null) {
            return var28;
         } else {
            double[] var31 = new double[]{var3 + var30[0], var5 + var30[1], var7 + var30[2]};
            double var32 = var31[0] - var3;
            double var33 = var31[1] - var5;
            double var34 = var31[2] - var7;
            if (Double.isFinite(var31[0])
               && Double.isFinite(var31[1])
               && Double.isFinite(var31[2])
               && !(Math.sqrt(var32 * var32 + var33 * var33 + var34 * var34) > 24.0)) {
               double var35 = Math.sqrt(Math.max(0.0, var30[3] + var30[0] * var30[0] + var30[1] * var30[1] + var30[2] * var30[2]));
               return Double.isFinite(var35) && !(var35 < 3.0) && !(var35 > 16.0) ? var31 : var28;
            } else {
               return var28;
            }
         }
      }
   }

   private static double[] a(double[][] ds, double[] es) {
      double[][] var2 = new double[4][5];

      for (int var3 = 0; var3 < 4; var3++) {
         System.arraycopy(ds[var3], 0, var2[var3], 0, 4);
         var2[var3][4] = es[var3];
      }

      for (int var10 = 0; var10 < 4; var10++) {
         int var4 = var10;

         for (int var5 = var10 + 1; var5 < 4; var5++) {
            if (Math.abs(var2[var5][var10]) > Math.abs(var2[var4][var10])) {
               var4 = var5;
            }
         }

         if (Math.abs(var2[var4][var10]) < 1.0E-6) {
            return null;
         }

         double[] var11 = var2[var10];
         var2[var10] = var2[var4];
         var2[var4] = var11;

         for (int var6 = 0; var6 < 4; var6++) {
            if (var6 != var10) {
               double var7 = var2[var6][var10] / var2[var10][var10];

               for (int var9 = var10; var9 < 5; var9++) {
                  var2[var6][var9] = var2[var6][var9] - var7 * var2[var10][var9];
               }
            }
         }
      }

      return new double[]{var2[0][4] / var2[0][0], var2[1][4] / var2[1][1], var2[2][4] / var2[2][2], var2[3][4] / var2[3][3]};
   }

   public static List<GeodeAnalyzer.e> geodes(List<GeodeAnalyzer.b> list) {
      ArrayList var1 = new ArrayList();

      for (List var3 : group(list)) {
         int[] var4 = new int[5];
         HashSet var5 = new HashSet();

         for (GeodeAnalyzer.b var7 : (Iterable<GeodeAnalyzer.b>) var3) {
            var4[var7.stage()]++;
            var5.add(var7.chunkKey());
         }

         if (var4[3] + var4[4] != 0 || var4[2] >= 3) {
            double[] var8 = fitCenter(var3);
            var1.add(new GeodeAnalyzer.e(List.copyOf(var3), Set.copyOf(var5), var4[1], var4[2], var4[3], var4[4], var8[0], var8[1], var8[2]));
         }
      }

      return var1;
   }

   public static int minClusters(int i) {
      return 7 + a(i);
   }

   public static int minYoungBuds(int i) {
      return Math.max(8, 12 + 2 * (a(i) - 4));
   }

   public static int minEvents(int i) {
      return Math.max(3, 2 + (a(i) + 1) / 2);
   }

   private static int a(int i) {
      return Math.max(1, Math.min(10, i));
   }

   public static boolean isGrown(GeodeAnalyzer.e arg, int i) {
      return arg.clusters() >= i;
   }

   public static boolean isHarvested(GeodeAnalyzer.e arg) {
      return isHarvested(arg, 12);
   }

   public static boolean isHarvested(GeodeAnalyzer.e arg, int i) {
      return arg.medium() >= 3 && arg.small() + arg.medium() >= i && arg.clusters() <= 2;
   }

   public static GeodeAnalyzer.f tierOf(GeodeAnalyzer.e arg, int i, boolean bl) {
      return tierOf(arg, i, 12, bl);
   }

   public static GeodeAnalyzer.f tierOf(GeodeAnalyzer.e arg, int i, int j, boolean bl) {
      if (isGrown(arg, i)) {
         return GeodeAnalyzer.f.GROWN;
      } else {
         return bl && isHarvested(arg, j) ? GeodeAnalyzer.f.HARVESTED : null;
      }
   }

   public static Map<Long, GeodeAnalyzer.d> flags(List<GeodeAnalyzer.e> list, Map<Long, GeodeAnalyzer.a> map, int i, boolean bl, int j) {
      return flags(list, map, i, 12, bl, j);
   }

   public static Map<Long, GeodeAnalyzer.d> flags(List<GeodeAnalyzer.e> list, Map<Long, GeodeAnalyzer.a> map, int i, int j, boolean bl, int k) {
      LinkedHashMap<Long, GeodeAnalyzer.d> var6 = new LinkedHashMap<>();

      for (GeodeAnalyzer.e var8 : list) {
         GeodeAnalyzer.f var9 = tierOf(var8, i, j, bl);
         if (var9 != null) {
            HashMap<Long, Integer> var10 = new HashMap<>();

            for (GeodeAnalyzer.b var12 : var8.cells()) {
               var10.merge(var12.chunkKey(), 1, Integer::sum);
            }

            int var21 = var8.small() + var8.medium();

            for (long var13 : (Iterable<Long>) var8.chunks()) {
               double var15 = var10.getOrDefault(var13, 0).intValue();
               a(var6, new GeodeAnalyzer.d(var13, var9, var8.clusters(), var21, 0, var15, var8.centerX() * var15, var8.centerZ() * var15));
            }
         }
      }

      for (Entry var18 : map.entrySet()) {
         GeodeAnalyzer.a var19 = (GeodeAnalyzer.a)var18.getValue();
         if (var19.events() >= k) {
            double var20 = var19.events() * 8.0;
            a(
               var6,
               new GeodeAnalyzer.d(
                  (Long)var18.getKey(), GeodeAnalyzer.f.ACTIVE, 0, 0, var19.events(), var20, var19.sumX() / var19.events() * var20, var19.sumZ() / var19.events() * var20
               )
            );
         }
      }

      return var6;
   }

   private static void a(Map<Long, GeodeAnalyzer.d> map, GeodeAnalyzer.d arg) {
      map.merge(
         arg.chunkKey(),
         arg,
         (argx, arg2) -> new GeodeAnalyzer.d(
            argx.chunkKey(),
            a(argx.tier(), arg2.tier()),
            Math.max(argx.clusters(), arg2.clusters()),
            Math.max(argx.youngBuds(), arg2.youngBuds()),
            argx.events() + arg2.events(),
            argx.weight() + arg2.weight(),
            argx.sumX() + arg2.sumX(),
            argx.sumZ() + arg2.sumZ()
         )
      );
   }

   private static GeodeAnalyzer.f a(GeodeAnalyzer.f arg, GeodeAnalyzer.f arg2) {
      return arg.compareTo(arg2) >= 0 ? arg : arg2;
   }

   public static List<GeodeAnalyzer.g> zones(Map<Long, GeodeAnalyzer.d> map, int i) {
      int var2 = Math.max(1, i);
      ArrayList<Long> var3 = new ArrayList<>(map.keySet());
      var3.sort(Comparator.naturalOrder());
      HashSet<Long> var4 = new HashSet<>();
      ArrayList<GeodeAnalyzer.g> var5 = new ArrayList<>();

      for (long var7 : (Iterable<Long>) var3) {
         if (var4.add(var7)) {
            ArrayList var9 = new ArrayList();
            ArrayDeque var10 = new ArrayDeque<>(List.of(var7));

            while (!var10.isEmpty()) {
               long var11 = (Long)var10.poll();
               var9.add((GeodeAnalyzer.d)map.get(var11));
               int var13 = chunkX(var11);
               int var14 = chunkZ(var11);

               for (int var15 = -var2; var15 <= var2; var15++) {
                  for (int var16 = -var2; var16 <= var2; var16++) {
                     long var17 = chunkKey(var13 + var15, var14 + var16);
                     if (map.containsKey(var17) && var4.add(var17)) {
                        var10.add(var17);
                     }
                  }
               }
            }

            var5.add(a(var9));
         }
      }

      var5.sort(Comparator.comparing(GeodeAnalyzer.g::tier).reversed().thenComparing(Comparator.comparingInt(GeodeAnalyzer.g::clusters).reversed()));
      return var5;
   }

   private static GeodeAnalyzer.g a(List<GeodeAnalyzer.d> list) {
      HashSet var1 = new HashSet();
      long var2 = Long.MAX_VALUE;
      GeodeAnalyzer.f var4 = GeodeAnalyzer.f.HARVESTED;
      int var5 = 0;
      int var6 = 0;
      int var7 = 0;
      double var8 = 0.0;
      double var10 = 0.0;
      double var12 = 0.0;
      double var14 = 0.0;
      double var16 = 0.0;

      for (GeodeAnalyzer.d var19 : list) {
         var1.add(var19.chunkKey());
         var2 = Math.min(var2, var19.chunkKey());
         var4 = a(var4, var19.tier());
         var5 = Math.max(var5, var19.clusters());
         var6 = Math.max(var6, var19.youngBuds());
         var7 += var19.events();
         var8 += var19.weight();
         var10 += var19.sumX();
         var12 += var19.sumZ();
         var14 += chunkX(var19.chunkKey()) * 16 + 8;
         var16 += chunkZ(var19.chunkKey()) * 16 + 8;
      }

      double var22 = var8 > 0.0 ? var10 / var8 : var14 / list.size();
      double var20 = var8 > 0.0 ? var12 / var8 : var16 / list.size();
      return new GeodeAnalyzer.g(Set.copyOf(var1), var2, var4, var5, var6, var7, var22, var20);
   }

   static List<List<GeodeAnalyzer.b>> group(List<GeodeAnalyzer.b> list) {
      int var1 = list.size();
      if (var1 == 0) {
         return List.of();
      } else {
         int[] var2 = new int[var1];
         int var3 = 0;

         while (var3 < var1) {
            var2[var3] = var3++;
         }

         byte var16 = 6;
         HashMap<Long, List<Integer>> var4 = new HashMap<>();

         for (int var5 = 0; var5 < var1; var5++) {
            GeodeAnalyzer.b var6 = (GeodeAnalyzer.b)list.get(var5);
            var4.computeIfAbsent(a(Math.floorDiv(var6.x(), var16), Math.floorDiv(var6.y(), var16), Math.floorDiv(var6.z(), var16)), long_ -> new ArrayList<>())
               .add(var5);
         }

         for (int var17 = 0; var17 < var1; var17++) {
            GeodeAnalyzer.b var19 = (GeodeAnalyzer.b)list.get(var17);
            int var7 = Math.floorDiv(var19.x(), var16);
            int var8 = Math.floorDiv(var19.y(), var16);
            int var9 = Math.floorDiv(var19.z(), var16);

            for (int var10 = -1; var10 <= 1; var10++) {
               for (int var11 = -1; var11 <= 1; var11++) {
                  for (int var12 = -1; var12 <= 1; var12++) {
                     List<Integer> var13 = (List<Integer>)var4.get(a(var7 + var10, var8 + var11, var9 + var12));
                     if (var13 != null) {
                        for (int var15 : (Iterable<Integer>) var13) {
                           if (var15 > var17 && a(var19, (GeodeAnalyzer.b)list.get(var15)) <= 5) {
                              var2[a(var2, var17)] = a(var2, var15);
                           }
                        }
                     }
                  }
               }
            }
         }

         LinkedHashMap<Integer, List<GeodeAnalyzer.b>> var18 = new LinkedHashMap<>();

         for (int var20 = 0; var20 < var1; var20++) {
            var18.computeIfAbsent(a(var2, var20), integer -> new ArrayList<>()).add((GeodeAnalyzer.b)list.get(var20));
         }

         return new ArrayList<>(var18.values());
      }
   }

   public static long chunkKey(int i, int j) {
      return i & 4294967295L | (j & 4294967295L) << 32;
   }

   public static int chunkX(long l) {
      return (int)l;
   }

   public static int chunkZ(long l) {
      return (int)(l >>> 32);
   }

   public static int chunkDistance(long l, long m) {
      return Math.max(Math.abs(chunkX(l) - chunkX(m)), Math.abs(chunkZ(l) - chunkZ(m)));
   }

   private static int a(GeodeAnalyzer.b arg, GeodeAnalyzer.b arg2) {
      return Math.max(Math.max(Math.abs(arg.x() - arg2.x()), Math.abs(arg.y() - arg2.y())), Math.abs(arg.z() - arg2.z()));
   }

   private static long a(int i, int j, int k) {
      return i & 2097151L | (j & 2097151L) << 21 | (k & 2097151L) << 42;
   }

   private static int a(int[] is, int i) {
      while (is[i] != i) {
         is[i] = is[is[i]];
         i = is[i];
      }

      return i;
   }

   @Environment(EnvType.CLIENT)
   public record a(int events, double sumX, double sumZ) {
   }

   @Environment(EnvType.CLIENT)
   public record b(int x, int y, int z, int stage) {
      long chunkKey() {
         return GeodeAnalyzer.chunkKey(this.x >> 4, this.z >> 4);
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum c {
      GROWTH,
      REMOVED,
      NONE;
   }

   @Environment(EnvType.CLIENT)
   public record d(long chunkKey, GeodeAnalyzer.f tier, int clusters, int youngBuds, int events, double weight, double sumX, double sumZ) {
   }

   @Environment(EnvType.CLIENT)
   public record e(List<GeodeAnalyzer.b> cells, Set<Long> chunks, int small, int medium, int large, int clusters, double centerX, double centerY, double centerZ) {
      public double maturity() {
         int var1 = this.clusters + this.large;
         return var1 == 0 ? 0.0 : (double)this.clusters / var1;
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum f {
      HARVESTED,
      GROWN,
      ACTIVE;
   }

   @Environment(EnvType.CLIENT)
   public record g(Set<Long> members, long id, GeodeAnalyzer.f tier, int clusters, int youngBuds, int events, double centroidX, double centroidZ) {
   }
}
