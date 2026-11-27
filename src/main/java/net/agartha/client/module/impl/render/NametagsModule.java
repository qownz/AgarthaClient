package net.agartha.client.module.impl.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.Render2DEvent;
import net.agartha.client.mixin.AccessorGameRenderer;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.misc.NameProtectModule;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.util.render.OffscreenIndicator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3x2fStack;

@Environment(EnvType.CLIENT)
public class NametagsModule extends Module {
   private static final int PANEL_RGB = 1842206;
   private static final int BORDER_COLOR = 1442840575;
   private static final int HIGHLIGHT = 419430399;
   private static final int SHADOW_COLOR = 1426063360;
   private static final int NAME_COLOR = -1;
   private static final int HEALTH_GOOD = -13318311;
   private static final int HEALTH_WARN = -13312;
   private static final int HEALTH_BAD = -50384;
   private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
   private static final int PADDING = 4;
   private static final int ICON_SIZE = 10;
   private static final int ICON_GAP = 2;
   private static final float ICON_SCALE = 0.625F;
   private static final int ITEM_NAME_COLOR = -1;
   private static final int ITEM_COUNT_COLOR = -10496;
   private final BooleanSetting sPing = this.addSetting(new BooleanSetting("Ping", true));
   private final BooleanSetting sArmor = this.addSetting(new BooleanSetting("Armor", true));
   private final BooleanSetting sHealth = this.addSetting(new BooleanSetting("Health", true));
   private final BooleanSetting sHideVanilla = this.addSetting(new BooleanSetting("Hide Vanilla", true));
   private final BooleanSetting sItems = this.addSetting(new BooleanSetting("Items", true));
   private final NumberSetting sScale = this.addSetting(new NumberSetting("Scale", 1.0F, 0.5F, 2.0F));
   private final NumberSetting sBackgroundAlpha = this.addSetting(new NumberSetting("Background Alpha", 0.45F, 0.0F, 1.0F));
   private final List<AbstractClientPlayerEntity> tagTargets = new ArrayList<>();
   private final List<ItemStack> wornScratch = new ArrayList<>(4);
   private double sortEyeX;
   private double sortEyeY;
   private double sortEyeZ;
   private final Comparator<AbstractClientPlayerEntity> farFirst = (abstractClientPlayerEntity, abstractClientPlayerEntity2) -> Double.compare(
      abstractClientPlayerEntity2.squaredDistanceTo(this.sortEyeX, this.sortEyeY, this.sortEyeZ),
      abstractClientPlayerEntity.squaredDistanceTo(this.sortEyeX, this.sortEyeY, this.sortEyeZ)
   );

   public NametagsModule() {
      super("Nametags", "Crisp player nametags with ping, armor and health.", ModuleCategory.RENDER);
   }

   public static boolean hideVanillaNametags() {
      NametagsModule var0 = ModuleManager.get(NametagsModule.class);
      return var0 != null && var0.isEnabled() && var0.sHideVanilla.value().get();
   }

