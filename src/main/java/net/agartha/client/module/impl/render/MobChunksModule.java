package net.agartha.client.module.impl.render;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.agartha.client.d.f;
import net.agartha.client.d.a_pkg.o;
import net.agartha.client.module.b;
import net.agartha.client.module.a_pkg.c;
import net.agartha.client.module.a_pkg.h;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.util.a_o;
import net.agartha.client.util.ac;
import net.agartha.client.util.l;
import net.agartha.client.util.w;
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
public final class MobChunksModule extends b {
   private static final int RED = -50373;
   private static final float LINE_WIDTH = 1.5F;
   private static final double PLATE_HEIGHT = 0.35;
   private static final double PLATE_HALF = 8.0;
   private final net.agartha.client.module.a_pkg.b sCustomColor = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Custom Color", true)
   );
   private final c sColor = (c)this.addSetting(new c("Color", -50373));
   private final net.agartha.client.module.a_pkg.b sThemeColor = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Theme Color", false)
   );
   private final j sPlateY = (j)this.addSetting(new j("Plate Y", 60.0F, -64.0F, 320.0F).whole());
   private final j sMinY = (j)this.addSetting(new j("Min Y", 0.0F, -64.0F, 320.0F).whole());
   private final j sMaxY = (j)this.addSetting(new j("Max Y", 16.0F, -64.0F, 320.0F).whole());
   private final j sMinMobs = (j)this.addSetting(new j("Min Mobs", 1.0F, 1.0F, 32.0F).whole());
   private final h sMobs = (h)this.addSetting(new h("Mobs"));
   private final Map<Long, Integer> counts = new HashMap<>();
   private final Set<Long> marked = new HashSet<>();
   private int scanTick;
   private final a_o fade = new a_o(0.7F, 0.5F);
   private final net.agartha.client.util.a_o.a fadingPlate = this::drawPlate;
   private int frameColor;

   public MobChunksModule() {
      super("Mob Chunks", "Marks chunks that have mobs.", net.agartha.client.module.c.DONUT);
      ac.wireExclusiveColorSource(this.sCustomColor, this.sThemeColor, this.sColor, false);
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

   @f
   public void onEndClientTick(net.agartha.client.d.a_pkg.c arg) {
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

   @f
   public void onRender3D(o arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null) {
         this.frameColor = ac.argb(this.sThemeColor, this.sCustomColor, this.sColor, -50373);
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
         double var8 = 8.0 * a_o.pop(f);
         double var10 = net.agartha.client.util.o.y(3, l, var4 - 8.0, this.sPlateY.get(), var6 - 8.0, var4 + 8.0, var6 + 8.0, 0.35) + a_o.rise(f);
         Box var12 = new Box(var4 - var8, var10, var6 - var8, var4 + var8, var10 + 0.35, var6 + var8);
         if (net.agartha.client.util.l.isSafeCuboid(var12)) {
            int var13 = scaleAlpha(this.frameColor, f);
            int var14 = scaleAlpha(this.frameColor & 16777215 | 1073741824, f);
            w.plate(3, var12, var13, 1.5F, var14);
         }
      }
   }

   private static int scaleAlpha(int i, float f) {
      int var2 = Math.round((i >>> 24 & 0xFF) * f);
      return var2 << 24 | i & 16777215;
   }
}
