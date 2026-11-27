package net.agartha.client.e;

import imgui.ImDrawList;
import imgui.ImFont;
import net.agartha.client.module.impl.client.HudModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class f {
   private static final float ICON = 26.0F;
   private static final float GAP = 4.0F;
   private static final float PAD = 5.0F;
   private static final float TEXT_SIZE = 13.0F;
   private static final float TEXT_GAP = 2.0F;
   private static final float RADIUS = 5.0F;
   private static final float CUT = 6.0F;
   private static final EquipmentSlot[] ARMOR = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
   private static final String[] PERCENTS = new String[101];
   private static final String WIDEST = "100%";

   private f() {
   }

   private static float a(float f) {
      return c.textWidth(net.agartha.client.f.a.getInstance().hudValueFont(), 13.0F * f, "100%");
   }

   public static f.a measure(HudModule hudModule, float f) {
      int var2 = ARMOR.length;
      float var3 = 26.0F * f;
      float var4 = 5.0F * f * 2.0F;
      float var5 = 4.0F * f;
      boolean var6 = hudModule.armorPercent();
      if (hudModule.armorVertical()) {
         float var9 = var3 + (var6 ? 2.0F * f * 2.0F + a(f) : 0.0F);
         return new f.a(var9 + var4, var3 * var2 + var5 * (var2 - 1) + var4);
      } else {
         float var7 = var6 ? Math.max(var3, a(f)) : var3;
         float var8 = var3 + (var6 ? 2.0F * f + 13.0F * f : 0.0F);
         return new f.a(var7 * var2 + var5 * (var2 - 1) + var4, var8 + var4);
      }
   }

   public static void render(ImDrawList imDrawList, HudModule hudModule, t.c arg, float f, float g, float h) {
      float var6 = c.clamp01(h);
      if (!(var6 <= 0.002F)) {
         ClientPlayerEntity var7 = MinecraftClient.getInstance().player;
         float var8 = v.scaleOf(arg);
         float var9 = arg.x + f;
         float var10 = arg.y + g;
         if (hudModule.armorBox()) {
            c.card(imDrawList, var9, var10, arg.width, arg.height, 5.0F * var8, 6.0F * var8, var6, false);
         }

         boolean var11 = hudModule.armorVertical();
         boolean var12 = hudModule.armorPercent();
         float var13 = 26.0F * var8;
         float var14 = 4.0F * var8;
         float var15 = var11 ? var13 : (var12 ? Math.max(var13, a(var8)) : var13);
         float var16 = var9 + 5.0F * var8;
         float var17 = var10 + 5.0F * var8;

         for (EquipmentSlot var21 : ARMOR) {
            ItemStack var22 = var7 == null ? ItemStack.EMPTY : var7.getEquippedStack(var21);
            float var23 = var16 + (var15 - var13) * 0.5F;
            if (!var22.isEmpty() || hudModule.armorBox()) {
               if (var22.isEmpty()) {
                  float var24 = 4.0F * var8;
                  imDrawList.addRectFilled(
                     var23 + var24, var17 + var24, var23 + var13 - var24, var17 + var13 - var24, c.f(c.alpha(352321535, var6)), 3.0F * var8
                  );
               } else {
                  ab.draw(imDrawList, var22, var23, var17, var13, var6);
                  if (var12) {
                     a(imDrawList, var22, var11, var16, var17, var15, var13, var8, var6);
                  }
               }
            }

            if (var11) {
               var17 += var13 + var14;
            } else {
               var16 += var15 + var14;
            }
         }
      }
   }

   private static void a(ImDrawList imDrawList, ItemStack itemStack, boolean bl, float f, float g, float h, float i, float j, float k) {
      if (itemStack.isDamageable() && itemStack.getMaxDamage() > 0) {
         int var9 = Math.round(100.0F * (itemStack.getMaxDamage() - itemStack.getDamage()) / itemStack.getMaxDamage());
         String var10 = PERCENTS[Math.max(0, Math.min(100, var9))];
         ImFont var11 = net.agartha.client.f.a.getInstance().hudValueFont();
         float var12 = 13.0F * j;
         float var13 = c.textWidth(var11, var12, var10);
         int var14 = c.alpha(-526345, k);
         if (bl) {
            c.text(imDrawList, var11, var12, f + i + 2.0F * j * 2.0F, c.midTop(var11, var12, g + i * 0.5F), var14, var10);
         } else {
            float var15 = g + i + 2.0F * j;
            c.text(imDrawList, var11, var12, f + (h - var13) * 0.5F, c.midTop(var11, var12, var15 + var12 * 0.5F), var14, var10);
         }
      }
   }

   static {
      for (int var0 = 0; var0 <= 100; var0++) {
         PERCENTS[var0] = var0 + "%";
      }
   }

   @Environment(EnvType.CLIENT)
   public record a(float width, float height) {
   }
}
