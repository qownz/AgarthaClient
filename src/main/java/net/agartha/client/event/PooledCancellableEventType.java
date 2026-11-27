package net.agartha.client.event;

import java.util.function.Supplier;
import net.agartha.client.event.CancellableEvent;
import net.agartha.client.event.CancellableEventType;
import net.agartha.client.event.PooledEventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PooledCancellableEventType<E extends CancellableEvent> extends PooledEventType<E> {
   public PooledCancellableEventType(Supplier<E> supplier, int i) {
      super(supplier, i);
   }

   public PooledCancellableEventType(String string, Supplier<E> supplier, int i) {
      super(string, supplier, i);
   }

   public void dispatchPre(E arg) {
      CancellableEventType.dispatchPreCancellable(this.list.preSnap(), (E)arg);
   }

   public void dispatchPost(E arg) {
      CancellableEventType.dispatchPostGuarded(this.list.postSnap(), (E)arg);
   }
}
