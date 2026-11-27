package net.agartha.client.platform.input;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import net.agartha.client.platform.input.JnaLibraryLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface X11Library extends Library {
   X11Library INSTANCE = JnaLibraryLoader.load(X11Library.class, "X11", "libX11.so.6", "libX11.so");

   Pointer XOpenDisplay(String string);

   int XCloseDisplay(Pointer pointer);

   int XFlush(Pointer pointer);

   NativeLong XStringToKeysym(String string);

   byte XKeysymToKeycode(Pointer pointer, NativeLong nativeLong);
}
