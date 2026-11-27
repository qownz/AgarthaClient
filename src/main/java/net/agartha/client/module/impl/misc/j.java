package net.agartha.client.module.impl.misc;

import net.agartha.client.util.ab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public final class j extends net.agartha.client.module.b {
   private final net.agartha.client.module.a_pkg.j sRenderDistance = this.addSetting(new net.agartha.client.module.a_pkg.j("Render Distance", 2.0F, 2.0F, 32.0F));
   private final net.agartha.client.module.a_pkg.j sSimDistance = this.addSetting(new net.agartha.client.module.a_pkg.j("Sim Distance", 5.0F, 5.0F, 32.0F));
   private final net.agartha.client.module.a_pkg.b sLock = this.addSetting(new net.agartha.client.module.a_pkg.b("Lock", true));
   private final net.agartha.client.module.a_pkg.b sNotify = this.addSetting(new net.agartha.client.module.a_pkg.b("Notify", true));
   private int savedRenderDistance = 12;
   private int savedSimDistance = 12;
   private static final int MIN_SIM_DISTANCE = 5;
   private static final int MIN_RENDER_DISTANCE = 2;

   public j() {
      super("Chunk Debug", "Drops render/sim distance for base packing. Restores originals on disable.", net.agartha.client.module.c.RENDER);
   }

   @Override
   protected void onEnable() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.options != null) {
         this.savedRenderDistance = (Integer)var1.options.getViewDistance().getValue();
         this.savedSimDistance = (Integer)var1.options.getSimulationDistance().getValue();
         this.a(var1);
         this.a(var1, true);
      }
   }

   @Override
   protected void onDisable() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.options != null) {
         var1.options.getViewDistance().setValue(this.savedRenderDistance);
         var1.options.getSimulationDistance().setValue(this.savedSimDistance);
         this.a(var1, false);
      }
   }

   @net.agartha.client.d.f
   public void onTick(net.agartha.client.d.a_pkg.q arg) {
      if (this.sLock.get()) {
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (var2.options != null) {
            int var3 = Math.max(2, (int)this.sRenderDistance.get());
            int var4 = Math.max(5, (int)this.sSimDistance.get());
            if ((Integer)var2.options.getViewDistance().getValue() != var3 || (Integer)var2.options.getSimulationDistance().getValue() != var4) {
               this.a(var2);
            }
         }
      }
   }

   private void a(MinecraftClient minecraftClient) {
      int var2 = Math.max(2, (int)this.sRenderDistance.get());
      int var3 = Math.max(5, (int)this.sSimDistance.get());
      minecraftClient.options.getViewDistance().setValue(var2);
      minecraftClient.options.getSimulationDistance().setValue(var3);
   }

   private void a(MinecraftClient minecraftClient, boolean bl) {
      if (this.sNotify.get()) {
         if (bl) {
            int var3 = b(minecraftClient);
            ab.send("Chunk Debug", "Enabled — RD: " + (int)this.sRenderDistance.get() + " | SD: " + (int)this.sSimDistance.get() + " | Entities: " + var3);
         } else {
            ab.send("Chunk Debug", "Disabled — RD restored to " + this.savedRenderDistance);
         }
      }
   }

   private static int b(MinecraftClient minecraftClient) {
      if (minecraftClient.world == null) {
         return 0;
      } else {
         int var1 = 0;

         for (Entity var3 : minecraftClient.world.getEntities()) {
            var1++;
         }

         return var1;
      }
   }
}
