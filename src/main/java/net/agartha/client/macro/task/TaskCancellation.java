package net.agartha.client.macro.task;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class TaskCancellation {
   private final AtomicBoolean cancelled = new AtomicBoolean(false);

   public void cancel() {
      this.cancelled.set(true);
   }

   public boolean isCancelled() {
      return this.cancelled.get();
   }

   public boolean sleep(long l) {
      long var3 = System.currentTimeMillis() + l;

      while (!this.cancelled.get() && System.currentTimeMillis() < var3) {
         LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1L));
      }

      return this.cancelled.get();
   }
}
