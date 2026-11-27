package net.agartha.client.integration.spotify;

import net.agartha.client.integration.spotify.SpotifyArtTexture;
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
public final class SpotifySmtcBridge {
   private static final SpotifySmtcBridge INSTANCE = new SpotifySmtcBridge();

   private SpotifySmtcBridge() {
   }

   public static SpotifySmtcBridge getInstance() {
      return INSTANCE;
   }

   public boolean isAvailable() {
      return false;
   }

   public SpotifyTrack snapshot() {
      return SpotifyTrack.IDLE;
   }

   public synchronized void ensureStarted() {
   }

   public synchronized void shutdown() {
   }

   public void debugTick(SpotifyTrack arg, SpotifyArtTexture arg2) {
   }
}
