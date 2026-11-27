package net.agartha.client.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.Impl;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList.Preset;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig.Target;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer.IndexedFeatures;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;

@Environment(EnvType.CLIENT)
public final class OreSim {
   private static final Map<String, OreSim.Dimension> DIMENSIONS = new HashMap<>();
   private static final Map<RegistryKey<Biome>, GenerationSettings> BIOMES = new HashMap<>();
   private static final Map<Block, BlockState> GEN_TIME = new IdentityHashMap<>();
   private static final Set<Block> LATER_SURFACE = Collections.newSetFromMap(new IdentityHashMap<>());
   private static final Map<HeightRangePlacementModifier, HeightProvider> HEIGHTS = new IdentityHashMap<>();
   private static volatile boolean loaded;

   private OreSim() {
   }

   public static boolean ready() {
      return loaded;
   }

   public static boolean supports(String string) {
      return DIMENSIONS.containsKey(string);
   }

   public static synchronized void load() throws ReflectiveOperationException {
      if (!loaded) {
         WrapperLookup var0 = BuiltinRegistries.createWrapperLookup();
         Impl var1 = var0.getOrThrow(RegistryKeys.BIOME);
         Impl var2 = var0.getOrThrow(RegistryKeys.CHUNK_GENERATOR_SETTINGS);
         var0.getOrThrow(RegistryKeys.BIOME)
            .streamEntries()
            .forEach(reference -> BIOMES.put(reference.registryKey(), ((Biome)reference.value()).getGenerationSettings()));
         build("minecraft:overworld", preset(Preset.OVERWORLD, var1), var2.getOrThrow(ChunkGeneratorSettings.OVERWORLD), -64, 384);
         build("minecraft:the_nether", preset(Preset.NETHER, var1), var2.getOrThrow(ChunkGeneratorSettings.NETHER), 0, 256);

         for (Block var4 : Registries.BLOCK) {
            String var5 = Registries.BLOCK.getId(var4).getPath();
            if (var5.startsWith("deepslate_") && var5.endsWith("_ore")) {
               GEN_TIME.put(var4, Blocks.DEEPSLATE.getDefaultState());
            } else if ((!var5.startsWith("nether_") || !var5.endsWith("_ore")) && !var5.equals("ancient_debris")) {
               if (var5.endsWith("_ore")) {
                  GEN_TIME.put(var4, Blocks.STONE.getDefaultState());
               }
            } else {
               GEN_TIME.put(var4, Blocks.NETHERRACK.getDefaultState());
            }

            if (var5.endsWith("_leaves")
               || var5.endsWith("_log")
               || var5.endsWith("_wood")
               || var5.endsWith("_stem")
               || var5.endsWith("_hyphae")
               || var5.endsWith("mushroom_block")
               || var5.contains("coral")
               || var5.contains("bamboo")
               || var5.contains("dripleaf")
               || var5.contains("azalea")
               || var5.equals("mangrove_roots")
               || var5.equals("bee_nest")
               || var5.equals("pumpkin")
               || var5.equals("melon")
               || var5.contains("creaking")) {
               LATER_SURFACE.add(var4);
            }
         }

         loaded = true;
      }
   }

   public static boolean groundAtGen(BlockState blockState) {
      return blockState.blocksMovement() && !LATER_SURFACE.contains(blockState.getBlock());
   }

   private static BiomeSource preset(Preset preset, RegistryEntryLookup<Biome> registryEntryLookup) {
      return MultiNoiseBiomeSource.create(RegistryEntry.of(new MultiNoiseBiomeSourceParameterList(preset, registryEntryLookup)));
   }

