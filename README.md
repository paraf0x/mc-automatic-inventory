# AutomaticInventory (Fork)

> This is a maintained fork of [ChristopherHaws/mc-automatic-inventory](https://github.com/ChristopherHaws/mc-automatic-inventory).

**Deposit ALL your loot into ALL the right boxes with a single slash command! Plus automatic inventory/chest sorting AND MORE!**

## Features

### /DepositAll

Instantly dump all your loot into all the right chests. Finds all chests (within one chunk of your location) that you have permission to open and automatically deposits any items from your backpack (not your hotbar) which match items in those chests.

**Shulker Box Handling:** Named shulker boxes match by name. Unnamed shulker boxes are only deposited if completely full with a single item type (e.g., a shulker with 27 full stacks of cobblestone goes into a chest containing cobblestone). Mixed or partially filled shulkers stay in your inventory. Shulker boxes are never deposited into other shulker boxes, with /DepositAll or Quick Deposit.

**Locks:** A locked item type is never deposited, by /DepositAll or Quick Deposit, whatever its slot or name. Sorting and hotbar refill still move it inside your inventory. Arrows, spectral arrows and tipped arrows start locked.

- `/da lock <item>` locks a type, for example `/da lock minecraft:ender_chest`. `ender_chest` and `enderchest` work too.
- `/da unlock <item>` unlocks it, including the three arrow types.
- `/da list` (or `/da lock list`) shows every lock. Click `[unlock]` to remove one.
- Tab after `lock` shows the items you carry. Type part of a name to search all items.

A type is the item ID: `potion` covers every drinkable potion, and each shulker box color is its own type. A lock does not look inside shulker boxes: a full box of a locked item can still be deposited. Locks are saved on the server for each player.

Items under `Auto Deposit: Excluded Items` in `config.yml` are locked for everyone and cannot be unlocked by players. Any argument other than the ones above shows help and deposits nothing.

Aliases: `da`, `dumpitems`, `dumploot`, `depositloot`

### Quick Deposit (Shift-left-click)

Like /DepositAll, but includes your hotbar items and only matches against items in the specific chest you're opening.

### Auto-sort Chests

When a player opens a chest, the contents will be automatically condensed and sorted. Players can disable this or exclude specific chests by renaming them with an asterisk (*).

### Auto-sort Player Inventory

When a player opens their inventory, it's sorted (hotbar untouched).

### Auto-refill Hotbar

When a player uses the last item in a stack or breaks a tool, a replacement item will be pulled from inventory.

## Permissions

By default, all players have all permissions.

| Permission                          | Description                                    |
| ----------------------------------- | ---------------------------------------------- |
| `automaticinventory.user.*`         | All permissions below                          |
| `automaticinventory.sortinventory`  | Personal inventory sorting                     |
| `automaticinventory.sortchests`     | Chest sorting                                  |
| `automaticinventory.refillstacks`   | Auto-replace broken tools and depleted stacks  |
| `automaticinventory.quickdeposit`   | Shift+right-click quick deposit                |
| `automaticinventory.depositall`     | Access to /DepositAll command                  |

## Commands

| Command                               | Description                           |
| ------------------------------------- | ------------------------------------- |
| `/autosort chests`                    | Toggle automatic chest sorting        |
| `/autosort inventory`                 | Toggle automatic inventory sorting    |
| `/depositall`                         | Dump backpack loot into nearby chests |
| `/depositall lock/unlock <item>`      | Lock or unlock an item type           |
| `/depositall list`                    | Show your locked item types           |
| `/quickdeposit toggle/enable/disable` | Control quick deposit                 |
| `/autorefill toggle/enable/disable`   | Control hotbar auto-refill            |

## Installation

Copy `AutomaticInventory.jar` into your server's plugins folder and reload/restart.

### Updating to 4.3.0

On the first start the plugin saves `config.yml` as `config.yml.bak` and removes the three arrow types from the server-wide list. Every player has them as default locks instead. To force them for everyone again, add them back to the list.

Back up `plugins/automaticinventory/` before updating. To go back to 4.2.0, restore that backup with the old jar: 4.2.0 does not keep locks and does not protect arrows with the new config.

**Note:** Some anti-cheat plugins may flag /DepositAll as cheating. Consider relaxing right-click reach checks in your anti-cheat config.

## Credits

Fork chain:

- [ChristopherHaws/mc-automatic-inventory](https://github.com/ChristopherHaws/mc-automatic-inventory)
- [MLG-Fortress/AutomaticInventory](https://github.com/MLG-Fortress/AutomaticInventory)
- [HelloWorldMinecraft/AutomaticInventory](https://github.com/HelloWorldMinecraft/AutomaticInventory)
- [BigScary/AutomaticInventory](https://github.com/BigScary/AutomaticInventory)
