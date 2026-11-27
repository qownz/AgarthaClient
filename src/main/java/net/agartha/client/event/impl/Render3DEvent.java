package net.agartha.client.event.impl;

import net.agartha.client.event.Event;
import net.agartha.client.event.EventType;
import net.agartha.client.util.render.FrustumCulling;
import net.agartha.client.util.render.GizmoBatch;
import net.agartha.client.util.render.LabelStacker;
import net.agartha.client.util.render.PlateRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public final class Render3DEvent extends Event {
   public static final EventType<Render3DEvent> TYPE = new EventType<>("Render3DEvent");
   public MatrixStack poseStack;
   public float partialTick;
   public Matrix4f projectionMatrix;
   private static final Render3DEvent INSTANCE = new Render3DEvent();

   private Render3DEvent() {
   }

   public static void dispatch(MatrixStack matrixStack, float f, Matrix4f matrix4f) {
      dispatch(matrixStack, f, matrix4f, null);
   }

   public static void dispatch(MatrixStack matrixStack, float f, Matrix4f matrix4f, Frustum frustum) {
      if (TYPE.hasPreListeners()) {
         INSTANCE.poseStack = matrixStack;
         INSTANCE.partialTick = f;
         INSTANCE.projectionMatrix = matrix4f;
         FrustumCulling.beginFrame(frustum);
         LabelStacker.beginFrame();
         GizmoBatch.beginFrame();
         PlateRenderer.beginFrame();

         try {
            TYPE.dispatch(INSTANCE);
            GizmoBatch.flush();
            PlateRenderer.flush();
         } finally {
            FrustumCulling.endFrame();
         }

         INSTANCE.poseStack = null;
         INSTANCE.projectionMatrix = null;
      }
   }
}
