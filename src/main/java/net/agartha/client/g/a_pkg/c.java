package net.agartha.client.g.a_pkg;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface c extends Library {
   c INSTANCE = a.load(c.class, "X11", "libX11.so.6", "libX11.so");

   Pointer XOpenDisplay(String string);

   int XCloseDisplay(Pointer pointer);

   int XFlush(Pointer pointer);

   NativeLong XStringToKeysym(String string);

   byte XKeysymToKeycode(Pointer pointer, NativeLong nativeLong);
}
