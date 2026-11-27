package net.agartha.client.c;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.agartha.client.AgarthaClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class b {
   private static final String OLD_DIR = "monster";
   private static final String NEW_DIR = "agartha";
   private static final String[] NOT_PORTABLE = new String[0];

   private b() {
   }

   public static void run() {
      try {
         Path var0 = FabricLoader.getInstance().getConfigDir();
         Path var1 = var0.resolve("monster");
         Path var2 = var0.resolve("agartha");
         if (!Files.isDirectory(var1) || Files.exists(var2)) {
            return;
         }

         Files.createDirectories(var2);
         int var3 = 0;

         try (Stream var4 = Files.list(var1)) {
            for (Path var6 : (Iterable<Path>) var4.toList()) {
               if (Files.isRegularFile(var6) && !a(var6)) {
                  Files.move(var6, var2.resolve(var6.getFileName().toString()));
                  var3++;
               }
            }
         }

         AgarthaClientMod.LOGGER.info("[Agartha] Migrated {} settings file(s) to {}.", var3, var2);
      } catch (Exception var9) {
         AgarthaClientMod.LOGGER.warn("[Agartha] Settings migration skipped: {}", var9.toString());
      }
   }

   private static boolean a(Path path) {
      String var1 = path.getFileName().toString();

      for (String var5 : NOT_PORTABLE) {
         if (var5.equals(var1)) {
            return true;
         }
      }

      return false;
   }
}
