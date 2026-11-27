package net.agartha.client.module.impl.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import imgui.ImDrawList;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.d.a_pkg.i;
import net.agartha.client.d.a_pkg.o;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.module.b;
import net.agartha.client.module.f;
import net.agartha.client.module.a_pkg.c;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.util.a_o;
import net.agartha.client.util.ac;
import net.agartha.client.util.ad;
import net.agartha.client.util.l;
import net.agartha.client.util.w;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public final class PlayerBypassModule extends b implements f {
   private static final int RENDER_DISTANCE_CHUNKS = 16;
   private static final double PLATE_HEIGHT = 0.35;
   private static final float LINE_WIDTH = 1.5F;
   private static final int YELLOW = -13312;
   static final int BASE_CEILING_Y = 62;
   static final int NIBBLES_PER_SECTION = 2048;
   private final net.agartha.client.module.a_pkg.b customColor = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Custom Color", true)
   );
   private final c color = (c)this.addSetting(new c("Color", -13312));
   private final net.agartha.client.module.a_pkg.b themeColor = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Theme Color", false)
   );
   private final j plateY = (j)this.addSetting(new j("Plate Y", 60.0F, -64.0F, 320.0F).whole());
   private final net.agartha.client.module.a_pkg.b tracers = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Tracers", false));
   private final Map<Long, PlayerBypassModule.Snapshot> snapshots = new HashMap<>();
   private final Set<Long> alerted = new HashSet<>();
   private final Map<Long, PlayerBypassModule.Snapshot> held = new HashMap<>();
   private final PlayerBypassModule.EntryGuard entryGuard = new PlayerBypassModule.EntryGuard();
   private final PlayerBypassModule.ProtectedChunks protectedChunks = new PlayerBypassModule.ProtectedChunks();
   private ClientWorld level;
   private final a_o fade = new a_o(0.7F, 0.5F);
   private final net.agartha.client.util.a_o.a fadingPlate = this::drawPlate;
   private int frameLine;
   private int frameFill;
   private ImDrawList tracerList;
   private MinecraftClient tracerMc;
   private float tracerW;
   private float tracerH;
   private float tracerOriginX;
   private float tracerOriginY;
   private int tracerColor;
   private final net.agartha.client.util.a_o.a plateTracer = this::tracerTo;

   public PlayerBypassModule() {
      super("Player Bypass", "Marks chunks with a hidden underground base.", net.agartha.client.module.c.DONUT);
      ac.wireExclusiveColorSource(this.customColor, this.themeColor, this.color, false);
   }

   private int plateColor() {
      return ac.argb(this.themeColor, this.customColor, this.color, -13312);
   }

   protected void onDisable() {
      this.snapshots.clear();
      this.alerted.clear();
      this.held.clear();
      this.entryGuard.reset();
      this.level = null;
      this.protectedChunks.flush();
      this.protectedChunks.select(null, null);
   }

   @net.agartha.client.d.f
   public void onTick(q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      this.syncLevel(var2);
      if (this.level != null && var2.player != null) {
         this.entryGuard.update(var2.player.getChunkPos().toLong(), this::loaded, this::flaggedOrHeld, this.protectedChunks);
      }

      this.protectedChunks.tickAutosave();
   }

   @net.agartha.client.d.f
   public void onPacketReceive(i arg) {
      int var2;
      int var3;
      LightData var4;
      if (arg.packet instanceof ChunkDataS2CPacket var6) {
         var2 = var6.getChunkX();
         var3 = var6.getChunkZ();
         var4 = var6.getLightData();
      } else {
         if (!(arg.packet instanceof LightUpdateS2CPacket var5)) {
            return;
         }

         var2 = var5.getChunkX();
         var3 = var5.getChunkZ();
         var4 = var5.getData();
      }

      MinecraftClient var10 = MinecraftClient.getInstance();
      ClientWorld var12 = var10.world;
      this.syncLevel(var10);
      if (var12 != null && var4 != null) {
         int var8 = var12.countVerticalSections();
         int var9 = var12.getBottomSectionCoord();
         if (var10.player != null && var10.player.getY() < 0.0) {
            this.hold(var2, var3, var4, var8, var9);
         } else {
            this.ingest(var2, var3, var4, var8, var9);
         }
      }
   }

   private void syncLevel(MinecraftClient minecraftClient) {
      ClientWorld var2 = minecraftClient.world;
      if (var2 != this.level) {
         this.snapshots.clear();
         this.alerted.clear();
         this.held.clear();
         this.entryGuard.reset();
         this.level = var2;
         this.protectedChunks.flush();
         if (var2 == null) {
            this.protectedChunks.select(null, null);
         } else {
            this.protectedChunks.select(worldKey(minecraftClient), var2.getRegistryKey().getValue().toString());
         }
      }
   }

   private static String worldKey(MinecraftClient minecraftClient) {
      IntegratedServer var1 = minecraftClient.getServer();
      if (var1 != null) {
         Path var3 = var1.getSavePath(WorldSavePath.ROOT).normalize().getFileName();
         return "singleplayer/" + (var3 == null ? "world" : var3.toString());
      } else {
         ServerInfo var2 = minecraftClient.getCurrentServerEntry();
         return PlayerBypassModule.ProtectedChunks.serverKey(var2 == null ? null : var2.address);
      }
   }

   private void ingest(int i, int j, LightData lightData, int k, int l) {
      long var6 = ChunkPos.toLong(i, j);
      if (this.held.containsKey(var6)) {
         this.hold(i, j, lightData, k, l);
      }

      PlayerBypassModule.Snapshot var8 = this.snapshots.get(var6);
      boolean var9 = var8 != null && this.isFlagged(var6, var8);
      if (var8 == null || var8.sectionCount != k || var8.bottomSectionY != l) {
         var8 = new PlayerBypassModule.Snapshot(k, l);
         this.snapshots.put(var6, var8);
      }

      try {
         applySky(lightData, var8.sky);
      } catch (RuntimeException var11) {
         AgarthaClientMod.LOGGER.error("Player Bypass light ingest failed for chunk {}, {}", new Object[]{i, j, var11});
         return;
      }

      boolean var10 = var8.hasBaseSignal();
      if (var10 && !this.protectedChunks.flagAllowed(var6, var9)) {
         this.snapshots.remove(var6);
      } else {
         if (var10) {
            this.alerted.add(var6);
         } else if (!this.alerted.contains(var6)) {
            this.snapshots.remove(var6);
         }
      }
   }

   private void hold(int i, int j, LightData lightData, int k, int l) {
      long var6 = ChunkPos.toLong(i, j);
      PlayerBypassModule.Snapshot var8 = this.held.get(var6);
      if (var8 == null || var8.sectionCount != k || var8.bottomSectionY != l) {
         var8 = new PlayerBypassModule.Snapshot(k, l);
      }

      try {
         applySky(lightData, var8.sky);
      } catch (RuntimeException var10) {
         AgarthaClientMod.LOGGER.error("Player Bypass light hold failed for chunk {}, {}", new Object[]{i, j, var10});
         return;
      }

      if (var8.hasBaseSignal() && this.protectedChunks.flagAllowed(var6, false)) {
         this.held.put(var6, var8);
      } else {
         this.held.remove(var6);
      }
   }

   private boolean isFlagged(long l, PlayerBypassModule.Snapshot snapshot) {
      return this.alerted.contains(l) || snapshot.hasBaseSignal();
   }

   private boolean flaggedOrHeld(long l) {
      PlayerBypassModule.Snapshot var3 = this.snapshots.get(l);
      return var3 != null && this.isFlagged(l, var3) || this.held.containsKey(l);
   }

   private boolean loaded(long l) {
      return this.level != null && this.level.getChunkManager().isChunkLoaded(ChunkPos.getPackedX(l), ChunkPos.getPackedZ(l));
   }

   @net.agartha.client.d.f
   public void onRender3D(o arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null) {
         int var3 = this.plateColor();
         int var4 = var3 & 16777215;
         int var5 = var3 >>> 24 & 0xFF;
         this.frameLine = var5 << 24 | var4;
         this.frameFill = var5 / 4 << 24 | var4;
         this.fade.begin();
         int var6 = var2.player.getChunkPos().x;
         int var7 = var2.player.getChunkPos().z;
         int var8 = (int)Math.floor(this.plateY.get());

         for (Entry var10 : this.snapshots.entrySet()) {
            long var11 = (Long)var10.getKey();
            if (this.isFlagged(var11, (PlayerBypassModule.Snapshot)var10.getValue())) {
               int var13 = ChunkPos.getPackedX(var11);
               int var14 = ChunkPos.getPackedZ(var11);
               if (Math.abs(var13 - var6) <= 16 && Math.abs(var14 - var7) <= 16) {
                  float var15 = this.fade.show(var11);
                  if (!l.columnOutOfView(var13, var14, var8, var8)) {
                     this.drawPlate(var11, var15);
                  }
               }
            }
         }

         this.fade.sweep(this.fadingPlate);
      }
   }

   private void drawPlate(long l, float f) {
      if (!(f <= 0.01F)) {
         double var4 = ChunkPos.getPackedX(l) << 4;
         double var6 = ChunkPos.getPackedZ(l) << 4;
         double var8 = 8.0 * (1.0 - a_o.pop(f));
         double var10 = net.agartha.client.util.o.y(2, l, var4, this.plateY.get(), var6, var4 + 16.0, var6 + 16.0, 0.35) + a_o.rise(f);
         Box var12 = new Box(var4 + var8, var10, var6 + var8, var4 + 16.0 - var8, var10 + 0.35, var6 + 16.0 - var8);
         if (net.agartha.client.util.l.isSafeCuboid(var12)) {
            w.plate(2, var12, scaleAlpha(this.frameLine, f), 1.5F, scaleAlpha(this.frameFill, f));
         }
      }
   }

   private static int scaleAlpha(int i, float f) {
      int var2 = Math.round((i >>> 24 & 0xFF) * f);
      return var2 << 24 | i & 16777215;
   }

   public boolean isTracerOverlayEnabled() {
      return this.isEnabled() && this.tracers.get() && !this.fade.isEmpty();
   }

   public void renderTracerOverlay(ImDrawList imDrawList, float f, float g) {
      if (imDrawList != null && this.isTracerOverlayEnabled()) {
         MinecraftClient var4 = MinecraftClient.getInstance();
         if (var4.world != null && var4.player != null) {
            this.tracerList = imDrawList;
            this.tracerMc = var4;
            this.tracerW = f;
            this.tracerH = g;
            this.tracerOriginX = ad.originX(f);
            this.tracerOriginY = ad.originY("Crosshair", g);
            this.tracerColor = 0xFF000000 | this.plateColor() & 16777215;
            this.fade.forEachVisible(this.plateTracer);
            this.tracerList = null;
         }
      }
   }

   private void tracerTo(long l, float f) {
      double var4 = ChunkPos.getPackedX(l) << 4;
      double var6 = ChunkPos.getPackedZ(l) << 4;
      double var8 = net.agartha.client.util.o.y(2, l, var4, this.plateY.get(), var6, var4 + 16.0, var6 + 16.0, 0.35) + a_o.rise(f);
      ad.draw(
         this.tracerList,
         this.tracerMc,
         this.tracerW,
         this.tracerH,
         this.tracerOriginX,
         this.tracerOriginY,
         new Vec3d(var4 + 8.0, var8 + 0.175, var6 + 8.0),
         tracerScale(this.tracerColor, f),
         1.5F
      );
   }

   private static int tracerScale(int i, float f) {
      int var2 = Math.round((i >>> 24 & 0xFF) * f);
      return var2 << 24 | i & 16777215;
   }

   static void applySky(LightData lightData, PlayerBypassModule.LightSectionStatus[] lightSectionStatuss) {
      applyTrack(lightData.getInitedSky(), lightData.getUninitedSky(), lightData.getSkyNibbles(), lightSectionStatuss);
   }

   static void applyTrack(BitSet bitSet, BitSet bitSet2, List<byte[]> list, PlayerBypassModule.LightSectionStatus[] lightSectionStatuss) {
      int var4 = 0;

      for (int var5 = 0; var5 < lightSectionStatuss.length; var5++) {
         if (bitSet.get(var5)) {
            byte[] var6 = var4 < list.size() ? (byte[])list.get(var4) : null;
            var4++;
            lightSectionStatuss[var5] = statusOf(var6);
         } else if (bitSet2.get(var5)) {
            lightSectionStatuss[var5] = PlayerBypassModule.LightSectionStatus.EMPTY_MASK;
         }
      }
   }

   public static PlayerBypassModule.LightSectionStatus statusOf(byte[] bs) {
      if (bs != null && bs.length == 2048) {
         boolean var1 = true;
         boolean var2 = true;

         for (byte var6 : bs) {
            if (var6 != 0) {
               var1 = false;
            }

            if (var6 != -1) {
               var2 = false;
            }

            if (!var1 && !var2) {
               return PlayerBypassModule.LightSectionStatus.NORMAL;
            }
         }

         if (var1) {
            return PlayerBypassModule.LightSectionStatus.ZEROED_PRESENT;
         } else {
            return var2 ? PlayerBypassModule.LightSectionStatus.UNIFORM_MAX : PlayerBypassModule.LightSectionStatus.NORMAL;
         }
      } else {
         return PlayerBypassModule.LightSectionStatus.UNKNOWN;
      }
   }

   public static boolean baseSignal(PlayerBypassModule.LightSectionStatus[] lightSectionStatuss, int i, int j) {
      for (int var3 = 1; var3 < i + 1; var3++) {
         int var4 = (j + var3 - 1) * 16 + 15;
         if (var4 < 62 && lightSectionStatuss[var3] == PlayerBypassModule.LightSectionStatus.ZEROED_PRESENT) {
            return true;
         }
      }

      return false;
   }

   @Environment(EnvType.CLIENT)
   private static final class EntryGuard {
      private final Set<Long> waiting = new HashSet<>();
      private boolean inChunk;
      private long chunk;
      private boolean entryPending;

      void reset() {
         this.inChunk = false;
         this.entryPending = false;
         this.waiting.clear();
      }

      void update(
         long l,
         PlayerBypassModule.LongPredicate longPredicate,
         PlayerBypassModule.LongPredicate longPredicate2,
         PlayerBypassModule.ProtectedChunks protectedChunks
      ) {
         if (!this.inChunk || l != this.chunk) {
            this.inChunk = true;
            this.chunk = l;
            this.entryPending = true;
            this.waiting.clear();
         }

         if (!this.entryPending) {
            if (!this.waiting.isEmpty()) {
               Iterator var12 = this.waiting.iterator();

               while (var12.hasNext()) {
                  long var13 = (Long)var12.next();
                  if (longPredicate.test(var13)) {
                     var12.remove();
                     if (!longPredicate2.test(var13)) {
                        protectedChunks.protect(var13);
                     }
                  }
               }
            }
         } else if (longPredicate.test(l)) {
            this.entryPending = false;
            if (!longPredicate2.test(l)) {
               int var6 = ChunkPos.getPackedX(l);
               int var7 = ChunkPos.getPackedZ(l);

               for (int var8 = var6 - 2; var8 <= var6 + 2; var8++) {
                  for (int var9 = var7 - 2; var9 <= var7 + 2; var9++) {
                     long var10 = ChunkPos.toLong(var8, var9);
                     if (!longPredicate.test(var10)) {
                        this.waiting.add(var10);
                     } else if (!longPredicate2.test(var10)) {
                        protectedChunks.protect(var10);
                     }
                  }
               }
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum LightSectionStatus {
      NORMAL,
      EMPTY_MASK,
      ZEROED_PRESENT,
      UNIFORM_MAX,
      UNKNOWN;
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   private interface LongPredicate {
      boolean test(long l);
   }

   @Environment(EnvType.CLIENT)
   static final class ProtectedChunks {
      static final int RADIUS = 2;
      private static final int CONFIG_VERSION = 1;
      private static final long SAVE_DELAY_NANOS = 10000000000L;
      private static final String DEFAULT_PORT = ":25565";
      private final Map<String, PlayerBypassModule.ProtectedChunks.Bucket> buckets = new HashMap<>();
      private final ExecutorService writer = Executors.newSingleThreadExecutor(runnable -> {
         Thread var1 = new Thread(runnable, "Agartha Player Bypass saves");
         var1.setDaemon(true);
         return var1;
      });
      private final ConcurrentHashMap<Path, long[]> queued = new ConcurrentHashMap<>();
      private PlayerBypassModule.ProtectedChunks.Bucket active;
      private boolean dirty;
      private long dirtySince;
      private volatile boolean writeFailed;

      static String serverKey(String string) {
         String var1 = string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
         if (var1.endsWith(":25565")) {
            var1 = var1.substring(0, var1.length() - ":25565".length());
         }

         return var1.isEmpty() ? "unknown" : var1;
      }

      void select(String string, String string2) {
         if (string != null && string2 != null) {
            String var3 = fileName(string, string2);
            PlayerBypassModule.ProtectedChunks.Bucket var4 = this.buckets.get(var3);
            if (var4 == null) {
               var4 = new PlayerBypassModule.ProtectedChunks.Bucket(directory().resolve(var3), string, string2);
               load(var4);
               this.buckets.put(var3, var4);
            }

            this.active = var4;
         } else {
            this.active = null;
         }
      }

      boolean isProtected(long l) {
         return this.active != null && this.active.chunks.contains(l);
      }

      boolean protect(long l) {
         if (this.active != null && this.active.chunks.add(l)) {
            this.changed(this.active);
            return true;
         } else {
            return false;
         }
      }

      boolean flagAllowed(long l, boolean bl) {
         return bl || !this.isProtected(l);
      }

      void tickAutosave() {
         this.retryFailedWrites();
         if (this.dirty && System.nanoTime() - this.dirtySince >= 10000000000L) {
            this.saveChanged();
         }
      }

      void flush() {
         this.retryFailedWrites();
         if (this.dirty) {
            this.saveChanged();
         }
      }

      private void changed(PlayerBypassModule.ProtectedChunks.Bucket bucket) {
         if (!bucket.readOnly) {
            bucket.dirty = true;
            if (!this.dirty) {
               this.dirty = true;
               this.dirtySince = System.nanoTime();
            }
         }
      }

      private void retryFailedWrites() {
         if (this.writeFailed) {
            this.writeFailed = false;

            for (PlayerBypassModule.ProtectedChunks.Bucket var2 : this.buckets.values()) {
               if (var2.writeFailed) {
                  var2.writeFailed = false;
                  this.changed(var2);
               }
            }
         }
      }

      private void saveChanged() {
         this.dirty = false;

         for (PlayerBypassModule.ProtectedChunks.Bucket var2 : this.buckets.values()) {
            if (var2.dirty) {
               var2.dirty = false;
               this.save(var2);
            }
         }
      }

      private void save(PlayerBypassModule.ProtectedChunks.Bucket bucket) {
         long[] var2 = new long[bucket.chunks.size()];
         int var3 = 0;

         for (Long var5 : bucket.chunks) {
            var2[var3++] = var5;
         }

         if (this.queued.put(bucket.file, var2) == null) {
            CompletableFuture.runAsync(() -> this.writeQueued(bucket), this.writer);
         }
      }

      private void writeQueued(PlayerBypassModule.ProtectedChunks.Bucket bucket) {
         long[] var2 = this.queued.remove(bucket.file);
         if (var2 != null) {
            try {
               write(bucket, var2);
            } catch (RuntimeException | IOException var4) {
               AgarthaClientMod.LOGGER.warn("Failed to save Player Bypass protected chunks to {}", bucket.file, var4);
               bucket.writeFailed = true;
               this.writeFailed = true;
            }
         }
      }

      private static void write(PlayerBypassModule.ProtectedChunks.Bucket bucket, long[] ls) throws IOException {
         Files.createDirectories(bucket.file.getParent());
         Path var2 = bucket.file.resolveSibling(bucket.file.getFileName() + ".tmp");
         JsonObject var3 = new JsonObject();
         var3.addProperty("version", 1);
         var3.addProperty("server", bucket.server);
         var3.addProperty("dimension", bucket.dimension);
         var3.addProperty("count", ls.length);
         JsonArray var4 = new JsonArray();

         for (long var8 : ls) {
            var4.add(ChunkPos.getPackedX(var8));
            var4.add(ChunkPos.getPackedZ(var8));
         }

         var3.add("chunks", var4);
         Files.writeString(var2, var3.toString(), StandardCharsets.UTF_8);

         try {
            Files.move(var2, bucket.file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (IOException var10) {
            Files.move(var2, bucket.file, StandardCopyOption.REPLACE_EXISTING);
         }
      }

      private static void load(PlayerBypassModule.ProtectedChunks.Bucket bucket) {
         if (Files.isRegularFile(bucket.file)) {
            int var1 = 0;

            try (BufferedReader var2 = Files.newBufferedReader(bucket.file)) {
               JsonElement var3 = JsonParser.parseReader(var2);
               if (!var3.isJsonObject()) {
                  throw new IOException("not an object");
               }

               JsonObject var4 = var3.getAsJsonObject();
               if (var4.has("version") && var4.get("version").isJsonPrimitive()) {
                  var1 = var4.get("version").getAsInt();
               }

               if (var4.has("chunks") && var4.get("chunks").isJsonArray()) {
                  JsonArray var5 = var4.getAsJsonArray("chunks");
                  long var6 = Long.MIN_VALUE;

                  for (int var8 = 0; var8 < var5.size(); var8++) {
                     JsonElement var9 = var5.get(var8);
                     if (var9.isJsonPrimitive() && var9.getAsJsonPrimitive().isNumber()) {
                        double var10 = var9.getAsDouble();
                        if (var10 != Math.rint(var10) || var10 < -2.1474836E9F || var10 > 2.147483647E9) {
                           var6 = Long.MIN_VALUE;
                        } else if ((var8 & 1) == 0) {
                           var6 = (long)var10;
                        } else if (var6 != Long.MIN_VALUE) {
                           bucket.chunks.add(ChunkPos.toLong((int)var6, (int)var10));
                        }
                     } else {
                        var6 = Long.MIN_VALUE;
                     }
                  }
               }
            } catch (RuntimeException | IOException var14) {
               AgarthaClientMod.LOGGER.warn("Failed to load Player Bypass protected chunks from {}; keeping the file", bucket.file, var14);
               bucket.chunks.clear();
               bucket.readOnly = true;
               return;
            }

            if (var1 > 1) {
               AgarthaClientMod.LOGGER
                  .warn("Player Bypass protected chunks file {} is version {}, newer than supported {}", new Object[]{bucket.file, var1, 1});
               bucket.chunks.clear();
               bucket.readOnly = true;
            }
         }
      }

      private static String fileName(String string, String string2) {
         try {
            byte[] var2 = MessageDigest.getInstance("SHA-256").digest((string + "\n" + string2).getBytes(StandardCharsets.UTF_8));
            String var3 = HexFormat.of().formatHex(var2, 0, 8);
            return readable(string) + "_" + readable(string2) + "-" + var3 + ".json";
         } catch (NoSuchAlgorithmException var4) {
            throw new IllegalStateException("SHA-256 is always available", var4);
         }
      }

      private static String readable(String string) {
         StringBuilder var1 = new StringBuilder(Math.min(string.length(), 40));

         for (int var2 = 0; var2 < string.length() && var1.length() < 40; var2++) {
            char var3 = Character.toLowerCase(string.charAt(var2));
            var1.append((char)((var3 < 'a' || var3 > 'z') && (var3 < '0' || var3 > '9') && var3 != '.' && var3 != '-' ? 95 : var3));
         }

         return var1.toString();
      }

      private static Path directory() {
         return FabricLoader.getInstance().getConfigDir().resolve("agartha").resolve("playerbypass-protected");
      }

      @Environment(EnvType.CLIENT)
      private static final class Bucket {
         final Path file;
         final String server;
         final String dimension;
         final Set<Long> chunks = new HashSet<>();
         boolean readOnly;
         boolean dirty;
         volatile boolean writeFailed;

         Bucket(Path path, String string, String string2) {
            this.file = path;
            this.server = string;
            this.dimension = string2;
         }
      }
   }

   @Environment(EnvType.CLIENT)
   static final class Snapshot {
      final int sectionCount;
      final int bottomSectionY;
      final PlayerBypassModule.LightSectionStatus[] sky;

      Snapshot(int i, int j) {
         this.sectionCount = i;
         this.bottomSectionY = j;
         this.sky = new PlayerBypassModule.LightSectionStatus[i + 2];
         Arrays.fill(this.sky, PlayerBypassModule.LightSectionStatus.UNKNOWN);
      }

      boolean hasBaseSignal() {
         return PlayerBypassModule.baseSignal(this.sky, this.sectionCount, this.bottomSectionY);
      }
   }
}
