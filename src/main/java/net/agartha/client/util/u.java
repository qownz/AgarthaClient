package net.agartha.client.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.light.ChunkLightingView;

@Environment(EnvType.CLIENT)
public final class u {
   public static final int SMALL_LIGHT = 1;
   public static final int MEDIUM_LIGHT = 2;
   public static final int LARGE_LIGHT = 4;
   public static final int CLUSTER_LIGHT = 5;
   private final Map<Long, short[]> sections = new ConcurrentHashMap<>();
   private int minStoredSy = Integer.MAX_VALUE;
   private int maxStoredSy = Integer.MIN_VALUE;
   private final ArrayDeque<byte[]> bufferPool = new ArrayDeque<>();
   private static final int MAX_POOLED_BUFFERS = 512;

   public void clear() {
      this.sections.clear();
      this.minStoredSy = Integer.MAX_VALUE;
      this.maxStoredSy = Integer.MIN_VALUE;
   }

   private byte[] a() {
      byte[] var1 = this.bufferPool.pollFirst();
      return var1 != null ? var1 : new byte[2048];
   }

   public void releaseBuffer(byte[] bs) {
      if (bs != null && bs.length == 2048 && this.bufferPool.size() < 512) {
         this.bufferPool.addFirst(bs);
      }
   }

   public int sectionCount() {
      return this.sections.size();
   }

   public boolean ingest(int i, int j, LightData lightData, int k, u.a arg) {
      BitSet var6 = lightData.getInitedBlock();
      List var7 = lightData.getBlockNibbles();
      boolean var8 = false;
      int var9 = 0;

      for (int var10 = var6.nextSetBit(0); var10 >= 0; var10 = var6.nextSetBit(var10 + 1)) {
         byte[] var11 = var9 < var7.size() ? (byte[])var7.get(var9) : null;
         var9++;
         if (var11 != null && var11.length == 2048) {
            var8 |= this.a(i, k + var10, j, index(var11), arg);
         }
      }

      BitSet var12 = lightData.getUninitedBlock();

      for (int var13 = var12.nextSetBit(0); var13 >= 0; var13 = var12.nextSetBit(var13 + 1)) {
         var8 |= this.a(i, k + var13, j, null, arg);
      }

      return var8;
   }

   public void seed(ClientWorld clientWorld, int i, int j, int k, int l) {
      ChunkLightingView var6 = clientWorld.getLightingProvider().get(LightType.BLOCK);
      int var7 = ChunkSectionPos.getSectionCoord(k);
      int var8 = ChunkSectionPos.getSectionCoord(l);

      for (int var9 = var7; var9 <= var8; var9++) {
         ChunkNibbleArray var10 = var6.getLightSection(ChunkSectionPos.from(i, var9, j));
         this.a(i, var9, j, var10 != null && !var10.isUninitialized() ? index(var10.asByteArray()) : null, null);
      }
   }

   public void forget(int i, int j) {
      if (this.minStoredSy <= this.maxStoredSy) {
         for (int var3 = this.minStoredSy; var3 <= this.maxStoredSy; var3++) {
            this.sections.remove(ChunkSectionPos.asLong(i, var3, j));
         }
      }
   }

   public List<u.b> emitters(int i, int j, int k, int l) {
      ArrayList var5 = new ArrayList();

      for (int var6 = ChunkSectionPos.getSectionCoord(k); var6 <= ChunkSectionPos.getSectionCoord(l); var6++) {
         short[] var7 = this.sections.get(ChunkSectionPos.asLong(i, var6, j));
         if (var7 != null) {
            for (short var11 : var7) {
               int var12 = var11 & 4095;
               int var13 = (var6 << 4) + (var12 >> 8);
               if (var13 >= k && var13 <= l) {
                  var5.add(new u.b(new BlockPos((i << 4) + (var12 & 15), var13, (j << 4) + (var12 >> 4 & 15)), var11 >> 12 & 15));
               }
            }
         }
      }

      return var5;
   }

   public static boolean isBudLight(int i) {
      return i == 1 || i == 2 || i == 4 || i == 5;
   }

   static short[] index(byte[] bs) {
      short[] var1 = null;
      int var2 = 0;

      for (int var3 = 0; var3 < bs.length; var3++) {
         int var4 = bs[var3] & 255;
         int var5 = var4 & 15;
         int var6 = var4 >>> 4;
         if (isBudLight(var5)) {
            int var7 = var3 << 1;
            if (!a(bs, var7, var5)) {
               var1 = a(var1, var2++, var5, var7);
            }
         }

         if (isBudLight(var6)) {
            int var8 = var3 << 1 | 1;
            if (!a(bs, var8, var6)) {
               var1 = a(var1, var2++, var6, var8);
            }
         }
      }

      return var1 == null ? null : Arrays.copyOf(var1, var2);
   }

