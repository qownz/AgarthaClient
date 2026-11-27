package net.agartha.client.event;

import java.util.function.Consumer;
import net.agartha.client.event.Event;
import net.agartha.client.event.EventPriority;
import net.agartha.client.event.EventStage;
import net.agartha.client.event.EventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record ListenerHandle<E extends Event>(EventType<E> type, EventStage stage, EventPriority priority, Object owner, Consumer<E> callback) {
   public void unregister() {
      this.type.unregister(this);
   }
}
