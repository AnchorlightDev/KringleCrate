package dev.anchorlight.kringlecrate.commands;

import dev.anchorlight.kringlecrate.KringleCrate;
import dev.anchorlight.kringlecrate.utils.DateUtils;
import dev.anchorlight.kringlecrate.utils.FormatterUtils;
import dev.anchorlight.kringlecrate.utils.ParticipantManager;
import dev.anchorlight.kringlecrate.utils.WishlistManager;
import dev.anchorlight.stonelib.command.SubCommand;
import dev.anchorlight.stonelib.message.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class RevealCommand implements SubCommand {

    private final KringleCrate plugin;

    public RevealCommand(KringleCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reveal";
    }

    @Override
    public String getUsage() {
        return "/kc reveal [wishlist]";
    }

    @Override
    public String getDescription() {
        return "Reveal your assigned recipient, or view their wishlist.";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        MessageService messages = plugin.getMessages();

        if (!DateUtils.isRevealDay() && !player.hasPermission("kringlecrate.override")) {
            messages.send(player, "reveal.not-reveal-day", FormatterUtils.getFormattedRevealDate());
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("wishlist")) {
            showRecipientWishlist(player);
            return;
        }

        if (args.length > 0) {
            messages.send(player, "reveal.usage");
            return;
        }

        revealRecipient(player);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Tab.filter(List.of("wishlist"), args[0]);
        }
        return List.of();
    }

    private void revealRecipient(Player player) {
        MessageService messages = plugin.getMessages();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ParticipantManager participantManager = plugin.getParticipantManager();

            List<String> participants = participantManager.getParticipants();
            if (!participants.contains(player.getUniqueId().toString())) {
                Bukkit.getScheduler().runTask(plugin, () -> messages.send(player, "reveal.not-participant"));
                return;
            }

            if (participants.size() <= 1) {
                Bukkit.getScheduler().runTask(plugin, () -> messages.send(player, "reveal.not-enough-participants"));
                return;
            }

            if (!plugin.getGiftConfigManager().getConfig().contains("assignments")) {
                participantManager.assignParticipants();
            }

            String recipientUUID = plugin.getGiftConfigManager()
                    .getConfig()
                    .getString("assignments." + player.getUniqueId());

            if (recipientUUID == null) {
                Bukkit.getScheduler().runTask(plugin, () -> messages.send(player, "reveal.no-recipient"));
                return;
            }

            UUID recipientId = UUID.fromString(recipientUUID);
            OfflinePlayer recipientPlayer = Bukkit.getOfflinePlayer(recipientId);
            String recipientName = recipientPlayer.getName() != null ? recipientPlayer.getName() : "Unknown Player";

            Bukkit.getScheduler().runTask(plugin, () -> messages.send(player, "reveal.recipient", recipientName));
        });
    }

    private void showRecipientWishlist(Player player) {
        MessageService messages = plugin.getMessages();
        WishlistManager wishlistManager = plugin.getWishlistManager();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ParticipantManager participantManager = plugin.getParticipantManager();
            List<String> participants = participantManager.getParticipants();
            if (!participants.contains(player.getUniqueId().toString())) {
                Bukkit.getScheduler().runTask(plugin, () -> messages.send(player, "reveal.not-participant"));
                return;
            }

            String recipientUUID = participantManager.getAssignedPlayer(player.getUniqueId().toString());
            if (recipientUUID == null) {
                Bukkit.getScheduler().runTask(plugin, () -> messages.send(player, "reveal.wishlist-need-reveal"));
                return;
            }

            UUID recipientId = UUID.fromString(recipientUUID);
            OfflinePlayer recipientPlayer = Bukkit.getOfflinePlayer(recipientId);
            List<String> wishlist = wishlistManager.getWishlist(recipientId);

            Bukkit.getScheduler().runTask(plugin, () -> {
                String recipientName = recipientPlayer.getName() != null ? recipientPlayer.getName() : "Unknown Player";
                if (wishlist.isEmpty()) {
                    messages.send(player, "reveal.wishlist-empty", recipientName);
                    return;
                }

                messages.send(player, "reveal.wishlist-header", recipientName);
                for (String entry : wishlist) {
                    messages.send(player, "reveal.wishlist-entry", entry);
                }
            });
        });
    }
}
