package dev.betasystem.managers;

import dev.betasystem.BetaSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BetaTesterManager {

    private final BetaSystem plugin;
    private final List<UUID> testers = new ArrayList<>();

    public BetaTesterManager(BetaSystem plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        testers.clear();
        for (String s : plugin.getConfig().getStringList("beta-testers")) {
            try { testers.add(UUID.fromString(s)); }
            catch (IllegalArgumentException ignored) {}
        }
    }

    public boolean isTester(UUID uuid) { return testers.contains(uuid); }
    public List<UUID> getTesters()     { return testers; }

    public boolean addTester(UUID uuid) {
        if (testers.contains(uuid)) return false;
        testers.add(uuid); save(); return true;
    }

    public boolean removeTester(UUID uuid) {
        boolean r = testers.remove(uuid);
        if (r) save();
        return r;
    }

    private void save() {
        List<String> list = new ArrayList<>();
        testers.forEach(u -> list.add(u.toString()));
        plugin.getConfig().set("beta-testers", list);
        plugin.saveConfig();
    }
}
