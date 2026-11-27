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
import net.agartha.client.module.impl.misc.AutoEatModule;
import net.agartha.client.module.impl.misc.AutoJumpModule;
import net.agartha.client.module.impl.misc.AutoMineModule;
import net.agartha.client.module.impl.misc.AutoReconnectModule;
import net.agartha.client.module.impl.misc.AutoSellModule;
import net.agartha.client.module.impl.misc.AutoToolModule;
import net.agartha.client.module.impl.misc.AutoWalkModule;
import net.agartha.client.module.impl.misc.BlockOverlayModule;
import net.agartha.client.module.impl.misc.BudEspModule;
import net.agartha.client.module.impl.misc.CakeEspModule;
import net.agartha.client.module.impl.misc.FakePayModule;
import net.agartha.client.module.impl.misc.FakeScoreboardModule;
import net.agartha.client.module.impl.misc.FreecamModule;
import net.agartha.client.module.impl.misc.HomeResetModule;
import net.agartha.client.module.impl.misc.NameProtectModule;
import net.agartha.client.module.impl.misc.PanicPayModule;
import net.agartha.client.module.impl.misc.PlayerAlertModule;
import net.agartha.client.module.impl.misc.PlayerBypassModule;
import net.agartha.client.module.impl.misc.RainNotifierModule;
import net.agartha.client.module.impl.misc.SkinChangeModule;
import net.agartha.client.module.impl.misc.SmoothHotbarModule;
import net.agartha.client.module.impl.misc.SneakModule;
import net.agartha.client.module.impl.misc.SpawnerProtectModule;
import net.agartha.client.module.impl.misc.SprintModule;
import net.agartha.client.module.impl.misc.StaffAlertModule;
import net.agartha.client.module.impl.misc.SwingSpeedModule;
import net.agartha.client.module.impl.misc.ZoomModule;
import net.agartha.client.module.impl.render.BlockEntityDebugModule;
import net.agartha.client.module.impl.render.BlockEspModule;
import net.agartha.client.module.impl.render.DiggerEspModule;
import net.agartha.client.module.impl.render.EspModule;
import net.agartha.client.module.impl.render.FreelookModule;
import net.agartha.client.module.impl.render.FullbrightModule;
import net.agartha.client.module.impl.render.HandOverlayModule;
import net.agartha.client.module.impl.render.HoleEspModule;
import net.agartha.client.module.impl.render.ItemEspModule;
import net.agartha.client.module.impl.render.JumpRingsModule;
import net.agartha.client.module.impl.render.LightEspModule;
import net.agartha.client.module.impl.render.MobChunksModule;
import net.agartha.client.module.impl.render.MotionBlurModule;
import net.agartha.client.module.impl.render.NametagsModule;
import net.agartha.client.module.impl.render.NetheriteFinderModule;
import net.agartha.client.module.impl.render.NoRenderModule;
import net.agartha.client.module.impl.render.PearlTrajectoryModule;
import net.agartha.client.module.impl.render.PlayerEspModule;
import net.agartha.client.module.impl.render.PlayerOverlayModule;
import net.agartha.client.module.impl.render.SeedXrayModule;
import net.agartha.client.module.impl.render.SpawnerNotifierModule;
import net.agartha.client.module.impl.render.StorageEspModule;
import net.agartha.client.module.impl.render.SusChunkFinderModule;
import net.agartha.client.module.impl.render.SusChunkFinderPlusModule;
import net.agartha.client.module.impl.render.SusEspModule;
import net.agartha.client.module.impl.render.SwingAnimationModule;
import net.agartha.client.module.impl.render.ViewModelModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class BuiltinModuleProvider implements ModuleProvider {
   @Override
   public String id() {
      return "builtin";
   }

   @Override
   public List<Module> createModules() {
      return new ArrayList<>(
         List.of(
            new AutoEatModule(),
            new AutoJumpModule(),
            new AutoMineModule(),
            new AutoReconnectModule(),
            new AutoToolModule(),
            new AutoWalkModule(),
            new FreelookModule(),
            new HomeResetModule(),
            new NameProtectModule(),
            new RainNotifierModule(),
            new SkinChangeModule(),
            new SmoothHotbarModule(),
            new SneakModule(),
            new SprintModule(),
            new SwingAnimationModule(),
            new SwingSpeedModule(),
            new ViewModelModule(),
            new BlockEspModule(),
            new BlockOverlayModule(),
            new PlayerOverlayModule(),
            new EspModule(),
            new PlayerEspModule(),
            new FreecamModule(),
            new FullbrightModule(),
            new HandOverlayModule(),
            new HoleEspModule(),
            new ItemEspModule(),
            new JumpRingsModule(),
            new MotionBlurModule(),
            new NametagsModule(),
            new NoRenderModule(),
            new PearlTrajectoryModule(),
            new StorageEspModule(),
            new ZoomModule(),
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
            new CakeEspModule(),
            new BudEspModule(),
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
