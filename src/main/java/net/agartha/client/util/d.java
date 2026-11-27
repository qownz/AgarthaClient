package net.agartha.client.util;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.agartha.client.module.impl.render.BlockEntityDebugModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

@Environment(EnvType.CLIENT)
public final class d {
   private static final Map<Block, d.a> ENTRY_BY_BLOCK = new ConcurrentHashMap<>();
   private static final Map<BlockPos, d.a> FOUND = new ConcurrentHashMap<>();
   private static final Map<Long, Map<BlockPos, d.a>> BY_CHUNK = new ConcurrentHashMap<>();
   private static final AtomicLong VERSION = new AtomicLong();

   private d() {
   }

   public static long version() {
      return VERSION.get();
   }

   public static Map<BlockPos, d.a> view() {
      return FOUND;
   }

   public static boolean isEmpty() {
      return FOUND.isEmpty();
   }

   public static boolean contains(BlockPos blockPos) {
      return FOUND.containsKey(blockPos);
   }

   public static Map<BlockPos, d.a> inChunk(long l) {
      Map var2 = BY_CHUNK.get(l);
      return var2 == null ? Collections.emptyMap() : var2;
   }

   public static void clear() {
      FOUND.clear();
      BY_CHUNK.clear();
      VERSION.incrementAndGet();
   }

   public static void ingest(Packet<?> packet) {
      if (packet instanceof ChunkDataS2CPacket var1) {
         ChunkData var6 = var1.getChunkData();
         a(ChunkPos.toLong(var1.getChunkX(), var1.getChunkZ()));

         try {
            var6.getBlockEntities(var1.getChunkX(), var1.getChunkZ()).accept((blockPos, blockEntityType, nbtCompound) -> {
               if (blockEntityType != null) {
                  a(blockPos, blockEntityType, nbtCompound);
               }
            });
         } catch (Throwable var8) {
         }
      } else if (packet instanceof BlockEntityUpdateS2CPacket var2) {
         a(var2.getPos(), var2.getBlockEntityType(), var2.getNbt());
      } else if (packet instanceof BlockUpdateS2CPacket var3) {
         if (var3.getState().isAir() && !a(var3.getPos())) {
            forget(var3.getPos());
         }
      } else if (packet instanceof ChunkDeltaUpdateS2CPacket var4) {
         var4.visitUpdates((blockPos, blockState) -> {
            if (blockState.isAir() && FOUND.containsKey(blockPos) && !a(blockPos)) {
               forget(blockPos);
            }
         });
      } else if (packet instanceof UnloadChunkS2CPacket var5) {
         a(var5.pos().toLong());
      } else if (packet instanceof GameJoinS2CPacket || packet instanceof PlayerRespawnS2CPacket) {
         clear();
      }
   }

   private static boolean a(BlockPos blockPos) {
      BlockEntityDebugModule var1 = net.agartha.client.module.e.get(BlockEntityDebugModule.class);
      return var1 != null && var1.isEnabled() && !BlockEntityDebugModule.brokenByYou(blockPos);
   }

   public static void forget(BlockPos blockPos) {
      if (FOUND.remove(blockPos) != null) {
         Map var1 = BY_CHUNK.get(ChunkPos.toLong(blockPos.getX() >> 4, blockPos.getZ() >> 4));
         if (var1 != null) {
            var1.remove(blockPos);
         }

         VERSION.incrementAndGet();
      }
   }

   private static void a(long l) {
      Map var2 = BY_CHUNK.remove(l);
      if (var2 != null) {
         for (BlockPos var4 : (Iterable<BlockPos>) var2.keySet()) {
            FOUND.remove(var4);
         }

         VERSION.incrementAndGet();
      }
   }

   private static void a(BlockPos blockPos, BlockEntityType<?> blockEntityType, NbtCompound nbtCompound) {
      BlockPos var3 = blockPos.toImmutable();
      d.a var4 = ENTRY_BY_BLOCK.computeIfAbsent(blockFromType(blockEntityType), block -> new d.a(block, null));
      FOUND.put(var3, var4);
      long var5 = ChunkPos.toLong(var3.getX() >> 4, var3.getZ() >> 4);
      BY_CHUNK.computeIfAbsent(var5, long_ -> new ConcurrentHashMap<>()).put(var3, var4);
      VERSION.incrementAndGet();
   }

   public static Block blockFromType(BlockEntityType<?> blockEntityType) {
      Identifier var1 = Registries.BLOCK_ENTITY_TYPE.getId(blockEntityType);
      if (var1 == null) {
         return Blocks.AIR;
      } else {
         String var2 = var1.getPath();

         Block var3 = switch (var2) {
            case "mob_spawner" -> Blocks.SPAWNER;
            case "piston" -> Blocks.MOVING_PISTON;
            case "sign" -> Blocks.OAK_SIGN;
            case "hanging_sign" -> Blocks.OAK_HANGING_SIGN;
            case "skull" -> Blocks.SKELETON_SKULL;
            case "banner" -> Blocks.WHITE_BANNER;
            case "bed" -> Blocks.RED_BED;
            case "brushable_block" -> Blocks.SUSPICIOUS_SAND;
            default -> null;
         };
         return var3 != null ? var3 : Registries.BLOCK.getEntry(var1).<Block>map(RegistryEntry::value).orElse(Blocks.AIR);
      }
   }

   @Environment(EnvType.CLIENT)
   public record a(Block block, NbtCompound nbt) {
   }
}
