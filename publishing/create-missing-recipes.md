# Listing: Create Missing Recipes

One listing for the three datapacks in this repository (`pointed-dripstone-recipes`, `sheet-recycling`,
`scoria-melting`). How to build the combined zip is in [PUBLISHING.md](../PUBLISHING.md), section 1.6; the
`pack.mcmeta` for it is [create-missing-recipes.pack.mcmeta](create-missing-recipes.pack.mcmeta).

| Field | Value |
| --- | --- |
| Name | Create Missing Recipes |
| Slug | `create-missing-recipes` |
| Project type | Data pack (Modrinth), Data Packs (CurseForge) |
| Loaders | Data pack |
| Game versions | 26.1.2 |
| Client side | Unsupported |
| Server side | Required |
| Modrinth categories | Utility, Technology |
| CurseForge categories | Recipes (primary), Tech |
| License | MIT |
| Required dependencies | Create Fly (`create-fly`) |
| Version number | `1.0.0+mc26.1.2` |
| Release channel | Release |

## Summary (≤ 256 characters)

Three small recipe fixes for Create Fly: melt or crush metal sheets back into ingots and nuggets, turn dripstone blocks back into pointed dripstone, and melt scoria into lava in a heated mixer. Nothing can be duplicated.

## Description

Three things Create lets you make but never take apart. Each recipe is balanced so that the round trip always
costs something: nothing in this pack can be duplicated.

## Sheet recycling

Create presses 1 ingot into 1 sheet but has no way back. Now there is one, for iron, copper, golden and brass
sheets:

- **Furnace:** 1 sheet → 1 ingot (10 s)
- **Blast furnace:** 1 sheet → 1 ingot (5 s)
- **Crushing wheels:** 1 sheet → 6 nuggets, plus 3 more at 50 % (7.5 on average)

Melting gives the full ingot back but costs fuel. The crushing wheels need no fuel, only rotation, and lose a
little, in line with Create's own recycling recipes for horse armor. Create's encased fan with lava behind it
(bulk blasting) runs furnace recipes too, so sheets can be melted down automatically.

Sturdy sheets and cardboard are not metal sheets and are left alone.

## Pointed dripstone

Vanilla lets you craft 4 pointed dripstone into 1 dripstone block, never the other way.

- **Stonecutter:** 1 dripstone block → 2 pointed dripstone
- **Crushing wheels:** 1 dripstone block → 3 pointed dripstone, plus 1 more at 25 % (3.25 on average)

Neither gives back the 4 pieces the block cost. The stonecutter is the cheap early way; crushing wheels are the
better way once you have Create. The mechanical saw also runs stonecutter recipes (Create's
`allowStonecuttingOnSaw` setting, on by default), so a saw gives 2 per block too. The millstone keeps Create's
own recipe and still turns dripstone blocks into clay.

## Scoria melting

Scoria piles up next to every lava-and-water generator. Now it melts back in a mechanical mixer over a basin:

- **Heated blaze burner:** 2 scoria → 125 mB lava
- **Superheated blaze burner:** 4 scoria → 500 mB lava (double the rate)

The heated recipe takes 2 scoria so that, over a superheated burner with 4 or more scoria in the basin, Create's
mixer always picks the superheated recipe (it prefers the recipe with more ingredients). Feed it whole stacks
from a vault with a funnel or chute to always get the double yield. For comparison, Create's own recipe melts
1 cobblestone into 50 mB, superheated only.

## Install

Put the zip into `saves/<world>/datapacks/` (or `world/datapacks/` on a server) and run `/reload`, or add it under
"Data Packs" when creating a world. Multiplayer: only the server needs it.

Needs **Create Fly** for the crushing, mixing, sheet and scoria recipes. The two stonecutter recipes also work
without Create; the log then shows an ignorable error about the unknown recipe type `create:crushing`.

## Changing the numbers

Every recipe is a plain JSON file in `data/<pack>/recipe/`. Unzip, edit the `count`, `chance` or `amount`
values, zip again. The README of each pack in the repository lists other balanced setups (lossy furnace,
crafting-table recipe, Create only).

## Links

Source, issues and the Create Fly mods from the same repository: https://github.com/mr-shortman/Missing-Fabric-Mods

## Changelog for 1.0.0+mc26.1.2

First public release, combining three packs:

- **Sheet recycling:** iron, copper, golden and brass sheets smelt or blast into ingots and crush into nuggets.
- **Pointed dripstone:** dripstone blocks give pointed dripstone in the stonecutter and the crushing wheels.
- **Scoria melting:** scoria melts into lava in a heated mixer, twice as much when superheated.
