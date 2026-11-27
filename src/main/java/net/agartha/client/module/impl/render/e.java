package net.agartha.client.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class e extends net.agartha.client.module.b {
   private final net.agartha.client.module.a_pkg.j gamma = this.addSetting(new net.agartha.client.module.a_pkg.j("Gamma", 8.0F, 1.0F, 16.0F));
   private final net.agartha.client.module.a_pkg.b smooth = this.addSetting(new net.agartha.client.module.a_pkg.b("Smooth", true));
   private static final float FADE_SECONDS = 0.4F;
   private float shown;
   private long shownAt;

   public e() {
      super("Fullbright", "Lights up dark areas so you can see without torches.", net.agartha.client.module.c.RENDER);
   }

   public float gamma() {
      return Math.max(1.0F, this.gamma.get());
   }

   public float blend(float f) {
      long var2 = System.nanoTime();
      float var4 = this.shownAt == 0L ? 0.0F : Math.min((float)(var2 - this.shownAt) / 1.0E9F, 0.25F);
      this.shownAt = var2;
      float var5 = this.smooth.get() ? var4 / 0.4F : 1.0F;
      this.shown = this.isEnabled() ? Math.min(1.0F, this.shown + var5) : Math.max(0.0F, this.shown - var5);
      if (this.shown <= 0.0F) {
         return f;
      } else {
         float var6 = this.shown * this.shown * (3.0F - 2.0F * this.shown);
         return f + (this.gamma() - f) * var6;
      }
   }
}
