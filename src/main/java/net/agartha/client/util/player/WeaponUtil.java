package net.agartha.client.util.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;

@Environment(EnvType.CLIENT)
public final class WeaponUtil {
   private WeaponUtil() {
   }

   public static boolean isMeleeWeapon(ItemStack itemStack) {
      return !itemStack.isEmpty() && (itemStack.isIn(ItemTags.SWORDS) || itemStack.isIn(ItemTags.AXES) || itemStack.isIn(ItemTags.SPEARS));
   }
}
