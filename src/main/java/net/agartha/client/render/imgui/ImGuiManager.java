package net.agartha.client.render.imgui;

import com.mojang.blaze3d.opengl.GlStateManager;
import imgui.ImFont;
import imgui.ImFontConfig;
import imgui.ImFontGlyphRangesBuilder;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.gui.AvatarCache;
import net.agartha.client.gui.BellTexture;
import net.agartha.client.gui.CanSpriteCache;
import net.agartha.client.gui.ClawTexture;
import net.agartha.client.gui.UiScale;
import net.agartha.client.integration.spotify.SpotifyArtTexture;
import net.agartha.client.platform.PlatformSupport;
import net.agartha.client.util.perf.Profiler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public final class ImGuiManager {
   private static final ImGuiManager INSTANCE = new ImGuiManager();
   private static final String CATEGORY_FONT_RESOURCE = "/assets/agartha/fonts/geist-semibold.ttf";
   private static final String MODULE_FONT_RESOURCE = "/assets/agartha/fonts/geist-medium.ttf";
   private static final String WATERMARK_FONT_RESOURCE = "/assets/agartha/fonts/geist-semibold.ttf";
   private static final String HUD_VALUE_FONT_RESOURCE = "/assets/agartha/fonts/rajdhani-bold.ttf";
   private static final String HUD_TEXT_FONT_RESOURCE = "/assets/agartha/fonts/plusjakartasans-semibold.ttf";
   private static final String HUD_LABEL_FONT_RESOURCE = "/assets/agartha/fonts/plusjakartasans-medium.ttf";
   private static final String MINECRAFT_FONT_RESOURCE = "/assets/agartha/fonts/Monocraft.ttf";
   private static final float MINECRAFT_FONT_SIZE = 20.0F;
   private static final String EXTENDED_LATIN_GLYPHS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789ÇĞİÖŞÜçğıöşüÂâÊêÎîÔôÛûÀÁÄÈÉËÌÍÏÒÓÖÙÚÜÝàáäèéëìíïòóöùúüýÿ";
   private final ImGuiImplGlfw glfw = new ImGuiImplGlfw();
   private final ImGuiImplGl3 gl3 = new ImGuiImplGl3();
   private final int[] framebufferBinding = new int[1];
   private boolean initialized;
   private Runnable activeRenderer;
   private Runnable hudRenderer;
   private BooleanSupplier hudVisibility = () -> true;
   private Runnable menuOverlayRenderer;
   private int frameCount;
   private int fontGeneration;
   private Runnable farewellRenderer;
   private long farewellUntilNanos;
   private float clickGuiScale = 1.0F;
   private float passScale = 1.0F;
   private ImFont categoryFont;
   private ImFont moduleFont;
   private ImFont watermarkFont;
   private ImFont hudValueFont;
   private ImFont hudWatermarkFont;
   private ImFont hudTextFont;
   private ImFont hudLabelFont;
   private ImFont minecraftFont;
   private short[] glyphRanges;
   private boolean textFieldHot;
   private boolean inputStale = true;
   private static final float[] HUD_LADDER = new float[]{
      8.0F, 9.0F, 10.0F, 11.0F, 12.0F, 13.0F, 14.0F, 15.0F, 16.0F, 17.0F, 18.0F, 19.0F, 20.0F, 22.0F, 24.0F, 26.0F, 28.0F, 30.0F, 32.0F, 36.0F, 42.0F, 48.0F
   };
   private static final float LARGE_SIZE = 34.0F;
   private final Map<ImFont, TreeMap<Float, ImFont>> ladders = new IdentityHashMap<>();
   private static Boolean imguiAvailable;
   private short[] largeGlyphRanges;
   private short[] ladderGlyphRanges;
   private final Map<String, byte[]> fontFiles = new HashMap<>();

   private ImGuiManager() {
   }

   public static ImGuiManager getInstance() {
      return INSTANCE;
   }

   public ImFont categoryFont() {
      return this.categoryFont;
   }

   public ImFont moduleFont() {
      return this.moduleFont;
   }

   public ImFont watermarkFont() {
      return this.watermarkFont;
   }

   public ImFont hudValueFont() {
      return this.hudValueFont;
   }

   public ImFont hudWatermarkFont() {
      return this.hudWatermarkFont;
   }

   public ImFont hudTextFont() {
      return this.hudTextFont;
   }

   public ImFont hudLabelFont() {
      return this.hudLabelFont;
   }

   public ImFont sized(ImFont imFont, float f) {
      if (imFont == null) {
         return null;
      } else {
         TreeMap<Float, ImFont> var3 = this.ladders.get(imFont);
         return var3 == null ? imFont : a(var3, f).getValue();
      }
   }

   public float snappedSize(ImFont imFont, float f) {
      if (imFont == null) {
         return f;
      } else {
         TreeMap<Float, ImFont> var3 = this.ladders.get(imFont);
         return var3 == null ? f : a(var3, f).getKey();
      }
   }

   private static Entry<Float, ImFont> a(TreeMap<Float, ImFont> treeMap, float f) {
      Entry<Float, ImFont> var2 = treeMap.floorEntry(f);
      Entry<Float, ImFont> var3 = treeMap.ceilingEntry(f);
      if (var2 == null) {
         return var3 != null ? var3 : treeMap.firstEntry();
      } else if (var3 == null) {
         return var2;
      } else {
         return f - var2.getKey() <= var3.getKey() - f ? var2 : var3;
      }
   }

   public int fontGeneration() {
      return this.fontGeneration;
   }

   public ImFont minecraftFont() {
      return this.minecraftFont;
   }

   public void setMenuOverlayRenderer(Runnable runnable) {
      this.menuOverlayRenderer = runnable;
   }

   public void armTextInput() {
      if (ImGui.isItemActive()) {
         this.textFieldHot = true;
      }
   }

   public void activate(Runnable runnable) {
      this.activeRenderer = runnable;
      this.inputStale = true;
   }

   public void latchClickGuiScale(float f) {
      if (Float.isNaN(f) || f < 0.5F || f > 1.5F) {
         f = 1.0F;
      }

      this.clickGuiScale = f;
   }

   public void playFarewell(Runnable runnable, float f) {
      this.farewellRenderer = runnable;
      this.farewellUntilNanos = System.nanoTime() + (long)(f * 1.0E9F);
   }

   public void deactivate() {
      this.activeRenderer = null;
      this.textFieldHot = false;
      if (this.initialized) {
         ImGuiIO var1 = ImGui.getIO();
         var1.removeConfigFlags(1);
         var1.addConfigFlags(64);
         var1.clearInputKeys();
         var1.setWantCaptureKeyboard(false);
         var1.setWantCaptureMouse(false);
      }
   }

   public int frameCount() {
      return this.frameCount;
   }

   public void setHudRenderer(Runnable runnable) {
      this.setHudRenderer(runnable, () -> true);
   }

   public void setHudRenderer(Runnable runnable, BooleanSupplier booleanSupplier) {
      this.hudRenderer = runnable;
      this.hudVisibility = booleanSupplier == null ? () -> true : booleanSupplier;
   }

   public void renderIfActive() {
      if (this.hudRenderer == null && this.activeRenderer == null && this.menuOverlayRenderer == null && this.farewellRenderer == null) {
         this.inputStale = true;
      } else {
         MinecraftClient var1 = MinecraftClient.getInstance();
         if (this.farewellRenderer != null && (System.nanoTime() >= this.farewellUntilNanos || this.activeRenderer != null)) {
            this.farewellRenderer = null;
         }

         boolean var2 = this.hudRenderer != null && this.hudVisibility.getAsBoolean() && b(var1);
         boolean var3 = this.activeRenderer != null;
         boolean var4 = this.menuOverlayRenderer != null && a(var1);
         boolean var5 = this.farewellRenderer != null;
         if (!var2 && !var3 && !var4 && !var5) {
            this.inputStale = true;
         } else if (a()) {
            Runnable var6 = this.farewellRenderer;
            boolean var7 = var3 || var5;
            this.passScale = var7 ? this.clickGuiScale : 1.0F;
            this.a((Runnable)(() -> {
               if (var2) {
                  this.hudRenderer.run();
               }

               if (var4) {
                  this.menuOverlayRenderer.run();
               }

               if (var3) {
                  this.activeRenderer.run();
               }

               if (var6 != null) {
                  var6.run();
               }
            }));
            this.passScale = 1.0F;
         }
      }
   }

   private static boolean a(MinecraftClient minecraftClient) {
      return minecraftClient.world == null && minecraftClient.currentScreen instanceof TitleScreen;
   }

   private static boolean b(MinecraftClient minecraftClient) {
      if (minecraftClient.world == null) {
         return false;
      } else {
         return minecraftClient.currentScreen instanceof GameMenuScreen ? false : !minecraftClient.getDebugHud().shouldShowDebugHud();
      }
   }

   private void a(Runnable runnable) {
      long var2 = Profiler.begin();
      long var4 = Profiler.allocatedBytes();
      MinecraftClient var6 = MinecraftClient.getInstance();
      if (!var6.isWindowFocused()) {
         this.inputStale = true;
      } else {
         int var7 = var6.getWindow().getFramebufferWidth();
         int var8 = var6.getWindow().getFramebufferHeight();
         if (var7 > 0 && var8 > 0) {
            this.b();
            ImGuiIO var9 = ImGui.getIO();
            if (this.inputStale) {
               this.inputStale = false;
               var9.clearEventsQueue();
               var9.clearInputMouse();
               var9.clearInputKeys();
            }

            var9.removeConfigFlags(1);
            var9.removeConfigFlags(2);
            boolean var10 = this.activeRenderer != null && this.textFieldHot;
            this.textFieldHot = false;
            if (var10) {
               var9.removeConfigFlags(64);
            } else {
               var9.addConfigFlags(64);
            }

            long var11 = Profiler.begin();
            GL11.glGetIntegerv(36006, this.framebufferBinding);
            Profiler.record("imgui.state-query", var11);

            try {
               GlStateManager._glBindFramebuffer(36160, this.framebufferBinding[0]);
               GL11.glViewport(0, 0, var7, var8);
               long var13 = Profiler.begin();
               this.glfw.newFrame();
               if (!var10) {
                  ImGui.getIO().clearInputKeys();
               }

               this.c();
               this.gl3.newFrame();
               ImGui.newFrame();
               this.frameCount++;
               Profiler.record("imgui.frame-prepare", var13);
               long var15 = Profiler.begin();
               runnable.run();
               Profiler.record("imgui.renderer-callback", var15);
               long var17 = Profiler.begin();
               GlStateManager._glBindFramebuffer(36160, this.framebufferBinding[0]);
               GL11.glViewport(0, 0, var7, var8);
               ImGui.render();
               this.gl3.renderDrawData(ImGui.getDrawData());
               Profiler.record("imgui.draw-data", var17);
            } finally {
               GlStateManager._glBindFramebuffer(36160, this.framebufferBinding[0]);
               GL11.glViewport(0, 0, var7, var8);
               GlStateManager._disableScissorTest();
               GlStateManager._activeTexture(33984);
               GlStateManager._bindTexture(0);
               Profiler.record("imgui.pass", var2);
               Profiler.recordAllocatedBytes("imgui.pass", var4);
            }
         } else {
            this.inputStale = true;
         }
      }
   }

   public void dispose() {
      if (this.initialized) {
         CanSpriteCache.invalidateAll();
         ClawTexture.getInstance().invalidate();
         BellTexture.getInstance().invalidate();
         SpotifyArtTexture.getInstance().invalidate();
         AvatarCache.getInstance().invalidate();
         this.gl3.shutdown();
         this.glfw.shutdown();
         ImGui.destroyContext();
         this.categoryFont = null;
         this.moduleFont = null;
         this.watermarkFont = null;
         this.hudValueFont = null;
         this.hudWatermarkFont = null;
         this.hudTextFont = null;
         this.hudLabelFont = null;
         this.minecraftFont = null;
         this.ladders.clear();
         this.initialized = false;
      }
   }

   private static boolean a() {
      if (imguiAvailable == null) {
         try {
            ImGui.getVersion();
            imguiAvailable = Boolean.TRUE;
         } catch (Throwable var1) {
            imguiAvailable = Boolean.FALSE;
            AgarthaClientMod.LOGGER.error("[Agartha] No ImGui native for this system ({}); interface disabled.", PlatformSupport.describe(), var1);
         }
      }

      return imguiAvailable;
   }

   private void b() {
      if (!this.initialized) {
         long var1 = MinecraftClient.getInstance().getWindow().getHandle();
         ImGui.createContext();
         ImGuiIO var3 = ImGui.getIO();
         var3.setIniFilename(null);
         var3.removeConfigFlags(1);
         var3.removeConfigFlags(2);
         var3.addConfigFlags(64);
         ImGui.styleColorsDark();
         this.a(var3);
         this.glfw.init(var1, true);
         this.gl3.init(null);
         var3.getFonts().build();
         this.gl3.createFontsTexture();
         this.initialized = true;
      }
   }

   private void c() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      int var2 = Math.max(1, var1.getWindow().getFramebufferWidth());
      int var3 = Math.max(1, var1.getWindow().getFramebufferHeight());
      float var4 = UiScale.uiScale(var2, var3) * this.passScale;
      ImGuiIO var5 = ImGui.getIO();
      if (this.passScale == 1.0F && var4 <= 1.001F) {
         var5.setDisplaySize(var2, var3);
         var5.setDisplayFramebufferScale(1.0F, 1.0F);
      } else {
         float var6 = var2 / var4;
         float var7 = var3 / var4;
         var5.setDisplaySize(var6, var7);
         var5.setDisplayFramebufferScale(var4, var4);
         long var8 = var1.getWindow().getHandle();
         int[] var10 = new int[1];
         int[] var11 = new int[1];
         double[] var12 = new double[1];
         double[] var13 = new double[1];
         GLFW.glfwGetWindowSize(var8, var10, var11);
         GLFW.glfwGetCursorPos(var8, var12, var13);
         float var14 = Math.max(1, var10[0]);
         float var15 = Math.max(1, var11[0]);
         var5.addMousePosEvent((float)var12[0] * var6 / var14, (float)var13[0] * var7 / var15);
      }
   }

   private void a(ImGuiIO imGuiIO) {
      this.glyphRanges = b(imGuiIO);
      this.largeGlyphRanges = e();
      this.ladderGlyphRanges = d();

      for (String var5 : new String[]{
         "/assets/agartha/fonts/geist-medium.ttf",
         "/assets/agartha/fonts/geist-semibold.ttf",
         "/assets/agartha/fonts/geist-semibold.ttf",
         "/assets/agartha/fonts/rajdhani-bold.ttf",
         "/assets/agartha/fonts/plusjakartasans-semibold.ttf",
         "/assets/agartha/fonts/plusjakartasans-medium.ttf",
         "/assets/agartha/fonts/Monocraft.ttf"
      }) {
         this.a(var5);
      }

      this.fontGeneration++;
      this.moduleFont = this.a(imGuiIO, "/assets/agartha/fonts/geist-medium.ttf", 14.0F, this.glyphRanges);
      this.categoryFont = this.a(imGuiIO, "/assets/agartha/fonts/geist-semibold.ttf", 16.0F, this.glyphRanges);
      this.watermarkFont = this.a(imGuiIO, "/assets/agartha/fonts/geist-semibold.ttf", 32.0F, this.glyphRanges);
      String var6 = "/assets/agartha/fonts/rajdhani-bold.ttf";
      String var7 = "/assets/agartha/fonts/plusjakartasans-semibold.ttf";
      String var8 = "/assets/agartha/fonts/plusjakartasans-medium.ttf";
      this.hudValueFont = this.a(imGuiIO, var6, 22.0F, this.glyphRanges);
      this.hudWatermarkFont = this.a(imGuiIO, var6, 36.0F, this.glyphRanges);
      this.hudTextFont = this.a(imGuiIO, var7, 15.0F, this.glyphRanges);
      this.hudLabelFont = this.a(imGuiIO, var8, 12.0F, this.glyphRanges);
      this.minecraftFont = this.a(imGuiIO, "/assets/agartha/fonts/Monocraft.ttf", 20.0F, this.glyphRanges);
      this.ladders.clear();
      this.a(imGuiIO, this.hudValueFont, var6, 22.0F);
      this.a(imGuiIO, this.hudTextFont, var7, 15.0F);
      this.a(imGuiIO, this.hudLabelFont, var8, 12.0F);
      if (this.moduleFont == null && this.categoryFont == null && this.watermarkFont == null) {
         imGuiIO.getFonts().addFontDefault();
      } else {
         if (this.moduleFont != null) {
            imGuiIO.setFontDefault(this.moduleFont);
         }
      }
   }

   private static short[] d() {
      ImFontGlyphRangesBuilder var0 = new ImFontGlyphRangesBuilder();
      var0.addRanges(new short[]{32, 383, 0});
      var0.addText("€₺•…–—’“”");
      return var0.buildRanges();
   }

   private static short[] e() {
      ImFontGlyphRangesBuilder var0 = new ImFontGlyphRangesBuilder();
      var0.addRanges(new short[]{32, 255, 0});
      var0.addText("ĞğİıŞş");
      return var0.buildRanges();
   }

   private static short[] b(ImGuiIO imGuiIO) {
      ImFontGlyphRangesBuilder var1 = new ImFontGlyphRangesBuilder();
      var1.addRanges(imGuiIO.getFonts().getGlyphRangesDefault());
      var1.addRanges(imGuiIO.getFonts().getGlyphRangesVietnamese());
      var1.addText("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789ÇĞİÖŞÜçğıöşüÂâÊêÎîÔôÛûÀÁÄÈÉËÌÍÏÒÓÖÙÚÜÝàáäèéëìíïòóöùúüýÿ");
      return var1.buildRanges();
   }

   private void a(ImGuiIO imGuiIO, ImFont imFont, String string, float f) {
      if (imFont != null) {
         byte[] var5 = this.a(string);
         if (var5 != null) {
            TreeMap var6 = new TreeMap();
            var6.put(f, imFont);

            for (float var10 : HUD_LADDER) {
               if (!(Math.abs(var10 - f) < 0.5F)) {
                  ImFontConfig var11 = new ImFontConfig();

                  try {
                     var11.setOversampleH(var10 <= 14.0F ? 2 : 1);
                     var11.setOversampleV(1);
                     var11.setPixelSnapH(true);
                     var11.setRasterizerMultiply(var10 <= 11.0F ? 1.25F : 1.0F);
                     short[] var12 = var10 >= 34.0F ? this.largeGlyphRanges : this.ladderGlyphRanges;
                     ImFont var13 = imGuiIO.getFonts().addFontFromMemoryTTF(var5, var10, var11, var12);
                     if (var13 != null) {
                        var6.put(var10, var13);
                     }
                  } finally {
                     var11.destroy();
                  }
               }
            }

            this.ladders.put(imFont, var6);
         }
      }
   }

   private ImFont a(ImGuiIO imGuiIO, String string, float f, short[] ss) {
      byte[] var5 = this.a(string);
      if (var5 == null) {
         return null;
      } else {
         ImFontConfig var6 = new ImFontConfig();

         Object var8;
         try {
            return imGuiIO.getFonts().addFontFromMemoryTTF(var5, f, var6, ss);
         } catch (RuntimeException var12) {
            System.err.println("[Agartha] Failed to load font " + string + ": " + var12.getMessage());
            var8 = null;
         } finally {
            var6.destroy();
         }

         return (ImFont)var8;
      }
   }

   private byte[] a(String string) {
      byte[] var2 = this.fontFiles.get(string);
      if (var2 != null) {
         return var2;
      } else {
         try {
            byte[] var5;
            try (InputStream var3 = ImGuiManager.class.getResourceAsStream(string)) {
               if (var3 == null) {
                  System.err.println("[Agartha] Font resource not found: " + string);
                  return null;
               }

               byte[] var4 = var3.readAllBytes();
               this.fontFiles.put(string, var4);
               var5 = var4;
            }

            return var5;
         } catch (IOException var8) {
            System.err.println("[Agartha] Failed to read font " + string + ": " + var8.getMessage());
            return null;
         }
      }
   }
}
