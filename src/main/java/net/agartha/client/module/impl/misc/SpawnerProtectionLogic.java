package net.agartha.client.module.impl.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.agartha.client.util.net.ProtectMessages;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

@Environment(EnvType.CLIENT)
public final class SpawnerProtectionLogic {
   private static final int MAX_SPAWNER_STACK_SLOTS = 35;
   private static final double DROPPED_SPAWNER_RADIUS = 4.0;
   private static final int GHOST_BREAK_RETRY_LIMIT = 3;
   private static final int GHOST_STALL_TICKS = 60;
   private static final double CHEST_INTERACT_DISTANCE_SQ = 25.0;
   private static final long COUNT_RESET_MS = 60000L;
   private static final Vec3d ENDER_CHEST_INTERACT_OFFSET = new Vec3d(0.5, 0.8125, 0.5);
   private final Runnable disableModule;
   private SpawnerProtectionLogic.c state = SpawnerProtectionLogic.c.MONITORING;
   private final Map<Long, SpawnerProtectionLogic.a> pendingBreaks = new HashMap<>();
   private final Set<BlockPos> skippedSpawners = new HashSet<>();
   private final List<BlockPos> confirmedBreakPositions = new ArrayList<>();
   private ClientWorld trackedWorld;
   private BlockPos currentSpawnerTarget;
   private BlockPos targetChest;
   private BlockPos anchorPos;
   private String anchorWorldId;
   private int observedBreakCount;
   private int brokenSpawnerCount;
   private int ghostBreakRetries;
   private int ghostStallTicks;
   private int ghostCooldownTicks;
   private int tickCounter;
   private int currentTargetTicks;
   private int currentTargetAbsentTicks;
   private int noSpawnerTicks;
   private int chestOpenTicks;
   private int transferCooldown;
   private int lastSpawnerItemCount;
   private boolean returnToBreakingAfterDeposit;
   private boolean automationAttackHeld;
   private boolean automationSneakHeld;
   private long lastCountTimestamp;
   private volatile SpawnerProtectionLogic.b settings;

   public SpawnerProtectionLogic(Runnable runnable) {
      this.disableModule = runnable;
   }

   public void setSettings(SpawnerProtectionLogic.b arg) {
      this.settings = arg;
   }

   public void tick(MinecraftClient minecraftClient) {
      if (minecraftClient != null && minecraftClient.player != null && minecraftClient.world != null && minecraftClient.interactionManager != null) {
         this.tickCounter++;
         SpawnerProtectionLogic.b var2 = this.settings;
         if (var2 != null) {
            if (this.g(minecraftClient)) {
               this.f(minecraftClient, var2);
            } else {
               if (this.trackedWorld == null) {
                  this.a(minecraftClient.world, minecraftClient.player);
               }

               if (this.transferCooldown > 0) {
                  this.transferCooldown--;
               }

               if (this.state == SpawnerProtectionLogic.c.MONITORING) {
                  this.a(minecraftClient.player, var2);
                  this.b(minecraftClient.player, var2);
               }

               switch (this.state) {
                  case SUSPENDED:
                     this.e(minecraftClient, var2);
                     break;
                  case MONITORING:
                     this.a(minecraftClient);
                     break;
                  case BREAKING:
                     this.a(minecraftClient, var2);
                     break;
                  case OPENING_CHEST:
                     this.c(minecraftClient, var2);
                     break;
                  case DEPOSITING:
                     this.d(minecraftClient, var2);
               }
            }
         }
      }
   }

   public void onBlockUpdate(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
      SpawnerProtectionLogic.b var4 = this.settings;
      if (var4 != null && blockState != null && blockState2 != null) {
         if (this.currentSpawnerTarget != null && blockState2.getBlock() == Blocks.SPAWNER && blockPos.equals(this.currentSpawnerTarget)) {
            this.currentTargetAbsentTicks = 0;
         }

         if (this.state == SpawnerProtectionLogic.c.MONITORING && this.trackedWorld != null) {
            if (this.a(blockPos, var4)) {
               long var5 = blockPos.asLong();
               if (!blockState.isAir() && blockState2.isAir()) {
                  this.pendingBreaks.put(var5, new SpawnerProtectionLogic.a(blockPos.toImmutable(), this.tickCounter + var4.brokenBlockConfirmTicks()));
               } else {
                  if (!blockState2.isAir()) {
                     this.pendingBreaks.remove(var5);
                  }
               }
            }
         }
      }
   }

