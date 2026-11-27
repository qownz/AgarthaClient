package net.agartha.client.integration.webhook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class WebhookMessage {
   private final List<WebhookEmbed> embeds = new ArrayList<>();
   private String content;

   public WebhookMessage content(String string) {
      this.content = string;
      return this;
   }

   public WebhookMessage addEmbed(WebhookEmbed arg) {
      if (arg != null) {
         this.embeds.add(arg);
      }

      return this;
   }

   public JsonObject build(WebhookProfile arg) {
      if ((this.content == null || this.content.isBlank()) && this.embeds.isEmpty()) {
         throw new IllegalStateException("Webhook body must include content or at least one embed");
      } else {
         JsonObject var2 = new JsonObject();
         var2.addProperty("username", arg.username());
         var2.addProperty("avatar_url", arg.avatarUrl());
         if (this.content != null && !this.content.isBlank()) {
            var2.addProperty("content", this.content);
         }

         if (!this.embeds.isEmpty()) {
            JsonArray var3 = new JsonArray();

            for (WebhookEmbed var5 : this.embeds) {
               var3.add(var5.toJson());
            }

            var2.add("embeds", var3);
         }

         return var2;
      }
   }
}
