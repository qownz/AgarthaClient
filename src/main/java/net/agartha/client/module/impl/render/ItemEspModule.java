package net.agartha.client.module.impl.render;

import imgui.ImDrawList;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.TracerOverlay;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ItemColorSetting;
import net.agartha.client.util.render.EntityVisibility;
import net.agartha.client.util.render.OffscreenIndicator;
import net.agartha.client.util.world.ViewDistance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public final class ItemEspModule extends Module implements TracerOverlay {
   private static final float TRACER_WIDTH = 1.5F;
   private final BooleanSetting sOutline = this.addSetting(new BooleanSetting("Outline", true));
   private final BooleanSetting sTracers = this.addSetting(new BooleanSetting("Tracers", false));
   private final ItemColorSetting sItemColors = this.addSetting(ItemColorSetting.createDefault());

   public ItemEspModule() {
      super("Item ESP", "Highlights selected dropped items with model outlines and tracers.", ModuleCategory.RENDER);
   }

   public static boolean shouldGlow(Entity entity) {
      ItemEspModule var1 = ModuleManager.get(ItemEspModule.class);
      return var1 != null && var1.a(entity);
   }

   public static boolean isGlowPathActive() {
      ItemEspModule var0 = ModuleManager.get(ItemEspModule.class);
      return var0 != null && var0.isEnabled() && var0.sOutline.value().get() && var0.sItemColors != null && var0.sItemColors.hasAnyEnabled();
   }

   public static int glowColor(Entity entity) {
      ItemEspModule var1 = ModuleManager.get(ItemEspModule.class);
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
               float var6 = (float)ViewDistance.blocks();
               float var7 = 1.5F;
               float var8 = OffscreenIndicator.originX(f);
               float var9 = OffscreenIndicator.originY("Crosshair", g);
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
                           OffscreenIndicator.drawWithFade(imDrawList, var4, f, g, var8, var9, var18, var19, var7, var10, var6);
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
               return !this.sItemColors.isEnabled(var5) ? false : this.a(var2, 1.0F, (float)ViewDistance.blocks());
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
         return itemEntity.getLerpedPos(f).squaredDistanceTo(var5) <= (double)g * g && EntityVisibility.drawnByGame(itemEntity);
      }
   }
}
