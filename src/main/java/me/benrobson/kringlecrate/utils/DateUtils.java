package me.benrobson.kringlecrate.utils;

import me.benrobson.kringlecrate.KringleCrate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("Australia/Sydney");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm z"); // Standard format
    private static KringleCrate plugin;

    public DateUtils(KringleCrate plugin) {
        this.plugin = plugin;
    }

    // Get the reveal date from the config
    public static ZonedDateTime getRevealDate() {
        String dateString = plugin.getConfig().getString("reveal-date");
        try {
            return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(getEventZoneId());
        } catch (Exception e) {
            plugin.getLogger().severe("Invalid reveal date format in config.yml: " + dateString);
            return ZonedDateTime.now(getEventZoneId()); // Return the current time if the config value is invalid
        }
    }

    public static boolean isRevealDay() {
        ZonedDateTime revealDate = getRevealDate();
        return !ZonedDateTime.now(getEventZoneId()).isBefore(revealDate);
    }

    public static boolean isBeforeRevealDate() {
        ZonedDateTime revealDate = getRevealDate();
        return ZonedDateTime.now(getEventZoneId()).isBefore(revealDate);
    }

    public static ZonedDateTime getRedemptionStart() {
        String startDateString = plugin.getConfig().getString("redemption-start");
        try {
            return LocalDateTime.parse(startDateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(getEventZoneId());
        } catch (Exception e) {
            plugin.getLogger().severe("Invalid redemption-start format in config.yml: " + startDateString);
            return ZonedDateTime.ofInstant(Instant.MIN, getEventZoneId()); // Return a minimal value to ensure it won't validate
        }
    }

    public static ZonedDateTime getRedemptionEnd() {
        String endDateString = plugin.getConfig().getString("redemption-end");
        try {
            return LocalDateTime.parse(endDateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(getEventZoneId());
        } catch (Exception e) {
            plugin.getLogger().severe("Invalid redemption-end format in config.yml: " + endDateString);
            return ZonedDateTime.ofInstant(Instant.MAX, getEventZoneId()); // Return a maximal value to ensure it won't validate
        }
    }

    public static boolean isInRedemptionPeriod() {
        ZonedDateTime redemptionStart = getRedemptionStart();
        ZonedDateTime redemptionEnd = getRedemptionEnd();
        ZonedDateTime now = ZonedDateTime.now(getEventZoneId());
        return !now.isBefore(redemptionStart) && !now.isAfter(redemptionEnd);
    }

    public static String getFormattedRedemptionPeriod() {
        return getRedemptionStart().format(formatter) +
                " to " +
                getRedemptionEnd().format(formatter);
    }

    public static ZoneId getEventZoneId() {
        String timezoneId = plugin.getConfig().getString("event-timezone", DEFAULT_TIMEZONE.getId());
        try {
            return ZoneId.of(timezoneId);
        } catch (Exception e) {
            plugin.getLogger().severe("Invalid event-timezone in config.yml: " + timezoneId
                    + ". Falling back to " + DEFAULT_TIMEZONE.getId());
            return DEFAULT_TIMEZONE;
        }
    }
}
