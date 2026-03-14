package dev.betasystem.commands;

import dev.betasystem.BetaSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BetaTesterCommand implements CommandExecutor, TabCompleter {

    private final BetaSystem plugin;
    public BetaTesterCommand(BetaSystem plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permission: beta.admin or OP
        boolean isConsole = !(sender instanceof Player);
        boolean allowed   = isConsole
            || ((Player) sender).isOp()
            || sender.hasPermission("beta.admin");

        if (!allowed) {
            sender.sendMessage(Component.text("✗ No permission.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 1) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "add"    -> handleAdd(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list"   -> handleList(sender);
            default       -> sendHelp(sender);
        }
        return true;
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /beta-tester add <player>", NamedTextColor.YELLOW));
            return;
        }
        OfflinePlayer target = resolve(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("✗ Player '" + args[1] + "' not found.", NamedTextColor.RED));
            return;
        }
        String name = target.getName() != null ? target.getName() : args[1];
        if (!plugin.getTesterManager().addTester(target.getUniqueId())) {
            sender.sendMessage(Component.text("⚠ " + name + " is already a Beta Tester.", NamedTextColor.YELLOW));
            return;
        }
        // Grant permission node if online
        Player online = Bukkit.getPlayer(target.getUniqueId());
        sender.sendMessage(Component.text("✔ ", NamedTextColor.GREEN)
            .append(Component.text(name, NamedTextColor.LIGHT_PURPLE))
            .append(Component.text(" is now a Beta Tester!", NamedTextColor.GREEN)));
        if (online != null) {
            online.sendMessage(Component.text("✔ You are now a ", NamedTextColor.GREEN)
                .append(Component.text("Beta Tester", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("! Use /beta to access features.", NamedTextColor.GREEN)));
        }
        String executor = sender instanceof Player p ? p.getName() : "Console";
        plugin.getDiscordLogger().logTesterAdded(executor, name);
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /beta-tester remove <player>", NamedTextColor.YELLOW));
            return;
        }
        OfflinePlayer target = resolve(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("✗ Player '" + args[1] + "' not found.", NamedTextColor.RED));
            return;
        }
        String name = target.getName() != null ? target.getName() : args[1];
        if (!plugin.getTesterManager().removeTester(target.getUniqueId())) {
            sender.sendMessage(Component.text("⚠ " + name + " is not a Beta Tester.", NamedTextColor.YELLOW));
            return;
        }
        // Reset their features if online
        Player online = Bukkit.getPlayer(target.getUniqueId());
        if (online != null) {
            plugin.getFeatureManager().resetPlayer(online);
            online.sendMessage(Component.text("✗ Your Beta Tester access was revoked.", NamedTextColor.RED));
        }
        sender.sendMessage(Component.text("✔ ", NamedTextColor.GREEN)
            .append(Component.text(name, NamedTextColor.LIGHT_PURPLE))
            .append(Component.text(" removed from Beta Testers.", NamedTextColor.GREEN)));
        String executor = sender instanceof Player p ? p.getName() : "Console";
        plugin.getDiscordLogger().logTesterRemoved(executor, name);
    }

    private void handleList(CommandSender sender) {
        List<UUID> testers = plugin.getTesterManager().getTesters();
        if (testers.isEmpty()) {
            sender.sendMessage(Component.text("⚠ No Beta Testers registered.", NamedTextColor.YELLOW));
            return;
        }
        sender.sendMessage(Component.text("── Beta Testers (" + testers.size() + ") ──", NamedTextColor.GOLD));
        int i = 1;
        for (UUID uuid : testers) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
            String name     = p.getName() != null ? p.getName() : uuid.toString();
            boolean online  = p.isOnline();
            sender.sendMessage(
                Component.text("  " + i++ + ". ", NamedTextColor.GRAY)
                    .append(Component.text(name, online ? NamedTextColor.GREEN : NamedTextColor.WHITE))
                    .append(Component.text(online ? " [ONLINE]" : " [OFFLINE]",
                        online ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY))
            );
        }
    }

    private OfflinePlayer resolve(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online;
        @SuppressWarnings("deprecation")
        OfflinePlayer op = Bukkit.getOfflinePlayer(name);
        return op.hasPlayedBefore() ? op : null;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("── /beta-tester ──", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("  add <player>    ", NamedTextColor.AQUA).append(Component.text("Add a beta tester", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  remove <player> ", NamedTextColor.AQUA).append(Component.text("Remove a beta tester", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  list            ", NamedTextColor.AQUA).append(Component.text("List all beta testers", NamedTextColor.GRAY)));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return List.of("add", "remove", "list");
        if (args.length == 2) {
            List<String> names = new ArrayList<>();
            String pref = args[1].toLowerCase();
            Bukkit.getOnlinePlayers().forEach(p -> { if (p.getName().toLowerCase().startsWith(pref)) names.add(p.getName()); });
            return names;
        }
        return List.of();
    }
}
