package dev.betasystem.gui;

import dev.betasystem.BetaSystem;
import dev.betasystem.managers.FeatureStateManager.Feature;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class BetaGUIListener implements Listener {

    private final BetaSystem plugin;

    public BetaGUIListener(BetaSystem plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = PlainTextComponentSerializer.plainText()
            .serialize(event.getView().title());

        if (!title.equals(BetaGUI.TITLE)) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        int[] featureSlots = BetaGUI.getFeatureSlots();

        // Find which feature was clicked
        for (int i = 0; i < featureSlots.length; i++) {
            if (featureSlots[i] == slot) {
                Feature feature  = BetaGUI.getFeatureOrder()[i];
                Material mat     = BetaGUI.getFeatureMats()[i];

                boolean nowActive = plugin.getFeatureManager().toggle(player, feature);

                // Update the item in the GUI
                ItemStack updated = BetaGUI.buildFeatureItem(feature, mat, nowActive);
                event.getInventory().setItem(slot, updated);

                // Discord log
                plugin.getDiscordLogger().logFeatureToggle(
                    player.getName(), feature.displayName, nowActive);

                return;
            }
        }
        // Clicked filler glass – do nothing
    }

    // Reset features when player leaves
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getFeatureManager().resetPlayer(event.getPlayer());
    }
}
