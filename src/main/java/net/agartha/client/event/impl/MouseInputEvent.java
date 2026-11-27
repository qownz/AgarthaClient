package net.agartha.client.event.impl;

import net.agartha.client.event.CancellableEvent;
import net.agartha.client.event.CancellableEventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MouseInputEvent extends CancellableEvent {
   public static final CancellableEventType<MouseInputEvent> TYPE = new CancellableEventType<>("MouseInputEvent");
   public long window;
   public int button;
   public int action;
   public int mods;
   private static final MouseInputEvent INSTANCE = new MouseInputEvent();

   private MouseInputEvent() {
   }

   public static boolean dispatch(long l, int i, int j, int k) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.window = l;
         INSTANCE.button = i;
         INSTANCE.action = j;
         INSTANCE.mods = k;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         return INSTANCE.isCancelled();
      }
   }
}
