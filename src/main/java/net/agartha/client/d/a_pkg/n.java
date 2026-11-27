package net.agartha.client.d.a_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;

@Environment(EnvType.CLIENT)
public final class n extends net.agartha.client.d.c {
   public static final net.agartha.client.d.j<n> TYPE = new net.agartha.client.d.j<>("Render2DEvent");
   public DrawContext graphics;
   public float partialTick;
   private static final n INSTANCE = new n();

   private n() {
   }

   public static void dispatch(DrawContext drawContext, float f) {
      if (TYPE.hasPreListeners()) {
         INSTANCE.graphics = drawContext;
         INSTANCE.partialTick = f;
         TYPE.dispatch(INSTANCE);
         INSTANCE.graphics = null;
      }
   }
}
