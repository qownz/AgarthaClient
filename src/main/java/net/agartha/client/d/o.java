package net.agartha.client.d;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class o<E extends c> {
   private volatile o.a<E> pre = o.a.empty();
   private volatile o.a<E> post = o.a.empty();
   private static final Comparator<n<?>> BY_PRIORITY_DESC = Comparator.<n<?>>comparingInt(arg -> arg.priority().value).reversed();

   public synchronized void register(n<E> arg) {
      if (arg.stage() == r.PRE) {
         this.pre = new o.a<>(this.a(this.pre.handles, arg));
      } else {
         this.post = new o.a<>(this.a(this.post.handles, arg));
      }
   }

   public synchronized void unregister(n<E> arg) {
      if (arg.stage() == r.PRE) {
         n[] var2 = this.b(this.pre.handles, arg);
         if (var2 != this.pre.handles) {
            this.pre = new o.a<>(var2);
         }
      } else {
         n[] var3 = this.b(this.post.handles, arg);
         if (var3 != this.post.handles) {
            this.post = new o.a<>(var3);
         }
      }
   }

   public void dispatchPre(E arg) {
      a(this.pre, (E)arg);
   }

   public void dispatchPost(E arg) {
      a(this.post, (E)arg);
   }

   private static <E extends c> void a(o.a<E> arg, E arg2) {
      Consumer[] var2 = arg.callbacks;
      if (!net.agartha.client.k.b_o.enabled()) {
         for (int var14 = 0; var14 < var2.length; var14++) {
            try {
               var2[var14].accept(arg2);
            } catch (Exception var11) {
               m.report(arg.handles[var14], arg2, var11);
            }
         }
      } else {
         for (int var3 = 0; var3 < var2.length; var3++) {
            long var4 = net.agartha.client.k.b_o.begin();

            try {
               var2[var3].accept(arg2);
            } catch (Exception var12) {
               m.report(arg.handles[var3], arg2, var12);
            } finally {
               net.agartha.client.k.b_o.recordListener(arg2, arg.handles[var3], var4);
            }
         }
      }
   }

   o.a<E> preSnap() {
      return this.pre;
   }

   o.a<E> postSnap() {
      return this.post;
   }

   public n<E>[] preSnapshot() {
      return this.pre.handles;
   }

   public n<E>[] postSnapshot() {
      return this.post.handles;
   }

   private n<E>[] a(n<E>[] args, n<E> arg) {
      n[] var3 = Arrays.copyOf(args, args.length + 1);
      var3[args.length] = arg;
      Arrays.sort(var3, (Comparator)BY_PRIORITY_DESC);
      return var3;
   }

   private n<E>[] b(n<E>[] args, n<E> arg) {
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
         n[] var5 = new n[args.length - 1];
         System.arraycopy(args, 0, var5, 0, var3);
         System.arraycopy(args, var3 + 1, var5, var3, args.length - var3 - 1);
         return var5;
      }
   }

   @Environment(EnvType.CLIENT)
   static final class a<E extends c> {
      private static final o.a EMPTY = new o.a(new n[0]);
      final n<E>[] handles;
      final Consumer<E>[] callbacks;

      a(n<E>[] args) {
         this.handles = args;
         this.callbacks = new Consumer[args.length];

         for (int var2 = 0; var2 < args.length; var2++) {
            this.callbacks[var2] = args[var2].callback();
         }
      }

      static <E extends c> o.a<E> empty() {
         return EMPTY;
      }
   }
}
