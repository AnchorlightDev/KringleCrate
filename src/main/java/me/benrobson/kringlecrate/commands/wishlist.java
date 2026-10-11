package me.benrobson.kringlecrate.commands;

import me.benrobson.kringlecrate.KringleCrate;
import me.benrobson.kringlecrate.utils.ParticipantManager;
import me.benrobson.kringlecrate.utils.WishlistManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class wishlist implements CommandExecutor {

    private final KringleCrate plugin;
    private final ParticipantManager participantManager;
    private final WishlistManager wishlistManager;

    public wishlist(KringleCrate plugin) {
        this.plugin = plugin;
        this.participantManager = plugin.getParticipantManager();
        this.wishlistManager = plugin.getWishlistManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        UUID playerUUID = player.getUniqueId();
        if (!participantManager.getParticipants().contains(playerUUID.toString())) {
            player.sendMessage(ChatColor.RED + "You must join the Secret Santa event before using a wishlist. Use /kc join.");
            return true;
        }

        wishlistManager.initializeWishlist(playerUUID);

        if (args.length == 0 || args[0].equalsIgnoreCase("view")) {
            sendWishlist(player, playerUUID);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "add":
                return handleAdd(player, playerUUID, args);
            case "remove":
                return handleRemove(player, playerUUID, args);
            default:
                player.sendMessage(ChatColor.RED + "Usage: /kc wishlist <view|add|remove> [item]");
                return true;
        }
    }

    private boolean handleAdd(Player player, UUID playerUUID, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /kc wishlist add <item description>");
            return true;
        }

        String wish = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        if (wish.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Wishlist entry cannot be blank.");
            return true;
        }

        boolean added = wishlistManager.addItem(playerUUID, wish);
        if (!added) {
            player.sendMessage(ChatColor.YELLOW + "That wishlist entry already exists or is invalid.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Added to your wishlist: " + ChatColor.GOLD + wish);
        return true;
    }

    private boolean handleRemove(Player player, UUID playerUUID, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /kc wishlist remove <item description>");
            return true;
        }

        String wish = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        boolean removed = wishlistManager.removeItem(playerUUID, wish);
        if (!removed) {
            player.sendMessage(ChatColor.YELLOW + "No wishlist entry matched that description.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Removed from your wishlist: " + ChatColor.GOLD + wish);
        return true;
    }

    private void sendWishlist(Player player, UUID playerUUID) {
        List<String> wishlist = wishlistManager.getWishlist(playerUUID);
        if (wishlist.isEmpty()) {
            player.sendMessage(ChatColor.AQUA + "Your wishlist is currently empty. Add items with /kc wishlist add <item>.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Your wishlist includes:");
        for (String entry : wishlist) {
            player.sendMessage(ChatColor.GOLD + " - " + ChatColor.WHITE + entry);
        }
    }
}
