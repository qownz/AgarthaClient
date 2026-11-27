package net.agartha.client.event;

import java.util.ArrayList;
import java.util.Comparator;
import net.agartha.client.event.EventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class EventDiagnostics {
   private EventDiagnostics() {
   }

   public static String dumpListeners() {
      ArrayList<EventType> var0 = new ArrayList<>(EventType.REGISTRY);
      var0.sort(Comparator.comparing(EventType::name));
      StringBuilder var1 = new StringBuilder("=== Event listeners ===\n");

      for (EventType var3 : (Iterable<EventType>) var0) {
         var1.append(var3.name()).append(" (").append(var3.listenerCount()).append(")\n");
         a(var1, "PRE ", var3.list.preSnapshot());
         a(var1, "POST", var3.list.postSnapshot());
      }

      return var1.toString();
   }

   private static void a(StringBuilder stringBuilder, String string, ListenerHandle<?>[] args) {
      for (ListenerHandle var6 : args) {
         Object var7 = var6.owner();
         stringBuilder.append("  ")
            .append(string)
            .append(' ')
            .append(var6.priority())
            .append(' ')
            .append(var7 == null ? "<no-owner>" : var7.getClass().getName())
            .append('\n');
      }
   }
}
