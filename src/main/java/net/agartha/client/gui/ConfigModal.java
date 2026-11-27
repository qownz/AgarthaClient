package net.agartha.client.gui;

import imgui.ImGui;
import imgui.type.ImString;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.agartha.client.config.ConfigProfileManager;
import net.agartha.client.gui.DrawUtils;
import net.agartha.client.gui.Palette;
import net.agartha.client.gui.Widgets;
import net.agartha.client.render.imgui.ImGuiManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class ConfigModal {
   private static final float MODAL_W = 560.0F;
   private static final float MODAL_H = 520.0F;
   private static final float ROW_H = 40.0F;
   private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("dd MMM HH:mm").withZone(ZoneId.systemDefault());
   private static boolean modalOpen;
   private static String status = "";
   private static boolean statusIsError;
   private static String pendingDelete;
   private static boolean pendingDeleteAll;
   private static final ImString nameBuf = new ImString(120);
   private static final ImString codeBuf = new ImString(200000);
   private static final AtomicReference<Runnable> pendingMain = new AtomicReference<>();
   private static volatile boolean shareBusy;

   private ConfigModal() {
   }

   public static boolean isModalOpen() {
      return modalOpen;
   }

   public static void open() {
      modalOpen = true;
      status = "";
      statusIsError = false;
      pendingDelete = null;
      pendingDeleteAll = false;
      nameBuf.set("");
      codeBuf.set("");
   }

   public static void closeAll() {
      modalOpen = false;
      pendingDelete = null;
      pendingDeleteAll = false;
   }

   public static boolean handleEscapeKey() {
      if (!modalOpen) {
         return false;
      } else if (pendingDelete == null && !pendingDeleteAll) {
         closeAll();
         return true;
      } else {
         pendingDelete = null;
         pendingDeleteAll = false;
         return true;
      }
   }

   public static void renderModal() {
      if (modalOpen) {
         Runnable var0 = pendingMain.getAndSet(null);
         if (var0 != null) {
            var0.run();
         }

         ConfigProfileManager var1 = ConfigProfileManager.getInstance();
         float var2 = ImGui.getIO().getDisplaySizeX();
         float var3 = ImGui.getIO().getDisplaySizeY();
         ImGui.setNextWindowSize(560.0F, 520.0F, 1);
         ImGui.setNextWindowPos((var2 - 560.0F) * 0.5F, (var3 - 520.0F) * 0.5F, 1);
         ImGui.setNextWindowFocus();
         short var4 = 294;
         if (!ImGui.begin("Configs##agartha_configs", var4)) {
            ImGui.end();
         } else {
            List var5 = var1.all();
            ImGui.textDisabled("Saved setups: " + var5.size() + " / 30  -  modules, keybinds and HUD layout. ESC closes.");
            ImGui.separator();
            ImGui.beginChild("##configlist", 0.0F, 260.0F, false);
            if (var5.isEmpty()) {
               ImGui.textDisabled("No saved configs yet. Type a name below and press Save.");
            } else {
               for (ConfigProfileManager.a var7 : (Iterable<ConfigProfileManager.a>) var5) {
                  ImGui.pushID(var7.name());
                  float var8 = ImGui.getCursorPosY();
                  float var9 = 310.0F;
                  ImGui.textUnformatted(DrawUtils.ellipsize(ImGui.getFont(), ImGui.getFontSize(), var7.name(), var9 - 16.0F));
                  ImGui.textDisabled(a(var7.savedAt()));
                  if (a(var9, var8, "##load", "Load", 60.0F, false)) {
                     a(var1.load(var7.name()), "Loaded " + var7.name());
                  }

                  if (var7.name().equals(pendingDelete)) {
                     if (a(var9 + 66.0F, var8, "##confirm", "Yes", 30.0F, true)) {
                        var1.delete(var7.name());
                        pendingDelete = null;
                        a(null, "Deleted " + var7.name());
                     }

                     if (a(var9 + 100.0F, var8, "##cancel", "No", 26.0F, false)) {
                        pendingDelete = null;
                     }
                  } else if (a(var9 + 66.0F, var8, "##delete", "Delete", 60.0F, false)) {
                     pendingDelete = var7.name();
                  }

                  if (a(var9 + 132.0F, var8, "##copy", "Copy code", 84.0F, false)) {
                     a(var1.exportCode(var7.name()));
                  }

                  ImGui.setCursorPosY(var8 + 40.0F);
                  ImGui.separator();
                  ImGui.popID();
               }
            }

            ImGui.endChild();
            ImGui.separator();
            ImGui.textDisabled("Save the current setup");
            ImGui.setNextItemWidth(220.0F);
            ImGui.inputTextWithHint("##configname", "Config name (optional)", nameBuf);
            ImGuiManager.getInstance().armTextInput();
            ImGui.sameLine();
            if (ImGui.button("Save", 90.0F, 0.0F)) {
               String var10 = nameBuf.get() == null ? "" : nameBuf.get().trim();
               String var13 = var10.isEmpty() ? var1.uniqueName("Config") : var10;
               String var15 = var1.save(var13);
               a(var15, "Saved " + var13);
               if (var15 == null) {
                  nameBuf.set("");
               }
            }

            ImGui.sameLine();
            if (ImGui.button("Copy current code", 150.0F, 0.0F)) {
               a(ConfigProfileManager.exportCurrentCode());
            }

            ImGui.textDisabled("Import a code");
            ImGui.setNextItemWidth(220.0F);
            ImGui.inputTextWithHint("##configcode", "Short code, or leave empty to use clipboard", codeBuf);
            ImGuiManager.getInstance().armTextInput();
            ImGui.sameLine();
            if (ImGui.button("Import & save", 110.0F, 0.0F)) {
               String var11 = nameBuf.get() == null ? "" : nameBuf.get().trim();
               String var14 = var11.isEmpty() ? var1.uniqueName("Imported") : var11;
               a(a(), var14, true);
            }

            ImGui.sameLine();
            if (ImGui.button("Apply only", 90.0F, 0.0F)) {
               a(a(), null, false);
            }

            ImGui.separator();
            if (pendingDeleteAll) {
               if (ImGui.button("Delete every config?", 180.0F, 0.0F)) {
                  var1.deleteAll();
                  pendingDeleteAll = false;
                  a(null, "All configs deleted");
               }

               ImGui.sameLine();
               if (ImGui.button("Cancel", 90.0F, 0.0F)) {
                  pendingDeleteAll = false;
               }
            } else {
               if (ImGui.button("Delete all", 110.0F, 0.0F)) {
                  pendingDeleteAll = true;
               }

               ImGui.sameLine();
               if (ImGui.button("Close", 90.0F, 0.0F)) {
                  closeAll();
               }
            }

            if (!status.isEmpty()) {
               ImGui.sameLine();
               if (statusIsError) {
                  ImGui.textColored(1.0F, 0.42F, 0.38F, 1.0F, status);
               } else {
                  float[] var12 = Palette.accentF();
                  ImGui.textColored(var12[0], var12[1], var12[2], 1.0F, status);
               }
            }

            ImGui.end();
         }
      }
   }

   private static boolean a(float f, float g, String string, String string2, float h, boolean bl) {
      ImGui.setCursorPos(f, g + 8.0F);
      return Widgets.chipButton(string, string2, h, 20.0F, bl);
   }

   private static void a(String string) {
      if (string == null) {
         a("Could not build the code", null);
      } else {
         b(string);
         a(null, "Config code copied");
      }
   }

   private static void a(String string, String string2, boolean bl) {
      String var3 = string == null ? "" : string.trim();
      if (var3.matches("[1-9][0-9]{5}")) {
         a("Code not found", null);
         return;
      }
      ConfigProfileManager var6 = ConfigProfileManager.getInstance();
      String var5 = var6.importCode(var3, bl ? string2 : null);
      a(var5, bl ? "Imported as " + string2 : "Config applied");
      if (var5 == null) {
         codeBuf.set("");
      }
   }

   private static String a() {
      String var0 = codeBuf.get();
      if (var0 != null && !var0.isBlank()) {
         return var0;
      } else {
         MinecraftClient var1 = MinecraftClient.getInstance();
         return var1.keyboard == null ? "" : var1.keyboard.getClipboard();
      }
   }

   private static void b(String string) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.keyboard != null) {
         var1.keyboard.setClipboard(string);
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

   private static String a(long l) {
      return l <= 0L ? "unknown date" : STAMP.format(Instant.ofEpochMilli(l));
   }
}
