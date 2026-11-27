package net.agartha.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.chunk.ChunkRenderData;
import net.minecraft.client.render.chunk.ChunkRendererRegionBuilder;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;

@Environment(EnvType.CLIENT)
public final class t {
   private static final long BUDGET_NANOS = 1500000L;
   private static final int LOOK_PER_FRAME = 4096;
   private static int cursor;
   private static net.agartha.client.module.impl.misc.k freecam;
   private static net.agartha.client.module.impl.render.d freelook;

   private t() {
   }

   public static void run(BuiltChunkStorage builtChunkStorage) {
      if (builtChunkStorage != null && a()) {
         BuiltChunk[] var1 = builtChunkStorage.chunks;
         if (var1 != null && var1.length != 0) {
            long var2 = System.nanoTime() + 1500000L;
            ChunkRendererRegionBuilder var4 = new ChunkRendererRegionBuilder();
            int var5 = var1.length;
            if (cursor >= var5) {
               cursor = 0;
            }

            for (int var6 = 0; var6 < 4096 && var6 < var5; var6++) {
               BuiltChunk var7 = var1[cursor];
               cursor = cursor + 1 >= var5 ? 0 : cursor + 1;
               if (var7 != null && var7.needsRebuild() && var7.getCurrentRenderData() == ChunkRenderData.HIDDEN && var7.shouldBuild()) {
                  var7.scheduleRebuild(var4);
                  var7.cancelRebuild();
                  if (System.nanoTime() > var2) {
                     break;
                  }
               }
            }
         }
      }
   }

   private static boolean a() {
      if (freecam == null) {
         freecam = net.agartha.client.module.e.get(net.agartha.client.module.impl.misc.k.class);
      }

      if (freelook == null) {
         freelook = net.agartha.client.module.e.get(net.agartha.client.module.impl.render.d.class);
      }

      return a(freecam) || a(freelook);
   }

   private static boolean a(net.agartha.client.module.b arg) {
      return arg != null && (arg.isEnabled() || arg.keybind() != -1);
   }
}