   public void softReset() {
      this.trackedWorld = null;
      this.state = SpawnerProtectionLogic.c.MONITORING;
      this.pendingBreaks.clear();
      this.skippedSpawners.clear();
      this.currentSpawnerTarget = null;
      this.targetChest = null;
      this.anchorPos = null;
      this.anchorWorldId = null;
      this.observedBreakCount = 0;
      this.brokenSpawnerCount = 0;
      this.ghostBreakRetries = 0;
      this.ghostStallTicks = 0;
      this.ghostCooldownTicks = 0;
      this.tickCounter = 0;
      this.currentTargetTicks = 0;
      this.currentTargetAbsentTicks = 0;
      this.noSpawnerTicks = 0;
      this.chestOpenTicks = 0;
      this.transferCooldown = 0;
      this.lastSpawnerItemCount = 0;
      this.returnToBreakingAfterDeposit = false;
      this.lastCountTimestamp = 0L;
   }

   public void onDisable(MinecraftClient minecraftClient) {
      if (minecraftClient != null) {
         this.d(minecraftClient);
      }

      this.softReset();
      this.settings = null;
   }

   private void a(ClientWorld clientWorld, ClientPlayerEntity clientPlayerEntity) {
      this.softReset();
      this.trackedWorld = clientWorld;
      this.anchorPos = clientPlayerEntity.getBlockPos().toImmutable();
      this.anchorWorldId = clientWorld.getRegistryKey().getValue().toString();
      this.lastSpawnerItemCount = this.e(clientPlayerEntity);
   }

   private void a(MinecraftClient minecraftClient) {
      this.c(minecraftClient);
   }

   private void a(ClientPlayerEntity clientPlayerEntity, SpawnerProtectionLogic.b arg) {
      if (!this.pendingBreaks.isEmpty()) {
         Iterator var3 = this.pendingBreaks.entrySet().iterator();

         while (var3.hasNext()) {
            SpawnerProtectionLogic.a var4 = (SpawnerProtectionLogic.a)((Entry)var3.next()).getValue();
            if (this.tickCounter >= var4.confirmTick) {
               if (this.trackedWorld != null && this.trackedWorld.getBlockState(var4.pos).isAir()) {
                  this.observedBreakCount++;
                  this.lastCountTimestamp = System.currentTimeMillis();
                  this.c(var4.pos);
                  String var5 = this.observedBreakCount + "/" + arg.triggerBreakCount();
                  this.a(arg, this.a("Progress: " + var5, var4.pos));
                  if (this.observedBreakCount >= arg.triggerBreakCount()) {
                     this.a(arg, this.a("Progress reached " + var5 + ". Starting spawner protection.", var4.pos));
                     this.a(clientPlayerEntity);
                     this.pendingBreaks.clear();
                     return;
                  }
               }

               var3.remove();
            }
         }
      }
   }

   private void b(ClientPlayerEntity clientPlayerEntity, SpawnerProtectionLogic.b arg) {
      if (this.observedBreakCount > 0 && this.lastCountTimestamp > 0L) {
         if (System.currentTimeMillis() - this.lastCountTimestamp >= 60000L) {
            this.observedBreakCount = 0;
            this.lastCountTimestamp = 0L;
            this.pendingBreaks.clear();
            this.a(arg, this.a("0/" + arg.triggerBreakCount() + " reset after 60 seconds without a new break."));
            this.confirmedBreakPositions.clear();
         }
      }
   }

   private void a(ClientPlayerEntity clientPlayerEntity) {
      this.state = SpawnerProtectionLogic.c.BREAKING;
      this.currentSpawnerTarget = null;
      this.targetChest = null;
      this.currentTargetTicks = 0;
      this.currentTargetAbsentTicks = 0;
      this.noSpawnerTicks = 0;
      this.skippedSpawners.clear();
      this.lastSpawnerItemCount = this.e(clientPlayerEntity);
   }

