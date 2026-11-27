package net.agartha.client.module.impl.render;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SealAccess {
   private SealAccess() {
   }

   public static Lookup lookup() {
      return MethodHandles.lookup();
   }
}
