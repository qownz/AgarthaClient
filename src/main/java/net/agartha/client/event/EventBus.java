package net.agartha.client.event;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.agartha.client.event.Event;
import net.agartha.client.event.EventType;
import net.agartha.client.event.listener.ListenerCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jctools.queues.MpscArrayQueue;

@Environment(EnvType.CLIENT)
public final class EventBus {
   public static final EventBus INSTANCE = new EventBus();
   private final Map<Object, List<EventBus.b<?>>> subscriptions = new IdentityHashMap<>();
   private final Map<Object, List<EventBus.b<?>>> handleCache = new IdentityHashMap<>();
   private final MpscArrayQueue<Runnable> asyncQueue = new MpscArrayQueue(16384);
   private static final ClassValue<List<EventBus.a>> HANDLER_SPECS = new ClassValue<List<EventBus.a>>() {
      protected List<EventBus.a> computeValue(Class<?> class_) {
         return EventBus.a(class_);
      }
   };
   private static final ClassValue<EventType<?>> TYPE_CACHE = new ClassValue<EventType<?>>() {
      protected EventType<?> computeValue(Class<?> class_) {
         try {
            Field var2 = class_.getField("TYPE");
            if (!EventType.class.isAssignableFrom(var2.getType())) {
               throw new IllegalArgumentException(class_.getName() + ".TYPE must be an EventType instance.");
            } else {
               return (EventType<?>)var2.get(null);
            }
         } catch (NoSuchFieldException var3) {
            throw new IllegalArgumentException("Event class " + class_.getName() + " must declare 'public static final EventType<...> TYPE'.", var3);
         } catch (IllegalAccessException var4) {
            throw new IllegalArgumentException("Cannot access " + class_.getName() + ".TYPE", var4);
         }
      }
   };

   public synchronized void prepare(Object object) {
      this.handleCache.computeIfAbsent(object, this::a);
   }

   public boolean postAsync(Runnable runnable) {
      boolean var2 = this.asyncQueue.offer(runnable);
      if (!var2) {
      }

      return var2;
   }

   public void drainAsync() {
      Runnable var1;
      while ((var1 = (Runnable)this.asyncQueue.poll()) != null) {
         var1.run();
      }
   }

   public <E extends Event> void off(ListenerHandle<E> arg) {
      arg.unregister();
   }

   public synchronized int register(Object object) {
      if (this.subscriptions.containsKey(object)) {
         throw new IllegalStateException("Subscriber already registered: " + object.getClass().getName());
      } else {
         List var2 = this.handleCache.get(object);
         if (var2 == null) {
            var2 = this.a(object);
            this.handleCache.put(object, var2);
         }

         if (var2.isEmpty()) {
            return 0;
         } else {
            for (EventBus.b var4 : (Iterable<EventBus.b>) var2) {
               var4.register();
            }

            this.subscriptions.put(object, var2);
            return var2.size();
         }
      }
   }

   public synchronized void unregister(Object object) {
      List var2 = this.subscriptions.remove(object);
      if (var2 != null) {
         for (EventBus.b var4 : (Iterable<EventBus.b>) var2) {
            var4.unregister();
         }
      }
   }

   private List<EventBus.b<?>> a(Object object) {
      List var2 = HANDLER_SPECS.get(object.getClass());
      if (var2.isEmpty()) {
         return List.of();
      } else {
         ArrayList var3 = new ArrayList(var2.size());

         for (EventBus.a var5 : (Iterable<EventBus.a>) var2) {
            ListenerCallback var6 = var5.staticListener();
            if (var6 == null) {
               try {
                  var6 = b((Object)var5.listenerFactory().invoke((Object)object));
               } catch (Throwable var8) {
                  throw new RuntimeException("Failed to bind listener for " + object.getClass().getName(), var8);
               }
            }

            ListenerHandle var7 = new ListenerHandle<>(var5.type(), var5.stage(), var5.priority(), object, var6::invoke);
            var3.add(new EventBus.b<>(var5.type(), var7));
         }

         return var3;
      }
   }

   private static ListenerCallback<Event> b(Object object) {
      return (ListenerCallback<Event>)object;
   }

   private static List<EventBus.a> a(Class<?> class_) {
      ArrayList var1 = new ArrayList();

      for (Method var5 : class_.getMethods()) {
         EventStage var6 = EventStage.PRE;
         EventPriority var7 = EventPriority.NORMAL;
         boolean var8 = false;
         EventListener var9 = var5.getAnnotation(EventListener.class);
         if (var9 != null) {
            var6 = var9.stage();
            var7 = var9.priority();
            var8 = true;
         } else {
            LegacyEventListener var10 = var5.getAnnotation(LegacyEventListener.class);
            if (var10 != null) {
               var6 = EventStage.PRE;
               var7 = EventPriority.fromInt(var10.priority());
               var8 = true;
            }
         }

         if (var8) {
            Class[] var13 = var5.getParameterTypes();
            if (var13.length == 1 && Event.class.isAssignableFrom(var13[0])) {
               Class var11 = var13[0];
               EventType var12 = resolveTypeFor(var11);
               if (Modifier.isStatic(var5.getModifiers())) {
                  var1.add(new EventBus.a(var12, var6, var7, null, ListenerFactory.createStatic(var5)));
               } else {
                  var1.add(new EventBus.a(var12, var6, var7, ListenerFactory.createFactory(var5), null));
               }
            }
         }
      }

      return var1.isEmpty() ? List.of() : List.copyOf(var1);
   }

   static <E extends Event> EventType<E> resolveTypeFor(Class<E> class_) {
      return (EventType<E>)TYPE_CACHE.get(class_);
   }

   @Environment(EnvType.CLIENT)
   private record a(EventType<Event> type, EventStage stage, EventPriority priority, MethodHandle listenerFactory, ListenerCallback<Event> staticListener) {
   }

   @Environment(EnvType.CLIENT)
   private record b<E extends Event>(EventType<E> type, ListenerHandle<E> handle) {
      void register() {
         this.type.register(this.handle);
      }

      void unregister() {
         this.type.unregister(this.handle);
      }
   }
}
