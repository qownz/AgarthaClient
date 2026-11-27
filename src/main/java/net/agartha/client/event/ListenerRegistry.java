package net.agartha.client.event;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;
import net.agartha.client.event.Event;
import net.agartha.client.event.EventStage;
import net.agartha.client.event.ListenerHandle;
import net.agartha.client.util.perf.Profiler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ListenerRegistry<E extends Event> {
   private volatile ListenerRegistry.a<E> pre = ListenerRegistry.a.empty();
   private volatile ListenerRegistry.a<E> post = ListenerRegistry.a.empty();
   private static final Comparator<ListenerHandle<?>> BY_PRIORITY_DESC = Comparator.<ListenerHandle<?>>comparingInt(arg -> arg.priority().value).reversed();

   public synchronized void register(ListenerHandle<E> arg) {
      if (arg.stage() == EventStage.PRE) {
         this.pre = new ListenerRegistry.a<>(this.a(this.pre.handles, arg));
      } else {
         this.post = new ListenerRegistry.a<>(this.a(this.post.handles, arg));
      }
   }

   public synchronized void unregister(ListenerHandle<E> arg) {
      if (arg.stage() == EventStage.PRE) {
         ListenerHandle[] var2 = this.b(this.pre.handles, arg);
         if (var2 != this.pre.handles) {
            this.pre = new ListenerRegistry.a<>(var2);
         }
      } else {
         ListenerHandle[] var3 = this.b(this.post.handles, arg);
         if (var3 != this.post.handles) {
            this.post = new ListenerRegistry.a<>(var3);
         }
      }
   }

   public void dispatchPre(E arg) {
      a(this.pre, (E)arg);
   }

   public void dispatchPost(E arg) {
      a(this.post, (E)arg);
   }

   private static <E extends Event> void a(ListenerRegistry.a<E> arg, E arg2) {
      Consumer[] var2 = arg.callbacks;
      if (!Profiler.enabled()) {
         for (int var14 = 0; var14 < var2.length; var14++) {
            try {
               var2[var14].accept(arg2);
            } catch (Exception var11) {
               ListenerErrorReporter.report(arg.handles[var14], arg2, var11);
            }
         }
      } else {
         for (int var3 = 0; var3 < var2.length; var3++) {
            long var4 = Profiler.begin();

            try {
               var2[var3].accept(arg2);
            } catch (Exception var12) {
               ListenerErrorReporter.report(arg.handles[var3], arg2, var12);
            } finally {
               Profiler.recordListener(arg2, arg.handles[var3], var4);
            }
         }
      }
   }

   ListenerRegistry.a<E> preSnap() {
      return this.pre;
   }

   ListenerRegistry.a<E> postSnap() {
      return this.post;
   }

   public ListenerHandle<E>[] preSnapshot() {
      return this.pre.handles;
   }

   public ListenerHandle<E>[] postSnapshot() {
      return this.post.handles;
   }

   private ListenerHandle<E>[] a(ListenerHandle<E>[] args, ListenerHandle<E> arg) {
      ListenerHandle[] var3 = Arrays.copyOf(args, args.length + 1);
      var3[args.length] = arg;
      Arrays.sort(var3, (Comparator)BY_PRIORITY_DESC);
      return var3;
   }

   private ListenerHandle<E>[] b(ListenerHandle<E>[] args, ListenerHandle<E> arg) {
      int var3 = -1;

      for (int var4 = 0; var4 < args.length; var4++) {
         if (args[var4] == arg) {
            var3 = var4;
            break;
         }
      }

      if (var3 < 0) {
         return args;
      } else {
         ListenerHandle[] var5 = new ListenerHandle[args.length - 1];
         System.arraycopy(args, 0, var5, 0, var3);
         System.arraycopy(args, var3 + 1, var5, var3, args.length - var3 - 1);
         return var5;
      }
   }

   @Environment(EnvType.CLIENT)
   static final class a<E extends Event> {
      private static final ListenerRegistry.a EMPTY = new ListenerRegistry.a(new ListenerHandle[0]);
      final ListenerHandle<E>[] handles;
      final Consumer<E>[] callbacks;

      a(ListenerHandle<E>[] args) {
         this.handles = args;
         this.callbacks = new Consumer[args.length];

         for (int var2 = 0; var2 < args.length; var2++) {
            this.callbacks[var2] = args[var2].callback();
         }
      }

      static <E extends Event> ListenerRegistry.a<E> empty() {
         return EMPTY;
      }
   }
}
