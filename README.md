# 🎄📦 KringleCrate

## Overview

KringleCrate is a Minecraft plugin designed to enable a Secret Santa gift exchange among players. It features an opt-in
system, recipient assignments, wishlist management, item and currency gift submissions, and a redemption window that
preserves item metadata such as enchantments, names, and lore.

## Features

- **Opt-in event flow** with join, reveal, submit, and redeem stages.
- **Recipient assignment** for every participant.
- **Wishlist management** so Santas can pick the perfect present.
- **Item + Vault currency gifts** with metadata retention.
- **Redemption window** that keeps delivery fair and predictable.

## Requirements

- Spigot/Paper server compatible with the plugin build.
- Vault (only required if you want currency gifts enabled).
- An economy plugin supported by Vault (for currency gifts).

## Installation

1. Drop the plugin jar into your server's `plugins/` folder.
2. Start the server once to generate `config.yml`.
3. Configure event dates/timezone (see below).
4. Restart the server or reload the plugin.

## Configuration

All event dates are ISO-local timestamps (no zone offset). The `event-timezone` is applied to interpret those dates.

### `config.yml`
```yaml
redemption-start: "2024-12-25T00:00:00"
redemption-end: "2025-01-01T23:59:59"
reveal-date: "2024-12-20T00:00:00"
event-timezone: "Australia/Sydney"
```

### Timezone behavior

All event dates (reveal, redemption start/end, and the join/submit/redeem windows derived from them) are interpreted in
the configured `event-timezone`. If the field is missing or invalid, KringleCrate defaults to `Australia/Sydney`.

## Commands

| Command | Description |
| --- | --- |
| `/kc join` | Opt into the event and create a wishlist profile. |
| `/kc reveal` | Reveal your recipient after the reveal date (or with override permission). |
| `/kc submit` | Submit the item in your main hand to your recipient. |
| `/kc submit currency <amount>` | Send Vault currency instead of an item. |
| `/kc wishlist <view|add|remove>` | Manage your gift wishlist once joined. |
| `/kc redeem` | Redeem stored gifts during the redemption period. |

## Permissions

| Permission | Description |
| --- | --- |
| `kringlecrate.override` | Bypass reveal date restrictions (admins). |
| `kringlecrate.redeem` | Use the redeem command (enabled for players by default). |

## How It Works

1. Players run `/kc join` to participate.
2. The plugin assigns a random recipient to each participant.
3. Participants add wishes with `/kc wishlist add <item>`.
4. Santas submit gifts using `/kc submit` (items) or `/kc submit currency <amount>` (Vault currency) before the
   redemption period begins.
5. On the reveal date, players use `/kc reveal` to see their assigned recipient.
6. Gifts can be redeemed using `/kc redeem` during the redemption period.

## Notes

- If Vault or an economy plugin is missing, currency gifts are unavailable but item gifts still work.
- Date messages shown to players include the configured timezone for clarity.
