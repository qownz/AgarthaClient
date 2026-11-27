package net.agartha.client.event.impl;

import net.agartha.client.event.CancellableEvent;
import net.agartha.client.event.CancellableEventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class KeyEvent extends CancellableEvent {
   public static final CancellableEventType<KeyEvent> TYPE = new CancellableEventType<>("KeyEvent");
   public int key;
   public int scancode;
   public int action;
   public int mods;
   private static final KeyEvent INSTANCE = new KeyEvent();

   private KeyEvent() {
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
