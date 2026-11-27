package net.agartha.client.d;

import java.util.IdentityHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
final class m {
   private static final int FULL_TRACE_LIMIT = 4;
   private static final int SUMMARY_INTERVAL = 256;
   private static final Map<n<?>, Integer> failureCounts = new IdentityHashMap<>();

   private m() {
   }

   static synchronized void report(n<?> arg, c arg2, Exception exception) {
      int var3 = failureCounts.merge(arg, 1, Integer::sum);
      String var4 = arg.owner() == null ? "<static>" : arg.owner().getClass().getName();
      if (var3 <= 4) {
         System.err
            .println(
               "[AgarthaEventBus] Listener threw (failure #"
                  + var3
                  + "): owner="
                  + var4
                  + " event="
                  + arg2.getClass().getSimpleName()
                  + " stage="
                  + arg.stage()
                  + " priority="
                  + arg.priority()
            );
         exception.printStackTrace();
      } else if (var3 % 256 == 0) {
         System.err
            .println(
               "[AgarthaEventBus] Listener still failing (" + var3 + " total): owner=" + var4 + " event=" + arg2.getClass().getSimpleName() + ": " + exception
            );
      }
   }
}
