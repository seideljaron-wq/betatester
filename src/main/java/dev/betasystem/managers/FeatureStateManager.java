package dev.betasystem.managers;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Tracks which features are active per player and applies/removes them.
 */
public class FeatureStateManager {

    public enum Feature {
        SPECTATOR("Spectator Mode",  "🔭"),
        VANISH   ("Vanish",          "👻"),
        SPEED    ("Speed Boost",     "⚡"),
        NIGHT_VIS("Night Vision",    "🌙"),
        NO_FALL  ("No Fall Damage",  "🛡");

        public final String displayName;
        public final String icon;
        Feature(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
    }

    // player UUID → set of active features
    private final Map<UUID, Set<Feature>> activeFeatures = new HashMap<>();

    public boolean isActive(UUID uuid, Feature feature) {
        return activeFeatures.getOrDefault(uuid, Set.of()).contains(feature);
    }

    /** Toggle a feature. Returns true if now ACTIVE, false if now INACTIVE. */
    public boolean toggle(Player player, Feature feature) {
        UUID uuid = player.getUniqueId();
        Set<Feature> active = activeFeatures.computeIfAbsent(uuid, k -> new HashSet<>());

        if (active.contains(feature)) {
            active.remove(feature);
            deactivate(player, feature);
            return false;
        } else {
            active.add(feature);
            activate(player, feature);
            return true;
        }
    }

    private void activate(Player player, Feature feature) {
        switch (feature) {
            case SPECTATOR -> {
                player.setMetadata("beta_prev_gamemode",
                    new org.bukkit.metadata.FixedMetadataValue(
                        dev.betasystem.BetaSystem.getInstance(), player.getGameMode().name()));
                player.setGameMode(GameMode.SPECTATOR);
            }
            case VANISH -> {
                for (org.bukkit.entity.Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
                    if (!online.getUniqueId().equals(player.getUniqueId())) {
                        online.hidePlayer(dev.betasystem.BetaSystem.getInstance(), player);
                    }
                }
            }
            case SPEED -> player.addPotionEffect(
                new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3, false, false, true));
            case NIGHT_VIS -> player.addPotionEffect(
                new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, true));
            case NO_FALL -> player.setFallDistance(0f);
        }
    }

    private void deactivate(Player player, Feature feature) {
        switch (feature) {
            case SPECTATOR -> {
                GameMode prev = GameMode.SURVIVAL;
                if (player.hasMetadata("beta_prev_gamemode")) {
                    try {
                        prev = GameMode.valueOf(
                            player.getMetadata("beta_prev_gamemode").get(0).asString());
                    } catch (Exception ignored) {}
                    player.removeMetadata("beta_prev_gamemode", dev.betasystem.BetaSystem.getInstance());
                }
                player.setGameMode(prev);
            }
            case VANISH -> {
                for (org.bukkit.entity.Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
                    online.showPlayer(dev.betasystem.BetaSystem.getInstance(), player);
                }
            }
            case SPEED    -> player.removePotionEffect(PotionEffectType.SPEED);
            case NIGHT_VIS -> player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            case NO_FALL  -> {} // nothing persistent to undo
        }
    }

    /** Called on plugin disable – reset everything */
    public void resetAll() {
        for (Map.Entry<UUID, Set<Feature>> entry : activeFeatures.entrySet()) {
            org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(entry.getKey());
            if (p == null) continue;
            for (Feature f : entry.getValue()) deactivate(p, f);
        }
        activeFeatures.clear();
    }

    /** Reset a specific player (e.g. on quit) */
    public void resetPlayer(Player player) {
        Set<Feature> active = activeFeatures.remove(player.getUniqueId());
        if (active == null) return;
        for (Feature f : active) deactivate(player, f);
    }
}
