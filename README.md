# 🎄📦 KringleCrate

## Overview

KringleCrate is a Minecraft plugin designed to enable a Secret Santa gift exchange among players. It features an opt-in system, recipient assignments, wishlist management, item and currency gift submissions, and a redemption system while preserving item metadata such as enchantments, names, and lore.

---

## Features

1. **Opt-in System**: Players can opt into the Secret Santa event using a command.
2. **Recipient Assignment**: Each participant is randomly assigned a recipient.
3. **Wishlist Support**: Participants can record their gift wishes to help Santas pick the perfect present.
4. **Gift Submission**: Players can submit item gifts while preserving all metadata or send currency directly through Vault.
5. **Gift Redemption**: Players can redeem their items and any accumulated currency during the configured redemption period.
---

## Configuration

### `config.yml`
```yaml
redemption-start: "2024-12-25T00:00:00"
redemption-end: "2025-01-01T23:59:59"
reveal-date: "2024-12-20T00:00:00"
```

## Commands

* `/kc join`
  * Opt into the Secret Santa event and create a wishlist profile.
* `/kc reveal`
  * Reveal your assigned recipient (after the reveal date or with the `kringlecrate.override` permission).
* `/kc submit`
  * Submit the item held in your main hand to your assigned recipient.
  * `/kc submit currency <amount>` sends Vault currency instead of an item.
* `/kc wishlist <view|add|remove>`
  * Manage your gift wishlist once you have joined the event.
* `/kc redeem`
  * Redeem stored gifts during the redemption period, including any currency deposits.

### Permissions

* `kringlecrate.override`: Allows admins to bypass reveal date restrictions.
* `kringlecrate.redeem`: Grants access to the redeem command (enabled for players by default).

## How It Works
* Players use `/kc join` to participate in the event.
* The plugin assigns a random recipient to each participant.
* Participants can request presents with `/kc wishlist add <item>`.
* Santas submit gifts using `/kc submit` (items) or `/kc submit currency <amount>` (Vault currency) before the redemption period begins.
* On the reveal date, players can use `/kc reveal` to see their assigned recipient.
* Gifts, including currency, can be redeemed using `/kc redeem` during the redemption period.
