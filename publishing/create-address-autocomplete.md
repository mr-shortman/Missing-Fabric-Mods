# Listing: Create Address Autocomplete

| Field | Value |
| --- | --- |
| Name | Create Address Autocomplete |
| Slug | `create-address-autocomplete` |
| Project type | Mod |
| Loaders | Fabric |
| Game versions | 26.1.2 |
| Client side | Required |
| Server side | Unsupported |
| Modrinth categories | Utility, Technology |
| CurseForge categories | Utility & QoL (primary), Technology, Addons → Create |
| License | MIT |
| Required dependencies | Create Fly (`create-fly`) |
| Version number | `1.0.1+mc26.1.2` |
| Release channel | Release |

## Summary (≤ 256 characters)

Client-side addon for Create Fly: typing a package address into a factory gauge, redstone requester or stock keeper now suggests the addresses that exist around you (frogports, postboxes, packager signs, other gauges). Pick with a click or Enter.

## Description

Create's package addresses are plain text. One typo in a factory gauge and the packages pile up at the wrong
frogport. This mod makes the address box suggest the addresses that already exist around you, so you pick one
instead of typing it from memory.

![Suggestions below the factory gauge address box](https://raw.githubusercontent.com/mr-shortman/Missing-Fabric-Mods/main/create-address-autocomplete/docs/suggestions.png)

## What it does

- **Suggests real addresses.** The list under the address box contains, in this order:
  1. what Create suggests on its own (the gauge's own frogport, `#address` entries on clipboards),
  2. the addresses of frogports and postboxes,
  3. the addresses written on packager signs,
  4. the addresses other factory gauges and redstone requesters already send to.
- **Filters as you type.** The list only shows addresses that begin with what is in the box. An empty box shows
  all of them; right-click the box to clear it.
- **Picks with a click or Enter.** Click an entry, or move with the arrow keys and press Enter or Tab. Escape
  closes the screen as usual.
- **Works in every address box**, not only factory gauges: redstone requester, stock keeper and package filter
  share the same list. Train schedule station names are not touched.

Picking with the mouse or Enter, and filtering by the whole text, also fix Create Fly's own clipboard
suggestions, which ignore clicks and Enter.

## What it can and cannot see

The mod has no server part, so it only knows what the server has sent to your client: the blocks in the loaded
chunks around you, in your current dimension. A frogport beyond your render distance is not suggested unless a
gauge or requester near you already uses its address.

Create does not tell the client which frogport belongs to which logistics network, so the list is not limited to
the gauge's own network.

While the list is not empty, Create hides its small clipboard hint icon next to the address box. Clipboard
addresses still work.

## Requirements

- Minecraft 26.1.2, Fabric Loader 0.18.4 or newer, Java 25
- [Create Fly](https://modrinth.com/mod/create-fly) 6.0.9 for 26.1.2
- Client-side only. Works on servers that do not have it.

## Links

Source, issues and the other Create Fly mods from the same repository: https://github.com/mr-shortman/Missing-Fabric-Mods

## Changelog for 1.0.1+mc26.1.2

First public release.

- Address boxes of factory gauges, redstone requesters, stock keepers and package filters suggest the addresses of
  frogports, postboxes, packager signs and other gauges in the loaded chunks around you.
- The list filters by the text in the box and is picked with a mouse click, Enter or Tab.
- Create Fly's own clipboard suggestions can now be picked with a click or Enter as well.
