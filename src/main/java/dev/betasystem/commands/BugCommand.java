package dev.betasystem.commands;

import dev.betasystem.BetaSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class BugCommand implements CommandExecutor {

    private final BetaSystem plugin;
    public BugCommand(BetaSystem plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is player-only.");
            return true;
        }

        // Must be beta tester or admin
        boolean isTester = plugin.getTesterManager().isTester(player.getUniqueId());
        boolean isAdmin  = player.isOp() || player.hasPermission("beta.admin");

        if (!isTester && !isAdmin) {
            player.sendMessage(Component.text(
                "✗ You are not a Beta Tester.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text(
                "Usage: /bug <description>", NamedTextColor.YELLOW));
            return true;
        }

        String reason = String.join(" ", args);

        // Confirm to player
        player.sendMessage(
            Component.text("✔ Bug report submitted: ", NamedTextColor.GREEN)
                .append(Component.text(reason, NamedTextColor.WHITE))
        );
        player.sendMessage(Component.text(
            "Thank you for helping improve RuneMC!", NamedTextColor.GRAY));

        // Log to Discord bug webhook
        plugin.getDiscordLogger().logBugReport(player.getName(), reason);

        return true;
    }
}
