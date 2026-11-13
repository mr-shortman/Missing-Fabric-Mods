# Sheet Recycling

A datapack for Minecraft 26.1.2 with Create Fly that turns Create's metal sheets back into metal.
Create presses 1 ingot into 1 sheet but has no way back.

| Machine | Input | Output |
| --- | --- | --- |
| Furnace | 1 sheet | 1 ingot (10 s) |
| Blast furnace | 1 sheet | 1 ingot (5 s) |
| Create crushing wheels | 1 sheet | 6 nuggets, plus 3 more at 50 % (7.5 on average) |

This works for all four metal sheets:

| Sheet | Ingot | Nugget |
| --- | --- | --- |
| Iron sheet | Iron ingot | Iron nugget |
| Copper sheet | Copper ingot | Copper nugget |
| Golden sheet | Gold ingot | Gold nugget |
| Brass sheet | Brass ingot | Brass nugget |

Melting gives the full ingot back but costs fuel. The crushing wheels need no fuel, only rotation, and lose a little
(7.5 of 9 nuggets on average), which is in line with Create's own recycling recipes for horse armor.
None of the ways gives more metal than the sheet cost, so nothing can be duplicated.

Create's encased fan with lava behind it (bulk blasting) runs furnace recipes too, so sheets can be melted down automatically.

The sturdy sheet (made from obsidian powder) and cardboard aren't metal sheets and are left out.

## Install

Download `sheet-recycling.zip` from the [latest release](https://github.com/mr-shortman/Missing-Fabric-Mods/releases/latest),
or zip the contents of this folder yourself (`pack.mcmeta` has to be at the top of the zip).
Put it into `saves/<world>/datapacks/` (or `world/datapacks/` on a server) and run `/reload`.

## Changing the numbers

The recipes are plain JSON in `data/sheet_recycling/recipe/`. Other balanced setups:

- **Lossy furnace:** let the normal furnace give 6 nuggets instead of an ingot (`"result": {"count": 6, "id": "minecraft:iron_nugget"}`),
  so only the blast furnace gives the full ingot back.
- **Lossless crushing:** set the second result's `chance` to `1.0` for a flat 9 nuggets. Then crushing is strictly better than melting.
