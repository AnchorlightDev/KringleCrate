package me.benrobson.kringlecrate.utils;

import me.benrobson.kringlecrate.KringleCrate;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

public class EconomyManager {

    private final KringleCrate plugin;
    private Economy economy;

    public EconomyManager(KringleCrate plugin) {
        this.plugin = plugin;
    }

    public boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean isEconomyAvailable() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (!isEconomyAvailable()) {
            return false;
        }

        EconomyResponse response = economy.withdrawPlayer(player, amount);
        if (!response.transactionSuccess()) {
            plugin.getLogger().log(Level.SEVERE,
                    "[KringleCrate] Failed to withdraw currency: {0}", response.errorMessage);
            return false;
        }
        return true;
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        if (!isEconomyAvailable()) {
            return false;
        }

        EconomyResponse response = economy.depositPlayer(player, amount);
        if (!response.transactionSuccess()) {
            plugin.getLogger().log(Level.SEVERE,
                    "[KringleCrate] Failed to deposit currency: {0}", response.errorMessage);
            return false;
        }
        return true;
    }
}
