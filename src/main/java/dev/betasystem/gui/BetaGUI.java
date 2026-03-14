package dev.betasystem.gui;

import dev.betasystem.BetaSystem;
import dev.betasystem.managers.FeatureStateManager.Feature;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class BetaGUI {

    public static final String TITLE = "✦ Beta Features ✦";

    // 4 rows x 9 cols = 36 slots
    // Feature slots (centered in row 2, slots 10-16):
    //   Slot 10 = Spectator
    //   Slot 12 = Vanish
    //   Slot 13 = Speed (center)
    //   Slot 14 = Night Vision
    //   Slot 16 = No Fall
    // Everything else = gray glass border/filler

    private static final int[] FEATURE_SLOTS = {10, 12, 13, 14, 16};
    private static final Feature[] FEATURE_ORDER = {
        Feature.SPECTATOR,
        Feature.VANISH,
        Feature.SPEED,
        Feature.NIGHT_VIS,
        Feature.NO_FALL
    };

    // Materials for each feature
    private static final Material[] FEATURE_MATERIALS = {
        Material.ENDER_EYE,          // Spectator
        Material.GRAY_DYE,           // Vanish
        Material.SUGAR,              // Speed
        Material.GOLDEN_CARROT,      // Night Vision
        Material.FEATHER             // No Fall
    };

    private final BetaSystem plugin;

    public BetaGUI(BetaSystem plugin) { this.plugin = plugin; }

    public Inventory build(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36,
            Component.text(TITLE, NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD));

        // Fill all with gray glass
        ItemStack filler = filler();
        for (int i = 0; i < 36; i++) inv.setItem(i, filler);

        // Place feature items
        for (int i = 0; i < FEATURE_SLOTS.length; i++) {
            boolean active = plugin.getFeatureManager().isActive(player.getUniqueId(), FEATURE_ORDER[i]);
            inv.setItem(FEATURE_SLOTS[i], buildFeatureItem(FEATURE_ORDER[i], FEATURE_MATERIALS[i], active));
        }

        return inv;
    }

    public static ItemStack buildFeatureItem(Feature feature, Material mat, boolean active) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta  = item.getItemMeta();

        // Display name: icon + feature name
        meta.displayName(
            Component.text(feature.icon + " " + feature.displayName,
                active ? NamedTextColor.GREEN : NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .decorate(TextDecoration.BOLD)
        );

        // Lore: status line
        meta.lore(List.of(
            Component.empty(),
            Component.text("Status: ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(
                    active ? "ACTIVATED" : "DEACTIVATED",
                    active ? NamedTextColor.GREEN : NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false)
                    .decorate(TextDecoration.BOLD)),
            Component.empty(),
            Component.text("Click to " + (active ? "deactivate" : "activate") + ".",
                NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)
        ));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack filler() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta  = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    public static int[] getFeatureSlots()    { return FEATURE_SLOTS; }
    public static Feature[] getFeatureOrder() { return FEATURE_ORDER; }
    public static Material[] getFeatureMats() { return FEATURE_MATERIALS; }
}
