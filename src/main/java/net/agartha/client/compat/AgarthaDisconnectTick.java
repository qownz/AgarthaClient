package net.agartha.client.compat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface AgarthaDisconnectTick {
   void agartha$tickReconnect();
}
