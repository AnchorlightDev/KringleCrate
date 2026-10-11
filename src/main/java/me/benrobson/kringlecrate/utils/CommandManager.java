package me.benrobson.kringlecrate.utils;

import me.benrobson.kringlecrate.KringleCrate;
import me.benrobson.kringlecrate.commands.join;
import me.benrobson.kringlecrate.commands.redeem;
import me.benrobson.kringlecrate.commands.reveal;
import me.benrobson.kringlecrate.commands.submit;
import me.benrobson.kringlecrate.commands.wishlist;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class CommandManager implements CommandExecutor {

    private final KringleCrate plugin;

    public CommandManager(KringleCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /kc <join|reveal|submit|redeem|wishlist>");
            return true;
        }

        // Dispatch to the correct subcommand
        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "join":
                return new join(plugin).onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            case "reveal":
                return new reveal(plugin).onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            case "submit":
                return new submit(plugin).onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            case "redeem":
                return new redeem(plugin).onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            case "wishlist":
                return new wishlist(plugin).onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /kc <join|reveal|submit|redeem|wishlist>");
                return true;
        }
    }
}
