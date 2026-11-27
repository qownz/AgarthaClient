package net.agartha.client.util.io;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.client.UtilitiesModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ClipboardUtil {
   private ClipboardUtil() {
   }

   public static void copyIfEnabled(File file) {
      UtilitiesModule var1 = ModuleManager.get(UtilitiesModule.class);
      if (var1 != null && var1.screenshotCopy() && file != null) {
         Thread var2 = new Thread(() -> {
            try {
               BufferedImage var1x = ImageIO.read(file);
               if (var1x == null) {
                  return;
               }

               Clipboard var2x = Toolkit.getDefaultToolkit().getSystemClipboard();
               var2x.setContents(new ClipboardUtil.a(var1x), null);
            } catch (Throwable var3) {
            }
         }, "Agartha Screenshot Clipboard");
         var2.setDaemon(true);
         var2.start();
      }
   }

   static {
      try {
         System.setProperty("java.awt.headless", "false");
      } catch (Throwable var1) {
      }
   }

   @Environment(EnvType.CLIENT)
   private record a(BufferedImage image) implements Transferable {
      @Override
      public DataFlavor[] getTransferDataFlavors() {
         return new DataFlavor[]{DataFlavor.imageFlavor};
      }

      @Override
      public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
         return DataFlavor.imageFlavor.equals(dataFlavor);
      }

      @Override
      public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException {
         if (DataFlavor.imageFlavor.equals(dataFlavor)) {
            return this.image;
         } else {
            throw new UnsupportedFlavorException(dataFlavor);
         }
      }
   }
}
