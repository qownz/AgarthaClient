package net.agartha.client.util;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2FloatMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class a_o {
   private final float inSeconds;
   private final float outSeconds;
   private final Long2FloatOpenHashMap alpha = new Long2FloatOpenHashMap();
   private final Long2IntOpenHashMap seen = new Long2IntOpenHashMap();
   private int frame;
   private long lastNanos;
   private float dt;

   public a_o(float f, float g) {
      this.inSeconds = f;
      this.outSeconds = g;
      this.alpha.defaultReturnValue(0.0F);
      this.seen.defaultReturnValue(-1);
   }

   public void begin() {
      long var1 = System.nanoTime();
      this.dt = this.lastNanos == 0L ? 0.016F : Math.min((float)(var1 - this.lastNanos) / 1.0E9F, 0.1F);
      this.lastNanos = var1;
      this.frame++;
   }

   public float show(long l) {
      float var3 = Math.min(1.0F, this.alpha.get(l) + this.dt / this.inSeconds);
      this.alpha.put(l, var3);
      this.seen.put(l, this.frame);
      return ease(var3);
   }

   public void sweep(net.agartha.client.util.a_o.a arg) {
      ObjectIterator var2 = this.alpha.long2FloatEntrySet().fastIterator();

      while (var2.hasNext()) {
         Entry var3 = (Entry)var2.next();
         long var4 = var3.getLongKey();
         if (this.seen.get(var4) != this.frame) {
            float var6 = var3.getFloatValue() - this.dt / this.outSeconds;
            if (var6 <= 0.0F) {
               var2.remove();
               this.seen.remove(var4);
            } else {
               var3.setValue(var6);
               if (arg != null) {
                  arg.draw(var4, ease(var6));
               }
            }
         }
      }
   }

   public void clear() {
      this.alpha.clear();
      this.seen.clear();
      this.lastNanos = 0L;
   }

   public static float ease(float f) {
      float var1 = f < 0.0F ? 0.0F : Math.min(1.0F, f);
      return var1 * var1 * (3.0F - 2.0F * var1);
   }

   public static double rise(float f) {
      return (1.0 - f) * 2.5;
   }

   public void drop(long l) {
      this.alpha.remove(l);
      this.seen.remove(l);
   }

   public void forEachVisible(net.agartha.client.util.a_o.a arg) {
      ObjectIterator var2 = this.alpha.long2FloatEntrySet().fastIterator();

      while (var2.hasNext()) {
         Entry var3 = (Entry)var2.next();
         float var4 = ease(var3.getFloatValue());
         if (var4 > 0.01F) {
            arg.draw(var3.getLongKey(), var4);
         }
      }
   }

   public boolean isEmpty() {
      return this.alpha.isEmpty();
   }

   public boolean contains(long l) {
      return this.alpha.containsKey(l);
   }

   public static double pop(float f) {
      return 0.35 + 0.65 * f;
   }

   @Environment(EnvType.CLIENT)
   public interface a {
      void draw(long l, float f);
   }
}
