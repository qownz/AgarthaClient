package net.agartha.client.event.impl;

import net.agartha.client.event.Event;
import net.agartha.client.event.EventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ScanFrameEvent extends Event {
   public static final EventType<ScanFrameEvent> TYPE = new EventType<>("ScanFrameEvent");
   private static final ScanFrameEvent INSTANCE = new ScanFrameEvent();

   private ScanFrameEvent() {
   }

   public static void dispatch() {
      if (TYPE.hasPreListeners()) {
         TYPE.dispatch(INSTANCE);
      }
   }
}
