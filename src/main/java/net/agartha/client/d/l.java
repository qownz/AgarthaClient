package net.agartha.client.d;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface l<E extends c> {
   void invoke(E arg);
}
