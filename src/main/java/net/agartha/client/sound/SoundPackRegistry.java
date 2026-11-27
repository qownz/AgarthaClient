package net.agartha.client.sound;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SoundPackRegistry {
   public static final List<String> PACK_IDS = List.of(
      "cherrymx_blue_abs", "cherrymx_blue_abs_2", "cherrymx_black_abs", "cherrymx_black_pbt", "cherrymx_brown_abs", "cherrymx_brown_pbt", "cherrymx_red_abs"
   );
   public static final String DEFAULT_KEYBOARD = "cherrymx_black_pbt";
   public static final String DEFAULT_MOUSE = "cherrymx_blue_abs_2";

   private SoundPackRegistry() {
   }

   public static String[] packArray() {
      return PACK_IDS.toArray(String[]::new);
   }

   public static String displayName(String string) {
      return switch (string) {
         case "cherrymx_blue_abs" -> "Cherry MX Blue ABS";
         case "cherrymx_blue_abs_2" -> "Cherry MX Blue ABS 2";
         case "cherrymx_black_abs" -> "Cherry MX Black ABS";
         case "cherrymx_black_pbt" -> "Cherry MX Black PBT";
         case "cherrymx_brown_abs" -> "Cherry MX Brown ABS";
         case "cherrymx_brown_pbt" -> "Cherry MX Brown PBT";
         case "cherrymx_red_abs" -> "Cherry MX Red ABS";
         default -> string;
      };
   }
}
