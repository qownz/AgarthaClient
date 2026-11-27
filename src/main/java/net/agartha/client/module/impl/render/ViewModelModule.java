package net.agartha.client.module.impl.render;

import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public final class ViewModelModule extends Module {
   private final NumberSetting sX = this.addSetting(new NumberSetting("X", 0.0F, -100.0F, 100.0F).whole());
   private final NumberSetting sY = this.addSetting(new NumberSetting("Y", 0.0F, -100.0F, 100.0F).whole());
   private final NumberSetting sZ = this.addSetting(new NumberSetting("Z", 0.0F, -100.0F, 100.0F).whole());
   private final NumberSetting sScale = this.addSetting(new NumberSetting("Scale", 100.0F, 10.0F, 300.0F).whole());
   private final NumberSetting sRotX = this.addSetting(new NumberSetting("Rotate X", 0.0F, -180.0F, 180.0F).whole());
   private final NumberSetting sRotY = this.addSetting(new NumberSetting("Rotate Y", 0.0F, -180.0F, 180.0F).whole());
   private final NumberSetting sRotZ = this.addSetting(new NumberSetting("Rotate Z", 0.0F, -180.0F, 180.0F).whole());
   private final BooleanSetting sOffHand = this.addSetting(new BooleanSetting("Off Hand", true));
   private final BooleanSetting sMirrorOffHand = this.addSetting(new BooleanSetting("Mirror Off Hand", true));

   public ViewModelModule() {
      super("View Model", "Adjusts where your hands and held items sit on screen.", ModuleCategory.MISC);
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
