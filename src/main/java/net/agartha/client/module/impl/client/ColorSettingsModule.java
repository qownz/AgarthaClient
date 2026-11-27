package net.agartha.client.module.impl.client;

import net.agartha.client.b_pkg.a;
import net.agartha.client.module.b;
import net.agartha.client.module.a_pkg.c;
import net.agartha.client.module.a_pkg.j;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class ColorSettingsModule extends b {
   private final j hue = (j)this.addSetting(new j("Hue", 0.0F, -180.0F, 180.0F));
   private final j saturation = (j)this.addSetting(new j("Saturation", 1.0F, 0.0F, 2.0F));
   private final j brightness = (j)this.addSetting(new j("Brightness", 1.0F, 0.0F, 2.0F));
   private final j contrast = (j)this.addSetting(new j("Contrast", 1.0F, 0.0F, 2.0F));
   private final c tint = (c)this.addSetting(new c("Tint Color", -6595329));
   private final j tintStrength = (j)this.addSetting(new j("Tint Strength", 0.0F, 0.0F, 1.0F));

   public ColorSettingsModule() {
      super("Color Settings", "Adjust world color with hue, saturation, brightness, and contrast.", net.agartha.client.module.c.CLIENT);
      this.hue.onChange(a::markDirty);
      this.saturation.onChange(a::markDirty);
      this.brightness.onChange(a::markDirty);
      this.contrast.onChange(a::markDirty);
      this.tint.onChange(a::markDirty);
      this.tintStrength.onChange(a::markDirty);
      this.tint.visibleWhen(() -> this.tintStrength.get() > 0.0F);
      net.agartha.client.d.a_pkg.c.TYPE.listen(this, arg -> a.tick(MinecraftClient.getInstance()));
   }

   protected void onDisable() {
      a.clearIfActive(MinecraftClient.getInstance());
      a.markDirty();
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
