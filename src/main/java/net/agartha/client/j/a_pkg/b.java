package net.agartha.client.j.a_pkg;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import net.agartha.client.AgarthaClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class b {
   private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
      Thread var1 = new Thread(runnable, "agartha-macro");
      var1.setDaemon(true);
      return var1;
   });
   private final AtomicReference<a> active = new AtomicReference<>();

   public boolean start(c arg) {
      a var2 = new a();
      if (!this.active.compareAndSet(null, var2)) {
         return false;
      } else {
         this.executor.submit(() -> {
            try {
               arg.run(var2);
            } catch (Throwable var7) {
               AgarthaClientMod.LOGGER.warn("[Macro] task threw", var7);
            } finally {
               this.active.compareAndSet(var2, null);
            }
         });
         return true;
      }
   }

   public void stop() {
      a var1 = this.active.getAndSet(null);
      if (var1 != null) {
         var1.cancel();
      }
   }

   public boolean isRunning() {
      return this.active.get() != null;
   }
}
