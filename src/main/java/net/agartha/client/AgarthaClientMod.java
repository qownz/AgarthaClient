package net.agartha.client;

import net.agartha.client.config.ConfigMigrator;
import net.agartha.client.config.ModuleConfigManager;
import net.agartha.client.event.EventBus;
import net.agartha.client.event.impl.PacketReceiveNettyEvent;
import net.agartha.client.gui.BellTexture;
import net.agartha.client.gui.CanSpriteCache;
import net.agartha.client.gui.ClawTexture;
import net.agartha.client.gui.ClickGuiScreen;
import net.agartha.client.gui.EffectIconCache;
import net.agartha.client.gui.Hud;
import net.agartha.client.gui.HudLayout;
import net.agartha.client.gui.RingRenderer;
import net.agartha.client.gui.UiSounds;
import net.agartha.client.input.Keybind;
import net.agartha.client.integration.spotify.SpotifyArtTexture;
import net.agartha.client.macro.MacroManager;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.client.AgarthaModule;
import net.agartha.client.render.imgui.ImGuiManager;
import net.agartha.client.util.net.ServerTicker;
import net.agartha.client.util.time.TpsMeter;
import net.agartha.client.util.world.BlockEntityTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class AgarthaClientMod implements ClientModInitializer {
   public static final String BRAND_NAME = "Agartha";
   public static final String BRAND_TEAM = "Agartha Team";
   public static final Logger LOGGER = LoggerFactory.getLogger("Agartha");
   public static final Identifier SPAWNER_NOTIFY_SOUND_ID = Identifier.of("agartha", "spawner_notify");
   public static final SoundEvent SPAWNER_NOTIFY_SOUND = SoundEvent.of(SPAWNER_NOTIFY_SOUND_ID);
   public static final Identifier NOTIFY_POP_1_SOUND_ID = Identifier.of("agartha", "notify_pop_1");
   public static final SoundEvent NOTIFY_POP_1_SOUND = SoundEvent.of(NOTIFY_POP_1_SOUND_ID);
   public static final Identifier NOTIFY_POP_2_SOUND_ID = Identifier.of("agartha", "notify_pop_2");
   public static final SoundEvent NOTIFY_POP_2_SOUND = SoundEvent.of(NOTIFY_POP_2_SOUND_ID);
   public static final Category KEY_CATEGORY = Category.create(Identifier.of("agartha", "controls"));
   private static KeyBinding openGuiKey;
   private static int syncedMenuKey = Integer.MIN_VALUE;
   private final boolean[] keybindLastPressed = new boolean[1050];
   private boolean openGuiWasDown;
   private static final Object LEAK_TRACKER_OWNER = new Object();

   private static void toggleClickGui(MinecraftClient minecraftClient) {
      if (minecraftClient.currentScreen instanceof ClickGuiScreen var1) {
         var1.requestClose();
      } else {
         minecraftClient.setScreen(new ClickGuiScreen(minecraftClient.currentScreen));
      }
   }

   private static boolean isMenuKeyDown(MinecraftClient minecraftClient, int i) {
      if (i >= 0 && minecraftClient.getWindow() != null) {
         long var2 = minecraftClient.getWindow().getHandle();
         return i >= 1000 ? GLFW.glfwGetMouseButton(var2, i - 1000) == 1 : GLFW.glfwGetKey(var2, i) == 1;
      } else {
         return false;
      }
   }

   private static void syncOpenGuiKey(int i) {
      if (openGuiKey != null && i != syncedMenuKey) {
         Key var1 = i < 0 ? InputUtil.UNKNOWN_KEY : (i >= 1000 ? Type.MOUSE.createFromCode(i - 1000) : Type.KEYSYM.createFromCode(i));
         openGuiKey.setBoundKey(var1);
         KeyBinding.updateKeysByCode();
         syncedMenuKey = i;
      }
   }

   public void onInitializeClient() {
      ConfigMigrator.run();
      LOGGER.info("[Agartha] Initialising");
      try {
         Registry.register(Registries.SOUND_EVENT, SPAWNER_NOTIFY_SOUND_ID, SPAWNER_NOTIFY_SOUND);
         Registry.register(Registries.SOUND_EVENT, NOTIFY_POP_1_SOUND_ID, NOTIFY_POP_1_SOUND);
         Registry.register(Registries.SOUND_EVENT, NOTIFY_POP_2_SOUND_ID, NOTIFY_POP_2_SOUND);
      } catch (IllegalStateException var0) {
         LOGGER.debug("[Agartha] Sound registry already frozen; using sounds.json entries instead", var0);
      }

      UiSounds.register();
      ImGuiManager.getInstance().setHudRenderer(() -> {
         if (!Hud.foreignMenuOpen()) {
            Hud.getInstance().render();
            RingRenderer.render();
         }
      }, () -> !Hud.foreignMenuOpen() && (Hud.getInstance().shouldRenderFrame() || RingRenderer.hasWork()));
      ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         public Identifier getFabricId() {
            return Identifier.of("agartha", "hud_logo");
         }

         public void reload(ResourceManager resourceManager) {
            BellTexture.getInstance().invalidate();
            SpotifyArtTexture.getInstance().invalidate();
            CanSpriteCache.invalidateAll();
            ClawTexture.getInstance().invalidate();
            EffectIconCache.invalidate();
         }
      });
      TpsMeter.install();
      PacketReceiveNettyEvent.TYPE.listen(LEAK_TRACKER_OWNER, arg -> BlockEntityTracker.ingest(arg.packet));

      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)minecraftClient -> {
         HudLayout.getInstance().save();
         ModuleConfigManager.getInstance().save();
         MacroManager.getInstance().save();
      });
      openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.agartha.open_gui", Type.KEYSYM, 344, KEY_CATEGORY));
      ModuleConfigManager.getInstance().load();

      for (Module var2 : ModuleManager.all()) {
         EventBus.INSTANCE.prepare(var2);
      }

      ClientTickEvents.END_CLIENT_TICK
         .register(
            (EndTick)minecraftClient -> {
               ModuleConfigManager.getInstance().applyPendingEnabled();
               ServerTicker.tick(minecraftClient);
               AgarthaModule var2x = ModuleManager.get(AgarthaModule.class);
               int var3 = var2x == null ? 344 : var2x.clickGuiKey();
               syncOpenGuiKey(var3);
               boolean var4 = isMenuKeyDown(minecraftClient, var3);
               boolean var5 = minecraftClient.currentScreen instanceof ClickGuiScreen;
               if (minecraftClient.world != null && minecraftClient.currentScreen != null && !var5) {
                  openGuiKey.wasPressed();
               } else if (openGuiKey.wasPressed() || var4 && !this.openGuiWasDown) {
                  toggleClickGui(minecraftClient);
               }

               this.openGuiWasDown = var4;
               if (!minecraftClient.isWindowFocused()) {
                  this.releaseHoldModulesOnFocusLoss();
               } else if (minecraftClient.currentScreen == null) {
                  for (Module var7 : ModuleManager.keyboundModules()) {
                     int var8 = var7.keybind();
                     if (var8 > 0
                        && var8 < this.keybindLastPressed.length
                        && !Keybind.isShiftKey(var8)
                        && !Keybind.isClickButton(var8)) {
                        boolean var9 = var8 >= 1000
                           ? GLFW.glfwGetMouseButton(minecraftClient.getWindow().getHandle(), var8 - 1000) == 1
                           : InputUtil.isKeyPressed(minecraftClient.getWindow(), var8);
                        boolean var10 = this.keybindLastPressed[var8];
                        if (var7.keybindTriggersAction()) {
                           if (var9 && !var10 && var7.isEnabled()) {
                              var7.onKeybindAction();
                           }
                        } else if (var7.isHoldMode()) {
                           if (var9 && !var10) {
                              var7.setEnabled(true);
                           } else if (!var9 && var10) {
                              var7.setEnabled(false);
                           }
                        } else if (var9 && !var10) {
                           var7.toggle();
                        }

                        this.keybindLastPressed[var8] = var9;
                     }
                  }
               }
            }
         );
   }

   private void releaseHoldModulesOnFocusLoss() {
      for (Module var2 : ModuleManager.keyboundModules()) {
         if (var2.isHoldMode() && var2.isEnabled()) {
            int var3 = var2.keybind();
            if (var3 > 0 && var3 < this.keybindLastPressed.length) {
               this.keybindLastPressed[var3] = true;
            }

            var2.setEnabled(false);
         }
      }
   }
}
