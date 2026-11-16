# Listing: JEI Inventory Highlight

| Field | Value |
| --- | --- |
| Name | JEI Inventory Highlight |
| Slug | `jei-inventory-highlight` |
| Project type | Mod |
| Loaders | Fabric |
| Game versions | 26.1.2 |
| Client side | Required |
| Server side | Unsupported |
| Modrinth categories | Utility, Storage |
| CurseForge categories | Map and Information (primary), Storage, Utility & QoL |
| License | MIT |
| Required dependencies | Fabric API (`fabric-api`), JEI (`jei`) |
| Version number | `1.1.1+mc26.1.2` |
| Release channel | Release |

## Summary (≤ 256 characters)

Double-click the JEI search bar to highlight every matching item in the open inventory and dim the rest. Uses JEI's own search, so @mod, #tag and tooltip search work. Shows how many matches are inside shulker boxes and bundles.

## Description

Ever typed something into JEI and then still had to stare at a chest full of stuff to find it? Not any more.

**Search for something in JEI, then double-click the JEI search bar.** Every slot of the open screen that holds a
matching item gets a yellow frame and all other slots are dimmed. Works in chests, barrels, shulker boxes, backpacks,
the player inventory, and any other container screen. Double-click again to switch it off.

![Highlight mode in a chest](https://raw.githubusercontent.com/mr-shortman/Missing-Fabric-Mods/main/jei-inventory-highlight/docs/highlight.png)

## Features

- **JEI does the matching.** `@mod`, `#tag`, `$oredict`, tooltip search, and everything else the JEI search bar
  understands behaves exactly like in the JEI item list. Potions and enchanted books are matched by their exact
  variant.
- **Looks inside containers.** Shulker boxes, bundles and any other item that carries its contents with it get a
  cyan frame and a counter in the corner: how many stacks inside match the search, nested containers included.
  Items from other mods that store their contents in their own component (a Traveler's Backpack lying in a slot,
  for example) are counted too.
- **No extra UI.** The JEI search bar gets a yellow frame while the mode is on; that is the whole interface.
- **Stays out of the way.** Nothing is dimmed while the search bar is empty or JEI is hidden.
- **Remembers the setting** in `config/jei_inventory_highlight.properties`.

## Limitations

- A *worn* Traveler's Backpack (or any worn container) cannot be searched: the server does not send its contents
  to the client.
- Client-side only. Nothing to install on servers; it works on vanilla servers.

## Requirements

- Minecraft 26.1.2, Fabric Loader 0.18.4 or newer, Java 25
- Fabric API
- JEI for 26.1.2

## Links

Source, issues and the other mods from the same repository: https://github.com/mr-shortman/Missing-Fabric-Mods

## Changelog for 1.1.1+mc26.1.2

First public release.

- Double-click the JEI search bar to highlight matching items in the open container screen and dim everything else.
- Matching uses JEI's own search, so `@mod`, `#tag` and tooltip search work.
- Shulker boxes, bundles and modded container items show how many matching stacks are inside, nested containers
  included.
- The on/off state is saved to `config/jei_inventory_highlight.properties`.
