package net.agartha.client.util;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.VisibilityConfigurable;

@Environment(EnvType.CLIENT)
public final class k {
   private static final Map<BlockPos, k.a> SUBMISSIONS = new LinkedHashMap<>();

   private k() {
   }

   public static void beginFrame() {
      SUBMISSIONS.clear();
   }

   public static void submitCuboid(BlockPos blockPos, Box box, int i, float f, Integer integer, boolean bl) {
      if (blockPos != null && net.agartha.client.util.l.isSafeCuboid(box) && !l.outOfView(box)) {
         k.a var6 = new k.a(blockPos.toImmutable(), box, i, Math.max(0.5F, f), integer, bl);
         k.a var7 = SUBMISSIONS.get(blockPos);
         if (var7 == null) {
            SUBMISSIONS.put(var6.key(), var6);
         } else {
            SUBMISSIONS.put(blockPos, a(var7, var6));
         }
      }
   }

   public static void flush() {
      long var0 = net.agartha.client.k.b_o.begin();
      long var2 = net.agartha.client.k.b_o.allocatedBytes();

      for (k.a var5 : SUBMISSIONS.values()) {
         DrawStyle var6 = var5.fillColor() != null
            ? DrawStyle.filledAndStroked(var5.strokeColor(), var5.lineWidth(), var5.fillColor())
            : DrawStyle.stroked(var5.strokeColor(), var5.lineWidth());
         VisibilityConfigurable var7 = GizmoDrawing.box(var5.box(), var6);
         if (var5.alwaysOnTop()) {
            var7.ignoreOcclusion();
         }
      }

      if (net.agartha.client.k.b_o.enabled()) {
         net.agartha.client.k.b_o.recordCount("gizmo.submissions", SUBMISSIONS.size());
      }

      SUBMISSIONS.clear();
      net.agartha.client.k.b_o.record("gizmo.flush", var0);
      net.agartha.client.k.b_o.recordAllocatedBytes("gizmo.flush", var2);
   }

   private static k.a a(k.a arg, k.a arg2) {
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

      return new k.a(arg.key(), var2, var3, var4, var5, arg.alwaysOnTop() || arg2.alwaysOnTop());
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
