package net.agartha.client.util.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.LongPredicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ChunkScanCache<T> {
   public static final int STALE_EMPTY_TICKS = 20;
   public static final int MAX_EMPTY_RESCAN_ATTEMPTS = 3;
   public static final int SLOW_RESCAN_TICKS = 200;
   public static final int TELEPORT_CHUNK_DELTA = 4;
   public static final int DISCOVERY_INTERVAL_TICKS = 20;
   private final Map<Long, List<T>> cache = new HashMap<>();
   private final Map<Long, Integer> emptySinceTick = new HashMap<>();
   private final Map<Long, Integer> emptyRetryCount = new HashMap<>();
   private final Map<Long, Integer> scannedAtTick = new HashMap<>();
   private final Set<Long> nonemptyChunks = new HashSet<>();
   private final Deque<Long> scanQueue = new ArrayDeque<>();
   private final Set<Long> queued = new HashSet<>();
   private int lastCenterX = Integer.MIN_VALUE;
   private int lastCenterZ = Integer.MIN_VALUE;
   private int discoveryTimer;

   public Map<Long, List<T>> cacheView() {
      return this.cache;
   }

   public void clear() {
      this.cache.clear();
      this.emptySinceTick.clear();
      this.emptyRetryCount.clear();
      this.scannedAtTick.clear();
      this.nonemptyChunks.clear();
      this.scanQueue.clear();
      this.queued.clear();
      this.lastCenterX = Integer.MIN_VALUE;
      this.lastCenterZ = Integer.MIN_VALUE;
      this.discoveryTimer = 0;
   }

   public void invalidateEmptyNear(int i, int j, int k) {
      ArrayList var4 = new ArrayList();

      for (Entry var6 : this.cache.entrySet()) {
         if (((List)var6.getValue()).isEmpty()) {
            long var7 = (Long)var6.getKey();
            int var9 = (int)var7;
            int var10 = (int)(var7 >>> 32);
            if (Math.abs(var9 - i) <= k && Math.abs(var10 - j) <= k) {
               var4.add(var7);
            }
         }
      }

      if (!var4.isEmpty()) {
         HashSet var11 = new HashSet(var4);

         for (long var13 : (Iterable<Long>) var4) {
            this.cache.remove(var13);
            this.emptySinceTick.remove(var13);
            this.emptyRetryCount.remove(var13);
            this.scannedAtTick.remove(var13);
            this.nonemptyChunks.remove(var13);
            this.queued.remove(var13);
         }

         if (!this.scanQueue.isEmpty()) {
            this.scanQueue.removeIf(var11::contains);
         }
      }
   }

   public void discover(int i, int j, int k, int l, LongPredicate longPredicate) {
      boolean var6 = i != this.lastCenterX || j != this.lastCenterZ;
      if (var6) {
         if (this.lastCenterX != Integer.MIN_VALUE) {
            int var7 = Math.abs(i - this.lastCenterX);
            int var8 = Math.abs(j - this.lastCenterZ);
            if (var7 >= 4 || var8 >= 4) {
               this.invalidateEmptyNear(i, j, k);
            }
         }

         this.lastCenterX = i;
         this.lastCenterZ = j;
      }

      boolean var14 = ++this.discoveryTimer >= 20;
      if (var6 || var14) {
         this.discoveryTimer = 0;

         for (int var15 = -k; var15 <= k; var15++) {
            for (int var9 = -k; var9 <= k; var9++) {
               int var10 = i + var15;
               int var11 = j + var9;
               long var12 = chunkKey(var10, var11);
               if (longPredicate.test(var12) && this.needsScan(var12, l) && this.queued.add(var12)) {
                  this.scanQueue.addLast(var12);
               }
            }
         }
      }
   }

   public void overwrite(long l, List<T> list) {
      this.emptySinceTick.remove(l);
      this.emptyRetryCount.remove(l);
      if (list != null && !list.isEmpty()) {
         this.cache.put(l, Collections.unmodifiableList(list));
         this.nonemptyChunks.add(l);
      } else {
         this.cache.put(l, List.of());
         this.nonemptyChunks.remove(l);
      }
   }

   public void requestRescan(long l) {
      this.emptySinceTick.remove(l);
      this.emptyRetryCount.remove(l);
      List var3 = this.cache.get(l);
      if (var3 != null && var3.isEmpty()) {
         this.cache.remove(l);
      }

      if (this.queued.add(l)) {
         this.scanQueue.addFirst(l);
      }
   }

   public void queueInView(int i, int j, int k, int l, LongPredicate longPredicate) {
      for (int var6 = -k; var6 <= k; var6++) {
         for (int var7 = -k; var7 <= k; var7++) {
            long var8 = chunkKey(i + var6, j + var7);
            if (longPredicate.test(var8) && this.needsScan(var8, l) && this.queued.add(var8)) {
               this.scanQueue.addLast(var8);
            }
         }
      }
   }

   public boolean needsScan(long l, int i) {
      if (this.queued.contains(l)) {
         return false;
      } else {
         List var4 = this.cache.get(l);
         if (var4 == null) {
            return true;
         } else {
            int var5 = this.scannedAtTick.getOrDefault(l, 0);
            if (!var4.isEmpty()) {
               return i - var5 >= slowRescanTicks(l);
            } else {
               Integer var6 = this.emptySinceTick.get(l);
               if (var6 == null) {
                  return true;
               } else {
                  int var7 = this.emptyRetryCount.getOrDefault(l, 0);
                  if (var7 >= 3) {
                     return i - var6 >= slowRescanTicks(l);
                  } else {
                     int var8 = 20 * (1 + var7);
                     return i - var6 >= var8;
                  }
               }
            }
         }
      }
   }

   static int slowRescanTicks(long l) {
      long var2 = l * -7046029254386353131L;
      int var4 = (int)Math.floorMod(var2 >>> 32, 200L);
      return 200 + var4;
   }

   public void store(long l, List<T> list, int i) {
      if (list != null && !list.isEmpty()) {
         this.cache.put(l, Collections.unmodifiableList(list));
         this.emptySinceTick.remove(l);
         this.emptyRetryCount.remove(l);
         this.scannedAtTick.put(l, i);
         this.nonemptyChunks.add(l);
      } else {
         List var5 = this.cache.get(l);
         boolean var6 = var5 != null && var5.isEmpty();
         this.cache.put(l, List.of());
         this.emptySinceTick.put(l, i);
         this.nonemptyChunks.remove(l);
         this.scannedAtTick.put(l, i);
         if (var6) {
            this.emptyRetryCount.merge(l, 1, Integer::sum);
         } else {
            this.emptyRetryCount.put(l, 0);
         }
      }
   }

   public Set<Long> nonemptyChunkKeys() {
      return this.nonemptyChunks;
   }

   public int nonemptyChunkCount() {
      return this.nonemptyChunks.size();
   }

   public void forEachNonemptyChunk(Consumer<Long> consumer) {
      for (long var3 : this.nonemptyChunks) {
         consumer.accept(var3);
      }
   }

   public List<T> get(long l) {
      return this.cache.get(l);
   }

   public void remove(long l) {
      this.cache.remove(l);
      this.emptySinceTick.remove(l);
      this.emptyRetryCount.remove(l);
      this.scannedAtTick.remove(l);
      this.nonemptyChunks.remove(l);
      this.queued.remove(l);
   }

   public Long poll() {
      Long var1;
      while ((var1 = this.scanQueue.pollFirst()) != null) {
         if (this.queued.remove(var1)) {
            return var1;
         }
      }

      return null;
   }

   public boolean isQueued(long l) {
      return this.queued.contains(l);
   }

   public static long chunkKey(int i, int j) {
      return i & 4294967295L | (long)j << 32;
   }

   public void prune(LongPredicate longPredicate) {
      HashSet var2 = new HashSet();
      this.cache.keySet().removeIf(long_ -> {
         if (!longPredicate.test(long_)) {
            return false;
         } else {
            this.emptySinceTick.remove(long_);
            this.emptyRetryCount.remove(long_);
            this.scannedAtTick.remove(long_);
            this.nonemptyChunks.remove(long_);
            this.queued.remove(long_);
            var2.add(long_);
            return true;
         }
      });
      if (!var2.isEmpty() && !this.scanQueue.isEmpty()) {
         this.scanQueue.removeIf(var2::contains);
      }
   }
}
