package net.agartha.client.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

@Environment(EnvType.CLIENT)
public final class k extends net.agartha.client.module.b {
   public final Vector3d pos = new Vector3d();
   public final Vector3d prevPos = new Vector3d();
   public float yaw;
   public float pitch;
   public float prevYaw;
   public float prevPitch;
   private final net.agartha.client.module.a_pkg.j speed = this.addSetting(new net.agartha.client.module.a_pkg.j("Speed", 1.0F, 0.05F, 15.0F));
   private final net.agartha.client.module.a_pkg.b sShowHands = this.addSetting(new net.agartha.client.module.a_pkg.b("Show Hands", false));
   private final net.agartha.client.module.a_pkg.b sScrollSpeed = this.addSetting(new net.agartha.client.module.a_pkg.b("Scroll Speed", false));
   private double scrollFactor = 1.0;
   private ClientWorld enabledLevel;
   private boolean forward;
   private boolean backward;
   private boolean left;
   private boolean right;
   private boolean up;
   private boolean down;
   private boolean sneakLocked;
   private static final double DIAGONAL = 1.0 / Math.sqrt(2.0);

   public k() {
      super("Freecam", "Detaches the camera so you can fly around while your body stays put.", net.agartha.client.module.c.RENDER);
   }

   @Override
   public boolean persistEnabled() {
      return false;
   }

