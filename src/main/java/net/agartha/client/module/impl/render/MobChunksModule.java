package net.agartha.client.module.impl.render;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.EndClientTickEvent;
import net.agartha.client.event.impl.Render3DEvent;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ColorSetting;
import net.agartha.client.module.setting.EntityTypeListSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.util.color.ColorSettingWiring;
import net.agartha.client.util.render.FadeTracker;
import net.agartha.client.util.render.FrustumCulling;
import net.agartha.client.util.render.LabelStacker;
import net.agartha.client.util.render.PlateRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public final class MobChunksModule extends Module {
   private static final int RED = -50373;
   private static final float LINE_WIDTH = 1.5F;
   private static final double PLATE_HEIGHT = 0.35;
   private static final double PLATE_HALF = 8.0;
   private final BooleanSetting sCustomColor = (BooleanSetting)this.addSetting(
      new BooleanSetting("Custom Color", true)
   );
   private final ColorSetting sColor = (ColorSetting)this.addSetting(new ColorSetting("Color", -50373));
   private final BooleanSetting sThemeColor = (BooleanSetting)this.addSetting(
      new BooleanSetting("Theme Color", false)
   );
   private final NumberSetting sPlateY = (NumberSetting)this.addSetting(new NumberSetting("Plate Y", 60.0F, -64.0F, 320.0F).whole());
   private final NumberSetting sMinY = (NumberSetting)this.addSetting(new NumberSetting("Min Y", 0.0F, -64.0F, 320.0F).whole());
   private final NumberSetting sMaxY = (NumberSetting)this.addSetting(new NumberSetting("Max Y", 16.0F, -64.0F, 320.0F).whole());
   private final NumberSetting sMinMobs = (NumberSetting)this.addSetting(new NumberSetting("Min Mobs", 1.0F, 1.0F, 32.0F).whole());
   private final EntityTypeListSetting sMobs = (EntityTypeListSetting)this.addSetting(new EntityTypeListSetting("Mobs"));
   private final Map<Long, Integer> counts = new HashMap<>();
   private final Set<Long> marked = new HashSet<>();
   private int scanTick;
   private final FadeTracker fade = new FadeTracker(0.7F, 0.5F);
   private final FadeTracker.a fadingPlate = this::drawPlate;
   private int frameColor;

   public MobChunksModule() {
      super("Mob Chunks", "Marks chunks that have mobs.", ModuleCategory.DONUT);
      ColorSettingWiring.wireExclusiveColorSource(this.sCustomColor, this.sThemeColor, this.sColor, false);
   }

   protected void onEnable() {
      this.clear();
   }

   protected void onDisable() {
      this.clear();
   }

   private void clear() {
      this.counts.clear();
      this.marked.clear();
      this.scanTick = 4;
   }

   @EventListener
   public void onEndClientTick(EndClientTickEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      ClientWorld var3 = var2.world;
      if (var3 != null && var2.player != null && var3.getRegistryKey() == World.OVERWORLD) {
         if (++this.scanTick >= 5) {
            this.scanTick = 0;
            this.counts.clear();
            this.marked.clear();
            ChunkPos var4 = var2.player.getChunkPos();
            int var5 = var2.options.getClampedViewDistance();
            int var6 = this.sMinY.getInt();
            int var7 = this.sMaxY.getInt();
            if (var6 > var7) {
               int var8 = var6;
               var6 = var7;
               var7 = var8;
            }

            int var15 = var6;
            int var9 = var7;

            for (Entity var11 : var3.getEntities()) {
               if (var11 instanceof LivingEntity && !(var11 instanceof PlayerEntity) && var11.isAlive()) {
                  int var12 = var11.getBlockY();
                  if (var12 >= var15 && var12 <= var9) {
                     int var13 = var11.getBlockX() >> 4;
                     int var14 = var11.getBlockZ() >> 4;
                     if (Math.abs(var13 - var4.x) <= var5 && Math.abs(var14 - var4.z) <= var5 && this.sMobs.allows(var11.getType())) {
                        this.counts.merge(ChunkPos.toLong(var13, var14), 1, Integer::sum);
                     }
                  }
               }
            }

            int var16 = Math.max(1, this.sMinMobs.getInt());

            for (Entry var18 : this.counts.entrySet()) {
               if ((Integer)var18.getValue() >= var16) {
                  this.marked.add((Long)var18.getKey());
               }
            }
         }
      } else {
         this.counts.clear();
         this.marked.clear();
      }
   }

   @EventListener
   public void onRender3D(Render3DEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null) {
         this.frameColor = ColorSettingWiring.argb(this.sThemeColor, this.sCustomColor, this.sColor, -50373);
         this.fade.begin();
         int var3 = var2.options.getClampedViewDistance();
         ChunkPos var4 = var2.player.getChunkPos();

         for (long var6 : this.marked) {
            if (Math.max(Math.abs(ChunkPos.getPackedX(var6) - var4.x), Math.abs(ChunkPos.getPackedZ(var6) - var4.z)) <= var3) {
               this.drawPlate(var6, this.fade.show(var6));
            }
         }

         this.fade.sweep(this.fadingPlate);
      }
   }

   private void drawPlate(long l, float f) {
      if (!(f <= 0.01F)) {
         double var4 = (ChunkPos.getPackedX(l) << 4) + 8.0;
         double var6 = (ChunkPos.getPackedZ(l) << 4) + 8.0;
         double var8 = 8.0 * FadeTracker.pop(f);
         double var10 = LabelStacker.y(3, l, var4 - 8.0, this.sPlateY.get(), var6 - 8.0, var4 + 8.0, var6 + 8.0, 0.35) + FadeTracker.rise(f);
         Box var12 = new Box(var4 - var8, var10, var6 - var8, var4 + var8, var10 + 0.35, var6 + var8);
         if (FrustumCulling.isSafeCuboid(var12)) {
            int var13 = scaleAlpha(this.frameColor, f);
            int var14 = scaleAlpha(this.frameColor & 16777215 | 1073741824, f);
            PlateRenderer.plate(3, var12, var13, 1.5F, var14);
         }
      }
   }

   private static int scaleAlpha(int i, float f) {
      int var2 = Math.round((i >>> 24 & 0xFF) * f);
      return var2 << 24 | i & 16777215;
   }
}
