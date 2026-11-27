package net.agartha.client.d.a_pkg;

import net.agartha.client.util.w;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public final class o extends net.agartha.client.d.c {
   public static final net.agartha.client.d.j<o> TYPE = new net.agartha.client.d.j<>("Render3DEvent");
   public MatrixStack poseStack;
   public float partialTick;
   public Matrix4f projectionMatrix;
   private static final o INSTANCE = new o();

   private o() {
   }

   public static void dispatch(MatrixStack matrixStack, float f, Matrix4f matrix4f) {
      dispatch(matrixStack, f, matrix4f, null);
   }

   public static void dispatch(MatrixStack matrixStack, float f, Matrix4f matrix4f, Frustum frustum) {
      if (TYPE.hasPreListeners()) {
         INSTANCE.poseStack = matrixStack;
         INSTANCE.partialTick = f;
         INSTANCE.projectionMatrix = matrix4f;
         net.agartha.client.util.l.beginFrame(frustum);
         net.agartha.client.util.o.beginFrame();
         net.agartha.client.util.k.beginFrame();
         w.beginFrame();

         try {
            TYPE.dispatch(INSTANCE);
            net.agartha.client.util.k.flush();
            w.flush();
         } finally {
            net.agartha.client.util.l.endFrame();
         }

         INSTANCE.poseStack = null;
         INSTANCE.projectionMatrix = null;
      }
   }
}
