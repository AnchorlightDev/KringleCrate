package me.benrobson.kringlecrate.commands;

import me.benrobson.kringlecrate.KringleCrate;
import me.benrobson.kringlecrate.utils.DateUtils;
import me.benrobson.kringlecrate.utils.ParticipantManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class join implements CommandExecutor {

    private final KringleCrate plugin;
    private final ParticipantManager participantManager;

    public join(KringleCrate plugin) {
        this.plugin = plugin;
        this.participantManager = plugin.getParticipantManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        // Check if the player is already opted in
        if (participantManager.getParticipants().contains(playerUUID.toString())) {
            player.sendMessage(ChatColor.RED + "You have already joined the Secret Santa event!");
            return true;
        }

        if (DateUtils.isInRedemptionPeriod()) {
            player.sendMessage(ChatColor.RED + "You cannot join the Secret Santa event during the redemption period.");
            plugin.getLogger().info("[JOIN] Join attempt inside redemption period by " + playerUUID + ".");
            return true;
        }

        // Add the player as a participant
        participantManager.addParticipant(playerUUID);
        plugin.getWishlistManager().initializeWishlist(playerUUID);

        // Notify the player
        player.sendMessage(ChatColor.GREEN + "You have successfully joined the Secret Santa event!"
                + ChatColor.AQUA + " Use /kc wishlist add <item> to share your wishes.");
        return true;
    }
}
