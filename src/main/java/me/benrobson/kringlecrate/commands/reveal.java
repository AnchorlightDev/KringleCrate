package me.benrobson.kringlecrate.commands;

import me.benrobson.kringlecrate.KringleCrate;
import me.benrobson.kringlecrate.utils.DateUtils;
import me.benrobson.kringlecrate.utils.FormatterUtils;
import me.benrobson.kringlecrate.utils.ParticipantManager;
import me.benrobson.kringlecrate.utils.WishlistManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class reveal implements CommandExecutor {

    private final KringleCrate plugin;
    private final WishlistManager wishlistManager;

    public reveal(KringleCrate plugin) {
        this.plugin = plugin;
        this.wishlistManager = plugin.getWishlistManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        // Verify the reveal date or override permission
        if (!DateUtils.isRevealDay() && !player.hasPermission("kringlecrate.override")) {
            player.sendMessage(ChatColor.RED + "You cannot reveal your recipient until the reveal day: "
                    + ChatColor.GOLD + FormatterUtils.getFormattedRevealDate());
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("wishlist")) {
            showRecipientWishlist(player);
            return true;
        }

        if (args.length > 0) {
            player.sendMessage(ChatColor.RED + "Usage: /kc reveal [wishlist]");
            return true;
        }

        revealRecipient(player);

        return true;
    }

    private void revealRecipient(Player player) {
        // Run the recipient lookup asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ParticipantManager participantManager = plugin.getParticipantManager();

            // Validate if the player is part of the Secret Santa event
            List<String> participants = participantManager.getParticipants();
            if (!participants.contains(player.getUniqueId().toString())) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(ChatColor.RED + "You are not part of the Secret Santa event. Use /kc join.")
                );
                return;
            }

            // Ensure enough participants for assignments
            if (participants.size() <= 1) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(ChatColor.RED + "Not enough participants to assign a recipient!")
                );
                return;
            }

            // Assign recipients if not done already
            if (!plugin.getGiftConfigManager().getConfig().contains("assignments")) {
                participantManager.assignParticipants();
            }

            // Retrieve the recipient for the player
            String recipientUUID = plugin.getGiftConfigManager()
                    .getConfig()
                    .getString("assignments." + player.getUniqueId().toString());

            if (recipientUUID == null) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(ChatColor.RED + "No recipient assigned. Please contact an administrator.")
                );
                return;
            }

            // Fetch the recipient's name
            UUID recipientId = UUID.fromString(recipientUUID);
            OfflinePlayer recipientPlayer = Bukkit.getOfflinePlayer(recipientId);

            // Notify the player of their assigned recipient
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage(ChatColor.GREEN + "Your assigned recipient is: "
                            + ChatColor.GOLD + (recipientPlayer.getName() != null
                            ? recipientPlayer.getName()
                            : "Unknown Player")));
        });
    }

    private void showRecipientWishlist(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ParticipantManager participantManager = plugin.getParticipantManager();
            List<String> participants = participantManager.getParticipants();
            if (!participants.contains(player.getUniqueId().toString())) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(ChatColor.RED + "You are not part of the Secret Santa event. Use /kc join.")
                );
                return;
            }

            String recipientUUID = participantManager.getAssignedPlayer(player.getUniqueId().toString());

            if (recipientUUID == null) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(ChatColor.RED + "You must reveal your recipient before viewing their wishlist.")
                );
                return;
            }

            UUID recipientId = UUID.fromString(recipientUUID);
            OfflinePlayer recipientPlayer = Bukkit.getOfflinePlayer(recipientId);
            List<String> wishlist = wishlistManager.getWishlist(recipientId);

            Bukkit.getScheduler().runTask(plugin, () -> {
                String recipientName = recipientPlayer.getName() != null ? recipientPlayer.getName() : "Unknown Player";
                if (wishlist.isEmpty()) {
                    player.sendMessage(ChatColor.AQUA + recipientName + " has not added any wishlist items yet.");
                    return;
                }

                player.sendMessage(ChatColor.GREEN + "Wishlist for " + ChatColor.GOLD + recipientName + ChatColor.GREEN + ":");
                for (String entry : wishlist) {
                    player.sendMessage(ChatColor.GOLD + " - " + ChatColor.WHITE + entry);
                }
            });
        });
    }
}
