package dev.anchorlight.kringlecrate.commands;

import dev.anchorlight.stonelib.command.CommandRouter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Bukkit executor + tab completer backed by StoneLib's {@link CommandRouter}. */
public class KcCommand implements CommandExecutor, TabCompleter {

    private final CommandRouter router;

    public KcCommand(CommandRouter router) {
        this.router = router;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        return router.dispatch(sender, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        return router.tabComplete(sender, args);
    }
}