   @Override
   protected void onEnable() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.player == null) {
         this.toggle();
      } else {
         Camera var2 = var1.gameRenderer.getCamera();
         Vec3d var3 = var2 != null && var2.isReady() ? var2.getCameraPos() : var1.player.getCameraPosVec(1.0F);
         this.pos.set(var3.x, var3.y, var3.z);
         this.prevPos.set(this.pos);
         this.yaw = var1.player.getYaw();
         this.pitch = var1.player.getPitch();
         if (var1.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            this.yaw += 180.0F;
            this.pitch = -this.pitch;
         }

         this.prevYaw = this.yaw;
         this.prevPitch = this.pitch;
         this.scrollFactor = 1.0;
         this.enabledLevel = var1.world;
         this.sneakLocked = var1.options.sneakKey.isPressed();
         this.forward = var1.options.forwardKey.isPressed();
         this.backward = var1.options.backKey.isPressed();
         this.left = var1.options.leftKey.isPressed();
         this.right = var1.options.rightKey.isPressed();
         this.up = var1.options.jumpKey.isPressed();
         this.down = var1.options.sneakKey.isPressed();
         a(var1.options.forwardKey, false);
         a(var1.options.backKey, false);
         a(var1.options.leftKey, false);
         a(var1.options.rightKey, false);
         a(var1.options.jumpKey, false);
         a(var1.options.sneakKey, this.sneakLocked);
         a(var1.options.sprintKey, false);
         var1.player.setSprinting(false);
         this.on(net.agartha.client.d.a_pkg.q.TYPE, this::a);
         this.on(net.agartha.client.d.a_pkg.f.TYPE, this::a);
         this.on(net.agartha.client.d.a_pkg.h.TYPE, this::a);
         this.on(net.agartha.client.d.a_pkg.i.TYPE, this::a);
      }
   }

   private static void a(KeyBinding keyBinding, boolean bl) {
      if (keyBinding != null) {
         keyBinding.setPressed(bl);
      }
   }

   @Override
   protected void onDisable() {
      this.forward = this.backward = this.left = this.right = this.up = this.down = false;
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (this.sneakLocked) {
         a(var1.options.sneakKey, false);
      }

      this.sneakLocked = false;
      this.enabledLevel = null;
      net.agartha.client.util.e.release();
   }

   private void a(net.agartha.client.d.a_pkg.q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null) {
         if (!var2.isWindowFocused()) {
            this.setEnabled(false);
         } else if (var2.world != this.enabledLevel) {
            this.setEnabled(false);
         } else {
            if (var2.currentScreen != null) {
               this.forward = this.backward = this.left = this.right = this.up = this.down = false;
            }

            if (this.sneakLocked) {
               a(var2.options.sneakKey, true);
            }

            this.prevPos.set(this.pos);
            this.prevYaw = this.yaw;
            this.prevPitch = this.pitch;
            Vec3d var3 = Vec3d.fromPolar(0.0F, this.yaw);
            Vec3d var4 = Vec3d.fromPolar(0.0F, this.yaw + 90.0F);
            boolean var5 = var2.options.sprintKey.isPressed();
            float var6 = this.speed.get();
            double var7 = var6 * this.scrollFactor * 1.1 * (var5 ? 2.0 : 1.0);
            double var9 = 0.0;
            double var11 = 0.0;
            double var13 = 0.0;
            if (this.forward) {
               var9 += var3.x * var7;
               var13 += var3.z * var7;
            }

            if (this.backward) {
               var9 -= var3.x * var7;
               var13 -= var3.z * var7;
            }

            if (this.right) {
               var9 += var4.x * var7;
               var13 += var4.z * var7;
            }

            if (this.left) {
               var9 -= var4.x * var7;
               var13 -= var4.z * var7;
            }

            if (this.forward != this.backward && this.left != this.right) {
               var9 *= DIAGONAL;
               var13 *= DIAGONAL;
            }

            if (this.up) {
               var11 += var7;
            }

            if (this.down) {
               var11 -= var7;
            }

            this.pos.add(var9, var11, var13);
         }
      }
   }

   private void a(net.agartha.client.d.a_pkg.h arg) {
      if (this.sScrollSpeed.get() && MinecraftClient.getInstance().currentScreen == null && arg.value != 0.0) {
         this.scrollFactor = MathHelper.clamp(this.scrollFactor * (arg.value > 0.0 ? 1.15 : 0.8695652173913044), 0.1, 10.0);
         arg.setCancelled(true);
      }
   }

   private void a(net.agartha.client.d.a_pkg.i arg) {
      if (arg.packet instanceof PlayerRespawnS2CPacket) {
         this.setEnabled(false);
      }
   }

   private void a(net.agartha.client.d.a_pkg.f arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.currentScreen == null) {
         KeyInput var3 = new KeyInput(arg.key, arg.scancode, arg.mods);
         boolean var4 = arg.action != 0;
         boolean var5 = true;
         if (var2.options.forwardKey.matchesKey(var3)) {
            this.forward = var4;
         } else if (var2.options.backKey.matchesKey(var3)) {
            this.backward = var4;
         } else if (var2.options.leftKey.matchesKey(var3)) {
            this.left = var4;
         } else if (var2.options.rightKey.matchesKey(var3)) {
            this.right = var4;
         } else if (var2.options.jumpKey.matchesKey(var3)) {
            this.up = var4;
         } else if (var2.options.sneakKey.matchesKey(var3)) {
            this.down = var4;
         } else {
            var5 = false;
         }

         if (var5) {
            arg.setCancelled(true);
         }
      }
   }

   public void changeLookDirection(double d, double e) {
      this.yaw = MathHelper.wrapDegrees(this.yaw + (float)(d * 0.15));
      this.pitch = MathHelper.clamp(this.pitch + (float)(e * 0.15), -90.0F, 90.0F);
   }

   public boolean showHands() {
      return this.sShowHands.get();
   }

   public double getX(float f) {
      return MathHelper.lerp(f, this.prevPos.x, this.pos.x);
   }

   public double getY(float f) {
      return MathHelper.lerp(f, this.prevPos.y, this.pos.y);
   }

   public double getZ(float f) {
      return MathHelper.lerp(f, this.prevPos.z, this.pos.z);
   }

   public float getYaw(float f) {
      return this.yaw;
   }

   public float getPitch(float f) {
      return this.pitch;
   }
}
