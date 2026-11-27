package net.agartha.client.module.impl.misc;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import net.agartha.client.d.f;
import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.module.e;
import net.agartha.client.module.a_pkg.l;
import net.agartha.client.util.aa;
import net.agartha.client.util.q;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

@Environment(EnvType.CLIENT)
public final class FakeScoreboardModule extends b {
   private static final int GRAY = 11184810;
   private static final int GREEN = 5635925;
   private static final int SLOT_MONEY = 0;
   private static final int SLOT_SHARDS = 1;
   private static final int SLOT_KILLS = 2;
   private static final int SLOT_DEATHS = 3;
   private static final int SLOT_PLAYTIME = 4;
   private static final int SLOTS = 5;
   private static final String DEFAULT_NAME = "/AgarthaClient";
   private static final String OLD_DEFAULT_NAME = "Player";
   private static final String DEFAULT_MONEY = "1m";
   private static final String DEFAULT_SHARDS = "0";
   private static final String DEFAULT_KILLS = "0";
   private static final String DEFAULT_DEATHS = "0";
   private static final String DEFAULT_PLAYTIME = "0d";
   private final l name = (l)this.addSetting(new l("Name", "/AgarthaClient"));
   private final l money = (l)this.addSetting(new l("Money", "1m"));
   private final l shards = (l)this.addSetting(new l("Shards", "0"));
   private final l kills = (l)this.addSetting(new l("Kills", "0"));
   private final l deaths = (l)this.addSetting(new l("Deaths", "0"));
   private final l playtime = (l)this.addSetting(new l("Playtime", "0d"));
   private final net.agartha.client.module.a_pkg.b sidebar = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Sidebar", true));
   private final net.agartha.client.module.a_pkg.b balanceCommand = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Balance Command", true)
   );
   private final net.agartha.client.module.a_pkg.b deductOnPay = (net.agartha.client.module.a_pkg.b)this.addSetting(
      new net.agartha.client.module.a_pkg.b("Deduct On Pay", true)
   );
   private String lastMoneyText;
   private double liveBalance;
   private int widthCursor;
   private int drawCursor;
   private int scoreCursor;
   private final boolean[] drawHadNumber = new boolean[5];

   public FakeScoreboardModule() {
      super("Fake Stats", "Shows your own money, shards, kills, deaths and playtime on the sidebar, the tab and /bal.", c.CLIENT);
      this.name.visibleWhen(this.sidebar::get);
   }

   protected void onEnable() {
      this.nameText();
      fillBlank(this.money, "1m");
      fillBlank(this.shards, "0");
      fillBlank(this.kills, "0");
      fillBlank(this.deaths, "0");
      fillBlank(this.playtime, "0d");
      this.lastMoneyText = null;
      this.syncFromSetting();
      aa.reapply();
   }

   protected void onDisable() {
      aa.reapply();
   }

   public static void beginPass() {
      FakeScoreboardModule var0 = current();
      if (var0 != null) {
         var0.beginSidebar();
      }
   }

   public static Text rewriteTitle(Text text) {
      FakeScoreboardModule var1 = current();
      return var1 == null ? text : var1.replaceTitle(text);
   }

   public static Text rewriteForWidth(Text text) {
      FakeScoreboardModule var1 = current();
      return var1 == null ? text : var1.rewriteLine(text, true);
   }

   public static Text rewriteForDraw(Text text) {
      FakeScoreboardModule var1 = current();
      return var1 == null ? text : var1.rewriteLine(text, false);
   }

   public static Text rewriteScore(Text text) {
      FakeScoreboardModule var1 = current();
      return var1 == null ? text : var1.rewriteScoreLine(text);
   }

   public static int sidebarGap() {
      FakeScoreboardModule var0 = current();
      return var0 != null && var0.isEnabled() && var0.sidebar.get() ? 9 : 0;
   }

   public static void deductPaid(String string) {
      FakeScoreboardModule var1 = current();
      if (var1 != null && var1.isEnabled() && var1.deductOnPay.get()) {
         double var2 = q.parse(string);
         if (!Double.isNaN(var2)) {
            var1.deduct(var2);
         }
      }
   }

   @f
   public void onChatSend(net.agartha.client.d.a_pkg.b arg) {
      if (arg.message != null && this.tryInterceptBalance(arg.message)) {
         arg.setCancelled(true);
      }
   }

   private void beginSidebar() {
      this.widthCursor = 0;
      this.drawCursor = 0;
      this.scoreCursor = 0;
      Arrays.fill(this.drawHadNumber, false);
   }

   private void syncFromSetting() {
      String var1 = this.money.get();
      if (!Objects.equals(var1, this.lastMoneyText)) {
         this.lastMoneyText = var1;
         double var2 = q.parse(var1);
         this.liveBalance = Double.isNaN(var2) ? 0.0 : Math.max(0.0, var2);
      }
   }

   private double getLiveBalance() {
      this.syncFromSetting();
      return this.liveBalance;
   }

   private void deduct(double d) {
      this.syncFromSetting();
      this.liveBalance = Math.max(0.0, this.liveBalance - Math.max(0.0, d));
      aa.reapply();
   }

   private Text replaceTitle(Text text) {
      return (Text)(this.isEnabled() && this.sidebar.get() && text != null ? Text.literal(this.nameText()).setStyle(leadingStyle(text)) : text);
   }

   private Text rewriteLine(Text text, boolean bl) {
      if (this.isEnabled() && this.sidebar.get() && text != null) {
         String var3 = text.getString().replaceAll("§.", "");
         if (var3.isBlank()) {
            return text;
         } else {
            int var4 = bl ? this.widthCursor++ : this.drawCursor++;
            if (var4 >= 5) {
               return text;
            } else {
               boolean var5 = q.lastNumberSpan(var3) != null || var4 == 4 && q.valueSpan(var3) != null;
               if (!bl) {
                  this.drawHadNumber[var4] = var5;
               }

               String var6 = this.displayOf(var4);
               if (var6.isEmpty()) {
                  return text;
               } else if (var4 == 4 && q.valueSpan(var3) != null) {
                  return q.replaceValue(text, var6);
               } else if (var5) {
                  return q.replaceNumber(text, var6);
               } else {
                  Style var7 = leadingStyle(text);
                  String var8 = var3.stripTrailing();
                  return Text.literal(var8.isEmpty() ? var6 : var8 + " " + var6).setStyle(var7);
               }
            }
         }
      } else {
         return text;
      }
   }

   private Text rewriteScoreLine(Text text) {
      if (this.isEnabled() && this.sidebar.get() && text != null) {
         int var2 = this.scoreCursor++;
         if (var2 >= 5 || this.drawHadNumber[var2]) {
            return text;
         } else if (text.getString().isBlank()) {
            return text;
         } else {
            String var3 = this.displayOf(var2);
            return (Text)(var3.isEmpty() ? text : Text.literal(var3).setStyle(leadingStyle(text)));
         }
      } else {
         return text;
      }
   }

   private String displayOf(int i) {
      return switch (i) {
         case 0 -> this.moneyDisplay();
         case 1 -> shown(this.shards, "0");
         case 2 -> shown(this.kills, "0");
         case 3 -> shown(this.deaths, "0");
         case 4 -> shown(this.playtime, "0d");
         default -> "";
      };
   }

   private String moneyDisplay() {
      String var1 = shown(this.money, "1m");
      return Double.isNaN(q.parse(var1)) ? var1 : q.shortForm(this.getLiveBalance());
   }

   private String nameText() {
      String var1 = this.name.get();
      if (var1 != null && !var1.trim().isEmpty() && !"Player".equals(var1.trim())) {
         return var1.trim();
      } else {
         this.name.set("/AgarthaClient");
         return "/AgarthaClient";
      }
   }

   private static String shown(l arg, String string) {
      String var2 = arg.get();
      if (var2 != null && !var2.trim().isEmpty()) {
         return var2.trim();
      } else {
         arg.set(string);
         return string;
      }
   }

   private static void fillBlank(l arg, String string) {
      shown(arg, string);
   }

   private boolean tryInterceptBalance(String string) {
      if (this.isEnabled() && this.balanceCommand.get()) {
         String[] var2 = string.trim().split("\\s+");
         if (var2.length != 0 && !var2[0].isEmpty()) {
            String var3 = var2[0];
            if (var3.startsWith("/")) {
               var3 = var3.substring(1);
            }

            if (!var3.equalsIgnoreCase("bal") && !var3.equalsIgnoreCase("balance")) {
               return false;
            } else {
               MinecraftClient var4 = MinecraftClient.getInstance();
               if (var4.player == null) {
                  return false;
               } else if (var2.length >= 2 && !var2[1].equalsIgnoreCase(var4.player.getGameProfile().name())) {
                  return false;
               } else {
                  String var5 = String.format(Locale.US, "%,d", Math.round(this.getLiveBalance()));
                  MutableText var6 = Text.literal("You have ")
                     .append(Text.literal("$").fillStyle(Style.EMPTY.withColor(TextColor.fromRgb(5635925))))
                     .append(Text.literal(" " + var5));
                  var4.player.sendMessage(var6, false);
                  var4.player.sendMessage(var6, true);
                  return true;
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static Style leadingStyle(Text text) {
      Style[] var1 = new Style[]{text.getStyle()};
      text.visit((style, string) -> {
         if (string != null && !string.isEmpty()) {
            var1[0] = style;
            return Optional.of(Boolean.TRUE);
         } else {
            return Optional.empty();
         }
      }, Style.EMPTY);
      return var1[0];
   }

   public static String tabMoney() {
      FakeScoreboardModule var0 = current();
      return var0 != null && var0.isEnabled() ? var0.moneyDisplay() : null;
   }

   private static FakeScoreboardModule current() {
      return (FakeScoreboardModule)e.get(FakeScoreboardModule.class);
   }
}
