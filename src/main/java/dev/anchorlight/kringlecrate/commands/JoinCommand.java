package dev.anchorlight.kringlecrate.commands;

import dev.anchorlight.kringlecrate.KringleCrate;
import dev.anchorlight.kringlecrate.utils.DateUtils;
import dev.anchorlight.kringlecrate.utils.ParticipantManager;
import dev.anchorlight.stonelib.command.SubCommand;
import dev.anchorlight.stonelib.message.MessageService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class JoinCommand implements SubCommand {

    private final KringleCrate plugin;

    public JoinCommand(KringleCrate plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Join the Secret Santa event.";
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
        UUID playerUUID = player.getUniqueId();

        if (participantManager.getParticipants().contains(playerUUID.toString())) {
            messages.send(player, "join.already");
            return;
        }

        if (DateUtils.isInRedemptionPeriod()) {
            messages.send(player, "join.redemption-locked");
            plugin.getLogger().info("[JOIN] Join attempt inside redemption period by " + playerUUID + ".");
            return;
        }

        participantManager.addParticipant(playerUUID);
        plugin.getWishlistManager().initializeWishlist(playerUUID);

        messages.send(player, "join.success");
    }
}