   private static short[] a(short[] ss, int i, int j, int k) {
      if (ss == null) {
         ss = new short[16];
      } else if (i == ss.length) {
         ss = Arrays.copyOf(ss, i * 2);
      }

      ss[i] = (short)(j << 12 | k);
      return ss;
   }

   public void collectSections(int i, int j, LightData lightData, int k, int l, int m, u.c arg) {
      BitSet var8 = lightData.getInitedBlock();
      List var9 = lightData.getBlockNibbles();
      int var10 = 0;

      for (int var11 = var8.nextSetBit(0); var11 >= 0; var11 = var8.nextSetBit(var11 + 1)) {
         byte[] var12 = var10 < var9.size() ? (byte[])var9.get(var10) : null;
         var10++;
         int var13 = k + var11;
         if (var13 >= l && var13 <= m && var12 != null && var12.length == 2048) {
            byte[] var14 = this.a();
            System.arraycopy(var12, 0, var14, 0, var12.length);
            arg.section(i, var13, j, var14);
         }
      }

      BitSet var15 = lightData.getUninitedBlock();

      for (int var16 = var15.nextSetBit(0); var16 >= 0; var16 = var15.nextSetBit(var16 + 1)) {
         int var17 = k + var16;
         if (var17 >= l && var17 <= m) {
            arg.section(i, var17, j, null);
         }
      }
   }

   public boolean applySection(int i, int j, int k, byte[] bs, u.a arg) {
      return bs != null && bs.length != 2048 ? false : this.a(i, j, k, bs == null ? null : index(bs), arg);
   }

   static void diff(short[] ss, short[] ts, int i, int j, int k, u.a arg) {
      short[] var6 = ss == null ? new short[0] : ss;
      short[] var7 = ts == null ? new short[0] : ts;
      int var8 = 0;
      int var9 = 0;

      while (var8 < var6.length || var9 < var7.length) {
         int var10 = var8 < var6.length ? var6[var8] & 4095 : Integer.MAX_VALUE;
         int var11 = var9 < var7.length ? var7[var9] & 4095 : Integer.MAX_VALUE;
         int var12 = Math.min(var10, var11);
         int var13 = var10 == var12 ? var6[var8++] >> 12 & 15 : 0;
         int var14 = var11 == var12 ? var7[var9++] >> 12 & 15 : 0;
         if (var13 != var14) {
            arg.changed(i + (var12 & 15), j + (var12 >> 8), k + (var12 >> 4 & 15), var13, var14);
         }
      }
   }

   private boolean a(int i, int j, int k, short[] ss, u.a arg) {
      long var6 = ChunkSectionPos.asLong(i, j, k);
      if (ss != null) {
         if (j < this.minStoredSy) {
            this.minStoredSy = j;
         }

         if (j > this.maxStoredSy) {
            this.maxStoredSy = j;
         }
      }

      short[] var8 = ss == null ? this.sections.remove(var6) : this.sections.put(var6, ss);
      if (Arrays.equals(var8, ss)) {
         return false;
      } else {
         if (arg != null && var8 != null) {
            diff(var8, ss, i << 4, j << 4, k << 4, arg);
         }

         return true;
      }
   }

   private static int a(byte[] bs, int i) {
      int var2 = bs[i >> 1] & 255;
      return (i & 1) == 0 ? var2 & 15 : var2 >> 4;
   }

   private static boolean a(byte[] bs, int i, int j) {
      int var3 = i & 15;
      int var4 = i >> 4 & 15;
      int var5 = i >> 8;
      return var3 > 0 && a(bs, i - 1) > j
         || var3 < 15 && a(bs, i + 1) > j
         || var4 > 0 && a(bs, i - 16) > j
         || var4 < 15 && a(bs, i + 16) > j
         || var5 > 0 && a(bs, i - 256) > j
         || var5 < 15 && a(bs, i + 256) > j;
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   public interface a {
      void changed(int i, int j, int k, int l, int m);
   }

   @Environment(EnvType.CLIENT)
   public record b(BlockPos pos, int light) {
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   public interface c {
      void section(int i, int j, int k, byte[] bs);
   }
}
