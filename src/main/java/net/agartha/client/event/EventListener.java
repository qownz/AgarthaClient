package net.agartha.client.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Environment(EnvType.CLIENT)
public @interface EventListener {
   EventPriority priority() default EventPriority.NORMAL;

   EventStage stage() default EventStage.PRE;
}
