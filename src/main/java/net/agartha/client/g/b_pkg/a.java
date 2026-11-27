package net.agartha.client.g.b_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface a {
   boolean isAvailable();

   void mouseDown(c arg);

   void mouseUp(c arg);

   void click(c arg, int i);

   void moveRelative(int i, int j);

   void tapSlot(int i, int j);
}
