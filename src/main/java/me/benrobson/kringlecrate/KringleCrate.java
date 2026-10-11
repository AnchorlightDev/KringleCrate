package me.benrobson.kringlecrate;

import me.benrobson.kringlecrate.utils.*;
import org.bukkit.plugin.java.JavaPlugin;

public class KringleCrate extends JavaPlugin {

    private static ConfigManager configManager;
    private GiftConfigManager giftConfigManager;
    private static GiftManager giftManager;
    private static ParticipantManager participantManager;
    private static WishlistManager wishlistManager;
    private static EconomyManager economyManager;

    private static FormatterUtils formatterUtils;
    private static DateUtils dateUtils;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        giftConfigManager = new GiftConfigManager(this);
        giftManager = new GiftManager(this);
        participantManager = new ParticipantManager(this);
        wishlistManager = new WishlistManager(this);
        economyManager = new EconomyManager(this);
        formatterUtils = new FormatterUtils(this);
        dateUtils = new DateUtils(this);

        if (!economyManager.setupEconomy()) {
            getLogger().warning("[KringleCrate] Vault economy not detected. Currency gifts will be disabled.");
        }

        // Register command and tab completer
        getCommand("kc").setExecutor(new CommandManager(this));
        getCommand("kc").setTabCompleter(new CommandCompleteManager(this));
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public GiftConfigManager getGiftConfigManager() {
        return giftConfigManager;
    }

    public static GiftManager getGiftManager() {
        return giftManager;
    }

    public ParticipantManager getParticipantManager() {
        return participantManager;
    }

    public WishlistManager getWishlistManager() {
        return wishlistManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
}
