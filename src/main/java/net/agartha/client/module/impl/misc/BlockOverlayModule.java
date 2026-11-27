package net.agartha.client.module.impl.misc;

import java.util.ArrayList;
import java.util.List;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.Render3DEvent;
import net.agartha.client.mixin.AccessorGameRenderer;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.BooleanSetting;
import net.agartha.client.module.setting.ColorSetting;
import net.agartha.client.module.setting.EnumSetting;
import net.agartha.client.module.setting.NumberSetting;
import net.agartha.client.render.effect.EffectSprites;
import net.agartha.client.render.effect.GizmoCanvas;
import net.agartha.client.util.color.ColorSettingWiring;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

@Environment(EnvType.CLIENT)
public final class BlockOverlayModule extends Module {
   private static final String STYLE_OUTLINE = "Outline";
   private static final String STYLE_FILL = "Fill";
   private static final float FADE_IN = 0.12F;
   private static final float FADE_OUT = 0.2F;
   private static final double SNAP_DISTANCE = 6.0;
   private static final float COLOR_SPEED = 9.0F;
   private static final double FILL_INFLATE = 0.002;
   private static final double LINE_LIFT = 0.004;
   private static final double GLOW_LIFT = 0.002;
   private final EnumSetting style = this.addSetting(new EnumSetting("Style", "Outline", "Outline", "Fill"));
   private final BooleanSetting customColor = this.addSetting(new BooleanSetting("Custom Color", true));
   private final ColorSetting color = this.addSetting(new ColorSetting("Color", -16777216));
   private final BooleanSetting themeColor = this.addSetting(new BooleanSetting("Theme Color", false));
   private final NumberSetting lineWidth = this.addSetting(new NumberSetting("Line Width", 5.0F, 0.5F, 10.0F));
   private final NumberSetting fillOpacity = this.addSetting(new NumberSetting("Fill Opacity", 20.0F, 2.0F, 80.0F));
   private final BooleanSetting glow = this.addSetting(new BooleanSetting("Glow", true));
   private final BooleanSetting glints = this.addSetting(new BooleanSetting("Corner Glints", true));
   private final BooleanSetting smooth = this.addSetting(new BooleanSetting("Smooth Movement", true));
   private final NumberSetting smoothSpeed = this.addSetting(new NumberSetting("Smoothing Speed", 50.0F, 0.0F, 100.0F).whole());
   private static final float FOLLOW_PER_POINT = 0.7F;
   private final BooleanSetting faceOnly = this.addSetting(new BooleanSetting("Face Only", false));
   private final BooleanSetting seeThrough = this.addSetting(new BooleanSetting("See Through", false));
   private final GizmoCanvas canvas = new GizmoCanvas();
   private final List<double[]> edges = new ArrayList<>();
   private final double[] faceEdges = new double[24];
   private ClientWorld level;
   private BlockPos pos;
   private BlockState state;
   private BlockPos partner;
   private BlockState partnerState;
   private List<Box> boxes = List.of();
   private Box localBounds;
   private final double[] goal = new double[6];
   private final double[] box = new double[6];
   private boolean hasBox;
   private Direction face;
   private Box faceBox;
   private float presence;
   private int shown;
   private boolean hasShown;
   private long lastFrame;
   private double time;
   private float fade;
   private Vec3d eye;
   private double ax;
   private double bx;
   private double ay;
   private double by;
   private double az;
   private double bz;
   private static final int[] CORNER_ORDER = new int[]{0, 1, 4, 5, 3, 2, 7, 6};
   private static final Direction[] DIRECTIONS = Direction.values();

   public BlockOverlayModule() {
      super(
         "Block Overlay",
         "A smooth outline, or outline and fill, on the block you look at: glides between blocks and fades in and out.",
         ModuleCategory.RENDER
      );
      ColorSettingWiring.wireExclusiveColorSource(this.customColor, this.themeColor, this.color);
      this.fillOpacity.visibleWhen(() -> this.style.is("Fill"));
      this.smoothSpeed.visibleWhen(this.smooth::get);
   }

   public boolean replacesVanillaOutline() {
      return this.isEnabled();
   }

