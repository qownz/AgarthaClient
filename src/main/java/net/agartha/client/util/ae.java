package net.agartha.client.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import imgui.ImDrawList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;

@Environment(EnvType.CLIENT)
public final class ae {
   private static final ImDrawList UNUSED_DRAW_LIST = new ImDrawList(0L);
   private static final float FEATHER = 1.0F;
   private static final int MAX_LINES = 16384;
   private static final int STRIDE = 6;
   private static final float[] LINES = new float[98304];
   private static final int[] COLORS = new int[32768];
   private static int count;
   private static boolean collecting;
   private static float toGuiX = 1.0F;
   private static float toGuiY = 1.0F;
   private static List<net.agartha.client.module.f> providers = List.of();
   private static int providerRevision = -1;
   private static final ae.a ELEMENT = new ae.a();

   private ae() {
   }

   public static void segment(float f, float g, float h, float i, int j, float k) {
      add(f, g, h, i, j, k, 0, 0.0F);
   }

   static void add(float f, float g, float h, float i, int j, float k, int l, float m) {
      if (collecting && count < 16384) {
         int var8 = count * 6;
         LINES[var8] = f;
         LINES[var8 + 1] = g;
         LINES[var8 + 2] = h;
         LINES[var8 + 3] = i;
         LINES[var8 + 4] = k;
         LINES[var8 + 5] = m;
         COLORS[count * 2] = j;
         COLORS[count * 2 + 1] = l;
         count++;
      }
   }

   public static void render(DrawContext drawContext) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.world != null && var1.player != null) {
         if (!(var1.currentScreen instanceof HandledScreen)) {
            float var2 = var1.getWindow().getFramebufferWidth();
            float var3 = var1.getWindow().getFramebufferHeight();
            if (!(var2 <= 0.0F) && !(var3 <= 0.0F)) {
               count = 0;
               collecting = true;

               try {
                  for (net.agartha.client.module.f var5 : a()) {
                     if (var5.isTracerOverlayEnabled()) {
                        var5.renderTracerOverlay(UNUSED_DRAW_LIST, var2, var3);
                     }
                  }
               } finally {
                  collecting = false;
               }

               if (count != 0) {
                  int var9 = drawContext.getScaledWindowWidth();
                  int var10 = drawContext.getScaledWindowHeight();
                  toGuiX = var9 / var2;
                  toGuiY = var10 / var3;
                  ELEMENT.lines = count;
                  ELEMENT.bounds(var9, var10);
                  ((net.agartha.client.mixin.AccessorDrawContextState)drawContext).agartha$getState().addSimpleElement(ELEMENT);
               }
            }
         }
      }
   }

   private static List<net.agartha.client.module.f> a() {
      int var0 = net.agartha.client.module.e.revision();
      if (var0 != providerRevision) {
         providers = net.agartha.client.module.e.all()
            .stream()
            .filter(net.agartha.client.module.f.class::isInstance)
            .map(net.agartha.client.module.f.class::cast)
            .toList();
         providerRevision = var0;
      }

      return providers;
   }

   @Environment(EnvType.CLIENT)
   private static final class a implements SimpleGuiElementRenderState {
      private static final Matrix3x2f IDENTITY = new Matrix3x2f();
      int lines;
      private ScreenRect bounds;
      private int boundsW = -1;
      private int boundsH = -1;

      void bounds(int i, int j) {
         if (i != this.boundsW || j != this.boundsH) {
            this.bounds = new ScreenRect(0, 0, i, j);
            this.boundsW = i;
            this.boundsH = j;
         }
      }

      public void setupVertices(VertexConsumer vertices) {
         for (int var2 = 0; var2 < this.lines; var2++) {
            int var3 = var2 * 6;
            a(vertices, var3, ae.COLORS[var2 * 2 + 1], ae.LINES[var3 + 5]);
         }

         for (int var4 = 0; var4 < this.lines; var4++) {
            int var5 = var4 * 6;
            a(vertices, var5, ae.COLORS[var4 * 2], ae.LINES[var5 + 4]);
         }
      }

      private static void a(VertexConsumer vertexConsumer, int i, int j, float f) {
         if (j >>> 24 != 0 && !(f <= 0.0F)) {
            float var4 = ae.LINES[i];
            float var5 = ae.LINES[i + 1];
            float var6 = ae.LINES[i + 2];
            float var7 = ae.LINES[i + 3];
            float var8 = var6 - var4;
            float var9 = var7 - var5;
            float var10 = (float)Math.sqrt(var8 * var8 + var9 * var9);
            if (!(var10 < 0.001F)) {
               float var11 = -var9 / var10;
               float var12 = var8 / var10;
               float var13 = Math.max(0.0F, f * 0.5F - 0.5F);
               float var14 = var13 + 1.0F;
               int var15 = j & 16777215;
               a(vertexConsumer, var4, var5, var6, var7, var11, var12, -var13, var13, j, j);
               a(vertexConsumer, var4, var5, var6, var7, var11, var12, var13, var14, j, var15);
               a(vertexConsumer, var4, var5, var6, var7, var11, var12, -var14, -var13, var15, j);
            }
         }
      }

      private static void a(VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, int n, int o) {
         a(vertexConsumer, f + j * l, g + k * l, n);
         a(vertexConsumer, f + j * m, g + k * m, o);
         a(vertexConsumer, h + j * m, i + k * m, o);
         a(vertexConsumer, h + j * l, i + k * l, n);
      }

      private static void a(VertexConsumer vertexConsumer, float f, float g, int i) {
         vertexConsumer.vertex(IDENTITY, f * ae.toGuiX, g * ae.toGuiY).color(i);
      }

      public RenderPipeline pipeline() {
         return RenderPipelines.GUI;
      }

      public TextureSetup textureSetup() {
         return TextureSetup.empty();
      }

      public ScreenRect scissorArea() {
         return null;
      }

      public ScreenRect bounds() {
         return this.bounds;
      }
   }
}