   private static void build(String string, BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> registryEntry, int i, int j) throws ReflectiveOperationException {
      List var5 = PlacedFeatureIndexer.collectIndexedFeatures(
         List.copyOf(biomeSource.getBiomes()), registryEntryx -> ((Biome)registryEntryx.value()).getGenerationSettings().getFeatures(), true
      );
      ArrayList var6 = new ArrayList();

      for (int var7 = 0; var7 < var5.size(); var7++) {
         List var8 = ((IndexedFeatures)var5.get(var7)).features();

         for (int var9 = 0; var9 < var8.size(); var9++) {
            PlacedFeature var10 = (PlacedFeature)var8.get(var9);
            ConfiguredFeature var11 = (ConfiguredFeature)var10.feature().value();
            if (var11.config() instanceof OreFeatureConfig var12) {
               boolean var18 = var11.feature() == Feature.SCATTERED_ORE;
               if (var11.feature() == Feature.ORE || var18) {
                  for (PlacementModifier var15 : var10.placementModifiers()) {
                     if (var15 instanceof HeightRangePlacementModifier var16) {
                        HEIGHTS.put(var16, heightOf(var16));
                     }
                  }

                  var6.add(new OreSim.Ore(var7, var9, var10, var12, var18));
               }
            }
         }
      }

      HeightContext var17 = new HeightContext(new NoiseChunkGenerator(biomeSource, registryEntry), HeightLimitView.create(i, j));
      DIMENSIONS.put(string, new OreSim.Dimension(var6, var17, i, i + j));
   }

   private static HeightProvider heightOf(HeightRangePlacementModifier heightRangePlacementModifier) throws ReflectiveOperationException {
      for (Field var4 : HeightRangePlacementModifier.class.getDeclaredFields()) {
         if (var4.getType() == HeightProvider.class) {
            var4.setAccessible(true);
            return (HeightProvider)var4.get(heightRangePlacementModifier);
         }
      }

      throw new NoSuchFieldException("height provider");
   }

   public static Map<Block, int[]> simulate(String string, long l, int i, int j, OreSim.Terrain terrain, Set<Block> set) {
      OreSim.Dimension var7 = DIMENSIONS.get(string);
      IdentityHashMap var8 = new IdentityHashMap();
      if (var7 == null) {
         return Map.of();
      } else {
         ChunkRandom var9 = new ChunkRandom(new Xoroshiro128PlusPlusRandom(0L));
         long var10 = var9.setPopulationSeed(l, i << 4, j << 4);
         OreSim.Run var12 = new OreSim.Run(var7, terrain, var9, set, var8);

         for (OreSim.Ore var14 : var7.ores()) {
            var9.setDecoratorSeed(var10, var14.index(), var14.step());
            var12.ore = var14;
            var12.apply(0, new BlockPos(i << 4, var7.minY(), j << 4));
         }

         IdentityHashMap var15 = new IdentityHashMap();
         var8.forEach((block, intArrayList) -> var15.put(block, ((it.unimi.dsi.fastutil.ints.IntArrayList)intArrayList).toIntArray()));
         return var15;
      }
   }

   @Environment(EnvType.CLIENT)
   private record Dimension(List<OreSim.Ore> ores, HeightContext height, int minY, int maxY) {
   }

   @Environment(EnvType.CLIENT)
   private record Ore(int step, int index, PlacedFeature placed, OreFeatureConfig config, boolean scattered) {
   }

   @Environment(EnvType.CLIENT)
   private static final class Run {
      private final OreSim.Dimension dim;
      private final OreSim.Terrain terrain;
      private final Random random;
      private final Set<Block> wanted;
      private final Map<Block, IntArrayList> found;
      private final Long2ObjectOpenHashMap<BlockState> placed = new Long2ObjectOpenHashMap();
      private final Function<BlockPos, BlockState> view = this::at;
      private final Mutable cursor = new Mutable();
      private OreSim.Ore ore;

      Run(OreSim.Dimension dimension, OreSim.Terrain terrain, Random random, Set<Block> set, Map<Block, IntArrayList> map) {
         this.dim = dimension;
         this.terrain = terrain;
         this.random = random;
         this.wanted = set;
         this.found = map;
      }