   @Override
   protected void onDisable() {
      this.b();
   }

   @EventListener
   public void onRender3D(Render3DEvent arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null) {
         if (var2.world != this.level) {
            this.b();
            this.level = var2.world;
         }

         long var3 = System.nanoTime();
         float var5 = this.lastFrame == 0L ? 0.0F : Math.min((float)(var3 - this.lastFrame) / 1.0E9F, 0.1F);
         this.lastFrame = var3;
         BlockHitResult var6 = a(var2);
         if (var6 != null && this.a(var2, var6)) {
            boolean var7 = !this.hasBox || this.presence < 0.02F || !this.smooth.get() || this.smoothSpeed.get() <= 0.0F || this.a();
            if (var7) {
               System.arraycopy(this.goal, 0, this.box, 0, 6);
               this.hasBox = true;
            } else {
               float var8 = 1.0F - (float)Math.exp(-this.smoothSpeed.get() * 0.7F * var5);

               for (int var9 = 0; var9 < 6; var9++) {
                  this.box[var9] = this.box[var9] + (this.goal[var9] - this.box[var9]) * var8;
               }
            }

            this.presence = Math.min(1.0F, this.presence + var5 / 0.12F);
         } else {
            var6 = null;
            this.presence = Math.max(0.0F, this.presence - var5 / 0.2F);
            if (this.presence == 0.0F) {
               this.b();
               this.level = var2.world;
               return;
            }
         }

         if (this.hasBox && this.pos != null) {
            this.a(var5);
            this.eye = var2.gameRenderer.getCamera().getCameraPos();
            this.time = var3 % 3600000000000L / 1.0E9;
            this.canvas.begin(var2.gameRenderer.getCamera());
            this.c();
            this.canvas.submit(this.seeThrough.get());
         }
      } else {
         this.b();
      }
   }

   private boolean a() {
      double var1 = (this.box[0] + this.box[3] - this.goal[0] - this.goal[3]) * 0.5;
      double var3 = (this.box[1] + this.box[4] - this.goal[1] - this.goal[4]) * 0.5;
      double var5 = (this.box[2] + this.box[5] - this.goal[2] - this.goal[5]) * 0.5;
      return var1 * var1 + var3 * var3 + var5 * var5 > 36.0;
   }

   private static BlockHitResult a(MinecraftClient minecraftClient) {
      if (!(minecraftClient.crosshairTarget instanceof BlockHitResult var1 && var1.getType() == Type.BLOCK && !var1.isInsideBlock())) {
         return null;
      } else if (!((AccessorGameRenderer)minecraftClient.gameRenderer).agartha$shouldRenderBlockOutline()) {
         return null;
      } else {
         BlockPos var3 = var1.getBlockPos();
         return !minecraftClient.world.getBlockState(var3).isAir() && minecraftClient.world.getWorldBorder().contains(var3) ? var1 : null;
      }
   }

   private boolean a(MinecraftClient minecraftClient, BlockHitResult blockHitResult) {
      ClientWorld var3 = minecraftClient.world;
      BlockPos var4 = blockHitResult.getBlockPos();
      BlockPos var5 = a(var3, var4, var3.getBlockState(var4));
      BlockPos var6 = var5 != null && var5.compareTo(var4) < 0 ? var5 : var4;
      BlockPos var7 = var5 == null ? null : (var6.equals(var4) ? var5 : var4);
      BlockState var8 = var3.getBlockState(var6);
      BlockState var9 = var7 == null ? null : var3.getBlockState(var7);
      if (!var6.equals(this.pos) || var8 != this.state || var9 != this.partnerState) {
         Entity var10 = minecraftClient.getCameraEntity();
         ShapeContext var11 = var10 == null ? ShapeContext.absent() : ShapeContext.of(var10);
         VoxelShape var12 = var8.getOutlineShape(var3, var6, var11);
         if (var7 != null) {
            var12 = VoxelShapes.union(var12, var9.getOutlineShape(var3, var7, var11).offset(var7.subtract(var6)));
         }

         var12 = var12.simplify();
         if (var12.isEmpty()) {
            return false;
         }

         this.pos = var6;
         this.state = var8;
         this.partner = var7;
         this.partnerState = var9;
         this.edges.clear();
         var12.forEachEdge((d, e, f, g, h, i) -> this.edges.add(new double[]{d, e, f, g, h, i}));
         this.boxes = var12.getBoundingBoxes();
         this.localBounds = var12.getBoundingBox();
         this.goal[0] = this.localBounds.minX + var6.getX();
         this.goal[1] = this.localBounds.minY + var6.getY();
         this.goal[2] = this.localBounds.minZ + var6.getZ();
         this.goal[3] = this.localBounds.maxX + var6.getX();
         this.goal[4] = this.localBounds.maxY + var6.getY();
         this.goal[5] = this.localBounds.maxZ + var6.getZ();
      }

      this.face = blockHitResult.getSide();
      Vec3d var13 = blockHitResult.getPos();
      this.faceBox = this.a(var13.x - this.pos.getX(), var13.y - this.pos.getY(), var13.z - this.pos.getZ());
      return true;
   }

   private static BlockPos a(ClientWorld clientWorld, BlockPos blockPos, BlockState blockState) {
      BlockPos var3 = null;
      if (blockState.contains(Properties.DOUBLE_BLOCK_HALF)) {
         var3 = blockState.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? blockPos.up() : blockPos.down();
      } else if (blockState.getBlock() instanceof BedBlock) {
         var3 = blockPos.offset(BedBlock.getOppositePartDirection(blockState));
      } else if (blockState.getBlock() instanceof ChestBlock && blockState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
         var3 = ChestBlock.getPosInFrontOf(blockPos, blockState);
      }

      return var3 != null && clientWorld.getBlockState(var3).isOf(blockState.getBlock()) ? var3 : null;
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private Box a(double d, double e, double f) {
      double var7 = 0.001;

      for (Box var10 : this.boxes) {
         boolean var11 = switch (this.face) {
            case EAST -> Math.abs(d - var10.maxX) < var7;
            case WEST -> Math.abs(d - var10.minX) < var7;
            case UP -> Math.abs(e - var10.maxY) < var7;
            case DOWN -> Math.abs(e - var10.minY) < var7;
            case SOUTH -> Math.abs(f - var10.maxZ) < var7;
            case NORTH -> Math.abs(f - var10.minZ) < var7;
            default -> throw new MatchException(null, null);
         };
         if (var11
            && d >= var10.minX - var7
            && d <= var10.maxX + var7
            && e >= var10.minY - var7
            && e <= var10.maxY + var7
            && f >= var10.minZ - var7
            && f <= var10.maxZ + var7) {
            return var10;
         }
      }

      return this.localBounds;
   }

   private void a(float f) {
      int var2 = ColorSettingWiring.argb(this.themeColor, this.customColor, this.color, -16777216);
      if (!this.hasShown) {
         this.shown = var2;
         this.hasShown = true;
      } else if (this.shown != var2) {
         this.shown = ColorHelper.lerp(1.0F - (float)Math.exp(-9.0F * f), this.shown, var2);
      }
   }

   private void b() {
      this.level = null;
      this.pos = null;
      this.state = null;
      this.partner = null;
      this.partnerState = null;
      this.edges.clear();
      this.boxes = List.of();
      this.localBounds = null;
      this.hasBox = false;
      this.face = null;
      this.faceBox = null;
      this.presence = 0.0F;
      this.lastFrame = 0L;
   }

   private void c() {
      this.fade = this.presence * this.presence * (3.0F - 2.0F * this.presence);
      double var1 = 0.94 + 0.06 * MathHelper.clamp(this.presence * this.presence * (3.0F - 2.0F * this.presence), 0.0F, 1.0F);
      this.a(0, this.pos.getX() - this.goal[0], var1);
      this.a(1, this.pos.getY() - this.goal[1], var1);
      this.a(2, this.pos.getZ() - this.goal[2], var1);
      boolean var3 = this.style.is("Fill");
      float var4 = this.fillOpacity.get() / 100.0F;
      if (var3) {
         if (this.faceOnly.get()) {
            this.a(this.faceBox, this.face, var4, 0.002);
         } else {
            for (Box var6 : this.boxes) {
               this.a(var6.minX, var6.minY, var6.minZ, var6.maxX, var6.maxY, var6.maxZ, var4, 0.002);
            }
         }
      }

      if (this.faceOnly.get()) {
         this.a(this.faceBox, this.face);
         this.e();
      } else {
         for (double[] var8 : this.edges) {
            this.a(var8[0], var8[1], var8[2], var8[3], var8[4], var8[5]);
         }
      }

      if (this.glints.get()) {
         this.d();
      }
   }

   private void d() {
      Box var1 = this.faceOnly.get() ? this.faceBox : this.localBounds;
      if (var1 != null) {
         int var2 = ColorHelper.lerp(0.55F, this.shown | 0xFF000000, -1);
         float var3 = 0.07F + 0.012F * this.lineWidth.get();

         for (int var4 = 0; var4 < 8; var4++) {
            double var5 = (var4 & 1) == 0 ? var1.minX : var1.maxX;
            double var7 = (var4 & 2) == 0 ? var1.minY : var1.maxY;
            double var9 = (var4 & 4) == 0 ? var1.minZ : var1.maxZ;
            if (!this.faceOnly.get() || this.a(var5, var7, var9, var1)) {
               double var11 = this.time * 2.2 - CORNER_ORDER[var4] * 0.78;
               float var13 = (float)Math.pow(Math.max(0.0, Math.sin(var11)), 6.0);
               float var14 = this.fade * (0.22F + 0.78F * var13);
               float var15 = var3 * (0.75F + 0.55F * var13);
               double var16 = this.a(var5);
               double var18 = this.b(var7);
               double var20 = this.c(var9);
               this.canvas.sprite(EffectSprites.SPARK, var16, var18, var20, var15, (float)(this.time * 0.6), ColorHelper.scaleAlpha(var2, var14));
            }
         }
      }
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private boolean a(double d, double e, double f, Box box) {
      return switch (this.face) {
         case EAST -> d == box.maxX;
         case WEST -> d == box.minX;
         case UP -> e == box.maxY;
         case DOWN -> e == box.minY;
         case SOUTH -> f == box.maxZ;
         case NORTH -> f == box.minZ;
         default -> throw new MatchException(null, null);
      };
   }

   private void a(int i, double d, double e) {
      double var6 = this.box[i];
      double var8 = this.box[i + 3];
      double var10 = this.goal[i + 3] - this.goal[i];
      double var12 = var10 > 1.0E-6 ? (var8 - var6) / var10 : 1.0;
      double var14 = (var6 + var8) * 0.5;
      double var16 = var14 * (1.0 - e) + (var6 + d * var12) * e;
      double var18 = var12 * e;
      switch (i) {
         case 0:
            this.ax = var16;
            this.bx = var18;
            break;
         case 1:
            this.ay = var16;
            this.by = var18;
            break;
         default:
            this.az = var16;
            this.bz = var18;
      }
   }

   private double a(double d) {
      return this.ax + this.bx * d;
   }

   private double b(double d) {
      return this.ay + this.by * d;
   }

   private double c(double d) {
      return this.az + this.bz * d;
   }

   private void a(double d, double e, double f, double g, double h, double i) {
      float var13 = this.lineWidth.get();
      int var14 = this.shown;
      if (this.glow.get()) {
         this.a(d, e, f, g, h, i, ColorHelper.scaleAlpha(var14, 0.34F * this.fade), var13 + 3.0F, 0.002);
         this.a(d, e, f, g, h, i, ColorHelper.scaleAlpha(var14, 0.13F * this.fade), var13 + 8.0F, 0.002);
      }

      this.a(d, e, f, g, h, i, ColorHelper.scaleAlpha(var14, this.fade), var13, 0.004);
   }

   private void e() {
      for (int var1 = 0; var1 < 4; var1++) {
         int var2 = var1 * 3;
         int var3 = (var1 + 1) % 4 * 3;
         this.a(
            this.faceEdges[var2], this.faceEdges[var2 + 1], this.faceEdges[var2 + 2], this.faceEdges[var3], this.faceEdges[var3 + 1], this.faceEdges[var3 + 2]
         );
      }
   }

   private void a(double d, double e, double f, double g, double h, double i, int j, float k, double l) {
      double var17 = this.a(d);
      double var19 = this.b(e);
      double var21 = this.c(f);
      double var23 = this.a(g);
      double var25 = this.b(h);
      double var27 = this.c(i);
      double var29 = l / Math.max(1.0E-6, Math.sqrt(d(this.eye.x - var17) + d(this.eye.y - var19) + d(this.eye.z - var21)));
      double var31 = l / Math.max(1.0E-6, Math.sqrt(d(this.eye.x - var23) + d(this.eye.y - var25) + d(this.eye.z - var27)));
      this.canvas
         .line(
            var17 + (this.eye.x - var17) * var29,
            var19 + (this.eye.y - var19) * var29,
            var21 + (this.eye.z - var21) * var29,
            var23 + (this.eye.x - var23) * var31,
            var25 + (this.eye.y - var25) * var31,
            var27 + (this.eye.z - var27) * var31,
            j,
            k
         );
   }

   private void a(double d, double e, double f, double g, double h, double i, float j, double k) {
      for (Direction var19 : DIRECTIONS) {
         this.a(d, e, f, g, h, i, var19, j, k);
      }
   }

   private void a(Box box, Direction direction, float f, double d) {
      this.a(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, direction, f, d);
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private void a(double d, double e, double f, double g, double h, double i, Direction direction, float j, double k) {
      double var17 = this.a(d) - k;
      double var19 = this.b(e) - k;
      double var21 = this.c(f) - k;
      double var23 = this.a(g) + k;
      double var25 = this.b(h) + k;
      double var27 = this.c(i) + k;
      int var29 = this.shown;
      int var30 = MathHelper.clamp(Math.round(j * this.fade * 255.0F), 0, 255) << 24 | var29 & 16777215;
      switch (direction) {
         case EAST:
            this.canvas.quad(var23, var19, var21, var23, var25, var21, var23, var25, var27, var23, var19, var27, var30);
            break;
         case WEST:
            this.canvas.quad(var17, var19, var27, var17, var25, var27, var17, var25, var21, var17, var19, var21, var30);
            break;
         case UP:
            this.canvas.quad(var17, var25, var21, var17, var25, var27, var23, var25, var27, var23, var25, var21, var30);
            break;
         case DOWN:
            this.canvas.quad(var17, var19, var27, var17, var19, var21, var23, var19, var21, var23, var19, var27, var30);
            break;
         case SOUTH:
            this.canvas.quad(var17, var19, var27, var23, var19, var27, var23, var25, var27, var17, var25, var27, var30);
            break;
         case NORTH:
            this.canvas.quad(var23, var19, var21, var17, var19, var21, var17, var25, var21, var23, var25, var21, var30);
      }
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private void a(Box box, Direction direction) {
      double[] var3 = this.faceEdges;
      switch (direction.getAxis()) {
         case X:
            double var7 = direction == Direction.EAST ? box.maxX : box.minX;
            a(var3, 0, var7, box.minY, box.minZ);
            a(var3, 1, var7, box.maxY, box.minZ);
            a(var3, 2, var7, box.maxY, box.maxZ);
            a(var3, 3, var7, box.minY, box.maxZ);
            break;
         case Y:
            double var6 = direction == Direction.UP ? box.maxY : box.minY;
            a(var3, 0, box.minX, var6, box.minZ);
            a(var3, 1, box.maxX, var6, box.minZ);
            a(var3, 2, box.maxX, var6, box.maxZ);
            a(var3, 3, box.minX, var6, box.maxZ);
            break;
         case Z:
            double var4 = direction == Direction.SOUTH ? box.maxZ : box.minZ;
            a(var3, 0, box.minX, box.minY, var4);
            a(var3, 1, box.maxX, box.minY, var4);
            a(var3, 2, box.maxX, box.maxY, var4);
            a(var3, 3, box.minX, box.maxY, var4);
      }
   }

   private static void a(double[] ds, int i, double d, double e, double f) {
      ds[i * 3] = d;
      ds[i * 3 + 1] = e;
      ds[i * 3 + 2] = f;
   }

   private static double d(double d) {
      return d * d;
   }
}