   private void a(MinecraftClient minecraftClient, SpawnerProtectionLogic.b arg) {
      if (!this.c(minecraftClient)) {
         ClientPlayerEntity var3 = minecraftClient.player;
         this.b(minecraftClient, true);
         if (this.c(var3)) {
            if (!this.a(var3, minecraftClient, true, arg)) {
               this.a(var3, minecraftClient, arg, "The inventory filled up, but no reachable ender chest was found.");
            }
         } else {
            if (this.currentSpawnerTarget != null) {
               BlockState var4 = minecraftClient.world.getBlockState(this.currentSpawnerTarget);
               if (var4.getBlock() == Blocks.SPAWNER) {
                  this.currentTargetAbsentTicks = 0;
               } else {
                  this.currentTargetAbsentTicks++;
                  if (this.currentTargetAbsentTicks < arg.brokenSpawnerConfirmTicks()) {
                     this.a(minecraftClient, false);
                     return;
                  }

                  int var5 = this.e(var3);
                  if (var5 <= this.lastSpawnerItemCount && !this.b(minecraftClient)) {
                     this.b(minecraftClient, arg);
                     return;
                  }

                  this.brokenSpawnerCount = this.brokenSpawnerCount + (var5 - this.lastSpawnerItemCount);
                  this.lastSpawnerItemCount = var5;
                  this.ghostBreakRetries = 0;
                  this.currentSpawnerTarget = null;
                  this.currentTargetTicks = 0;
                  this.currentTargetAbsentTicks = 0;
               }
            }

            if (this.currentSpawnerTarget == null) {
               this.currentSpawnerTarget = this.a(minecraftClient, arg.spawnerRange());
               this.currentTargetTicks = 0;
               this.currentTargetAbsentTicks = 0;
               if (this.currentSpawnerTarget == null) {
                  this.noSpawnerTicks++;
                  if (this.noSpawnerTicks >= arg.noSpawnerConfirmTicks()) {
                     if (this.e(var3) > 0) {
                        if (!this.a(var3, minecraftClient, false, arg)) {
                           this.a(var3, minecraftClient, arg, "No reachable ender chest was found after the spawners were collected.");
                        }
                     } else {
                        this.a(var3, minecraftClient, arg, "No rendered spawners remain nearby.");
                     }
                  }

                  return;
               }

               this.noSpawnerTicks = 0;
               this.ghostBreakRetries = 0;
               this.ghostStallTicks = 0;
               this.a(arg, "Target spawner: " + this.b(this.currentSpawnerTarget));
            }

            if (this.ghostCooldownTicks > 0) {
               this.ghostCooldownTicks--;
               this.a(minecraftClient, false);
               this.b(minecraftClient, false);
               minecraftClient.interactionManager.cancelBlockBreaking();
            } else {
               if (this.e(var3) <= this.lastSpawnerItemCount && !this.b(minecraftClient)) {
                  if (++this.ghostStallTicks >= 60) {
                     this.b(minecraftClient, arg);
                     return;
                  }
               } else {
                  this.ghostStallTicks = 0;
               }

               this.currentTargetTicks++;
               if (this.currentTargetTicks > arg.mineTimeoutTicks()) {
                  this.skippedSpawners.add(this.currentSpawnerTarget.toImmutable());
                  this.a(arg, "Spawner timed out, skipping: " + this.b(this.currentSpawnerTarget));
                  this.currentSpawnerTarget = null;
                  this.currentTargetTicks = 0;
                  this.currentTargetAbsentTicks = 0;
                  this.a(minecraftClient, false);
               } else {
                  int var8 = this.b(var3);
                  if (var8 == -1) {
                     this.a(var3, minecraftClient, arg, "No pickaxe was found.");
                  } else {
                     this.c(minecraftClient, var8);
                     Direction var9 = this.b(minecraftClient, this.currentSpawnerTarget);
                     if (var9 == null) {
                        this.skippedSpawners.add(this.currentSpawnerTarget.toImmutable());
                        this.a(arg, "Spawner is not visible, skipping: " + this.b(this.currentSpawnerTarget));
                        this.currentSpawnerTarget = null;
                        this.currentTargetTicks = 0;
                        this.currentTargetAbsentTicks = 0;
                     } else {
                        this.b(minecraftClient, this.currentSpawnerTarget, var9);
                        if (minecraftClient.crosshairTarget instanceof BlockHitResult var6
                           && var6.getType() == Type.BLOCK
                           && var6.getBlockPos().equals(this.currentSpawnerTarget)) {
                           this.a(minecraftClient, true);
                        } else {
                           this.a(minecraftClient, false);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean b(MinecraftClient minecraftClient) {
      if (minecraftClient.world != null && this.currentSpawnerTarget != null) {
         Box var2 = new Box(this.currentSpawnerTarget).expand(4.0);

         for (ItemEntity var4 : minecraftClient.world.getNonSpectatingEntities(ItemEntity.class, var2)) {
            if (var4.getStack().isOf(Items.SPAWNER)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void b(MinecraftClient minecraftClient, SpawnerProtectionLogic.b arg) {
      this.ghostStallTicks = 0;
      this.currentTargetAbsentTicks = 0;
      this.a(minecraftClient, false);
      this.b(minecraftClient, false);
      minecraftClient.interactionManager.cancelBlockBreaking();
      this.ghostBreakRetries++;
      if (this.ghostBreakRetries >= 3) {
         if (this.currentSpawnerTarget != null) {
            this.skippedSpawners.add(this.currentSpawnerTarget.toImmutable());
            this.a(arg, "Break never registered, skipping: " + this.b(this.currentSpawnerTarget));
         }

         this.ghostBreakRetries = 0;
         this.ghostCooldownTicks = 0;
         this.currentSpawnerTarget = null;
         this.currentTargetTicks = 0;
      } else {
         this.ghostCooldownTicks = Math.max(1, arg.ghostRetryDelayTicks());
         this.currentTargetTicks = 0;
         this.a(arg, "Break did not register, retrying (" + this.ghostBreakRetries + "/3): " + this.b(this.currentSpawnerTarget));
      }
   }

   private boolean a(ClientPlayerEntity clientPlayerEntity, MinecraftClient minecraftClient, boolean bl, SpawnerProtectionLogic.b arg) {
      this.targetChest = this.b(minecraftClient, arg.chestSearchRange());
      if (this.targetChest == null) {
         return false;
      } else {
         this.returnToBreakingAfterDeposit = bl;
         this.state = SpawnerProtectionLogic.c.OPENING_CHEST;
         this.chestOpenTicks = 0;
         this.currentSpawnerTarget = null;
         this.currentTargetTicks = 0;
         this.currentTargetAbsentTicks = 0;
         this.a(minecraftClient, false);
         this.b(minecraftClient, false);
         this.a(arg, "Opening ender chest: " + this.b(this.targetChest));
         return true;
      }
   }

   private void c(MinecraftClient minecraftClient, SpawnerProtectionLogic.b arg) {
      if (!this.c(minecraftClient)) {
         ClientPlayerEntity var3 = minecraftClient.player;
         this.e(minecraftClient);
         this.b(minecraftClient, false);
         if (this.targetChest == null || minecraftClient.world.getBlockState(this.targetChest).getBlock() != Blocks.ENDER_CHEST) {
            this.a(var3, minecraftClient, arg, "The ender chest is no longer valid.");
         } else if (!this.a(minecraftClient, this.targetChest)) {
            this.a(var3, minecraftClient, arg, "The ender chest cannot be opened without moving.");
         } else if (var3.currentScreenHandler instanceof GenericContainerScreenHandler) {
            this.state = SpawnerProtectionLogic.c.DEPOSITING;
            this.chestOpenTicks = 0;
         } else {
            this.b(minecraftClient, this.targetChest, Direction.UP, this.a(this.targetChest));
            this.chestOpenTicks++;
            if (this.chestOpenTicks % 4 == 0) {
               minecraftClient.interactionManager
                  .interactBlock(var3, Hand.MAIN_HAND, new BlockHitResult(this.a(this.targetChest), Direction.UP, this.targetChest, false));
               var3.swingHand(Hand.MAIN_HAND);
            }

            if (this.chestOpenTicks > 40) {
               this.a(var3, minecraftClient, arg, "Failed to open the ender chest.");
            }
         }
      }
   }

   private void d(MinecraftClient minecraftClient, SpawnerProtectionLogic.b arg) {
      if (!this.c(minecraftClient)) {
         ClientPlayerEntity var3 = minecraftClient.player;
         this.b(minecraftClient, false);
         if (var3.currentScreenHandler instanceof GenericContainerScreenHandler var4) {
            if (this.transferCooldown <= 0) {
               if (!this.a(var4) && this.e(var3) > 0) {
                  this.a(var3, minecraftClient, arg, "The ender chest is full.");
               } else {
                  int var8 = var4.slots.size() - 36;

                  for (int var6 = var8; var6 < var4.slots.size(); var6++) {
                     ItemStack var7 = var4.getSlot(var6).getStack();
                     if (!var7.isEmpty() && var7.getItem() == Items.SPAWNER) {
                        minecraftClient.interactionManager.clickSlot(var4.syncId, var6, 0, SlotActionType.QUICK_MOVE, var3);
                        this.transferCooldown = 2;
                        return;
                     }
                  }

                  var3.closeHandledScreen();
                  this.transferCooldown = 5;
                  this.lastSpawnerItemCount = this.e(var3);
                  this.targetChest = null;
                  if (this.returnToBreakingAfterDeposit) {
                     this.state = SpawnerProtectionLogic.c.BREAKING;
                     this.noSpawnerTicks = 0;
                     this.a(arg, "Spawners were moved into the ender chest. Breaking continues.");
                  } else {
                     this.a(var3, minecraftClient, arg, "All spawners were moved into the ender chest.");
                  }
               }
            }
         } else {
            this.state = SpawnerProtectionLogic.c.OPENING_CHEST;
            this.chestOpenTicks = 0;
         }
      }
   }

   private void a(ClientPlayerEntity clientPlayerEntity, MinecraftClient minecraftClient, SpawnerProtectionLogic.b arg, String string) {
      this.d(minecraftClient);
      this.pendingBreaks.clear();
      this.skippedSpawners.clear();
      this.currentSpawnerTarget = null;
      this.targetChest = null;
      this.observedBreakCount = 0;
      this.brokenSpawnerCount = 0;
      this.ghostBreakRetries = 0;
      this.ghostStallTicks = 0;
      this.ghostCooldownTicks = 0;
      this.currentTargetTicks = 0;
      this.currentTargetAbsentTicks = 0;
      this.noSpawnerTicks = 0;
      this.chestOpenTicks = 0;
      this.transferCooldown = 0;
      this.returnToBreakingAfterDeposit = false;
      this.automationAttackHeld = false;
      this.automationSneakHeld = false;
      this.lastSpawnerItemCount = this.e(clientPlayerEntity);
      this.lastCountTimestamp = 0L;
      this.state = SpawnerProtectionLogic.c.MONITORING;
      this.confirmedBreakPositions.clear();
      this.a(arg, string);
      this.a(arg, "Spawner Protect returned to monitoring.");
      if (arg.autoQuitAfterBreak() && minecraftClient.world != null) {
         this.a(arg, "Auto quit is enabled, disconnecting now.");
         minecraftClient.world.disconnect(Text.literal("Agartha Protect auto quit."));
      }
   }

   private boolean c(MinecraftClient minecraftClient) {
      if (this.anchorPos != null && this.anchorWorldId != null) {
         String var2 = minecraftClient.world.getRegistryKey().getValue().toString();
         BlockPos var3 = minecraftClient.player.getBlockPos();
         boolean var4 = !this.anchorWorldId.equals(var2);
         boolean var5 = !this.anchorPos.equals(var3);
         if (this.state == SpawnerProtectionLogic.c.SUSPENDED || !var4 && !var5) {
            return false;
         } else {
            this.a(minecraftClient.player, var2, var3, var4);
            return true;
         }
      } else {
         return false;
      }
   }

   private void a(ClientPlayerEntity clientPlayerEntity, String string, BlockPos blockPos, boolean bl) {
      this.a();
      this.state = SpawnerProtectionLogic.c.SUSPENDED;
      String var5 = bl
         ? "Maintenance or teleport detected. World changed to: " + string
         : "Maintenance or teleport detected. Position changed to: " + this.b(blockPos);
      this.a(this.settings, var5);
   }

   private void e(MinecraftClient minecraftClient, SpawnerProtectionLogic.b arg) {
      this.f(minecraftClient);
      if (this.anchorPos != null && this.anchorWorldId != null) {
         String var3 = minecraftClient.world.getRegistryKey().getValue().toString();
         BlockPos var4 = minecraftClient.player.getBlockPos();
         if (this.anchorWorldId.equals(var3) && this.anchorPos.equals(var4)) {
            this.state = SpawnerProtectionLogic.c.MONITORING;
            this.pendingBreaks.clear();
            this.observedBreakCount = 0;
            this.brokenSpawnerCount = 0;
            this.ghostBreakRetries = 0;
            this.ghostStallTicks = 0;
            this.ghostCooldownTicks = 0;
            this.lastSpawnerItemCount = this.e(minecraftClient.player);
            this.lastCountTimestamp = 0L;
            this.a(arg, "Maintenance ended. I returned to my anchor position and resumed monitoring.");
         }
      }
   }

   private BlockPos a(MinecraftClient minecraftClient, int i) {
      BlockPos var3 = minecraftClient.player.getBlockPos();
      BlockPos var4 = null;
      double var5 = Double.MAX_VALUE;

      for (int var7 = -i; var7 <= i; var7++) {
         for (int var8 = -i; var8 <= i; var8++) {
            for (int var9 = -i; var9 <= i; var9++) {
               BlockPos var10 = var3.add(var7, var8, var9);
               if (!this.skippedSpawners.contains(var10) && minecraftClient.world.getBlockState(var10).getBlock() == Blocks.SPAWNER) {
                  Direction var11 = this.b(minecraftClient, var10);
                  if (var11 != null) {
                     double var12 = Vec3d.ofCenter(var10)
                        .squaredDistanceTo(minecraftClient.player.getX(), minecraftClient.player.getY(), minecraftClient.player.getZ());
                     if (var12 < var5) {
                        var5 = var12;
                        var4 = var10.toImmutable();
                     }
                  }
               }
            }
         }
      }

      return var4;
   }

   private BlockPos b(MinecraftClient minecraftClient, int i) {
      BlockPos var3 = minecraftClient.player.getBlockPos();
      BlockPos var4 = null;
      double var5 = Double.MAX_VALUE;

      for (int var7 = -i; var7 <= i; var7++) {
         for (int var8 = -i; var8 <= i; var8++) {
            for (int var9 = -i; var9 <= i; var9++) {
               BlockPos var10 = var3.add(var7, var8, var9);
               if (minecraftClient.world.getBlockState(var10).getBlock() == Blocks.ENDER_CHEST && this.a(minecraftClient, var10)) {
                  double var11 = Vec3d.ofCenter(var10)
                     .squaredDistanceTo(minecraftClient.player.getX(), minecraftClient.player.getY(), minecraftClient.player.getZ());
                  if (var11 < var5) {
                     var5 = var11;
                     var4 = var10.toImmutable();
                  }
               }
            }
         }
      }

      return var4;
   }

   private boolean a(MinecraftClient minecraftClient, BlockPos blockPos) {
      Vec3d var3 = this.a(blockPos);
      double var4 = var3.squaredDistanceTo(minecraftClient.player.getEyePos());
      return var4 <= 25.0 && this.a(minecraftClient, blockPos, Direction.UP, var3);
   }

   private int b(ClientPlayerEntity clientPlayerEntity) {
      for (int var2 = 0; var2 < clientPlayerEntity.getInventory().size(); var2++) {
         ItemStack var3 = clientPlayerEntity.getInventory().getStack(var2);
         String var4 = var3.getItem().toString().toLowerCase(Locale.ROOT);
         if (var4.contains("pickaxe")) {
            return var2;
         }
      }

      return -1;
   }

   private void c(MinecraftClient minecraftClient, int i) {
      if (i < 9) {
         minecraftClient.player.getInventory().setSelectedSlot(i);
      } else {
         int var3 = minecraftClient.player.getInventory().getSelectedSlot();
         minecraftClient.interactionManager.clickSlot(minecraftClient.player.playerScreenHandler.syncId, i, var3, SlotActionType.SWAP, minecraftClient.player);
      }
   }

   private boolean c(ClientPlayerEntity clientPlayerEntity) {
      return this.d(clientPlayerEntity) >= 35;
   }

   private int d(ClientPlayerEntity clientPlayerEntity) {
      int var2 = 0;

      for (int var3 = 0; var3 < 36; var3++) {
         ItemStack var4 = clientPlayerEntity.getInventory().getStack(var3);
         if (!var4.isEmpty() && var4.getItem() == Items.SPAWNER) {
            var2++;
         }
      }

      return var2;
   }

   private int e(ClientPlayerEntity clientPlayerEntity) {
      int var2 = 0;

      for (int var3 = 0; var3 < 36; var3++) {
         ItemStack var4 = clientPlayerEntity.getInventory().getStack(var3);
         if (!var4.isEmpty() && var4.getItem() == Items.SPAWNER) {
            var2 += var4.getCount();
         }
      }

      return var2;
   }

   private Direction b(MinecraftClient minecraftClient, BlockPos blockPos) {
      for (Direction var6 : Direction.values()) {
         BlockPos var7 = blockPos.offset(var6);
         BlockState var8 = minecraftClient.world.getBlockState(var7);
         if ((var8.isAir() || !var8.isFullCube(minecraftClient.world, var7)) && this.a(minecraftClient, blockPos, var6)) {
            return var6;
         }
      }

      return null;
   }

   private boolean a(MinecraftClient minecraftClient, BlockPos blockPos, Direction direction) {
      Vec3d var4 = Vec3d.ofCenter(blockPos).add(Vec3d.of(direction.getVector()).multiply(0.5));
      return this.a(minecraftClient, blockPos, direction, var4);
   }

   private void b(MinecraftClient minecraftClient, BlockPos blockPos, Direction direction) {
      this.b(minecraftClient, blockPos, direction, Vec3d.ofCenter(blockPos).add(Vec3d.of(direction.getVector()).multiply(0.5)));
   }

   private boolean a(MinecraftClient minecraftClient, BlockPos blockPos, Direction direction, Vec3d vec3d) {
      Vec3d var5 = minecraftClient.player.getEyePos();
      BlockHitResult var6 = minecraftClient.world.raycast(new RaycastContext(var5, vec3d, ShapeType.COLLIDER, FluidHandling.NONE, minecraftClient.player));
      return var6.getType() != Type.BLOCK || var6.getBlockPos().equals(blockPos);
   }

   private void b(MinecraftClient minecraftClient, BlockPos blockPos, Direction direction, Vec3d vec3d) {
      Vec3d var5 = minecraftClient.player.getEyePos();
      Vec3d var6 = vec3d.subtract(var5);
      double var7 = Math.sqrt(var6.x * var6.x + var6.z * var6.z);
      float var9 = (float)Math.toDegrees(Math.atan2(-var6.x, var6.z));
      float var10 = (float)(-Math.toDegrees(Math.atan2(var6.y, var7)));
      minecraftClient.player.setYaw(var9);
      minecraftClient.player.setPitch(var10);
   }

   private boolean a(BlockPos blockPos, SpawnerProtectionLogic.b arg) {
      return this.a(this.anchorPos, blockPos, arg.monitorChunkRadius());
   }

   private void d(MinecraftClient minecraftClient) {
      this.b(minecraftClient, false);
      this.f(minecraftClient);
   }

   private void e(MinecraftClient minecraftClient) {
      this.a(minecraftClient, false);
   }

   private void f(MinecraftClient minecraftClient) {
      this.a(minecraftClient, false);
   }

   private boolean g(MinecraftClient minecraftClient) {
      return minecraftClient.options.forwardKey.isPressed()
         || minecraftClient.options.backKey.isPressed()
         || minecraftClient.options.leftKey.isPressed()
         || minecraftClient.options.rightKey.isPressed()
         || minecraftClient.options.jumpKey.isPressed();
   }

   private void f(MinecraftClient minecraftClient, SpawnerProtectionLogic.b arg) {
      this.a();
      this.state = SpawnerProtectionLogic.c.MONITORING;
      this.trackedWorld = minecraftClient.world;
      this.anchorPos = minecraftClient.player.getBlockPos().toImmutable();
      this.anchorWorldId = minecraftClient.world.getRegistryKey().getValue().toString();
      this.f(minecraftClient);
      this.a(arg, "Manual movement detected. Spawner Protect disabled itself.");
      this.disableModule.run();
   }

   private void a() {
      this.b();
   }

   private void b() {
      this.pendingBreaks.clear();
      this.skippedSpawners.clear();
      this.confirmedBreakPositions.clear();
      this.currentSpawnerTarget = null;
      this.targetChest = null;
      this.observedBreakCount = 0;
      this.brokenSpawnerCount = 0;
      this.ghostBreakRetries = 0;
      this.ghostStallTicks = 0;
      this.ghostCooldownTicks = 0;
      this.currentTargetTicks = 0;
      this.currentTargetAbsentTicks = 0;
      this.noSpawnerTicks = 0;
      this.chestOpenTicks = 0;
      this.transferCooldown = 0;
      this.returnToBreakingAfterDeposit = false;
      this.automationAttackHeld = false;
      this.automationSneakHeld = false;
      this.lastCountTimestamp = 0L;
   }

   private Vec3d a(BlockPos blockPos) {
      return new Vec3d(
         blockPos.getX() + ENDER_CHEST_INTERACT_OFFSET.x, blockPos.getY() + ENDER_CHEST_INTERACT_OFFSET.y, blockPos.getZ() + ENDER_CHEST_INTERACT_OFFSET.z
      );
   }

   private boolean a(GenericContainerScreenHandler genericContainerScreenHandler) {
      int var2 = genericContainerScreenHandler.slots.size() - 36;

      for (int var3 = 0; var3 < var2; var3++) {
         ItemStack var4 = genericContainerScreenHandler.getSlot(var3).getStack();
         if (var4.isEmpty()) {
            return true;
         }

         if (var4.getItem() == Items.SPAWNER && var4.getCount() < var4.getMaxCount()) {
            return true;
         }
      }

      return false;
   }

   private void a(MinecraftClient minecraftClient, boolean bl) {
      if (bl || this.automationAttackHeld) {
         minecraftClient.options.attackKey.setPressed(bl);
         this.automationAttackHeld = bl;
      }
   }

   private void b(MinecraftClient minecraftClient, boolean bl) {
      if (bl || this.automationSneakHeld) {
         minecraftClient.options.sneakKey.setPressed(bl);
         this.automationSneakHeld = bl;
      }
   }

   private boolean a(BlockPos blockPos, BlockPos blockPos2, int i) {
      if (blockPos == null) {
         return false;
      } else {
         int var4 = blockPos.getX() >> 4;
         int var5 = blockPos.getZ() >> 4;
         int var6 = blockPos2.getX() >> 4;
         int var7 = blockPos2.getZ() >> 4;
         int var8 = var6 - var4;
         int var9 = var7 - var5;
         return var8 * var8 + var9 * var9 <= i * i;
      }
   }

   private String b(BlockPos blockPos) {
      return blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ();
   }

   private void c(BlockPos blockPos) {
      if (blockPos != null) {
         this.confirmedBreakPositions.add(blockPos.toImmutable());
         if (this.confirmedBreakPositions.size() > 64) {
            this.confirmedBreakPositions.remove(0);
         }
      }
   }

   private String a(String string, BlockPos blockPos) {
      StringBuilder var3 = new StringBuilder(string);
      if (blockPos != null) {
         var3.append("\nBreak: ").append(this.b(blockPos));
      }

      if (!this.confirmedBreakPositions.isEmpty()) {
         var3.append("\nRecent breaks: ").append(this.c());
      }

      return var3.toString();
   }

   private String a(String string) {
      return this.confirmedBreakPositions.isEmpty() ? string : string + "\nRecent breaks: " + this.c();
   }

   private String c() {
      return this.confirmedBreakPositions.stream().map(this::b).collect(Collectors.joining(" | "));
   }

   private void a(SpawnerProtectionLogic.b arg, String string) {
      if (arg == null || arg.chatNotify()) {
         ProtectMessages.send(string);
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class a {
      private final BlockPos pos;
      private final int confirmTick;

      private a(BlockPos blockPos, int i) {
         this.pos = blockPos;
         this.confirmTick = i;
      }
   }

   @Environment(EnvType.CLIENT)
   public interface b {
      int triggerBreakCount();

      int monitorChunkRadius();

      int spawnerRange();

      int chestSearchRange();

      int mineTimeoutTicks();

      int noSpawnerConfirmTicks();

      int brokenBlockConfirmTicks();

      int brokenSpawnerConfirmTicks();

      int ghostRetryDelayTicks();

      boolean chatNotify();

      boolean autoQuitAfterBreak();
   }

   @Environment(EnvType.CLIENT)
   private static enum c {
      SUSPENDED,
      MONITORING,
      BREAKING,
      OPENING_CHEST,
      DEPOSITING;
   }
}
