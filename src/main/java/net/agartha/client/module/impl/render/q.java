package net.agartha.client.module.impl.render;

import imgui.ImDrawList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.agartha.client.util.ad;
import net.agartha.client.util.af;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

@Environment(EnvType.CLIENT)
public final class q extends net.agartha.client.module.b implements net.agartha.client.module.f {
   private final net.agartha.client.module.a_pkg.i sShape = this.addSetting(new net.agartha.client.module.a_pkg.i("Shape", "Glow", "Glow", "Box", "Fill", "Both"));
   private final net.agartha.client.module.a_pkg.c sEnemyColor = this.addSetting(new net.agartha.client.module.a_pkg.c("Enemy Color", -52429));
   private final net.agartha.client.module.a_pkg.c sFriendColor = this.addSetting(new net.agartha.client.module.a_pkg.c("Friend Color", -13369498));
   private final net.agartha.client.module.a_pkg.j sFillAlpha = this.addSetting(new net.agartha.client.module.a_pkg.j("Fill Alpha", 0.25F, 0.0F, 1.0F));
   private final net.agartha.client.module.a_pkg.j sLineWidth = this.addSetting(new net.agartha.client.module.a_pkg.j("Line Width", 1.5F, 0.5F, 10.0F));
   private final net.agartha.client.module.a_pkg.b sTracers = this.addSetting(new net.agartha.client.module.a_pkg.b("Tracers", true));
   private final net.agartha.client.module.a_pkg.j sTracerWidth = this.addSetting(new net.agartha.client.module.a_pkg.j("Tracer Width", 2.5F, 0.5F, 10.0F));
   private final net.agartha.client.module.a_pkg.j sMaxPlayers = this.addSetting(new net.agartha.client.module.a_pkg.j("Max Players", 256.0F, 1.0F, 256.0F).whole());
   private final net.agartha.client.module.a_pkg.l sFriends = this.addSetting(new net.agartha.client.module.a_pkg.l("Friends", "", 512));
   private final Set<String> friends = new HashSet<>();
   private String friendsCache;
   private final List<AbstractClientPlayerEntity> candidates = new ArrayList<>();
   private int[] colors = new int[32];

   public q() {
      super("Player ESP", "Marks players only — Friends in the Friend color, everyone else in the Enemy color.", net.agartha.client.module.c.RENDER);
      this.sFillAlpha.visibleWhen(() -> this.sShape.is("Fill") || this.sShape.is("Both"));
      this.sLineWidth.visibleWhen(() -> !this.sShape.is("Glow") && !this.sShape.is("Fill"));
      this.sTracerWidth.visibleWhen(this.sTracers::get);
      this.sFriends.visibleWhen(() -> false);
   }

   @Override
   protected void onDisable() {
      this.candidates.clear();
   }

   private Set<String> a() {
      String var1 = this.sFriends.get();
      if (!var1.equals(this.friendsCache)) {
         this.friendsCache = var1;
         this.friends.clear();

         for (String var5 : var1.split("[,\\s]+")) {
            if (!var5.isBlank()) {
               this.friends.add(var5.trim().toLowerCase(Locale.ROOT));
            }
         }
      }

      return this.friends;
   }

   public List<String> friendNames() {
      ArrayList var1 = new ArrayList();
      HashSet var2 = new HashSet();

      for (String var6 : this.sFriends.get().split("[,\\s]+")) {
         String var7 = var6.trim();
         if (!var7.isEmpty() && var2.add(var7.toLowerCase(Locale.ROOT))) {
            var1.add(var7);
         }
      }

      return var1;
   }

   public boolean isFriendName(String string) {
      return string != null && this.a().contains(string.trim().toLowerCase(Locale.ROOT));
   }

   public boolean addFriend(String string) {
      String var2 = string == null ? "" : string.trim();
      if (var2.matches("[A-Za-z0-9_]{1,16}") && !this.isFriendName(var2)) {
         List var3 = this.friendNames();
         var3.add(var2);
         this.a(var3);
         return true;
      } else {
         return false;
      }
   }

   public void removeFriend(String string) {
      List var2 = this.friendNames();
      if (var2.removeIf(string2 -> ((String)string2).equalsIgnoreCase(string))) {
         this.a(var2);
      }
   }

   private void a(List<String> list) {
      this.sFriends.set(String.join(", ", list));
      net.agartha.client.c.e.getInstance().save();
   }

   private boolean a(AbstractClientPlayerEntity abstractClientPlayerEntity) {
      Set var2 = this.a();
      return !var2.isEmpty() && var2.contains(abstractClientPlayerEntity.getGameProfile().name().toLowerCase(Locale.ROOT));
   }

   private int b(AbstractClientPlayerEntity abstractClientPlayerEntity) {
      return this.a(abstractClientPlayerEntity) ? this.sFriendColor.argb() : this.sEnemyColor.argb();
   }

