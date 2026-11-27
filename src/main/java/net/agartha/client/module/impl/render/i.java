package net.agartha.client.module.impl.render;

import imgui.ImDrawList;
import net.agartha.client.util.ad;
import net.agartha.client.util.af;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public final class i extends net.agartha.client.module.b implements net.agartha.client.module.f {
   private static final float TRACER_WIDTH = 1.5F;
   private final net.agartha.client.module.a_pkg.b sOutline = this.addSetting(new net.agartha.client.module.a_pkg.b("Outline", true));
   private final net.agartha.client.module.a_pkg.b sTracers = this.addSetting(new net.agartha.client.module.a_pkg.b("Tracers", false));
   private final net.agartha.client.module.a_pkg.d sItemColors = this.addSetting(net.agartha.client.module.a_pkg.d.createDefault());

   public i() {
      super("Item ESP", "Highlights selected dropped items with model outlines and tracers.", net.agartha.client.module.c.RENDER);
   }

   public static boolean shouldGlow(Entity entity) {
      i var1 = net.agartha.client.module.e.get(i.class);
      return var1 != null && var1.a(entity);
   }

   public static boolean isGlowPathActive() {
      i var0 = net.agartha.client.module.e.get(i.class);
      return var0 != null && var0.isEnabled() && var0.sOutline.value().get() && var0.sItemColors != null && var0.sItemColors.hasAnyEnabled();
   }

   public static int glowColor(Entity entity) {
      i var1 = net.agartha.client.module.e.get(i.class);
      return var1 != null && entity instanceof ItemEntity var2 ? var1.sItemColors.colorArgb(var2.getStack().getItem()) & 16777215 : 16777215;
   }

   @Override
   public boolean isTracerOverlayEnabled() {
      return this.isEnabled() && this.sTracers.value().get() && this.sItemColors != null && this.sItemColors.hasAnyEnabled();
   }

   @Override
   public void renderTracerOverlay(ImDrawList imDrawList, float f, float g) {
      if (this.isTracerOverlayEnabled() && imDrawList != null) {
         MinecraftClient var4 = MinecraftClient.getInstance();
         if (var4.world != null && var4.player != null) {
            if (this.sItemColors != null && this.sItemColors.hasAnyEnabled()) {
               float var5 = var4.getRenderTickCounter().getTickProgress(false);
               float var6 = (float)af.blocks();
               float var7 = 1.5F;
               float var8 = ad.originX(f);
               float var9 = ad.originY("Crosshair", g);
               Vec3d var10 = var4.player.getCameraPosVec(var5);
               double var11 = (double)var6 * var6;

               for (Entity var14 : var4.world.getEntities()) {
                  if (var14 instanceof ItemEntity var15 && !var15.isRemoved()) {
                     ItemStack var16 = var15.getStack();
                     Item var17 = var16.getItem();
                     if (this.sItemColors.isEnabled(var17) && this.a(var15, 1.0F, var6)) {
                        Vec3d var18 = var15.getLerpedPos(var5);
                        int var19 = this.sItemColors.colorArgb(var17);
                        if (var19 >>> 24 != 0) {
                           ad.drawWithFade(imDrawList, var4, f, g, var8, var9, var18, var19, var7, var10, var6);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean a(Entity entity) {
      if (!this.isEnabled() || !this.sOutline.value().get()) {
         return false;
      } else if (entity instanceof ItemEntity var2) {
         if (var2.isRemoved()) {
            return false;
         } else if (this.sItemColors != null && this.sItemColors.hasAnyEnabled()) {
            MinecraftClient var3 = MinecraftClient.getInstance();
            if (var3.world != null && var3.getEntityRenderDispatcher().getRenderer(entity) != null) {
               ItemStack var4 = var2.getStack();
               Item var5 = var4.getItem();
               return !this.sItemColors.isEnabled(var5) ? false : this.a(var2, 1.0F, (float)af.blocks());
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean a(ItemEntity itemEntity, float f, float g) {
      MinecraftClient var4 = MinecraftClient.getInstance();
      if (var4.player == null) {
         return true;
      } else {
         Vec3d var5 = var4.player.getCameraPosVec(f);
         return itemEntity.getLerpedPos(f).squaredDistanceTo(var5) <= (double)g * g && net.agartha.client.util.i.drawnByGame(itemEntity);
      }
   }
}
