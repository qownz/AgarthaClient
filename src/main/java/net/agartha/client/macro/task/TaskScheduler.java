package net.agartha.client.macro.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.macro.task.TaskCancellation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class TaskScheduler {
   private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
      Thread var1 = new Thread(runnable, "agartha-macro");
      var1.setDaemon(true);
      return var1;
   });
   private final AtomicReference<TaskCancellation> active = new AtomicReference<>();

   public boolean start(MacroTask arg) {
      TaskCancellation var2 = new TaskCancellation();
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
      TaskCancellation var1 = this.active.getAndSet(null);
      if (var1 != null) {
         var1.cancel();
      }
   }

   public boolean isRunning() {
      return this.active.get() != null;
   }
}
