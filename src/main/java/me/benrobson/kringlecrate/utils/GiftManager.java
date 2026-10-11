package me.benrobson.kringlecrate.utils;

import me.benrobson.kringlecrate.KringleCrate;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GiftManager {
    private final KringleCrate plugin;

    public GiftManager(KringleCrate plugin) {
        this.plugin = plugin;
    }

    public void saveGiftSubmission(String recipient, String senderName, ItemStack gift) {
        Map<String, Object> giftData = new HashMap<>();
        giftData.put("type", GiftType.ITEM.name());
        giftData.put("item", gift.clone());
        giftData.put("sender", senderName);

        appendGift(UUID.fromString(recipient), giftData);
        plugin.getLogger().info("[KringleCrate] [SAVE] Item gift saved for recipient " + recipient + ": " + giftData);
    }

    public void saveCurrencyGift(String recipient, String senderName, double amount) {
        Map<String, Object> giftData = new HashMap<>();
        giftData.put("type", GiftType.CURRENCY.name());
        giftData.put("amount", amount);
        giftData.put("sender", senderName);

        appendGift(UUID.fromString(recipient), giftData);
        plugin.getLogger().info("[KringleCrate] [SAVE] Currency gift saved for recipient " + recipient + ": " + amount);
    }

    private void appendGift(UUID recipientUUID, Map<String, Object> giftData) {
        String basePath = "gifts." + recipientUUID;

        List<Map<String, Object>> gifts = (List<Map<String, Object>>) plugin.getGiftConfigManager().getConfig().getList(basePath);
        if (gifts == null) {
            gifts = new ArrayList<>();
        }

        gifts.add(giftData);

        plugin.getGiftConfigManager().getConfig().set(basePath, gifts);
        plugin.getGiftConfigManager().saveConfigFile();
    }

    public List<GiftRecord> getGiftRecords(UUID recipientUUID) {
        String basePath = "gifts." + recipientUUID;
        List<Map<String, Object>> giftsData = (List<Map<String, Object>>) plugin.getGiftConfigManager().getConfig().getList(basePath);
        List<GiftRecord> giftRecords = new ArrayList<>();

        if (giftsData == null) {
            return giftRecords;
        }

        for (Map<String, Object> giftData : giftsData) {
            String typeValue = String.valueOf(giftData.getOrDefault("type", GiftType.ITEM.name()));
            GiftType type = GiftType.ITEM;
            try {
                type = GiftType.valueOf(typeValue.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("[KringleCrate] Unknown gift type '" + typeValue + "' for recipient " + recipientUUID);
            }

            String sender = (String) giftData.getOrDefault("sender", "Unknown");

            if (type == GiftType.CURRENCY) {
                Object amountObj = giftData.get("amount");
                if (amountObj instanceof Number number) {
                    giftRecords.add(GiftRecord.currency(number.doubleValue(), sender));
                } else {
                    plugin.getLogger().warning("[KringleCrate] Invalid currency data for recipient " + recipientUUID);
                }
                continue;
            }

            Object serializedItem = giftData.get("item");
            if (serializedItem instanceof ItemStack gift) {
                giftRecords.add(GiftRecord.item(gift.clone(), sender));
            } else {
                plugin.getLogger().warning("[KringleCrate] Unexpected item format for recipient UUID: " + recipientUUID);
            }
        }
        return giftRecords;
    }

    public void clearGifts(UUID recipientUUID) {
        String basePath = "gifts." + recipientUUID;
        plugin.getGiftConfigManager().getConfig().set(basePath, null);
        plugin.getGiftConfigManager().saveConfigFile();
        plugin.getLogger().info("[KringleCrate] [CLEAR] Cleared gifts for recipient: " + recipientUUID);
    }

    public enum GiftType {
        ITEM,
        CURRENCY
    }

    public static class GiftRecord {
        private final GiftType type;
        private final ItemStack item;
        private final double currencyAmount;
        private final String sender;

        private GiftRecord(GiftType type, ItemStack item, double currencyAmount, String sender) {
            this.type = type;
            this.item = item;
            this.currencyAmount = currencyAmount;
            this.sender = sender;
        }

        public static GiftRecord item(ItemStack item, String sender) {
            return new GiftRecord(GiftType.ITEM, item, 0, sender);
        }

        public static GiftRecord currency(double amount, String sender) {
            return new GiftRecord(GiftType.CURRENCY, null, amount, sender);
        }

        public GiftType getType() {
            return type;
        }

        public ItemStack getItem() {
            return item == null ? null : item.clone();
        }

        public double getCurrencyAmount() {
            return currencyAmount;
        }

        public String getSender() {
            return sender;
        }
    }

}
