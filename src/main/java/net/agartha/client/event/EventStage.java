package net.agartha.client.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum EventStage {
   PRE,
   POST;
}
