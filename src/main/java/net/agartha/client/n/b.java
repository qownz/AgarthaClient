package net.agartha.client.n;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Spotify SMTC bridge stub.
 *
 * The original implementation used the bundled dev.codeman:smtc4j native library.
 * The native library and its Java bindings are intentionally not part of this
 * build, so the HUD reports "unavailable" and stays idle.
 */
@Environment(EnvType.CLIENT)
public final class b {
   private static final b INSTANCE = new b();

   private b() {
   }

   public static b getInstance() {
      return INSTANCE;
   }

   public boolean isAvailable() {
      return false;
   }

   public c snapshot() {
      return c.IDLE;
   }

   public synchronized void ensureStarted() {
   }

   public synchronized void shutdown() {
   }

   public void debugTick(c arg, a_o arg2) {
   }
}