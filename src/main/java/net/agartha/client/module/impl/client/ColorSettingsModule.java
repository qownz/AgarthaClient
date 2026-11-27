package net.agartha.client.module.impl.client;
import net.agartha.client.module.setting.ColorSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.render.ColorEffectController;
import net.agartha.client.event.impl.EndClientTickEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class ColorSettingsModule extends Module {
   private final NumberSetting hue = (NumberSetting)this.addSetting(new NumberSetting("Hue", 0.0F, -180.0F, 180.0F));
   private final NumberSetting saturation = (NumberSetting)this.addSetting(new NumberSetting("Saturation", 1.0F, 0.0F, 2.0F));
   private final NumberSetting brightness = (NumberSetting)this.addSetting(new NumberSetting("Brightness", 1.0F, 0.0F, 2.0F));
   private final NumberSetting contrast = (NumberSetting)this.addSetting(new NumberSetting("Contrast", 1.0F, 0.0F, 2.0F));
   private final ColorSetting tint = (ColorSetting)this.addSetting(new ColorSetting("Tint Color", -6595329));
   private final NumberSetting tintStrength = (NumberSetting)this.addSetting(new NumberSetting("Tint Strength", 0.0F, 0.0F, 1.0F));

   public ColorSettingsModule() {
      super("Color Settings", "Adjust world color with hue, saturation, brightness, and contrast.", ModuleCategory.CLIENT);
      this.hue.onChange(ColorEffectController::markDirty);
      this.saturation.onChange(ColorEffectController::markDirty);
      this.brightness.onChange(ColorEffectController::markDirty);
      this.contrast.onChange(ColorEffectController::markDirty);
      this.tint.onChange(ColorEffectController::markDirty);
      this.tintStrength.onChange(ColorEffectController::markDirty);
      this.tint.visibleWhen(() -> this.tintStrength.get() > 0.0F);
      EndClientTickEvent.TYPE.listen(this, arg -> ColorEffectController.tick(MinecraftClient.getInstance()));
   }

   protected void onDisable() {
      ColorEffectController.clearIfActive(MinecraftClient.getInstance());
      ColorEffectController.markDirty();
   }

   public float hue() {
      return this.hue.get();
   }

   public float saturation() {
      return this.saturation.get();
   }

   public float brightness() {
      return this.brightness.get();
   }

   public float contrast() {
      return this.contrast.get();
   }

   public int tintRgb() {
      return this.tint.rgb();
   }

   public float tintStrength() {
      return this.tintStrength.get();
   }
}
