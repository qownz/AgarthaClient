package net.agartha.client.d;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record n<E extends c>(j<E> type, r stage, i priority, Object owner, Consumer<E> callback) {
   public void unregister() {
      this.type.unregister(this);
   }
}
