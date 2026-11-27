package net.agartha.client.util.perf;

import com.sun.management.ThreadMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.event.Event;
import net.agartha.client.event.ListenerHandle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class Profiler {
   private static final boolean ENABLED = Boolean.getBoolean("agartha.perf");
   private static final long REPORT_INTERVAL_NS = Math.max(1L, Long.getLong("agartha.perf.intervalSeconds", 10L)) * 1000000000L;
   private static final int MAX_SAMPLES = 8192;
   private static final Map<String, Profiler.a> TIMINGS = ENABLED ? new ConcurrentHashMap<String, Profiler.a>() : Map.<String, Profiler.a>of();
   private static final Map<String, Profiler.a> ALLOCATIONS = ENABLED
      ? new ConcurrentHashMap<String, Profiler.a>()
      : Map.<String, Profiler.a>of();
   private static final Map<String, Profiler.a> COUNTERS = ENABLED ? new ConcurrentHashMap<String, Profiler.a>() : Map.<String, Profiler.a>of();
   private static final AtomicLong NEXT_REPORT_NS = new AtomicLong(System.nanoTime() + REPORT_INTERVAL_NS);
   private static final ThreadMXBean THREAD_BEAN = a();
   private static volatile long previousFrameNs;

   private Profiler() {
   }

   public static boolean enabled() {
      return ENABLED;
   }

   public static long begin() {
      return ENABLED ? System.nanoTime() : 0L;
   }

   public static long allocatedBytes() {
      return ENABLED && THREAD_BEAN != null ? THREAD_BEAN.getThreadAllocatedBytes(Thread.currentThread().threadId()) : 0L;
   }

   public static void record(String string, long l) {
      if (ENABLED && l != 0L) {
         recordNanos(string, System.nanoTime() - l);
      }
   }

   public static void recordNanos(String string, long l) {
      if (ENABLED && l >= 0L) {
         TIMINGS.computeIfAbsent(string, stringx -> new Profiler.a()).add(l);
      }
   }

   public static void recordAllocatedBytes(String string, long l) {
      if (ENABLED && THREAD_BEAN != null && l != 0L) {
         long var3 = allocatedBytes() - l;
         if (var3 >= 0L) {
            ALLOCATIONS.computeIfAbsent(string, stringx -> new Profiler.a()).add(var3);
         }
      }
   }

   public static void recordCount(String string, long l) {
      if (ENABLED && l >= 0L) {
         COUNTERS.computeIfAbsent(string, stringx -> new Profiler.a()).add(l);
      }
   }

   public static void recordListener(Event arg, ListenerHandle<?> arg2, long l) {
      if (ENABLED) {
         String var4 = arg.getClass().getSimpleName();
         Object var5 = arg2.owner();
         String var6 = var5 == null ? "unknown" : var5.getClass().getSimpleName();
         if (var6.isEmpty()) {
            var6 = var5.getClass().getName();
         }

         record("event." + var4 + "." + var6, l);
      }
   }

   public static void framePresented() {
      if (ENABLED) {
         long var0 = System.nanoTime();
         long var2 = previousFrameNs;
         previousFrameNs = var0;
         if (var2 != 0L) {
            recordNanos("frame.interval", var0 - var2);
         }
      }
   }

   public static void flushIfDue() {
      if (ENABLED) {
         long var0 = System.nanoTime();
         long var2 = NEXT_REPORT_NS.get();
         if (var0 >= var2 && NEXT_REPORT_NS.compareAndSet(var2, var0 + REPORT_INTERVAL_NS)) {
            TIMINGS.forEach((string, arg) -> a(string, arg.drain()));
            ALLOCATIONS.forEach((string, arg) -> b(string, arg.drain()));
            COUNTERS.forEach((string, arg) -> c(string, arg.drain()));
         }
      }
   }

   private static void a(String string, Profiler.b arg) {
      if (arg.count() != 0L) {
         double var2 = arg.median() / 1000000.0;
         double var4 = arg.p95() / 1000000.0;
         double var6 = arg.p99() / 1000000.0;
         double var8 = arg.max() / 1000000.0;
         if ("frame.interval".equals(string)) {
            double var10 = var2 <= 0.0 ? 0.0 : 1000.0 / var2;
            double var12 = var6 <= 0.0 ? 0.0 : 1000.0 / var6;
            AgarthaClientMod.LOGGER
               .info(
                  "[Perf] {} samples={} median={}ms p95={}ms p99={}ms max={}ms median-fps={} 1%-low-fps={}",
                  new Object[]{string, arg.count(), a(var2), a(var4), a(var6), a(var8), a(var10), a(var12)}
               );
         } else {
            AgarthaClientMod.LOGGER.info("[Perf] {} calls={} median={}ms p95={}ms max={}ms", new Object[]{string, arg.count(), a(var2), a(var4), a(var8)});
         }
      }
   }

   private static void b(String string, Profiler.b arg) {
      if (arg.count() != 0L) {
         AgarthaClientMod.LOGGER
            .info("[Perf] {} allocations calls={} median={}B p95={}B max={}B", new Object[]{string, arg.count(), arg.median(), arg.p95(), arg.max()});
      }
   }

   private static void c(String string, Profiler.b arg) {
      if (arg.count() != 0L) {
         AgarthaClientMod.LOGGER.info("[Perf] {} samples={} median={} p95={} max={}", new Object[]{string, arg.count(), arg.median(), arg.p95(), arg.max()});
      }
   }

   private static String a(double d) {
      return String.format(Locale.ROOT, "%.3f", d);
   }

   private static ThreadMXBean a() {
      if (!ENABLED) {
         return null;
      } else if (ManagementFactory.getThreadMXBean() instanceof ThreadMXBean var1 && var1.isThreadAllocatedMemorySupported()) {
         if (!var1.isThreadAllocatedMemoryEnabled()) {
            var1.setThreadAllocatedMemoryEnabled(true);
         }

         return var1;
      } else {
         return null;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class a {
      private final long[] values = new long[8192];
      private int size;
      private long totalCount;
      private long max;

      synchronized void add(long l) {
         this.totalCount++;
         if (l > this.max) {
            this.max = l;
         }

         if (this.size < this.values.length) {
            this.values[this.size++] = l;
         } else {
            this.values[(int)(this.totalCount % this.values.length)] = l;
         }
      }

      synchronized Profiler.b drain() {
         if (this.totalCount == 0L) {
            return Profiler.b.EMPTY;
         } else {
            long[] var1 = Arrays.copyOf(this.values, this.size);
            Arrays.sort(var1);
            Profiler.b var2 = new Profiler.b(this.totalCount, a(var1, 0.5), a(var1, 0.95), a(var1, 0.99), this.max);
            this.size = 0;
            this.totalCount = 0L;
            this.max = 0L;
            return var2;
         }
      }

      private static long a(long[] ls, double d) {
         if (ls.length == 0) {
            return 0L;
         } else {
            int var3 = (int)Math.ceil(d * ls.length) - 1;
            return ls[Math.max(0, Math.min(ls.length - 1, var3))];
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private record b(long count, long median, long p95, long p99, long max) {
      private static final Profiler.b EMPTY = new Profiler.b(0L, 0L, 0L, 0L, 0L);
   }
}
