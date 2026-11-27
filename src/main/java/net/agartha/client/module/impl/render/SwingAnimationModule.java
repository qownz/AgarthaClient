package net.agartha.client.module.impl.render;

import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public final class SwingAnimationModule extends Module {
   private final EnumSetting sType = this.addSetting(new EnumSetting("Type", "Smooth 1", "Smooth 1", "Smooth 2"));
   private final NumberSetting sStrength = this.addSetting(new NumberSetting("Strength", 1.0F, 0.5F, 3.0F));
   private final BooleanSetting sOnlySwing = this.addSetting(new BooleanSetting("Only Swing", false));

   public SwingAnimationModule() {
      super(
         "Swing Animation",
         "Restyles the first-person swing of the held item — the item only changes angle, with no hand drift.",
         ModuleCategory.MISC
      );
   }

   public boolean shouldApply(Arm arm) {
      if (!this.isEnabled()) {
         return false;
      } else {
         ClientPlayerEntity var2 = MinecraftClient.getInstance().player;
         return var2 != null && arm == var2.getMainArm();
      }
   }

   public void apply(MatrixStack matrixStack, Arm arm, float f) {
      int var4 = arm == Arm.RIGHT ? 1 : -1;
      float var5 = this.sStrength.get();
      float var6 = MathHelper.sin(f * f * (float) Math.PI);
      float var7 = MathHelper.sin(MathHelper.sqrt(f) * (float) Math.PI);
      if ("Smooth 2".equals(this.sType.get())) {
         matrixStack.translate(0.0F, 0.1F, 0.0F);
         matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(var4 * (45.0F + var6 * -20.0F * var5)));
         matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(var4 * var7 * -20.0F * var5));
         matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(var7 * -80.0F * var5));
         matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(var4 * -45.0F));
         matrixStack.translate(0.0F, -0.1F, 0.0F);
      } else {
         matrixStack.translate(0.0F, 0.1F, 0.0F);
         matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(var7 * -80.0F * var5));
         matrixStack.translate(0.0F, -0.1F, 0.0F);
      }
   }

   public boolean skipWhileIdle(float f) {
      return this.sOnlySwing.get() && f <= 0.001F;
   }
}
