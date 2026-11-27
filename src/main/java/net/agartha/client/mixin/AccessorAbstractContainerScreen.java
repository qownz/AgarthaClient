package net.agartha.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin({HandledScreen.class})
public interface AccessorAbstractContainerScreen<T extends ScreenHandler> {
   @Accessor("focusedSlot")
   Slot agartha$getHoveredSlot();
}
