package net.agartha.client.module.impl.render;

import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.PacketReceiveEvent;
import net.agartha.client.event.impl.PlayerAttackEntityEvent;
import net.agartha.client.event.impl.Render3DEvent;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ColorSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.render.effect.GizmoCanvas;
import net.agartha.client.render.effect.ParticleSystem;
import net.agartha.client.util.color.ColorSettingWiring;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public final class ParticlesModule extends Module {
   private static final int CAPACITY = 900;
   private static final double PROJECTILE_RANGE_SQR = 6400.0;
   private static final double RESTING_SQR = 9.0E-4;
   private static final double STEP_MIN_SPEED = 0.07;
   private static final byte TOTEM_EVENT = 35;
   private final BooleanSetting pops = this.addSetting(new BooleanSetting("Totem Pops", true));
   private final BooleanSetting ownPops = this.addSetting(new BooleanSetting("Own Pops", true));
   private final BooleanSetting hits = this.addSetting(new BooleanSetting("Hits", true));
   private final BooleanSetting projectiles = this.addSetting(new BooleanSetting("Projectiles", true));
   private final BooleanSetting arrows = this.addSetting(new BooleanSetting("Arrows", true));
   private final BooleanSetting tridents = this.addSetting(new BooleanSetting("Tridents", true));
   private final BooleanSetting footsteps = this.addSetting(new BooleanSetting("Footsteps", false));
   private final NumberSetting size = this.addSetting(new NumberSetting("Size", 1.0F, 0.4F, 2.5F));
   private final NumberSetting lifetime = this.addSetting(new NumberSetting("Lifetime", 1.0F, 0.4F, 2.5F));
   private final NumberSetting limit = this.addSetting(new NumberSetting("Limit", 260.0F, 40.0F, 900.0F));
   private final BooleanSetting customColor = this.addSetting(new BooleanSetting("Custom Color", false));
   private final ColorSetting color = this.addSetting(new ColorSetting("Color", -1));
   private final BooleanSetting themeColor = this.addSetting(new BooleanSetting("Theme Color", true));
   private final ParticleSystem swarm = new ParticleSystem(900);
   private final GizmoCanvas canvas = new GizmoCanvas();
   private final Random random = Random.create();
   private ClientWorld boundLevel;
   private long lastFrame;
   private int base;
   private int bright;
   private boolean leftFoot;

   public ParticlesModule() {
      super(
         "Particles",
         "Agartha effects: a rising star constellation for totem pops, a shatter for your hits, comet trails behind arrows and tridents, and light prints for your steps.",
         ModuleCategory.RENDER
      );
      ColorSettingWiring.wireExclusiveColorSource(this.customColor, this.themeColor, this.color, true);
      this.ownPops.visibleWhen(this.pops::get);
      this.arrows.visibleWhen(this.projectiles::get);
      this.tridents.visibleWhen(this.projectiles::get);
   }

   @Override
   protected void onDisable() {
      this.swarm.clear();
      this.boundLevel = null;
      this.lastFrame = 0L;
   }

   private boolean a(ClientWorld clientWorld) {
      if (clientWorld == null) {
         if (this.boundLevel != null) {
            this.onDisable();
         }

         return false;
      } else {
         if (clientWorld != this.boundLevel) {
            this.swarm.clear();
            this.boundLevel = clientWorld;
            this.lastFrame = 0L;
         }

         return true;
      }
   }

   private void a() {
      this.base = ColorSettingWiring.argb(this.themeColor, this.customColor, this.color, -1) | 0xFF000000;
      this.bright = ColorHelper.lerp(0.6F, this.base, -1);
   }

   @EventListener
   public void onPacket(PacketReceiveEvent arg) {
      if (this.pops.get() && arg.packet instanceof EntityStatusS2CPacket var2) {
         if (var2.getStatus() == 35) {
            MinecraftClient var6 = MinecraftClient.getInstance();
            if (var6.world != null) {
               if (var2.getEntity(var6.world) instanceof LivingEntity var5) {
                  if (var5 == var6.player && !this.ownPops.get()) {
                     return;
                  }

                  this.a(var5);
               }
            }
         }
      }
   }

   @EventListener
   public void onAttack(PlayerAttackEntityEvent arg) {
      if (this.hits.get() && arg.target instanceof LivingEntity var2) {
         this.b(var2);
      }
   }

   @EventListener
   public void onTick(TickEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientPlayerEntity var3 = var2.player;
      if (this.a(var2.world) && var3 != null) {
         this.a();
         if (this.projectiles.get()) {
            this.a(var2.world, var3);
         }

         if (this.footsteps.get()) {
            this.a(var3);
         }
      }
   }

   @EventListener
   public void onRender3D(Render3DEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (this.a(var2.world)) {
         long var3 = System.nanoTime();
         float var5 = this.lastFrame == 0L ? 0.0F : Math.min(0.1F, (float)(var3 - this.lastFrame) / 1.0E9F);
         this.lastFrame = var3;
         if (var2.isPaused()) {
            var5 = 0.0F;
         }

         this.swarm.trim(this.limit.getInt());
         this.swarm.step(var5);
         if (this.swarm.count() != 0) {
            this.canvas.begin(var2.gameRenderer.getCamera());
            this.swarm.draw(this.canvas, 1.0F);
            this.canvas.submit(false);
         }
      }
   }

   private void a(LivingEntity livingEntity) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (this.a(var2.world) && livingEntity.getEntityWorld() == var2.world) {
         this.a();
         Box var3 = livingEntity.getBoundingBox();
         double var4 = (var3.minX + var3.maxX) * 0.5;
         double var6 = (var3.minZ + var3.maxZ) * 0.5;
         float var8 = this.size.get();
         float var9 = this.lifetime.get();
         int var10 = this.limit.getInt();
         float var11 = (float)Math.max(var3.getLengthX(), var3.getLengthZ());

         for (int var12 = 0; var12 < 2; var12++) {
            int var13 = var12 == 0 ? 8 : 6;
            double var14 = var3.minY + var3.getLengthY() * (var12 == 0 ? 0.3 : 0.75);
            float var16 = var11 * (var12 == 0 ? 0.95F : 0.7F);
            float var17 = var12 == 0 ? 1.0F : -1.0F;
            float var18 = this.random.nextFloat() * (float) (Math.PI * 2);

            for (int var19 = 0; var19 < var13; var19++) {
               float var20 = var18 + (float) (Math.PI * 2) * var19 / var13;
               this.swarm
                  .orbit(
                     var4,
                     var14,
                     var6,
                     var16,
                     var20,
                     var17 * 2.6F,
                     0.9F + 0.25F * var12,
                     var13,
                     var9 * 1.7F,
                     0.15F * var8 * (var12 == 0 ? 1.0F : 0.8F),
                     this.base,
                     this.bright,
                     var10
                  );
            }
         }

         for (int var21 = 0; var21 < 14; var21++) {
            float var22 = this.random.nextFloat() * (float) (Math.PI * 2);
            float var23 = 1.2F + this.random.nextFloat() * 1.6F;
            this.swarm
               .spawn(
                  (byte)1,
                  var4,
                  var3.minY + var3.getLengthY() * 0.5,
                  var6,
                  MathHelper.cos(var22) * var23,
                  3.2F + this.random.nextFloat() * 2.4F,
                  MathHelper.sin(var22) * var23,
                  var9 * (0.7F + 0.4F * this.random.nextFloat()),
                  0.12F * var8,
                  var22,
                  6.0F,
                  this.base,
                  this.bright,
                  var10
               );
         }

         this.swarm
            .spawn(
               (byte)2,
               var4,
               var3.minY + var3.getLengthY() * 0.55,
               var6,
               0.0F,
               0.0F,
               0.0F,
               var9 * 0.7F,
               0.65F * var8,
               this.random.nextFloat() * (float) (Math.PI * 2),
               1.5F,
               this.base,
               this.bright,
               var10
            );
      }
   }

   private void b(LivingEntity livingEntity) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientPlayerEntity var3 = var2.player;
      if (this.a(var2.world) && var3 != null && livingEntity.getEntityWorld() == var2.world) {
         this.a();
         Box var4 = livingEntity.getBoundingBox();
         double var5 = (var4.minX + var4.maxX) * 0.5;
         double var7 = var4.minY + var4.getLengthY() * 0.6;
         double var9 = (var4.minZ + var4.maxZ) * 0.5;
         double var11 = var5 - var3.getX();
         double var13 = var9 - var3.getZ();
         double var15 = Math.max(1.0E-4, Math.sqrt(var11 * var11 + var13 * var13));
         float var17 = (float)(var11 / var15);
         float var18 = (float)(var13 / var15);
         double var19 = Math.max(var4.getLengthX(), var4.getLengthZ()) * 0.5;
         double var21 = var5 - var17 * var19;
         double var23 = var9 - var18 * var19;
         float var25 = this.size.get();
         float var26 = this.lifetime.get();
         int var27 = this.limit.getInt();
         this.swarm
            .spawn(
               (byte)2,
               var21,
               var7,
               var23,
               0.0F,
               0.0F,
               0.0F,
               var26 * 0.45F,
               0.42F * var25,
               this.random.nextFloat() * (float) (Math.PI * 2),
               2.0F,
               this.base,
               this.bright,
               var27
            );

         for (int var28 = 0; var28 < 10; var28++) {
            float var29 = (this.random.nextFloat() - 0.5F) * 1.6F;
            float var30 = MathHelper.cos(var29);
            float var31 = MathHelper.sin(var29);
            float var32 = var17 * var30 - var18 * var31;
            float var33 = var17 * var31 + var18 * var30;
            float var34 = 2.4F + this.random.nextFloat() * 2.6F;
            this.swarm
               .spawn(
                  (byte)1,
                  var21,
                  var7 + (this.random.nextFloat() - 0.5F) * 0.4F,
                  var23,
                  var32 * var34,
                  1.2F + this.random.nextFloat() * 2.2F,
                  var33 * var34,
                  var26 * (0.45F + 0.3F * this.random.nextFloat()),
                  0.1F * var25,
                  this.random.nextFloat() * (float) (Math.PI * 2),
                  8.0F,
                  this.base,
                  this.bright,
                  var27
               );
         }
      }
   }

   private void a(ClientWorld clientWorld, ClientPlayerEntity clientPlayerEntity) {
      float var3 = this.size.get();
      float var4 = this.lifetime.get();
      int var5 = this.limit.getInt();

      for (Entity var7 : clientWorld.getEntities()) {
         if (this.a(var7)) {
            double var8 = var7.getX() - var7.lastX;
            double var10 = var7.getY() - var7.lastY;
            double var12 = var7.getZ() - var7.lastZ;
            if (!(var8 * var8 + var10 * var10 + var12 * var12 < 9.0E-4) && !(var7.squaredDistanceTo(clientPlayerEntity) > 6400.0)) {
               double var14 = var7.getHeight() * 0.5;
               this.swarm
                  .spawn(
                     (byte)3,
                     var7.lastX + var8 * 0.5,
                     var7.lastY + var10 * 0.5 + var14,
                     var7.lastZ + var12 * 0.5,
                     this.a(0.15F),
                     this.a(0.15F),
                     this.a(0.15F),
                     var4 * 0.8F,
                     0.1F * var3,
                     this.random.nextFloat() * (float) (Math.PI * 2),
                     1.5F,
                     this.base,
                     this.bright,
                     var5
                  );
               this.swarm
                  .spawn(
                     (byte)4,
                     var7.lastX + this.a(0.05F),
                     var7.lastY + var14 + this.a(0.05F),
                     var7.lastZ + this.a(0.05F),
                     this.a(0.25F),
                     0.2F + this.random.nextFloat() * 0.3F,
                     this.a(0.25F),
                     var4 * 0.9F,
                     0.06F * var3,
                     this.random.nextFloat() * (float) (Math.PI * 2),
                     3.0F,
                     this.base,
                     this.bright,
                     var5
                  );
            }
         }
      }
   }

   private boolean a(Entity entity) {
      if (entity.isRemoved()) {
         return false;
      } else if (entity instanceof TridentEntity) {
         return this.tridents.get();
      } else {
         return entity instanceof PersistentProjectileEntity ? this.arrows.get() : false;
      }
   }

   private void a(ClientPlayerEntity clientPlayerEntity) {
      if (clientPlayerEntity.isOnGround() && !clientPlayerEntity.isSpectator() && !clientPlayerEntity.hasVehicle() && !clientPlayerEntity.isInFluid()) {
         double var2 = clientPlayerEntity.getX() - clientPlayerEntity.lastX;
         double var4 = clientPlayerEntity.getZ() - clientPlayerEntity.lastZ;
         double var6 = Math.sqrt(var2 * var2 + var4 * var4);
         if (!(var6 < 0.07)) {
            if (clientPlayerEntity.age % (clientPlayerEntity.isSprinting() ? 3 : 4) == 0) {
               this.leftFoot = !this.leftFoot;
               double var8 = this.leftFoot ? 0.17 : -0.17;
               double var10 = clientPlayerEntity.getX() + -var4 / var6 * var8;
               double var12 = clientPlayerEntity.getZ() + var2 / var6 * var8;
               float var14 = this.size.get();
               this.swarm
                  .spawn(
                     (byte)3,
                     var10,
                     clientPlayerEntity.getY() + 0.06,
                     var12,
                     0.0F,
                     0.05F,
                     0.0F,
                     this.lifetime.get() * 1.4F,
                     0.13F * var14,
                     (float)Math.atan2(var4, var2),
                     0.4F,
                     this.base,
                     this.bright,
                     this.limit.getInt()
                  );
            }
         }
      }
   }

   private float a(float f) {
      return (this.random.nextFloat() * 2.0F - 1.0F) * f;
   }
}
