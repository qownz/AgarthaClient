package net.agartha.client.integration.webhook;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import net.agartha.client.AgarthaClientMod;
import net.agartha.client.integration.webhook.WebhookMessage;
import net.agartha.client.integration.webhook.WebhookProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class WebhookClient {
   private final HttpClient httpClient;

   public WebhookClient() {
      this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build());
   }

   WebhookClient(HttpClient httpClient) {
      this.httpClient = httpClient;
   }

   public boolean send(WebhookProfile arg, WebhookMessage arg2) {
      JsonObject var3;
      try {
         var3 = arg2.build(arg);
      } catch (RuntimeException var7) {
         AgarthaClientMod.LOGGER.warn("[Agartha] Failed to build webhook body", var7);
         return false;
      }

      HttpRequest var4 = HttpRequest.newBuilder(URI.create(arg.url()))
         .timeout(Duration.ofSeconds(10L))
         .header("Content-Type", "application/json")
         .POST(BodyPublishers.ofString(var3.toString()))
         .build();

      try {
         HttpResponse var5 = this.httpClient.send(var4, BodyHandlers.discarding());
         int var6 = var5.statusCode();
         if (var6 >= 200 && var6 < 300) {
            return true;
         }

         AgarthaClientMod.LOGGER.warn("[Agartha] Webhook request failed with status {}", var6);
      } catch (InterruptedException | IOException var8) {
         if (var8 instanceof InterruptedException) {
            Thread.currentThread().interrupt();
         }

         AgarthaClientMod.LOGGER.warn("[Agartha] Webhook request failed", var8);
      }

      return false;
   }
}
