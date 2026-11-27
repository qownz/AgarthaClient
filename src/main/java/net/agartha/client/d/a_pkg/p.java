package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class p extends net.agartha.client.d.c {
   public static final net.agartha.client.d.j<p> TYPE = new net.agartha.client.d.j<>("ScanFrameEvent");
   private static final p INSTANCE = new p();

   private p() {
   }

   public static void dispatch() {
      if (TYPE.hasPreListeners()) {
         TYPE.dispatch(INSTANCE);
      }
   }
}
