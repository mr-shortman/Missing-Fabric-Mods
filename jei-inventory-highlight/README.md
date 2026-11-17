# JEI Inventory Highlight

Client-side Fabric mod for Minecraft **26.1.2**.

Search for something in [JEI](https://github.com/mezz/JustEnoughItems), then **double-click the JEI search bar**:
every slot of the open inventory (chest, backpack, player inventory, any container screen) that holds a matching
item gets a yellow frame, all other slots are dimmed. Double-click again to switch it off.

Instead of double-clicking there are two keyboard shortcuts:

- **Press the inventory key twice quickly** (E E): the first press opens the inventory as usual, the second one
  switches the highlight mode on or off instead of closing it again. Can be turned off in the settings.
- **H** switches it on or off in any inventory, chest or other container screen, and also in-game (with a short
  message above the hotbar). The key can be changed under *Controls → Key Binds → Inventory*.

Neither does anything while you are typing into a text field (JEI search bar, creative search, anvil, ...).
When a shortcut switches the mode on and the JEI search bar is empty, the cursor is put into it, so you can type
your search right away: E, E, `diamond`.

- The JEI search bar has a yellow frame while the mode is on.
- Matching is done by JEI itself, so `@mod`, `#tag`, tooltip search etc. work exactly like in the JEI item list.
  Potions and enchanted books are matched by their exact variant.
- Shulker boxes, bundles and other items that carry their contents with them get a cyan frame and a number in
  the top-left corner: how many stacks inside them match the search (nested containers included). This also
  works for mods that store their contents in a component of their own, e.g. a Traveler's Backpack lying in an
  inventory slot. It cannot work for a *worn* Traveler's Backpack: the server does not send its contents to the
  client.
- Nothing is dimmed while the search bar is empty or JEI is hidden.
- The on/off state is remembered in `config/jei_inventory_highlight.properties`.

## Settings

The settings screen opens via the **Configure** button in [Mod Menu](https://modrinth.com/mod/modmenu)
(optional), or with a key you can assign under *Controls → Key Binds → Inventory* (unbound by default).

- **Highlight color**: the frame around matching slots and around the JEI search bar. Enter it as `#RRGGBB`
  or set it with the red, green and blue sliders.
- **Dimmed slots**: how opaque the grey layer over non-matching slots is, from 0 % (not dimmed) to 100 %.
- **Shortcuts**: whether pressing the inventory key twice toggles the mode; also shows the current toggle key.
- A preview shows the colour and dimming right away. *Done* or Esc saves, *Cancel* discards, *Reset* restores the defaults
  (`#FFD83D`, 75 %).

The values end up in the same file as `highlight_color`, `dim_opacity` and `double_tap_inventory` and can be
edited there as well.

## Requirements

- Minecraft 26.1.2, Fabric Loader ≥ 0.18.4, Java 25
- Fabric API
- JEI for 26.1.2
- optional: Mod Menu for 26.1.2

## Building

```bash
./gradlew build
```

A JDK 25 has to be installed; Gradle picks it up on its own (`gradle/gradle-daemon-jvm.properties`).
The jar ends up in `build/libs/`.
`./gradlew runClient` starts a dev client with JEI installed.
