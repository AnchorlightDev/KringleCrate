package dev.anchorlight.kringlecrate.commands;

import dev.anchorlight.kringlecrate.KringleCrate;
import dev.anchorlight.kringlecrate.utils.DateUtils;
import dev.anchorlight.kringlecrate.utils.EconomyManager;
import dev.anchorlight.kringlecrate.utils.GiftManager;
import dev.anchorlight.stonelib.command.SubCommand;
import dev.anchorlight.stonelib.message.MessageService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RedeemCommand implements SubCommand {

    private final KringleCrate plugin;

    public RedeemCommand(KringleCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "redeem";
    }

    @Override
    public String getPermission() {
        return "kringlecrate.redeem";
    }

    @Override
    public String getDescription() {
        return "Redeem the gifts waiting for you during the redemption period.";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        MessageService messages = plugin.getMessages();
        GiftManager giftManager = plugin.getGiftManager();
        UUID playerUUID = player.getUniqueId();

        plugin.getLogger().info("[REDEEM] Command triggered by player UUID: " + playerUUID);

        if (!DateUtils.isInRedemptionPeriod()) {
            String redemptionPeriod = DateUtils.getFormattedRedemptionPeriod();
            messages.send(player, "redeem.outside-period", redemptionPeriod);
            plugin.getLogger().info("[REDEEM] Redemption attempt outside period by " + playerUUID + ". Period: " + redemptionPeriod);
            return;
        }

        List<GiftManager.GiftRecord> giftRecords = giftManager.getGiftRecords(playerUUID);
        if (giftRecords.isEmpty()) {
            messages.send(player, "redeem.no-gifts");
            plugin.getLogger().info("[REDEEM] No valid gifts found for player UUID: " + playerUUID);
            return;
        }

        List<ItemStack> itemGifts = giftRecords.stream()
                .filter(record -> record.getType() == GiftManager.GiftType.ITEM)
                .map(GiftManager.GiftRecord::getItem)
                .collect(Collectors.toList());

        int freeSlots = countFreeInventorySlots(player);
        if (freeSlots < itemGifts.size()) {
            messages.send(player, "redeem.not-enough-space");
            plugin.getLogger().info("[REDEEM] Insufficient inventory space for player UUID: " + playerUUID + ". Needed: " + itemGifts.size() + ", Available: " + freeSlots);
            return;
        }

        double totalCurrency = giftRecords.stream()
                .filter(record -> record.getType() == GiftManager.GiftType.CURRENCY)
                .mapToDouble(GiftManager.GiftRecord::getCurrencyAmount)
                .sum();

        EconomyManager economyManager = plugin.getEconomyManager();
        if (totalCurrency > 0 && (economyManager == null || !economyManager.isEconomyAvailable())) {
            messages.send(player, "redeem.currency-unavailable");
            plugin.getLogger().severe("[REDEEM] Currency gifts pending but no economy is available for player UUID: " + playerUUID);
            return;
        }

        addGiftsToInventory(player, itemGifts);

        if (totalCurrency > 0) {
            boolean deposited = economyManager.deposit(player, totalCurrency);
            if (!deposited) {
                messages.send(player, "redeem.currency-error");
                return;
            }
            NumberFormat formatter = NumberFormat.getCurrencyInstance();
            messages.send(player, "redeem.currency-received", formatter.format(totalCurrency));
        }

        giftManager.clearGifts(playerUUID);
        messages.send(player, "redeem.success");
        plugin.getLogger().info("[REDEEM] Successfully redeemed gifts for player UUID: " + playerUUID);
    }

    private int countFreeInventorySlots(Player player) {
        int freeSlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
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
            plugin.getMessages().send(player, "redeem.item-error");
        }
    }
}
