package net.agartha.client.util.render;

import java.util.LinkedHashMap;
import java.util.Map;
import net.agartha.client.util.perf.Profiler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.VisibilityConfigurable;

@Environment(EnvType.CLIENT)
public final class GizmoBatch {
   private static final Map<BlockPos, GizmoBatch.a> SUBMISSIONS = new LinkedHashMap<>();

   private GizmoBatch() {
   }

   public static void beginFrame() {
      SUBMISSIONS.clear();
   }

   public static void submitCuboid(BlockPos blockPos, Box box, int i, float f, Integer integer, boolean bl) {
      if (blockPos != null && FrustumCulling.isSafeCuboid(box) && !FrustumCulling.outOfView(box)) {
         GizmoBatch.a var6 = new GizmoBatch.a(blockPos.toImmutable(), box, i, Math.max(0.5F, f), integer, bl);
         GizmoBatch.a var7 = SUBMISSIONS.get(blockPos);
         if (var7 == null) {
            SUBMISSIONS.put(var6.key(), var6);
         } else {
            SUBMISSIONS.put(blockPos, a(var7, var6));
         }
      }
   }

   public static void flush() {
      long var0 = Profiler.begin();
      long var2 = Profiler.allocatedBytes();

      for (GizmoBatch.a var5 : SUBMISSIONS.values()) {
         DrawStyle var6 = var5.fillColor() != null
            ? DrawStyle.filledAndStroked(var5.strokeColor(), var5.lineWidth(), var5.fillColor())
            : DrawStyle.stroked(var5.strokeColor(), var5.lineWidth());
         VisibilityConfigurable var7 = GizmoDrawing.box(var5.box(), var6);
         if (var5.alwaysOnTop()) {
            var7.ignoreOcclusion();
         }
      }

      if (Profiler.enabled()) {
         Profiler.recordCount("gizmo.submissions", SUBMISSIONS.size());
      }

      SUBMISSIONS.clear();
      Profiler.record("gizmo.flush", var0);
      Profiler.recordAllocatedBytes("gizmo.flush", var2);
   }

   private static GizmoBatch.a a(GizmoBatch.a arg, GizmoBatch.a arg2) {
      Box var2 = arg.box();
      int var3 = arg.strokeColor();
      float var4 = Math.max(arg.lineWidth(), arg2.lineWidth());
      if (a(arg2.box()) > a(arg.box())) {
         var2 = arg2.box();
         var3 = arg2.strokeColor();
      }

      Integer var5 = arg.fillColor();
      if (var5 == null || a(arg2.fillColor()) > a(var5)) {
         var5 = arg2.fillColor();
      }

      return new GizmoBatch.a(arg.key(), var2, var3, var4, var5, arg.alwaysOnTop() || arg2.alwaysOnTop());
   }

   private static double a(Box box) {
      return Math.max(0.0, box.getLengthX()) * Math.max(0.0, box.getLengthY()) * Math.max(0.0, box.getLengthZ());
   }

   private static int a(Integer integer) {
      return integer == null ? -1 : integer >>> 24 & 0xFF;
   }

   @Environment(EnvType.CLIENT)
   private record a(BlockPos key, Box box, int strokeColor, float lineWidth, Integer fillColor, boolean alwaysOnTop) {
   }
}
