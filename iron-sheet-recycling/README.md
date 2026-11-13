# Iron Sheet Recycling

A datapack for Minecraft 26.1.2 with Create Fly that turns Create's iron sheets back into iron.
Create presses 1 iron ingot into 1 iron sheet but has no way back.

| Machine | Input | Output |
| --- | --- | --- |
| Furnace | 1 iron sheet | 1 iron ingot (10 s) |
| Blast furnace | 1 iron sheet | 1 iron ingot (5 s) |
| Create crushing wheels | 1 iron sheet | 6 iron nuggets, plus 3 more at 50 % (7.5 on average) |

Melting gives the full ingot back but costs fuel. The crushing wheels need no fuel, only rotation, and lose a little
(7.5 of 9 nuggets on average), which is in line with Create's own recycling recipes for horse armor.
None of the ways gives more iron than the sheet cost, so nothing can be duplicated.

Create's encased fan with lava behind it (bulk blasting) runs furnace recipes too, so sheets can be melted down automatically.

## Install

Download `iron-sheet-recycling.zip` from the [latest release](https://github.com/mr-shortman/Missing-Fabric-Mods/releases/latest),
or zip the contents of this folder yourself (`pack.mcmeta` has to be at the top of the zip).
Put it into `saves/<world>/datapacks/` (or `world/datapacks/` on a server) and run `/reload`.

## Changing the numbers

The recipes are plain JSON in `data/iron_sheet_recycling/recipe/`. Other balanced setups:

- **Lossy furnace:** let the normal furnace give 6 iron nuggets instead of an ingot (`"result": {"count": 6, "id": "minecraft:iron_nugget"}`),
  so only the blast furnace gives the full ingot back.
- **Lossless crushing:** set the second result's `chance` to `1.0` for a flat 9 nuggets. Then crushing is strictly better than melting.
- **Other sheets:** copy a recipe and swap the ids, e.g. `create:golden_sheet` → `minecraft:gold_ingot`,
  `create:copper_sheet` → `minecraft:copper_ingot`, `create:brass_sheet` → `create:brass_ingot`.