   private void a(MinecraftClient minecraftClient, float f) {
      this.candidates.clear();
      if (minecraftClient.world != null && minecraftClient.player != null) {
         Vec3d var3 = minecraftClient.player.getCameraPosVec(f);

         for (AbstractClientPlayerEntity var5 : minecraftClient.world.getPlayers()) {
            if (var5 != minecraftClient.player && !var5.isRemoved() && a(var5, minecraftClient)) {
               this.candidates.add(var5);
            }
         }

         int var6 = this.sShape.is("Glow") ? Integer.MAX_VALUE : Math.max(1, this.sMaxPlayers.getInt());
         if (this.candidates.size() > var6) {
            this.candidates.sort(Comparator.comparingDouble(abstractClientPlayerEntity -> abstractClientPlayerEntity.getEntityPos().squaredDistanceTo(var3)));
            this.candidates.subList(var6, this.candidates.size()).clear();
         }

         if (this.colors.length < this.candidates.size()) {
            this.colors = new int[Math.max(this.candidates.size(), this.colors.length * 2)];
         }

         for (int var7 = 0; var7 < this.candidates.size(); var7++) {
            this.colors[var7] = this.b(this.candidates.get(var7));
         }
      }
   }

   @net.agartha.client.d.f
   public void onRender3D(net.agartha.client.d.a_pkg.o arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      this.a(var2, arg.partialTick);
      if (!this.sShape.is("Glow") && !this.candidates.isEmpty()) {
         float var3 = this.sLineWidth.get();
         int var4 = Math.round(this.sFillAlpha.get() * 255.0F);
         String var5 = this.sShape.get();

         for (int var6 = 0; var6 < this.candidates.size(); var6++) {
            AbstractClientPlayerEntity var7 = this.candidates.get(var6);
            Box var8 = a(var7, arg.partialTick);
            if (net.agartha.client.util.l.isSafeCuboid(var8)) {
               int var9 = this.colors[var6];
               int var10 = var9 & 16777215 | Math.max(0, Math.min(255, var4)) << 24;

               DrawStyle var11 = switch (var5) {
                  case "Fill" -> DrawStyle.filled(var10);
                  case "Both" -> DrawStyle.filledAndStroked(var9, var3, var10);
                  default -> DrawStyle.stroked(var9, var3);
               };
               GizmoDrawing.box(var8, var11).ignoreOcclusion();
            }
         }
      }
   }

   public static boolean isGlowPathActive() {
      q var0 = net.agartha.client.module.e.get(q.class);
      return var0 != null && var0.isEnabled() && var0.sShape.is("Glow");
   }

   public static boolean shouldGlow(Entity entity) {
      if (entity instanceof AbstractClientPlayerEntity var1) {
         q var2 = net.agartha.client.module.e.get(q.class);
         if (var2 != null && var2.isEnabled() && var2.sShape.is("Glow")) {
            MinecraftClient var3 = MinecraftClient.getInstance();
            return var3.player != null && var1 != var3.player && !var1.isRemoved() ? a(var1, var3) : false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean a(AbstractClientPlayerEntity abstractClientPlayerEntity, MinecraftClient minecraftClient) {
      double var2 = af.blocks();
      double var4 = abstractClientPlayerEntity.getX() - minecraftClient.player.getX();
      double var6 = abstractClientPlayerEntity.getY() - minecraftClient.player.getEyeY();
      double var8 = abstractClientPlayerEntity.getZ() - minecraftClient.player.getZ();
      return var4 * var4 + var6 * var6 + var8 * var8 <= var2 * var2 && net.agartha.client.util.i.drawnByGame(abstractClientPlayerEntity);
   }

   public static int glowColor(Entity entity) {
      q var1 = net.agartha.client.module.e.get(q.class);
      return var1 != null && entity instanceof AbstractClientPlayerEntity var2 ? var1.b(var2) & 16777215 : 16777215;
   }

   @Override
   public boolean isTracerOverlayEnabled() {
      return this.isEnabled() && this.sTracers.get() && !this.candidates.isEmpty();
   }

   @Override
   public void renderTracerOverlay(ImDrawList imDrawList, float f, float g) {
      if (imDrawList != null && this.isTracerOverlayEnabled()) {
         MinecraftClient var4 = MinecraftClient.getInstance();
         if (var4.world != null && var4.player != null) {
            float var5 = var4.getRenderTickCounter().getTickProgress(false);
            float var6 = ad.originX(f);
            float var7 = ad.originY("Crosshair", g);
            Vec3d var8 = var4.player.getCameraPosVec(var5);
            float var9 = (float)af.blocks();
            float var10 = this.sTracerWidth.get();

            for (int var11 = 0; var11 < this.candidates.size(); var11++) {
               AbstractClientPlayerEntity var12 = this.candidates.get(var11);
               if (!var12.isRemoved()) {
                  Vec3d var13 = var12.getLerpedPos(var5);
                  Vec3d var14 = new Vec3d(var13.x, var13.y + var12.getHeight() * 0.5, var13.z);
                  ad.drawWithFade(imDrawList, var4, f, g, var6, var7, var14, this.colors[var11], var10, var8, var9);
               }
            }
         }
      }
   }

   private static Box a(AbstractClientPlayerEntity abstractClientPlayerEntity, float f) {
      Vec3d var2 = abstractClientPlayerEntity.getEntityPos();
      Vec3d var3 = abstractClientPlayerEntity.getLerpedPos(f);
      return abstractClientPlayerEntity.getBoundingBox().offset(var3.x - var2.x, var3.y - var2.y, var3.z - var2.z);
   }
}
