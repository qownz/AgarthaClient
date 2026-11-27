package net.agartha.client.event.impl;

import net.agartha.client.event.CancellableEvent;
import net.agartha.client.event.CancellableEventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class KeyInputEvent extends CancellableEvent {
   public static final CancellableEventType<KeyInputEvent> TYPE = new CancellableEventType<>("KeyInputEvent");
   public int key;
   public int scancode;
   public int action;
   public int mods;
   private static final KeyInputEvent INSTANCE = new KeyInputEvent();

   private KeyInputEvent() {
   }

   public static boolean dispatch(int i, int j, int k, int l) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.key = i;
         INSTANCE.scancode = j;
         INSTANCE.action = k;
         INSTANCE.mods = l;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         return INSTANCE.isCancelled();
      }
   }
}
