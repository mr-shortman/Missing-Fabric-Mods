# Create Chain Conveyor Mover

Fabric mod for Minecraft **26.1.2** and [Create Fly](https://modrinth.com/mod/create-fly).

A chain conveyor that stands one block off normally means: break it, lose every chain on it, and reconnect
all of them and their frogports by hand. With this mod you pick the conveyor up with the **wrench** and set it
down somewhere else. Its chain network stays in one piece.

## How to use it

1. **Right-click a chain conveyor with the wrench** (not sneaking). It is now picked up and outlined.
2. Look at the place it should go. The preview shows the conveyor and its chains there: green if the move is
   allowed, red with the reason in the action bar if it is not.
3. **Right-click** to set it down. It lands where a block would be placed: against the face you click, or
   inside the clicked block if that is grass, snow or similar.

Right-click the picked-up conveyor again, or sneak-right-click anywhere, to cancel. Sneak-right-clicking a
conveyor with nothing picked up still dismantles it, as in Create.

## What moves along

- every chain of the conveyor, on both ends,
- the packages on the conveyor and on those chains,
- the routing tables, so packages keep finding their way without a pause,
- the frogports: ports on the conveyor's wheel move with it, ports on one of its chains stay hooked to the
  spot of the new chain that is closest to their old spot.

A shaft or cogwheel that drove the conveyor from above or below stays where it is. At its new place the
conveyor is driven by its chains, or by whatever shaft is there.

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

## Requirements

- Minecraft 26.1.2, Fabric Loader ≥ 0.18.4, Java 25
- Create Fly 6.0.9 for 26.1.2
- In multiplayer the **server** needs the mod; it does the moving. Clients should have it too: without it they
  get no preview and see untranslated messages.

## Building

```bash
./gradlew build
```

A JDK 25 has to be installed; Gradle picks it up on its own (`gradle/gradle-daemon-jvm.properties`).
The jar ends up in `build/libs/`.
The build runs a test that starts Fabric Loader with Create Fly and checks that the mixins still apply.
`./gradlew runClient` starts a dev client with Create Fly installed.
