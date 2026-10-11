package dev.anchorlight.kringlecrate.utils;

import dev.anchorlight.kringlecrate.KringleCrate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class GiftConfigManager {
    private final KringleCrate plugin;
    private final File configFile;
    private final FileConfiguration config;

    public GiftConfigManager(KringleCrate plugin) {
        this.plugin = plugin;

        // Initialize config.yml
        configFile = new File(plugin.getDataFolder(), "gifts.yml");

        if (!configFile.exists()) {
            plugin.saveResource("gifts.yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfigFile() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save gifts.yml!");
        }
    }
}