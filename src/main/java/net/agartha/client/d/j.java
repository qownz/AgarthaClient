package net.agartha.client.d;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class j<E extends c> {
   static final CopyOnWriteArrayList<j<?>> REGISTRY = new CopyOnWriteArrayList<>();
   protected final o<E> list = new o<>();
   private final String name;

   public j() {
      this("unnamed");
   }

   public j(String string) {
      this.name = string;
      REGISTRY.add(this);
   }

   public final String name() {
      return this.name;
   }

   public synchronized void register(n<E> arg) {
      this.list.register(arg);
   }

   public synchronized void unregister(n<E> arg) {
      this.list.unregister(arg);
   }

   public n<E> listen(Object object, Consumer<E> consumer) {
      return this.listen(object, r.PRE, i.NORMAL, consumer);
   }

   public n<E> listen(Object object, i arg, Consumer<E> consumer) {
      return this.listen(object, r.PRE, arg, consumer);
   }

   public n<E> listen(Object object, r arg, i arg2, Consumer<E> consumer) {
      n var5 = new n<>(this, arg, arg2, object, consumer);
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
