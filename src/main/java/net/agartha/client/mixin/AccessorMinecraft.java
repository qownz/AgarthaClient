package net.agartha.client.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.UserApiService.UserProperties;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.util.ApiServices;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin({MinecraftClient.class})
public interface AccessorMinecraft {
   @Mutable
   @Accessor("session")
   void agartha$setUser(Session session);

   @Mutable
   @Accessor("userApiService")
   void agartha$setUserApiService(UserApiService userApiService);

   @Mutable
   @Accessor("profileKeys")
   void agartha$setProfileKeyPairManager(ProfileKeys profileKeys);

   @Mutable
   @Accessor("userPropertiesFuture")
   void agartha$setUserPropertiesFuture(CompletableFuture<UserProperties> completableFuture);

   @Mutable
   @Accessor("apiServices")
   void agartha$setServices(ApiServices apiServices);

   @Mutable
   @Accessor("socialInteractionsManager")
   void agartha$setPlayerSocialManager(SocialInteractionsManager socialInteractionsManager);

   @Mutable
   @Accessor("abuseReportContext")
   void agartha$setReportingContext(AbuseReportContext abuseReportContext);

   @Mutable
   @Accessor("gameProfileFuture")
   void agartha$setProfileFuture(CompletableFuture<ProfileResult> completableFuture);
}
