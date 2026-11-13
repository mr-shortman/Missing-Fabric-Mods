# Pointed Dripstone Recipes

A datapack for Minecraft 26.1.2 (Fabric, works in vanilla too) that turns dripstone blocks back into pointed dripstone.
Vanilla only lets you craft 4 pointed dripstone into 1 dripstone block, never the other way.

| Machine | Input | Output | Needs |
| --- | --- | --- | --- |
| Stonecutter | 1 dripstone block | 2 pointed dripstone | nothing |
| Create crushing wheels | 1 dripstone block | 3 pointed dripstone, plus 1 more at 25 % (3.25 on average) | Create Fly |

Neither gives back the 4 pieces the block cost, so you can't duplicate dripstone by going back and forth.
The stonecutter is the cheap early way; crushing wheels are the better way once you have Create.

With Create:

- The mechanical saw also runs stonecutter recipes (as long as Create's `allowStonecuttingOnSaw` setting is on, which it is by default), so a saw gives 2 per block too.
- The millstone still turns a dripstone block into clay (Create's own recipe). Only the crushing wheels make pointed dripstone, because a crushing recipe takes priority there.

Without Create the crushing recipe is skipped. The server log shows an error about the unknown recipe type `create:crushing` for it, which you can ignore.

## Install

Download `pointed-dripstone-recipes.zip` from the [latest release](https://github.com/mr-shortman/Missing-Fabric-Mods/releases/latest),
or zip the contents of this folder yourself (`pack.mcmeta` has to be at the top of the zip).

- New world: Create World → More → Data Packs, and drag the zip in.
- Existing world: put the zip into `saves/<world>/datapacks/` (or `world/datapacks/` on a server) and run `/reload`.

## Changing the numbers

The recipes are plain JSON in `data/pointed_dripstone_recipes/recipe/`. Other balanced setups:

- **Cheaper stonecutter:** set `count` in `stonecutting/pointed_dripstone_from_dripstone_block.json` to `3`.
  Still below 4, so no duplication, but Create gives hardly any advantage then.
- **Create only:** delete the `stonecutting` folder. Pointed dripstone then comes from crushing wheels or from growing it under dripstone with water above.
- **Crafting table:** a shapeless recipe, 1 dripstone block → 2 pointed dripstone, does the same as the stonecutter without a stonecutter.
  Put this in `recipe/pointed_dripstone_from_dripstone_block.json`:

  ```json
  {
    "type": "minecraft:crafting_shapeless",
    "category": "building",
    "ingredients": ["minecraft:dripstone_block"],
    "result": { "count": 2, "id": "minecraft:pointed_dripstone" }
  }
  ```
