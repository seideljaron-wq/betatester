package dev.betasystem;

import dev.betasystem.commands.BetaCommand;
import dev.betasystem.commands.BetaTesterCommand;
import dev.betasystem.commands.BugCommand;
import dev.betasystem.managers.BetaTesterManager;
import dev.betasystem.managers.DiscordLogger;
import dev.betasystem.managers.FeatureStateManager;
import dev.betasystem.gui.BetaGUIListener;
import org.bukkit.plugin.java.JavaPlugin;

public class BetaSystem extends JavaPlugin {

    private static BetaSystem instance;
    private BetaTesterManager testerManager;
    private FeatureStateManager featureManager;
    private DiscordLogger discordLogger;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        testerManager  = new BetaTesterManager(this);
        featureManager = new FeatureStateManager();
        discordLogger  = new DiscordLogger(this);

        BetaTesterCommand testerCmd = new BetaTesterCommand(this);
        getCommand("beta-tester").setExecutor(testerCmd);
        getCommand("beta-tester").setTabCompleter(testerCmd);

        BetaCommand betaCmd = new BetaCommand(this);
        getCommand("beta").setExecutor(betaCmd);

        BugCommand bugCmd = new BugCommand(this);
        getCommand("bug").setExecutor(bugCmd);

        getServer().getPluginManager().registerEvents(new BetaGUIListener(this), this);

        getLogger().info("BetaSystem enabled!");
    }

    @Override
    public void onDisable() {
        // Reset all active features on shutdown
        featureManager.resetAll();
        getLogger().info("BetaSystem disabled.");
    }

    public static BetaSystem getInstance()         { return instance; }
    public BetaTesterManager getTesterManager()    { return testerManager; }
    public FeatureStateManager getFeatureManager() { return featureManager; }
    public DiscordLogger getDiscordLogger()        { return discordLogger; }
}
