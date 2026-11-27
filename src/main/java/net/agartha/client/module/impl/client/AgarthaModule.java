package net.agartha.client.module.impl.client;

import net.agartha.client.AgarthaClientMod;
import net.agartha.client.a_pkg.a_o;
import net.agartha.client.d.f;
import net.agartha.client.e.k;
import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.module.a_pkg.g;
import net.agartha.client.module.a_pkg.i;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.module.a_pkg.l;
import net.agartha.client.util.ab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class AgarthaModule extends b {
   public static final String ANIM_LEFT_TO_RIGHT = "Left to Right";
   public static final String ANIM_RIGHT_TO_LEFT = "Right to Left";
   public static final String ANIM_TOP_TO_BOTTOM = "Top to Bottom";
   public static final String ANIM_BOTTOM_TO_TOP = "Bottom to Top";
   private final i sTheme = (i)this.addSetting(
      new i("Theme", k.WHITE.displayName(), new String[]{"White", "Green", "Red", "Blue", "Pink", "Purple"}).alias("Turquoise", "Blue")
   );
   public static final String STYLE_SHARP = "Sharp";
   public static final String STYLE_SMOOTH = "Smooth";
   private final i sStyle = (i)this.addSetting(new i("Style", "Smooth", new String[]{"Smooth", "Sharp"}));
   private final net.agartha.client.module.a_pkg.b sThemeFill = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Fill With Theme", false)
   );
   private final net.agartha.client.module.a_pkg.b sMenuAnimation = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Menu Animation", true)
   );
   private final i sAnimation = (i)this.addSetting(
      new i("Animation", "Left to Right", new String[]{"Top to Bottom", "Left to Right", "Right to Left", "Bottom to Top"})
   );
   private final net.agartha.client.module.a_pkg.b sMenuBackground = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Menu Background", false)
   );
   private final net.agartha.client.module.a_pkg.b sMenuParticles = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Menu Particles", true)
   );
   private final net.agartha.client.module.a_pkg.b sUiSounds = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("UI Sounds (Experimental)", true)
   );
   private final j sUiSoundVolume = (j)this.addSetting(new j("UI Sound Volume", 0.6F, 0.0F, 1.0F));
   private final net.agartha.client.module.a_pkg.b sLockClickGui = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Lock Click GUI", false)
   );
   private final j sGuiOpacity = (j)this.addSetting(new j("GUI Opacity", 1.0F, 0.15F, 1.0F));
   private final l sChatPrefix = (l)this.addSetting(new l("Chat Prefix", "--"));
   private final g sClickGuiKey = (g)this.addSetting(new g("Click GUI", 344));

   public AgarthaModule() {
      super("Agartha", "Client identity, theme, and global reset controls.", c.CLIENT);
      this.setEnabled(true);
      this.sAnimation.visibleWhen(this.sMenuAnimation::get);
      this.sUiSoundVolume.visibleWhen(this.sUiSounds::get);
      this.sStyle.onChange(this::applyStyle);
      this.applyStyle();
      this.sThemeFill.onChange(() -> net.agartha.client.e.c.setThemeFill(this.sThemeFill.get()));
      net.agartha.client.e.c.setThemeFill(this.sThemeFill.get());
   }

   private void applyStyle() {
      net.agartha.client.e.c.setStyle(this.sStyle.is("Sharp") ? 0 : 1);
   }

   public net.agartha.client.module.a_pkg.b uiSounds() {
      return this.sUiSounds;
   }

   public j uiSoundVolume() {
      return this.sUiSoundVolume;
   }

   public net.agartha.client.module.a_pkg.b menuBackground() {
      return this.sMenuBackground;
   }

   public net.agartha.client.module.a_pkg.b menuParticles() {
      return this.sMenuParticles;
   }

   protected boolean lockEnabledOn() {
      return true;
   }

   public i theme() {
      return this.sTheme;
   }

   public net.agartha.client.module.a_pkg.b menuAnimation() {
      return this.sMenuAnimation;
   }

   public net.agartha.client.module.a_pkg.b lockClickGui() {
      return this.sLockClickGui;
   }

   public g clickGuiKeySetting() {
      return this.sClickGuiKey;
   }

   public int clickGuiKey() {
      return this.sClickGuiKey.value();
   }

   public float guiOpacity() {
      float var1 = this.sGuiOpacity.get();
      return Float.isNaN(var1) ? 1.0F : Math.max(0.15F, Math.min(1.0F, var1));
   }

   public String chatPrefix() {
      String var1 = this.sChatPrefix.get();
      if (var1 == null) {
         return "--";
      } else {
         var1 = var1.trim();
         if (!var1.isEmpty() && var1.indexOf(32) < 0 && var1.indexOf(10) < 0) {
            if (var1.length() > 16) {
               var1 = var1.substring(0, 16);
            }

            return var1;
         } else {
            return "--";
         }
      }
   }

   @f(
      priority = net.agartha.client.d.i.HIGHEST
   )
   public void onChatSend(net.agartha.client.d.a_pkg.b arg) {
      if (a_o.handle(arg.message, this.chatPrefix())) {
         arg.setCancelled(true);
      }
   }

   public i animation() {
      return this.sAnimation;
   }

   public void resetClientToFactoryDefaults() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      var1.execute(() -> {
         try {
            net.agartha.client.c.a.resetAll(var1);
            if (var1.player != null) {
               ab.send("Client reset to defaults.");
            }
         } catch (Throwable var2) {
            AgarthaClientMod.LOGGER.error("[Agartha] Reset Client failed — mod jar may be corrupt. Close the game, redeploy, restart.", var2);
            if (var1.player != null) {
               ab.send("Reset failed. Close game, redeploy jar, restart.");
            }
         }
      });
   }

   static {
      net.agartha.client.c.a.class.getName();
   }
}
