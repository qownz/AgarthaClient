package net.agartha.client.platform.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface OsInputBridge {
   boolean isAvailable();

   void mouseDown(MouseButton arg);

   void mouseUp(MouseButton arg);

   void click(MouseButton arg, int i);

   void moveRelative(int i, int j);

   void tapSlot(int i, int j);
}
