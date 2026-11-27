package net.agartha.client.e;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ap<K> {
   public static final float VISIBLE_EPSILON = 0.0015F;
   private static final float FULL_DURATION = 0.24F;
   private static final float MIN_DURATION = 0.08F;
   private final Map<K, ap.a> states = new HashMap<>();

   public boolean isOpen(K object) {
      ap.a var2 = this.states.get(object);
      return var2 != null && var2.open;
   }

   public float factor(K object) {
      return this.factor((K)object, System.nanoTime());
   }

   public float factor(K object, long l) {
      ap.a var4 = this.states.get(object);
      if (var4 == null) {
         return 0.0F;
      } else {
         float var5 = var4.open ? 1.0F : 0.0F;
         if (var4.duration <= 0.0F) {
            return var5;
         } else {
            float var6 = (float)(l - var4.startNanos) / (var4.duration * 1.0E9F);
            if (var6 >= 1.0F) {
               var4.duration = 0.0F;
               var4.from = var5;
               return var5;
            } else if (var6 <= 0.0F) {
               return var4.from;
            } else {
               float var7 = 1.0F - var6;
               float var8 = 1.0F - var7 * var7 * var7;
               return var4.from + (var5 - var4.from) * var8;
            }
         }
      }
   }

   public void setOpen(K object, boolean bl) {
      this.setOpen((K)object, bl, System.nanoTime());
   }

   public void setOpen(K object, boolean bl, long l) {
      ap.a var5 = this.states.get(object);
      if (var5 == null) {
         if (!bl) {
            return;
         }

         var5 = new ap.a();
         this.states.put((K)object, var5);
      } else if (var5.open == bl) {
         return;
      }

      float var6 = this.factor((K)object, l);
      float var7 = bl ? 1.0F : 0.0F;
      var5.open = bl;
      var5.from = var6;
      var5.startNanos = l;
      var5.duration = Math.max(0.08F, 0.24F * Math.abs(var7 - var6));
   }

   public boolean toggle(K object) {
      return this.toggle((K)object, System.nanoTime());
   }

   public boolean toggle(K object, long l) {
      boolean var4 = !this.isOpen((K)object);
      this.setOpen((K)object, var4, l);
      return var4;
   }

   public void prime(K object, boolean bl) {
      if (bl && !this.states.containsKey(object)) {
         ap.a var3 = new ap.a();
         var3.open = true;
         var3.from = 1.0F;
         var3.duration = 0.0F;
         this.states.put((K)object, var3);
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class a {
      boolean open;
      float from;
      long startNanos;
      float duration;
   }
}
