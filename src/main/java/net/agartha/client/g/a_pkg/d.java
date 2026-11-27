package net.agartha.client.g.a_pkg;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface d extends Library {
   d INSTANCE = a.load(d.class, "Xtst", "libXtst.so.6", "libXtst.so");

   int XTestFakeButtonEvent(Pointer pointer, int i, int j, NativeLong nativeLong);

   int XTestFakeKeyEvent(Pointer pointer, int i, int j, NativeLong nativeLong);

   int XTestFakeRelativeMotionEvent(Pointer pointer, int i, int j, NativeLong nativeLong);
}
