package net.agartha.client.module.impl.misc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.event.EventListener;
import net.agartha.client.event.impl.TickEvent;
import net.agartha.client.gui.NotificationManager;
import net.agartha.client.module.Module;
import net.agartha.client.module.ModuleCategory;
import net.agartha.client.module.setting.StringSetting;
import net.agartha.client.util.net.ChatBus;
import net.agartha.client.util.net.ChatSender;
import net.agartha.client.util.net.MoneyCommandGuard;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.dialog.DialogScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;

@Environment(EnvType.CLIENT)
public final class PanicPayModule extends Module {
   private static final Pattern VALID_NAME = Pattern.compile("\\.?[A-Za-z0-9_]{3,16}");
   private static final Pattern RATE_LIMIT = Pattern.compile("wait (?:another )?([\\d.]+) seconds?");
   private static final Pattern MONEY = Pattern.compile("\\$\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kmbt])?(?![a-z])");
   private static final String SMALL_CAPS = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";
   private static final long COMMAND_GAP_MS = 1000L;
   private static final long REPLY_TIMEOUT_MS = 6000L;
   private static final int MAX_TRIES = 3;
   private static final int MAX_LOWERS = 4;
   private static final long RUN_LIMIT_MS = 45000L;
   private final StringSetting sPayTo = (StringSetting)this.addSetting(new StringSetting("Player", "", 17));
   private final Consumer<String> chatListener = this::onChat;
   private PanicPayModule.Step step;
   private String target;
   private String amount;
   private long sentAt;
   private long lastCommandAt;
   private int tries;
   private int lowers;
   private long startedAt;
   private Screen pressedDialog;
   private volatile long holdUntil;
   private volatile long paidAt;
   private volatile long poorAt;
   private volatile long errorAt;
   private volatile String errorText = "";
   private volatile long balanceAt;
   private volatile String balanceNumber;
   private volatile String balanceSuffix;

   public PanicPayModule() {
      super("Panic Pay", "Sends all your money to the player you type in.", ModuleCategory.DONUT);
      this.sPayTo.sessionOnly();
   }

   public boolean persistEnabled() {
      return false;
   }

