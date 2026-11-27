package net.agartha.client.event.impl;

import net.agartha.client.event.CancellableEvent;
import net.agartha.client.event.CancellableEventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ChatSendEvent extends CancellableEvent {
   public static final CancellableEventType<ChatSendEvent> TYPE = new CancellableEventType<>("ChatSendEvent");
   public String message;
   private static final ChatSendEvent INSTANCE = new ChatSendEvent();

   private ChatSendEvent() {
   }

   public static boolean dispatch(String string) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.message = string;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         boolean var1 = INSTANCE.isCancelled();
         INSTANCE.message = null;
         return var1;
      }
   }
}
