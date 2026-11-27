package net.agartha.client.event.impl;

import net.agartha.client.event.Event;
import net.agartha.client.event.EventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class GetFovEvent extends Event {
   public static final EventType<GetFovEvent> TYPE = new EventType<>("GetFovEvent");
   public float fov;
   private static final GetFovEvent INSTANCE = new GetFovEvent();

   private GetFovEvent() {
   }

   public static GetFovEvent get(float f) {
      INSTANCE.fov = f;
      return INSTANCE;
   }
}
