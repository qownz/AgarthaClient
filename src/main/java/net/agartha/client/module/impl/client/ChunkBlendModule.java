package net.agartha.client.module.impl.client;

import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ChunkBlendModule extends Module {
   private final BooleanSetting sMerge = (BooleanSetting)this.addSetting(
      new BooleanSetting("Join Neighbours", true)
   );
   private final NumberSetting sRadius = (NumberSetting)this.addSetting(new NumberSetting("Corner Radius", 3.0F, 0.0F, 8.0F));
   private final NumberSetting sDetail = (NumberSetting)this.addSetting(new NumberSetting("Corner Detail", 6.0F, 2.0F, 12.0F).whole());
   private final BooleanSetting sWalls = (BooleanSetting)this.addSetting(new BooleanSetting("Side Walls", true));
   private final NumberSetting sShade = (NumberSetting)this.addSetting(new NumberSetting("Wall Shade", 0.75F, 0.3F, 1.0F));
   private final BooleanSetting sOutline = (BooleanSetting)this.addSetting(new BooleanSetting("Outline", true));

   public ChunkBlendModule() {
      super("Chunk Blend", "Joins neighbouring chunk plates into one shape with rounded corners.", ModuleCategory.CLIENT);
      this.sShade.visibleWhen(this.sWalls::get);
   }

   public static ChunkBlendModule active() {
      ChunkBlendModule var0 = (ChunkBlendModule)ModuleManager.get(ChunkBlendModule.class);
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
