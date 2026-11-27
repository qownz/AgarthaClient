package net.agartha.client.module;

import imgui.ImDrawList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface TracerOverlay {
   boolean isTracerOverlayEnabled();

   void renderTracerOverlay(ImDrawList imDrawList, float f, float g);
}
