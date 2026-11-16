# Publishing guide: Modrinth and CurseForge

How to get the projects in this repository onto Modrinth and CurseForge, in the order that avoids rework.
The ready-to-paste listing texts (name, summary, description, categories, dependencies, changelog) are in
[`publishing/`](publishing/), one file per listing:

| Listing | File | Type |
| --- | --- | --- |
| JEI Inventory Highlight | [publishing/jei-inventory-highlight.md](publishing/jei-inventory-highlight.md) | mod |
| Create Address Autocomplete | [publishing/create-address-autocomplete.md](publishing/create-address-autocomplete.md) | mod |
| Create Chain Conveyor Mover | [publishing/create-conveyor-mover.md](publishing/create-conveyor-mover.md) | mod |
| Create Missing Recipes | [publishing/create-missing-recipes.md](publishing/create-missing-recipes.md) | datapack (the three packs in one zip) |

The three datapacks go out as **one** listing. On their own each is two or three recipe files; together they are
one sensible "Create Fly recipe fixes" pack, and modpack authors prefer one download over three.

## 1. One-time preparation in the repository

Do these before creating any listing. Both sites ask for a license and an icon, and both show the links from
`fabric.mod.json` to players.

### 1.1 Choose a license

There is no `LICENSE` file yet. Without one the code is "all rights reserved", Modrinth will not let you pick
"MIT" for the listing truthfully, and modpacks cannot legally bundle the jars.

Suggestion: **MIT**. Create, Create Fly, JEI and Fabric API are all MIT, and it lets modpacks include the mods
without conditions. Put this into `LICENSE` at the repository root (fill in the year):

```
MIT License

Copyright (c) 2026 mr-shortman

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### 1.2 Complete `fabric.mod.json` of every mod

Add the license, an icon and the issue tracker. Mod menus and both sites read these.

```json
"license": "MIT",
"icon": "assets/<mod id>/icon.png",
"contact": {
    "sources": "https://github.com/mr-shortman/Missing-Fabric-Mods",
    "issues": "https://github.com/mr-shortman/Missing-Fabric-Mods/issues",
    "homepage": "https://modrinth.com/mod/<slug>"
},
```

The `homepage` can be added after the Modrinth listing exists; the slug is in each file in `publishing/`.

### 1.3 Icons

One icon per listing, four in total. Put the mod icons at
`<mod>/src/main/resources/assets/<mod id>/icon.png` (the path from 1.2) so they are also in the jar, and the
datapack icon at `pack.png` next to `pack.mcmeta` in each pack folder.

- Square PNG, **512×512** is the sweet spot (Modrinth shows it at 96 px, CurseForge at 400 px; both accept
  larger).
- CurseForge **requires** a logo before a project can be submitted. Modrinth does not require one, but a
  listing without an icon looks abandoned in search results.
- Keep it simple: one in-game item rendered large on a flat background works, e.g. a JEI-style magnifying glass
  on a chest slot, a chain conveyor wheel, a frogport, a scoria block. Textures from Minecraft and Create may
  not be redistributed as your own art, so draw the shapes yourself or use the Minecraft screenshot route below.

### 1.4 Screenshots

Each listing needs at least one gallery image; a mod that *shows* something sells only with a picture.
Take them with `./gradlew runClient` (JEI or Create Fly is already on the dev client's classpath) at GUI scale 3
or 4, then crop:

| Listing | Pictures to take |
| --- | --- |
| JEI Inventory Highlight | A chest with highlight mode on: yellow frames, dimmed rest, a shulker box with its cyan counter, JEI search bar framed. A second one with `#tag` or `@mod` search. |
| Create Address Autocomplete | The factory gauge address box with the suggestion list open, a half-typed address filtering it. |
| Create Chain Conveyor Mover | A picked-up conveyor with the green preview, the same with a red preview and the action-bar reason, a before/after of a moved conveyor with chains. A short GIF (5–10 s) of a move is worth more than all three. |
| Create Missing Recipes | The JEI recipe pages for crushing a sheet, stonecutting dripstone and mixing scoria. |

### 1.5 Tag the versions

Both sites want a version number per file and a changelog. The `latest` GitHub release is replaced on every push,
so it is no stable reference. Tag each release instead:

```bash
git tag jei-inventory-highlight-v1.1.1
git tag create-address-autocomplete-v1.0.1
git tag create-conveyor-mover-v1.0.0
git tag create-missing-recipes-v1.0.0
git push origin --tags
```

The version in `gradle.properties` must match the tag. Use `MAJOR.MINOR.PATCH`; bump PATCH for fixes, MINOR for
features, MAJOR when a new Minecraft or Create version is required. On Modrinth and CurseForge name the file
version `<version>+mc26.1.2` (e.g. `1.1.1+mc26.1.2`), so a later `1.1.1+mc26.2` for another game version does not
collide.

