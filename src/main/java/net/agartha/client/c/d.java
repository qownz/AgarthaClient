package net.agartha.client.c;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class d {
   private d() {
   }

   public static Path backupOf(Path path) {
      return path.resolveSibling(path.getFileName() + ".bak");
   }

   public static void keepBackup(Path path) {
      try {
         Files.copy(path, backupOf(path), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException var2) {
      }
   }

   public static Path quarantine(Path path) {
      try {
         String var1 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
         Path var2 = path.resolveSibling(path.getFileName() + ".broken-" + var1);
         Files.copy(path, var2, StandardCopyOption.REPLACE_EXISTING);
         return var2;
      } catch (IOException var3) {
         return null;
      }
   }

   public static void writeAtomic(Path path, String string) throws IOException {
      Files.createDirectories(path.getParent());
      Path var2 = path.resolveSibling(path.getFileName() + ".tmp");
      Files.writeString(var2, string);

      try {
         Files.move(var2, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (IOException var4) {
         Files.move(var2, path, StandardCopyOption.REPLACE_EXISTING);
      }
   }
}
