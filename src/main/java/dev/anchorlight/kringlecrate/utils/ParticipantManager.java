package dev.anchorlight.kringlecrate.utils;

import dev.anchorlight.kringlecrate.KringleCrate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ParticipantManager {
    private final KringleCrate plugin;

    public ParticipantManager(KringleCrate plugin) {
        this.plugin = plugin;
    }

    // Method to get the list of participants
    public List<String> getParticipants() {
        return plugin.getGiftConfigManager().getConfig().getStringList("participants");
    }

    // Method to add a participant
    public void addParticipant(UUID playerUUID) {
        List<String> participants = plugin.getGiftConfigManager().getConfig().getStringList("participants");

        // Convert UUID to String for storage
        if (!participants.contains(playerUUID.toString())) {
            participants.add(playerUUID.toString()); // Add the player's UUID as a string
            plugin.getGiftConfigManager().getConfig().set("participants", participants);
            plugin.getGiftConfigManager().saveConfigFile(); // Save the changes
            plugin.getLogger().info("Player " + playerUUID + " added to the Secret Santa participants list.");
        }
    }

    // Method to get the assigned player for a participant
    public String getAssignedPlayer(String playerUUID) {
        return plugin.getGiftConfigManager().getConfig().getString("assignments." + playerUUID);
    }

    // Method to assign participants (for Secret Santa pairing)
    public void assignParticipants() {
        List<String> participants = plugin.getGiftConfigManager().getConfig().getStringList("participants");

        if (participants.size() < 2) {
            plugin.getLogger().severe("Not enough participants to assign recipients.");
            return;
        }

        // Shuffle participants and assign them Secret Santa partners
        Collections.shuffle(participants);

        for (int i = 0; i < participants.size(); i++) {
            String giver = participants.get(i);
            String recipient = participants.get((i + 1) % participants.size());
            plugin.getGiftConfigManager().getConfig().set("assignments." + giver, recipient);
        }

        plugin.getGiftConfigManager().saveConfigFile();
        plugin.getLogger().info("Recipients successfully assigned!");
    }
}
