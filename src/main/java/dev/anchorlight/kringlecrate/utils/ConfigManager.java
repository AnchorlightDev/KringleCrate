package dev.anchorlight.kringlecrate.utils;

import dev.anchorlight.kringlecrate.KringleCrate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final KringleCrate plugin;

    public ConfigManager(KringleCrate plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public void saveConfigFile() {
        plugin.saveConfig();
    }
}