### 1.6 Build the files to upload

```bash
(cd jei-inventory-highlight && ./gradlew build)
(cd create-address-autocomplete && ./gradlew build)
(cd create-conveyor-mover && ./gradlew build)
```

Upload `build/libs/<name>-<version>.jar`, **not** the `-sources.jar`. For the datapack bundle:

```bash
mkdir -p build && rm -rf build/create-missing-recipes && mkdir build/create-missing-recipes
cp -r pointed-dripstone-recipes/data sheet-recycling/data scoria-melting/data build/create-missing-recipes/
cp publishing/create-missing-recipes.pack.mcmeta build/create-missing-recipes/pack.mcmeta
(cd build/create-missing-recipes && zip -r ../create-missing-recipes-1.0.0.zip pack.mcmeta data)
```

The three packs use different namespaces, so their `data/` folders merge without conflicts. The `pack.mcmeta` for
the bundle is in `publishing/create-missing-recipes.pack.mcmeta`. Test the zip once in a world before uploading
(`/reload`, then check that `/recipe give @s sheet_recycling:crushing/iron_sheet` works).

### 1.7 Test on a clean instance

Not the dev client: a fresh Fabric 26.1.2 profile in the launcher with only Fabric API, JEI or Create Fly, and
the jar. This catches the two mistakes that get listings reported most: a dependency that was only on the dev
classpath, and a mixin that applies against the dev build of Create but not the published one. For the conveyor
mover also start a dedicated server with the jar and join with a client **without** it: the move must still work
and nothing may crash.

## 2. Modrinth

Modrinth is the smaller amount of work, has the better API and is where Fabric players look first. Do it first;
the CurseForge listing can then link to it.

### 2.1 Create the project

`https://modrinth.com/create/project`, or the "+" in the top right.

| Field | What to enter |
| --- | --- |
| Project type | Mod for the three mods, **Data pack** for Create Missing Recipes |
| Name, URL (slug), Summary | From the `publishing/` file. The summary is limited to 256 characters and is what search shows. |
| Visibility | **Private** while you fill it in; switch to Public at the end |

### 2.2 Fill in the listing

Settings → General:

- **Description**: paste the "Description" block from the `publishing/` file. Modrinth renders Markdown; images
  need an absolute URL, e.g. uploaded to the gallery first and copied from there, or a `raw.githubusercontent.com`
  link into this repository.
- **Categories**: from the file. Up to 3 can be shown on the project card, the rest only count for search.
- **Client-side / Server-side**: from the file. Get this right; it drives the "client mod" filter and the
  modpack installer's warnings.
- **License**: MIT (or whatever 1.1 ended with), link to the `LICENSE` file on GitHub.
- **Links**: Source → repository URL, Issues → `.../issues`, Discord if you have one. Wiki → the folder README on
  GitHub works well.
- **Gallery**: upload the screenshots from 1.4, mark the best as featured, give each a one-line title.

### 2.3 Upload the first version

Versions → "Upload a version" (or drag the jar onto the versions page).

| Field | Value |
| --- | --- |
| Version number | `1.1.1+mc26.1.2` style, see 1.5 |
| Version title | Can stay empty; Modrinth then shows the number |
| Release channel | Release. Use Beta while Create Fly for 26.1.2 is itself beta, if you want to signal that moves may still break |
| Loaders | Fabric. For the datapack: Data pack (and Fabric if you also offer it as a jar) |
| Game versions | 26.1.2 only. Do not tick 26.1 or 26.1.1: `fabric.mod.json` pins `~26.1.2` and the mixins were only tested there |
| Dependencies | From the file. Pick the Modrinth project, not "external". Fabric API, JEI and Create Fly all have Modrinth projects (`fabric-api`, `jei`, `create-fly`) |
| Changelog | From the file, Markdown |
| Files | The jar from 1.6. Mark it as the primary file |

Modrinth's automatic checks read `fabric.mod.json` and will complain about a missing `license` field or a
`depends` entry without a matching dependency. That is why 1.2 comes first.

### 2.4 Submit for review

Settings → General → Visibility → **Public** → Submit for review. Moderation takes up to 24–48 h; after approval
the project is live and all further versions are published instantly without review. Common rejection reasons
you can avoid: a description that is only a copy of the summary, no license, screenshots missing for a visual mod,
a dependency that is required but not listed. If the moderators ask for a change, the message appears under
Settings → Moderation.

### 2.5 After approval

- Copy the project URL into `homepage` in `fabric.mod.json` (1.2) and the folder README.
- Add the Modrinth badge to the repository README, e.g.
  `[![Modrinth](https://img.shields.io/modrinth/dt/<slug>?logo=modrinth)](https://modrinth.com/mod/<slug>)`.
