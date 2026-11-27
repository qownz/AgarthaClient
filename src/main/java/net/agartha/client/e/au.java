package net.agartha.client.e;

import java.util.concurrent.ThreadLocalRandom;
import net.agartha.client.module.impl.client.AgarthaModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class au {
   private static final SoundEvent HOVER = a("ui_hover");
   private static final SoundEvent TOGGLE_ON = a("ui_toggle_on");
   private static final SoundEvent TOGGLE_OFF = a("ui_toggle_off");
   private static final SoundEvent EXPAND = a("ui_expand");
   private static final SoundEvent COLLAPSE = a("ui_collapse");
   private static final SoundEvent OPEN = a("ui_open");
   private static final SoundEvent CLOSE = a("ui_close");
   private static final SoundEvent CLICK = a("ui_click");
   private static final SoundEvent SLIDER = a("ui_slider");
   private static final long HOVER_GAP_NANOS = 38000000L;
   private static final long SLIDER_GAP_NANOS = 28000000L;
   private static long lastHoverNanos;
   private static long lastSliderNanos;

   private au() {
   }

   private static SoundEvent a(String string) {
      return SoundEvent.of(Identifier.of("agartha", string));
   }

   public static void register() {
      for (SoundEvent var3 : new SoundEvent[]{HOVER, TOGGLE_ON, TOGGLE_OFF, EXPAND, COLLAPSE, OPEN, CLOSE, CLICK, SLIDER}) {
         try {
            Registry.register(Registries.SOUND_EVENT, var3.id(), var3);
         } catch (IllegalStateException var5) {
            // Built-in registries are frozen before client entrypoints on 1.21.11; sounds.json covers playback.
            break;
         }
      }
   }

   public static void hover() {
      long var0 = System.nanoTime();
      if (var0 - lastHoverNanos >= 38000000L) {
         lastHoverNanos = var0;
         a(HOVER, 0.55F, a(0.07F));
      }
   }

   public static void toggle(boolean bl) {
      a(bl ? TOGGLE_ON : TOGGLE_OFF, 1.0F, a(0.02F));
   }

   public static void drawer(boolean bl) {
      a(bl ? EXPAND : COLLAPSE, 0.85F, a(0.03F));
   }

   public static void guiOpen() {
      a(OPEN, 1.0F, 1.0F);
   }

   public static void guiClose() {
      a(CLOSE, 1.0F, 1.0F);
   }

   public static void click() {
      a(CLICK, 0.8F, a(0.04F));
   }

   public static void sliderTick(float f) {
      long var1 = System.nanoTime();
      if (var1 - lastSliderNanos >= 28000000L) {
         lastSliderNanos = var1;
         a(SLIDER, 0.6F, 0.9F + 0.25F * Math.max(0.0F, Math.min(1.0F, f)));
      }
   }

   private static float a(float f) {
      return 1.0F + (ThreadLocalRandom.current().nextFloat() * 2.0F - 1.0F) * f;
   }

   private static void a(SoundEvent soundEvent, float f, float g) {
      AgarthaModule var3 = net.agartha.client.module.e.get(AgarthaModule.class);
      if (var3 != null && var3.uiSounds().get()) {
         float var4 = f * var3.uiSoundVolume().get();
         if (!(var4 <= 0.001F)) {
            MinecraftClient var5 = MinecraftClient.getInstance();
            if (var5.getSoundManager() != null) {
               var5.getSoundManager().play(PositionedSoundInstance.ui(soundEvent, g, Math.min(1.0F, var4)));
            }
         }
      }
   }
}
