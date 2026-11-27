package net.agartha.client.p;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public final class a {
   private static final int MAX_SCROLL_TIERS = 10;
   private static final double ZOOM_IN_TIME = 0.2;
   private static final double ZOOM_OUT_TIME = 0.15;
   private static final double SCROLL_SMOOTHNESS = 0.7;
   private static final double TICK_DELTA = 0.05;
   private double prevInitialInterpolation;
   private double initialInterpolation;
   private boolean zoomingLastTick;
   private double prevScrollInterpolation;
   private double scrollInterpolation;
   private int scrollSteps;
   private int lastScrollTier;
   private boolean resetting;
   private double resetMultiplier = 1.0;
   private double previousDivisor = 1.0;

   public void tick(boolean bl, boolean bl2) {
      this.tick(bl, bl2, 0.05);
   }

   public void tick(boolean bl, boolean bl2, double d) {
      this.a(bl, bl2, d);
      this.a(bl2, d);
   }

   public void addScrollStep(int i) {
      this.scrollSteps = MathHelper.clamp(this.scrollSteps + i, 0, 10);
   }

   public void resetScroll() {
      if (!this.resetting && this.scrollInterpolation > 0.0) {
         this.resetting = true;
         this.scrollInterpolation = 0.0;
         this.prevScrollInterpolation = 0.0;
      }
   }

   public void clearScrollSteps() {
      this.scrollSteps = 0;
      this.lastScrollTier = 0;
   }

   public boolean isAnimating() {
      return this.initialInterpolation > 0.001 || this.scrollInterpolation > 0.001;
   }

   public void forceReset() {
      this.initialInterpolation = 0.0;
      this.prevInitialInterpolation = 0.0;
      this.scrollInterpolation = 0.0;
      this.prevScrollInterpolation = 0.0;
      this.scrollSteps = 0;
      this.lastScrollTier = 0;
      this.resetting = false;
      this.zoomingLastTick = false;
      this.previousDivisor = 1.0;
   }

   public double getPreviousDivisor() {
      return this.previousDivisor;
   }

   public float getZoomDivisor(float f, int i, int j, boolean bl) {
      double var5 = this.a(f, i, j, bl);
      this.previousDivisor = var5;
      return (float)var5;
   }

   private double a(float f, int i, int j, boolean bl) {
      double var5 = this.a(f, i, bl);
      double var7 = 1.0 / var5;
      double var9 = this.resetting ? 0.0 : this.a(f, bl);
      double var11 = j / 100.0;
      double var13 = var9 * 10.0;
      double var15 = var7 * Math.pow(var11, var13);
      double var17 = MathHelper.clamp(var15, 0.5, 500.0);
      if (this.initialInterpolation <= 0.001 && this.scrollInterpolation <= 0.001) {
         this.resetting = false;
      }

      if (!this.resetting) {
         this.resetMultiplier = 1.0 / var17;
      }

      return var17;
   }

   private double a(float f, boolean bl) {
      return bl ? MathHelper.lerp(f, this.prevScrollInterpolation, this.scrollInterpolation) : this.scrollInterpolation;
   }

   private double a(float f, int i, boolean bl) {
      double var4 = bl ? MathHelper.lerp(f, this.prevInitialInterpolation, this.initialInterpolation) : this.initialInterpolation;
      double var6 = this.resetting ? this.resetMultiplier : 1.0 / Math.max(i, 1);
      return MathHelper.lerp(var4, 1.0, var6);
   }

   private void a(boolean bl, boolean bl2, double d) {
      if (bl && !this.zoomingLastTick) {
         this.resetting = false;
      }

      double var5 = bl ? 1.0 : 0.0;
      this.prevInitialInterpolation = this.initialInterpolation;
      if (!bl2) {
         this.initialInterpolation = var5;
         this.prevInitialInterpolation = var5;
      } else if (var5 > this.initialInterpolation) {
         this.initialInterpolation = Math.min(this.initialInterpolation + d / 0.2, var5);
      } else if (var5 < this.initialInterpolation) {
         this.initialInterpolation = Math.max(this.initialInterpolation - d / 0.15, var5);
      }

      this.zoomingLastTick = bl;
   }

   private void a(boolean bl, double d) {
      if (this.scrollSteps > this.lastScrollTier) {
         this.resetting = false;
      }

      double var4 = this.scrollSteps / 10.0;
      this.prevScrollInterpolation = this.scrollInterpolation;
      if (!bl) {
         this.scrollInterpolation = var4;
         this.prevScrollInterpolation = var4;
      } else {
         double var6 = var4 - this.scrollInterpolation;
         this.scrollInterpolation += var6 * 0.7 / 0.05 * d;
      }

      this.lastScrollTier = this.scrollSteps;
   }
}
