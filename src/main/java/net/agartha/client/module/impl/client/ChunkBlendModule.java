package net.agartha.client.module.impl.client;

import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.module.e;
import net.agartha.client.module.a_pkg.j;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ChunkBlendModule extends b {
   private final net.agartha.client.module.a_pkg.b sMerge = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Join Neighbours", true)
   );
   private final j sRadius = (j)this.addSetting(new j("Corner Radius", 3.0F, 0.0F, 8.0F));
   private final j sDetail = (j)this.addSetting(new j("Corner Detail", 6.0F, 2.0F, 12.0F).whole());
   private final net.agartha.client.module.a_pkg.b sWalls = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Side Walls", true));
   private final j sShade = (j)this.addSetting(new j("Wall Shade", 0.75F, 0.3F, 1.0F));
   private final net.agartha.client.module.a_pkg.b sOutline = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Outline", true));

   public ChunkBlendModule() {
      super("Chunk Blend", "Joins neighbouring chunk plates into one shape with rounded corners.", c.CLIENT);
      this.sShade.visibleWhen(this.sWalls::get);
   }

   public static ChunkBlendModule active() {
      ChunkBlendModule var0 = (ChunkBlendModule)e.get(ChunkBlendModule.class);
      return var0 != null && var0.isEnabled() ? var0 : null;
   }

   public boolean merge() {
      return this.sMerge.get();
   }

   public double radius() {
      return this.sRadius.get();
   }

   public int detail() {
      return Math.max(2, this.sDetail.getInt());
   }

   public boolean walls() {
      return this.sWalls.get();
   }

   public float shade() {
      return this.sShade.get();
   }

   public boolean outline() {
      return this.sOutline.get();
   }
}
