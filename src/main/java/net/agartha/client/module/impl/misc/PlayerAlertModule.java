package net.agartha.client.module.impl.misc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.agartha.client.c.e;
import net.agartha.client.d.f;
import net.agartha.client.d.a_pkg.q;
import net.agartha.client.e.ak;
import net.agartha.client.module.b;
import net.agartha.client.module.c;
import net.agartha.client.module.a_pkg.i;
import net.agartha.client.module.a_pkg.j;
import net.agartha.client.module.a_pkg.l;
import net.agartha.client.util.ab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public final class PlayerAlertModule extends b {
   private static final float IN_SECONDS = 0.22F;
   private static final float HOLD_SECONDS = 3.4F;
   private static final float OUT_SECONDS = 0.45F;
   private static final long RTP_COOLDOWN_MS = 8000L;
   private final j sRange = (j)this.addSetting(new j("Range", 64.0F, 8.0F, 256.0F).whole());
   private final i sAction = (i)this.addSetting(new i("On Spot", "Alert Only", new String[]{"Alert Only", "Auto Quit", "Auto RTP"}));
   private final net.agartha.client.module.a_pkg.b sSound = (net.agartha.client.module.a_pkg.b)this.addSetting(new net.agartha.client.module.a_pkg.b("Sound", true));
   private final l sWhitelist = (l)this.addSetting(new l("Whitelist", "", 512));
   private final Set<String> whitelist = new HashSet<>();
   private String whitelistCache;
   private final Set<String> knownNearby = new HashSet<>();
   private final Set<String> seenThisTick = new HashSet<>();
   private final List<AbstractClientPlayerEntity> newcomers = new ArrayList<>();
   private long lastRtpMs;
   private String bannerName = "";
   private String bannerDetail = "";
   private long bannerStart;

   public PlayerAlertModule() {
      super("Player Alert", "Warns when another player comes near, and can quit or /rtp away for you.", c.DONUT);
      this.sWhitelist.visibleWhen(() -> false);
   }

   protected void onDisable() {
      this.knownNearby.clear();
      this.bannerName = "";
      this.bannerStart = 0L;
   }

   private Set<String> whitelist() {
      String var1 = this.sWhitelist.get();
      if (!var1.equals(this.whitelistCache)) {
         this.whitelistCache = var1;
         this.whitelist.clear();

         for (String var5 : var1.split("[,\\s]+")) {
            if (!var5.isBlank()) {
               this.whitelist.add(var5.trim().toLowerCase(Locale.ROOT));
            }
         }
      }

      return this.whitelist;
   }

   public List<String> whitelistNames() {
      ArrayList var1 = new ArrayList();
      HashSet var2 = new HashSet();

      for (String var6 : this.sWhitelist.get().split("[,\\s]+")) {
         String var7 = var6.trim();
         if (!var7.isEmpty() && var2.add(var7.toLowerCase(Locale.ROOT))) {
            var1.add(var7);
         }
      }

      return var1;
   }

   public boolean isWhitelisted(String string) {
      return string != null && this.whitelist().contains(string.trim().toLowerCase(Locale.ROOT));
   }

   public boolean addWhitelist(String string) {
      String var2 = string == null ? "" : string.trim();
      if (var2.matches("[A-Za-z0-9_]{1,16}") && !this.isWhitelisted(var2)) {
         List var3 = this.whitelistNames();
         var3.add(var2);
         this.setWhitelist(var3);
         return true;
      } else {
         return false;
      }
   }

   public void removeWhitelist(String string) {
      List var2 = this.whitelistNames();
      if (var2.removeIf(string2 -> ((String)string2).equalsIgnoreCase(string))) {
         this.setWhitelist(var2);
      }
   }

   private void setWhitelist(List<String> list) {
      this.sWhitelist.set(String.join(", ", list));
      e.getInstance().save();
   }

   @f
   public void onTick(q arg) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null && var2.player != null) {
         double var3 = this.sRange.getInt();
         double var5 = var3 * var3;
         this.seenThisTick.clear();
         this.newcomers.clear();
         AbstractClientPlayerEntity var7 = null;
         double var8 = Double.MAX_VALUE;

         for (AbstractClientPlayerEntity var11 : var2.world.getPlayers()) {
            if (var11 != var2.player && !var11.isRemoved()) {
               String var12 = var11.getGameProfile().name();
               if (var12 != null && !this.isWhitelisted(var12)) {
                  double var13 = var2.player.squaredDistanceTo(var11);
                  if (!(var13 > var5)) {
                     String var15 = var12.toLowerCase(Locale.ROOT);
                     this.seenThisTick.add(var15);
                     if (!this.knownNearby.contains(var15)) {
                        this.newcomers.add(var11);
                        if (var13 < var8) {
                           var8 = var13;
                           var7 = var11;
                        }
                     }
                  }
               }
            }
         }

         this.knownNearby.retainAll(this.seenThisTick);
         this.knownNearby.addAll(this.seenThisTick);
         if (var7 != null) {
            if (this.sAction.is("Auto Quit")) {
               var2.world.disconnect(Text.literal("[Agartha+] Player Alert: quit"));
            } else if (this.sAction.is("Auto RTP")) {
               long var17 = System.currentTimeMillis();
               if (var17 - this.lastRtpMs >= 8000L && var2.getNetworkHandler() != null) {
                  this.lastRtpMs = var17;
                  var2.getNetworkHandler().sendChatCommand("rtp");
                  ab.send("Player", var7.getGameProfile().name() + " nearby, /rtp");
               }
            } else {
               for (AbstractClientPlayerEntity var18 : this.newcomers) {
                  ab.send("Player", var18.getGameProfile().name() + " is nearby");
               }

               this.raiseBanner(var7.getGameProfile().name());
            }
         }
      } else {
         this.knownNearby.clear();
      }
   }

   private void raiseBanner(String string) {
      this.bannerName = string;
      this.bannerDetail = "nearby";
      this.bannerStart = System.nanoTime();
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (this.sSound.get() && var2.getSoundManager() != null) {
         var2.getSoundManager().play(PositionedSoundInstance.ui(ak.resolve("Pop 1"), 1.0F, 0.95F));
      }
   }

   public boolean bannerActive() {
      return this.isEnabled() && !this.bannerName.isEmpty() && this.bannerAlpha() > 0.01F;
   }

   public String bannerName() {
      return this.bannerName;
   }

   public String bannerDetail() {
      return this.bannerDetail;
   }

   public float bannerAlpha() {
      if (!this.bannerName.isEmpty() && this.bannerStart != 0L) {
         float var1 = (float)(System.nanoTime() - this.bannerStart) / 1.0E9F;
         if (var1 < 0.22F) {
            return var1 / 0.22F;
         } else if (var1 < 3.6200001F) {
            return 1.0F;
         } else {
            float var2 = var1 - 0.22F - 3.4F;
            return var2 >= 0.45F ? 0.0F : 1.0F - var2 / 0.45F;
         }
      } else {
         return 0.0F;
      }
   }
}
