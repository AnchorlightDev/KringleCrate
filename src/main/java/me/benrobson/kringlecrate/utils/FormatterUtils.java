package me.benrobson.kringlecrate.utils;

import me.benrobson.kringlecrate.KringleCrate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class FormatterUtils {

    private static KringleCrate plugin;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm z"); // Format for display

    public FormatterUtils(KringleCrate plugin) {
        this.plugin = plugin;
    }

    public static String getFormattedRedemptionPeriod() {
        try {
            ZonedDateTime redemptionStart = DateUtils.getRedemptionStart();
            ZonedDateTime redemptionEnd = DateUtils.getRedemptionEnd();
            return "from " + redemptionStart.format(formatter) + " to " + redemptionEnd.format(formatter);
        } catch (Exception e) {
            plugin.getLogger().severe("Error formatting redemption period: " + e.getMessage());
            return "Unknown redemption period";
        }
    }

    public static String getFormattedRevealDate() {
        try {
            ZonedDateTime revealDate = DateUtils.getRevealDate();
            return revealDate.format(formatter);
        } catch (Exception e) {
            plugin.getLogger().severe("Error formatting reveal date: " + e.getMessage());
            return "Unknown date";
        }
    }
}
