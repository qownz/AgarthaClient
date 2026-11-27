package net.agartha.client.d;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Environment(EnvType.CLIENT)
public @interface f {
   i priority() default i.NORMAL;

   r stage() default r.PRE;
}
