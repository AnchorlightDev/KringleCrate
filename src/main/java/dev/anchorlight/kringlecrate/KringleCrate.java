package dev.anchorlight.kringlecrate;

import dev.anchorlight.kringlecrate.commands.JoinCommand;
import dev.anchorlight.kringlecrate.commands.KcCommand;
import dev.anchorlight.kringlecrate.commands.RedeemCommand;
import dev.anchorlight.kringlecrate.commands.RevealCommand;
import dev.anchorlight.kringlecrate.commands.SubmitCommand;
import dev.anchorlight.kringlecrate.commands.WishlistCommand;
import dev.anchorlight.kringlecrate.utils.*;
import dev.anchorlight.stonelib.command.CommandRouter;
import dev.anchorlight.stonelib.command.SubCommand;
import dev.anchorlight.stonelib.message.MessageService;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class KringleCrate extends JavaPlugin {

    private static ConfigManager configManager;
    private GiftConfigManager giftConfigManager;
    private static GiftManager giftManager;
    private static ParticipantManager participantManager;
    private static WishlistManager wishlistManager;
    private static EconomyManager economyManager;

    private static FormatterUtils formatterUtils;
    private static DateUtils dateUtils;

    private MessageService messages;

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

        // StoneLib: MiniMessage-backed messages.yml (copied/merged from the jar on first load).
        messages = new MessageService(this, "messages.yml");

        if (!economyManager.setupEconomy()) {
            getLogger().warning("[KringleCrate] Vault economy not detected. Currency gifts will be disabled.");
        }

        registerCommands();
    }

    private void registerCommands() {
        // StoneLib command router: sub-command dispatch, permission + player-only checks, tab
        // completion. The router's own replies are routed back through messages.yml.
        CommandRouter router = new CommandRouter(this, "kc");
        router.feedback(new CommandRouter.Feedback() {
            @Override
            public void usage(CommandSender sender, String rootLabel, Collection<String> visibleNames) {
                messages.send(sender, "usage", String.join("|", visibleNames));
            }

            @Override
            public void unknown(CommandSender sender, String rootLabel, String input) {
                messages.send(sender, "unknown-subcommand", input);
            }

            @Override
            public void noPermission(CommandSender sender, SubCommand sub) {
                messages.send(sender, "no-permission");
            }

            @Override
            public void playersOnly(CommandSender sender, SubCommand sub) {
                messages.send(sender, "players-only");
            }
        });

        router.register(new JoinCommand(this));
        router.register(new RevealCommand(this));
        router.register(new SubmitCommand(this));
        router.register(new RedeemCommand(this));
        router.register(new WishlistCommand(this));

        KcCommand executor = new KcCommand(router);
        PluginCommand kc = getCommand("kc");
        if (kc == null) {
            getLogger().severe("[KringleCrate] Command 'kc' is missing from plugin.yml; commands will not work.");
            return;
        }
        kc.setExecutor(executor);
        kc.setTabCompleter(executor);
    }

    public MessageService getMessages() {
        return messages;
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
