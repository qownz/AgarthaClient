package net.agartha.client.input.macro;

import net.agartha.client.platform.input.MouseButton;
import net.agartha.client.platform.input.OsInputBridge;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class FallbackInputBridge implements OsInputBridge {
   @Override
   public boolean isAvailable() {
      return false;
   }

   @Override
   public void mouseDown(MouseButton arg) {
   }

   @Override
   public void mouseUp(MouseButton arg) {
   }

   @Override
   public void click(MouseButton arg, int i) {
   }

   @Override
   public void moveRelative(int i, int j) {
   }

   @Override
   public void tapSlot(int i, int j) {
   }
}
