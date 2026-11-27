package net.agartha.client.event.impl;

import net.agartha.client.event.Event;
import net.agartha.client.event.EventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class EndClientTickEvent extends Event {
   public static final EventType<EndClientTickEvent> TYPE = new EventType<>("EndClientTickEvent");
   private static final EndClientTickEvent INSTANCE = new EndClientTickEvent();

   private EndClientTickEvent() {
   }

   public static void dispatch() {
      TYPE.dispatch(INSTANCE);
   }
}
