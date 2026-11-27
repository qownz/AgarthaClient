package net.agartha.client.event.impl;

import net.agartha.client.event.CancellableEvent;
import net.agartha.client.event.CancellableEventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MouseScrollEvent extends CancellableEvent {
   public static final CancellableEventType<MouseScrollEvent> TYPE = new CancellableEventType<>("MouseScrollEvent");
   public double value;
   private static final MouseScrollEvent INSTANCE = new MouseScrollEvent();

   private MouseScrollEvent() {
   }

   public static MouseScrollEvent get(double d) {
      INSTANCE.resetCancellation();
      INSTANCE.value = d;
      return INSTANCE;
   }

   public static boolean dispatch(double d) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.value = d;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         return INSTANCE.isCancelled();
      }
   }
}
