package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class c extends net.agartha.client.d.c {
   public static final net.agartha.client.d.j<c> TYPE = new net.agartha.client.d.j<>("EndClientTickEvent");
   private static final c INSTANCE = new c();

   private c() {
   }

   public static void dispatch() {
      TYPE.dispatch(INSTANCE);
   }
}
