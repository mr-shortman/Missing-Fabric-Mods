# Scoria Melting

A datapack for Minecraft 26.1.2 with Create Fly that melts scoria into lava in a mechanical mixer over a basin.

| Blaze burner | Input | Output |
| --- | --- | --- |
| Heated | 2 scoria | 125 mB lava (= 250 mB per 4 scoria) |
| Superheated | 4 scoria | 500 mB lava (double) |

Fluid amounts in the JSON are in Fabric droplets: 81000 droplets are 1 bucket (1000 mB), so 125 mB is 10125 and 500 mB is 40500.

## Why the heated recipe takes 2 scoria and not 4

A superheated blaze burner also counts as heated, so at superheat both recipes fit. Create's mixer then picks the recipe
with the most ingredients; on a tie the order is not defined. With 2 scoria in the heated recipe and 4 in the superheated one,
the superheated recipe always wins as soon as 4 scoria are in the basin. The rate at plain heat is the same as 4 → 250 mB.

If the basin only gets 2 or 3 scoria at a time over a superheated burner, the heated recipe runs and you get the normal yield.
Feed it whole stacks (a funnel or chute from a vault) to always get the double yield.

## Install

Download `scoria-melting.zip` from the [latest release](https://github.com/mr-shortman/Missing-Fabric-Mods/releases/latest),
or zip the contents of this folder yourself (`pack.mcmeta` has to be at the top of the zip).
Put it into `saves/<world>/datapacks/` (or `world/datapacks/` on a server) and run `/reload`.

For comparison: Create's own recipe melts 1 cobblestone into 50 mB of lava, superheated only.
