package net.agartha.client.util.b_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public final class e {
   private e() {
   }

   public static LivingEntity findBestLivingTarget(MinecraftClient minecraftClient, double d, double e, boolean bl, double f, double g) {
      ClientPlayerEntity var10 = minecraftClient.player;
      if (var10 != null && minecraftClient.world != null) {
         float var11 = minecraftClient.getRenderTickCounter().getTickProgress(false);
         Vec3d var12 = var10.getCameraPosVec(var11);
         c var13 = net.agartha.client.util.b_pkg.d.entityRotation(var10);
         double var14 = d * d;
         double var16 = e * 0.5;
         LivingEntity var18 = null;
         double var19 = Double.MAX_VALUE;

         for (Entity var22 : minecraftClient.world.getEntities()) {
            if (var22 instanceof LivingEntity var23 && isValidTarget(var10, var23, bl)) {
               Box var24 = predictedBox(var23, var11, f, g);
               Vec3d var25 = closestPoint(var12, var24);
               double var26 = var25.squaredDistanceTo(var12);
               if (!(var26 > var14)) {
                  c var28 = net.agartha.client.util.b_pkg.d.fromTo(var12, var25);
                  double var29 = net.agartha.client.util.b_pkg.d.angleTo(var13, var28);
                  if (!(var29 > var16)) {
                     double var31 = var29 * 4.0 + var26 * 0.01;
                     if (var31 < var19) {
                        var19 = var31;
                        var18 = var23;
                     }
                  }
               }
            }
         }

         return var18;
      } else {
         return null;
      }
   }

   public static boolean isValidTarget(ClientPlayerEntity clientPlayerEntity, LivingEntity livingEntity, boolean bl) {
      if (livingEntity == clientPlayerEntity || livingEntity.isRemoved() || !livingEntity.isAlive() || livingEntity.isDead()) {
         return false;
      } else {
         return bl && !(livingEntity instanceof PlayerEntity) ? false : !(livingEntity instanceof PlayerEntity var3 && var3.isSpectator());
      }
   }

   public static Box predictedBox(Entity entity, float f, double d, double e) {
      Vec3d var6 = entity.getEntityPos();
      Vec3d var7 = entity.getLerpedPos(f);
      Vec3d var8 = var6.subtract(entity.getLastRenderPos());
      Box var9 = entity.getBoundingBox().offset(var7.x - var6.x, var7.y - var6.y, var7.z - var6.z);
      if (d > 0.0) {
         var9 = var9.contract(Math.min(d, var9.getLengthX() * 0.45), Math.min(d, var9.getLengthY() * 0.45), Math.min(d, var9.getLengthZ() * 0.45));
      }

      return var9.offset(var8.multiply(e));
   }

   public static Vec3d aimPoint(Vec3d vec3d, Box box, String string) {
      if ("Center".equals(string)) {
         return box.getCenter();
      } else {
         return box.contains(vec3d)
            ? new Vec3d((box.minX + box.maxX) * 0.5, e.a.a(vec3d.y, box.minY, box.maxY), (box.minZ + box.maxZ) * 0.5)
            : closestPoint(vec3d, box);
      }
   }

   public static Vec3d closestPoint(Vec3d vec3d, Box box) {
      return new Vec3d(e.a.a(vec3d.x, box.minX, box.maxX), e.a.a(vec3d.y, box.minY, box.maxY), e.a.a(vec3d.z, box.minZ, box.maxZ));
   }

   public static boolean rayIntersects(Vec3d vec3d, Vec3d vec3d2, Box box, double d) {
      Vec3d var5 = vec3d.add(vec3d2.normalize().multiply(d));
      return box.raycast(vec3d, var5).isPresent();
   }

   public static LivingEntity raycastLivingTarget(MinecraftClient minecraftClient, double d, boolean bl, boolean bl2) {
      ClientPlayerEntity var5 = minecraftClient.player;
      if (var5 != null && minecraftClient.world != null) {
         float var6 = minecraftClient.getRenderTickCounter().getTickProgress(false);
         Vec3d var7 = var5.getCameraPosVec(var6);
         Vec3d var8 = var5.getRotationVec(var6);
         double var9 = d;
         if (!bl) {
            HitResult var11 = var5.raycast(d, var6, false);
            if (var11.getType() != Type.MISS) {
               var9 = Math.min(d, var11.getPos().distanceTo(var7));
            }
         }

         Vec3d var16 = var7.add(var8.multiply(var9));
         Box var12 = var5.getBoundingBox().stretch(var8.multiply(var9)).expand(1.0);
         EntityHitResult var13 = ProjectileUtil.raycast(
            var5,
            var7,
            var16,
            var12,
            entity -> entity instanceof LivingEntity var3 && EntityPredicates.CAN_HIT.test(entity) && isValidTarget(var5, var3, bl2),
            var9 * var9
         );
         return var13 != null && var13.getEntity() instanceof LivingEntity var14 ? var14 : null;
      } else {
         return null;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class a {
      private static double a(double d, double e, double f) {
         return Math.max(e, Math.min(f, d));
      }
   }
}
