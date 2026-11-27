package net.agartha.client.n;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record c(
   boolean present,
   boolean playing,
   String title,
   String artist,
   String album,
   double positionSeconds,
   double durationSeconds,
   byte[] albumArtBytes,
   long artHash,
   long sampledAtNanos
) {
   public static final c IDLE = new c(false, false, "", "", "", 0.0, 0.0, null, 0L, 0L);

   public double progress01() {
      return this.durationSeconds <= 0.001 ? 0.0 : Math.max(0.0, Math.min(1.0, this.positionSeconds / this.durationSeconds));
   }

   public c withSmoothedPosition(long l) {
      if (this.present && this.playing && !(this.durationSeconds <= 0.0) && this.sampledAtNanos > 0L) {
         double var3 = (l - this.sampledAtNanos) / 1.0E9;
         double var5 = Math.min(this.durationSeconds, Math.max(0.0, this.positionSeconds + var3));
         return new c(this.present, this.playing, this.title, this.artist, this.album, var5, this.durationSeconds, this.albumArtBytes, this.artHash, l);
      } else {
         return this;
      }
   }
}
