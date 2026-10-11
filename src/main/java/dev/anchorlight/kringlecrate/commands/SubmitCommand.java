package dev.anchorlight.kringlecrate.commands;

import dev.anchorlight.kringlecrate.KringleCrate;
import dev.anchorlight.kringlecrate.utils.DateUtils;
import dev.anchorlight.kringlecrate.utils.EconomyManager;
import dev.anchorlight.kringlecrate.utils.FormatterUtils;
import dev.anchorlight.stonelib.command.SubCommand;
import dev.anchorlight.stonelib.message.MessageService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SubmitCommand implements SubCommand {

    private final KringleCrate plugin;

    public SubmitCommand(KringleCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "submit";
    }

    @Override
    public String getUsage() {
        return "/kc submit [currency <amount>]";
    }

    @Override
    public String getDescription() {
        return "Submit the item in your hand, or currency, as a gift.";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        MessageService messages = plugin.getMessages();
        String playerUUID = player.getUniqueId().toString();

        if (DateUtils.isBeforeRevealDate()) {
            messages.send(player, "submit.before-reveal", FormatterUtils.getFormattedRevealDate());
            return;
        }

        if (DateUtils.isInRedemptionPeriod()) {
            messages.send(player, "submit.redemption-started");
            return;
        }

        plugin.getLogger().info("Submitting gift for player UUID: " + playerUUID);

        String assignedPlayer = plugin.getParticipantManager().getAssignedPlayer(playerUUID);
        if (assignedPlayer == null) {
            messages.send(player, "submit.no-recipient");
            plugin.getLogger().info("No assigned recipient found for UUID: " + playerUUID);
            return;
        }

        plugin.getLogger().info("Assigned recipient for " + playerUUID + ": " + assignedPlayer);

        if (args.length > 0 && args[0].equalsIgnoreCase("currency")) {
            handleCurrencySubmission(player, assignedPlayer, args);
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType().isAir()) {
            messages.send(player, "submit.need-item");
            return;
        }

        String itemDisplayName = itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasDisplayName()
                ? itemInHand.getItemMeta().getDisplayName()
                : itemInHand.getType().name().toLowerCase().replace('_', ' ');

        plugin.getGiftManager().saveGiftSubmission(assignedPlayer, player.getName(), itemInHand);

        player.getInventory().setItemInMainHand(null);

        messages.send(player, "submit.item-success", itemDisplayName);
        plugin.getLogger().info("Gift successfully submitted for recipient UUID: " + assignedPlayer);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Tab.filter(List.of("currency"), args[0]);
        }
        return List.of();
    }

    private void handleCurrencySubmission(Player player, String assignedPlayer, String[] args) {
        MessageService messages = plugin.getMessages();
        EconomyManager economyManager = plugin.getEconomyManager();
        if (economyManager == null || !economyManager.isEconomyAvailable()) {
            messages.send(player, "submit.currency-unavailable");
            return;
        }

        if (args.length < 2) {
            messages.send(player, "submit.currency-usage");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            messages.send(player, "submit.currency-invalid-amount");
            return;
        }

        if (amount <= 0) {
            messages.send(player, "submit.currency-nonpositive");
            return;
        }

        if (!economyManager.getEconomy().has(player, amount)) {
            messages.send(player, "submit.currency-insufficient");
            return;
        }

        if (!economyManager.withdraw(player, amount)) {
            messages.send(player, "submit.currency-withdraw-error");
            return;
        }

        plugin.getGiftManager().saveCurrencyGift(assignedPlayer, player.getName(), amount);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        messages.send(player, "submit.currency-success", formatter.format(amount));
        plugin.getLogger().info("Currency gift submitted for recipient UUID: " + assignedPlayer + " amount: " + amount);
    }
}