      void apply(int i, BlockPos blockPos) {
         List var3 = this.ore.placed().placementModifiers();
         if (i == var3.size()) {
            if (this.ore.scattered()) {
               this.scattered(blockPos);
            } else {
               this.vein(blockPos);
            }
         } else {
            PlacementModifier var4 = (PlacementModifier)var3.get(i);
            if (var4 instanceof BiomePlacementModifier) {
               RegistryKey var6 = this.terrain.biome(blockPos.getX(), blockPos.getY(), blockPos.getZ());
               GenerationSettings var7 = var6 == null ? null : OreSim.BIOMES.get(var6);
               if (var7 != null && var7.isFeatureAllowed(this.ore.placed())) {
                  this.apply(i + 1, blockPos);
               }
            } else if (var4 instanceof HeightRangePlacementModifier var5) {
               this.apply(i + 1, blockPos.withY(OreSim.HEIGHTS.get(var5).get(this.random, this.dim.height())));
            } else {
               for (BlockPos var8 : var4.getPositions(null, this.random, blockPos).toList()) {
                  this.apply(i + 1, var8);
               }
            }
         }
      }

      private BlockState at(BlockPos blockPos) {
         return this.state(blockPos.getX(), blockPos.getY(), blockPos.getZ());
      }

      private BlockState state(int i, int j, int k) {
         BlockState var4 = (BlockState)this.placed.get(BlockPos.asLong(i, j, k));
         if (var4 != null) {
            return var4;
         } else {
            BlockState var5 = this.terrain.state(i, j, k);
            if (var5 == null) {
               return j < 0 ? Blocks.DEEPSLATE.getDefaultState() : Blocks.STONE.getDefaultState();
            } else {
               return normal(var5);
            }
         }
      }

      private static BlockState normal(BlockState blockState) {
         BlockState var1 = OreSim.GEN_TIME.get(blockState.getBlock());
         return var1 != null ? var1 : blockState;
      }

      private void set(int i, int j, int k, BlockState blockState) {
         this.placed.put(BlockPos.asLong(i, j, k), blockState);
         if (this.wanted.contains(blockState.getBlock())) {
            IntArrayList var5 = this.found.computeIfAbsent(blockState.getBlock(), block -> new IntArrayList());
            var5.add(i);
            var5.add(j);
            var5.add(k);
         }
      }

      private void tryPlace(int i, int j, int k) {
         this.cursor.set(i, j, k);
         BlockState var4 = this.state(i, j, k);

         for (Target var6 : this.ore.config().targets) {
            if (OreFeature.shouldPlace(var4, this.view, this.random, this.ore.config(), var6, this.cursor)) {
               this.set(i, j, k, var6.state);
               return;
            }
         }
      }

      private void scattered(BlockPos blockPos) {
         int var2 = this.random.nextInt(this.ore.config().size + 1);

         for (int var3 = 0; var3 < var2; var3++) {
            int var4 = Math.min(var3, 7);
            int var5 = this.offset(var4);
            int var6 = this.offset(var4);
            int var7 = this.offset(var4);
            this.tryPlace(blockPos.getX() + var5, blockPos.getY() + var6, blockPos.getZ() + var7);
         }
      }

      private int offset(int i) {
         return Math.round((this.random.nextFloat() - this.random.nextFloat()) * i);
      }

      private void vein(BlockPos blockPos) {
         int var2 = this.ore.config().size;
         float var3 = this.random.nextFloat() * (float) Math.PI;
         float var4 = var2 / 8.0F;
         int var5 = MathHelper.ceil((var2 / 16.0F * 2.0F + 1.0F) / 2.0F);
         double var6 = blockPos.getX() + Math.sin(var3) * var4;
         double var8 = blockPos.getX() - Math.sin(var3) * var4;
         double var10 = blockPos.getZ() + Math.cos(var3) * var4;
         double var12 = blockPos.getZ() - Math.cos(var3) * var4;
         double var14 = blockPos.getY() + this.random.nextInt(3) - 2;
         double var16 = blockPos.getY() + this.random.nextInt(3) - 2;
         int var18 = blockPos.getX() - MathHelper.ceil(var4) - var5;
         int var19 = blockPos.getY() - 2 - var5;
         int var20 = blockPos.getZ() - MathHelper.ceil(var4) - var5;
         int var21 = 2 * (MathHelper.ceil(var4) + var5);
         int var22 = 2 * (2 + var5);
         boolean var23 = false;

         label26:
         for (int var24 = var18; var24 <= var18 + var21; var24++) {
            for (int var25 = var20; var25 <= var20 + var21; var25++) {
               if (var19 <= this.terrain.floorTop(var24, var25)) {
                  var23 = true;
                  break label26;
               }
            }
         }

         if (var23) {
            this.fill(var6, var8, var10, var12, var14, var16, var18, var19, var20, var21, var22);
         }
      }