   protected boolean canEnable() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      String var2 = this.sPayTo.get().trim();
      if (var1.player == null || var1.getNetworkHandler() == null) {
         alert("you are not on a server.");
         return false;
      } else if (!VALID_NAME.matcher(var2).matches()) {
         alert("type a player name first.");
         return false;
      } else if (var2.equalsIgnoreCase(var1.getSession().getUsername())) {
         alert("that is your own name.");
         return false;
      } else if (onlineName(var2) == null) {
         alert(var2 + " is offline.");
         return false;
      } else {
         return true;
      }
   }

   protected void onEnable() {
      this.target = onlineName(this.sPayTo.get().trim());
      if (this.target == null) {
         this.step = null;
      } else {
         this.amount = null;
         this.sentAt = 0L;
         this.tries = 0;
         this.lowers = 0;
         this.pressedDialog = null;
         this.startedAt = System.currentTimeMillis();
         this.holdUntil = this.paidAt = this.poorAt = this.errorAt = this.balanceAt = 0L;
         ChatBus.addListener(this.chatListener);
         ChatSender.send("Panic Pay", "Sending your whole balance to " + this.target + ". Turn Panic Pay off to stop.");
         this.step = PanicPayModule.Step.BALANCE;
      }
   }

   protected void onDisable() {
      ChatBus.removeListener(this.chatListener);
      this.step = null;
   }

   private void finish(String string) {
      alert(string);
      this.step = null;
      this.setEnabled(false);
   }

   private static void alert(String string) {
      NotificationManager.getInstance().pushAlert("Panic Pay: " + string);
      AgarthaClientMod.LOGGER.warn("[Panic Pay] {}", string);
   }

   private void onChat(String string) {
      if (this.step != null) {
         String var2 = normalize(string);
         long var3 = System.currentTimeMillis();
         Matcher var5 = RATE_LIMIT.matcher(var2);
         if (var5.find()) {
            try {
               this.holdUntil = var3 + (long)Math.ceil(Double.parseDouble(var5.group(1)) * 1000.0) + 100L;
            } catch (NumberFormatException var7) {
            }
         } else {
            if (var2.startsWith("you paid ") || var2.startsWith("you sent ")) {
               this.paidAt = var3;
            } else if (!var2.contains("enough money") && !var2.contains("insufficient")) {
               if ((var2.startsWith("you have") || var2.startsWith("balance") || var2.contains("your balance")) && var2.contains("$")) {
                  Matcher var6 = MONEY.matcher(var2);
                  if (var6.find()) {
                     this.balanceNumber = var6.group(1);
                     this.balanceSuffix = var6.group(2) == null ? "" : var6.group(2);
                     this.balanceAt = var3;
                  }
               } else if (var2.contains("not online")
                  || var2.contains("not found")
                  || var2.contains("cannot pay")
                  || var2.contains("can't pay")
                  || var2.contains("not a valid")
                  || var2.contains("invalid")
                  || var2.contains("doesn't exist")
                  || var2.contains("does not exist")) {
                  this.errorText = string;
                  this.errorAt = var3;
               }
            } else {
               this.poorAt = var3;
            }
         }
      }
   }

   @EventListener
   public void onTick(TickEvent arg) {
      if (this.step != null) {
         MinecraftClient var2 = MinecraftClient.getInstance();
         ClientPlayerEntity var3 = var2.player;
         if (var3 != null && var2.getNetworkHandler() != null) {
            long var4 = System.currentTimeMillis();
            if (var4 - this.startedAt > 45000L) {
               this.finish(this.step == PanicPayModule.Step.BALANCE ? "took too long, nothing was sent." : "took too long waiting for /pay, check /bal.");
            } else {
               if (this.step == PanicPayModule.Step.BALANCE) {
                  this.balance(var3, var4);
               } else {
                  this.pay(var2, var3, var4);
               }
            }
         } else {
            this.finish("disconnected, nothing was sent.");
         }
      }
   }

   private void balance(ClientPlayerEntity clientPlayerEntity, long l) {
      if (this.sentAt != 0L && this.balanceAt >= this.sentAt) {
         this.amount = payAmount(this.balanceNumber, this.balanceSuffix);
         if (this.amount == null) {
            this.finish("your balance is empty, nothing to send.");
         } else {
            this.step = PanicPayModule.Step.PAY;
            this.sentAt = 0L;
            this.tries = 0;
         }
      } else if (this.sentAt == 0L || l - this.sentAt >= 6000L) {
         if (this.sentAt != 0L && ++this.tries >= 3) {
            this.finish("/bal gave no balance, nothing was sent.");
         } else if (l >= Math.max(this.holdUntil, this.lastCommandAt + 1000L)) {
            if (!MoneyCommandGuard.allowAutomated("/bal", "Panic Pay")) {
               this.finish("/bal was blocked.");
            } else {
               clientPlayerEntity.networkHandler.sendChatCommand("bal");
               this.lastCommandAt = l;
               this.sentAt = l;
            }
         }
      }
   }

   private void pay(MinecraftClient minecraftClient, ClientPlayerEntity clientPlayerEntity, long l) {
      if (this.sentAt != 0L) {
         if (this.paidAt >= this.sentAt) {
            ChatSender.send("Panic Pay", "Sent $" + this.amount + " to " + this.target + ".");
            this.step = null;
            this.setEnabled(false);
         } else if (this.poorAt >= this.sentAt) {
            this.amount = lower(this.amount);
            if (this.amount != null && ++this.lowers <= 4) {
               this.sentAt = 0L;
            } else {
               this.finish("the server refused /pay (not enough money).");
            }
         } else if (this.errorAt >= this.sentAt) {
            this.finish("/pay failed: " + this.errorText);
         } else if (this.holdUntil > this.sentAt) {
            if (++this.tries >= 3) {
               this.finish("the server kept rate limiting /pay.");
            } else {
               this.sentAt = 0L;
            }
         } else {
            this.pressConfirm(minecraftClient);
            if (l - this.sentAt >= 6000L) {
               if (++this.tries >= 3) {
                  this.finish("no reply to /pay " + this.target + " " + this.amount + ", check /bal.");
               } else {
                  this.sentAt = 0L;
               }
            }
         }
      } else if (l >= Math.max(this.holdUntil, this.lastCommandAt + 1000L)) {
         clientPlayerEntity.networkHandler.sendChatCommand("pay " + this.target + " " + this.amount);
         this.lastCommandAt = l;
         this.sentAt = l;
         this.pressedDialog = null;
      }
   }

   private void pressConfirm(MinecraftClient minecraftClient) {
      if (minecraftClient.currentScreen instanceof DialogScreen var2 && var2 != this.pressedDialog) {
         for (Element var4 : var2.children()) {
            if (var4 instanceof ButtonWidget var5 && var5.active) {
               String var6 = normalize(var5.getMessage().getString());
               if (!var6.contains("cancel")
                  && !var6.equals("no")
                  && !var6.contains("deny")
                  && (var6.contains("confirm") || var6.equals("yes") || var6.contains("pay") || var6.contains("accept"))) {
                  this.pressedDialog = var2;
                  var5.onPress(new KeyInput(257, 0, 0));
                  return;
               }
            }
         }
      }
   }

   private static String onlineName(String string) {
      ClientPlayNetworkHandler var1 = MinecraftClient.getInstance().getNetworkHandler();
      if (var1 == null) {
         return null;
      } else {
         for (PlayerListEntry var3 : var1.getPlayerList()) {
            String var4 = var3.getProfile().name();
            if (var4 != null && var4.equalsIgnoreCase(string)) {
               return var4;
            }
         }

         return null;
      }
   }

   static String payAmount(String string, String string2) {
      if (string == null) {
         return null;
      } else {
         BigDecimal var2;
         try {
            var2 = new BigDecimal(string.replace(",", ""));
         } catch (NumberFormatException var4) {
            return null;
         }

         if (string2 != null && !string2.isEmpty()) {
            return var2.signum() > 0 ? var2.stripTrailingZeros().toPlainString() + string2.toLowerCase(Locale.ROOT) : null;
         } else {
            BigDecimal var3 = var2.setScale(0, RoundingMode.FLOOR);
            return var3.signum() > 0 ? var3.toPlainString() : null;
         }
      }
   }

   static String lower(String string) {
      if (string == null) {
         return null;
      } else {
         int var1 = string.length();

         while (var1 > 0 && Character.isLetter(string.charAt(var1 - 1))) {
            var1--;
         }

         String var2 = string.substring(var1);

         BigDecimal var3;
         try {
            var3 = new BigDecimal(string.substring(0, var1));
         } catch (NumberFormatException var6) {
            return null;
         }

         int var4 = Math.max(var3.scale(), var2.isEmpty() ? 0 : 2);
         BigDecimal var5 = var3.setScale(var4, RoundingMode.FLOOR).subtract(BigDecimal.ONE.movePointLeft(var4));
         return var5.signum() > 0 ? var5.stripTrailingZeros().toPlainString() + var2 : null;
      }
   }

   private static String normalize(String string) {
      if (string == null) {
         return "";
      } else {
         String var1 = string.replaceAll("§.", "");
         StringBuilder var2 = new StringBuilder(var1.length());

         for (int var3 = 0; var3 < var1.length(); var3++) {
            char var4 = var1.charAt(var3);
            int var5 = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ".indexOf(var4);
            var2.append(var5 >= 0 ? (char)(97 + var5) : var4);
         }

         return var2.toString().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum Step {
      BALANCE,
      PAY;
   }
}
