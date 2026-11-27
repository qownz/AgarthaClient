package net.agartha.client.k;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class c {
   private static final long MIN_FRAME_NS = 150000L;
   private long tick = Long.MIN_VALUE;
   private long leftNs;
   private int leftUnits;
   private long startNs;
   private long deadline;

   public boolean begin(long l, int i) {
      long var4 = a.tickCount();
      if (var4 != this.tick) {
         this.tick = var4;
         this.leftNs = l;
         this.leftUnits = i;
      }

      this.startNs = System.nanoTime();
      if (this.leftNs > 0L && this.leftUnits > 0) {
         long var6 = Math.max(150000L, a.frameShareOf(l));
         this.deadline = this.startNs + Math.min(this.leftNs, var6);
         return true;
      } else {
         this.deadline = this.startNs;
         return false;
      }
   }

   public long deadline() {
      return this.deadline;
   }

   public boolean hasTime() {
      return System.nanoTime() < this.deadline;
   }

   public int unitsLeft() {
      return this.leftUnits;
   }

   public void useUnits(int i) {
      this.leftUnits -= i;
   }

   public void end() {
      this.leftNs = this.leftNs - (System.nanoTime() - this.startNs);
   }
}
