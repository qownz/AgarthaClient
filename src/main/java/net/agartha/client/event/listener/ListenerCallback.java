package net.agartha.client.event.listener;

import net.agartha.client.event.Event;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface ListenerCallback<E extends Event> {
   void invoke(E arg);
}
