package net.agartha.client.util.b_pkg;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;

@Environment(EnvType.CLIENT)
public final class b {
   private b() {
   }

   public static boolean isMeleeWeapon(ItemStack itemStack) {
      return !itemStack.isEmpty() && (itemStack.isIn(ItemTags.SWORDS) || itemStack.isIn(ItemTags.AXES) || itemStack.isIn(ItemTags.SPEARS));
   }
}
