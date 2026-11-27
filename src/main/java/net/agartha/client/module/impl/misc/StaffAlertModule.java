package net.agartha.client.module.impl.misc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.agartha.client.d.f;
import net.agartha.client.d.a_pkg.j;
import net.agartha.client.d.a_pkg.k;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.e.ak;
import net.agartha.client.e.n;
import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.util.ab;
import net.agartha.client.util.x;
import net.agartha.client.util.x.a;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket.Suggestion;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Entry;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public final class StaffAlertModule extends b {
   private static final String PROBE = "minecraft:msg ";
   private static final int PROBE_INTERVAL = 100;
   private static final int SETTLE_TICKS = 60;
   private static final int VANISH_DELAY = 20;
   private static final long TRUSTED_LISTED_MS = 1500L;
   private static final long LEAVE_MEMORY_MS = 8000L;
   private static final double INVISIBLE_SQ = 4096.0;
   private static final long BODY_GRACE_MS = 10000L;
   private static final long SIGNAL_WINDOW_MS = 15000L;
   private static final long CRITICAL_COOLDOWN_MS = 12000L;
   private static final long PROVEN_MS = 600000L;
   private static final int CRITICAL_SCORE = 35;
   private static final int SIG_INVISIBLE = 1;
   private static final int SIG_SOUND = 2;
   private static final int SIG_PARTICLE = 3;
   private static final int SIG_VANISH = 4;
   private static final int SENSE_SOUND = 1;
   private static final int SENSE_ENTITY_SOUND = 2;
   private static final int SENSE_PARTICLE = 3;
   private static final int SENSE_EXPLOSION = 4;
   private static final int SENSE_METADATA = 5;
   private static final float IN_SECONDS = 0.22F;
   private static final float HOLD_SECONDS = 3.4F;
   private static final float OUT_SECONDS = 0.45F;
   private static final int NOTE_HIDE = 1;
   private static final int NOTE_REMOVE = 2;
   private static final int NOTE_CHAT = 3;
   private static final int NOTE_PROBE = 4;
   private final net.agartha.client.module.a_pkg.b sound = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Sound", true));
   private final ConcurrentLinkedQueue<StaffAlertModule.Note> notes = new ConcurrentLinkedQueue<>();
   private final ConcurrentLinkedQueue<StaffAlertModule.Sense> senses = new ConcurrentLinkedQueue<>();
   private final ArrayDeque<StaffAlertModule.Signal> signals = new ArrayDeque<>();
   private final ArrayDeque<StaffAlertModule.Boom> booms = new ArrayDeque<>();
   private final Map<String, Long> signalCooldown = new HashMap<>();
   private final Map<String, ArrayDeque<Long>> particleBursts = new HashMap<>();
   private final Map<Long, Long> selfPlaced = new ConcurrentHashMap<>();
   private final Map<Long, Long> poweredDoors = new HashMap<>();
   private final Map<String, Long> provenAt = new HashMap<>();
   private final Mutable cursor = new Mutable();
   private final Set<Integer> probeIds = ConcurrentHashMap.newKeySet();
   private final ArrayDeque<StaffAlertModule.Pending> pending = new ArrayDeque<>();
   private final ArrayDeque<StaffAlertModule.Leave> leaves = new ArrayDeque<>();
   private final Set<String> staffNames = new HashSet<>();
   private final Set<String> onServer = new HashSet<>();
   private final Map<String, StaffAlertModule.Body> bodies = new HashMap<>();
   private final Map<UUID, Long> listedAt = new HashMap<>();
   private final List<String> probeBatch = new ArrayList<>();
   private ClientWorld level;
   private ClientPlayNetworkHandler connection;
   private int region = Integer.MIN_VALUE;
   private int tickCount;
   private int settleTicks;
   private int nextProbeId = 30000;
   private volatile long lastLocalActionMs;
   private long lastContainerMs;
   private long lastCriticalMs;
   private String lastServerName = "";
   private String bannerName = "";
   private String bannerDetail = "";
   private long bannerStart;
   private boolean testBanner;

   public StaffAlertModule() {
      super("Staff Alert", "Warns when a vanished staff member is on your server, and again when they come near.", c.DONUT);
   }

   protected void onDisable() {
      this.clear();
   }

   public boolean bannerActive() {
      return !this.bannerName.isEmpty() && !(this.bannerAlpha() <= 0.01F) ? this.isEnabled() || this.testBanner : false;
   }

   public void playTest() {
      this.testBanner = true;
      MinecraftClient var1 = MinecraftClient.getInstance();
      this.bannerName = "Test";
      this.bannerDetail = "nearby";
      this.bannerStart = System.nanoTime();
      ab.send("Staff", "Test is nearby");
      if (this.sound.get() && var1.getSoundManager() != null) {
         var1.getSoundManager().play(PositionedSoundInstance.ui(ak.resolve("Pop 1"), 1.0F, 0.85F));
      }
   }

   public String bannerName() {
      return this.bannerName;
   }

   public String bannerDetail() {
      return this.bannerDetail;
   }

   public float bannerAlpha() {
      if (!this.bannerName.isEmpty() && this.bannerStart != 0L) {
         float var1 = (float)(System.nanoTime() - this.bannerStart) / 1.0E9F;
         if (var1 < 0.22F) {
            return var1 / 0.22F;
         } else if (var1 < 3.6200001F) {
            return 1.0F;
         } else {
            float var2 = var1 - 0.22F - 3.4F;
            return var2 >= 0.45F ? 0.0F : 1.0F - var2 / 0.45F;
         }
      } else {
         return 0.0F;
      }
   }

   private void clear() {
      this.notes.clear();
      this.senses.clear();
      this.signals.clear();
      this.booms.clear();
      this.signalCooldown.clear();
      this.particleBursts.clear();
      this.selfPlaced.clear();
      this.poweredDoors.clear();
      this.provenAt.clear();
      this.probeIds.clear();
      this.pending.clear();
      this.leaves.clear();
      this.listedAt.clear();
      this.probeBatch.clear();
      this.staffNames.clear();
      this.onServer.clear();
      this.bodies.clear();
      this.level = null;
      this.connection = null;
      this.region = Integer.MIN_VALUE;
      this.tickCount = 0;
      this.settleTicks = 0;
      this.bannerName = "";
      this.bannerDetail = "";
      this.bannerStart = 0L;
      this.testBanner = false;
      this.lastLocalActionMs = 0L;
      this.lastContainerMs = 0L;
      this.lastCriticalMs = 0L;
      this.lastServerName = "";
   }

   @f
   public void onPacket(j arg) {
      this.captureSense(arg);
      if (arg.packet instanceof PlayerRemoveS2CPacket var8) {
         if (var8.profileIds().size() < 4) {
            for (UUID var19 : var8.profileIds()) {
               if (var19 != null) {
                  this.notes.add(new StaffAlertModule.Note(2, var19, null));
               }
            }
         }
      } else if (arg.packet instanceof PlayerListS2CPacket var2 && var2.getActions().contains(Action.UPDATE_LISTED)) {
         int var14 = 0;

         for (Entry var20 : var2.getEntries()) {
            if (var20 != null && !var20.listed()) {
               var14++;
            }
         }

         if (var14 > 0 && var14 < 4) {
            for (Entry var21 : var2.getEntries()) {
               if (var21 != null && !var21.listed() && var21.profileId() != null) {
                  this.notes.add(new StaffAlertModule.Note(1, var21.profileId(), null));
               }
            }
         }
      } else if (arg.packet instanceof GameMessageS2CPacket var7) {
         Text var13 = var7.content();
         String var16 = var13 == null ? "" : var13.getString();
         if (!var16.isBlank()) {
            this.notes.add(new StaffAlertModule.Note(3, null, var16));
         }
      } else {
         if (arg.packet instanceof CommandSuggestionsS2CPacket var6 && this.probeIds.remove(var6.id())) {
            arg.setCancelled(true);
            int var12 = 0;

            for (Suggestion var5 : var6.suggestions()) {
               if (var5 != null && var5.text() != null && !var5.text().isBlank()) {
                  this.notes.add(new StaffAlertModule.Note(4, null, var5.text().trim()));
                  if (++var12 >= 256) {
                     break;
                  }
               }
            }
         }
      }
   }

   @f
   public void onTick(q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null && var2.getNetworkHandler() != null) {
         int var3 = var2.world.getRegistryKey() == World.OVERWORLD ? n.regionAt(var2.player.getX(), var2.player.getZ()) : Integer.MIN_VALUE;
         if (var2.world != this.level || var2.getNetworkHandler() != this.connection || var3 != this.region) {
            this.clear();
            this.level = var2.world;
            this.connection = var2.getNetworkHandler();
            this.region = var3;
            this.settleTicks = 60;
         }

         this.tickCount++;
         if (this.settleTicks > 0) {
            this.settleTicks--;
         }

         this.learnStaff();
         this.rememberLeaves();
         this.drainNotes(var2);
         this.finishPending(var2);
         if (this.settleTicks == 0 && this.tickCount % 100 == 0) {
            this.sendProbe(var2);
         }

         if (this.settleTicks == 0) {
            this.watchBodies(var2);
            if (var2.player.currentScreenHandler != var2.player.playerScreenHandler) {
               this.lastContainerMs = System.currentTimeMillis();
            }

            this.drainSenses(var2);
         }
      } else {
         if (this.level != null) {
            this.clear();
         }
      }
   }

   @f
   public void onSend(k arg) {
      if (arg.packet instanceof PlayerInteractBlockC2SPacket var2) {
         this.lastLocalActionMs = System.currentTimeMillis();
         if (var2.getBlockHitResult() != null) {
            long var6 = this.lastLocalActionMs + 6000L;
            BlockPos var5 = var2.getBlockHitResult().getBlockPos();
            this.selfPlaced.put(var5.asLong(), var6);
            this.selfPlaced.put(var5.offset(var2.getBlockHitResult().getSide()).asLong(), var6);
            this.selfPlaced.put(var5.up().asLong(), var6);
            this.selfPlaced.put(var5.down().asLong(), var6);
         }
      } else {
         if (arg.packet instanceof PlayerActionC2SPacket
            || arg.packet instanceof PlayerInteractItemC2SPacket
            || arg.packet instanceof PlayerInteractEntityC2SPacket
            || arg.packet instanceof HandSwingC2SPacket) {
            this.lastLocalActionMs = System.currentTimeMillis();
         }
      }
   }

   private void learnStaff() {
      long var1 = System.currentTimeMillis();

      for (a var4 : x.online()) {
         if (var4.name() != null) {
            this.staffNames.add(var4.name().toLowerCase(Locale.ROOT));
         }
      }

      if (this.connection != null) {
         for (PlayerListEntry var8 : this.connection.getListedPlayerListEntries()) {
            if (var8.getProfile() != null && var8.getProfile().name() != null) {
               String var5 = var8.getProfile().name();
               if (x.watchesAccount(var5)) {
                  this.staffNames.add(var5.toLowerCase(Locale.ROOT));
               }

               if (this.staffNames.contains(var5.toLowerCase(Locale.ROOT))) {
                  String var6 = var5.toLowerCase(Locale.ROOT);
                  this.onServer.remove(var6);
                  this.bodies.remove(var6);
                  this.provenAt.remove(var6);
                  if (this.lastServerName.equalsIgnoreCase(var5)) {
                     this.lastServerName = "";
                  }
               }

               this.listedAt.putIfAbsent(var8.getProfile().id(), var1);
            }
         }
      }
   }

   private void rememberLeaves() {
      long var1 = System.currentTimeMillis();

      while (!this.leaves.isEmpty() && var1 - this.leaves.peekFirst().at > 8000L) {
         this.leaves.removeFirst();
      }
   }

   private void drainNotes(MinecraftClient minecraftClient) {
      this.probeBatch.clear();
      int var3 = 0;

      StaffAlertModule.Note var2;
      while ((var2 = this.notes.poll()) != null && var3++ < 1024) {
         switch (var2.kind) {
            case 1:
            case 2:
               this.queueVanish(var2.id);
               break;
            case 3:
               this.leaves.addLast(new StaffAlertModule.Leave(var2.text, System.currentTimeMillis()));
               break;
            case 4:
               if (var2.text != null) {
                  this.probeBatch.add(var2.text);
               }
         }
      }

      this.applyProbe(minecraftClient);
   }

   private void applyProbe(MinecraftClient minecraftClient) {
      if (!this.probeBatch.isEmpty() && this.settleTicks <= 0) {
         int var2 = 0;

         for (int var3 = 0; var3 < this.probeBatch.size(); var3++) {
            String var4 = this.probeBatch.get(var3);
            if (this.hiddenStaff(minecraftClient, var4)) {
               var2++;
            }
         }

         if (var2 > 0 && var2 <= 3) {
            for (int var5 = 0; var5 < this.probeBatch.size(); var5++) {
               String var6 = this.probeBatch.get(var5);
               if (this.hiddenStaff(minecraftClient, var6)) {
                  if (this.bodyLoaded(minecraftClient, this.uuidOf(var6))) {
                     this.markNear(minecraftClient, var6);
                  } else {
                     this.raise(minecraftClient, var6, false);
                  }
               }
            }
         }

         this.probeBatch.clear();
      } else {
         this.probeBatch.clear();
      }
   }

   private boolean hiddenStaff(MinecraftClient minecraftClient, String string) {
      if (this.isStaff(string) && plausible(string) && !this.listedName(string) && !this.leftRecently(string)) {
         ClientPlayerEntity var3 = minecraftClient.player;
         return var3 == null || var3.getGameProfile().name() == null || !string.equalsIgnoreCase(var3.getGameProfile().name());
      } else {
         return false;
      }
   }

   private void queueVanish(UUID uUID) {
      if (uUID != null && this.connection != null) {
         String var2 = this.nameOf(uUID);
         if (this.isStaff(var2) && this.trusted(uUID) && !this.leftRecently(var2)) {
            this.pending.addLast(new StaffAlertModule.Pending(uUID, var2, this.tickCount + 20));
         }
      }
   }

   private void finishPending(MinecraftClient minecraftClient) {
      while (!this.pending.isEmpty() && this.pending.peekFirst().due <= this.tickCount) {
         StaffAlertModule.Pending var2 = this.pending.removeFirst();
         if (!this.listed(var2.id) && !this.leftRecently(var2.name)) {
            if (this.bodyLoaded(minecraftClient, var2.id)) {
               this.markNear(minecraftClient, var2.name);
            } else {
               this.raise(minecraftClient, var2.name, false);
            }
         }
      }
   }

   private void sendProbe(MinecraftClient minecraftClient) {
      if (minecraftClient.getNetworkHandler() != null && !this.connection.getListedPlayerListEntries().isEmpty()) {
         int var2 = this.nextProbeId++;
         if (this.nextProbeId > 40000) {
            this.nextProbeId = 30000;
         }

         this.probeIds.add(var2);

         while (this.probeIds.size() > 8) {
            this.probeIds.remove(this.probeIds.iterator().next());
         }

         minecraftClient.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(var2, "minecraft:msg "));
      }
   }

   private void watchBodies(MinecraftClient minecraftClient) {
      ClientPlayerEntity var2 = minecraftClient.player;

      for (AbstractClientPlayerEntity var4 : minecraftClient.world.getPlayers()) {
         if (var4 != var2) {
            String var5 = var4.getGameProfile().name();
            if (this.isStaff(var5)) {
               boolean var6 = !this.listed(var4.getUuid());
               boolean var7 = var4.isInvisible() && var2.squaredDistanceTo(var4) <= 4096.0;
               if (var6 || var7) {
                  this.markNear(minecraftClient, var5);
               }
            }
         }
      }

      long var8 = System.currentTimeMillis();

      for (StaffAlertModule.Body var10 : this.bodies.values()) {
         if (var10.present && var10.tick != this.tickCount) {
            var10.present = false;
            var10.lostAt = var8;
         }
      }
   }

   private void markNear(MinecraftClient minecraftClient, String string) {
      if (this.settleTicks <= 0 && string != null) {
         String var3 = string.toLowerCase(Locale.ROOT);
         StaffAlertModule.Body var4 = this.bodies.get(var3);
         if (var4 == null) {
            var4 = new StaffAlertModule.Body();
            this.bodies.put(var3, var4);
         }

         var4.tick = this.tickCount;
         if (!var4.present) {
            var4.present = true;
            long var5 = System.currentTimeMillis();
            if (var4.lostAt == 0L || var5 - var4.lostAt > 10000L) {
               this.onServer.add(var3);
               this.provenAt.put(var3, var5);
               this.show(minecraftClient, string, true);
            }
         }
      }
   }

   private void raise(MinecraftClient minecraftClient, String string, boolean bl) {
      if (bl) {
         this.markNear(minecraftClient, string);
      } else if (this.settleTicks <= 0 && string != null) {
         String var4 = string.toLowerCase(Locale.ROOT);
         if (this.onServer.add(var4)) {
            this.lastServerName = string;
            this.provenAt.put(var4, System.currentTimeMillis());
            this.show(minecraftClient, string, false);
         }
      }
   }

   private void show(MinecraftClient minecraftClient, String string, boolean bl) {
      this.bannerName = string;
      this.bannerDetail = bl ? "nearby" : "on your server";
      this.bannerStart = System.nanoTime();
      ab.send("Staff", string + (bl ? " is nearby" : " vanished onto your server"));
      if (this.sound.get() && minecraftClient.getSoundManager() != null) {
         minecraftClient.getSoundManager().play(PositionedSoundInstance.ui(ak.resolve("Pop 1"), 1.0F, 0.85F));
      }
   }

   private boolean bodyLoaded(MinecraftClient minecraftClient, UUID uUID) {
      if (minecraftClient.world != null && minecraftClient.player != null && uUID != null) {
         PlayerEntity var3 = minecraftClient.world.getPlayerByUuid(uUID);
         return var3 != null && var3 != minecraftClient.player && !var3.isRemoved();
      } else {
         return false;
      }
   }

   private boolean listed(UUID uUID) {
      if (this.connection != null && uUID != null) {
         for (PlayerListEntry var3 : this.connection.getListedPlayerListEntries()) {
            if (var3.getProfile() != null && uUID.equals(var3.getProfile().id())) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean listedName(String string) {
      if (this.connection != null && string != null) {
         for (PlayerListEntry var3 : this.connection.getListedPlayerListEntries()) {
            if (var3.getProfile() != null && string.equalsIgnoreCase(var3.getProfile().name())) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private UUID uuidOf(String string) {
      if (this.connection != null && string != null) {
         for (PlayerListEntry var3 : this.connection.getPlayerList()) {
            if (var3.getProfile() != null && string.equalsIgnoreCase(var3.getProfile().name())) {
               return var3.getProfile().id();
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private String nameOf(UUID uUID) {
      if (this.connection != null && uUID != null) {
         for (PlayerListEntry var3 : this.connection.getPlayerList()) {
            if (var3.getProfile() != null && uUID.equals(var3.getProfile().id())) {
               return var3.getProfile().name();
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean trusted(UUID uUID) {
      Long var2 = this.listedAt.get(uUID);
      return var2 != null && System.currentTimeMillis() - var2 >= 1500L;
   }

   private boolean isStaff(String string) {
      return string != null && (this.staffNames.contains(string.toLowerCase(Locale.ROOT)) || x.watchesAccount(string));
   }

   private boolean leftRecently(String string) {
      if (string == null) {
         return false;
      } else {
         String var2 = string.toLowerCase(Locale.ROOT);
         long var3 = System.currentTimeMillis();

         for (StaffAlertModule.Leave var6 : this.leaves) {
            if (var3 - var6.at <= 8000L) {
               String var7 = var6.text.toLowerCase(Locale.ROOT);
               if (var7.contains(var2) && (var7.contains("left") || var7.contains("quit") || var7.contains("disconnect"))) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private static boolean plausible(String string) {
      int var1 = string.length();
      if (var1 >= 3 && var1 <= 16 && string.indexOf(32) < 0) {
         for (int var2 = 0; var2 < var1; var2++) {
            char var3 = string.charAt(var2);
            boolean var4 = var3 >= 'A' && var3 <= 'Z' || var3 >= 'a' && var3 <= 'z' || var3 >= '0' && var3 <= '9' || var3 == '_';
            if (!var4) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private void captureSense(j arg) {
      if (this.senses.size() <= 4096) {
         if (arg.packet instanceof PlaySoundS2CPacket var7) {
            String var15 = soundId(var7.getSound());
            if (suspiciousSound(var15)) {
               this.senses.add(new StaffAlertModule.Sense(1, 0, var7.getX(), var7.getY(), var7.getZ(), var15));
            }
         } else if (arg.packet instanceof PlaySoundFromEntityS2CPacket var6) {
            String var14 = soundId(var6.getSound());
            if (suspiciousSound(var14)) {
               this.senses.add(new StaffAlertModule.Sense(2, var6.getEntityId(), 0.0, 0.0, 0.0, var14));
            }
         } else if (arg.packet instanceof ParticleS2CPacket var5) {
            String var13 = particleId(var5.getParameters());
            if (suspiciousParticle(var13)) {
               this.senses.add(new StaffAlertModule.Sense(3, 0, var5.getX(), var5.getY(), var5.getZ(), var13));
            }
         } else if (arg.packet instanceof ExplosionS2CPacket var4) {
            Vec3d var12 = var4.center();
            if (var12 != null) {
               this.senses.add(new StaffAlertModule.Sense(4, 0, var12.x, var12.y, var12.z, Float.toString(var4.radius())));
            }
         } else {
            if (arg.packet instanceof EntityTrackerUpdateS2CPacket var2) {
               this.senses.add(new StaffAlertModule.Sense(5, var2.id(), 0.0, 0.0, 0.0, null));
            }
         }
      }
   }

   private void drainSenses(MinecraftClient minecraftClient) {
      int var3 = 0;

      StaffAlertModule.Sense var2;
      while ((var2 = this.senses.poll()) != null && var3++ < 512) {
         switch (var2.kind) {
            case 1:
               this.inspectSound(minecraftClient, var2.x, var2.y, var2.z, var2.text);
               break;
            case 2:
               this.inspectEntitySound(minecraftClient, var2.entityId, var2.text);
               break;
            case 3:
               this.inspectParticle(minecraftClient, var2.x, var2.y, var2.z, var2.text);
               break;
            case 4:
               this.rememberBoom(var2.x, var2.y, var2.z, var2.text);
               break;
            case 5:
               this.inspectMetadata(minecraftClient, var2.entityId);
         }
      }

      long var4 = System.currentTimeMillis();

      while (!this.signals.isEmpty() && var4 - this.signals.peekFirst().at > 15000L) {
         this.signals.removeFirst();
      }

      while (!this.booms.isEmpty() && var4 - this.booms.peekFirst().at > 3000L) {
         this.booms.removeFirst();
      }

      this.selfPlaced.entrySet().removeIf(entry -> entry.getValue() <= var4);
      this.poweredDoors.entrySet().removeIf(entry -> entry.getValue() <= var4);
   }

   private void inspectSound(MinecraftClient minecraftClient, double d, double e, double f, String string) {
      if (suspiciousSound(string) && minecraftClient.player != null) {
         long var9 = System.currentTimeMillis();
         if (!(minecraftClient.player.squaredDistanceTo(d, e, f) > 4096.0)
            && !this.visibleCause(minecraftClient, d, e, f)
            && !this.explosionNear(d, e, f)
            && !this.poweredDoor(minecraftClient, d, e, f, string)
            && var9 - this.lastLocalActionMs >= 1000L
            && !this.selfPlacedNear(d, e, f)
            && !this.villagerDoor(minecraftClient, d, e, f, string)
            && (var9 - this.lastContainerMs >= 2500L || !containerSound(string))) {
            this.addSignal(2, located(minecraftClient, d, e, f), false, 14, 3000L);
            this.proveVanishedStaff();
         }
      }
   }

   private void inspectEntitySound(MinecraftClient minecraftClient, int i, String string) {
      if (suspiciousSound(string) && minecraftClient.world != null && minecraftClient.player != null) {
         if (minecraftClient.world.getEntityById(i) instanceof PlayerEntity var5 && var5 != minecraftClient.player && var5.isInvisible()) {
            if (!(minecraftClient.player.squaredDistanceTo(var5) > 4096.0)) {
               String var6 = var5.getGameProfile().name();
               if (this.isStaff(var6)) {
                  this.addSignal(2, var6, true, 16, 3000L);
                  this.addSignal(1, var6, true, 35, 5000L);
               }
            }
         }
      }
   }

   private void inspectParticle(MinecraftClient minecraftClient, double d, double e, double f, String string) {
      if (suspiciousParticle(string) && minecraftClient.player != null) {
         long var9 = System.currentTimeMillis();
         String var11 = pathOf(string);
         if (!(minecraftClient.player.squaredDistanceTo(d, e, f) > 4096.0)
            && !this.visibleCause(minecraftClient, d, e, f)
            && !this.explosionNear(d, e, f)
            && var9 - this.lastLocalActionMs >= 900L
            && !this.ambientSmoke(minecraftClient, d, e, f, var11)) {
            if (!var11.equals("block") && !var11.contains("smoke") || !(minecraftClient.player.squaredDistanceTo(d, e, f) < 6.25)) {
               if (!var11.equals("block") && !var11.contains("smoke") || this.particleBurst(var11, var9)) {
                  this.addSignal(3, located(minecraftClient, d, e, f), false, 16, 3000L);
                  this.proveVanishedStaff();
               }
            }
         }
      }
   }

   private void inspectMetadata(MinecraftClient minecraftClient, int i) {
      if (minecraftClient.world != null && minecraftClient.player != null) {
         if (minecraftClient.world.getEntityById(i) instanceof PlayerEntity var4 && var4 != minecraftClient.player && var4.isInvisible()) {
            if (!(minecraftClient.player.squaredDistanceTo(var4) > 4096.0) && this.isStaff(var4.getGameProfile().name())) {
               this.addSignal(1, var4.getGameProfile().name(), true, 35, 5000L);
            }
         }
      }
   }

   private void proveVanishedStaff() {
      if (!this.lastServerName.isEmpty() && this.onServer.contains(this.lastServerName.toLowerCase(Locale.ROOT))) {
         Long var1 = this.provenAt.get(this.lastServerName.toLowerCase(Locale.ROOT));
         if (var1 != null && System.currentTimeMillis() - var1 <= 600000L) {
            this.addSignal(4, this.lastServerName, true, 30, 3000L);
         }
      }
   }

   private void addSignal(int i, String string, boolean bl, int j, long l) {
      if (string != null && !string.isEmpty()) {
         long var7 = System.currentTimeMillis();
         String var9 = i + "|" + string.toLowerCase(Locale.ROOT);
         Long var10 = this.signalCooldown.get(var9);
         if (var10 == null || var7 - var10 >= l) {
            this.signalCooldown.put(var9, var7);
            this.signals.addLast(new StaffAlertModule.Signal(i, string, bl, j, var7));
            this.evaluateCritical();
         }
      }
   }

   private void evaluateCritical() {
      long var1 = System.currentTimeMillis();

      while (!this.signals.isEmpty() && var1 - this.signals.peekFirst().at > 15000L) {
         this.signals.removeFirst();
      }

      int var3 = 0;
      int var4 = 0;
      int var5 = 0;
      int var6 = 0;
      String var7 = null;

      for (StaffAlertModule.Signal var9 : this.signals) {
         switch (var9.type) {
            case 1:
               var3 = Math.max(var3, var9.weight);
               break;
            case 2:
               var4 = Math.max(var4, var9.weight);
               break;
            case 3:
               var5 = Math.max(var5, var9.weight);
               break;
            case 4:
               var6 = Math.max(var6, var9.weight);
         }

         if (var9.proof && plausible(var9.subject)) {
            var7 = var9.subject;
         }
      }

      int var11 = (var3 > 0 ? 1 : 0) + (var4 > 0 ? 1 : 0) + (var5 > 0 ? 1 : 0) + (var6 > 0 ? 1 : 0);
      int var12 = var3 + var4 + var5 + var6;
      if (var7 != null && var11 >= 2 && var12 >= 35) {
         if (var1 - this.lastCriticalMs >= 12000L) {
            this.lastCriticalMs = var1;
            MinecraftClient var10 = MinecraftClient.getInstance();
            this.onServer.add(var7.toLowerCase(Locale.ROOT));
            this.provenAt.put(var7.toLowerCase(Locale.ROOT), var1);
            this.show(var10, var7, true);
         }
      }
   }

   private void rememberBoom(double d, double e, double f, String string) {
      float var8 = 4.0F;

      try {
         var8 = Float.parseFloat(string);
      } catch (NumberFormatException var10) {
         var8 = 4.0F;
      }

      this.booms.addLast(new StaffAlertModule.Boom(d, e, f, Math.max(2.0, var8 + 4.0), System.currentTimeMillis()));

      while (this.booms.size() > 8) {
         this.booms.removeFirst();
      }
   }

   private boolean visibleCause(MinecraftClient minecraftClient, double d, double e, double f) {
      if (minecraftClient.world == null) {
         return true;
      } else {
         for (Entity var9 : minecraftClient.world.getEntities()) {
            if (var9 != minecraftClient.player) {
               double var10 = var9.squaredDistanceTo(d, e, f);
               if (var9 instanceof PlayerEntity var12) {
                  if (!var12.isInvisible() && var10 <= 16.0) {
                     return true;
                  }
               } else if (var9 instanceof ProjectileEntity && var10 <= 16.0) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean explosionNear(double d, double e, double f) {
      long var7 = System.currentTimeMillis();

      for (StaffAlertModule.Boom var10 : this.booms) {
         if (var7 - var10.at <= 3000L) {
            double var11 = var10.x - d;
            double var13 = var10.y - e;
            double var15 = var10.z - f;
            if (var11 * var11 + var13 * var13 + var15 * var15 <= var10.radius * var10.radius) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean poweredDoor(MinecraftClient minecraftClient, double d, double e, double f, String string) {
      String var9 = pathOf(string);
      if (minecraftClient.world != null && (var9.contains("door") || var9.contains("trapdoor"))) {
         long var10 = System.currentTimeMillis();
         int var12 = (int)Math.floor(d);
         int var13 = (int)Math.floor(e);
         int var14 = (int)Math.floor(f);

         for (int var15 = -1; var15 <= 1; var15++) {
            this.cursor.set(var12, var13 + var15, var14);
            BlockState var16 = minecraftClient.world.getBlockState(this.cursor);
            long var17 = this.cursor.asLong();
            boolean var19 = var16.contains(Properties.POWERED) && Boolean.TRUE.equals(var16.get(Properties.POWERED));
            if (var19 || minecraftClient.world.isReceivingRedstonePower(this.cursor)) {
               this.poweredDoors.put(var17, var10 + 5000L);
               return true;
            }

            if (this.poweredDoors.getOrDefault(var17, 0L) > var10) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean selfPlacedNear(double d, double e, double f) {
      Long var7 = this.selfPlaced.get(BlockPos.ofFloored(d, e, f).asLong());
      return var7 != null && var7 > System.currentTimeMillis();
   }

   private boolean villagerDoor(MinecraftClient minecraftClient, double d, double e, double f, String string) {
      String var9 = pathOf(string);
      boolean var10 = var9.contains("door") && !var9.contains("trapdoor") || var9.contains("fence_gate");
      if (var10 && minecraftClient.world != null) {
         for (Entity var12 : minecraftClient.world.getEntities()) {
            if (var12.getType() == EntityType.VILLAGER && var12.squaredDistanceTo(d, e, f) <= 9.0) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean ambientSmoke(MinecraftClient minecraftClient, double d, double e, double f, String string) {
      if (string.contains("smoke") && minecraftClient.world != null) {
         int var9 = (int)Math.floor(d);
         int var10 = (int)Math.floor(e);
         int var11 = (int)Math.floor(f);

         for (int var12 = -2; var12 <= 2; var12++) {
            for (int var13 = -2; var13 <= 2; var13++) {
               for (int var14 = -2; var14 <= 2; var14++) {
                  this.cursor.set(var9 + var12, var10 + var13, var11 + var14);
                  String var15 = Registries.BLOCK.getId(minecraftClient.world.getBlockState(this.cursor).getBlock()).getPath();
                  if (var15.contains("campfire")
                     || var15.contains("furnace")
                     || var15.contains("smoker")
                     || var15.contains("torch")
                     || var15.contains("fire")
                     || var15.contains("candle")
                     || var15.contains("respawn_anchor")) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean particleBurst(String string, long l) {
      ArrayDeque<Long> var4 = this.particleBursts.computeIfAbsent(string, stringx -> new ArrayDeque<>());
      var4.addLast(l);

      while (!var4.isEmpty() && l - var4.peekFirst() > 2000L) {
         var4.removeFirst();
      }

      if (var4.size() < 2) {
         return false;
      } else {
         var4.clear();
         return true;
      }
   }

   private static String located(MinecraftClient minecraftClient, double d, double e, double f) {
      ClientPlayerEntity var7 = minecraftClient.player;
      if (var7 == null) {
         return "nearby";
      } else {
         double var8 = d - var7.getX();
         double var10 = f - var7.getZ();
         long var12 = Math.round(Math.sqrt(var7.squaredDistanceTo(d, e, f)));
         String var14 = var10 < -1.0 ? "N" : (var10 > 1.0 ? "S" : "");
         String var15 = var8 > 1.0 ? "E" : (var8 < -1.0 ? "W" : "");
         String var16 = var14 + var15;
         return var16.isEmpty() ? var12 + "m" : var12 + "m " + var16;
      }
   }

   private static boolean containerSound(String string) {
      String var1 = pathOf(string);
      return var1.contains("chest") || var1.contains("barrel") || var1.contains("shulker");
   }

   private static boolean suspiciousSound(String string) {
      String var1 = pathOf(string);
      return var1.contains("chest.open")
         || var1.contains("chest.close")
         || var1.contains("chest.locked")
         || var1.contains("barrel.open")
         || var1.contains("barrel.close")
         || var1.contains("shulker_box.open")
         || var1.contains("shulker_box.close")
         || var1.contains("door.open")
         || var1.contains("door.close")
         || var1.contains("fence_gate.open")
         || var1.contains("fence_gate.close")
         || var1.contains("button.click")
         || var1.contains("lever.click");
   }

   private static boolean suspiciousParticle(String string) {
      String var1 = pathOf(string);
      return var1.contains("crit") || var1.contains("enchanted_hit") || var1.contains("damage_indicator") || var1.contains("smoke") || var1.equals("block");
   }

   private static String soundId(RegistryEntry<SoundEvent> registryEntry) {
      return registryEntry != null && registryEntry.value() != null && ((SoundEvent)registryEntry.value()).id() != null
         ? ((SoundEvent)registryEntry.value()).id().toString()
         : "";
   }

   private static String particleId(ParticleEffect particleEffect) {
      if (particleEffect != null && particleEffect.getType() != null) {
         Identifier var1 = Registries.PARTICLE_TYPE.getId(particleEffect.getType());
         return var1 == null ? "" : var1.toString();
      } else {
         return "";
      }
   }

   private static String pathOf(String string) {
      if (string != null && !string.isEmpty()) {
         int var1 = string.indexOf(58);
         String var2 = var1 >= 0 && var1 + 1 < string.length() ? string.substring(var1 + 1) : string;
         return var2.toLowerCase(Locale.ROOT);
      } else {
         return "";
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class Body {
      boolean present;
      int tick;
      long lostAt;
   }

   @Environment(EnvType.CLIENT)
   private record Boom(double x, double y, double z, double radius, long at) {
   }

   @Environment(EnvType.CLIENT)
   private record Leave(String text, long at) {
   }

   @Environment(EnvType.CLIENT)
   private record Note(int kind, UUID id, String text) {
   }

   @Environment(EnvType.CLIENT)
   private record Pending(UUID id, String name, int due) {
   }

   @Environment(EnvType.CLIENT)
   private record Sense(int kind, int entityId, double x, double y, double z, String text) {
   }

   @Environment(EnvType.CLIENT)
   private record Signal(int type, String subject, boolean proof, int weight, long at) {
   }
}
