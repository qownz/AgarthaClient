package net.agartha.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin({AbstractSignEditScreen.class})
public interface AccessorAbstractSignEditScreen {
   @Accessor("messages")
   String[] agartha$getMessages();
}
