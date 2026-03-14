package dev.betasystem.commands;

import dev.betasystem.BetaSystem;
import dev.betasystem.gui.BetaGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;

public class BetaCommand implements CommandExecutor {

    private final BetaSystem plugin;
    public BetaCommand(BetaSystem plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is player-only.");
            return true;
        }

        // Must be a registered beta tester (or OP / beta.admin)
        boolean isTester = plugin.getTesterManager().isTester(player.getUniqueId());
        boolean isAdmin  = player.isOp() || player.hasPermission("beta.admin");

        if (!isTester && !isAdmin) {
            player.sendMessage(Component.text(
                "✗ You are not a Beta Tester.", NamedTextColor.RED));
            return true;
        }

        BetaGUI gui = new BetaGUI(plugin);
        player.openInventory(gui.build(player));
        return true;
    }
}
