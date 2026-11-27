package net.agartha.client.util.b_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record c(float yaw, float pitch) {
}
