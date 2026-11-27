package net.agartha.client.gui;

import imgui.ImGui;
import imgui.type.ImString;
import java.util.List;
import net.agartha.client.module.ModuleManager;
import net.agartha.client.module.impl.misc.PlayerAlertModule;
import net.agartha.client.render.imgui.ImGuiManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class WhitelistModal {
   private static final float MODAL_W = 420.0F;
   private static final float MODAL_H = 360.0F;
   private static final float ROW_H = 32.0F;
   private static final float CHIP_H = 22.0F;
   private static boolean modalOpen;
   private static String status = "";
   private static boolean statusIsError;
   private static final ImString nameBuf = new ImString(32);

   private WhitelistModal() {
   }

   public static boolean isModalOpen() {
      return modalOpen;
   }

   public static void open() {
      modalOpen = true;
      status = "";
      statusIsError = false;
      nameBuf.set("");
   }

   public static void closeAll() {
      modalOpen = false;
   }

   public static boolean handleEscapeKey() {
      if (!modalOpen) {
         return false;
      } else {
         closeAll();
         return true;
      }
   }

   public static void renderModal() {
      if (modalOpen) {
         PlayerAlertModule var0 = ModuleManager.get(PlayerAlertModule.class);
         if (var0 == null) {
            closeAll();
         } else {
            float var1 = ImGui.getIO().getDisplaySizeX();
            float var2 = ImGui.getIO().getDisplaySizeY();
            ImGui.setNextWindowSize(420.0F, 360.0F, 1);
            ImGui.setNextWindowPos((var1 - 420.0F) * 0.5F, (var2 - 360.0F) * 0.5F, 1);
            ImGui.setNextWindowFocus();
            int var3 = 196902;
            if (!ImGui.begin("Whitelist##agartha_player_whitelist", var3)) {
               ImGui.end();
            } else {
               List var4 = var0.whitelistNames();
               ImGui.textDisabled("Whitelist: " + var4.size() + "   ESC closes.");
               ImGui.separator();
               ImGui.setNextItemWidth(280.0F);
               boolean var5 = ImGui.inputTextWithHint("##whitelistname", "Name", nameBuf, 64);
               ImGuiManager.getInstance().armTextInput();
               ImGui.sameLine();
               if (Widgets.chipButton("##addwhitelist", "Add", 80.0F, 22.0F, false) || var5) {
                  a(var0);
               }

               ImGui.separator();
               ImGui.beginChild("##whitelist", 0.0F, 210.0F, false);
               if (var4.isEmpty()) {
                  ImGui.textDisabled("No one whitelisted yet.");
               } else {
                  for (String var7 : (Iterable<String>) var4) {
                     ImGui.pushID("w_" + var7);
                     float var8 = ImGui.getCursorPosY();
                     ImGui.setCursorPosY(var8 + 6.0F);
                     ImGui.textUnformatted(var7);
                     ImGui.setCursorPos(290.0F, var8 + 4.0F);
                     if (Widgets.chipButton("##remove", "Remove", 80.0F, 22.0F, false)) {
                        var0.removeWhitelist(var7);
                        a(null, "Removed " + var7);
                     }

                     ImGui.setCursorPosY(var8 + 32.0F);
                     ImGui.popID();
                  }
               }

               ImGui.endChild();
               ImGui.separator();
               if (Widgets.chipButton("##closewhitelist", "Close", 80.0F, 22.0F, false)) {
                  closeAll();
               }

               if (!status.isEmpty()) {
                  ImGui.sameLine();
                  if (statusIsError) {
                     ImGui.textColored(1.0F, 0.42F, 0.38F, 1.0F, status);
                  } else {
                     float[] var9 = Palette.accentF();
                     ImGui.textColored(var9[0], var9[1], var9[2], 1.0F, status);
                  }
               }

               ImGui.end();
            }
         }
      }
   }

   private static void a(PlayerAlertModule playerAlertModule) {
      String var1 = nameBuf.get() == null ? "" : nameBuf.get().trim();
      if (var1.isEmpty()) {
         a("Type a name first", null);
      } else if (playerAlertModule.isWhitelisted(var1)) {
         a(var1 + " is already whitelisted", null);
      } else if (playerAlertModule.addWhitelist(var1)) {
         a(null, "Added " + var1);
         nameBuf.set("");
      } else {
         a("Not a valid Minecraft name", null);
      }
   }

   private static void a(String string, String string2) {
      if (string != null) {
         status = string;
         statusIsError = true;
      } else {
         status = string2 == null ? "" : string2;
         statusIsError = false;
      }
   }
}
