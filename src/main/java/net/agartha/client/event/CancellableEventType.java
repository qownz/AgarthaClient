package net.agartha.client.event;

import java.util.function.Consumer;
import net.agartha.client.event.CancellableEvent;
import net.agartha.client.event.EventType;
import net.agartha.client.util.perf.Profiler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CancellableEventType<E extends CancellableEvent> extends EventType<E> {
   public CancellableEventType() {
   }

   public CancellableEventType(String string) {
      super(string);
   }

   public void dispatchPre(E arg) {
      dispatchPreCancellable(this.list.preSnap(), (E)arg);
   }

   public void dispatchPost(E arg) {
      dispatchPostGuarded(this.list.postSnap(), (E)arg);
   }

   static <E extends CancellableEvent> void dispatchPreCancellable(ListenerRegistry.a<E> arg, E arg2) {
      Consumer[] var2 = arg.callbacks;

      for (int var3 = 0; var3 < var2.length; var3++) {
         long var4 = Profiler.begin();

         try {
            var2[var3].accept(arg2);
         } catch (Exception var10) {
            ListenerErrorReporter.report(arg.handles[var3], arg2, var10);
         } finally {
            Profiler.recordListener(arg2, arg.handles[var3], var4);
         }

         if (arg2.cancelled) {
            break;
         }
      }
   }

   static <E extends CancellableEvent> void dispatchPostGuarded(ListenerRegistry.a<E> arg, E arg2) {
      arg2.postStage = true;

      try {
         Consumer[] var2 = arg.callbacks;

         for (int var3 = 0; var3 < var2.length; var3++) {
            long var4 = Profiler.begin();

            try {
               var2[var3].accept(arg2);
            } catch (Exception var15) {
               ListenerErrorReporter.report(arg.handles[var3], arg2, var15);
            } finally {
               Profiler.recordListener(arg2, arg.handles[var3], var4);
            }
         }
      } finally {
         arg2.postStage = false;
      }
   }
}
