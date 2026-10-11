package me.benrobson.kringlecrate.commands;

import me.benrobson.kringlecrate.KringleCrate;
import me.benrobson.kringlecrate.utils.DateUtils;
import me.benrobson.kringlecrate.utils.EconomyManager;
import me.benrobson.kringlecrate.utils.FormatterUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.Locale;

public class submit implements CommandExecutor {

    private final KringleCrate plugin;

    public submit(KringleCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();

        if (DateUtils.isBeforeRevealDate()) {
            player.sendMessage(ChatColor.RED + "You cannot submit gifts before the reveal date: "
                    + ChatColor.GOLD + FormatterUtils.getFormattedRevealDate());
            return true;
        }

        if (DateUtils.isInRedemptionPeriod()) {
            player.sendMessage(ChatColor.RED + "You submit any more gifts during as the redemption period has now started.");
            return true;
        }

        plugin.getLogger().info("Submitting gift for player UUID: " + playerUUID);

        String assignedPlayer = plugin.getParticipantManager().getAssignedPlayer(playerUUID);
        if (assignedPlayer == null) {
            player.sendMessage(ChatColor.RED + "You do not have an assigned recipient!");
            plugin.getLogger().info("No assigned recipient found for UUID: " + playerUUID);
            return true;
        }

        plugin.getLogger().info("Assigned recipient for " + playerUUID + ": " + assignedPlayer);

        if (args.length > 0 && args[0].equalsIgnoreCase("currency")) {
            return handleCurrencySubmission(player, assignedPlayer, args);
        }

        // Check if the player is holding an item
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You need to hold an item to submit it as a gift.");
            return true;
        }

        // Get the display name of the item
        String itemDisplayName = itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasDisplayName()
                ? itemInHand.getItemMeta().getDisplayName()
                : itemInHand.getType().name().toLowerCase().replace('_', ' ');

        plugin.getGiftManager().saveGiftSubmission(assignedPlayer, player.getName(), itemInHand);

        player.getInventory().setItemInMainHand(null);

        player.sendMessage(ChatColor.GREEN + "Your gift has been submitted to your recipient: " + ChatColor.AQUA + itemDisplayName + ChatColor.GREEN + "!");
        plugin.getLogger().info("Gift successfully submitted for recipient UUID: " + assignedPlayer);
        return true;
    }

    private boolean handleCurrencySubmission(Player player, String assignedPlayer, String[] args) {
        EconomyManager economyManager = plugin.getEconomyManager();
        if (economyManager == null || !economyManager.isEconomyAvailable()) {
            player.sendMessage(ChatColor.RED + "Currency gifts are currently unavailable. Please contact an administrator.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /kc submit currency <amount>");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount. Please enter a valid number.");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "The amount must be greater than zero.");
            return true;
        }

        if (!economyManager.getEconomy().has(player, amount)) {
            player.sendMessage(ChatColor.RED + "You do not have enough funds to send that amount.");
            return true;
        }

        if (!economyManager.withdraw(player, amount)) {
            player.sendMessage(ChatColor.RED + "An error occurred while withdrawing your funds. Please try again later.");
            return true;
        }

        plugin.getGiftManager().saveCurrencyGift(assignedPlayer, player.getName(), amount);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        player.sendMessage(ChatColor.GREEN + "You have gifted " + ChatColor.GOLD + formatter.format(amount)
                + ChatColor.GREEN + " to your recipient!");
        plugin.getLogger().info("Currency gift submitted for recipient UUID: " + assignedPlayer + " amount: " + amount);
        return true;
    }
}
