package net.agartha.client.module.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.agartha.client.util.ac;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class f extends net.agartha.client.module.b {
   public static volatile f active;
   private static final float HIGHLIGHT_LIFT = 0.45F;
   private static final long PULSE_NANOS = 2800000000L;
   private static final long SWEEP_NANOS = 5200000000L;
   private final net.agartha.client.module.a_pkg.b customColor = this.addSetting(new net.agartha.client.module.a_pkg.b("Custom Color", false));
   private final net.agartha.client.module.a_pkg.c color = this.addSetting(new net.agartha.client.module.a_pkg.c("Color", -8585260));
   private final net.agartha.client.module.a_pkg.b themeColor = this.addSetting(new net.agartha.client.module.a_pkg.b("Theme Color", true));
   private final net.agartha.client.module.a_pkg.j strength = this.addSetting(new net.agartha.client.module.a_pkg.j("Strength", 50.0F, 0.0F, 100.0F));
   private final net.agartha.client.module.a_pkg.j spread = this.addSetting(new net.agartha.client.module.a_pkg.j("Spread", 5.0F, 1.0F, 16.0F));
   private final net.agartha.client.module.a_pkg.j edge = this.addSetting(new net.agartha.client.module.a_pkg.j("Edge", 25.0F, 0.0F, 100.0F));
   private final net.agartha.client.module.a_pkg.b pulse = this.addSetting(new net.agartha.client.module.a_pkg.b("Pulse", true));

   public f() {
      super("Hand Overlay", "Colours your hand and the item you hold, with a soft edge.", net.agartha.client.module.c.RENDER);
      ac.wireExclusiveColorSource(this.customColor, this.themeColor, this.color, true);
   }

   public void applyShippedDefaults() {
      if (this.strength.get() == 55.0F) {
         this.strength.set(50.0F);
      }
   }

   @Override
   protected void onEnable() {
      active = this;
   }

   @Override
   protected void onDisable() {
      active = null;
      if (RenderSystem.isOnRenderThread()) {
         g.release();
      } else {
         MinecraftClient.getInstance().execute(f::a);
      }
   }

   private static void a() {
      if (active == null) {
         g.release();
      }
   }

   public static boolean beginCapture() {
      f var0 = active;
      return var0 != null && var0.b() && g.begin();
   }

   public static void endCapture(boolean bl) {
      if (g.end() && bl) {
         f var1 = active;
         float var2 = Math.max(1.0F, MinecraftClient.getInstance().getFramebuffer().textureHeight / 1080.0F);
         if (var1 == null) {
            g.compose(-1, 0.0F, 2.0F, 0.0F, 2.0F * var2, 0.0F);
         } else {
            int var3 = ac.argb(var1.themeColor, var1.customColor, var1.color, -1);
            float var4 = (float)(System.nanoTime() % 5200000000L / 5.2E9);
            g.compose(var3, var1.c(), var1.spread.get() * var2, var1.edge.get() / 100.0F, 1.6F * var2, var4);
         }
      }
   }

   private boolean b() {
      return this.strength.get() > 0.0F || this.edge.get() > 0.0F;
   }

   private float c() {
      float var1 = this.strength.get() / 100.0F;
      if (!this.pulse.get()) {
         return var1;
      } else {
         double var2 = System.nanoTime() % 2800000000L / 2.8E9;
         float var4 = (float)Math.sin(var2 * Math.PI);
         return var1 * (0.72F + 0.28F * var4 * var4);
      }
   }

   static int highlight(int i) {
      int var1 = i >> 16 & 0xFF;
      int var2 = i >> 8 & 0xFF;
      int var3 = i & 0xFF;
      var1 += Math.round((255 - var1) * 0.45F);
      var2 += Math.round((255 - var2) * 0.45F);
      var3 += Math.round((255 - var3) * 0.45F);
      return 0xFF000000 | var1 << 16 | var2 << 8 | var3;
   }
}
