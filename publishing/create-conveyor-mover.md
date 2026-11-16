# Listing: Create Chain Conveyor Mover

| Field | Value |
| --- | --- |
| Name | Create Chain Conveyor Mover |
| Slug | `create-conveyor-mover` |
| Project type | Mod |
| Loaders | Fabric |
| Game versions | 26.1.2 |
| Client side | Optional |
| Server side | Required |
| Modrinth categories | Utility, Technology, Transportation |
| CurseForge categories | Utility & QoL (primary), Technology, Addons → Create |
| License | MIT |
| Required dependencies | Create Fly (`create-fly`) |
| Version number | `1.0.0+mc26.1.2` |
| Release channel | Release (or Beta while Create Fly for 26.1.2 is still beta) |

## Summary (≤ 256 characters)

Addon for Create Fly: pick up a chain conveyor with the wrench and set it down somewhere else. Its chains, packages, routing tables and frogports come along, as long as Create's connection rules still hold at the new spot.

## Description

A chain conveyor that stands one block off normally means: break it, lose every chain on it, and reconnect all
of them and their frogports by hand. With this mod you **pick the conveyor up with the wrench and set it down
somewhere else**. Its chain network stays in one piece.

![Moving a chain conveyor: green preview at the new spot](https://raw.githubusercontent.com/mr-shortman/Missing-Fabric-Mods/main/create-conveyor-mover/docs/move.gif)

## How to use it

1. **Right-click a chain conveyor with the wrench** (not sneaking). It is now picked up and outlined.
2. Look at the place it should go. The preview shows the conveyor and its chains there: green if the move is
   allowed, red with the reason in the action bar if it is not.
3. **Right-click** to set it down. It lands where a block would be placed: against the face you click, or inside
   the clicked block if that is grass, snow or similar.

Right-click the picked-up conveyor again, or sneak-right-click anywhere, to cancel. Sneak-right-clicking a
conveyor with nothing picked up still dismantles it, as in Create.

## What moves along

- every chain of the conveyor, on both ends,
- the packages on the conveyor and on those chains,
- the routing tables, so packages keep finding their way without a pause,
- the frogports: ports on the conveyor's wheel move with it, ports on one of its chains stay hooked to the spot
  of the new chain that is closest to their old spot.

A shaft or cogwheel that drove the conveyor from above or below stays where it is. At its new place the conveyor
is driven by its chains, or by whatever shaft is there.

## When the move is refused

The new place has to pass the rules Create applies when you build the same thing by hand:

- the block is free, and no other chain conveyor is directly next to it,
- every chain is still within the maximum length (32 blocks by default), not shorter than 2.5 blocks, not
  vertical and not steeper than 45°,
- every frogport still reaches its chain: within the port range (5 blocks by default) and not above the chain,
- every connected conveyor is loaded,
- in survival, if the chains get longer in total you need the extra chains in your inventory; if they get
  shorter you get chains back.

If a rule fails nothing changes, and the action bar says which one.

## Where to install

- **Server** (or the singleplayer world): required. The server does the moving.
- **Client**: recommended. Without it you get no preview and see untranslated messages, but the move still works.

## Requirements

- Minecraft 26.1.2, Fabric Loader 0.18.4 or newer, Java 25
- [Create Fly](https://modrinth.com/mod/create-fly) 6.0.9 for 26.1.2

The mod hooks into Create Fly's chain conveyor code with mixins. A Create Fly update that changes that code can
break it; if a move stops working after a Create Fly update, open an issue with both version numbers.

## Links

Source, issues and the other Create Fly mods from the same repository: https://github.com/mr-shortman/Missing-Fabric-Mods

## Changelog for 1.0.0+mc26.1.2

First public release.

- Right-click a chain conveyor with the wrench to pick it up, right-click again to set it down elsewhere.
- Chains, packages, routing tables and frogports move with the conveyor.
- A green or red preview shows whether the new spot passes Create's connection rules; the action bar names the
  rule that fails.
- In survival the chain difference is taken from or returned to the inventory.
