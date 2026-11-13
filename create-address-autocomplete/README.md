# Create Address Autocomplete

Client-side Fabric mod for Minecraft **26.1.2** and [Create Fly](https://modrinth.com/mod/create-fly).

When you type a package address into a **factory gauge**, the address box now suggests the addresses that
already exist around you, so you pick one instead of typing it from memory.

- Start typing: the list only shows addresses that begin with what is in the box. An empty box shows all of
  them (right-click the box to clear it).
- Pick an entry with a **mouse click**, or move with the arrow keys and press **Enter** or Tab.
- Escape closes the screen as usual.

Picking with the mouse or Enter and filtering by the whole text are fixes on top of Create Fly, whose own
clipboard suggestions ignore clicks and Enter. Clipboard suggestions benefit from them as well.

The list contains, in this order:

1. what Create suggests on its own (the gauge's own frogport, `#address` entries on clipboards),
2. the addresses of frogports and postboxes,
3. the addresses written on packager signs,
4. the addresses other factory gauges and redstone requesters already send to.

The same suggestions show up in every other Create address box (redstone requester, stock keeper, package
filter), because they all share one list. Station name suggestions in train schedules are not touched.

## What it can and cannot see

The mod has no server part, so it only knows what the server has sent to your client: the blocks in the
**chunks loaded around you**, in your current dimension. A frogport beyond your render distance is not
suggested, unless a gauge or requester near you already uses its address (group 4).

Create does not tell the client which frogport belongs to which logistics network, so the list is not
limited to the gauge's own network.

As soon as the list is not empty, Create hides its small clipboard hint icon next to the address box.
Clipboard addresses still work.

## Requirements

- Minecraft 26.1.2, Fabric Loader ≥ 0.18.4, Java 25
- Create Fly 6.0.9 for 26.1.2
- Only the client needs the mod; it works on servers that do not have it.

## Building

```bash
./gradlew build
```

A JDK 25 has to be installed; Gradle picks it up on its own (`gradle/gradle-daemon-jvm.properties`).
The jar ends up in `build/libs/`.
The build runs one test that starts Fabric Loader with Create Fly and checks that the mixins still apply.
`./gradlew runClient` starts a dev client with Create Fly installed.