   @EventListener
   public void onRender2D(Render2DEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null && !var2.options.hudHidden) {
         Camera var3 = var2.gameRenderer.getCamera();
         if (var3 != null && var3.isReady()) {
            Vec3d var4 = var2.player.getCameraPosVec(arg.partialTick);
            List var5 = this.tagTargets;
            var5.clear();

            for (AbstractClientPlayerEntity var7 : var2.world.getPlayers()) {
               if (var7 != var2.player && !var7.isRemoved()) {
                  var5.add(var7);
               }
            }

            this.sortEyeX = var4.x;
            this.sortEyeY = var4.y;
            this.sortEyeZ = var4.z;
            var5.sort(this.farFirst);

            for (AbstractClientPlayerEntity var11 : (Iterable<AbstractClientPlayerEntity>) var5) {
               this.a(arg.graphics, var2, var3, var11, arg.partialTick);
            }

            if (this.sItems.value().get()) {
               for (Entity var12 : var2.world.getEntities()) {
                  if (var12 instanceof ItemEntity var8 && !var8.isRemoved()) {
                     this.a(arg.graphics, var2, var3, var8, arg.partialTick);
                  }
               }
            }
         }
      }
   }

   private void a(DrawContext drawContext, MinecraftClient minecraftClient, Camera camera, AbstractClientPlayerEntity abstractClientPlayerEntity, float f) {
      Vec3d var6 = abstractClientPlayerEntity.getLerpedPos(f).add(0.0, abstractClientPlayerEntity.getHeight() + 0.15, 0.0);
      if (OffscreenIndicator.isInFront(camera, var6)) {
         Vec3d var7 = minecraftClient.gameRenderer.project(var6);
         if (Double.isFinite(var7.x) && Double.isFinite(var7.y)) {
            float var8 = (float)(var7.x + 1.0) * 0.5F * drawContext.getScaledWindowWidth();
            float var9 = (float)(1.0 - var7.y) * 0.5F * drawContext.getScaledWindowHeight();
            if (!(var8 < -128.0F)
               && !(var8 > drawContext.getScaledWindowWidth() + 128)
               && !(var9 < -64.0F)
               && !(var9 > drawContext.getScaledWindowHeight() + 64)) {
               TextRenderer var10 = minecraftClient.textRenderer;
               String var11 = abstractClientPlayerEntity.getGameProfile().name();
               NameProtectModule var12 = ModuleManager.get(NameProtectModule.class);
               if (var12 != null) {
                  var11 = var12.protect(var11);
               }

               String var13 = null;
               int var14 = -5197648;
               if (this.sPing.value().get() && minecraftClient.getNetworkHandler() != null) {
                  PlayerListEntry var15 = minecraftClient.getNetworkHandler().getPlayerListEntry(abstractClientPlayerEntity.getUuid());
                  if (var15 != null) {
                     int var16 = var15.getLatency();
                     var13 = var16 + "ms";
                     var14 = var16 < 60 ? -13318311 : (var16 < 130 ? -13312 : -50384);
                  }
               }

               List var41 = List.of();
               if (this.sArmor.value().get()) {
                  List var42 = this.wornScratch;
                  var42.clear();

                  for (EquipmentSlot var20 : ARMOR_SLOTS) {
                     ItemStack var21 = abstractClientPlayerEntity.getEquippedStack(var20);
                     if (!var21.isEmpty()) {
                        var42.add(var21);
                     }
                  }

                  var41 = var42;
               }

               String var43 = null;
               int var44 = -13318311;
               if (this.sHealth.value().get()) {
                  float var45 = abstractClientPlayerEntity.getHealth() + abstractClientPlayerEntity.getAbsorptionAmount();
                  float var47 = Math.min(1.0F, var45 / Math.max(1.0F, abstractClientPlayerEntity.getMaxHealth()));
                  var43 = String.valueOf((int)Math.ceil(var45));
                  var44 = var47 > 0.6F ? -13318311 : (var47 > 0.3F ? -13312 : -50384);
               }

               double var46 = var6.distanceTo(camera.getCameraPos());
               String var48 = Math.round((float)var46) + "m";
               int var49 = var10.getWidth(var11) + (var13 != null ? 5 + var10.getWidth(var13) : 0);
               int var22 = 0;
               if (!var41.isEmpty()) {
                  var22 = var41.size() * 12 - 2;
               }

               if (var43 != null) {
                  var22 += (var22 > 0 ? 4 : 0) + var10.getWidth(var43);
               }

               var22 += (var22 > 0 ? 4 : 0) + var10.getWidth(var48);
               boolean var23 = var22 > 0;
               int var24 = Math.max(var49, var22);
               int var25 = var24 + 8;
               int var26 = 8 + 9 + (var23 ? 13 : 0);
               float var27 = Math.max(0.5F, Math.min(2.0F, this.sScale.value()[0]));
               float var28 = (float)Math.min(1.0, Math.max(0.3, 8.0 / Math.max(1.0, var46)));
               float var29 = var27 * var28 * a(minecraftClient, camera, f);
               Matrix3x2fStack var30 = drawContext.getMatrices();
               var30.pushMatrix();
               var30.translate(var8, var9);
               var30.scale(var29, var29);
               int var31 = -var25 / 2;
               int var32 = -var26 - 1;
               this.a(drawContext, var31, var32, var31 + var25, var32 + var26);
               int var33 = var31 + 4;
               int var34 = var32 + 4;
               drawContext.drawText(var10, var11, var33, var34, -1, true);
               if (var13 != null) {
                  drawContext.drawText(var10, var13, var33 + var10.getWidth(var11) + 5, var34, var14, true);
               }

               if (var23) {
                  int var35 = var32 + 4 + 9 + 3;
                  int var36 = var35 + (10 - 9) / 2 + 1;
                  int var37 = var31 + 4 + (var24 - var22) / 2;
                  int var38 = var37;

                  for (ItemStack var40 : (Iterable<ItemStack>) var41) {
                     var30.pushMatrix();
                     var30.translate(var38, var35);
                     var30.scale(0.625F, 0.625F);
                     drawContext.drawItem(var40, 0, 0);
                     var30.popMatrix();
                     var38 += 12;
                  }

                  if (var43 != null) {
                     if (var38 > var37) {
                        var38 += 2;
                     }

                     drawContext.drawText(var10, var43, var38, var36, var44, true);
                     var38 += var10.getWidth(var43);
                  }

                  int var51 = var38 + (var38 > var37 ? 4 : 0);
                  drawContext.drawText(var10, var48, var51, var36, -5592406, true);
               }

               var30.popMatrix();
            }
         }
      }
   }

   private void a(DrawContext drawContext, MinecraftClient minecraftClient, Camera camera, ItemEntity itemEntity, float f) {
      Vec3d var6 = itemEntity.getLerpedPos(f).add(0.0, itemEntity.getHeight() + 0.12, 0.0);
      if (OffscreenIndicator.isInFront(camera, var6)) {
         Vec3d var7 = minecraftClient.gameRenderer.project(var6);
         if (Double.isFinite(var7.x) && Double.isFinite(var7.y)) {
            float var8 = (float)(var7.x + 1.0) * 0.5F * drawContext.getScaledWindowWidth();
            float var9 = (float)(1.0 - var7.y) * 0.5F * drawContext.getScaledWindowHeight();
            if (!(var8 < -128.0F)
               && !(var8 > drawContext.getScaledWindowWidth() + 128)
               && !(var9 < -64.0F)
               && !(var9 > drawContext.getScaledWindowHeight() + 64)) {
               ItemStack var10 = itemEntity.getStack();
               String var11 = var10.getName().getString();
               int var12 = var10.getCount();
               String var13 = var12 > 1 ? "x" + var12 : null;
               TextRenderer var14 = minecraftClient.textRenderer;
               int var15 = var14.getWidth(var11);
               int var16 = var13 != null ? var14.getWidth(var13) : 0;
               int var17 = var15 + (var13 != null ? 3 + var16 : 0);
               double var18 = var6.distanceTo(camera.getCameraPos());
               float var20 = (float)Math.min(1.0, Math.max(0.25, 6.0 / Math.max(1.0, var18))) * a(minecraftClient, camera, f);
               Matrix3x2fStack var21 = drawContext.getMatrices();
               var21.pushMatrix();
               var21.translate(var8, var9);
               var21.scale(var20, var20);
               int var22 = -var17 / 2;
               int var23 = -9 - 2;
               drawContext.drawText(var14, var11, var22, var23, -1, true);
               if (var13 != null) {
                  drawContext.drawText(var14, var13, var22 + var15 + 3, var23, -10496, true);
               }

               var21.popMatrix();
            }
         }
      }
   }

   private static float a(MinecraftClient minecraftClient, Camera camera, float f) {
      float var3 = ((AccessorGameRenderer)minecraftClient.gameRenderer).agartha$invokeGetFov(camera, f, true);
      float var4 = ((Integer)minecraftClient.options.getFov().getValue()).intValue();
      return var4 / Math.max(10.0F, var3);
   }

   private void a(DrawContext drawContext, int i, int j, int k, int l) {
      int var6 = Math.round(Math.max(0.0F, Math.min(1.0F, this.sBackgroundAlpha.value()[0])) * 255.0F);
      int var7 = var6 << 24 | 1842206;
      a(drawContext, i + 1, j + 2, k + 1, l + 2, 1426063360);
      a(drawContext, i, j, k, l, var7);
      drawContext.fill(i + 4, j + 1, k - 4, j + 2, 419430399);
      drawContext.fill(i + 2, j + 2, k - 2, j + 3, 419430399);
      b(drawContext, i, j, k, l, 1442840575);
   }

   private static void a(DrawContext drawContext, int i, int j, int k, int l, int m) {
      drawContext.fill(i + 4, j, k - 4, j + 1, m);
      drawContext.fill(i + 2, j + 1, k - 2, j + 2, m);
      drawContext.fill(i + 1, j + 2, k - 1, j + 4, m);
      drawContext.fill(i, j + 4, k, l - 4, m);
      drawContext.fill(i + 1, l - 4, k - 1, l - 2, m);
      drawContext.fill(i + 2, l - 2, k - 2, l - 1, m);
      drawContext.fill(i + 4, l - 1, k - 4, l, m);
   }

   private static void b(DrawContext drawContext, int i, int j, int k, int l, int m) {
      drawContext.fill(i + 4, j, k - 4, j + 1, m);
      drawContext.fill(i + 4, l - 1, k - 4, l, m);
      drawContext.fill(i, j + 4, i + 1, l - 4, m);
      drawContext.fill(k - 1, j + 4, k, l - 4, m);
      drawContext.fill(i + 2, j + 1, i + 4, j + 2, m);
      drawContext.fill(i + 1, j + 2, i + 2, j + 4, m);
      drawContext.fill(k - 4, j + 1, k - 2, j + 2, m);
      drawContext.fill(k - 2, j + 2, k - 1, j + 4, m);
      drawContext.fill(i + 1, l - 4, i + 2, l - 2, m);
      drawContext.fill(i + 2, l - 2, i + 4, l - 1, m);
      drawContext.fill(k - 2, l - 4, k - 1, l - 2, m);
      drawContext.fill(k - 4, l - 2, k - 2, l - 1, m);
   }
}
