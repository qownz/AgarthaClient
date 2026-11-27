package net.agartha.client.util;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class m {
   private static volatile Consumer<String> listener;
   private static final CopyOnWriteArrayList<Consumer<String>> EXTRA = new CopyOnWriteArrayList<>();

   private m() {
   }

   public static void setListener(Consumer<String> consumer) {
      listener = consumer;
   }

   public static void clearListener() {
      listener = null;
   }

   public static void addListener(Consumer<String> consumer) {
      if (consumer != null && !EXTRA.contains(consumer)) {
         EXTRA.add(consumer);
      }
   }

   public static void removeListener(Consumer<String> consumer) {
      EXTRA.remove(consumer);
   }

   public static void publish(String string) {
      if (string != null && !string.isBlank()) {
         Consumer var1 = listener;
         if (var1 != null) {
            var1.accept(string);
         }

         for (Consumer var3 : EXTRA) {
            try {
               var3.accept(string);
            } catch (RuntimeException var5) {
            }
         }
      }
   }
}
