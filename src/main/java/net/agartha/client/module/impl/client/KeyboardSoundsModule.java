package net.agartha.client.module.impl.client;

import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.KeyInputEvent;
import net.agartha.client.event.impl.MouseInputEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.sound.KeyboardSoundPlayer;
import net.agartha.client.sound.SoundPackRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;

@Environment(EnvType.CLIENT)
public final class KeyboardSoundsModule extends Module {
   private final BooleanSetting keyboardSounds = (BooleanSetting)this.addSetting(
      new BooleanSetting("Keyboard Sounds", true)
   );
   private final BooleanSetting mouseSounds = (BooleanSetting)this.addSetting(
      new BooleanSetting("Mouse Sounds", true)
   );
   private final BooleanSetting allKeys = (BooleanSetting)this.addSetting(new BooleanSetting("All Keys", true));
   private final BooleanSetting playInMenus = (BooleanSetting)this.addSetting(
      new BooleanSetting("Play In Menus", false)
   );
   private final EnumSetting keyboardPack = (EnumSetting)this.addSetting(new EnumSetting("Keyboard", SoundPackRegistry.displayName("cherrymx_black_pbt"), packLabels()));
   private final EnumSetting mousePack = (EnumSetting)this.addSetting(new EnumSetting("Mouse", SoundPackRegistry.displayName("cherrymx_blue_abs_2"), packLabels()));
   private final NumberSetting volume = (NumberSetting)this.addSetting(new NumberSetting("Volume", 1.0F, 0.0F, 1.0F));
   private final NumberSetting amplify = (NumberSetting)this.addSetting(new NumberSetting("Amplify", 1.0F, 1.0F, 8.0F));

   public KeyboardSoundsModule() {
      super("Keyboard Sounds", "Keyboard and mouse click sounds while you play.", ModuleCategory.CLIENT);
      this.allKeys.visibleWhen(this.keyboardSounds::get);
      this.keyboardPack.visibleWhen(this.keyboardSounds::get);
      this.mousePack.visibleWhen(this.mouseSounds::get);
   }

   @EventListener
   public void onKey(KeyInputEvent arg) {
      if (this.isEnabled() && this.keyboardSounds.get() && arg.action == 1) {
         if (this.shouldPlayInCurrentContext()) {
            MinecraftClient var2 = MinecraftClient.getInstance();
            if (var2.options != null) {
               KeyInput var3 = new KeyInput(arg.key, arg.scancode, arg.mods);
               if (var2.options.useKey.matchesKey(var3) || var2.options.attackKey.matchesKey(var3)) {
                  this.playSecondary();
                  return;
               }
            }

            if (this.allKeys.get()) {
               this.playPrimary();
            }
         }
      }
   }

   @EventListener
   public void onMouse(MouseInputEvent arg) {
      if (this.isEnabled() && this.mouseSounds.get() && arg.action == 1) {
         if (!KeyboardSoundPlayer.consumeMouseSuppression()) {
            if (this.shouldPlayInCurrentContext()) {
               if (arg.button == 0 || arg.button == 1) {
                  this.playSecondary();
               }
            }
         }
      }
   }

   private boolean shouldPlayInCurrentContext() {
      if (this.playInMenus.get()) {
         return true;
      } else {
         MinecraftClient var1 = MinecraftClient.getInstance();
         return var1.player != null && var1.currentScreen == null;
      }
   }

   private void playPrimary() {
      KeyboardSoundPlayer.playPack(packId(this.keyboardPack.get()), this.volume.get(), Math.round(this.amplify.get()));
   }

   private void playSecondary() {
      KeyboardSoundPlayer.playPack(packId(this.mousePack.get()), this.volume.get(), Math.round(this.amplify.get()));
   }

   private static String[] packLabels() {
      String[] var0 = new String[SoundPackRegistry.PACK_IDS.size()];

      for (int var1 = 0; var1 < var0.length; var1++) {
         var0[var1] = SoundPackRegistry.displayName((String)SoundPackRegistry.PACK_IDS.get(var1));
      }

      return var0;
   }

   private static String packId(String string) {
      for (String var2 : SoundPackRegistry.PACK_IDS) {
         if (SoundPackRegistry.displayName(var2).equals(string)) {
            return var2;
         }
      }

      return "cherrymx_black_pbt";
   }
}
