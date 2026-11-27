package net.agartha.client.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public final class u extends net.agartha.client.module.b {
   private final net.agartha.client.module.a_pkg.j sX = this.addSetting(new net.agartha.client.module.a_pkg.j("X", 0.0F, -100.0F, 100.0F).whole());
   private final net.agartha.client.module.a_pkg.j sY = this.addSetting(new net.agartha.client.module.a_pkg.j("Y", 0.0F, -100.0F, 100.0F).whole());
   private final net.agartha.client.module.a_pkg.j sZ = this.addSetting(new net.agartha.client.module.a_pkg.j("Z", 0.0F, -100.0F, 100.0F).whole());
   private final net.agartha.client.module.a_pkg.j sScale = this.addSetting(new net.agartha.client.module.a_pkg.j("Scale", 100.0F, 10.0F, 300.0F).whole());
   private final net.agartha.client.module.a_pkg.j sRotX = this.addSetting(new net.agartha.client.module.a_pkg.j("Rotate X", 0.0F, -180.0F, 180.0F).whole());
   private final net.agartha.client.module.a_pkg.j sRotY = this.addSetting(new net.agartha.client.module.a_pkg.j("Rotate Y", 0.0F, -180.0F, 180.0F).whole());
   private final net.agartha.client.module.a_pkg.j sRotZ = this.addSetting(new net.agartha.client.module.a_pkg.j("Rotate Z", 0.0F, -180.0F, 180.0F).whole());
   private final net.agartha.client.module.a_pkg.b sOffHand = this.addSetting(new net.agartha.client.module.a_pkg.b("Off Hand", true));
   private final net.agartha.client.module.a_pkg.b sMirrorOffHand = this.addSetting(new net.agartha.client.module.a_pkg.b("Mirror Off Hand", true));

   public u() {
      super("View Model", "Adjusts where your hands and held items sit on screen.", net.agartha.client.module.c.MISC);
      this.sMirrorOffHand.visibleWhen(this.sOffHand::get);
   }

   public boolean appliesTo(boolean bl) {
      return bl || this.sOffHand.get();
   }

   public void applyTransform(MatrixStack matrixStack, boolean bl) {
      float var3 = !bl && this.sMirrorOffHand.get() ? -1.0F : 1.0F;
      matrixStack.translate(this.sX.get() / 100.0F * var3, this.sY.get() / 100.0F, this.sZ.get() / 100.0F);
      float var4 = (float)Math.toRadians(this.sRotX.get());
      float var5 = (float)Math.toRadians(this.sRotY.get() * var3);
      float var6 = (float)Math.toRadians(this.sRotZ.get() * var3);
      if (var4 != 0.0F || var5 != 0.0F || var6 != 0.0F) {
         matrixStack.multiply(new Quaternionf().rotateXYZ(var4, var5, var6));
      }

      float var7 = this.sScale.get() / 100.0F;
      if (var7 != 1.0F) {
         matrixStack.scale(var7, var7, var7);
      }
   }
}
