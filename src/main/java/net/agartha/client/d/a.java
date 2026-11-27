package net.agartha.client.d;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class a extends c {
   boolean cancelled;
   boolean postStage;

   public final void setCancelled(boolean bl) {
      if (!this.postStage) {
         this.cancelled = bl;
      }
   }

   public final boolean isCancelled() {
      return this.cancelled;
   }

   protected void resetCancellation() {
      this.cancelled = false;
      this.postStage = false;
   }
}
