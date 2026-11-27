package net.agartha.client.module;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface d {
   String id();

   List<b> createModules();
}
