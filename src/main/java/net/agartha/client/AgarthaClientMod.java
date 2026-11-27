package net.agartha.client;

import net.agartha.client.c.e;
import net.agartha.client.d.a_pkg.j;
import net.agartha.client.e.ae;
import net.agartha.client.e.ag;
import net.agartha.client.e.ah;
import net.agartha.client.e.au;
import net.agartha.client.e.o;
import net.agartha.client.e.t;
import net.agartha.client.e.v;
import net.agartha.client.e.x;
import net.agartha.client.module.impl.client.AgarthaModule;
import net.agartha.client.util.d;
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
      if (minecraftClient.currentScreen instanceof x var1) {
         var1.requestClose();
      } else {
         minecraftClient.setScreen(new x(minecraftClient.currentScreen));
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
      if (!b.isSupported()) {
         LOGGER.error(
            "[Agartha] Unsupported platform: {}. Agartha is a Windows desktop client — its interface and input bridges have no build for this system, so nothing will load.",
            b.describe()
         );
      } else {
         net.agartha.client.c.b.run();
         LOGGER.info("[Agartha] Initialising");
         try {
            Registry.register(Registries.SOUND_EVENT, SPAWNER_NOTIFY_SOUND_ID, SPAWNER_NOTIFY_SOUND);
            Registry.register(Registries.SOUND_EVENT, NOTIFY_POP_1_SOUND_ID, NOTIFY_POP_1_SOUND);
            Registry.register(Registries.SOUND_EVENT, NOTIFY_POP_2_SOUND_ID, NOTIFY_POP_2_SOUND);
         } catch (IllegalStateException var0) {
            LOGGER.debug("[Agartha] Sound registry already frozen; using sounds.json entries instead", var0);
         }

         au.register();
         net.agartha.client.f.a.getInstance().setHudRenderer(() -> {
            if (!v.foreignMenuOpen()) {
               v.getInstance().render();
               ae.render();
            }
         }, () -> !v.foreignMenuOpen() && (v.getInstance().shouldRenderFrame() || ae.hasWork()));
         ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            public Identifier getFabricId() {
               return Identifier.of("agartha", "hud_logo");
            }

            public void reload(ResourceManager resourceManager) {
               ah.getInstance().invalidate();
               net.agartha.client.n.a_o.getInstance().invalidate();
               ag.invalidateAll();
               net.agartha.client.e.b.getInstance().invalidate();
               o.invalidate();
            }
         });
         net.agartha.client.util.v.install();
         j.TYPE.listen(LEAK_TRACKER_OWNER, arg -> d.ingest(arg.packet));

         ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)minecraftClient -> {
            t.getInstance().save();
            e.getInstance().save();
            net.agartha.client.j.d.getInstance().save();
         });
         openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.agartha.open_gui", Type.KEYSYM, 344, KEY_CATEGORY));
         e.getInstance().load();

         for (net.agartha.client.module.b var2 : net.agartha.client.module.e.all()) {
            net.agartha.client.d.d.INSTANCE.prepare(var2);
         }

         ClientTickEvents.END_CLIENT_TICK
            .register(
               (EndTick)minecraftClient -> {
                  e.getInstance().applyPendingEnabled();
                  net.agartha.client.util.b.tick(minecraftClient);
                  AgarthaModule var2x = net.agartha.client.module.e.get(AgarthaModule.class);
                  int var3 = var2x == null ? 344 : var2x.clickGuiKey();
                  syncOpenGuiKey(var3);
                  boolean var4 = isMenuKeyDown(minecraftClient, var3);
                  boolean var5 = minecraftClient.currentScreen instanceof x;
                  if (minecraftClient.world != null && minecraftClient.currentScreen != null && !var5) {
                     openGuiKey.wasPressed();
                  } else if (openGuiKey.wasPressed() || var4 && !this.openGuiWasDown) {
                     toggleClickGui(minecraftClient);
                  }

                  this.openGuiWasDown = var4;
                  if (!minecraftClient.isWindowFocused()) {
                     this.releaseHoldModulesOnFocusLoss();
                  } else if (minecraftClient.currentScreen == null) {
                     for (net.agartha.client.module.b var7 : net.agartha.client.module.e.keyboundModules()) {
                        int var8 = var7.keybind();
                        if (var8 > 0
                           && var8 < this.keybindLastPressed.length
                           && !net.agartha.client.j.e.isShiftKey(var8)
                           && !net.agartha.client.j.e.isClickButton(var8)) {
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
   }

   private void releaseHoldModulesOnFocusLoss() {
      for (net.agartha.client.module.b var2 : net.agartha.client.module.e.keyboundModules()) {
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
