package net.agartha.client.e;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum k {
   WHITE("White", 255, 255, 255),
   GREEN("Green", 0, 235, 54),
   RED("Red", 255, 69, 58),
   BLUE("Blue", 2, 176, 254),
   PINK("Pink", 255, 55, 155),
   PURPLE("Purple", 191, 90, 242);

   private final String displayName;
   private final int r;
   private final int g;
   private final int b;

   private k(String string2, int j, int k, int l) {
      this.displayName = string2;
      this.r = j;
      this.g = k;
      this.b = l;
   }

   public String displayName() {
      return this.displayName;
   }

   public int r() {
      return this.r;
   }

   public int g() {
      return this.g;
   }

   public int b() {
      return this.b;
   }

   public int rgb() {
      return this.r << 16 | this.g << 8 | this.b;
   }

   public int abgr() {
      return d.rgbAbgr(this.r, this.g, this.b, 255);
   }

   public int withAlpha(int i) {
      return d.rgbAbgr(this.r, this.g, this.b, i);
   }

   public float[] accentF() {
      return new float[]{this.r / 255.0F, this.g / 255.0F, this.b / 255.0F, 1.0F};
   }

   public float[] borderF() {
      return new float[]{this.r / 255.0F, this.g / 255.0F, this.b / 255.0F, 0.28F};
   }

   public float[] colorFloats() {
      return new float[]{this.r / 255.0F, this.g / 255.0F, this.b / 255.0F, 1.0F};
   }

   public static k byName(String string) {
      if (string != null && !string.isBlank()) {
         for (k var4 : values()) {
            if (var4.displayName.equalsIgnoreCase(string.trim())) {
               return var4;
            }
         }

         return "Turquoise".equalsIgnoreCase(string.trim()) ? BLUE : WHITE;
      } else {
         return WHITE;
      }
   }

   public static String[] displayNames() {
      k[] var0 = values();
      String[] var1 = new String[var0.length];

      for (int var2 = 0; var2 < var0.length; var2++) {
         var1[var2] = var0[var2].displayName;
      }

      return var1;
   }
}
