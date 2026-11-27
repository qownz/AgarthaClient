package net.agartha.client.e;

import imgui.ImGui;
import imgui.type.ImString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class g {
   private static final float LIST_MODAL_W = 520.0F;
   private static final float LIST_MODAL_H = 420.0F;
   private static final float EDIT_MODAL_W = 520.0F;
   private static final float EDIT_MODAL_H = 460.0F;
   private static boolean modalOpen;
   private static g.a screen = g.a.LIST;
   private static net.agartha.client.j.a editingMacro;
   private static boolean creatingNew;
   private static String statusMessage = "";
   private static final ImString nameBuf = new ImString(64);
   private static final List<ImString> messageBufs = new ArrayList<>();
   private static net.agartha.client.j.e draftKeybind = new net.agartha.client.j.e();
   private static boolean listeningKeybind;
   private static boolean listeningFirstFrame;
   private static final boolean[] heldWhenListening = new boolean[1050];
   private static net.agartha.client.j.a pendingDelete;

   private g() {
   }

   public static boolean isModalOpen() {
      return modalOpen;
   }

   public static void openListModal() {
      modalOpen = true;
      screen = g.a.LIST;
      pendingDelete = null;
      listeningKeybind = false;
      statusMessage = "";
   }

   public static void closeAll() {
      modalOpen = false;
      screen = g.a.LIST;
      editingMacro = null;
      creatingNew = false;
      pendingDelete = null;
      listeningKeybind = false;
      statusMessage = "";
      messageBufs.clear();
   }

   public static boolean handleEscapeKey() {
      if (!modalOpen) {
         return false;
      } else if (listeningKeybind) {
         listeningKeybind = false;
         return true;
      } else if (screen == g.a.EDIT) {
         screen = g.a.LIST;
         editingMacro = null;
         creatingNew = false;
         statusMessage = "";
         return true;
      } else {
         closeAll();
         return true;
      }
   }

   public static void renderModal(float f) {
      if (modalOpen) {
         if (screen == g.a.LIST) {
            a();
         } else {
            b();
         }
      }
   }

   private static void a() {
      a(520.0F, 420.0F);
      ImGui.setNextWindowFocus();
      if (!ImGui.begin("Chat Macros##macrosmodal", e())) {
         ImGui.end();
      } else {
         ImGui.textDisabled("Keybind macros and .macro command scheduling.");
         ImGui.separator();
         float var0 = 300.0F;
         ImGui.beginChild("##macrolist", 0.0F, var0, false);
         ArrayList var1 = new ArrayList<>(net.agartha.client.j.d.getInstance().all());
         if (var1.isEmpty()) {
            ImGui.textDisabled("No macros yet. Click Create to add one.");
         } else {
            for (net.agartha.client.j.a var3 : (Iterable<net.agartha.client.j.a>) var1) {
               ImGui.pushID(var3.name());
               String var4 = var3.name() + " (" + var3.keybind() + ")";
               ImGui.text(var4);
               ImGui.sameLine(Math.max(ImGui.getCursorPosX(), 300.0F));
               if (ImGui.smallButton("Edit")) {
                  a(var3, false);
               }

               ImGui.sameLine();
               if (pendingDelete == var3) {
                  ImGui.pushStyleColor(21, 0.85F, 0.15F, 0.15F, 1.0F);
                  if (ImGui.smallButton("Confirm")) {
                     net.agartha.client.j.d.getInstance().remove(var3);
                     pendingDelete = null;
                  }

                  ImGui.popStyleColor();
                  ImGui.sameLine();
                  if (ImGui.smallButton("Cancel")) {
                     pendingDelete = null;
                  }
               } else if (ImGui.smallButton("Delete")) {
                  pendingDelete = var3;
               }

               ImGui.separator();
               ImGui.popID();
            }
         }

         ImGui.endChild();
         ImGui.separator();
         if (ImGui.button("Create", 100.0F, 0.0F)) {
            a(null, true);
         }

         ImGui.sameLine();
         if (ImGui.button("Close", 100.0F, 0.0F)) {
            closeAll();
         }

         ImGui.end();
      }
   }

   private static void a(net.agartha.client.j.a arg, boolean bl) {
      screen = g.a.EDIT;
      editingMacro = arg;
      creatingNew = bl;
      pendingDelete = null;
      listeningKeybind = false;
      statusMessage = "";
      nameBuf.set(arg == null ? "" : arg.name());
      messageBufs.clear();
      if (arg != null && !arg.messages().isEmpty()) {
         for (String var3 : arg.messages()) {
            ImString var4 = new ImString(256);
            var4.set(var3);
            messageBufs.add(var4);
         }
      } else {
         messageBufs.add(new ImString(256));
      }

      draftKeybind = arg == null ? new net.agartha.client.j.e() : arg.keybind().copy();
   }

   private static void b() {
      a(520.0F, 460.0F);
      ImGui.setNextWindowFocus();
      String var0 = (creatingNew ? "Create Macro" : "Edit Macro") + "##macroedit";
      if (!ImGui.begin(var0, e())) {
         ImGui.end();
      } else {
         ImGui.text("Name");
         ImGui.setNextItemWidth(-1.0F);
         ImGui.inputText("##macroname", nameBuf);
         net.agartha.client.f.a.getInstance().armTextInput();
         ImGui.spacing();
         ImGui.text("Keybind");
         String var1 = listeningKeybind ? "Press a key..." : draftKeybind.toString();
         if (ImGui.button(var1 + "##macrobind", 180.0F, 0.0F)) {
            listeningKeybind = true;
            listeningFirstFrame = true;
         }

         ImGui.sameLine();
         if (ImGui.button("Clear##macrobindclear", 60.0F, 0.0F)) {
            draftKeybind.reset();
            listeningKeybind = false;
         }

         if (listeningKeybind) {
            d();
         }

         ImGui.spacing();
         ImGui.text("Messages (one per line, / prefix for commands)");
         float var2 = 220.0F;
         ImGui.beginChild("##macromessages", 0.0F, var2, false);

         for (int var3 = 0; var3 < messageBufs.size(); var3++) {
            ImGui.pushID(var3);
            ImGui.setNextItemWidth(-60.0F);
            ImGui.inputText("##msg", messageBufs.get(var3));
            net.agartha.client.f.a.getInstance().armTextInput();
            ImGui.sameLine();
            if (messageBufs.size() > 1 && ImGui.smallButton("X")) {
               messageBufs.remove(var3);
               ImGui.popID();
               break;
            }

            ImGui.popID();
         }

         ImGui.endChild();
         if (ImGui.button("+ Add Message", 120.0F, 0.0F)) {
            messageBufs.add(new ImString(256));
         }

         if (!statusMessage.isEmpty()) {
            ImGui.pushStyleColor(0, 1.0F, 0.35F, 0.35F, 1.0F);
            ImGui.textWrapped(statusMessage);
            ImGui.popStyleColor();
         }

         ImGui.separator();
         if (ImGui.button("Save", 100.0F, 0.0F) && c()) {
            screen = g.a.LIST;
            editingMacro = null;
            creatingNew = false;
            statusMessage = "";
         }

         ImGui.sameLine();
         if (ImGui.button("Back", 100.0F, 0.0F)) {
            screen = g.a.LIST;
            editingMacro = null;
            creatingNew = false;
            listeningKeybind = false;
            statusMessage = "";
         }

         ImGui.end();
      }
   }

   private static boolean c() {
      String var0 = nameBuf.get().trim();
      if (var0.isBlank()) {
         statusMessage = "Name is required.";
         return false;
      } else {
         ArrayList var1 = new ArrayList();

         for (ImString var3 : messageBufs) {
            String var4 = var3.get().trim();
            if (!var4.isEmpty()) {
               var1.add(var4);
            }
         }

         if (var1.isEmpty()) {
            statusMessage = "Add at least one message.";
            return false;
         } else if (creatingNew) {
            net.agartha.client.j.a var6 = net.agartha.client.j.d.getInstance().get(var0);
            if (var6 != null) {
               statusMessage = "A macro with that name already exists.";
               return false;
            } else {
               net.agartha.client.j.a var7 = new net.agartha.client.j.a(var0, var1, draftKeybind);
               net.agartha.client.j.d.getInstance().add(var7);
               return true;
            }
         } else if (editingMacro == null) {
            statusMessage = "Nothing to save.";
            return false;
         } else {
            net.agartha.client.j.a var5 = net.agartha.client.j.d.getInstance().get(var0);
            if (var5 != null && var5 != editingMacro) {
               statusMessage = "A macro with that name already exists.";
               return false;
            } else {
               editingMacro.setName(var0);
               editingMacro.setMessages(var1);
               editingMacro.setKeybind(draftKeybind);
               net.agartha.client.j.d.getInstance().save();
               return true;
            }
         }
      }
   }

   private static void d() {
      long var0 = MinecraftClient.getInstance().getWindow().getHandle();
      if (listeningFirstFrame) {
         listeningFirstFrame = false;
         a(var0);
      } else {
         for (int var2 = 32; var2 <= 348; var2++) {
            if (!net.agartha.client.j.e.isShiftKey(var2)) {
               boolean var3 = GLFW.glfwGetKey(var0, var2) == 1;
               if (var2 < heldWhenListening.length && !var3) {
                  heldWhenListening[var2] = false;
               }

               if (var3 && (var2 >= heldWhenListening.length || !heldWhenListening[var2])) {
                  if (var2 == 256) {
                     draftKeybind.reset();
                  } else {
                     net.agartha.client.j.e var4 = new net.agartha.client.j.e(true, var2, 0);
                     if (var4.canBindTo(true, var2, 0)) {
                        draftKeybind = var4;
                     }
                  }

                  listeningKeybind = false;
                  return;
               }
            }
         }

         for (int var6 = 0; var6 <= 7; var6++) {
            int var7 = 1000 + var6;
            boolean var8 = GLFW.glfwGetMouseButton(var0, var6) == 1;
            if (var7 < heldWhenListening.length && !var8) {
               heldWhenListening[var7] = false;
            }

            if (var8 && (var7 >= heldWhenListening.length || !heldWhenListening[var7])) {
               net.agartha.client.j.e var5 = new net.agartha.client.j.e(false, var6, 0);
               if (var5.canBindTo(false, var6, 0)) {
                  draftKeybind = var5;
               }

               listeningKeybind = false;
               return;
            }
         }
      }
   }

   private static void a(long l) {
      Arrays.fill(heldWhenListening, false);

      for (int var2 = 32; var2 <= 348 && var2 < heldWhenListening.length; var2++) {
         if (!net.agartha.client.j.e.isShiftKey(var2)) {
            heldWhenListening[var2] = GLFW.glfwGetKey(l, var2) == 1;
         }
      }

      for (int var4 = 0; var4 <= 7; var4++) {
         int var3 = 1000 + var4;
         if (var3 < heldWhenListening.length) {
            heldWhenListening[var3] = GLFW.glfwGetMouseButton(l, var4) == 1;
         }
      }
   }

   private static int b(long l) {
      byte var2 = 0;
      if (a(l, 341) || a(l, 345)) {
         var2 |= 2;
      }

      if (a(l, 343) || a(l, 347)) {
         var2 |= 8;
      }

      if (a(l, 342) || a(l, 346)) {
         var2 |= 4;
      }

      if (a(l, 340) || a(l, 344)) {
         var2 |= 1;
      }

      return var2;
   }

   private static boolean a(long l, int i) {
      return GLFW.glfwGetKey(l, i) == 1;
   }

   private static void a(float f, float g) {
      float var2 = ImGui.getIO().getDisplaySizeX();
      float var3 = ImGui.getIO().getDisplaySizeY();
      ImGui.setNextWindowSize(f, g, 1);
      ImGui.setNextWindowPos((var2 - f) * 0.5F, (var3 - g) * 0.5F, 1);
   }

   private static int e() {
      return 294;
   }

   @Environment(EnvType.CLIENT)
   private static enum a {
      LIST,
      EDIT;
   }
}
