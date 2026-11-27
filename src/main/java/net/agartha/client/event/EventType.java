package net.agartha.client.event;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import net.agartha.client.event.Event;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EventType<E extends Event> {
   static final CopyOnWriteArrayList<EventType<?>> REGISTRY = new CopyOnWriteArrayList<>();
   protected final ListenerRegistry<E> list = new ListenerRegistry<>();
   private final String name;

   public EventType() {
      this("unnamed");
   }

   public EventType(String string) {
      this.name = string;
      REGISTRY.add(this);
   }

   public final String name() {
      return this.name;
   }

   public synchronized void register(ListenerHandle<E> arg) {
      this.list.register(arg);
   }

   public synchronized void unregister(ListenerHandle<E> arg) {
      this.list.unregister(arg);
   }

   public ListenerHandle<E> listen(Object object, Consumer<E> consumer) {
      return this.listen(object, EventStage.PRE, EventPriority.NORMAL, consumer);
   }

   public ListenerHandle<E> listen(Object object, EventPriority arg, Consumer<E> consumer) {
      return this.listen(object, EventStage.PRE, arg, consumer);
   }

   public ListenerHandle<E> listen(Object object, EventStage arg, EventPriority arg2, Consumer<E> consumer) {
      ListenerHandle var5 = new ListenerHandle<>(this, arg, arg2, object, consumer);
      this.register(var5);
      return var5;
   }

   public boolean hasPreListeners() {
      return this.list.preSnapshot().length != 0;
   }

   public boolean hasPostListeners() {
      return this.list.postSnapshot().length != 0;
   }

   public boolean hasListeners() {
      return this.hasPreListeners() || this.hasPostListeners();
   }

   public final void dispatch(E arg) {
      this.dispatchPre((E)arg);
   }

   public void dispatchPre(E arg) {
      this.list.dispatchPre((E)arg);
   }

   public void dispatchPost(E arg) {
      this.list.dispatchPost((E)arg);
   }

   public int listenerCount() {
      return this.list.preSnapshot().length + this.list.postSnapshot().length;
   }
}
