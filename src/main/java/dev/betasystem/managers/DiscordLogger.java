package dev.betasystem.managers;

import dev.betasystem.BetaSystem;
import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class DiscordLogger {

    private final BetaSystem plugin;

    private static final int COLOR_GREEN  = 0x57F287;
    private static final int COLOR_RED    = 0xED4245;
    private static final int COLOR_YELLOW = 0xFEE75C;
    private static final int COLOR_BLUE   = 0x5865F2;

    public DiscordLogger(BetaSystem plugin) { this.plugin = plugin; }

    public void logFeatureToggle(String playerName, String feature, boolean activated) {
        send(
            plugin.getConfig().getString("webhook-features", ""),
            activated ? COLOR_GREEN : COLOR_RED,
            (activated ? "✅" : "❌") + " Beta Feature " + (activated ? "Activated" : "Deactivated"),
            "**" + playerName + "** " + (activated ? "enabled" : "disabled") + " **" + feature + "**",
            "Status", activated ? "ACTIVATED" : "DEACTIVATED"
        );
    }

    public void logBugReport(String playerName, String reason) {
        send(
            plugin.getConfig().getString("webhook-bugs", ""),
            COLOR_YELLOW,
            "🐛 Bug Report",
            "**" + playerName + "** reported a bug",
            "Description", reason
        );
    }

    public void logTesterAdded(String executor, String target) {
        send(
            plugin.getConfig().getString("webhook-features", ""),
            COLOR_BLUE,
            "➕ Beta Tester Added",
            "**" + target + "** was added as Beta Tester by **" + executor + "**",
            null, null
        );
    }

    public void logTesterRemoved(String executor, String target) {
        send(
            plugin.getConfig().getString("webhook-features", ""),
            COLOR_RED,
            "➖ Beta Tester Removed",
            "**" + target + "** was removed as Beta Tester by **" + executor + "**",
            null, null
        );
    }

    private void send(String webhookUrl, int color, String title, String desc,
                      String fieldName, String fieldValue) {
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.contains("YOUR_")) return;

        String field = (fieldName != null)
            ? ",\"fields\":[{\"name\":\"" + esc(fieldName) + "\",\"value\":\"" + esc(fieldValue) + "\",\"inline\":false}]"
            : "";

        String json = "{\"embeds\":[{"
            + "\"title\":\""       + esc(title) + "\","
            + "\"description\":\"" + esc(desc)  + "\","
            + "\"color\":"         + color       + ","
            + "\"timestamp\":\""   + Instant.now() + "\","
            + "\"footer\":{\"text\":\"BetaSystem | RuneMC\"}"
            + field + "}]}";

        final String finalUrl  = webhookUrl;
        final String finalJson = json;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(finalUrl).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("User-Agent", "BetaSystem");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(finalJson.getBytes(StandardCharsets.UTF_8));
                }
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("[Discord] " + e.getMessage());
            }
        });
    }

    private String esc(String s) {
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");
    }
}
