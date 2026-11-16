# Create Gauge Highlight

Fabric mod for Minecraft **26.1.2** and [Create Fly](https://modrinth.com/mod/create-fly).

On a big wall of factory gauges it is hard to see where an item goes: you have to follow its arrows by eye.
With this mod you hold the **wrench**, look at a gauge, and every gauge on that wall that uses its item blinks.

## How to use it

1. Hold the wrench in either hand.
2. Look at a factory gauge that has an item set.

Every gauge that has this item linked in as an input blinks orange. The gauge you look at gets a thin white
frame, and the action bar says how many gauges use the item. Look away and it stops. Nothing is clicked and
nothing changes in the world.

## What counts

- A gauge blinks if one of its inputs comes from the gauge you look at, or from another gauge showing the
  same item (same item and same components).
- "The wall" is what Create means by it when linking gauges: gauges facing the same way in one flat plane.
  The search reaches 32 blocks along the wall from the gauge you look at.
- Gauges in chunks that are not loaded on your client are not found.

## Requirements

- Minecraft 26.1.2, Fabric Loader ≥ 0.18.4, Java 25
- Create Fly 6.0.9 for 26.1.2
- Client only. The server does not need it.

## Building

```bash
./gradlew build
```

A JDK 25 has to be installed; Gradle picks it up on its own (`gradle/gradle-daemon-jvm.properties`).
The jar ends up in `build/libs/`.
The build runs a test that starts Fabric Loader with Create Fly and checks that the mixin still applies.
`./gradlew runClient` starts a dev client with Create Fly installed.
