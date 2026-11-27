package net.agartha.client.gui;

import imgui.ImDrawList;
import imgui.ImGui;
import net.agartha.client.mixin.AccessorGameRenderer;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.render.JumpRingsModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public final class RingRenderer {
   private static final float EMBLEM_SCALE = 0.85F;
   private static final float MIN_PIXELS = 6.0F;
   private static final float MAX_PIXELS = 512.0F;

   private RingRenderer() {
   }

   public static boolean hasWork() {
      JumpRingsModule var0 = ModuleManager.get(JumpRingsModule.class);
      return var0 != null && var0.hasRings() && var0.logo();
   }

   public static void render() {
      JumpRingsModule var0 = ModuleManager.get(JumpRingsModule.class);
      if (var0 != null && var0.hasRings() && var0.logo()) {
         MinecraftClient var1 = MinecraftClient.getInstance();
         if (var1.player != null && var1.world != null && var1.gameRenderer != null) {
            Camera var2 = var1.gameRenderer.getCamera();
            if (var2 != null) {
               float var3 = ImGui.getIO().getDisplaySizeX();
               float var4 = ImGui.getIO().getDisplaySizeY();
               if (!(var3 <= 0.0F) && !(var4 <= 0.0F)) {
                  float var5 = var1.getRenderTickCounter().getTickProgress(false);
                  float var6 = ((AccessorGameRenderer)var1.gameRenderer).agartha$invokeGetFov(var2, var5, true);
                  Matrix4f var7 = var1.gameRenderer.getBasicProjectionMatrix(var6);
                  Matrix4f var8 = new Matrix4f().rotation(var2.getRotation().conjugate(new Quaternionf()));
                  Vec3d var9 = var2.getCameraPos();
                  float var10 = (float)(var4 / (2.0 * Math.tan(Math.toRadians(var6) * 0.5)));
                  ImDrawList var11 = ImGui.getBackgroundDrawList();
                  long var12 = System.nanoTime();
                  float var14 = var0.lifetimeSeconds();
                  float var15 = var0.baseRadius();
                  int var16 = var0.currentRgb();
                  Vector4f var17 = new Vector4f();

                  for (JumpRingsModule.a var19 : var0.ringsView()) {
                     float var20 = (float)(var12 - var19.spawnNanos()) / 1.0E9F;
                     if (!(var20 < 0.0F) && !(var20 >= var14)) {
                        float var21 = var20 / var14;
                        var17.set((float)(var19.x() - var9.x), (float)(var19.y() - var9.y), (float)(var19.z() - var9.z), 1.0F);
                        var8.transform(var17);
                        var7.transform(var17);
                        if (!(var17.w <= 1.0E-4F)) {
                           float var22 = 1.0F / var17.w;
                           float var23 = (var17.x * var22 * 0.5F + 0.5F) * var3;
                           float var24 = (1.0F - (var17.y * var22 * 0.5F + 0.5F)) * var4;
                           double var25 = var9.distanceTo(new Vec3d(var19.x(), var19.y(), var19.z()));
                           if (!(var25 < 1.0E-4)) {
                              float var27 = var15 * (0.25F + a(var21) * 0.95F);
                              float var28 = (float)(var27 * 0.85F * 2.0 / var25) * var10;
                              if (!(var28 < 6.0F)) {
                                 var28 = Math.min(var28, 512.0F);
                                 float var29 = 1.0F - var21;
                                 float var30 = var28 * 0.95F;
                                 float var31 = var30 * ClawTexture.getInstance().aspect();
                                 float var32 = var23 - var31 * 0.5F;
                                 float var33 = var24 - var30 * 0.5F;
                                 ClawTexture.draw(var11, var32 + var30 * 0.03F, var33 + var30 * 0.03F, var30, a(0, var29 * 0.4F));
                                 ClawTexture.draw(var11, var32, var33, var30, a(var16, var29));
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static int a(int i, float f) {
      int var2 = Math.round(Math.max(0.0F, Math.min(1.0F, f)) * 255.0F);
      int var3 = i >>> 16 & 0xFF;
      int var4 = i >>> 8 & 0xFF;
      int var5 = i & 0xFF;
      return var2 << 24 | var5 << 16 | var4 << 8 | var3;
   }

   private static float a(float f) {
      float var1 = 1.0F - f;
      return 1.0F - var1 * var1 * var1;
   }
}
