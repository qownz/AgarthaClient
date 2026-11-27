package net.agartha.client.i;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class b {
   private static final AtomicBoolean SUPPRESS_MOUSE_ONCE = new AtomicBoolean(false);

   private b() {
   }

   public static void suppressNextMouseClick() {
      SUPPRESS_MOUSE_ONCE.set(true);
   }

   public static boolean consumeMouseSuppression() {
      return SUPPRESS_MOUSE_ONCE.getAndSet(false);
   }

   public static void playPack(String string, float f, int i) {
      MinecraftClient var3 = MinecraftClient.getInstance();
      if (var3 != null && var3.getSoundManager() != null) {
         float var4 = Math.max(0.0F, Math.min(1.0F, f));
         float var5 = var3.options.getCategorySoundVolume(SoundCategory.PLAYERS);
         float var6 = Math.max(0.0F, Math.min(1.0F, var4 * var5));
         int var7 = Math.max(1, Math.min(8, i));

         for (int var8 = 0; var8 < var7; var8++) {
            Identifier var9 = a(string);
            SoundEvent var10 = SoundEvent.of(var9);
            var3.getSoundManager().play(PositionedSoundInstance.ui(var10, var6, 1.0F));
         }
      }
   }

   private static Identifier a(String string) {
      int var1 = ThreadLocalRandom.current().nextInt(1, 41);
      String var2 = var1 < 10 ? "0" + var1 : Integer.toString(var1);
      return Identifier.of("agartha", "keyboards." + string + ".key_" + var2);
   }
}
