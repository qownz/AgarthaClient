package net.agartha.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public final class e {
   private static boolean forcedOff;
   private static int clearFrames;
   private static final int RELEASE_AFTER_FRAMES = 10;
   private static final double MARGIN = 0.25;

   private e() {
   }

   public static void update(World world, Vec3d vec3d) {
      boolean var2 = world != null && vec3d != null && a(world, vec3d);
      if (var2) {
         clearFrames = 0;
         if (!forcedOff) {
            MinecraftClient.getInstance().chunkCullingEnabled = false;
            forcedOff = true;
         }
      } else if (forcedOff && ++clearFrames >= 10) {
         release();
      }
   }

   private static boolean a(World world, Vec3d vec3d) {
      return a(world, vec3d.x, vec3d.y, vec3d.z)
         ? true
         : a(world, vec3d.x + 0.25, vec3d.y, vec3d.z)
            || a(world, vec3d.x - 0.25, vec3d.y, vec3d.z)
            || a(world, vec3d.x, vec3d.y + 0.25, vec3d.z)
            || a(world, vec3d.x, vec3d.y - 0.25, vec3d.z)
            || a(world, vec3d.x, vec3d.y, vec3d.z + 0.25)
            || a(world, vec3d.x, vec3d.y, vec3d.z - 0.25);
   }

   private static boolean a(World world, double d, double e, double f) {
      return world.getBlockState(BlockPos.ofFloored(d, e, f)).isOpaqueFullCube();
   }

   public static void release() {
      clearFrames = 0;
      if (forcedOff) {
         MinecraftClient.getInstance().chunkCullingEnabled = true;
         forcedOff = false;
      }
   }
}
