package me.benrobson.kringlecrate.utils;

import me.benrobson.kringlecrate.KringleCrate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandCompleteManager implements TabCompleter {

    private final KringleCrate plugin;

    public CommandCompleteManager(KringleCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("join", "reveal", "submit", "redeem", "wishlist"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("submit")) {
            return filter(List.of("currency"), args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("wishlist")) {
            return filter(Arrays.asList("view", "add", "remove"), args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reveal")) {
            return filter(List.of("wishlist"), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("wishlist") && args[1].equalsIgnoreCase("remove")
                && sender instanceof Player player) {
            List<String> wishlist = plugin.getWishlistManager().getWishlist(player.getUniqueId());
            return filter(wishlist, args[2]);
        }

        return new ArrayList<>();
    }

    private List<String> filter(List<String> options, String input) {
        List<String> results = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(input.toLowerCase())) {
                results.add(option);
            }
        }
        return results;
    }
}
