package net.agartha.client.input;

import java.util.Locale;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class Keybind {
   private boolean isKey;
   private int value;
   private int modifiers;

   public Keybind() {
      this(false, -1, 0);
   }

   public Keybind(boolean bl, int i, int j) {
      this.isKey = bl;
      this.value = i;
      this.modifiers = j;
   }

   public Keybind copy() {
      return new Keybind(this.isKey, this.value, this.modifiers);
   }

   public boolean isKey() {
      return this.isKey;
   }

   public int value() {
      return this.value;
   }

   public int modifiers() {
      return this.modifiers;
   }

   public void set(boolean bl, int i, int j) {
      this.isKey = bl;
      this.value = i;
      this.modifiers = j;
   }

   public void reset() {
      this.set(false, -1, 0);
   }

   public boolean isSet() {
      return this.value != -1;
   }

   public boolean hasMods() {
      return this.modifiers != 0;
   }

   public boolean canBindTo(boolean bl, int i, int j) {
      if (bl) {
         return isShiftKey(i) ? false : i != -1 && i != 256;
      } else {
         return i != 0 && i != 1;
      }
   }

   public boolean matches(boolean bl, int i, int j) {
      if (!this.isSet() || this.isKey != bl) {
         return false;
      } else if (bl && isShiftKey(this.value)) {
         return false;
      } else {
         return !this.hasMods() ? this.value == i : this.value == i && (this.modifiers & j) == this.modifiers;
      }
   }

   public static boolean isClickButton(int i) {
      return i == 1000 || i == 1001;
   }

   public static boolean isShiftKey(int i) {
      return i == 340 || i == 344;
   }

   public static int currentModifiers(long l) {
      byte var2 = 0;
      if (GLFW.glfwGetKey(l, 340) == 1 || GLFW.glfwGetKey(l, 344) == 1) {
         var2 |= 1;
      }

      if (GLFW.glfwGetKey(l, 341) == 1 || GLFW.glfwGetKey(l, 345) == 1) {
         var2 |= 2;
      }

      if (GLFW.glfwGetKey(l, 342) == 1 || GLFW.glfwGetKey(l, 346) == 1) {
         var2 |= 4;
      }

      if (GLFW.glfwGetKey(l, 343) == 1 || GLFW.glfwGetKey(l, 347) == 1) {
         var2 |= 8;
      }

      return var2;
   }

   @Override
   public String toString() {
      if (!this.isSet()) {
         return "None";
      } else if (!this.isKey) {
         return buttonName(this.value);
      } else if (this.modifiers == 0) {
         return keyName(this.value);
      } else {
         StringBuilder var1 = new StringBuilder();
         if ((this.modifiers & 2) != 0) {
            var1.append("Ctrl + ");
         }

         if ((this.modifiers & 8) != 0) {
            var1.append("Cmd + ");
         }

         if ((this.modifiers & 4) != 0) {
            var1.append("Alt + ");
         }

         if ((this.modifiers & 1) != 0) {
            var1.append("Shift + ");
         }

         if ((this.modifiers & 16) != 0) {
            var1.append("Caps Lock + ");
         }

         if ((this.modifiers & 32) != 0) {
            var1.append("Num Lock + ");
         }

         var1.append(keyName(this.value));
         return var1.toString();
      }
   }

   public static String keyName(int i) {
      if (i >= 39 && i <= 96 || i >= 161 && i <= 162) {
         String var1 = GLFW.glfwGetKeyName(i, -1);
         if (var1 != null && !var1.isBlank()) {
            return var1.toUpperCase(Locale.ROOT);
         }
      }
      return switch (i) {
         case 32 -> "SPACE";
         case 256 -> "ESC";
         case 257 -> "ENTER";
         case 258 -> "TAB";
         case 259 -> "BKSP";
         case 260 -> "INS";
         case 261 -> "DEL";
         case 262 -> "RIGHT";
         case 263 -> "LEFT";
         case 264 -> "DOWN";
         case 265 -> "UP";
         case 266 -> "PGUP";
         case 267 -> "PGDN";
         case 268 -> "HOME";
         case 269 -> "END";
         case 290 -> "F1";
         case 291 -> "F2";
         case 292 -> "F3";
         case 293 -> "F4";
         case 294 -> "F5";
         case 295 -> "F6";
         case 296 -> "F7";
         case 297 -> "F8";
         case 298 -> "F9";
         case 299 -> "F10";
         case 300 -> "F11";
         case 301 -> "F12";
         case 335 -> "NUM ENTER";
         case 340, 344 -> "SHIFT";
         case 341, 345 -> "CTRL";
         case 342, 346 -> "ALT";
         default -> "K" + i;
      };
   }

   public static String buttonName(int i) {
      return switch (i) {
         case 0 -> "LMB";
         case 1 -> "RMB";
         case 2 -> "MMB";
         default -> "MB" + (i + 1);
      };
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         Keybind var2 = (Keybind)object;
         return this.isKey == var2.isKey && this.value == var2.value && this.modifiers == var2.modifiers;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.isKey, this.value, this.modifiers);
   }
}
