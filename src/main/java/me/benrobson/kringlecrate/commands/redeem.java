package me.benrobson.kringlecrate.commands;

import me.benrobson.kringlecrate.KringleCrate;
import me.benrobson.kringlecrate.utils.DateUtils;
import me.benrobson.kringlecrate.utils.EconomyManager;
import me.benrobson.kringlecrate.utils.GiftManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class redeem implements CommandExecutor {

    private final KringleCrate plugin;
    private final GiftManager giftManager;

    public redeem(KringleCrate plugin) {
        this.plugin = plugin;
        this.giftManager = plugin.getGiftManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        plugin.getLogger().info("[REDEEM] Command triggered by player UUID: " + playerUUID);

        // Check redemption period
        if (!DateUtils.isInRedemptionPeriod()) {
            String redemptionPeriod = DateUtils.getFormattedRedemptionPeriod();
            player.sendMessage(ChatColor.RED + "Gifts can only be redeemed during the redemption period: "
                    + ChatColor.GOLD + redemptionPeriod);
            plugin.getLogger().info("[REDEEM] Redemption attempt outside period by " + playerUUID + ". Period: " + redemptionPeriod);
            return true;
        }

        List<GiftManager.GiftRecord> giftRecords = giftManager.getGiftRecords(playerUUID);
        if (giftRecords.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You have no valid gifts to redeem.");
            plugin.getLogger().info("[REDEEM] No valid gifts found for player UUID: " + playerUUID);
            return true;
        }

        List<ItemStack> itemGifts = giftRecords.stream()
                .filter(record -> record.getType() == GiftManager.GiftType.ITEM)
                .map(GiftManager.GiftRecord::getItem)
                .collect(Collectors.toList());

        int freeSlots = countFreeInventorySlots(player);
        if (freeSlots < itemGifts.size()) {
            player.sendMessage(ChatColor.RED + "You don't have enough inventory space for all your gifts.");
            plugin.getLogger().info("[REDEEM] Insufficient inventory space for player UUID: " + playerUUID + ". Needed: " + itemGifts.size() + ", Available: " + freeSlots);
            return true;
        }

        double totalCurrency = giftRecords.stream()
                .filter(record -> record.getType() == GiftManager.GiftType.CURRENCY)
                .mapToDouble(GiftManager.GiftRecord::getCurrencyAmount)
                .sum();

        EconomyManager economyManager = plugin.getEconomyManager();
        if (totalCurrency > 0 && (economyManager == null || !economyManager.isEconomyAvailable())) {
            player.sendMessage(ChatColor.RED + "Currency gifts are unavailable right now. Please contact an administrator.");
            plugin.getLogger().severe("[REDEEM] Currency gifts pending but no economy is available for player UUID: " + playerUUID);
            return true;
        }

        addGiftsToInventory(player, itemGifts);

        if (totalCurrency > 0) {
            boolean deposited = economyManager.deposit(player, totalCurrency);
            if (!deposited) {
                player.sendMessage(ChatColor.RED + "An error occurred while redeeming your currency gifts. Please contact an administrator.");
                return true;
            }
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            player.sendMessage(ChatColor.GREEN + "You received currency gifts totalling " + ChatColor.GOLD
                    + formatter.format(totalCurrency) + ChatColor.GREEN + ".");
        }

        giftManager.clearGifts(playerUUID);
        player.sendMessage(ChatColor.GREEN + "All your gifts have been redeemed!");
        plugin.getLogger().info("[REDEEM] Successfully redeemed gifts for player UUID: " + playerUUID);

        return true;
    }

    private int countFreeInventorySlots(Player player) {
        int freeSlots = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) freeSlots++;
        }
        return freeSlots;
    }

    private void addGiftsToInventory(Player player, List<ItemStack> gifts) {
        try {
            for (ItemStack gift : gifts) {
                player.getInventory().addItem(gift);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("[REDEEM] Error adding gifts to inventory for player UUID: " + player.getUniqueId());
            plugin.getLogger().severe(e.getMessage());
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while redeeming your gifts. Please contact an administrator.");
        }
    }
}