package net.agartha.client.event.impl;

import net.agartha.client.event.Event;
import net.agartha.client.event.EventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;

@Environment(EnvType.CLIENT)
public final class Render2DEvent extends Event {
   public static final EventType<Render2DEvent> TYPE = new EventType<>("Render2DEvent");
   public DrawContext graphics;
   public float partialTick;
   private static final Render2DEvent INSTANCE = new Render2DEvent();

   private Render2DEvent() {
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