      private void fill(double d, double e, double f, double g, double h, double i, int j, int k, int l, int m, int n) {
         int var18 = this.ore.config().size;
         double[] var19 = new double[var18 * 4];

         for (int var20 = 0; var20 < var18; var20++) {
            float var21 = (float)var20 / var18;
            double var22 = this.random.nextDouble() * var18 / 16.0;
            var19[var20 * 4] = MathHelper.lerp(var21, d, e);
            var19[var20 * 4 + 1] = MathHelper.lerp(var21, h, i);
            var19[var20 * 4 + 2] = MathHelper.lerp(var21, f, g);
            var19[var20 * 4 + 3] = ((MathHelper.sin((float) Math.PI * var21) + 1.0F) * var22 + 1.0) / 2.0;
         }

         for (int var46 = 0; var46 < var18 - 1; var46++) {
            if (!(var19[var46 * 4 + 3] <= 0.0)) {
               for (int var48 = var46 + 1; var48 < var18; var48++) {
                  if (!(var19[var48 * 4 + 3] <= 0.0)) {
                     double var50 = var19[var46 * 4] - var19[var48 * 4];
                     double var24 = var19[var46 * 4 + 1] - var19[var48 * 4 + 1];
                     double var26 = var19[var46 * 4 + 2] - var19[var48 * 4 + 2];
                     double var28 = var19[var46 * 4 + 3] - var19[var48 * 4 + 3];
                     if (var28 * var28 > var50 * var50 + var24 * var24 + var26 * var26) {
                        if (var28 > 0.0) {
                           var19[var48 * 4 + 3] = -1.0;
                        } else {
                           var19[var46 * 4 + 3] = -1.0;
                        }
                     }
                  }
               }
            }
         }

         BitSet var47 = new BitSet(m * n * m);

         for (int var49 = 0; var49 < var18; var49++) {
            double var51 = var19[var49 * 4 + 3];
            if (!(var51 < 0.0)) {
               double var52 = var19[var49 * 4];
               double var53 = var19[var49 * 4 + 1];
               double var54 = var19[var49 * 4 + 2];
               int var30 = Math.max(MathHelper.floor(var52 - var51), j);
               int var31 = Math.max(MathHelper.floor(var53 - var51), k);
               int var32 = Math.max(MathHelper.floor(var54 - var51), l);
               int var33 = Math.max(MathHelper.floor(var52 + var51), var30);
               int var34 = Math.max(MathHelper.floor(var53 + var51), var31);
               int var35 = Math.max(MathHelper.floor(var54 + var51), var32);

               for (int var36 = var30; var36 <= var33; var36++) {
                  double var37 = (var36 + 0.5 - var52) / var51;
                  if (!(var37 * var37 >= 1.0)) {
                     for (int var39 = var31; var39 <= var34; var39++) {
                        double var40 = (var39 + 0.5 - var53) / var51;
                        if (!(var37 * var37 + var40 * var40 >= 1.0)) {
                           for (int var42 = var32; var42 <= var35; var42++) {
                              double var43 = (var42 + 0.5 - var54) / var51;
                              if (!(var37 * var37 + var40 * var40 + var43 * var43 >= 1.0) && var39 >= this.dim.minY() && var39 < this.dim.maxY()) {
                                 int var45 = var36 - j + (var39 - k) * m + (var42 - l) * m * n;
                                 if (!var47.get(var45)) {
                                    var47.set(var45);
                                    this.tryPlace(var36, var39, var42);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public interface Terrain {
      BlockState state(int i, int j, int k);

      int floorTop(int i, int j);

      RegistryKey<Biome> biome(int i, int j, int k);
   }
}
