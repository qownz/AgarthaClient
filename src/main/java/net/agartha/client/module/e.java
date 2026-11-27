package net.agartha.client.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class e {
   private static final Logger LOGGER = LoggerFactory.getLogger("Agartha");
   private static final List<d> PROVIDERS = new ArrayList<>(List.of(new a()));
   private static volatile List<b> modules;
   private static Map<String, b> byName;
   private static Map<Class<?>, b> byClass;
   private static int revision;
   private static List<b> keyboundModules = List.of();
   private static int keyboundCacheRevision = -1;

   private e() {
   }

   public static synchronized void registerProvider(d arg) {
      if (modules != null) {
         throw new IllegalStateException("ModuleRegistry is frozen — providers must register during client init, before the first read");
      } else {
         PROVIDERS.add(arg);
      }
   }

   public static List<b> all() {
      List var0 = modules;
      if (var0 == null) {
         b();
         var0 = modules;
      }

      return var0;
   }

   public static b byName(String string) {
      all();
      return byName.get(string);
   }

   public static <T extends b> T get(Class<T> class_) {
      all();
      return (T)class_.cast(byClass.get(class_));
   }

   public static int revision() {
      all();
      return revision;
   }

   public static List<b> keyboundModules() {
      all();
      if (keyboundCacheRevision != revision) {
         a();
      }

      return keyboundModules;
   }

   public static void invalidateKeybindCache() {
      keyboundCacheRevision = -1;
   }

   private static void a() {
      keyboundModules = all().stream().filter(arg -> {
         int var1 = arg.keybind();
         return var1 > 0 && var1 < 1050;
      }).toList();
      keyboundCacheRevision = revision;
   }

   private static synchronized void b() {
      if (modules == null) {
         ArrayList var0 = new ArrayList();
         LinkedHashMap var1 = new LinkedHashMap();
         HashMap var2 = new HashMap();

         for (d var4 : PROVIDERS) {
            int var5 = var0.size();

            for (b var7 : var4.createModules()) {
               if (var1.putIfAbsent(var7.name(), var7) != null) {
                  LOGGER.warn("[Agartha] Duplicate module name '{}' from provider '{}' — skipped", var7.name(), var4.id());
               } else {
                  var0.add(var7);
                  var2.putIfAbsent(var7.getClass(), var7);
               }
            }

            LOGGER.info("[Agartha] Module provider '{}' contributed {} modules", var4.id(), var0.size() - var5);
         }

         byName = var1;
         byClass = var2;
         revision++;
         modules = List.copyOf(var0);
      }
   }
}
