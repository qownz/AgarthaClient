package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class d extends net.agartha.client.d.c {
   public static final net.agartha.client.d.j<d> TYPE = new net.agartha.client.d.j<>("GetFovEvent");
   public float fov;
   private static final d INSTANCE = new d();

   private d() {
   }

   public static d get(float f) {
      INSTANCE.fov = f;
      return INSTANCE;
   }
}
