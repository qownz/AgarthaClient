package net.agartha.client.module.impl.render;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.agartha.client.d.f;
import net.agartha.client.d.a_pkg.o;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.module.a_pkg.a_o;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.util.OreSim;
import net.agartha.client.util.ab;
import net.agartha.client.util.h;
import net.agartha.client.util.k;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

@Environment(EnvType.CLIENT)
public final class SeedXrayModule extends b {
   private static final long SEED = 6608149111735331168L;
   private static final String TAG = "Seed X-Ray";
   private static final int OLD_BORDER = 175000;
   private static final String OVERWORLD = "minecraft:overworld";
   private static final int WORK_TICKS = 5;
   private static final int REBUILD_TICKS = 10;
   private static final int CHUNKS_PER_JOB = 6;
   private final a_o sOres = (a_o)this.addSetting(a_o.createForSeedOres());
   private final j sRadius = (j)this.addSetting(new j("Radius (chunks)", 8.0F, 1.0F, 32.0F).whole());
   private final j sRange = (j)this.addSetting(new j("Render Distance", 64.0F, 8.0F, 256.0F).whole());
   private final j sMaxBoxes = (j)this.addSetting(new j("Max Boxes", 3000.0F, 100.0F, 20000.0F).whole());
   private final net.agartha.client.module.a_pkg.b sHideMined = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Hide Mined", true)
   );
   private final net.agartha.client.module.a_pkg.b sOldArea = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Inside Old Border", false)
   );
   private final j sFillAlpha = (j)this.addSetting(new j("Fill Alpha", 0.18F, 0.0F, 1.0F));
   private final Map<Long, Map<Block, int[]>> ores = new ConcurrentHashMap<>();
   private final AtomicBoolean simBusy = new AtomicBoolean();
   private final AtomicBoolean rebuildBusy = new AtomicBoolean();
   private ExecutorService worker;
   private volatile int generation;
   private ClientWorld level;
   private String dimension = "";
   private Set<Block> allOres = Set.of();
   private int ticks;
   private boolean warnedNotDonut;
   private static final SeedXrayModule.Shown NOTHING = new SeedXrayModule.Shown(new BlockPos[0], new Box[0], new int[0]);
   private volatile SeedXrayModule.Shown shown = NOTHING;

   public SeedXrayModule() {
      super("Seed X-Ray", "Shows DonutSMP ores through the rock.", c.DONUT);
   }

   protected void onEnable() {
      this.worker = Executors.newSingleThreadExecutor(runnable -> {
         Thread var1 = new Thread(runnable, "agartha-seed-xray");
         var1.setDaemon(true);
         var1.setPriority(1);
         return var1;
      });
      this.worker.execute(() -> {
         try {
            OreSim.load();
         } catch (LinkageError | Exception var1) {
            MinecraftClient.getInstance().execute(() -> ab.send("Seed X-Ray", "Could not start: " + var1));
         }
      });
      this.reset();
      this.warnedNotDonut = false;
   }

   protected void onDisable() {
      this.reset();
      if (this.worker != null) {
         this.worker.shutdownNow();
      }

      this.worker = null;
   }

   private void reset() {
      this.generation++;
      this.ores.clear();
      this.shown = NOTHING;
      this.level = null;
      this.dimension = "";
   }

   @f
   public void onTick(q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null && this.worker != null && OreSim.ready()) {
         if (!h.connected()) {
            if (!this.warnedNotDonut) {
               this.warnedNotDonut = true;
               ab.send("Seed X-Ray", "Only works on DonutSMP: the seed is Donut's.");
            }

            this.shown = NOTHING;
         } else {
            this.warnedNotDonut = false;
            if (var2.world != this.level) {
               this.reset();
               this.level = var2.world;
               this.dimension = var2.world.getRegistryKey().getValue().toString();
               this.allOres = this.oreBlocks();
            }

            if (OreSim.supports(this.dimension)) {
               this.ticks++;
               int var3 = var2.player.getBlockX() >> 4;
               int var4 = var2.player.getBlockZ() >> 4;
               if (this.ticks % 5 == 0 && !this.simBusy.get()) {
                  this.startSimulation(var3, var4);
               }

               if (this.ticks % 10 == 0 && !this.rebuildBusy.get()) {
                  this.startRebuild(var2);
               }

               if (this.ticks % 200 == 0) {
                  int var5 = this.sRadius.getInt() * 2 + (this.sRange.getInt() >> 4) + 4;
                  this.ores
                     .keySet()
                     .removeIf(long_ -> Math.abs(ChunkPos.getPackedX(long_) - var3) > var5 || Math.abs(ChunkPos.getPackedZ(long_) - var4) > var5);
               }
            }
         }
      }
   }

   private Set<Block> oreBlocks() {
      Set var1 = Collections.newSetFromMap(new IdentityHashMap());

      for (net.agartha.client.module.a_pkg.a_o.a var3 : this.sOres.entries()) {
         Identifier var4 = Identifier.tryParse(var3.id());
         if (var4 != null) {
            Registries.BLOCK.getOptionalValue(var4).ifPresent(var1::add);
         }
      }

      return var1;
   }

   private boolean skipped(int i, int j) {
      return !this.sOldArea.get() && "minecraft:overworld".equals(this.dimension)
         ? Math.abs((long)i << 4) < 175000L && Math.abs((long)j << 4) < 175000L
         : false;
   }

   private void startSimulation(int i, int j) {
      int var3 = this.sRadius.getInt();
      long[] var4 = new long[6];
      int var5 = 0;

      label45:
      for (int var6 = 0; var6 <= var3; var6++) {
         for (int var7 = -var6; var7 <= var6; var7++) {
            for (int var8 = -var6; var8 <= var6; var8++) {
               if (Math.max(Math.abs(var7), Math.abs(var8)) == var6) {
                  int var9 = i + var7;
                  int var10 = j + var8;
                  long var11 = ChunkPos.toLong(var9, var10);
                  if (!this.ores.containsKey(var11) && !this.skipped(var9, var10) && this.readyToSimulate(var9, var10)) {
                     var4[var5++] = var11;
                     if (var5 == var4.length) {
                        break label45;
                     }
                  }
               }
            }
         }
      }

      if (var5 != 0) {
         this.simBusy.set(true);
         int var13 = this.generation;
         int var14 = var5;
         ClientWorld var15 = this.level;
         String var16 = this.dimension;
         Set var17 = this.allOres;
         this.worker.execute(() -> {
            try {
               SeedXrayModule.ClientTerrain var7x = new SeedXrayModule.ClientTerrain(var15);

               for (int var8x = 0; var8x < var14 && var13 == this.generation; var8x++) {
                  long var9x = var4[var8x];

                  Map var11x;
                  try {
                     var11x = OreSim.simulate(var16, 6608149111735331168L, ChunkPos.getPackedX(var9x), ChunkPos.getPackedZ(var9x), var7x, var17);
                  } catch (RuntimeException var16x) {
                     continue;
                  }

                  if (var13 == this.generation) {
                     this.ores.put(var9x, var11x);
                  }
               }
            } finally {
               this.simBusy.set(false);
            }
         });
      }
   }

   private boolean readyToSimulate(int i, int j) {
      for (int var3 = -1; var3 <= 1; var3++) {
         for (int var4 = -1; var4 <= 1; var4++) {
            if (!this.level.getChunkManager().isChunkLoaded(i + var3, j + var4)) {
               return false;
            }
         }
      }

      return true;
   }

   private void startRebuild(MinecraftClient minecraftClient) {
      IdentityHashMap var2 = new IdentityHashMap();

      for (Block var4 : this.allOres) {
         if (this.sOres.isEnabled(var4)) {
            var2.put(var4, this.sOres.colorArgb(var4));
         }
      }

      this.rebuildBusy.set(true);
      double var16 = minecraftClient.player.getX();
      double var5 = minecraftClient.player.getEyeY();
      double var7 = minecraftClient.player.getZ();
      int var9 = this.sRange.getInt();
      int var10 = this.sMaxBoxes.getInt();
      boolean var11 = this.sHideMined.get();
      boolean var12 = this.sOldArea.get();
      int var13 = this.generation;
      ClientWorld var14 = this.level;
      String var15 = this.dimension;
      this.worker.execute(() -> {
         try {
            SeedXrayModule.Shown var15x = this.rebuild(var14, var15, var2, var16, var5, var7, var9, var10, var11, var12);
            if (var13 == this.generation) {
               this.shown = var15x;
            }
         } catch (RuntimeException var19) {
         } finally {
            this.rebuildBusy.set(false);
         }
      });
   }

   private SeedXrayModule.Shown rebuild(
      ClientWorld clientWorld, String string, Map<Block, Integer> map, double d, double e, double f, int i, int j, boolean bl, boolean bl2
   ) {
      double var14 = (double)i * i;
      int var16 = (i >> 4) + 1;
      int var17 = (int)Math.floor(d) >> 4;
      int var18 = (int)Math.floor(f) >> 4;
      boolean var19 = !bl2 && "minecraft:overworld".equals(string);
      Mutable var20 = new Mutable();
      ArrayList<long[]> var21 = new ArrayList<>();
      ArrayList var22 = new ArrayList();
      ArrayList var23 = new ArrayList();

      for (Entry var25 : this.ores.entrySet()) {
         int var26 = ChunkPos.getPackedX((Long)var25.getKey());
         int var27 = ChunkPos.getPackedZ((Long)var25.getKey());
         if (Math.abs(var26 - var17) <= var16
            && Math.abs(var27 - var18) <= var16
            && (!var19 || Math.abs((long)var26 << 4) >= 175000L || Math.abs((long)var27 << 4) >= 175000L)) {
            for (Entry var29 : (Iterable<Entry>) ((Map)var25.getValue()).entrySet()) {
               Integer var30 = (Integer)map.get(var29.getKey());
               if (var30 != null) {
                  int[] var31 = (int[])var29.getValue();

                  for (int var32 = 0; var32 + 2 < var31.length; var32 += 3) {
                     double var33 = var31[var32] + 0.5 - d;
                     double var35 = var31[var32 + 1] + 0.5 - e;
                     double var37 = var31[var32 + 2] + 0.5 - f;
                     double var39 = var33 * var33 + var35 * var35 + var37 * var37;
                     if (!(var39 > var14)) {
                        if (bl && clientWorld.getChunkManager().isChunkLoaded(var31[var32] >> 4, var31[var32 + 2] >> 4)) {
                           BlockState var41 = clientWorld.getBlockState(var20.set(var31[var32], var31[var32 + 1], var31[var32 + 2]));
                           if (var41.isAir() || !var41.getFluidState().isEmpty()) {
                              continue;
                           }
                        }

                        var21.add(new long[]{Double.doubleToRawLongBits(var39), var22.size()});
                        var22.add(new BlockPos(var31[var32], var31[var32 + 1], var31[var32 + 2]));
                        var23.add(var30);
                     }
                  }
               }
            }
         }
      }

      if (var21.size() > j) {
         var21.sort((ls, ms) -> Double.compare(Double.longBitsToDouble(ls[0]), Double.longBitsToDouble(ms[0])));
      }

      int var42 = Math.min(j, var21.size());
      BlockPos[] var43 = new BlockPos[var42];
      Box[] var44 = new Box[var42];
      int[] var45 = new int[var42];

      for (int var46 = 0; var46 < var42; var46++) {
         int var47 = (int)((long[])var21.get(var46))[1];
         BlockPos var48 = (BlockPos)var22.get(var47);
         var43[var46] = var48;
         var44[var46] = new Box(var48.getX() + 0.02, var48.getY() + 0.02, var48.getZ() + 0.02, var48.getX() + 0.98, var48.getY() + 0.98, var48.getZ() + 0.98);
         var45[var46] = (Integer)var23.get(var47);
      }

      return new SeedXrayModule.Shown(var43, var44, var45);
   }

   @f
   public void onRender3D(o arg) {
      SeedXrayModule.Shown var2 = this.shown;
      int var3 = Math.round(this.sFillAlpha.get() * 255.0F);

      for (int var4 = 0; var4 < var2.pos().length; var4++) {
         int var5 = var2.color()[var4];
         Integer var6 = var3 > 0 ? var5 & 16777215 | var3 << 24 : null;
         k.submitCuboid(var2.pos()[var4], var2.box()[var4], var5, 1.0F, var6, true);
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class ClientTerrain implements OreSim.Terrain {
      private final ClientWorld world;
      private final Long2ObjectOpenHashMap<int[]> floors = new Long2ObjectOpenHashMap();
      private final Mutable cursor = new Mutable();
      private int lastX = Integer.MIN_VALUE;
      private int lastZ = Integer.MIN_VALUE;
      private WorldChunk last;

      ClientTerrain(ClientWorld clientWorld) {
         this.world = clientWorld;
      }

      private WorldChunk chunk(int i, int j) {
         if (i != this.lastX || j != this.lastZ) {
            this.last = this.world.getChunkManager().getWorldChunk(i, j);
            this.lastX = i;
            this.lastZ = j;
         }

         return this.last;
      }

      @Override
      public BlockState state(int i, int j, int k) {
         WorldChunk var4 = this.chunk(i >> 4, k >> 4);
         if (var4 == null) {
            return null;
         } else if (j >= var4.getBottomY() && j < var4.getTopYInclusive() + 1) {
            ChunkSection var5 = var4.getSection(var4.getSectionIndex(j));
            return var5.getBlockState(i & 15, j & 15, k & 15);
         } else {
            return Blocks.AIR.getDefaultState();
         }
      }

      @Override
      public int floorTop(int i, int j) {
         long var3 = ChunkPos.toLong(i >> 4, j >> 4);
         int[] var5 = (int[])this.floors.get(var3);
         if (var5 == null) {
            var5 = new int[256];
            Arrays.fill(var5, Integer.MIN_VALUE);
            this.floors.put(var3, var5);
         }

         int var6 = i & 15 | (j & 15) << 4;
         if (var5[var6] != Integer.MIN_VALUE) {
            return var5[var6];
         } else {
            WorldChunk var7 = this.chunk(i >> 4, j >> 4);
            int var8 = Integer.MAX_VALUE;
            if (var7 != null) {
               var8 = var7.getBottomY();

               for (int var9 = var7.sampleHeightmap(Type.WORLD_SURFACE, i & 15, j & 15); var9 >= var7.getBottomY(); var9--) {
                  BlockState var10 = this.state(i, var9, j);
                  if (var10 != null && OreSim.groundAtGen(var10)) {
                     var8 = var9 + 1;
                     break;
                  }
               }
            }

            var5[var6] = var8;
            return var8;
         }
      }

      @Override
      public RegistryKey<Biome> biome(int i, int j, int k) {
         return (RegistryKey<Biome>)this.world.getBiome(this.cursor.set(i, j, k)).getKey().orElse(null);
      }
   }

   @Environment(EnvType.CLIENT)
   private record Shown(BlockPos[] pos, Box[] box, int[] color) {
   }
}
