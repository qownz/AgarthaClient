package net.agartha.client.e;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderState.Glint;
import net.minecraft.client.render.item.ItemRenderState.LayerRenderState;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class ac extends ItemRenderState {
   public final List<Object> identity = new ArrayList<>();
   private final List<LayerRenderState> layers = new ArrayList<>();

   public void clear() {
      super.clear();
      this.identity.clear();
      this.layers.clear();
   }

   public LayerRenderState newLayer() {
      LayerRenderState var1 = super.newLayer();
      this.layers.add(var1);
      return var1;
   }

   public void addModelKey(Object modelKey) {
      if (!(modelKey instanceof Glint)) {
         this.identity.add(modelKey);
      }
   }

   public boolean resolve(ItemStack itemStack) {
      MinecraftClient var2 = MinecraftClient.getInstance();

      try {
         var2.getItemModelManager().clearAndUpdate(this, itemStack, ItemDisplayContext.GUI, var2.world, var2.player, 0);
      } catch (RuntimeException var5) {
         this.clear();
         return false;
      }

      for (LayerRenderState var4 : this.layers) {
         var4.setGlint(Glint.NONE);
      }

      if (this.identity.isEmpty()) {
         this.identity.add(itemStack.getItem());
      }

      return true;
   }
}
