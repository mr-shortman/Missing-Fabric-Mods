# JEI Inventory Highlight

Client-side Fabric mod for Minecraft **26.1.2**.

Search for something in [JEI](https://github.com/mezz/JustEnoughItems), then **double-click the JEI search bar**:
every slot of the open inventory (chest, backpack, player inventory, any container screen) that holds a matching
item gets a yellow frame, all other slots are dimmed. Double-click again to switch it off.

- The JEI search bar has a yellow frame while the mode is on.
- Matching is done by JEI itself, so `@mod`, `#tag`, tooltip search etc. work exactly like in the JEI item list.
  Potions and enchanted books are matched by their exact variant.
- Shulker boxes, bundles and other items that carry their contents with them get a cyan frame and a number in
  the top-left corner: how many stacks inside them match the search (nested containers included).
- Nothing is dimmed while the search bar is empty or JEI is hidden.
- The on/off state is remembered in `config/jei_inventory_highlight.properties`.

## Requirements

- Minecraft 26.1.2, Fabric Loader ≥ 0.18.4, Java 25
- Fabric API
- JEI for 26.1.2

## Building

```bash
./gradlew build
```

A JDK 25 has to be installed; Gradle picks it up on its own (`gradle/gradle-daemon-jvm.properties`).
The jar ends up in `build/libs/`.
`./gradlew runClient` starts a dev client with JEI installed.
