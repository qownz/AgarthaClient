package net.agartha.client.event;

import java.util.function.Supplier;
import net.agartha.client.event.Event;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jctools.queues.MpmcArrayQueue;

@Environment(EnvType.CLIENT)
public final class EventPool<E extends Event> {
   private final MpmcArrayQueue<E> pool;
   private final Supplier<E> factory;

   public EventPool(Supplier<E> supplier, int i) {
      this.factory = supplier;
      this.pool = new MpmcArrayQueue(i);
   }

   public E acquire() {
      Event var1 = (Event)this.pool.poll();
      if (var1 == null) {
         var1 = this.factory.get();
      }

      return (E)var1;
   }

   public void release(E arg) {
      this.pool.offer(arg);
   }
}
