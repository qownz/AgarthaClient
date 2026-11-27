package net.agartha.client.render.post;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class FrameExposure {
   public static final int CAPACITY = 12;
   private final double[] frameEnds = new double[12];
   private int next;
   private int filled;
   private final int[] slots = new int[12];
   private final float[] weights = new float[12];
   private int count;
   private float totalWeight;

   public void clear() {
      Arrays.fill(this.frameEnds, 0.0);
      this.next = 0;
      this.filled = 0;
      this.count = 0;
      this.totalWeight = 0.0F;
   }

   public int push(double d) {
      int var3 = this.next;
      this.frameEnds[var3] = d;
      this.next = (this.next + 1) % 12;
      if (this.filled < 12) {
         this.filled++;
      }

      return var3;
   }

   public int weigh(double d, int i, float f) {
      Arrays.fill(this.weights, 0.0F);
      this.count = 0;
      this.totalWeight = 0.0F;
      if (this.filled != 0 && i > 0) {
         double var5 = d - 1.0 / i;
         double var7 = f > 0.0F ? 1.0 / f : 1.0 / i;
         int var9 = Math.floorMod(this.next - this.filled, 12);
         double var10 = Double.NaN;

         for (int var12 = 0; var12 < this.filled; var12++) {
            int var13 = (var9 + var12) % 12;
            double var14 = this.frameEnds[var13];
            double var16 = !Double.isNaN(var10) && !(var10 >= var14) ? var10 : var14 - var7;
            var10 = var14;
            double var18 = Math.min(var14, d) - Math.max(var16, var5);
            if (var18 > 1.0E-7) {
               this.slots[this.count] = var13;
               this.weights[this.count] = (float)var18;
               this.totalWeight += (float)var18;
               this.count++;
            }
         }

         return this.count;
      } else {
         return 0;
      }
   }

   public int count() {
      return this.count;
   }

   public int slot(int i) {
      return this.slots[i];
   }

   public float weight(int i) {
      return this.weights[i];
   }

   public float inverseTotalWeight() {
      return this.totalWeight > 0.0F ? 1.0F / this.totalWeight : 1.0F;
   }
}
