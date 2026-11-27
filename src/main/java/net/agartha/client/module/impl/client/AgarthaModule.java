package net.agartha.client.module.impl.client;

import net.agartha.client.AgarthaClientMod;
import net.agartha.client.config.ConfigResetService;
import net.agartha.client.core.CommandHandler;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.EventPriority;
import net.agartha.client.event.impl.ChatSendEvent;
import net.agartha.client.gui.DrawUtils;
import net.agartha.client.gui.Theme;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.KeybindSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.module.setting.StringSetting;
import net.agartha.client.util.net.ChatSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class AgarthaModule extends Module {
   public static final String ANIM_LEFT_TO_RIGHT = "Left to Right";
   public static final String ANIM_RIGHT_TO_LEFT = "Right to Left";
   public static final String ANIM_TOP_TO_BOTTOM = "Top to Bottom";
   public static final String ANIM_BOTTOM_TO_TOP = "Bottom to Top";
   private final EnumSetting sTheme = (EnumSetting)this.addSetting(
      new EnumSetting("Theme", Theme.WHITE.displayName(), new String[]{"White", "Green", "Red", "Blue", "Pink", "Purple"}).alias("Turquoise", "Blue")
   );
   public static final String STYLE_SHARP = "Sharp";
   public static final String STYLE_SMOOTH = "Smooth";
   private final EnumSetting sStyle = (EnumSetting)this.addSetting(new EnumSetting("Style", "Smooth", new String[]{"Smooth", "Sharp"}));
   private final BooleanSetting sThemeFill = (BooleanSetting)this.addSetting(
      new BooleanSetting("Fill With Theme", false)
   );
   private final BooleanSetting sMenuAnimation = (BooleanSetting)this.addSetting(
      new BooleanSetting("Menu Animation", true)
   );
   private final EnumSetting sAnimation = (EnumSetting)this.addSetting(
      new EnumSetting("Animation", "Left to Right", new String[]{"Top to Bottom", "Left to Right", "Right to Left", "Bottom to Top"})
   );
   private final BooleanSetting sMenuBackground = (BooleanSetting)this.addSetting(
      new BooleanSetting("Menu Background", false)
   );
   private final BooleanSetting sMenuParticles = (BooleanSetting)this.addSetting(
      new BooleanSetting("Menu Particles", true)
   );
   private final BooleanSetting sUiSounds = (BooleanSetting)this.addSetting(
      new BooleanSetting("UI Sounds (Experimental)", true)
   );
   private final NumberSetting sUiSoundVolume = (NumberSetting)this.addSetting(new NumberSetting("UI Sound Volume", 0.6F, 0.0F, 1.0F));
   private final BooleanSetting sLockClickGui = (BooleanSetting)this.addSetting(
      new BooleanSetting("Lock Click GUI", false)
   );
   private final NumberSetting sGuiOpacity = (NumberSetting)this.addSetting(new NumberSetting("GUI Opacity", 1.0F, 0.15F, 1.0F));
   private final StringSetting sChatPrefix = (StringSetting)this.addSetting(new StringSetting("Chat Prefix", "--"));
   private final KeybindSetting sClickGuiKey = (KeybindSetting)this.addSetting(new KeybindSetting("Click GUI", 344));

   public AgarthaModule() {
      super("Agartha", "Client identity, theme, and global reset controls.", ModuleCategory.CLIENT);
      this.setEnabled(true);
      this.sAnimation.visibleWhen(this.sMenuAnimation::get);
      this.sUiSoundVolume.visibleWhen(this.sUiSounds::get);
      this.sStyle.onChange(this::applyStyle);
      this.applyStyle();
      this.sThemeFill.onChange(() -> DrawUtils.setThemeFill(this.sThemeFill.get()));
      DrawUtils.setThemeFill(this.sThemeFill.get());
   }

   private void applyStyle() {
      DrawUtils.setStyle(this.sStyle.is("Sharp") ? 0 : 1);
   }

   public BooleanSetting uiSounds() {
      return this.sUiSounds;
   }

   public NumberSetting uiSoundVolume() {
      return this.sUiSoundVolume;
   }

   public BooleanSetting menuBackground() {
      return this.sMenuBackground;
   }

   public BooleanSetting menuParticles() {
      return this.sMenuParticles;
   }

   protected boolean lockEnabledOn() {
      return true;
   }

   public EnumSetting theme() {
      return this.sTheme;
   }

   public BooleanSetting menuAnimation() {
      return this.sMenuAnimation;
   }

   public BooleanSetting lockClickGui() {
      return this.sLockClickGui;
   }

   public KeybindSetting clickGuiKeySetting() {
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

   @EventListener(
      priority = EventPriority.HIGHEST
   )
   public void onChatSend(ChatSendEvent arg) {
      if (CommandHandler.handle(arg.message, this.chatPrefix())) {
         arg.setCancelled(true);
      }
   }

   public EnumSetting animation() {
      return this.sAnimation;
   }

   public void resetClientToFactoryDefaults() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      var1.execute(() -> {
         try {
            ConfigResetService.resetAll(var1);
            if (var1.player != null) {
               ChatSender.send("Client reset to defaults.");
            }
         } catch (Throwable var2) {
            AgarthaClientMod.LOGGER.error("[Agartha] Reset Client failed — mod jar may be corrupt. Close the game, redeploy, restart.", var2);
            if (var1.player != null) {
               ChatSender.send("Reset failed. Close game, redeploy jar, restart.");
            }
         }
      });
   }

   static {
      ConfigResetService.class.getName();
   }
}
