package net.agartha.client.event.impl;

import net.agartha.client.event.CancellableEvent;
import net.agartha.client.event.CancellableEventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public final class PlayerMoveEvent extends CancellableEvent {
   public static final CancellableEventType<PlayerMoveEvent> TYPE = new CancellableEventType<>("PlayerMoveEvent");
   public double x;
   public double y;
   public double z;
   private static final PlayerMoveEvent INSTANCE = new PlayerMoveEvent();

   private PlayerMoveEvent() {
   }

   public static boolean dispatch(Vec3d vec3d) {
      if (!TYPE.hasPreListeners()) {
         return false;
      } else {
         INSTANCE.x = vec3d.x;
         INSTANCE.y = vec3d.y;
         INSTANCE.z = vec3d.z;
         INSTANCE.resetCancellation();
         TYPE.dispatch(INSTANCE);
         return INSTANCE.isCancelled();
      }
   }
}
