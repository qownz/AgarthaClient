package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class q extends net.agartha.client.d.c {
   public static final net.agartha.client.d.j<q> TYPE = new net.agartha.client.d.j<>("TickEvent");
   private static final q INSTANCE = new q();

   private q() {
   }

   public static void dispatch() {
      TYPE.dispatch(INSTANCE);
   }
}
