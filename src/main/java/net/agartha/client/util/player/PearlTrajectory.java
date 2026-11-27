package net.agartha.client.util.player;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

@Environment(EnvType.CLIENT)
public final class PearlTrajectory {
   private static final double SPAWN_EYE_OFFSET = 0.1;
   private static final double GRAVITY = 0.03;
   private static final float AIR_DRAG = 0.99F;
   private static final float WATER_DRAG = 0.8F;
   private static final int MAX_TICKS = 300;

   private PearlTrajectory() {
   }

   public static PearlTrajectory.a simulate(World world, ClientPlayerEntity clientPlayerEntity, float f) {
      Vec3d var3 = clientPlayerEntity.getCameraPosVec(f);
      Vec3d var4 = new Vec3d(var3.x, var3.y - 0.1, var3.z);
      Vec3d var5 = clientPlayerEntity.getRotationVec(f).multiply(EnderPearlItem.POWER);
      ArrayList var6 = new ArrayList(301);
      var6.add(var4);
      Vec3d var7 = var4;
      BlockHitResult var8 = null;

      for (int var9 = 0; var9 < 300; var9++) {
         Vec3d var14 = var5.add(0.0, -0.03, 0.0);
         float var10 = a(world, var4) ? 0.8F : 0.99F;
         var5 = var14.multiply(var10);
         Vec3d var11 = var4.add(var5);
         BlockHitResult var12 = world.raycast(new RaycastContext(var4, var11, ShapeType.COLLIDER, FluidHandling.NONE, clientPlayerEntity));
         if (var12.getType() != Type.MISS) {
            var7 = var12.getPos();
            var6.add(var7);
            if (var12 instanceof BlockHitResult var13) {
               var8 = var13;
            }
            break;
         }

         var4 = var11;
         var6.add(var11);
         var7 = var11;
         if (var11.y < world.getBottomY() - 64) {
            break;
         }
      }

      return new PearlTrajectory.a(List.copyOf(var6), var7, var8);
   }

   private static boolean a(World world, Vec3d vec3d) {
      BlockPos var2 = BlockPos.ofFloored(vec3d);
      return world.getFluidState(var2).isIn(FluidTags.WATER);
   }

   public static boolean isHoldingPearl(ClientPlayerEntity clientPlayerEntity) {
      return clientPlayerEntity.getMainHandStack().isOf(Items.ENDER_PEARL) || clientPlayerEntity.getOffHandStack().isOf(Items.ENDER_PEARL);
   }

   @Environment(EnvType.CLIENT)
   public record a(List<Vec3d> points, Vec3d landing, BlockHitResult blockHit) {
   }
}
