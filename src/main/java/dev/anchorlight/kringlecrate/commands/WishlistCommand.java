package dev.anchorlight.kringlecrate.commands;

import dev.anchorlight.kringlecrate.KringleCrate;
import dev.anchorlight.kringlecrate.utils.ParticipantManager;
import dev.anchorlight.kringlecrate.utils.WishlistManager;
import dev.anchorlight.stonelib.command.SubCommand;
import dev.anchorlight.stonelib.message.MessageService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class WishlistCommand implements SubCommand {

    private final KringleCrate plugin;

    public WishlistCommand(KringleCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "wishlist";
    }

    @Override
    public String getUsage() {
        return "/kc wishlist <view|add|remove> [item]";
    }

    @Override
    public String getDescription() {
        return "View or edit your wishlist.";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        MessageService messages = plugin.getMessages();
        ParticipantManager participantManager = plugin.getParticipantManager();
        WishlistManager wishlistManager = plugin.getWishlistManager();
        UUID playerUUID = player.getUniqueId();

        if (!participantManager.getParticipants().contains(playerUUID.toString())) {
            messages.send(player, "wishlist.not-joined");
            return;
        }

        wishlistManager.initializeWishlist(playerUUID);

        if (args.length == 0 || args[0].equalsIgnoreCase("view")) {
            sendWishlist(player, playerUUID);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                handleAdd(player, playerUUID, args);
                return;
            case "remove":
                handleRemove(player, playerUUID, args);
                return;
            default:
                messages.send(player, "wishlist.usage");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Tab.filter(Arrays.asList("view", "add", "remove"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove") && sender instanceof Player player) {
            return Tab.filter(plugin.getWishlistManager().getWishlist(player.getUniqueId()), args[1]);
        }
        return List.of();
    }

    private void handleAdd(Player player, UUID playerUUID, String[] args) {
        MessageService messages = plugin.getMessages();
        if (args.length < 2) {
            messages.send(player, "wishlist.add-usage");
            return;
        }

        String wish = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        if (wish.isEmpty()) {
            messages.send(player, "wishlist.add-blank");
            return;
        }

        boolean added = plugin.getWishlistManager().addItem(playerUUID, wish);
        if (!added) {
            messages.send(player, "wishlist.add-duplicate");
            return;
        }

        messages.send(player, "wishlist.add-success", wish);
    }

    private void handleRemove(Player player, UUID playerUUID, String[] args) {
        MessageService messages = plugin.getMessages();
        if (args.length < 2) {
            messages.send(player, "wishlist.remove-usage");
            return;
        }

        String wish = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        boolean removed = plugin.getWishlistManager().removeItem(playerUUID, wish);
        if (!removed) {
            messages.send(player, "wishlist.remove-nomatch");
            return;
        }

        messages.send(player, "wishlist.remove-success", wish);
    }

    private void sendWishlist(Player player, UUID playerUUID) {
        MessageService messages = plugin.getMessages();
        List<String> wishlist = plugin.getWishlistManager().getWishlist(playerUUID);
        if (wishlist.isEmpty()) {
            messages.send(player, "wishlist.empty");
            return;
        }

        messages.send(player, "wishlist.header");
        for (String entry : wishlist) {
            messages.send(player, "wishlist.entry", entry);
        }
    }
}
