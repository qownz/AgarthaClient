package net.agartha.client.gui;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.buffers.GpuBuffer.MappedView;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import imgui.ImDrawList;
import imgui.ImGui;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.render.imgui.ImGuiManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BlockRenderLayers;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.ProjectionMatrix2;
import net.minecraft.client.render.DiffuseLighting.Type;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.client.texture.NativeImage.Format;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Atlases;
import org.joml.Matrix4fStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public final class ItemIconRenderer {
   private static final int SHEET_SIDE = 512;
   private static final int MIN_PX = 8;
   private static final int MAX_PX = 96;
   private static final int MAX_SHEETS = 8;
   private static final int BUSY_FRAMES = 2;
   private static final int IDLE_FRAMES = 600;
   private static final int PAINTS_PER_FRAME = 6;
   private static final ItemIconRenderer.a BLANK = new ItemIconRenderer.a();
   private static final Int2ObjectOpenHashMap<ItemIconRenderer.b> SHEETS = new Int2ObjectOpenHashMap();
   private static final List<ItemIconRenderer.b> RETIRED = new ArrayList<>();
   private static final Reference2ObjectOpenHashMap<Item, ItemStack> PLAIN = new Reference2ObjectOpenHashMap();
   private static final ReferenceOpenHashSet<Item> BROKEN = new ReferenceOpenHashSet();
   private static final ItemModelState MODEL = new ItemModelState();
   private static final int[] VIEWPORT = new int[4];
   private static GpuTexture depth;
   private static GpuTextureView depthView;
   private static ProjectionMatrix2 projection;
   private static GpuBuffer readback;
   private static ByteBuffer straightened;
   private static boolean straightenFailed;
   private static GpuTexture itemAtlas;
   private static int frame = Integer.MIN_VALUE;
   private static int retiredFrame;
   private static int paintsLeft;
   private static boolean disabled;
   private static final int GLINT_STRIPS = 8;
   private static final int GLINT_RGB = 16734890;

   private ItemIconRenderer() {
   }

   static float pixels(float f) {
      float var1 = f < 16.0F ? Math.round(f) : 16.0F * (float)Math.ceil(f / 16.0F - 0.02F);
      return Math.max(8.0F, Math.min(96.0F, var1));
   }

   public static float drawnSize(float f) {
      return f;
   }

   public static void draw(ImDrawList imDrawList, Item item, float f, float g, float h, float i) {
      if (item != null && item != Items.AIR) {
         ItemStack var6 = (ItemStack)PLAIN.get(item);
         if (var6 == null) {
            var6 = item.getDefaultStack();
            PLAIN.put(item, var6);
         }

         draw(imDrawList, var6, f, g, h, i);
      }
   }

   public static void draw(ImDrawList imDrawList, ItemStack itemStack, float f, float g, float h, float i) {
      if (itemStack != null && !itemStack.isEmpty() && !(i <= 0.004F)) {
         a();
         float var6 = Math.max(0.01F, ImGui.getIO().getDisplayFramebufferScaleX());
         int var7 = (int)pixels(h * var6);
         float var8 = Math.round(h * var6) / var6;
         float var9 = Math.round(f * var6) / var6;
         float var10 = Math.round(g * var6) / var6;

         ItemIconRenderer.a var11;
         try {
            var11 = a(itemStack, var7);
         } finally {
            MODEL.clear();
         }

         if (var11 != null) {
            if (var11.texture == 0) {
               float var19 = Math.max(1.0F, var8 * 0.18F);
               imDrawList.addRectFilled(var9 + 1.0F, var10 + 1.0F, var9 + var8 - 1.0F, var10 + var8 - 1.0F, DrawUtils.alpha(452984831, i), var19);
            } else {
               float var12 = Math.abs(var8 * var6 - var7) > 0.5F ? 9.765625E-4F : 0.0F;
               float var13 = var11.u0 + var12;
               float var14 = var11.v0 - var12;
               float var15 = var11.u1 - var12;
               float var16 = var11.v1 + var12;
               imDrawList.addImage(var11.texture, var9, var10, var9 + var8, var10 + var8, var13, var14, var15, var16, DrawUtils.alpha(-1, i));
               if (itemStack.hasGlint()) {
                  a(imDrawList, var11.texture, var9, var10, var8, var13, var14, var15, var16, i, itemStack.getItem().hashCode());
               }
            }
         }
      }
   }

   private static void a(ImDrawList imDrawList, int i, float f, float g, float h, float j, float k, float l, float m, float n, int o) {
      double var11 = System.nanoTime() % 1000000000000L / 1.0E9;
      float var13 = (float)((var11 * 0.55 + (o & 0xFF) / 255.0) % 1.0 * 1.8 - 0.4);
      float var14 = h / 8.0F;
      float var15 = (l - j) / 8.0F;

      for (int var16 = 0; var16 < 8; var16++) {
         float var17 = (var16 + 0.5F) / 8.0F;
         float var18 = (var17 - var13) / 0.22F;
         float var19 = (float)Math.exp(-var18 * var18);
         int var20 = Math.round(255.0F * n * (0.16F + 0.44F * var19));
         if (var20 > 2) {
            imDrawList.addImage(
               i, f + var14 * var16, g, f + var14 * (var16 + 1), g + h, j + var15 * var16, k, j + var15 * (var16 + 1), m, var20 << 24 | 16734890
            );
         }
      }
   }

   private static void a() {
      int var0 = ImGuiManager.getInstance().frameCount();
      if (var0 != frame) {
         frame = var0;
         paintsLeft = 6;
         if (!RETIRED.isEmpty() && var0 != retiredFrame) {
            for (ItemIconRenderer.b var2 : RETIRED) {
               var2.release();
            }

            RETIRED.clear();
         }

         GpuTexture var4 = b();
         if (var4 == itemAtlas) {
            ObjectIterator var6 = SHEETS.values().iterator();

            while (var6.hasNext()) {
               ItemIconRenderer.b var7 = (ItemIconRenderer.b)var6.next();
               if (var0 - var7.lastUsed > 600) {
                  var7.release();
                  var6.remove();
               }
            }
         } else {
            itemAtlas = var4;
            ObjectIterator var5 = SHEETS.values().iterator();

            while (var5.hasNext()) {
               ItemIconRenderer.b var3 = (ItemIconRenderer.b)var5.next();
               var3.release();
            }

            SHEETS.clear();
            BROKEN.clear();
         }
      }
   }

   private static GpuTexture b() {
      try {
         return MinecraftClient.getInstance().getAtlasManager().getAtlasTexture(Atlases.ITEMS).getGlTexture();
      } catch (RuntimeException var1) {
         return null;
      }
   }

   private static ItemIconRenderer.a a(ItemStack itemStack, int i) {
      ItemIconRenderer.b var2 = a(i);
      if (var2 == null) {
         return null;
      } else {
         var2.lastUsed = frame;
         if (!a(itemStack)) {
            return BLANK;
         } else {
            ItemIconRenderer.a var3 = (ItemIconRenderer.a)var2.cells.get(MODEL.identity);
            if (var3 != null) {
               return var3;
            } else if (disabled) {
               return BLANK;
            } else if (paintsLeft <= 0) {
               return null;
            } else {
               paintsLeft--;
               if (var2.full()) {
                  a(var2);
                  var2 = new ItemIconRenderer.b(i);
                  var2.lastUsed = frame;
                  SHEETS.put(i, var2);
               }

               var3 = a(var2, a(itemStack.getItem()));
               var2.cells.put(Arrays.asList(MODEL.identity.toArray()), var3);
               return var3;
            }
         }
      }
   }

   private static ItemIconRenderer.b a(int i) {
      ItemIconRenderer.b var1 = (ItemIconRenderer.b)SHEETS.get(i);
      if (var1 != null) {
         return var1;
      } else {
         if (SHEETS.size() >= 8) {
            ItemIconRenderer.b var2 = c();
            if (var2 == null || frame - var2.lastUsed < 2) {
               return null;
            }

            a(var2);
         }

         var1 = new ItemIconRenderer.b(i);
         SHEETS.put(i, var1);
         return var1;
      }
   }

   private static ItemIconRenderer.b c() {
      ItemIconRenderer.b var0 = null;
      ObjectIterator var1 = SHEETS.values().iterator();

      while (var1.hasNext()) {
         ItemIconRenderer.b var2 = (ItemIconRenderer.b)var1.next();
         if (var0 == null || var2.lastUsed < var0.lastUsed) {
            var0 = var2;
         }
      }

      return var0;
   }

   private static void a(ItemIconRenderer.b arg) {
      SHEETS.remove(arg.px);
      RETIRED.add(arg);
      retiredFrame = frame;
   }

   private static boolean a(ItemStack itemStack) {
      if (BROKEN.contains(itemStack.getItem())) {
         return false;
      } else if (MODEL.resolve(itemStack)) {
         return true;
      } else {
         BROKEN.add(itemStack.getItem());
         return false;
      }
   }

   private static boolean a(Item item) {
      return item instanceof BlockItem var1 ? BlockRenderLayers.getBlockLayer(var1.getBlock().getDefaultState()) == BlockRenderLayer.TRANSLUCENT : false;
   }

   private static ItemIconRenderer.a a(ItemIconRenderer.b arg, boolean bl) {
      if (MODEL.isEmpty()) {
         return BLANK;
      } else {
         try {
            if (arg.color == null) {
               b(arg);
            }

            int var2 = arg.px;
            int var3 = 512 / var2;
            int var4 = arg.filled++;
            int var5 = var4 % var3 * var2;
            int var6 = var4 / var3 * var2;
            int var7 = 512 - var6 - var2;
            render(MODEL, arg.color, arg.colorView, depth, depthView, var5, var6, var2);
            if (bl) {
               straighten(arg.color, var5, var7, var2);
            }

            ItemIconRenderer.a var8 = new ItemIconRenderer.a();
            var8.texture = ((GlTexture)arg.color).getGlId();
            var8.u0 = var5 / 512.0F;
            var8.u1 = (var5 + var2) / 512.0F;
            var8.v0 = (512 - var6) / 512.0F;
            var8.v1 = var7 / 512.0F;
            return var8;
         } catch (RuntimeException var9) {
            disabled = true;
            MinecraftClient.getInstance().gameRenderer.getEntityRenderCommandQueue().clear();
            AgarthaClientMod.LOGGER.warn("[Agartha] HUD item icons could not be drawn; showing plain slots", var9);
            return BLANK;
         }
      }
   }

   private static void b(ItemIconRenderer.b arg) {
      GpuDevice var1 = RenderSystem.getDevice();
      if (depth == null) {
         depth = var1.createTexture("Agartha icon depth", 8, TextureFormat.DEPTH32, 512, 512, 1, 1);
         depthView = var1.createTextureView(depth);
      }

      arg.color = var1.createTexture("Agartha icons " + arg.px, 15, TextureFormat.RGBA8, 512, 512, 1, 1);
      arg.colorView = var1.createTextureView(arg.color);
      var1.createCommandEncoder().clearColorAndDepthTextures(arg.color, 0, depth, 1.0);
      if (arg.color instanceof GlTexture var2) {
         nearest(var2);
      }
   }

   public static void render(
      ItemRenderState itemRenderState,
      GpuTexture gpuTexture,
      GpuTextureView gpuTextureView,
      GpuTexture gpuTexture2,
      GpuTextureView gpuTextureView2,
      int i,
      int j,
      int k
   ) {
      MinecraftClient var8 = MinecraftClient.getInstance();
      int var9 = gpuTexture.getWidth(0);
      int var10 = var9 - j - k;
      RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(gpuTexture, 0, gpuTexture2, 1.0, i, var10, k, k);
      GpuTextureView var11 = RenderSystem.outputColorTextureOverride;
      GpuTextureView var12 = RenderSystem.outputDepthTextureOverride;
      GpuBufferSlice var13 = RenderSystem.getShaderLights();
      GpuBufferSlice var14 = RenderSystem.getProjectionMatrixBuffer();
      ProjectionType var15 = RenderSystem.getProjectionType();
      GL11.glGetIntegerv(2978, VIEWPORT);
      GlStateManager._viewport(VIEWPORT[0], VIEWPORT[1], VIEWPORT[2], VIEWPORT[3]);
      Matrix4fStack var16 = RenderSystem.getModelViewStack();
      var16.pushMatrix();
      var16.identity();

      try {
         RenderSystem.outputColorTextureOverride = gpuTextureView;
         RenderSystem.outputDepthTextureOverride = gpuTextureView2;
         if (projection == null) {
            projection = new ProjectionMatrix2("Agartha icons", -1000.0F, 1000.0F, true);
         }

         RenderSystem.setProjectionMatrix(projection.set(var9, var9), ProjectionType.ORTHOGRAPHIC);
         Type var17 = itemRenderState.isSideLit() ? Type.ITEMS_3D : Type.ITEMS_FLAT;
         var8.gameRenderer.getDiffuseLighting().setShaderLights(var17);
         RenderSystem.enableScissorForRenderTypeDraws(i, var10, k, k);
         MatrixStack var18 = new MatrixStack();
         var18.translate(i + k * 0.5F, j + k * 0.5F, 0.0F);
         var18.scale(k, -k, k);
         itemRenderState.render(var18, var8.gameRenderer.getEntityRenderCommandQueue(), 15728880, OverlayTexture.DEFAULT_UV, 0);
         var8.gameRenderer.getEntityRenderDispatcher().render();
         var8.getBufferBuilders().getEntityVertexConsumers().draw();
      } finally {
         RenderSystem.disableScissorForRenderTypeDraws();
         RenderSystem.outputColorTextureOverride = var11;
         RenderSystem.outputDepthTextureOverride = var12;
         RenderSystem.setShaderLights(var13);
         RenderSystem.setProjectionMatrix(var14, var15);
         var16.popMatrix();
         GlStateManager._viewport(VIEWPORT[0], VIEWPORT[1], VIEWPORT[2], VIEWPORT[3]);
      }
   }

   static void straighten(GpuTexture gpuTexture, int i, int j, int k) {
      if (!straightenFailed) {
         int var4 = k * k * 4;
         long var5 = (long)gpuTexture.getWidth(0) * gpuTexture.getHeight(0) * 4L;

         try {
            GpuDevice var7 = RenderSystem.getDevice();
            if (readback == null || readback.size() < var5) {
               if (readback != null) {
                  readback.close();
               }

               readback = var7.createBuffer(() -> "Agartha icon readback", 9, var5);
            }

            if (straightened == null || straightened.capacity() < var4) {
               straightened = BufferUtils.createByteBuffer(var4);
            }

            CommandEncoder var8 = var7.createCommandEncoder();
            var8.copyTextureToBuffer(gpuTexture, readback, 0L, () -> {}, 0, i, j, k, k);
            GpuFence var9 = var8.createFence();

            try {
               GL11.glFlush();
               if (!var9.awaitCompletion(1000000000L)) {
                  throw new IllegalStateException("icon read-back timed out");
               }
            } catch (Throwable var18) {
               if (var9 != null) {
                  try {
                     var9.close();
                  } catch (Throwable var16) {
                     var18.addSuppressed(var16);
                  }
               }

               throw var18;
            }

            if (var9 != null) {
               var9.close();
            }

            straightened.clear().limit(var4);
            MappedView var20 = var8.mapBuffer(readback.slice(0L, var4), true, false);

            try {
               ByteBuffer var10 = var20.data();

               for (int var11 = 0; var11 < var4; var11 += 4) {
                  int var12 = var10.get(var11 + 3) & 255;

                  for (int var13 = 0; var13 < 3; var13++) {
                     int var14 = var12 == 0 ? 0 : Math.min(255, ((var10.get(var11 + var13) & 255) * 255 + var12 / 2) / var12);
                     straightened.put(var11 + var13, (byte)var14);
                  }

                  straightened.put(var11 + 3, (byte)var12);
               }
            } catch (Throwable var17) {
               if (var20 != null) {
                  try {
                     var20.close();
                  } catch (Throwable var15) {
                     var17.addSuppressed(var15);
                  }
               }

               throw var17;
            }

            if (var20 != null) {
               var20.close();
            }

            var8.writeToTexture(gpuTexture, straightened, Format.RGBA, 0, 0, i, j, k, k);
         } catch (RuntimeException var19) {
            straightenFailed = true;
            AgarthaClientMod.LOGGER.warn("[Agartha] Couldn't read icons back; see-through ones stay darker", var19);
         }
      }
   }

   static void nearest(GlTexture glTexture) {
      int var1 = GL11.glGetInteger(32873);
      GL11.glBindTexture(3553, glTexture.getGlId());
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      GL11.glBindTexture(3553, var1);
   }

   @Environment(EnvType.CLIENT)
   private static final class a {
      int texture;
      float u0;
      float v0;
      float u1;
      float v1;
   }

   @Environment(EnvType.CLIENT)
   private static final class b {
      final int px;
      final Object2ObjectOpenHashMap<Object, ItemIconRenderer.a> cells = new Object2ObjectOpenHashMap();
      GpuTexture color;
      GpuTextureView colorView;
      int filled;
      int lastUsed;

      b(int i) {
         this.px = i;
      }

      boolean full() {
         int var1 = 512 / this.px;
         return this.filled >= var1 * var1;
      }

      void release() {
         if (this.colorView != null) {
            this.colorView.close();
         }

         if (this.color != null) {
            this.color.close();
         }

         this.colorView = null;
         this.color = null;
         this.cells.clear();
      }
   }
}
