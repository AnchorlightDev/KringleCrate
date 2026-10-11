package me.benrobson.kringlecrate.utils;

import me.benrobson.kringlecrate.KringleCrate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class WishlistManager {

    private final KringleCrate plugin;

    public WishlistManager(KringleCrate plugin) {
        this.plugin = plugin;
    }

    public void initializeWishlist(UUID playerUUID) {
        String path = getWishlistPath(playerUUID);
        if (!plugin.getGiftConfigManager().getConfig().contains(path)) {
            plugin.getGiftConfigManager().getConfig().set(path, new ArrayList<String>());
            plugin.getGiftConfigManager().saveConfigFile();
        }
    }

    public List<String> getWishlist(UUID playerUUID) {
        List<String> wishlist = plugin.getGiftConfigManager().getConfig().getStringList(getWishlistPath(playerUUID));
        return Collections.unmodifiableList(new ArrayList<>(wishlist));
    }

    public boolean addItem(UUID playerUUID, String wish) {
        List<String> wishlist = new ArrayList<>(plugin.getGiftConfigManager().getConfig().getStringList(getWishlistPath(playerUUID)));
        String normalizedWish = wish.trim();
        if (normalizedWish.isEmpty()) {
            return false;
        }

        for (String entry : wishlist) {
            if (entry.equalsIgnoreCase(normalizedWish)) {
                return false;
            }
        }

        wishlist.add(normalizedWish);
        plugin.getGiftConfigManager().getConfig().set(getWishlistPath(playerUUID), wishlist);
        plugin.getGiftConfigManager().saveConfigFile();
        return true;
    }

    public boolean removeItem(UUID playerUUID, String wish) {
        List<String> wishlist = new ArrayList<>(plugin.getGiftConfigManager().getConfig().getStringList(getWishlistPath(playerUUID)));
        if (wishlist.isEmpty()) {
            return false;
        }

        String normalizedWish = wish.trim();
        boolean removed = wishlist.removeIf(entry -> entry.equalsIgnoreCase(normalizedWish));
        if (removed) {
            plugin.getGiftConfigManager().getConfig().set(getWishlistPath(playerUUID), wishlist);
            plugin.getGiftConfigManager().saveConfigFile();
        }
        return removed;
    }

    private String getWishlistPath(UUID playerUUID) {
        return "wishlists." + playerUUID;
    }
}
