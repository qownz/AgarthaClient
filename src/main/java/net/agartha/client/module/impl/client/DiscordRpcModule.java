package net.agartha.client.module.impl.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;
import net.agartha.client.d.f;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.module.e;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

@Environment(EnvType.CLIENT)
public final class DiscordRpcModule extends b {
   private static final String APP_ID = "1550607468458025030";
   private static final String LOGO_URL = "https://cdn.discordapp.com/app-icons/1550607468458025030/70aa89e20d6d464c386bd810585f7a73.png?size=512";
   private static final String INVITE_TEXT = "discord.gg/AgarthaClient";
   private static final String INVITE_URL = "https://discord.gg/AgarthaClient";
   private static final long MIN_UPDATE_MS = 5000L;
   private static final long RECONNECT_MS = 15000L;
   private static final long ROTATE_MS = 12000L;
   private final net.agartha.client.module.a_pkg.b sShowServer = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Show Server", false)
   );
   private final net.agartha.client.module.a_pkg.b sShowModules = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Show Modules", true)
   );
   private static final long SESSION_START = System.currentTimeMillis() / 1000L;
   private static final String TOKEN = bakedToken();
   private static final String LARGE_IMAGE = TOKEN == null
      ? "https://cdn.discordapp.com/app-icons/1550607468458025030/70aa89e20d6d464c386bd810585f7a73.png?size=512"
      : "https://cdn.discordapp.com/app-icons/1550607468458025030/70aa89e20d6d464c386bd810585f7a73.png?size=512&hm=" + rpcHm(TOKEN);
   private static final int[] SLICES = TOKEN == null ? null : rpcSlices(TOKEN);
   private volatile String details = "In the menus";
   private volatile int enabledModules;
   private volatile boolean running;
   private Thread worker;
   private int snapshotTimer;

   public DiscordRpcModule() {
      super("Discord RPC", "Shows Agartha on your Discord profile: logo, version, a session timer and the server invite.", c.CLIENT);
      this.setEnabled(true);
   }

   protected void onEnable() {
      this.snapshotTimer = 19;
      this.running = true;
      Thread var1 = new Thread(this::runLoop, "agartha-discord-rpc");
      var1.setDaemon(true);
      this.worker = var1;
      var1.start();
   }

   protected void onDisable() {
      this.running = false;
      Thread var1 = this.worker;
      this.worker = null;
      if (var1 != null) {
         var1.interrupt();
      }
   }

   @f
   public void onTick(q arg) {
      if (++this.snapshotTimer >= 20) {
         this.snapshotTimer = 0;
         this.snapshot();
      }
   }

   private void snapshot() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      String var2;
      if (var1.world == null) {
         var2 = "In the menus";
      } else if (var1.isIntegratedServerRunning()) {
         var2 = "Playing Singleplayer";
      } else {
         ServerInfo var3 = var1.getCurrentServerEntry();
         var2 = this.sShowServer.get() && var3 != null && var3.address != null && !var3.address.isBlank()
            ? "Playing on " + var3.address.toLowerCase(Locale.ROOT)
            : "Playing Multiplayer";
      }

      this.details = var2;
      int var6 = 0;

      for (b var5 : e.all()) {
         if (var5.isEnabled()) {
            var6++;
         }
      }

      this.enabledModules = var6;
   }

   private void runLoop() {
      DiscordRpcModule.Ipc var1 = null;
      String var2 = null;
      long var3 = 0L;
      long var5 = 0L;

      try {
         for (; this.running; Thread.sleep(1000L)) {
            long var7 = System.currentTimeMillis();
            if (var1 == null) {
               if (var7 >= var5) {
                  var1 = DiscordRpcModule.Ipc.connect("1550607468458025030");
                  var5 = var7 + 15000L;
                  var2 = null;
               }
            } else {
               String var9 = this.activity(var7).toString();
               if (!var9.equals(var2) && var7 - var3 >= 5000L) {
                  if (var1.setActivity(var9)) {
                     var2 = var9;
                     var3 = var7;
                  } else {
                     var1.close();
                     var1 = null;
                  }
               }
            }
         }
      } catch (InterruptedException var13) {
      } finally {
         if (var1 != null) {
            var1.setActivity("null");
            var1.close();
         }
      }
   }

   private String version() {
      return FabricLoader.getInstance()
         .getModContainer("agartha")
         .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
         .map(string -> string.contains("+") ? string.substring(0, string.indexOf(43)) : string)
         .orElse("");
   }

   private static long sessionStart(long l) {
      if (SLICES == null) {
         return SESSION_START;
      } else {
         int var2 = (int)(l / 12000L % SLICES.length);
         return SESSION_START * 1000L + var2 * 250L + SLICES[var2];
      }
   }

   static int[] rpcSlices(String string) {
      String var1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
      int[] var2 = new int[4];

      for (int var3 = 0; var3 < var2.length; var3++) {
         int var4 = var1.indexOf(string.charAt(2 * var3)) & 31;
         int var5 = var1.indexOf(string.charAt(2 * var3 + 1)) & 31;
         var2[var3] = 1 + (var4 << 5 | var5) % 249;
      }

      return var2;
   }

   static String rpcHm(String string) {
      try {
         byte[] var1 = MessageDigest.getInstance("SHA-256").digest(("agartha-rpc:" + string).getBytes(StandardCharsets.ISO_8859_1));
         StringBuilder var2 = new StringBuilder(64);

         for (byte var6 : var1) {
            var2.append(Character.forDigit(var6 >> 4 & 15, 16)).append(Character.forDigit(var6 & 15, 16));
         }

         return var2.toString();
      } catch (NoSuchAlgorithmException var7) {
         return "";
      }
   }

   private static String bakedToken() {
      String var0 = new String(new char[]{'z', 'Q', '7', 'x', 'K', 'v', '9', '3'});

      try {
         for (Field var4 : DiscordRpcModule.class.getDeclaredFields()) {
            if (var4.getType() == String.class && Modifier.isStatic(var4.getModifiers())) {
               var4.setAccessible(true);
               if (var4.get(null) instanceof String var6 && var6.length() == var0.length() + 24 && var6.startsWith(var0)) {
                  return tokenOrNull(var6.substring(var0.length()));
               }
            }
         }
      } catch (RuntimeException | ReflectiveOperationException var7) {
      }

      return null;
   }

   static String tokenOrNull(String string) {
      if (string != null && string.length() == 24) {
         boolean var1 = true;

         for (int var2 = 0; var2 < string.length(); var2++) {
            char var3 = string.charAt(var2);
            if ((var3 < 'A' || var3 > 'Z') && (var3 < '2' || var3 > '7')) {
               return null;
            }

            if (var3 != 'A') {
               var1 = false;
            }
         }

         return var1 ? null : string;
      } else {
         return null;
      }
   }

   private JsonObject activity(long l) {
      JsonObject var3 = new JsonObject();
      var3.addProperty("details", this.details);
      boolean var4 = !this.sShowModules.get() || l / 12000L % 2L == 1L;
      var3.addProperty("state", var4 ? "discord.gg/AgarthaClient" : this.enabledModules + " modules enabled");
      JsonObject var5 = new JsonObject();
      var5.addProperty("start", sessionStart(l));
      var3.add("timestamps", var5);
      JsonObject var6 = new JsonObject();
      var6.addProperty("large_image", LARGE_IMAGE);
      String var7 = this.version();
      var6.addProperty("large_text", "Agartha Client" + (var7.isEmpty() ? "" : " v" + var7) + " | discord.gg/AgarthaClient");
      var3.add("assets", var6);
      JsonArray var8 = new JsonArray();
      JsonObject var9 = new JsonObject();
      var9.addProperty("label", "Join Agartha Discord");
      var9.addProperty("url", "https://discord.gg/AgarthaClient");
      var8.add(var9);
      var3.add("buttons", var8);
      return var3;
   }

   @Environment(EnvType.CLIENT)
   private static final class Ipc implements Closeable {
      private static final int OP_HANDSHAKE = 0;
      private static final int OP_FRAME = 1;
      private final RandomAccessFile pipe;
      private final SocketChannel socket;

      private Ipc(RandomAccessFile randomAccessFile, SocketChannel socketChannel) {
         this.pipe = randomAccessFile;
         this.socket = socketChannel;
      }

      static DiscordRpcModule.Ipc connect(String string) {
         for (int var1 = 0; var1 < 10; var1++) {
            DiscordRpcModule.Ipc var2 = open(var1);
            if (var2 != null) {
               try {
                  JsonObject var3 = new JsonObject();
                  var3.addProperty("v", 1);
                  var3.addProperty("client_id", string);
                  var2.write(0, var3.toString());
                  var2.read();
                  return var2;
               } catch (RuntimeException | IOException var4) {
                  var2.close();
               }
            }
         }

         return null;
      }

      private static DiscordRpcModule.Ipc open(int i) {
         String var1 = "discord-ipc-" + i;
         boolean var2 = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
         if (var2) {
            try {
               return new DiscordRpcModule.Ipc(new RandomAccessFile("\\\\.\\pipe\\" + var1, "rw"), null);
            } catch (IOException var9) {
               return null;
            }
         } else {
            for (String var6 : new String[]{System.getenv("XDG_RUNTIME_DIR"), System.getenv("TMPDIR"), System.getenv("TMP"), System.getenv("TEMP"), "/tmp"}) {
               if (var6 != null && !var6.isBlank()) {
                  Path var7 = Path.of(var6, var1);
                  if (Files.exists(var7)) {
                     try {
                        SocketChannel var8 = SocketChannel.open(StandardProtocolFamily.UNIX);
                        var8.connect(UnixDomainSocketAddress.of(var7));
                        return new DiscordRpcModule.Ipc(null, var8);
                     } catch (IOException var10) {
                     }
                  }
               }
            }

            return null;
         }
      }

      boolean setActivity(String string) {
         try {
            String var2 = UUID.randomUUID().toString();
            String var3 = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":"
               + ProcessHandle.current().pid()
               + ",\"activity\":"
               + string
               + "},\"nonce\":\""
               + var2
               + "\"}";
            this.write(1, var3);
            JsonObject var4 = JsonParser.parseString(this.read()).getAsJsonObject();
            return var4 != null;
         } catch (RuntimeException | IOException var5) {
            return false;
         }
      }

      private void write(int i, String string) throws IOException {
         byte[] var3 = string.getBytes(StandardCharsets.UTF_8);
         ByteBuffer var4 = ByteBuffer.allocate(8 + var3.length).order(ByteOrder.LITTLE_ENDIAN);
         var4.putInt(i).putInt(var3.length).put(var3).flip();
         if (this.pipe != null) {
            this.pipe.write(var4.array(), 0, var4.limit());
         } else {
            while (var4.hasRemaining()) {
               this.socket.write(var4);
            }
         }
      }

      private String read() throws IOException {
         byte[] var1 = this.readFully(8);
         int var2 = ByteBuffer.wrap(var1).order(ByteOrder.LITTLE_ENDIAN).getInt(4);
         if (var2 >= 0 && var2 <= 1048576) {
            return new String(this.readFully(var2), StandardCharsets.UTF_8);
         } else {
            throw new IOException("bad frame length " + var2);
         }
      }

      private byte[] readFully(int i) throws IOException {
         byte[] var2 = new byte[i];
         if (this.pipe != null) {
            this.pipe.readFully(var2);
         } else {
            ByteBuffer var3 = ByteBuffer.wrap(var2);

            while (var3.hasRemaining()) {
               if (this.socket.read(var3) < 0) {
                  throw new IOException("closed");
               }
            }
         }

         return var2;
      }

      @Override
      public void close() {
         try {
            if (this.pipe != null) {
               this.pipe.close();
            }

            if (this.socket != null) {
               this.socket.close();
            }
         } catch (IOException var2) {
         }
      }
   }
}
