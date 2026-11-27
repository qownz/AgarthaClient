package net.agartha.client.module.impl.client;

import net.agartha.client.d.f;
import net.agartha.client.d.a_pkg.g;
import net.agartha.client.i.a;
import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.module.a_pkg.i;
import net.agartha.client.module.a_pkg.j;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;

@Environment(EnvType.CLIENT)
public final class KeyboardSoundsModule extends b {
   private final net.agartha.client.module.a_pkg.b keyboardSounds = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Keyboard Sounds", true)
   );
   private final net.agartha.client.module.a_pkg.b mouseSounds = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Mouse Sounds", true)
   );
   private final net.agartha.client.module.a_pkg.b allKeys = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("All Keys", true));
   private final net.agartha.client.module.a_pkg.b playInMenus = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Play In Menus", false)
   );
   private final i keyboardPack = (i)this.addSetting(new i("Keyboard", a.displayName("cherrymx_black_pbt"), packLabels()));
   private final i mousePack = (i)this.addSetting(new i("Mouse", a.displayName("cherrymx_blue_abs_2"), packLabels()));
   private final j volume = (j)this.addSetting(new j("Volume", 1.0F, 0.0F, 1.0F));
   private final j amplify = (j)this.addSetting(new j("Amplify", 1.0F, 1.0F, 8.0F));

   public KeyboardSoundsModule() {
      super("Keyboard Sounds", "Keyboard and mouse click sounds while you play.", c.CLIENT);
      this.allKeys.visibleWhen(this.keyboardSounds::get);
      this.keyboardPack.visibleWhen(this.keyboardSounds::get);
      this.mousePack.visibleWhen(this.mouseSounds::get);
   }

   @f
   public void onKey(net.agartha.client.d.a_pkg.f arg) {
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

   @f
   public void onMouse(g arg) {
      if (this.isEnabled() && this.mouseSounds.get() && arg.action == 1) {
         if (!net.agartha.client.i.b.consumeMouseSuppression()) {
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
      net.agartha.client.i.b.playPack(packId(this.keyboardPack.get()), this.volume.get(), Math.round(this.amplify.get()));
   }

   private void playSecondary() {
      net.agartha.client.i.b.playPack(packId(this.mousePack.get()), this.volume.get(), Math.round(this.amplify.get()));
   }

   private static String[] packLabels() {
      String[] var0 = new String[a.PACK_IDS.size()];

      for (int var1 = 0; var1 < var0.length; var1++) {
         var0[var1] = a.displayName((String)a.PACK_IDS.get(var1));
      }

      return var0;
   }

   private static String packId(String string) {
      for (String var2 : a.PACK_IDS) {
         if (a.displayName(var2).equals(string)) {
            return var2;
         }
      }

      return "cherrymx_black_pbt";
   }
}
