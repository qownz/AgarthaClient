package net.agartha.client.d;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class q<E extends c> extends j<E> {
   public final h<E> pool;

   public q(Supplier<E> supplier, int i) {
      this.pool = new h<>(supplier, i);
   }

   public q(String string, Supplier<E> supplier, int i) {
      super(string);
      this.pool = new h<>(supplier, i);
   }
}
