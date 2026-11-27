package net.agartha.client.module;

import java.util.ArrayList;
import java.util.List;
import net.agartha.client.module.impl.client.AgarthaModule;
import net.agartha.client.module.impl.client.ChatMacrosModule;
import net.agartha.client.module.impl.client.ChunkBlendModule;
import net.agartha.client.module.impl.client.ColorSettingsModule;
import net.agartha.client.module.impl.client.ConfigModule;
import net.agartha.client.module.impl.client.CrosshairModule;
import net.agartha.client.module.impl.client.DiscordRpcModule;
import net.agartha.client.module.impl.client.HudModule;
import net.agartha.client.module.impl.client.KeyboardSoundsModule;
import net.agartha.client.module.impl.client.MonsterModeModule;
import net.agartha.client.module.impl.client.NotificationsModule;
import net.agartha.client.module.impl.client.RegionMapModule;
import net.agartha.client.module.impl.client.SpotifyHudModule;
import net.agartha.client.module.impl.client.UtilitiesModule;
import net.agartha.client.module.impl.misc.AhTraderModule;
import net.agartha.client.module.impl.misc.AutoSellModule;
import net.agartha.client.module.impl.misc.FakePayModule;
import net.agartha.client.module.impl.misc.FakeScoreboardModule;
import net.agartha.client.module.impl.misc.PanicPayModule;
import net.agartha.client.module.impl.misc.PlayerAlertModule;
import net.agartha.client.module.impl.misc.PlayerBypassModule;
import net.agartha.client.module.impl.misc.SpawnerProtectModule;
import net.agartha.client.module.impl.misc.StaffAlertModule;
import net.agartha.client.module.impl.misc.g;
import net.agartha.client.module.impl.misc.k;
import net.agartha.client.module.impl.misc.l;
import net.agartha.client.module.impl.misc.m;
import net.agartha.client.module.impl.misc.n;
import net.agartha.client.module.impl.misc.o;
import net.agartha.client.module.impl.misc.p;
import net.agartha.client.module.impl.misc.q;
import net.agartha.client.module.impl.misc.s;
import net.agartha.client.module.impl.misc.v;
import net.agartha.client.module.impl.render.BlockEntityDebugModule;
import net.agartha.client.module.impl.render.DiggerEspModule;
import net.agartha.client.module.impl.render.LightEspModule;
import net.agartha.client.module.impl.render.MobChunksModule;
import net.agartha.client.module.impl.render.NetheriteFinderModule;
import net.agartha.client.module.impl.render.SeedXrayModule;
import net.agartha.client.module.impl.render.SpawnerNotifierModule;
import net.agartha.client.module.impl.render.SusChunkFinderModule;
import net.agartha.client.module.impl.render.SusChunkFinderPlusModule;
import net.agartha.client.module.impl.render.SusEspModule;
import net.agartha.client.module.impl.render.h;
import net.agartha.client.module.impl.render.i;
import net.agartha.client.module.impl.render.j;
import net.agartha.client.module.impl.render.r;
import net.agartha.client.module.impl.render.t;
import net.agartha.client.module.impl.render.u;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class a implements d {
   @Override
   public String id() {
      return "builtin";
   }

   @Override
   public List<b> createModules() {
      return new ArrayList<>(
         List.of(
            new net.agartha.client.module.impl.misc.a(),
            new net.agartha.client.module.impl.misc.b(),
            new net.agartha.client.module.impl.misc.c(),
            new net.agartha.client.module.impl.misc.d(),
            new net.agartha.client.module.impl.misc.e(),
            new net.agartha.client.module.impl.misc.f(),
            new net.agartha.client.module.impl.render.d(),
            new l(),
            new m(),
            new n(),
            new o(),
            new p(),
            new q(),
            new s(),
            new t(),
            new net.agartha.client.module.impl.misc.t(),
            new u(),
            new net.agartha.client.module.impl.render.a_o(),
            new g(),
            new r(),
            new net.agartha.client.module.impl.render.c(),
            new net.agartha.client.module.impl.render.q(),
            new k(),
            new net.agartha.client.module.impl.render.e(),
            new net.agartha.client.module.impl.render.f(),
            new h(),
            new i(),
            new j(),
            new net.agartha.client.module.impl.render.k(),
            new net.agartha.client.module.impl.render.l(),
            new net.agartha.client.module.impl.render.m(),
            new net.agartha.client.module.impl.render.p(),
            new net.agartha.client.module.impl.render.s(),
            new v(),
            new AgarthaModule(),
            new ChatMacrosModule(),
            new UtilitiesModule(),
            new DiscordRpcModule(),
            new ChunkBlendModule(),
            new ColorSettingsModule(),
            new ConfigModule(),
            new CrosshairModule(),
            new FakeScoreboardModule(),
            new HudModule(),
            new KeyboardSoundsModule(),
            new MonsterModeModule(),
            new NotificationsModule(),
            new RegionMapModule(),
            new SpotifyHudModule(),
            new SpawnerProtectModule(),
            new net.agartha.client.module.impl.misc.i(),
            new net.agartha.client.module.impl.misc.h(),
            new AhTraderModule(),
            new AutoSellModule(),
            new PanicPayModule(),
            new FakePayModule(),
            new SpawnerNotifierModule(),
            new SusChunkFinderModule(),
            new SusChunkFinderPlusModule(),
            new DiggerEspModule(),
            new MobChunksModule(),
            new SusEspModule(),
            new NetheriteFinderModule(),
            new BlockEntityDebugModule(),
            new LightEspModule(),
            new StaffAlertModule(),
            new PlayerAlertModule(),
            new PlayerBypassModule(),
            new SeedXrayModule()
         )
      );
   }
}
