package net.agartha.client.e;

import java.lang.reflect.Method;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;

@Environment(EnvType.CLIENT)
public class an {
   public static void main(String[] strings) {
      for (Method var4 : Screen.class.getMethods()) {
         if (var4.getName().equals("keyPressed")) {
            System.out.println("FOUND: " + var4.toString());
         }
      }
   }
}
