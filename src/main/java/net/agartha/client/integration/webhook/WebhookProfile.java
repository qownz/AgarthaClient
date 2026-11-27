package net.agartha.client.integration.webhook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record WebhookProfile(String url, String username, String avatarUrl) {
   public static final String DEFAULT_USERNAME = "Agartha";
   public static final String DEFAULT_AVATAR_URL = "https://i.imgur.com/W3cTqvc.png";

   public WebhookProfile(String url, String username, String avatarUrl) {
      url = a(url, "url");
      username = b(username, "Agartha");
      avatarUrl = b(avatarUrl, "https://i.imgur.com/W3cTqvc.png");
      this.url = url;
      this.username = username;
      this.avatarUrl = avatarUrl;
   }

   public WebhookProfile(String string) {
      this(string, "Agartha", "https://i.imgur.com/W3cTqvc.png");
   }

   public WebhookProfile withIdentity(String string, String string2) {
      return new WebhookProfile(this.url, string, string2);
   }

   private static String a(String string, String string2) {
      String var2 = b(string, null);
      if (var2 == null) {
         throw new IllegalArgumentException(string2 + " cannot be blank");
      } else {
         return var2;
      }
   }

   private static String b(String string, String string2) {
      if (string == null) {
         return string2;
      } else {
         String var2 = string.trim();
         return var2.isEmpty() ? string2 : var2;
      }
   }
}
