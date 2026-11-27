package net.agartha.client.d;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jctools.queues.MpmcArrayQueue;

@Environment(EnvType.CLIENT)
public final class h<E extends c> {
   private final MpmcArrayQueue<E> pool;
   private final Supplier<E> factory;

   public h(Supplier<E> supplier, int i) {
      this.factory = supplier;
      this.pool = new MpmcArrayQueue(i);
   }

   public E acquire() {
      c var1 = (c)this.pool.poll();
      if (var1 == null) {
         var1 = this.factory.get();
      }

      return (E)var1;
   }

   public void release(E arg) {
      this.pool.offer(arg);
   }
}