- Settings → Members: add a second person only if you want them to be able to publish versions.

## 3. CurseForge

Same content, more clicking, and the first file of a project waits in a review queue. Start it right after Modrinth
so both go live around the same time.

### 3.1 Author account and project

`https://authors.curseforge.com` → Start a Project → Minecraft → **Mods** (for Create Missing Recipes:
**Data Packs**). The author account needs the project owner's e-mail verified first.

| Field | What to enter |
| --- | --- |
| Name, Summary | From the `publishing/` file (summary limit here is 250 characters) |
| Project URL | The slug; CurseForge can turn it into a different one if taken |
| Logo | **Required** here. The icon from 1.3 |
| Primary category + categories | From the file. CurseForge allows several categories; the first is primary |
| License | MIT from the dropdown. "Custom" would need a URL |
| Description | Paste the "Description" block. CurseForge's editor accepts Markdown in the "Markdown" tab; images need absolute URLs like on Modrinth |
| Source / Issues / Wiki | The same URLs as on Modrinth |

Save as draft, then Images → upload the screenshots from 1.4.

### 3.2 Upload the first file

Files → Upload file.

| Field | Value |
| --- | --- |
| File | The jar from 1.6 |
| Display name | `JEI Inventory Highlight 1.1.1 for 26.1.2` (CurseForge shows this instead of the file name) |
| Release type | Release (or Beta, as on Modrinth) |
| Game versions | 26.1.2 and **Fabric** (the loader is a game version on CurseForge) and **Java 25**. If 26.1.2 is not in the list yet, pick 26.1 and write the exact version into the changelog; CurseForge adds versions a few days after Mojang releases them |
| Changelog | From the file. The editor has a Markdown mode |
| Relations | Required dependency → `fabric-api`, `jei` or `create-fly` (search by name, these are the CurseForge slugs). For the datapack, Create Fly as **optional** is wrong, it is required for scoria and sheets: mark it required and say so in the description |

The first file goes through moderation (usually a few hours, up to a day). Only after it is approved does the
project page become visible; it is already "Approved" as a draft before that but not findable.

### 3.3 Things CurseForge does differently

- The description editor strips some Markdown (nested lists, tables inside lists). Check the preview; the texts
  in `publishing/` avoid those constructs.
- CurseForge has no "client/server side" field. The description says it instead, under "Where to install".
- "Mod loader" is chosen per file, so a NeoForge port later is just another file on the same project.
- Rewards program: opt in under project settings if you want the points; it changes nothing for players.

## 4. Automate later releases (optional, do it once the four listings exist)

Once the projects exist, `mc-publish` can upload to both sites from GitHub Actions on every tag, so a release is
`git tag … && git push --tags`. Add a job to `.github/workflows/build.yml` that runs after `build` when the ref is a
tag, per mod:

```yaml
  publish:
    name: Publish ${{ matrix.mod }}
    needs: build
    if: startsWith(github.ref, 'refs/tags/')
    runs-on: ubuntu-latest
    strategy:
      matrix:
        mod: [jei-inventory-highlight, create-address-autocomplete, create-conveyor-mover]
    steps:
      - uses: actions/download-artifact@v4
        with:
          name: ${{ matrix.mod }}
          path: jars
      - uses: Kir-Antipov/mc-publish@v3.3
        with:
          files: jars/!(*-sources).jar
          modrinth-id: <project id from the Modrinth settings page>
          modrinth-token: ${{ secrets.MODRINTH_TOKEN }}
          curseforge-id: <numeric project id from the CurseForge project page>
          curseforge-token: ${{ secrets.CURSEFORGE_TOKEN }}
          loaders: fabric
          game-versions: 26.1.2
          java: 25
          changelog-file: ${{ matrix.mod }}/CHANGELOG.md
```

`mc-publish` reads name, version and `depends` from `fabric.mod.json`, so the dependency list on both sites stays
in sync with the jar. Each mod needs its own ids, so this job wants one matrix entry with `modrinth-id` and
`curseforge-id` per mod (use `include:` in the matrix). Tokens: Modrinth → Settings → PATs with the
"Create versions" scope; CurseForge → authors.curseforge.com → API tokens. Store both as repository secrets.
Tag with the mod name in front (`create-conveyor-mover-v1.0.1`) and filter the matrix on the tag prefix, so a tag
only publishes that one mod.

## 5. Checklist per release

1. `version` in `gradle.properties` bumped, changelog written.
2. `./gradlew build` green, jar tested on a clean instance (1.7).
3. Tag pushed.
4. Upload on Modrinth (2.3) and CurseForge (3.2), or let the workflow do it.
5. Folder README and repository README table updated if the feature list changed.
