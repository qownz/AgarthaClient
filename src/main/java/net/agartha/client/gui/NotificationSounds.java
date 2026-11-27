package net.agartha.client.gui;

import net.agartha.client.AgarthaClientMod;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.client.NotificationsModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;

@Environment(EnvType.CLIENT)
public final class NotificationSounds {
   private static final long CONFIGURED_SOUND_COOLDOWN_MS = 350L;
   private static long lastConfiguredSoundAtMs;

   private NotificationSounds() {
   }

   public static void playConfigured() {
      NotificationsModule var0 = ModuleManager.get(NotificationsModule.class);
      if (var0 != null && var0.isEnabled() && var0.sound()) {
         long var1 = System.currentTimeMillis();
         if (var1 - lastConfiguredSoundAtMs >= 350L) {
            lastConfiguredSoundAtMs = var1;
            a(resolve(var0.soundEffect()), var0.effectiveVolume());
         }
      }
   }

   public static SoundEvent resolve(String string) {
      if ("Pop 1".equals(string)) {
         return AgarthaClientMod.NOTIFY_POP_1_SOUND;
      } else {
         return "Pop 2".equals(string) ? AgarthaClientMod.NOTIFY_POP_2_SOUND : AgarthaClientMod.SPAWNER_NOTIFY_SOUND;
      }
   }

   private static void a(SoundEvent soundEvent, float f) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null && var2.world != null && var2.getSoundManager() != null && soundEvent != null) {
         float var3 = Math.max(0.0F, Math.min(1.0F, f));
         if (!(var3 <= 0.001F)) {
            var2.getSoundManager().play(PositionedSoundInstance.ui(soundEvent, 1.0F, var3));
         }
      }
   }
}
